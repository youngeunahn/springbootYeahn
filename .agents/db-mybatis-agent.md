# db-mybatis-agent

## Role

이 에이전트는 MyBatis XML, Mapper 인터페이스, MariaDB SQL, `schema.sql` 변경을 담당한다.

주요 범위는 `src/main/resources/query/mapper`, `src/main/resources/schema.sql`, 관련 Mapper 인터페이스다.

## Good Tasks

- `CommonMapper`, `ConfigMapper`, `LoginMapper`, `LogMapper`, `PlanMapper`, `TemplateMapper` SQL/XML 수정
- Mapper 인터페이스와 XML namespace/id 매칭 점검
- `DEL_YN`, `USE_YN` 조건 유지
- MariaDB 전용 쿼리, 함수, full-text 검색, recursive query 점검
- 테스트용 schema 또는 seed 데이터 점검

## Project Patterns

- Mapper 방식이 혼재되어 있다.
- `UserMapper`, `PlanMapper`, `TemplateMapper`는 `@Mapper` 인터페이스 방식이다.
- `CodeMapper`, `LogMapper`, `MenuMapper`는 `SqlSessionTemplate` 문자열 호출 방식이다.
- XML `namespace`가 인터페이스 FQCN인 경우와 단순 문자열인 경우가 섞여 있으므로 호출 방식을 먼저 확인한다.
- 대부분 업무 테이블은 `USE_YN`, `DEL_YN`을 사용한다.
- 조회 기본 조건은 대체로 `DEL_YN = 'N'`, 활성 데이터는 `USE_YN = 'Y' AND DEL_YN = 'N'`이다.
- 삭제는 대부분 물리 삭제가 아니라 `UPDATE ... SET DEL_YN = 'Y'`다.
- 신규 insert 기본값은 보통 `USE_YN = 'Y'`, `DEL_YN = 'N'`, `INS_DT = NOW()`다.
- update/delete 시 감사 컬럼 `UPD_DT`, `UPD_IP`, `UPD_USER_ID` 갱신 패턴을 확인한다.

## SQL Notes

- `TemplateMapper`는 `FN_GET_COMM_CODE_DESC(...)`, `MATCH ... AGAINST`, `GROUP BY`, `foreach IN`, `CASE WHEN` 일괄 정렬 업데이트를 사용한다.
- `ConfigMapper`는 MariaDB/MySQL `WITH RECURSIVE` 메뉴 트리 쿼리를 사용한다.
- `PlanMapper`는 명시적 `resultMap`을 사용하며 날짜를 `DATE_FORMAT(..., '%Y-%m-%d')`로 반환하는 구간이 있다.
- 단일 파라미터 SQL에서 `parameterType="int/long"`와 `#{planSeq}`, `#{tplSeq}` 같은 이름 참조가 섞일 수 있으므로 인터페이스 바인딩과 런타임 동작을 확인한다.
- `TB_EXER` 관계 테이블은 `deleteRelationByTplSeq`에서 실제 `DELETE`를 사용하는 예외가 있다.

## Risks

- soft delete 조건 누락은 삭제 데이터 노출, 중복 카운트, 수정 대상 오염으로 이어질 수 있다.
- MariaDB 함수 `FN_GET_COMM_CODE_DESC`에 의존하는 쿼리가 있다.
- H2 대체 실행은 `WITH RECURSIVE`, `MATCH AGAINST`, MySQL/MariaDB 함수/문법 때문에 깨질 가능성이 높다.
- `schema.sql`은 주석/문자열 인코딩이나 COMMENT 구간 문법 리스크를 확인해야 한다.
- 템플릿/운동 관계는 `TB_EXER` 물리 삭제와 `TB_EXER_ATTR` soft delete가 섞여 있다.
- `Plan` 저장/수정은 master/detail을 함께 다루므로 detail soft delete 후 재삽입/수정 흐름을 확인한다.

## Test Notes

- 통합 테스트는 MariaDB와 `schema.sql` 스키마 객체가 필요할 수 있다.
- 쿼리 변경 후 `mvn test -Dspring.profiles.active=test` 또는 관련 통합 테스트를 우선 고려한다.

