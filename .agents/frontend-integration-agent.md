# frontend-integration-agent

## Role

이 에이전트는 백엔드 `springbootYeahn`과 별도 Next.js 프론트엔드 `D:\Projects\yeahn-fitbase` 사이의 API 연동 변경을 담당한다.

주요 범위는 백엔드의 `/api/user/**` 공개 API와 프론트엔드의 `src/api`, 해당 화면 컴포넌트다.

## Related Projects

- Backend: `D:\Projects\springbootYeahn`
- Frontend: `D:\Projects\yeahn-fitbase`

## Good Tasks

- `/api/user/**` 응답 변경에 맞춰 프론트 API client 수정
- 백엔드 `ResponseDto<T>`와 프론트 TypeScript 타입 정합성 확인
- `src/api/templates.ts`, `src/api/auth.ts`, 관련 page/component 호출부 수정
- 백엔드 Controller 테스트와 프론트 타입/빌드 검증을 함께 확인
- 공개 API와 관리자용 Mustache/jQuery AJAX API 범위 구분

## Backend Contract

- 공개 API 경로는 `/api/user/**`다.
- 응답 wrapper는 기존 `com.yeahn.common.dto.ResponseDto<T>`를 사용한다.
- 응답 필드는 `status`, `message`, `data`다.
- 성공 상태값은 `"SUCCESS"`, 실패 상태값은 `"FAIL"`이다.
- 신규 `ApiResponse` 클래스를 만들지 않는다.
- 관리자용 Mustache/jQuery AJAX API는 이 연동 범위가 아니다.
- 세션 로그인은 `/login` Spring Security form login을 사용한다. JSON controller가 아니므로 요청은 `application/x-www-form-urlencoded`로 `userId`, `password`를 보낸다.
- 로그인 성공/실패를 JSON으로 받으려면 요청에 `Accept: application/json`을 포함해야 한다. 그렇지 않으면 관리자 화면용 redirect/HTML 흐름으로 갈 수 있다.
- 로그인/로그아웃은 세션 쿠키 기반이므로 프론트 fetch에는 `credentials: 'include'`가 필요하다.
- 로그인 인증 실패는 백엔드 실패 메시지 문자열이 아니라 HTTP `401`로 판단한다.
- 로컬 Next.js 직접 호출을 사용할 때 백엔드는 `application-local.properties`에서 `app.user-web.allowed-origins=http://localhost:3000`을 허용해야 한다.

## Frontend Patterns

- `D:\Projects\yeahn-fitbase`는 Next.js 프로젝트다.
- API 공통 함수는 `src/api/client.ts`에 있다.
- 인증/회원 API는 `src/api/auth.ts`에서 이미 `ResponseDto<T>` 타입을 사용한다.
- 템플릿 API는 `src/api/templates.ts`에서 관리한다.
- API helper는 가능하면 `ResponseDto<T>`를 처리한 뒤 화면에는 `data`만 반환해 page/component 변경 범위를 줄인다.
- 로컬 개발에서는 `D:\Projects\yeahn-fitbase\.env.development`의 `NEXT_PUBLIC_API_BASE_URL=http://localhost:8080`을 기준으로 백엔드에 직접 요청한다.
- `NEXT_PUBLIC_API_BASE_URL`이 설정된 상태에서 로그인/로그아웃은 각각 `${NEXT_PUBLIC_API_BASE_URL}/login`, `${NEXT_PUBLIC_API_BASE_URL}/logout`으로 호출한다.
- `/login`은 Next.js 페이지 경로이기도 하므로, base URL 없이 상대 경로로 인증 요청을 보내면 Next 라우트/redirect와 충돌할 수 있다.

## Current Known Calls

- `GET /api/user/check-id`: `src/api/auth.ts`, `ResponseDto<boolean>` 사용
- `POST /api/user/signUp`: `src/api/auth.ts`, `ResponseDto<string>` 사용
- `POST /login`: `src/api/auth.ts`, form-urlencoded `userId`, `password`, `Accept: application/json`, `credentials: 'include'`
- `POST /logout`: `src/api/auth.ts`, `Accept: application/json`, `credentials: 'include'`
- `GET /api/user/templates`: `src/api/templates.ts`, `ResponseDto<Template[]>` 기준으로 처리
- `GET /api/user/templates/{tplSeq}`: 상세 화면이 생기면 같은 방식으로 처리

## Verification

- 백엔드 변경 후 우선 실행:

```bash
mvn test "-Dspring.profiles.active=local" "-Dtest=UserTemplateApiControllerTest"
mvn test "-Dspring.profiles.active=local" "-Dtest=UserApiControllerTest"
mvn -B package -DskipTests
```

- 프론트 변경 후 우선 확인:
  - `npm run lint`
  - `npm run build`
  - 로그인 실패 시 사용자용 실패 문구가 나오고, Network 요청 URL이 `http://localhost:8080/login`인지 확인
  - `/api/user/templates` 목록 화면 수동 확인
- `.env.development` 또는 `application-local.properties`를 바꾸면 프론트 dev 서버와 백엔드를 모두 재시작해야 한다.

## Risks

- 백엔드가 `ResponseDto<T>`로 감싸면 기존 raw array/object를 기대하던 프론트 호출부는 깨진다.
- `/api/user/**`는 anonymous 접근 허용 영역이므로 오류 메시지에 내부 정보나 개인정보를 노출하지 않는다.
- 프론트에서 `status`는 HTTP status가 아니라 백엔드 응답 body의 비즈니스 상태값이다.
- 로그인 실패 사유(`BAD_PASSWORD`, `USER_NOT_FOUND` 등)를 그대로 화면에 노출하지 않는다. 인증 실패는 HTTP `401`을 사용자용 문구로 매핑하고, 그 외 서버 오류만 백엔드 메시지를 검토해 표시한다.
- CORS 오류는 프론트 코드 문제가 아니라 백엔드 `app.user-web.allowed-origins` 또는 실행 프로파일 문제일 수 있다.
