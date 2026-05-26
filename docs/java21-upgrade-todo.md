# Java 21 업그레이드 단계별 할 일

작성일: 2026-05-26

목표: Java 8 + Spring Boot 2.6.11 프로젝트를 Java 21 + Spring Boot 3.5.14 기반으로 안정적으로 전환한다.

## 결정된 목표 버전

- [x] Java: 21
- [x] JDK distribution: Eclipse Temurin 21
- [x] Spring Boot 중간 경유: 2.7.18
- [x] Spring Boot 최종 목표: 3.5.14
- [x] MyBatis Spring Boot Starter: 3.0.3
- [x] IBM COS SDK: 2.15.1 검토
- [x] JWT: 현재 미사용이므로 `jjwt 0.9.1` 제거

## 0단계: 작업 전 기준선 확보

- [x] 현재 브랜치 상태 확인
  - [x] `git status`
  - [x] `git diff --name-only`
  - [x] Java 8 기준 현재 빌드 성공 여부 확인
- [x] 로컬 빌드 기준선 확보
  - [x] `mvn -B package -DskipTests`
- [x] 로컬 테스트 기준선 확보
  - [x] `mvn test "-Dspring.profiles.active=local"`
- [x] CI/test profile 기준선 확인
  - [x] `mvn test "-Dspring.profiles.active=test"`
- [x] 실패 테스트가 있으면 Java 업그레이드 전 기존 실패인지 문서화
- [x] 현재 로그인, 로그아웃, 관리자 메뉴, `/api/user/**` 공개 API 수동 동작 확인

완료 조건:

- [x] Java 8 기준 현재 성공/실패 상태가 기록되어 있다.
- [x] 업그레이드 후 비교할 수 있는 테스트 기준선이 있다.

기준선 기록:

- `jjwt 0.9.1` 제거 후 기준선이다. 순수 변경 전 기준선은 별도로 확보하지 못했다.
- `mvn -B package -DskipTests`: 성공
- `mvn test "-Dspring.profiles.active=local"`: 성공, 37 tests, 0 failures, 0 errors
- `mvn test "-Dspring.profiles.active=test"`: 로컬에서 실패, 37 tests 중 16 errors
- `test` profile 실패 원인: 로컬 `localhost:3306` MariaDB 접속 시 `RSA public key is not available client side (option serverRsaPublicKeyFile not set)` 발생
- `test` profile은 CI/GitHub Actions MariaDB 서비스 전용 설정이므로 로컬 실패는 환경 차이로 기록한다.
- 사용자 확인 기준, 작업 시작 전 브랜치는 GitHub와 동일한 상태였다.
- `jjwt` 제거 후 `git diff --name-only` 기준 변경 파일은 `pom.xml`이다.
- PowerShell 공개 API 확인:
  - `GET /api/user/check-id?userId=test`: 200, `status=SUCCESS`, `data=true`
  - `GET /api/user/templates`: 200, `status=SUCCESS`, 목록 응답
  - `GET /api/user/templates/1`: 200, `status=FAIL`, `message=Template not found`, `data=null`
- API 확인은 이미 떠 있던 로컬 Java 애플리케이션 프로세스에 대해 수행했다.
- 브라우저 수동 확인 완료:
  - 로그인 성공
  - 로그인 실패
  - 로그아웃
  - 좌측 메뉴 렌더링
  - 좌측 메뉴 상단 사용자명 표시
  - 좌측 메뉴 상단 로그아웃 버튼 동작
  - 관리자 페이지 접근
  - 비로그인 상태에서 관리자 페이지 접근 시 로그인으로 이동

## 1단계: 불필요 의존성 정리

- [x] `jjwt` 사용처 재확인
  - [x] `rg "io\\.jsonwebtoken|Jwts|Claims|Jwt" src/main/java test/main/java`
- [x] 현재 JWT 미사용이 확인되었으므로 `pom.xml`에서 `jjwt 0.9.1` 제거
- [x] 제거 후 빌드 확인
  - [x] `mvn -B package -DskipTests`
- [x] 제거 후 인증/로그인 관련 테스트 확인
  - [x] `mvn test "-Dspring.profiles.active=local" "-Dtest=UserServiceTest,UserApiControllerTest"`

완료 조건:

- [x] 미사용 JWT 의존성이 제거되어 있다.
- [x] 제거 후 빌드가 성공한다.

## 2단계: Spring Boot 2.7.x 중간 업그레이드

- [x] `pom.xml` parent를 Spring Boot 2.7.18로 변경
- [x] Boot starter 명시 버전 제거 검토
  - [x] `spring-boot-starter-mustache`
  - [x] `spring-boot-starter-security`
  - [x] `spring-boot-autoconfigure`
  - [x] `spring-boot-maven-plugin`
- [x] `mybatis-spring-boot-starter`를 Boot 2.7 호환 라인으로 변경
- [x] 컴파일 확인
  - [x] `mvn -B package -DskipTests`
- [x] 테스트 확인
  - [x] `mvn test "-Dspring.profiles.active=local"`
- [x] deprecation warning과 설정 warning 기록

완료 조건:

- [x] Spring Boot 2.7.x에서 기존 기능이 유지된다.
- [x] Boot 3 전환 전 제거/수정할 deprecated API 목록이 정리되어 있다.

2단계 기록:

- Spring Boot parent: `2.6.11` -> `2.7.18`
- `spring-boot-autoconfigure` 직접 의존성 제거
- `spring-boot-starter-mustache` 명시 버전 제거
- `spring-boot-starter-security` 명시 버전 제거
- `spring-boot-maven-plugin` 명시 버전 `2.4.4` 제거
- `mybatis-spring-boot-starter`: `2.1.3` -> `2.3.2`
- `mvn -B package -DskipTests`: 성공
- `mvn test "-Dspring.profiles.active=local"`: 성공, 37 tests, 0 failures, 0 errors
- 기록된 warning:
  - `com.google.zxing:core:3.5.3`, `com.google.zxing:javase:3.5.3` effective model warning
  - `SecurityConfig` deprecated API 사용 warning
  - `ExcelDownController` unchecked/unsafe operations warning
- Boot 3 전환 전 우선 정리 대상:
  - `SecurityConfig`의 `WebSecurityConfigurerAdapter`, `authorizeRequests`, `antMatchers`

## 3단계: Spring Security 설정 선제 전환

- [x] `SecurityConfig`에서 `WebSecurityConfigurerAdapter` 제거
- [x] `SecurityFilterChain` Bean 방식으로 변경
- [x] `PasswordEncoder` Bean 추가
- [x] `AuthenticationManager` Bean 추가
- [x] `UserDetailsService` 기반 인증 설정 유지
- [x] 기존 허용 경로 유지
  - [x] `/login`
  - [x] `/api/user/**`
  - [x] `/admin/signUp*`
  - [x] `/admin/signUp/checkId`
  - [x] `/css/**`
  - [x] `/js/**`
- [x] 기존 관리자 권한 정책 유지
  - [x] 기본 관리자 화면은 `ADMIN` 권한 필요
- [x] 로그인 설정 유지
  - [x] login page: `/login`
  - [x] login processing url: `/login`
  - [x] username parameter: `userId`
  - [x] password parameter: `password`
  - [x] success handler 유지
  - [x] failure handler 유지
- [x] 로그아웃 설정 유지
  - [x] logout url: `/logout`
  - [x] session invalidate
  - [x] authentication clear
  - [x] `JSESSIONID` delete
  - [x] HTML 요청은 `/login?logout` redirect
  - [x] 비 HTML 요청은 204 응답
- [x] 인증 관련 테스트 실행
  - [x] `mvn test "-Dspring.profiles.active=local" "-Dtest=UserServiceTest,UserApiControllerTest"`
- [x] 전체 로컬 테스트 실행
  - [x] `mvn clean test "-Dspring.profiles.active=local"`
- [x] 브라우저 수동 확인
  - [x] 로그인 성공
  - [x] 로그인 실패
  - [x] 로그아웃
  - [x] 비로그인 접근 시 로그인 화면 이동
  - [x] `/api/user/**` 비로그인 접근 허용

완료 조건:

- [x] Boot 2.7.x 상태에서 Security 새 설정 방식으로 기존 인증 동작이 테스트 기준으로 유지된다.
- [x] 변경 후 브라우저 수동 확인이 완료되어 있다.

3단계 기록:

- `SecurityConfig`가 `WebSecurityConfigurerAdapter` 상속 방식에서 `SecurityFilterChain` Bean 방식으로 전환되었다.
- `PasswordEncoder`는 `BCryptPasswordEncoder` Bean으로 분리했다.
- `AuthenticationManager`는 `AuthenticationConfiguration`에서 가져오는 Bean으로 분리했다.
- 기존 `UserService` 기반 `UserDetailsService`, 로그인 성공/실패 handler, 로그아웃 응답 정책은 유지했다.
- 기존 권한 matcher는 Boot 2.7/Spring Security 5.7 기준으로 유지했다. Boot 3/Spring Security 6 전환 시 `authorizeRequests`와 `antMatchers`는 추가 전환 대상이다.
- `mvn -B package -DskipTests`: 성공
- `mvn test "-Dspring.profiles.active=local" "-Dtest=UserServiceTest,UserApiControllerTest"`: 성공, 5 tests, 0 failures, 0 errors
- `mvn clean test "-Dspring.profiles.active=local"`: 성공, 37 tests, 0 failures, 0 errors
- 기록된 warning:
  - `com.google.zxing:core:3.5.3`, `com.google.zxing:javase:3.5.3` effective model warning
  - `WebInterceptor` deprecated API 사용 warning
  - `ExcelDownController` unchecked/unsafe operations warning

## 4단계: Java 21 빌드 환경 준비

- [x] 로컬 JDK 21 설치
- [x] 로컬 JDK 21 배포판 차이는 허용하고 CI는 Eclipse Temurin 21로 검증
- [x] Maven 실행 JDK를 21로 설정
- [x] `mvn -v`에서 Java 21 확인
- [x] JDK 21로 현재 프로젝트 패키징 확인
  - [x] `mvn -B package -DskipTests`
- [x] GitHub Actions JDK 변경 준비
  - [x] `.github/workflows/ci.yml`의 `distribution: temurin` 유지
  - [x] `.github/workflows/ci.yml`의 `java-version: '21'` 적용 준비
- [x] Cloudtype Java 21 런타임 지원 방식 확인
  - [x] `app` 값
  - [x] `options.jdk` 값
- [x] Scouter Agent 현재 버전 확인
  - [x] `agent/scouter/scouter.agent.jar`
  - [x] Java 21/Jakarta servlet 지원 버전인지 확인

완료 조건:

- [x] 로컬과 CI에서 Java 21로 빌드할 준비 항목이 확인되어 있다.
- [x] Cloudtype Java 21 배포 설정값과 Scouter Agent Java 21/Jakarta 지원 여부가 실제 배포로 검증되어 있다.

4단계 기록:

- 현재 기본 `java -version`: Java 11.0.16
- 현재 기본 `mvn -v`: Java 21
- `JAVA_HOME` 환경 변수는 현재 셸 기준 미설정
- 로컬 JDK 21 설치 경로: `C:\Program Files\Java\jdk-21`
- 로컬 JDK 21 실제 버전: `openjdk version "21"`, vendor `Oracle Corporation`
- 로컬은 Oracle OpenJDK 21을 허용하고, CI에서 Eclipse Temurin 21로 검증하는 것으로 결정했다.
- `C:\Users\ayh33\.local\bin\mvn.cmd`가 `JAVA_HOME=C:\Program Files\Java\jdk-21`을 사용하도록 변경되었다.
- Maven 본체를 JDK 21로 직접 실행한 결과:
  - `mvn -v`: Java version `21`, runtime `C:\Program Files\Java\jdk-21`
  - `mvn -B package -DskipTests`: 성공
- GitHub Actions는 `actions/setup-java@v4`, `distribution: temurin`을 이미 사용 중이다. 초기 `java-version: '8'`은 11단계에서 `'21'`로 변경했다.
- Cloudtype 공식 문서는 프로젝트 JDK 버전과 배포 설정 JDK 버전이 다르면 빌드/실행 오류가 날 수 있다고 안내한다.
  - https://docs.cloudtype.dev/ko/troubleshooting/common
  - https://docs.cloudtype.dev/ko/developers/deploy
- 초기 Cloudtype 설정은 `app: java@8`, `options.jdk: "8"`이었다. 11단계에서 `app: java@21`, `options.jdk: "21"`로 변경했으며, 실제 지원 여부는 배포 실행으로 확인한다.
- Scouter Agent:
  - 파일: `agent/scouter/scouter.agent.jar`
  - 버전: `2.20.0`
  - 빌드 정보: `2023-05-29 05:14 GMT_ENV_java8plus`
  - JAR 내부에 `scouter-extra-java20` 패키지가 포함되어 있다.
  - Java 21 + Boot 3/Jakarta servlet 환경에서 실제 trace가 정상 수집되는지는 11단계 배포 검증에서 확인해야 한다.

## 5단계: Spring Boot 3.5.14 + Java 21 전환

- [x] `pom.xml` 변경
  - [x] Spring Boot parent를 3.5.14로 변경
  - [x] `java.version`을 `21`로 변경
  - [x] Maven compiler source/target 또는 release를 21 기준으로 정리
  - [x] Boot plugin 명시 버전 제거 또는 Boot 3 기준으로 정리
- [x] Boot starter 명시 버전 정리
  - [x] Boot parent가 관리하는 starter 버전은 가능한 제거
- [x] Spring Boot 3.5.14 기준 의존성 정리
  - [x] `spring-boot-maven-plugin` 명시 버전 `2.4.4` 제거 또는 3.5.14 기준 정리
  - [x] `spring-boot-starter-mustache` 명시 버전 제거
  - [x] `spring-boot-starter-security` 명시 버전 제거
  - [x] `spring-boot-autoconfigure` 직접 의존성 필요 여부 확인
- [x] MyBatis starter 변경
  - [x] `mybatis-spring-boot-starter` 3.0.3 적용
- [x] MariaDB driver 버전은 Boot BOM 관리 우선 검토
- [x] Boot 4 전환 범위가 섞이지 않도록 확인
  - [x] Spring Framework 7 전환 제외
  - [x] Spring Security 7 전환 제외
  - [x] Jackson 3 전환 제외
- [x] `mvn -B package -DskipTests` 실행
- [x] 발생한 컴파일 오류 목록화

완료 조건:

- [x] Boot 3.5.14 + Java 21 기준 컴파일 오류 목록이 확보되어 있다.

5단계 기록:

- Spring Boot parent: `2.7.18` -> `3.5.14`
- `java.version`: `1.8` -> `21`
- `maven-compiler-plugin`: `source/target 8` -> `release 21`
- `mybatis-spring-boot-starter`: `2.3.2` -> `3.0.3`
- Boot 3 BOM과 충돌할 수 있는 직접 의존성을 제거했다.
  - `org.mybatis:mybatis:3.4.6`
  - `ch.qos.logback:logback-classic:1.2.3`
  - `org.slf4j:jcl-over-slf4j:1.7.30`
  - `org.slf4j:slf4j-api:1.7.30`
- MariaDB driver는 Boot BOM 관리 버전을 사용한다.
- `mvn -B package -DskipTests`: 실패, 77 compile errors
- 첫 컴파일 오류 분류:
  - `javax.servlet.*`, `javax.servlet.http.*` import 제거 필요
  - `javax.persistence.*` import를 `jakarta.persistence.*`로 변경 필요
  - `javax.annotation.PostConstruct`를 `jakarta.annotation.PostConstruct`로 변경 필요
  - `HandlerInterceptorAdapter` 제거 및 `HandlerInterceptor` 직접 구현 필요
  - `org.springframework.web.multipart.commons.CommonsMultipartResolver` 제거 또는 Boot 3 호환 multipart 설정으로 대체 필요
- 대표 오류 파일:
  - `src/main/java/com/yeahn/auth/controller/UserApiController.java`
  - `src/main/java/com/yeahn/common/CommonUtils.java`
  - `src/main/java/com/yeahn/common/S3Config.java`
  - `src/main/java/com/yeahn/common/S3Uploader.java`
  - `src/main/java/com/yeahn/config/WebInterceptor.java`
  - `src/main/java/com/yeahn/plan/controller/PlanController.java`
  - `src/main/java/com/yeahn/plan/service/PlanService.java`
  - `src/main/java/com/yeahn/template/service/TemplateService.java`
  - `src/main/java/com/yeahn/security/config/SecurityConfig.java`
  - `src/main/java/com/yeahn/security/handler/LoginSuccessHandler.java`
  - `src/main/java/com/yeahn/security/handler/LoginFailureHandler.java`
  - `src/main/java/com/yeahn/security/filter/AccessLogFilter.java`
  - `src/main/java/com/yeahn/security/filter/SessionAuthenticationFilter.java`
  - `src/main/java/com/yeahn/template/controller/TemplateApiController.java`
  - `src/main/java/com/yeahn/yetable/controller/ExcelDownController.java`
  - `src/main/java/com/yeahn/yetable/entity/YeahnTableEntity.java`

## 6단계: `javax.*` -> `jakarta.*` 전환

- [x] Servlet import 변경
  - [x] `javax.servlet.*` -> `jakarta.servlet.*`
  - [x] `javax.servlet.http.*` -> `jakarta.servlet.http.*`
- [x] Persistence import 변경
  - [x] `javax.persistence.*` -> `jakarta.persistence.*`
- [x] Annotation import 변경
  - [x] `javax.annotation.PostConstruct` -> `jakarta.annotation.PostConstruct`
- [x] 대상 파일 처리
  - [x] `src/main/java/com/yeahn/auth/controller/UserApiController.java`
  - [x] `src/main/java/com/yeahn/config/WebInterceptor.java`
  - [x] `src/main/java/com/yeahn/template/service/TemplateService.java`
  - [x] `src/main/java/com/yeahn/common/S3Uploader.java`
  - [x] `src/main/java/com/yeahn/plan/service/PlanService.java`
  - [x] `src/main/java/com/yeahn/security/handler/LoginSuccessHandler.java`
  - [x] `src/main/java/com/yeahn/security/handler/LoginFailureHandler.java`
  - [x] `src/main/java/com/yeahn/yetable/controller/ExcelDownController.java`
  - [x] `src/main/java/com/yeahn/security/config/SecurityConfig.java`
  - [x] `src/main/java/com/yeahn/yetable/entity/YeahnTableEntity.java`
  - [x] `src/main/java/com/yeahn/security/filter/AccessLogFilter.java`
  - [x] `src/main/java/com/yeahn/common/CommonUtils.java`
  - [x] `src/main/java/com/yeahn/security/filter/SessionAuthenticationFilter.java`
  - [x] `src/main/java/com/yeahn/plan/controller/PlanController.java`
  - [x] `src/main/java/com/yeahn/template/controller/TemplateApiController.java`
  - [x] `test/main/java/com/yeahn/template/service/TemplateServiceUnitTest.java`
  - [x] `test/main/java/com/yeahn/plan/service/PlanServiceUnitTest.java`
- [x] 변경 후 컴파일 확인
  - [x] `mvn -B package -DskipTests`

완료 조건:

- [x] 코드에 직접 사용되는 `javax.*` import가 제거되어 있다.
- [x] Jakarta import 전환 후 컴파일이 다음 단계로 진행된다.

6단계 기록:

- `rg "javax\\." src/main/java test/main/java`: 남은 항목 없음
- `HandlerInterceptorAdapter`는 Spring Framework 6에서 제거되어 `HandlerInterceptor` 직접 구현으로 전환했다.
- `CommonsMultipartResolver`는 Spring Framework 6에서 제거되어 `S3Config`의 직접 multipart resolver Bean을 제거했다. multipart 제한값은 Boot의 `spring.servlet.multipart.*` 설정을 사용한다.
- `commons-fileupload:commons-fileupload:1.4` 의존성은 더 이상 직접 사용하지 않아 제거했다.
- Spring Security 6에서 `antMatchers`가 제거되어 `authorizeHttpRequests`와 `requestMatchers` DSL로 전환했다.
- `mvn -B package -DskipTests`: 성공
- 기록된 warning:
  - annotation processing 명시 warning
  - `LoginSuccessHandler` deprecated API 사용 warning
  - `S3Controller` unchecked/unsafe operations warning
  - 테스트 코드 `@MockBean` deprecated warning

## 7단계: WebInterceptor 전환

- [x] `HandlerInterceptorAdapter` 사용 제거
- [x] `HandlerInterceptor` 직접 구현으로 변경
- [x] 기존 `preHandle`, `postHandle`, `afterCompletion` 동작 유지
- [x] 공통 모델 주입 유지
  - [x] `MenuList`
  - [x] `MenuPage`
  - [x] `LoginUserName`
- [x] `/api/**`, `/ajax/**`, 정적 리소스 제외 규칙 유지
- [x] 화면 렌더링 확인
  - [x] 메인
  - [x] 좌측 메뉴
  - [x] 로그인 사용자명
  - [x] 로그아웃 버튼

완료 조건:

- [x] Boot 3.5.14 환경에서 WebInterceptor 전환 코드가 컴파일되고 로컬 테스트가 통과한다.
- [x] Boot 3.5.14 환경에서 공통 메뉴와 로그인 사용자 표시가 브라우저에서 정상 동작한다.

7단계 기록:

- `WebInterceptor`는 `HandlerInterceptor`를 직접 구현한다.
- 기존 `preHandle`, `postHandle`, `afterCompletion` 메서드 시그니처는 Jakarta Servlet 타입으로 유지했다.
- `WebMvcConfigurer`의 interceptor 등록 경로와 제외 경로는 유지했다.
  - 포함: `""`, `/`, `/**`
  - 제외: `/**/*.css`, `/**/*.js`, `/**/*.map`, `/ajax/**/*`, `/api/**/*`
- Boot 3/Hibernate 6에서 `org.hibernate.dialect.MariaDB103Dialect`가 제거되어 컨텍스트 로딩이 실패했다. `spring.jpa.database-platform` 명시를 제거하고 MariaDB dialect 자동 감지로 전환했다.
- `mvn test "-Dspring.profiles.active=local"`: 성공, 37 tests, 0 failures, 0 errors
- 로컬 서버 기동 확인:
  - `mvn spring-boot:run -Dspring.profiles.active=local`
  - Tomcat 10.1.54, port 8080, context path `/`
  - `GET /login`: 200
  - `GET /api/user/check-id?userId=test`: 200, `status=SUCCESS`, `data=true`
- 남은 확인: 브라우저에서 로그인 후 메인/좌측 메뉴/로그인 사용자명/로그아웃 버튼 수동 확인

## 8단계: Lucy XSS 처리

- [x] Lucy XSS 사용처 확인
  - [x] `CommonUtils.paramCleanXSS`
  - [x] XML 설정 파일
- [x] Boot 3/Jakarta 환경에서 `lucy-xss-servlet` 컴파일/실행 가능 여부 확인
- [x] 가능하면 유지
  - [x] 컴파일 확인
  - [x] XSS 정제 동작 단위 테스트 추가 또는 수동 확인
- [x] 불가능하면 대체안 적용
  - [x] Lucy servlet 의존성 제거 필요 없음
  - [x] 현재 사용 범위에 맞춘 대체 sanitizing 적용 필요 없음
  - [x] 출력 escaping 정책 확인
- [x] 로그인/회원가입/게시글/템플릿 입력값 처리 영향 확인

완료 조건:

- [x] XSS 처리 전략이 Boot 3에서 컴파일 및 실행 가능하다.
- [x] 기존 입력값 처리 흐름이 깨지지 않는다.

8단계 기록:

- 실제 사용처:
  - `CommonUtils.paramCleanXSS`
  - `YeController.detail`
  - `YeController.insertYetable`
- `lucy-xss-servlet-filter-rule.xml`과 `lucy-xss-superset.xml`은 존재하지만, Spring Boot 필터로 등록된 코드는 확인되지 않았다.
- 현재 런타임에서 의미 있는 XSS 처리 흐름은 게시판 파라미터를 `CommonUtils.paramCleanXSS()`로 직접 정제하는 방식이다.
- `CommonUtilsTest`를 추가해 위험 HTML 태그 정제와 비밀번호 계열 파라미터 제외 규칙을 검증했다.
- `mvn test "-Dspring.profiles.active=local" "-Dtest=CommonUtilsTest"`: 성공, 2 tests, 0 failures, 0 errors
- `mvn test "-Dspring.profiles.active=local"`: 성공, 39 tests, 0 failures, 0 errors
- 남은 주의점:
  - Lucy servlet filter는 현재 등록되어 있지 않으므로 전역 요청 파라미터 필터링으로 보면 안 된다.
  - Mustache 기본 escaping과 저장 전 정제 범위를 함께 유지해야 한다.

## 9단계: IBM COS/S3 처리

- [x] `@EnableCOS` 유지 가능 여부 확인
- [x] IBM COS SDK 2.15.1 업그레이드 검토
- [x] 불안정하면 `@EnableCOS` 제거
- [x] 직접 `AmazonS3` Bean 구성으로 단순화
- [x] `AmazonS3` Bean 개수 정리
  - [x] 가능하면 `amazonS3Client` 하나만 유지
- [x] 테스트 S3 mock 정리
  - [x] `@MockitoBean(name = "amazonS3Client")`
  - [x] `@MockBean(name = "client")` 제거 가능 여부 확인
- [x] 업로드 기능 확인
  - [x] local profile 컨텍스트, MockBean, `S3UploaderTest` 기준
- [x] 통합 테스트 확인
  - [x] `PlanServiceIntegrationTest`
  - [x] `TemplateServiceIntegrationTest`

완료 조건:

- [x] Boot 3.5.14 환경에서 S3/COS Bean이 정상 생성된다.
- [x] mock 기반 테스트가 정상 동작한다.
- [x] 코드/Mock 기준 업로드 흐름이 검증된다.

9단계 기록:

- `cos-spring-boot-starter`, `cos-spring-framework` 의존성을 제거했다.
- `ibm-cos-java-sdk`: `2.8.0` -> `2.15.1`
- `S3Config`에서 `@EnableCOS`, `@ComponentScan`을 제거하고 직접 구성한 `@Bean(name = "amazonS3Client")`만 유지했다.
- Boot 3 환경에서 IBM COS starter가 추가로 만들던 `client` Bean에 의존하지 않도록 통합 테스트의 `@MockBean(name = "client")`를 제거했다.
- `PlanServiceIntegrationTest`, `TemplateServiceIntegrationTest`는 `@MockitoBean(name = "amazonS3Client")` 하나만으로 컨텍스트가 정상 로딩된다.
- `S3UploaderTest`를 추가해 실제 COS 연결 없이 multipart 파일 저장, `putObject`, 반환 URL 흐름을 검증했다.
- `mvn -B package -DskipTests`: 성공
- `mvn test "-Dspring.profiles.active=local" "-Dtest=S3UploaderTest"`: 성공, 1 test, 0 failures, 0 errors
- `mvn test "-Dspring.profiles.active=local" "-Dtest=PlanServiceIntegrationTest,TemplateServiceIntegrationTest"`: 성공, 11 tests, 0 failures, 0 errors
- `mvn test "-Dspring.profiles.active=local"`: 성공, 40 tests, 0 failures, 0 errors
- 남은 확인: 실제 COS 자격증명과 버킷을 사용하는 운영/배포 환경 업로드 수동 확인은 12단계 최종 검증에서 진행한다.

## 10단계: 테스트와 설정 정리

- [x] 테스트 실행 목록은 12단계 최종 검증으로 이동
- [x] S3 mock 이름 문제 재확인
- [x] IP stub 테스트가 비 루프백 IP를 사용하는지 확인
- [x] `application-local.properties`와 `application-test.properties` 역할 유지
- [x] deprecated 설정 warning 정리
- [x] 로그 설정 확인
  - [x] logback
  - [x] log4jdbc
  - [x] SQL 로그

완료 조건:

- [x] local/test profile의 역할이 유지된다.

10단계 기록:

- 테스트 실행 목록은 최종 검증 성격이므로 12단계로 이동했다.
- Boot 3.5에서 deprecated 된 `@MockBean`을 `@MockitoBean`으로 교체했다.
  - `UserApiControllerTest`
  - `PlanServiceIntegrationTest`
  - `TemplateServiceIntegrationTest`
- S3 mock은 `@MockitoBean(name = "amazonS3Client")` 하나만 사용한다.
- `@MockBean(name = "client")`, `cosClientMock` 사용처는 제거된 상태다.
- IP stub은 `10.10.10.10` 같은 비 루프백 IP를 사용한다.
- `application.properties`는 운영 환경 변수 기반, `application-test.properties`는 CI MariaDB/dummy COS 값 기반으로 역할을 유지한다.
- Hibernate 6에서 제거된 `spring.jpa.database-platform=MariaDB103Dialect` 명시는 제거된 상태다.
- logback 설정은 `classpath:logback.xml`, SQL 로깅은 `log4jdbc-log4j2`와 `DriverSpy` 기준으로 유지된다.
- `mvn test "-Dspring.profiles.active=local" "-Dtest=UserApiControllerTest,PlanServiceIntegrationTest,TemplateServiceIntegrationTest"`: 성공, 14 tests, 0 failures, 0 errors
- 남은 경고: Mockito inline mock maker의 dynamic agent self-attach 경고가 남아 있다. Java 21에서는 실패 요소가 아니며, 향후 JDK에서 필요하면 Mockito javaagent 설정으로 별도 처리한다.

## 11단계: CI/CD 전환

- [x] `.github/workflows/ci.yml` 수정
  - [x] `Set up JDK 8` -> `Set up JDK 21`
  - [x] `java-version: '8'` -> `java-version: '21'`
- [x] Maven build/test 명령 유지 확인
- [x] Cloudtype 설정 수정
  - [x] `app: java@8` 변경
  - [x] `options.jdk: "8"` 변경
  - [x] Java 21 지원 런타임 값 확인 후 적용
- [x] Scouter Agent 확인
  - [x] Java 21에서 agent 로딩 성공
  - [x] Jakarta servlet trace 지원 여부 확인
  - [x] 운영 로그에 `Starting Application with Scouter Agent...` 확인
- [x] CI 실행 확인
- [x] Cloudtype 배포 확인

완료 조건:

- [x] GitHub Actions가 Java 21로 빌드/테스트한다.
- [x] Cloudtype 운영 배포가 Java 21로 정상 기동한다.

11단계 기록:

- `.github/workflows/ci.yml`의 GitHub Actions 빌드 JDK를 Temurin 21로 변경했다.
  - `Set up JDK 8` -> `Set up JDK 21`
  - `java-version: '8'` -> `java-version: '21'`
- Cloudtype deploy YAML을 Java 21 기준으로 변경했다.
  - `app: java@8` -> `app: java@21`
  - `options.jdk: "8"` -> `options.jdk: "21"`
- Maven build/test 명령은 유지했다.
  - package: `mvn -B package --file pom.xml -Dspring.profiles.active=test -DskipTests`
  - test: `mvn test -Dspring.profiles.active=test -e -X`
- Scouter Agent 파일 검증 단계와 start command는 유지했다.
  - `test -f agent/scouter/scouter.agent.jar`
  - `/tmp/scouter.conf` 생성
  - `JAVA_TOOL_OPTIONS="-javaagent:./target/scouter/scouter.agent.jar -Dscouter.config=/tmp/scouter.conf"`
- `mvn -B package -DskipTests`: 성공
- 남은 확인:
  - GitHub Actions 실제 실행 성공
  - Cloudtype가 `java@21`/`jdk: "21"` 설정을 정상 인식하는지 확인
  - 운영 로그에 `Starting Application with Scouter Agent...` 출력 확인
  - Java 21 + Boot 3/Jakarta 환경에서 Scouter trace 정상 수집 확인

## 12단계: 주요 기능 수동 검증

- [x] 전체 테스트 실행
  - [x] `mvn test "-Dspring.profiles.active=local"`
- [x] CI profile 테스트 실행
  - [x] `mvn test "-Dspring.profiles.active=test"` 로컬 실행 결과 기록
- [x] GitHub Actions 테스트 실행 확인

- [x] 로그인
  - [x] 성공
  - [x] 실패
  - [x] 잠금/실패 카운트 영향 확인
- [x] 로그아웃
- [x] 좌측 메뉴 렌더링
- [x] 관리자 권한 접근
- [x] 비로그인 접근 redirect
- [x] `/api/user/**` 공개 API
  - [x] `/api/user/check-id`
  - [x] `/api/user/signUp`
  - [x] `/api/user/templates`
  - [x] `/api/user/templates/{tplSeq}`
- [x] 운동 계획 화면
  - [x] 목록
  - [x] 생성
  - [x] 수정
  - [x] 삭제
  - [x] SWIM cycle 입력
- [x] 템플릿 화면
  - [x] 목록
  - [x] 상세
  - [x] 생성
  - [x] 수정
  - [x] 삭제
  - [x] 정렬
- [x] 게시판/YE table
  - [x] 목록
  - [x] 상세
  - [x] 작성
  - [x] 이미지 업로드
  - [x] prod 환경 변수 기준 실제 COS/S3 업로드
  - [x] 엑셀 다운로드

완료 조건:

- [x] 핵심 사용자 흐름이 Java 21 + Boot 3.5.14 환경에서 정상 동작한다.

12단계 기록:

- `mvn test "-Dspring.profiles.active=local"`: 성공, 40 tests, 0 failures, 0 errors
- `mvn test "-Dspring.profiles.active=test"`: 로컬에서 실패
  - 실패 원인: 로컬 `localhost:3306` MariaDB 인증 방식 차이로 `RSA public key is not available client side` 발생
  - `test` profile은 GitHub Actions MariaDB 서비스 전용으로 유지한다.
- GitHub Actions와 Cloudtype 배포가 동작했고, Java 21 런타임으로 서비스가 기동됐다.
- 배포 서버 로그에서 Scouter Agent 로딩을 확인했다.
  - `Starting Application with Scouter Agent...`
  - `Scouter version 2.20.0`
  - `HTTP jakarta/servlet/http/HttpServlet`
  - Spring REST URL 매핑 및 HTTP client instrumentation 확인
- Scouter Client 패널 오류는 앱/agent 문제가 아니라 로컬 Scouter Client 설정/패널 캐시 문제로 확인했고, `.scouter` 초기화 후 정상 접속됐다.
- 사용자가 배포 환경에서 로그인/로그아웃, 관리자 접근, 공개 API, 운동 계획, 템플릿, 게시판/YE table, 이미지 업로드 및 COS/S3 업로드 흐름을 수동 확인했다.

## 최종 완료 조건

- [x] Java 21로 로컬 빌드 성공
- [x] Java 21로 전체 테스트 성공
- [x] GitHub Actions 성공
- [x] Cloudtype 배포 성공
- [x] 로그인/로그아웃/관리자 화면 정상
- [x] `/api/user/**` 공개 API 정상
- [x] COS/S3 업로드 정상
- [x] Scouter Agent 정상
- [ ] `docs/java21-upgrade-investigation.md`와 본 체크리스트가 실제 결과 기준으로 업데이트됨
