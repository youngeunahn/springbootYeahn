# Swagger/OpenAPI 추가 작업

작성일: 2026-05-27

목표: Java 21 + Spring Boot 3.5.14 전환 이후 Swagger/OpenAPI를 추가하고, 관리자 로그인 사용자만 API 문서를 볼 수 있게 제한한다.

## 결정된 버전과 정책

- [x] springdoc OpenAPI: `springdoc-openapi-starter-webmvc-ui 2.8.17`
- [x] springdoc 3.x 라인은 이번 범위에서 제외
- [x] Swagger UI 접근 권한: `ADMIN` 권한 필요
- [x] OpenAPI JSON 접근 권한: `ADMIN` 권한 필요
- [x] 문서화 범위: 우선 `/api/user/templates/**` 공개 템플릿 API 중심
- [x] `user-api-controller` 회원가입/아이디 확인 API는 문서 노출 제외
- [x] 관리자 API는 문서 노출 범위에서 제외

## 작업 항목

- [x] Boot 3 기준 springdoc 의존성 추가
  - [x] `org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.17`
- [x] OpenAPI 기본 설정 클래스 추가
  - [x] `src/main/java/com/yeahn/config/OpenApiConfig.java`
- [x] Security에 Swagger 경로 정책 추가
  - [x] `/swagger-ui/**`: `ADMIN` 권한 필요
  - [x] `/swagger-ui.html`: `ADMIN` 권한 필요
  - [x] `/v3/api-docs/**`: `ADMIN` 권한 필요
  - [x] `/v3/api-docs.yaml`: `ADMIN` 권한 필요
- [x] 문서화 범위 제한
  - [x] `springdoc.paths-to-match=/api/user/**`
  - [x] `springdoc.paths-to-exclude=/api/user/check-id,/api/user/signUp`
- [x] Swagger UI 확인
  - [x] `/swagger-ui/index.html`
  - [x] `/v3/api-docs`
- [x] `ResponseDto<T>` 응답 구조가 문서에 드러나는지 확인
- [x] Swagger 보안 테스트 추가
  - [x] 비로그인 상태에서 `/v3/api-docs`는 로그인 화면으로 redirect
  - [x] 일반 사용자 권한에서 `/v3/api-docs`는 403
  - [x] 관리자 권한에서 `/v3/api-docs`는 200
  - [x] 비로그인 상태에서 `/swagger-ui/index.html`은 로그인 화면으로 redirect
  - [x] 관리자 권한에서 `/swagger-ui/index.html`은 200

## 검증 기록

- `mvn -B package -DskipTests`: 성공
- `mvn test "-Dspring.profiles.active=local" "-Dtest=OpenApiSecurityTest"`: 성공, 5 tests, 0 failures, 0 errors
- `OpenApiSecurityTest`에서 `/v3/api-docs`에 `/api/user/templates`가 포함되고 `/api/user/check-id`, `/api/user/signUp`, `/admin/signUp`이 포함되지 않는지 검증했다.

## 완료 조건

- [x] Java 21 + Boot 3.5.14 환경에서 Swagger UI와 OpenAPI JSON이 정상 동작한다.
- [x] Swagger UI와 OpenAPI JSON은 관리자 로그인 사용자에게만 노출된다.
- [x] 공개 API와 관리자 API 문서 노출 범위가 명확하다.

## 참고

- springdoc은 시작 시 운영에서 문서 노출을 끄려면 `springdoc.api-docs.enabled=false`, `springdoc.swagger-ui.enabled=false`를 설정하라는 warning을 출력한다.
- 현재 정책은 문서를 끄는 방식이 아니라, Spring Security에서 `ADMIN` 권한으로 보호하는 방식이다.
