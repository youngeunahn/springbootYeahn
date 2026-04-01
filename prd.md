# [PRD] 상세 운동 구성 순서 변경 기능 (Server-side)

## 1. 개요
현재 `exercise/template/list.mustache` 화면에서 jQuery UI Sortable을 통해 상세 조회된 운동 리스트의 순서를 UI상에서 변경할 수 있습니다. 하지만 변경된 순서가 서버에 저장되지 않아 새로고침 시 초기화됩니다. 본 기능은 변경된 순서를 DB(`TB_EXER` 테이블의 `SORT_ORDER`)에 반영하는 것을 목표로 합니다.

## 2. 사용자 시나리오
1. 관리자가 특정 템플릿의 상세 정보를 조회합니다.
2. 상세 조회된 운동 리스트에서 왼쪽의 핸들(번호 배지)을 잡고 드래그하여 순서를 바꿉니다.
3. 드래그가 끝나는 시점에 시스템은 변경된 순서 정보를 서버로 전송합니다.
4. 서버는 해당 정보를 바탕으로 DB의 순서(`SORT_ORDER`)를 업데이트합니다.
5. 업데이트 완료 후 사용자에게 성공 메시지(또는 토스트 알림)를 표시합니다.

## 3. 기술적 요구사항

### 3.1 Frontend (기존 mustache 수정)
- **이벤트 핸들링**: `sortable`의 `update` 콜백 함수 내에서 정렬된 후의 시퀀스 ID 리스트를 추출합니다.
- **데이터 구조**: `{ tplSeq: 1, exercises: [{ tplExerSeq: 101, tplSortOrder: 1 }, ...] }` 형태의 JSON 데이터를 구성합니다.
- **API 호출**: `POST /api/exercise/templates/reorder` 비동기 호출.

### 3.2 Backend (Java/Spring)
- **Controller**:
    - 순서 변경 요청을 받을 `@PostMapping` 엔드포인트를 추가합니다.
- **Service**:
    - `@Transactional` 내에서 동작하며, 리스트로 받은 각 운동 매핑 항목의 `SORT_ORDER` 값을 업데이트합니다.
- **Mapper (MyBatis)**:
    - `TB_EXER` 테이블의 `SORT_ORDER`를 업데이트하는 쿼리를 작성합니다.

### 3.3 Database
- **대상 테이블**: `TB_EXER` (템플릿-운동 속성 매핑 테이블)
- **수정 컬럼**: `SORT_ORDER` (또는 `TPL_SORT_ORDER`)

## 4. 상세 설계 (예시)

### 4.1 API 명세
- **Endpoint**: `POST /api/exercise/templates/reorder`
- **Request Body**:
```json
{
  "tplSeq": 123,
  "exercises": [
    { "tplExerSeq": 501, "tplSortOrder": 1 },
    { "tplExerSeq": 502, "tplSortOrder": 2 }
  ]
}
```

## 5. 유의사항
- **트랜잭션**: 업데이트 도중 오류 발생 시 전체 롤백되어야 합니다.
- **성능**: 대량의 운동이 포함된 경우 MyBatis의 `<foreach>`를 이용한 Batch Update를 고려합니다.
