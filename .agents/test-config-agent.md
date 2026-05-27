# test-config-agent

## Role

이 에이전트는 Maven 테스트, Spring profile, CI, 설정 파일, S3/COS MockBean 문제를 담당한다.

주요 범위는 `pom.xml`, `test/main/java`, `src/main/resources/application*.properties`, `src/main/resources/schema.sql`, `src/main/java/com/yeahn/config`, `.github/workflows/ci.yml`이다.

## Good Tasks

- 단위/통합 테스트 추가 및 수정
- 로컬 local 프로파일과 CI test 프로파일 실행 조건 점검
- Maven test source 경로와 플러그인 설정 점검
- GitHub Actions CI 실패 원인 분석
- S3/COS 관련 테스트 컨텍스트 로딩 문제 해결
- MariaDB 기반 통합 테스트 환경 점검

## Commands

```bash
mvn -B package -DskipTests
mvn test -Dspring.profiles.active=local
mvn test -Dspring.profiles.active=local -Dtest=PlanServiceUnitTest
mvn test -Dspring.profiles.active=local -Dtest=TemplateServiceIntegrationTest
mvn spring-boot:run -Dspring.profiles.active=local
mvn test -Dspring.profiles.active=test
```

## Project Patterns

- Java 8, Spring Boot 2.6.11 기준을 유지한다.
- 테스트 소스는 표준 `src/test/java`가 아니라 `test/main/java`이며, `build-helper-maven-plugin`이 Maven test source로 추가한다.
- 로컬 테스트 실행은 `local` 프로파일을 사용하며 `application-local.properties`의 DB/COS 설정을 로드한다.
- `application-test.properties`는 CI/GitHub Actions 전용이며 `localhost:3306/yeahn_test`, `root/root` MariaDB를 전제로 한다.
- CI에서는 MariaDB 10.6 서비스로 `yeahn_test` DB를 띄우고 `src/main/resources/schema.sql`을 먼저 적용한다.
- CI에서는 `schema.sql` 외에 `FN_GET_COMM_CODE_DESC` 함수를 별도로 생성할 수 있다.

## S3/COS Notes

- `S3Config`는 `@EnableCOS`와 직접 정의한 `amazonS3Client()` 때문에 `AmazonS3` 빈이 두 개 생긴다.
- 빈 이름은 `amazonS3Client`, `client`다.
- `@SpringBootTest` 통합 테스트에서 타입만으로 `@MockBean AmazonS3`를 쓰면 모호하다.

```java
@MockBean(name = "amazonS3Client")
private AmazonS3 amazonS3ClientMock;

@MockBean(name = "client")
private AmazonS3 cosClientMock;
```

- `@MockBean`으로 S3 빈을 대체해도 `S3Config`의 `@Value` 주입은 먼저 일어날 수 있으므로, 로컬은 `application-local.properties`의 COS 값이 필요하고 CI test 프로파일에는 COS dummy 값이 필요하다.

## Test Notes

- `PlanServiceIntegrationTest`, `TemplateServiceIntegrationTest`는 `@SpringBootTest(classes = Application.class)`와 `@Transactional`을 사용한다.
- 통합 테스트 후 DB 변경은 트랜잭션 롤백된다.
- soft delete 검증이 많으므로 `DEL_YN = 'N'/'Y'` 조건을 깨뜨리지 않는다.
- 일부 검증은 `JdbcTemplate`으로 직접 DB를 조회한다.
- 단위 테스트는 `@ExtendWith(MockitoExtension.class)`로 Mapper를 mock 처리하고 Spring 컨텍스트/DB 없이 Service 로직을 검증한다.
- `SecurityContextHolder`를 직접 세팅하면 `@AfterEach`에서 `SecurityContextHolder.clearContext()`를 유지한다.
- `HttpServletRequest.getRemoteAddr()` stub 시 `127.0.0.1`은 피한다. `CommonUtils.getIP()`가 루프백 IP를 실제 호스트 IP로 바꿀 수 있다.
- `@DisplayName`은 기존 테스트와 동일하게 한국어 설명형 문장으로 작성한다. 예: `"비로그인 사용자는 Swagger UI 접근 시 로그인으로 이동한다"`, `"템플릿 삭제 시 마스터와 운동 정보를 삭제 처리한다"`.

## Config Notes

- `fileUploadConfig`는 `resource.handler`, `resource.location`이 필요하다.
- `UserWebCorsConfig`는 `app.user-web.allowed-origins` fallback이 있고 기본값은 `http://localhost:3000`이다.
- `WebMvcConfigurer`는 인터셉터를 전역 등록하지만 `/api/**/*`, `/ajax/**/*`, 정적 리소스는 제외한다.
- `CacheConfig`는 Caffeine 캐시 `getMenuList`를 등록한다.
- `application-local.properties`에는 실제 값으로 보이는 DB/COS 값이 있을 수 있으므로 노출/커밋에 주의한다.

