# Repository Guidelines

## 프로젝트 구조와 모듈 구성

이 저장소는 Java 8 기반의 Maven Spring Boot 2.6.11 애플리케이션입니다. 주요 Java 코드는 `src/main/java/com/yeahn` 아래에 있으며 `auth`, `security`, `plan`, `template`, `menu`, `yetable`, `log`, `config`, `common` 패키지로 나뉩니다. Mustache 화면은 `src/main/resources/templates`, 정적 CSS/JS/이미지는 `src/main/resources/static`, MyBatis XML 매퍼는 `src/main/resources/query/mapper`에 있습니다. DB 초기 스키마는 `src/main/resources/schema.sql`을 사용합니다. 테스트 코드는 표준 경로가 아닌 `test/main/java`에 있으며 Maven 설정으로 포함됩니다. DB 접근은 MariaDB, MyBatis 3.4.6, log4jdbc 기반 SQL 로깅을 전제로 합니다.

별도 프론트엔드 프로젝트는 `D:\Projects\yeahn-fitbase`에 있습니다. 이 프론트엔드는 Next.js 기반이며 이 백엔드의 `/api/user/**` 공개 API를 사용합니다. 백엔드 공개 API 응답은 기존 `ResponseDto<T>` 규격(`status`, `message`, `data`, `SUCCESS`/`FAIL`)을 유지하고, 프론트 호출부는 `response.data`를 실제 payload로 사용하도록 맞춥니다.

## 빌드, 테스트, 개발 명령

- `mvn -B package -DskipTests`: 테스트 없이 애플리케이션 jar를 빌드합니다.
- `mvn spring-boot:run`: 기본 설정으로 로컬 서버를 실행합니다.
- `mvn spring-boot:run -Dspring.profiles.active=local`: 로컬 `application-local.properties` DB/COS 설정으로 실행합니다.
- `mvn test -Dspring.profiles.active=local`: 로컬 `application-local.properties` DB/COS 설정으로 전체 테스트를 실행합니다.
- `mvn test -Dspring.profiles.active=local -Dtest=PlanServiceIntegrationTest`: 로컬 `application-local.properties`로 특정 테스트 클래스만 실행합니다.
- `mvn test -Dspring.profiles.active=test`: CI/GitHub Actions 전용 test profile로 전체 테스트를 실행합니다.

로컬 통합 테스트는 반드시 `local` 프로파일을 명시해 `application-local.properties`의 DB/COS 설정을 사용합니다. 테스트 JVM은 `Application.main()`의 `spring.profiles.default=local` 설정을 거치지 않으므로, `mvn test`만 실행하면 `application.properties`의 환경 변수 placeholder가 해소되지 않을 수 있습니다. CI는 `application-test.properties`와 GitHub Actions MariaDB 서비스를 사용합니다.

## 코딩 스타일과 명명 규칙

Java 8 문법만 사용합니다. 기존 패키지 구조를 따르고 Controller, Service, DTO, Mapper 인터페이스/XML의 책임을 분리합니다. 클래스명은 `PascalCase`, 메서드와 필드는 `camelCase`를 사용합니다. DTO는 주변 코드의 `Dto`, `Vo` 접미사 관례를 따릅니다. Lombok은 기존 사용 방식과 일관되게 적용합니다. UI 변경은 Mustache 템플릿을 단순하게 유지하고 `static`에 있는 Bootstrap 4, jQuery, jQuery UI, jqGrid, C3.js 관례를 따릅니다. 새 UI 스타일은 가능하면 Bootstrap 4 유틸리티 클래스를 우선 사용하고, 화면별 커스텀 스타일은 최소화합니다.

MyBatis 변경은 신규 `@Mapper` 인터페이스 방식과 XML namespace를 인터페이스 전체 경로로 맞추는 방식을 우선 고려합니다. 대량 정렬/수정은 MyBatis `<foreach>`와 SQL `CASE WHEN` 조합으로 단일 쿼리 처리하는 기존 패턴을 검토합니다.

운동 계획/템플릿 도메인에서 수영(`SWIM`) 유형은 페이즈별 `cycle` 필드(인터벌 시간)를 가질 수 있습니다. UI는 `cycle` 옵션 비활성 시 4컬럼, 활성 시 5컬럼으로 동적 전환되는 기존 그리드 흐름과 입력값 보존 방식을 유지합니다. 템플릿 API는 `/api/exercise/templates/**` 하위의 생성(`/create`), 수정(`/update`), 삭제(`/delete/{seq}`), 정렬(`/reorder`) 패턴을 우선 따릅니다.

## 테스트 가이드라인

테스트는 `spring-boot-starter-test`와 Spring Security 테스트 지원을 사용합니다. 신규 단위/통합 테스트는 `test/main/java/com/yeahn/...` 아래에 추가하고 `PlanServiceUnitTest`, `TemplateServiceIntegrationTest`처럼 대상과 범위를 드러내는 이름을 사용합니다. 테스트 메서드는 검증하려는 동작이 드러나게 작성합니다. 로컬에서는 `-Dspring.profiles.active=local`로 `application-local.properties`의 DB/COS 설정을 사용하고, `-Dspring.profiles.active=test`는 CI/GitHub Actions용으로만 사용합니다.

**IP stub 주의:** 단위 테스트에서 `when(request.getRemoteAddr()).thenReturn(...)` 사용 시 반드시 비-루프백 IP(예: `"10.10.10.10"`)를 사용합니다. `CommonUtils.getIP()`가 `127.0.0.1`과 `0:0:0:0:0:0:0:1`을 `InetAddress.getLocalHost().getHostAddress()`로 자동 교체하므로 루프백 IP를 stub하면 실제 호스트 IP(예: VirtualBox 어댑터 IP)가 감사 필드에 기록되어 단언이 실패합니다.

**AmazonS3 MockBean:** `@SpringBootTest` 통합 테스트에서 AmazonS3 관련 빈이 두 개(`amazonS3Client`, `client`) 등록되므로 타입만으로 `@MockBean`하면 모호합니다. 빈 이름을 명시해야 합니다.
```java
@MockBean(name = "amazonS3Client") private AmazonS3 amazonS3ClientMock;
@MockBean(name = "client")         private AmazonS3 cosClientMock;
```

## 커밋과 Pull Request 가이드라인

최근 커밋은 `포트추가`, `서버 프리셋 변경`처럼 짧은 한국어 요약을 사용합니다. 커밋 메시지는 간결하고 변경 동작이 드러나게 작성합니다. PR에는 변경 설명, 영향을 받는 기능이나 경로, 실행한 테스트 명령, Mustache/UI 변경 시 스크린샷을 포함합니다. 관련 이슈가 있으면 연결하고 설정, DB, 배포 영향은 별도로 명시합니다.

## 보안과 설정 팁

DB 비밀번호, Cloudtype 토큰, COS/S3 키, 로컬 IDE 비밀값은 커밋하지 않습니다. 환경별 설정은 Spring profile 또는 환경 변수로 분리합니다. SQL이나 매퍼를 수정할 때는 기존 쿼리가 의존하는 `DEL_YN` 기반 soft delete 조건을 유지합니다.

Spring Security는 `WebSecurityConfigurerAdapter` 기반이며 BCrypt를 사용합니다. `/login`, `/signUp`, `/api/user/**`를 제외한 경로는 기본적으로 `ADMIN` 권한을 요구합니다. `/api/user/**`는 비로그인 접근을 허용하는 공개 API 영역이므로 새 엔드포인트 추가 시 개인정보 노출과 쓰기 API 노출을 별도로 점검합니다. XSS 대응은 Naver Lucy XSS Filter 설정 영향을 확인합니다.

**프론트엔드 연동 주의:** `D:\Projects\yeahn-fitbase`의 `src/api/auth.ts`는 `ResponseDto<T>` 타입과 `/api/user/check-id`, `/api/user/signUp` 처리를 갖고 있습니다. `src/api/templates.ts`도 `/api/user/templates` 응답을 `ResponseDto<Template[]>`로 받은 뒤 `status === "SUCCESS"`를 확인하고 `data`만 반환합니다. 신규/변경 호출부는 raw 배열이나 raw 객체에 의존하지 말고 같은 패턴을 우선 사용합니다.

**Cloudtype Scouter 설정:** Cloudtype `java@8` 배포 시 `options.start` 필드를 사용하여 런타임에 Scouter 설정을 동적으로 주입합니다. 배포 전 GitHub Actions 단계에서 Scouter Agent JAR를 준비(`agent/scouter/scouter.agent.jar`)해야 하며, 실행 시점에 `printf`를 통해 `/tmp/scouter.conf`를 생성하고 `JAVA_TOOL_OPTIONS`를 설정하여 에이전트를 로드합니다. 수집 서버 IP는 GitHub Secrets의 `SCOUTER_COLLECTOR_IP`를 사용하며, 정상 작동 시 로그에 `Starting Application with Scouter Agent...` 문구가 나타납니다.

**application-test.properties 용도:** `application-test.properties`는 CI/GitHub Actions 전용입니다. 로컬 통합 테스트에서 MySQL/MariaDB 인증 방식이 다르다고 이 파일의 DB URL을 로컬 기준으로 고치지 않습니다. 로컬은 `application-local.properties`를 사용합니다. `@MockBean`으로 AmazonS3 빈을 대체하더라도 `S3Config` 클래스 자체의 `@Value` 필드 주입은 일어나므로, 로컬 테스트는 `local` 프로파일을 명시해 COS 값을 로드해야 합니다.
