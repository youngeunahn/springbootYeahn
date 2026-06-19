# JWT 보완 작업

작성일: 2026-06-13

목표: 프론트엔드 JWT 로그인 연동에 맞춰 `/api/user/**` 인증 정책을 정리하고, 새 API가 기본적으로 보호되도록 JWT 검증 로직과 보안 설정을 보완한다.

## 결정된 정책

- [x] `/api/user/**`는 기본적으로 JWT 인증이 필요하다.
- [x] 공개 API는 method와 path를 함께 기준으로 판단한다.
- [x] 공개 API 판정은 `SecurityConfig`와 `JwtAuthenticationFilter`에서 같은 기준을 사용한다.
- [x] JWT는 직접 구현한 `HS256` 서명 검증 로직을 사용한다.
- [x] JWT header는 `alg = HS256`, `typ = JWT`만 허용한다.
- [x] 프론트엔드는 JWT를 `Authorization: Bearer ...` 헤더로 전달한다.

## 공개 API 범위

현재 JWT 없이 접근 가능한 `/api/user` API는 다음으로 제한한다.

- [x] `POST /api/user/login`
- [x] `POST /api/user/signUp`
- [x] `GET /api/user/check-id`
- [x] `GET /api/user/templates`
- [x] `GET /api/user/templates/**`

그 외 `/api/user/**` 경로는 기본적으로 JWT 인증이 필요하다. 새 API를 추가할 때 공개가 필요한 API만 `PublicUserApiPaths`에 명시하고, 명시하지 않은 API는 보호 API로 둔다.

예를 들어 사용자 프론트의 운동계획 저장 API인 `POST /api/user/plans`는 공개 API에 포함하지 않으므로 JWT 인증이 필요하다.

## 작업 항목

- [x] `/api/user/templates/**` 공개 범위 제한
  - [x] path만 보던 공개 API 판정을 method + path 기준으로 변경
  - [x] `GET /api/user/templates`, `GET /api/user/templates/**`만 공개 유지
  - [x] `POST`, `PUT`, `DELETE` 등 쓰기 API는 JWT 인증 필요
- [x] 로그인 요청 body null 처리
  - [x] `UserApiController.login()`에서 `loginRequest == null` 검증 추가
  - [x] body가 `null`이거나 필수값이 없으면 `400 Bad Request`와 실패 `ResponseDto` 반환
  - [x] 아이디/비밀번호 인증 실패는 기존처럼 `401 Unauthorized` 유지
- [x] 공개 API 목록 중복 관리 개선
  - [x] 공개 API 판정 클래스를 `PublicUserApiPaths`로 분리
  - [x] `SecurityConfig`에서 `PublicUserApiPaths::matches` 사용
  - [x] `JwtAuthenticationFilter`에서 같은 `PublicUserApiPaths` 사용
- [x] JWT header 검증 추가
  - [x] `JwtService.parseAndValidate()`에서 header JSON 파싱
  - [x] `alg`가 `HS256`인지 확인
  - [x] `typ`가 `JWT`인지 확인
  - [x] header 값이 없거나 기대값과 다르면 토큰 검증 실패 처리

## 변경 파일

- `src/main/java/com/yeahn/security/jwt/PublicUserApiPaths.java`
- `src/main/java/com/yeahn/security/config/SecurityConfig.java`
- `src/main/java/com/yeahn/security/jwt/JwtAuthenticationFilter.java`
- `src/main/java/com/yeahn/security/jwt/JwtService.java`
- `src/main/java/com/yeahn/auth/controller/UserApiController.java`
- `src/main/java/com/yeahn/plan/controller/UserPlanApiController.java`
- `test/main/java/com/yeahn/security/jwt/JwtServiceTest.java`
- `test/main/java/com/yeahn/security/jwt/JwtApiSecurityTest.java`
- `D:\Projects\yeahn-fitbase\src\api\plans.ts`

## 검증 기록

- `mvn -B package -DskipTests`: 성공
- `npm run build` (`D:\Projects\yeahn-fitbase`): 성공
- `mvn test "-Dtest=JwtServiceTest"`: 성공
- `mvn test "-Dspring.profiles.active=test" "-Dtest=JwtApiSecurityTest"`: 실패
  - 실패 원인: 애플리케이션 컨텍스트 로딩 중 test profile DB 접속 실패
  - 핵심 메시지: `RSA public key is not available client side`
  - JWT 코드 컴파일 오류나 테스트 assertion 실패가 아니라 로컬 MariaDB 인증 설정 문제다.

## 완료 조건

- [x] 새 `/api/user/**` API가 기본적으로 JWT 인증 대상이 된다.
- [x] 공개 API는 명시된 method + path 조합만 허용된다.
- [x] Security 설정과 JWT 필터가 같은 공개 API 기준을 사용한다.
- [x] 로그인 요청 body null 케이스가 500이 아니라 400으로 처리된다.
- [x] JWT header의 `alg`, `typ` 값이 발급 정책과 일치하는지 검증된다.
- [x] 단위 수준 JWT 검증 테스트가 통과한다.

## 참고

- `Clock.systemUTC()`는 토큰 발급과 만료 계산 기준 시간을 UTC로 가져오기 위한 설정이다. JWT의 `iat`, `exp`는 epoch seconds라 한국 시간으로 별도 변환하지 않아도 만료 계산은 동일하게 동작한다.
- 운영에서는 `security.jwt.secret` 또는 `SECURITY_JWT_SECRET`을 충분히 긴 랜덤 문자열로 설정해야 한다.
- 새 API를 공개해야 하는 경우에만 `PublicUserApiPaths`에 method와 path를 명시한다.
