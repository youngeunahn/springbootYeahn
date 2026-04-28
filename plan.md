# 실행 계획: list.mustache 개선

fix.md의 10개 항목을 단계별로 수정한다.  
우선순위: 버그(1~3) → 성능(4~5) → 코드 품질(8~9) → 미구현(6~7) → 장기(10)

---

## STEP 1 — 버그 수정 (list.mustache)

### 1-A. `addExerciseRow` 비동기 DOM 순서 수정

**문제**: AJAX 콜백이 늦게 응답할 경우 카테고리 버튼 영역(`$topRow`)이 삭제 버튼·입력 행 아래에 삽입됨.

**수정 위치**: `list.mustache` > `addExerciseRow` 함수

**수정 방법**:
1. AJAX 호출 전에 `$topRow`를 빈 div로 먼저 동기적으로 `$item`에 prepend
2. AJAX 성공 콜백에서 `$topRow`의 내용만 채우기

```javascript
// Before: AJAX 콜백 안에서 $item.prepend($topRow)
// After:
const $topRow = $('<div/>', { class: 'mb-2 d-flex flex-wrap align-items-center' });
$item.prepend($topRow); // 먼저 DOM에 삽입

$.ajax({
    ...
    success: function(codeData) {
        // $topRow 내용만 채우기 (prepend 없음)
        if (typeCode === 'GYM' && codeData.tplKind) { ... $topRow.append($kindSelect); }
        $topRow.append($partGroup);
    }
});
```

---

### 1-B. SWIM 모드 `name="time"` 필드명 혼동 수정

**문제**: SWIM 모드에서 Distance 값을 `name="time"` 필드로 수집하여 혼동 발생.  
`updateTotalDistance`도 `[name="time"]`을 합산하므로 GYM의 시간 입력값과 충돌 위험.

**수정 위치**: `list.mustache` > `addExerciseRow`, `addPhaseBlock`, `updateTotalDistance`, 저장 로직

**수정 방법**:
- SWIM 모드에서 Distance 입력 필드의 `name`을 `distance`로 변경
- `updateTotalDistance`를 SWIM/GYM 분기 처리

```javascript
// addExerciseRow 내 non-cycle 모드 SWIM 분기
{ cls: 'col-3', name: typeCode === 'SWIM' ? 'distance' : 'time', ph: typeCode === 'SWIM' ? 'Distance' : 'Time/Dist', ... }

// 저장 로직 수정
if (typeCode === 'SWIM') {
    rowData.planTime = $ex.find('[name=cycle]').val();
    rowData.planTotalDistance = $ex.find('[name=distance]').val(); // time → distance
} else {
    rowData.planTime = $ex.find('[name=time]').val();
}

// updateTotalDistance 수정
function updateTotalDistance($form) {
    let total = 0;
    const selector = typeCode === 'SWIM' ? 'input[name="distance"]' : 'input[name="time"]';
    $form.find(selector).each(function() { total += parseInt($(this).val()) || 0; });
    $form.find('#totalDistance').text(total.toLocaleString());
}
```

- Cycle 모드(`isCycleMode`)에서도 동일하게 `distance` 적용

---

### 1-C. `window.categoryCodes` 미사용 코드 제거

**수정 위치**: `list.mustache` 라인 75~104

**수정 방법**: 아래 코드 블록 전체 제거
```javascript
// 제거 대상: window.categoryCodes 세팅 블록 전체
window.categoryCodes = [];
$.get('/api/codes/TPL_CATEGORY_CODE', function(data) { ... });
function setFallbackCategories() { ... }
```

---

## STEP 2 — 성능 개선 (list.mustache)

### 2-A. `addExerciseRow` 내 AJAX 중복 호출 제거

**문제**: `addExerciseRow` 호출마다 `/api/exercise/templates` POST 요청 발생.

**수정 위치**: `list.mustache` > `openPlanForm`, `addExerciseRow`

**수정 방법**:
1. `openPlanForm` 진입 시 1회 코드 데이터 로딩 후 Promise 또는 변수로 캐싱
2. `addExerciseRow(container, data, codeData)` 시그니처로 변경하여 캐싱된 데이터 주입

```javascript
function openPlanForm(data) {
    ...
    // 코드 데이터 1회 로딩
    $.ajax({
        url: '/api/exercise/templates',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({ ref1: typeCode }),
        success: function(codeData) {
            // 폼 초기화 후 addExerciseRow에 codeData 전달
            if (!isUpdate) {
                addPhaseBlock($form.find('.phase-list'), '본운동', codeData);
            } else {
                // 수정 데이터 복원
                Object.keys(phases).forEach(pCode => {
                    const $exList = addPhaseBlock(..., codeData);
                    phases[pCode].forEach(ex => addExerciseRow($exList, ex, codeData));
                });
            }
        }
    });
}

function addExerciseRow($container, data, codeData) {
    // AJAX 제거, codeData로 직접 DOM 구성
    buildCategoryButtons($topRow, codeData);
}
```

---

### 2-B. 검색창 디바운스 추가

**수정 위치**: `list.mustache` > `#tplLibSearch` keyup 핸들러

```javascript
let searchTimer;
$('#tplLibSearch').on('keyup', function() {
    const kw = $(this).val().trim().toLowerCase();

    // 로컬 필터 (즉시 실행)
    $('#recordListOrigin .record-group').each(function() {
        $(this).toggle($(this).text().toLowerCase().indexOf(kw) > -1);
    });

    if (!kw) {
        $('#tplListOrigin').show();
        $('#tplSearchResult').hide();
        return;
    }

    // 서버 API 호출에만 디바운스 적용
    clearTimeout(searchTimer);
    searchTimer = setTimeout(function() {
        $.get('/exercise/plan/search-exercises', { keyword: kw, typeCode: typeCode }, function(data) {
            // 기존 렌더링 로직
        });
    }, 300);
});
```

---

## STEP 3 — 코드 품질 (list.mustache)

### 3-A. `console.log` 3곳 제거

| 위치 | 내용 |
|---|---|
| 라인 79 | `console.log('Category codes loaded from server:', ...)` |
| 라인 103 | `console.log('Fallback category codes loaded:', ...)` |
| 라인 410 | `console.log('Current typeCode:', typeCode)` |

STEP 1-C와 함께 처리 (75~104 블록 제거 시 79, 103은 자동 제거).

---

### 3-B. `alert` → `showToast` 전면 교체

**수정 위치**: `list.mustache` 내 `alert()` 호출 전체

| 현재 | 변경 |
|---|---|
| `alert('운동 계획이 저장되었습니다.')` | `showToast('운동 계획이 저장되었습니다.')` |
| `alert('저장 중 오류가 발생했습니다.')` | `showToast('저장 중 오류가 발생했습니다.')` (스타일 `alert-danger`로 변경) |
| `alert('삭제되었습니다.')` | `showToast('삭제되었습니다.')` |
| `alert('작성 폼을 먼저 열어주세요.')` | `showToast('작성 폼을 먼저 열어주세요.')` |
| `alert('추가할 운동을 먼저 선택해주세요.')` | `showToast('추가할 운동을 먼저 선택해주세요.')` |
| `alert('검색 실행')` | 실제 기능으로 대체 (STEP 4-A) |

`showToast`에 `type` 파라미터 추가로 성공/오류 색상 분기:
```javascript
function showToast(msg, type = 'info') {
    const cls = `alert alert-${type} position-fixed fixed-bottom m-3 shadow`;
    ...
}
```

확인이 필요한 삭제 다이얼로그는 `confirm()` 유지.

---

## STEP 4 — 미구현 기능

### 4-A. 왼쪽 패널 기간 검색 구현

**수정 범위**: `list.mustache` + `PlanVo.java` + `PlanMapper.xml` + `PlanController.java`

**① PlanVo.java에 검색 필드 추가**
```java
private String planNameSearch;  // 계획명 검색어
private String searchStartDate; // 검색 시작일
private String searchEndDate;   // 검색 종료일
```

**② PlanMapper.xml — getPlanList에 조건 추가**
```xml
<if test="planNameSearch != null and planNameSearch != ''">
    AND PLAN_NAME LIKE CONCAT('%', #{planNameSearch}, '%')
</if>
<if test="searchStartDate != null and searchStartDate != ''">
    AND PLAN_DATE >= #{searchStartDate}
</if>
<if test="searchEndDate != null and searchEndDate != ''">
    AND PLAN_DATE &lt;= #{searchEndDate}
</if>
```

**③ PlanController.java — searchPlanList 엔드포인트 추가**
```java
@GetMapping("/search")
@ResponseBody
public List<PlanVo> searchPlanList(@ModelAttribute PlanVo planVo) {
    return planService.getPlanList(planVo);
}
```

**④ list.mustache — btnSearchApply 핸들러 구현**
```javascript
$('#btnSearchApply').on('click', function() {
    const params = {
        planNameSearch: $('#searchPlanName').val(),
        searchStartDate: $('#searchStartDate').val(),
        searchEndDate: $('#searchEndDate').val(),
        planTypeCode: typeCode
    };
    $.get('/exercise/plan/search', params, function(data) {
        renderPlanList(data); // 목록 갱신 함수로 분리
    });
});
```

---

### 4-B. 과거 기록 탭 구현

**수정 범위**: `list.mustache` (서버 API 재사용 — 별도 엔드포인트 불필요)

**방침**: 기존 `/exercise/plan/search` 엔드포인트를 퀵 필터에도 재사용.

**① 퀵 필터 버튼 핸들러 구현**
```javascript
$(document).on('click', '.btn-quick-filter', function() {
    const now = new Date();
    const endDate = now.toISOString().split('T')[0];
    const days = $(this).data('days'); // data-days="7" 또는 "30"
    const startDate = new Date(now - days * 86400000).toISOString().split('T')[0];

    $.get('/exercise/plan/search', { searchStartDate: startDate, searchEndDate: endDate, planTypeCode: typeCode }, function(data) {
        renderRecordList(data);
    });

    $(this).siblings().removeClass('active');
    $(this).addClass('active');
});
```

**② HTML 수정**: 퀵 필터 버튼에 `data-days` 속성 추가
```html
<button class="btn-quick-filter" data-days="7">최근 1주</button>
<button class="btn-quick-filter" data-days="30">최근 1달</button>
```

**③ renderRecordList 함수 추가**: 과거 기록을 `#recordListOrigin`에 체크박스 형태로 렌더링.

---

## STEP 5 — 장기 개선 (별도 PR)

### 5-A. JS 외부 파일 분리

인라인 `<script>` 760줄을 `static/js/plan.js`로 분리.  
함수를 네임스페이스로 묶어 전역 오염 방지:

```javascript
const PlanApp = {
    View: { render, renderPlanView },
    Form: { open, addPhaseBlock, addExerciseRow },
    Library: { renderTemplateList, renderRecordList }
};
```

---

## 수정 파일 요약

| 파일 | STEP |
|---|---|
| `templates/exercise/plan/list.mustache` | 1-A, 1-B, 1-C, 2-A, 2-B, 3-A, 3-B, 4-A④, 4-B |
| `plan/dto/PlanVo.java` | 4-A① |
| `query/mapper/plan/PlanMapper.xml` | 4-A② |
| `plan/controller/PlanController.java` | 4-A③ |

---

## 작업 순서

```
STEP 1 (버그) → STEP 2 (성능) → STEP 3 (품질) → STEP 4 (미구현) → STEP 5 (장기)
```

- STEP 1~3은 `list.mustache`만 수정. 서버 변경 없음.
- STEP 4-A 진행 전에 서버 측(VO, XML, Controller) 먼저 수정 후 프론트 연동.
- STEP 4-B는 STEP 4-A의 `/exercise/plan/search` 엔드포인트 완성 후 진행.
