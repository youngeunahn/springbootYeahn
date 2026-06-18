# backend-domain-agent

## Role

이 에이전트는 Java 21, Spring Boot 3.5.14 기반 백엔드 도메인 변경을 담당한다.

주요 범위는 `src/main/java/com/yeahn` 아래의 `plan`, `template`, `menu`, `yetable`, `auth`, `security` 패키지다.

## Good Tasks

- `plan`: 운동 계획 CRUD, 계획 상세 항목 정렬/저장, 검색 조건 추가, `TB_PLAN`/`TB_PLAN_DETAIL` MyBatis 쿼리 개선
- `template`: 운동 템플릿 마스터, 운동 항목, 관계 테이블 처리, 검색 필터, 사용자 공개 API(`/api/user/templates`) 확장
- `menu`: 메뉴 트리 조회, 메뉴 설정 AJAX 수정, 캐시 무효화 포함 메뉴 관리
- `yetable`: 게시판성 예제 기능, 엑셀 다운로드, S3 이미지 업로드 보조
- `auth/security`: 로그인/회원가입, Spring Security 설정, 로그인/접근 로그, 공개 API 권한 범위 조정

## Project Patterns

- Java 21 기준으로 컴파일하되, 기존 코드 스타일과 패키지 구조를 우선 따른다.
- 도메인 흐름은 대체로 `controller -> service -> mapper/dao -> XML` 구조다.
- `plan`, `template`, `auth`는 MyBatis `@Mapper` 인터페이스와 XML namespace를 맞춘다.
- `menu`, `common.code`는 `SqlSessionTemplate`으로 문자열 mapper id를 호출한다.
- 저장/수정/삭제는 서비스에 `@Transactional(rollbackFor = Exception.class)`를 두는 패턴이 많다.
- 로그인 사용자 ID는 `SecurityContextHolder.getContext().getAuthentication().getName()`을 사용한다.
- IP 감사 필드는 `CommonUtils.getIP(request)`를 사용한다.
- 삭제는 대부분 `DEL_YN = 'Y'` soft delete를 유지한다.
- 화면 컨트롤러는 Mustache view name을 반환하고, AJAX/API는 `@ResponseBody` 또는 `@RestController`를 사용한다.
- 코드 목록은 `CodeService.getCodeList(new CodeDto(...), selected)` 패턴을 따른다.
- `TemplateService`처럼 master/detail/relation을 함께 저장하는 서비스는 한 트랜잭션에서 마스터 저장, 상세 저장, 관계 매핑을 처리하는 흐름을 유지한다.
- 수정 API는 ID 존재 여부에 따라 insert/update가 갈리는 구간이 있으므로 신규/기존 상세 항목을 분리해 검증한다.
- 수영 모드(`typeCode = 'SWIM'`)의 `cycle` 필드는 해당 모드에서만 저장/검증되는지 확인한다.
- S3/COS 연동은 `S3Uploader`, `S3Config`의 기존 설정 방식과 테스트 대체 방식을 먼저 확인한다.

## Risks

- `plan` 상세 저장은 수정 시 기존 상세를 전부 `DEL_YN='Y'` 처리한 뒤 전달된 상세를 update/insert한다. 프론트 payload 누락은 상세 삭제처럼 보일 수 있다.
- `template`은 `TB_EXER_TPL`, `TB_EXER_ATTR`, `TB_EXER` 관계를 함께 다룬다. 상세 삭제와 관계 삭제 순서, soft delete와 physical delete 차이를 유지해야 한다.
- `template` 검색은 MariaDB full-text `MATCH ... AGAINST`를 사용하므로 DB 호환성과 인덱스에 민감하다.
- `menu`는 캐시(`getMenuList`)가 있고 update에는 `@CacheEvict`가 있으나 insert에는 캐시 무효화가 없을 수 있다.
- `SecurityConfig`는 CSRF가 꺼져 있고, 명시한 공개 API만 `permitAll`이며 그 외 `/api/user/**`는 기본 JWT 인증 대상이다. 공개 API를 추가할 때는 개인정보와 쓰기 API 노출 여부를 확인하고, `PublicUserApiPaths`에 method와 path를 명시한다. 공개로 명시하지 않은 `/api/user/**` API는 보호 API로 둔다.
- 회원가입 기본 권한이 `ROLE_ADMIN`으로 들어가는 흐름이 있으므로 권한 정책 변경은 영향이 크다.
- `yetable`은 JPA Repository 기반이고 핵심 도메인은 MyBatis 기반이다. 구조를 혼동하지 않는다.
- 일부 소스 주석/문자열은 인코딩이 깨져 보일 수 있다. 불필요한 인코딩 변경 diff를 만들지 않는다.

## Test Notes

- IP stub은 루프백이 아닌 값, 예: `"10.10.10.10"`을 사용한다.
- S3 관련 `@MockBean`은 타입만 지정하지 말고 빈 이름을 지정한다.

