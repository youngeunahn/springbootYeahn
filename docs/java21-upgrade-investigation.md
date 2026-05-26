# Java 21 업그레이드 검토

작성일: 2026-05-26

## 결론

현재 프로젝트는 Java 8, Spring Boot 2.6.11 기반이므로 Java 21 업그레이드는 단순 JDK 변경이 아니라 Spring Boot 3.5.14 마이그레이션으로 봐야 한다.

추천 방향은 다음과 같다.

1. 현재 테스트 기준선을 먼저 확보한다.
2. Spring Boot 2.7.x로 중간 업그레이드한다.
3. Spring Security 설정을 새 방식으로 정리한다.
4. Spring Boot 3.5.14 + Java 21로 전환한다.
5. Jakarta 전환, COS/S3, Lucy XSS, CI/배포 런타임을 해결한다.
6. 안정화 후 Swagger/OpenAPI를 추가한다.

Java 25로 바로 가는 것보다 Java 21을 먼저 목표로 잡는 것이 낫다. Java 21은 LTS이고 Spring Boot 3.5.14, 라이브러리, 배포 환경에서 검증 폭이 넓다.

최종 목표 버전:

- Java: 21
- JDK distribution: Eclipse Temurin 21
- Spring Boot: 3.5.14
- Spring Boot 중간 경유: 2.7.18
- Spring Framework: Spring Boot 3.5.14가 관리하는 6.x 라인
- Spring Security: Spring Boot 3.5.14가 관리하는 6.x 라인
- MyBatis Spring Boot Starter: 3.0.3
- springdoc OpenAPI: springdoc-openapi-starter-webmvc-ui 2.8.17
- IBM COS SDK: 2.15.1 검토
- JWT: 현재 미사용이므로 jjwt 0.9.1 제거

## 공식 문서 기준

- Spring Boot 3.2.3은 Java 17 이상이 필요하고 Java 21까지 호환된다.
  - https://docs.spring.io/spring-boot/docs/3.2.3/reference/html/getting-started.html
- Spring Boot 3 마이그레이션 가이드는 먼저 최신 Spring Boot 2.7.x로 올린 뒤 Boot 3로 전환할 것을 권장한다.
  - https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide
- Spring Security 6에서는 `WebSecurityConfigurerAdapter`가 제거되고 `antMatchers` 계열도 `requestMatchers`로 전환해야 한다.
  - https://docs.enterprise.spring.io/spring-security/reference/5.8-SNAPSHOT/migration/servlet/config.html
- MyBatis Spring Boot Starter 호환표 기준으로 Boot 3.2-3.5는 MyBatis starter 3.0 라인과 Java 17 이상을 사용한다.
  - https://mybatis.org/spring-boot-starter/mybatis-spring-boot-autoconfigure/
- Springdoc OpenAPI는 Spring Boot 3 지원 시 v2 계열을 사용해야 한다.
  - https://github.com/springdoc/springdoc-openapi

## 현재 프로젝트 영향 범위

### Maven

현재 위치:

- `pom.xml`
- `spring-boot-starter-parent`: 2.6.11
- `java.version`: 1.8
- `mybatis-spring-boot-starter`: 2.1.3
- `spring-boot-maven-plugin`: 2.4.4 명시
- `spring-boot-starter-mustache`: 2.6.3 명시
- `spring-boot-starter-security`: 2.6.11 명시
- `cos-spring-boot-starter`: 1.0.2 (초기 상태, 9단계에서 제거)
- `ibm-cos-java-sdk`: 2.8.0 (초기 상태, 9단계에서 2.15.1로 변경)
- `lucy-xss-servlet`: 2.0.1
- `jjwt`: 0.9.1

검토 사항:

- Spring Boot 최종 목표 버전은 `3.5.14`로 고정한다.
- Spring Boot 중간 경유 버전은 `2.7.18`로 고정한다.
- Spring Boot parent를 3.x로 올리면 Boot가 관리하는 의존성 버전은 가능한 한 명시 버전을 제거하는 편이 안전하다.
- MyBatis starter는 `3.0.3`으로 올린다.
- `jjwt`는 현재 미사용으로 확인되었으므로 제거한다. 나중에 JWT를 실제 도입할 때 `jjwt-api`, `jjwt-impl`, `jjwt-jackson` 최신 구조로 다시 추가한다.
- IBM COS starter는 오래된 편이라 Boot 3에서 `@EnableCOS` 유지가 리스크다. 직접 `AmazonS3` Bean을 구성하는 쪽을 우선 검토한다.
- IBM COS SDK는 `2.15.1`을 검토한다.
- springdoc은 Boot 3.5.14 기준 `springdoc-openapi-starter-webmvc-ui 2.8.17`을 사용한다.
- `springdoc-openapi` 3.x 라인은 Boot 4 변화와 섞일 수 있으므로 이번 범위에서는 우선 제외한다.

예상 parent 변경:

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.14</version>
    <relativePath/>
</parent>
```

### Jakarta 전환

Boot 3는 Jakarta EE 기반이라 `javax.*` import를 `jakarta.*`로 바꿔야 한다.

현재 `javax.*` 사용 파일:

- `src/main/java/com/yeahn/auth/controller/UserApiController.java`
- `src/main/java/com/yeahn/config/WebInterceptor.java`
- `src/main/java/com/yeahn/template/service/TemplateService.java`
- `src/main/java/com/yeahn/common/S3Uploader.java`
- `src/main/java/com/yeahn/plan/service/PlanService.java`
- `src/main/java/com/yeahn/security/handler/LoginSuccessHandler.java`
- `src/main/java/com/yeahn/security/handler/LoginFailureHandler.java`
- `src/main/java/com/yeahn/yetable/controller/ExcelDownController.java`
- `src/main/java/com/yeahn/security/config/SecurityConfig.java`
- `src/main/java/com/yeahn/yetable/entity/YeahnTableEntity.java`
- `src/main/java/com/yeahn/security/filter/AccessLogFilter.java`
- `src/main/java/com/yeahn/common/CommonUtils.java`
- `src/main/java/com/yeahn/security/filter/SessionAuthenticationFilter.java`
- `src/main/java/com/yeahn/plan/controller/PlanController.java`
- `src/main/java/com/yeahn/template/controller/TemplateApiController.java`
- `test/main/java/com/yeahn/template/service/TemplateServiceUnitTest.java`
- `test/main/java/com/yeahn/plan/service/PlanServiceUnitTest.java`

대표 변경:

- `javax.servlet.*` -> `jakarta.servlet.*`
- `javax.servlet.http.*` -> `jakarta.servlet.http.*`
- `javax.persistence.*` -> `jakarta.persistence.*`
- `javax.annotation.PostConstruct` -> `jakarta.annotation.PostConstruct`

### Spring Security

현재 위치:

- `src/main/java/com/yeahn/security/config/SecurityConfig.java`

현재 구조:

- `WebSecurityConfigurerAdapter` 상속
- `configure(HttpSecurity http)` override
- `configure(AuthenticationManagerBuilder auth)` override
- `authorizeRequests()`
- `antMatchers()`

Boot 3 / Security 6 전환 시 필요한 작업:

- `WebSecurityConfigurerAdapter` 제거
- `SecurityFilterChain` Bean 추가
- `AuthenticationManagerBuilder` 직접 override 제거
- `PasswordEncoder` Bean 명시
- `UserDetailsService` 기반 인증 설정 재구성
- `authorizeRequests()` -> `authorizeHttpRequests()`
- `antMatchers()` -> `requestMatchers()`
- Swagger를 이후 추가하면 `/swagger-ui/**`, `/swagger-ui.html`, `/v3/api-docs/**` 허용 정책도 같이 정리

### WebInterceptor

현재 위치:

- `src/main/java/com/yeahn/config/WebInterceptor.java`

현재 구조:

- `HandlerInterceptorAdapter` 상속

검토 사항:

- Spring Framework 6 환경에서 `HandlerInterceptor` 직접 구현으로 바꾸는 편이 안전하다.
- `postHandle`의 공통 메뉴/로그인 사용자 모델 주입 동작은 유지해야 한다.

### Lucy XSS

현재 위치:

- `src/main/java/com/yeahn/common/CommonUtils.java`
- `src/main/resources/lucy-xss-servlet-filter-rule.xml`
- `src/main/resources/lucy-xss-superset.xml`

현재 사용:

- `XssPreventerDefender`
- `XssFilter`
- `javax.servlet.http.HttpServletRequest`

리스크:

- `lucy-xss-servlet`가 Jakarta Servlet 환경에서 정상 동작하는지 불확실하다.
- Boot 3 전환 초기에 별도 spike가 필요하다.

대응 후보:

- Lucy XSS 사용을 유지할 수 있는 Jakarta 대응 버전이 있는지 확인
- 유지가 어렵다면 현재 `CommonUtils.paramCleanXSS`의 사용 범위를 먼저 파악하고 대체 구현을 적용
- 입력 정제보다 출력 이스케이프/템플릿 escaping 정책을 우선 확인

### IBM COS/S3

현재 위치:

- `src/main/java/com/yeahn/common/S3Config.java`
- `src/main/java/com/yeahn/common/S3Uploader.java`
- `test/main/java/com/yeahn/plan/service/PlanServiceIntegrationTest.java`
- `test/main/java/com/yeahn/template/service/TemplateServiceIntegrationTest.java`

초기 구조:

- `@EnableCOS`
- `cos-spring-boot-starter`
- `ibm-cos-java-sdk`
- `AmazonS3` Bean 2개 이슈로 테스트에서 `@MockBean(name = "amazonS3Client")`, `@MockBean(name = "client")` 사용

리스크:

- `cos-spring-boot-starter`가 오래된 편이고 Jakarta/Boot 3 자동 설정 호환성이 불확실하다.
- 현재도 `@EnableCOS`가 `client` Bean을 추가로 만들고 있어 테스트 복잡도가 있다.

추천 대응 및 9단계 적용 결과:

- Boot 3 전환 시 `@EnableCOS` 제거 검토 -> 제거 완료
- 직접 `AmazonS3` Bean만 구성하도록 단순화 -> `amazonS3Client` Bean 하나만 유지
- IBM COS Java SDK는 최신 2.15.x까지 존재하므로 SDK 자체는 최신화 검토 -> `2.15.1` 적용
- 테스트 MockBean은 Bean 이름 기준으로 계속 유지하거나, Bean을 하나로 줄여 테스트를 단순화 -> `@MockBean(name = "client")` 제거, `@MockBean(name = "amazonS3Client")`만 유지

### Scouter

현재 위치:

- `.github/workflows/ci.yml`
- `agent/scouter/scouter.agent.jar`

조사 내용:

- Scouter 릴리즈 노트에는 Java 20, virtual threads, Jakarta servlet 지원 언급이 있다.
  - https://github.com/scouter-project/scouter/releases

검토 사항:

- 현재 저장소에 포함된 `scouter.agent.jar`의 실제 버전을 확인해야 한다.
- Java 21 + Boot 3 배포 전 Cloudtype에서 Java agent 로딩 로그와 수집 정상 여부를 확인해야 한다.

### CI/CD와 Cloudtype

현재 위치:

- `.github/workflows/ci.yml`

현재 설정:

- GitHub Actions: `java-version: '8'` (초기 상태, 11단계에서 `'21'`로 변경)
- Cloudtype: `app: java@8` (초기 상태, 11단계에서 `java@21`로 변경)
- Cloudtype options: `jdk: "8"` (초기 상태, 11단계에서 `"21"`로 변경)

변경 필요:

- GitHub Actions JDK를 21로 변경
- Cloudtype Java 21 런타임 지원 방식 확인
- Cloudtype yaml의 `app`, `options.jdk` 값을 Java 21 기준으로 변경
- Scouter Agent와 `JAVA_TOOL_OPTIONS`가 Java 21 런타임에서 정상 동작하는지 확인

## Swagger와의 순서

Swagger는 Java 21 업그레이드 이후 추가하는 것이 낫다.

이유:

- 현재 Boot 2.6.11에서는 `springdoc-openapi-ui` 1.8.0 계열이 맞다.
- Boot 3로 올리면 `springdoc-openapi-starter-webmvc-ui` v2 계열로 다시 바꿔야 한다.
- 지금 Swagger를 붙이면 업그레이드 때 다시 제거/교체할 가능성이 높다.

따라서 추천 순서는 다음과 같다.

1. Java 21 + Spring Boot 3.5.14 업그레이드
2. Security/Jakarta/COS/Lucy 안정화
3. Swagger/OpenAPI 추가

## Spring Boot 3.5.14 기준 추가 변경 포인트

- Spring Boot 3.5.14는 Boot 3 계열이므로 Boot 3.0 전환의 핵심 변경인 Jakarta 전환과 Spring Security 6 전환이 그대로 필요하다.
- Boot 4.0이 아니라 3.5.14를 목표로 하므로 Spring Framework 7, Spring Security 7, Jackson 3 계열 전환은 이번 범위에서 제외한다.
- Springdoc OpenAPI는 Boot 3 호환 v2 계열을 사용한다. Boot 2용 `springdoc-openapi-ui` 1.x는 사용하지 않는다.
- MyBatis는 Boot 3.2-3.5 호환 starter 3.0 라인 중 `3.0.3`을 사용한다.
- Boot BOM이 관리하는 의존성은 직접 버전을 박지 않는 방향으로 정리한다.
- `spring-boot-maven-plugin`의 기존 명시 버전 `2.4.4`는 제거하거나 3.5.14에 맞춘다.
- JDK 배포판은 CI와 로컬 모두 Eclipse Temurin 21을 기준으로 맞춘다.

## 추천 마이그레이션 단계

### 1단계: 기준선 확보

- 현재 브랜치에서 전체 빌드와 테스트 결과 기록
- 로컬 테스트:
  - `mvn -B package -DskipTests`
  - `mvn test "-Dspring.profiles.active=local"`
- CI 테스트:
  - `mvn test "-Dspring.profiles.active=test"`

### 2단계: Spring Boot 2.7.x 중간 업그레이드

- Boot 2.7 최신 라인으로 이동
- Security 5.8 전환 준비
- deprecated API와 설정 경고 확인
- 이 단계에서 기능 변경은 최소화

### 3단계: Security 설정 선제 정리

- `WebSecurityConfigurerAdapter` 제거
- `SecurityFilterChain` Bean 방식으로 전환
- 기존 허용 경로 유지:
  - `/login`
  - `/api/user/**`
  - `/admin/signUp*`
  - `/admin/signUp/checkId`
  - `/css/**`
  - `/js/**`
- 기존 ADMIN 권한 정책 유지
- 로그인 성공/실패 핸들러 동작 유지
- 로그아웃 HTML 요청 redirect 동작 유지

### 4단계: Java 21 + Spring Boot 3.5.14 전환

- `java.version` 21
- Spring Boot parent 3.5.14
- Maven plugin 버전 정리
- 명시된 Boot starter 버전 제거 또는 Boot 3 기준으로 정리
- `javax.*` -> `jakarta.*`
- 컴파일 오류를 기준으로 라이브러리 호환성 해결

### 5단계: 외부 라이브러리 리스크 해결

- Lucy XSS 대체/유지 결정
- IBM COS starter 제거 여부 결정
- Scouter Agent 버전 확인
- `jjwt` 미사용이면 제거
- MyBatis starter 3.0 라인 적용

### 6단계: CI/CD 전환

- GitHub Actions JDK 21
- Cloudtype Java 21 런타임 설정
- 배포 후 Scouter Agent 로딩 확인
- 운영 profile 환경 변수 유지 확인

### 7단계: Swagger 추가

- `springdoc-openapi-starter-webmvc-ui` v2 계열 추가
- Security에 Swagger 경로 정책 추가
- 우선 `/api/user/**` 공개 API 중심으로 문서화
- 관리자 API와 공개 API를 문서에서 명확히 분리

## 리스크 우선순위

높음:

- Spring Security 6 전환
- Lucy XSS Jakarta 호환성
- IBM COS Spring Boot starter 호환성
- Cloudtype Java 21 런타임 지원과 Scouter Agent

중간:

- `javax.*` -> `jakarta.*` 일괄 전환
- MyBatis starter 업그레이드
- JPA import 전환
- 테스트 profile과 S3 MockBean 정리

낮음:

- Mustache/Bootstrap UI
- MyBatis XML 쿼리
- MariaDB SQL 자체
- 공개 API `ResponseDto<T>` 규격

## 별도 확인이 필요한 사항

- Cloudtype에서 Java 21 앱 타입과 옵션이 정확히 무엇인지
- 현재 `agent/scouter/scouter.agent.jar` 버전
- `lucy-xss-servlet`의 Jakarta 대응 가능 여부
- IBM COS starter를 제거해도 현재 업로드 기능이 동일하게 동작하는지
- `jjwt`가 실제로 미사용인지
- 전체 통합 테스트가 local/test profile에서 모두 재현 가능하게 통과하는지
