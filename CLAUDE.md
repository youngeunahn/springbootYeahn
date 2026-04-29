# CLAUDE.md

이 파일은 Claude Code(claude.ai/code)가 이 저장소에서 작업할 때 참고하는 가이드입니다.

작업 영역별 상세 지침은 `.claude/agents/` 아래의 전문 에이전트 파일을 참조합니다:

| 에이전트 | 파일 | 사용 시점 |
|---|---|---|
| springboot-expert | `.claude/agents/springboot-expert.md` | 서비스 계층, 트랜잭션, Controller/Security 구현 |
| mybatis-expert | `.claude/agents/mybatis-expert.md` | Mapper XML, resultMap, 동적 SQL 작성 |
| db-expert | `.claude/agents/db-expert.md` | 스키마 설계, 인덱스, ERD |
| frontend-expert | `.claude/agents/frontend-expert.md` | Mustache 템플릿, jQuery, Bootstrap 4 구현 |
| design-expert | `.claude/agents/design-expert.md` | UI/UX 디자인 감수, 시각적 일관성 |
| test-expert | `.claude/agents/test-expert.md` | 단위/통합 테스트 작성, CI 실패 분석 |
| github-actions-expert | `.claude/agents/github-actions-expert.md` | CI/CD 워크플로우 수정, 배포 파이프라인 |

## 빌드 & 실행

```bash
# 빌드 (테스트 생략)
mvn -B package -DskipTests

# 로컬 실행 (application.properties 사용, spring.environment=local)
mvn spring-boot:run

# 특정 프로파일로 실행
mvn spring-boot:run -Dspring.profiles.active=test
```

## 테스트

테스트 소스는 `src/test/java/`가 아닌 `test/main/java/` 아래에 위치하며, `build-helper-maven-plugin`으로 설정되어 있습니다.

```bash
# 전체 테스트 실행 (MariaDB 필요, application-test.properties 사용)
mvn test -Dspring.profiles.active=test

# 특정 테스트 클래스 실행
mvn test -Dspring.profiles.active=test -Dtest=PlanServiceIntegrationTest

# 특정 테스트 메서드 실행
mvn test -Dspring.profiles.active=test -Dtest=PlanServiceIntegrationTest#createAndGetPlan_Integration_Test
```

통합 테스트(`@SpringBootTest + @Transactional`)는 실제 MariaDB에 연결합니다. CI 워크플로우는 Docker로 MariaDB를 실행한 뒤 `src/main/resources/schema.sql`을 적용합니다. 로컬 실행 시에는 `application.properties`의 DB에 접근 가능해야 합니다.

> 테스트 작성 및 CI 실패 분석 상세 지침 → `.claude/agents/test-expert.md`

## 아키텍처

**기술 스택:** Spring Boot 2.6.11, Java 8, MyBatis, MariaDB, Mustache 템플릿, Spring Security

### 패키지 구조

| 패키지 | 역할 |
|---|---|
| `com.yeahn.security` | Security 설정, `AccessLogFilter`, `SessionAuthenticationFilter`, 로그인 핸들러 |
| `com.yeahn.auth` | `UserService` (Spring Security `UserDetailsService`), 로그인 컨트롤러 |
| `com.yeahn.config` | `WebMvcConfigurer` (인터셉터 등록), `WebInterceptor`, `CacheConfig`, 파일 업로드 설정 |
| `com.yeahn.menu` | 메뉴 CRUD + Caffeine 캐시 적용 `getMenuList` (TTL 1시간) |
| `com.yeahn.log` | `AccessLogFilter`로 모든 HTTP 요청 저장, `LogService`로 로그인 로그 저장 |
| `com.yeahn.plan` | 운동 계획 마스터-디테일 CRUD (TB_PLAN / TB_PLAN_DETAIL) |
| `com.yeahn.template` | 운동 템플릿 관리 |
| `com.yeahn.common` | `S3Uploader` (IBM COS), `ExcelUtils`, `CodeService` (공통 코드 조회), `CommonUtils` |
| `com.yeahn.otp` | Google Authenticator 호환 TOTP + QR 코드 생성 |
| `com.yeahn.yetable` | 범용 데이터 테이블 + 엑셀 다운로드 |

MyBatis 매퍼 XML 파일: `src/main/resources/query/mapper/**/*.xml`  
Mustache 뷰 템플릿: `src/main/resources/templates/`

### 요청 처리 흐름

1. **`AccessLogFilter`** (`OncePerRequestFilter`) — 가장 먼저 실행. URI, IP, User-Agent, 응답 시간을 DB에 기록 (정적 자산 제외).
2. **Spring Security** — 폼 로그인 처리. `ROLE_ADMIN` 접근 제어, CSRF 비활성화.
3. **`WebInterceptor`** (`postHandle`) — 모든 `ModelAndView`에 `MenuList`와 `MenuPage` 자동 주입 (`/ajax/**`, `/api/**` 제외).

### 프로파일 & 설정

| 프로파일 | 파일 | 용도 |
|---|---|---|
| *(없음 / 기본)* | `application.properties` | 로컬 개발용; DB 및 경로 하드코딩 |
| `test` | `application-test.properties` | CI 및 통합 테스트용; 민감 정보는 환경 변수로 주입 |
| `prod` | Cloudtype 환경 변수로 해결 | 운영 배포용 |

`spring.environment` (`local` / `development` / `prod`) 값에 따라 `S3Uploader`의 파일 경로 동작이 달라집니다.

### 데이터 패턴

- **소프트 삭제** — 운동 계획/템플릿 테이블은 물리 삭제 대신 `DEL_YN = 'Y'`를 사용합니다.
- **공통 코드 테이블** — `CodeService` + `TB_COMM_CODE` / `TB_COMM_CLASS`가 운동 유형, 단계, 카테고리 등 모든 타입 코드의 중앙 조회 창구입니다. DB 함수 `FN_GET_COMM_CODE_DESC`가 SQL 내에서 코드 → 설명 변환을 담당합니다.
- **마스터-디테일 저장** — `PlanService.savePlan`은 마스터 행을 upsert한 뒤, 디테일 행을 전체 교체합니다 (제거된 항목은 소프트 삭제, 새 항목은 정렬 순서를 재계산하여 삽입).

### CI/CD

GitHub Actions (`.github/workflows/ci.yml`)이 `master` 브랜치 push/PR마다 실행됩니다:
1. MariaDB 서비스 시작 → `schema.sql` 적용 → `FN_GET_COMM_CODE_DESC` 함수 생성.
2. `mvn -B package -DskipTests`로 빌드.
3. `mvn test -Dspring.profiles.active=test`로 테스트 실행.
4. `master` 머지 시 시크릿(`CLOUDTYPE_TOKEN`, `GHP_TOKEN`, DB/COS 자격증명)을 사용해 Cloudtype에 자동 배포.

> CI/CD 워크플로우 수정 상세 지침 → `.claude/agents/github-actions-expert.md`

---

## 코딩 가이드라인

### 백엔드 (Spring Boot / Java)
> 상세 지침 → `.claude/agents/springboot-expert.md`

- **Java 8만 사용** — Java 9+ 문법(var, records, sealed class 등) 사용 금지.
- **소프트 삭제 조회** — 모든 조회 쿼리에는 반드시 `DEL_YN = 'N'` 조건 포함.

### MyBatis
> 상세 지침 → `.claude/agents/mybatis-expert.md`

### DB 스키마
> 상세 지침 → `.claude/agents/db-expert.md`

- **UDF 주입** — `DELIMITER`가 포함된 함수는 Spring `schema.sql` 자동 실행 불가. CI에서는 `mysql` CLI로 직접 주입.

### 프론트엔드 (Mustache / jQuery / Bootstrap 4)
> 구현 상세 → `.claude/agents/frontend-expert.md` / 디자인 감수 → `.claude/agents/design-expert.md`

- **피드백** — 사용자 액션 결과는 `alert()` 대신 `showToast()`로 통일.

### 테스트
> 상세 지침 → `.claude/agents/test-expert.md`

- **테스트 소스 위치** — `test/main/java/` (비표준 경로).
- **CI 실패 진단** — `Failed to load ApplicationContext` 발생 시 `mvn test -e -X`로 상세 로그 확인.
