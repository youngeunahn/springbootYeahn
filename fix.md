# 코드 리뷰: exercise/plan/list.mustache

---

## 버그 (Bug)

### 1. `addExerciseRow` 내 비동기 DOM 순서 불일치
`addExerciseRow`에서 삭제 버튼과 `$row`를 먼저 `$item`에 append한 뒤, AJAX 콜백에서 `$item.prepend($topRow)`로 카테고리 버튼을 삽입함. AJAX가 지연될 경우 DOM 순서가 비동기적으로 뒤바뀜.

**해결**: 카테고리 버튼 영역을 먼저 빈 `$topRow` placeholder로 동기적으로 삽입하고, AJAX 완료 후 내용만 채우는 방식으로 변경.

---

### 2. SWIM 모드 총거리 계산 필드명 혼동
`updateTotalDistance`는 `input[name="time"]`의 합계를 계산하지만, SWIM 모드에서 `time` 필드의 실제 의미는 Distance임. 변수명과 실제 의미가 불일치.

```javascript
// 현재: name="time" 이지만 거리 값을 담음 (SWIM 모드)
rowData.planTotalDistance = $ex.find('[name=time]').val();
```

**해결**: SWIM 모드에서는 `name="distance"` 필드를 별도로 구분하거나, placeholder 텍스트처럼 `name` 속성도 `distance`로 분리.

---

### 3. `window.categoryCodes` 선언 후 미사용
라인 75~104에서 `/api/codes/TPL_CATEGORY_CODE` API 호출 및 fallback으로 `window.categoryCodes`를 세팅하지만, 실제 `addExerciseRow`에서는 `/api/exercise/templates`를 직접 호출하여 카테고리를 다시 가져옴. `window.categoryCodes`는 어디서도 참조되지 않음.

**해결**: 해당 코드 블록 전체 제거.

---

## 성능 (Performance)

### 4. 운동 행 추가마다 AJAX 반복 호출
`addExerciseRow`가 호출될 때마다 `/api/exercise/templates` POST 요청이 발생. 수정 폼을 열 때 운동 항목이 10개면 10번 호출됨.

**해결**: `openPlanForm` 진입 시 코드 데이터를 1회 로딩하여 캐싱 후 `addExerciseRow`에 파라미터로 전달.

---

### 5. 검색 입력 시 디바운스 없음
`#tplLibSearch`의 `keyup` 이벤트마다 서버 API(`/exercise/plan/search-exercises`)를 즉시 호출. 빠르게 타이핑 시 다수의 요청이 발생.

```javascript
// 현재
$('#tplLibSearch').on('keyup', function() { ... $.get(...) });
```

**해결**: 300ms 디바운스 적용.

```javascript
let searchTimer;
$('#tplLibSearch').on('keyup', function() {
    clearTimeout(searchTimer);
    searchTimer = setTimeout(() => { /* API 호출 */ }, 300);
});
```

---

## 미구현 기능 (Incomplete)

### 6. 왼쪽 패널 기간 검색 미구현
```javascript
$('#btnSearchApply').on('click', function() { alert('검색 실행'); });
```
검색 버튼이 `alert`만 호출하고 실제 필터링 없음. `#searchPlanName`, `#searchStartDate`, `#searchEndDate` 값도 사용되지 않음.

---

### 7. 과거 기록 탭 미구현
```html
<div id="recordListOrigin">
    <!-- 더미 데이터 -->
</div>
```
탭이 존재하지만 데이터 로딩 로직 없음. 퀵 필터 버튼도 `showToast`만 출력.

---

## 코드 품질 (Code Quality)

### 8. `console.log` 운영 코드에 잔류
```javascript
console.log('Category codes loaded from server:', window.categoryCodes); // line 79
console.log('Fallback category codes loaded:', window.categoryCodes);    // line 103
console.log('Current typeCode:', typeCode);                              // line 410
```
프로덕션 배포 전 제거 필요.

---

### 9. `alert` / `showToast` 혼용
저장 성공·실패, 삭제 성공 모두 `alert()`를 사용하는 반면, 운동 담기 성공에는 `showToast()`를 사용함. `showToast()`가 이미 정의되어 있으므로 전체 통일 권장.

```javascript
// 현재 (혼용)
alert('운동 계획이 저장되었습니다.');
showToast('...');

// 개선: alert → showToast 통일
```

---

### 10. 거대한 단일 `<script>` 블록
전체 JS 로직(약 760줄)이 하나의 `<script>` 태그 안에 인라인으로 존재. 함수명이나 변수가 충돌 위험 있고 유지보수 어려움.

**장기 개선 방향**: `plan.js` 외부 파일로 분리, 관련 함수를 `PlanForm`, `PlanView`, `Library` 등 네임스페이스로 묶기.

---

## 요약

| 구분 | 건수 |
|---|---|
| 버그 | 3 |
| 성능 | 2 |
| 미구현 | 2 |
| 코드 품질 | 3 |
| **합계** | **10** |

우선순위: **버그 → 성능(4번) → 미구현 → 품질** 순으로 대응 권장.
