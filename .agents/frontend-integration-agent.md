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

## Frontend Patterns

- `D:\Projects\yeahn-fitbase`는 Next.js 프로젝트다.
- API 공통 함수는 `src/api/client.ts`에 있다.
- 인증/회원 API는 `src/api/auth.ts`에서 이미 `ResponseDto<T>` 타입을 사용한다.
- 템플릿 API는 `src/api/templates.ts`에서 관리한다.
- API helper는 가능하면 `ResponseDto<T>`를 처리한 뒤 화면에는 `data`만 반환해 page/component 변경 범위를 줄인다.

## Current Known Calls

- `GET /api/user/check-id`: `src/api/auth.ts`, `ResponseDto<boolean>` 사용
- `POST /api/user/signUp`: `src/api/auth.ts`, `ResponseDto<string>` 사용
- `GET /api/user/templates`: `src/api/templates.ts`, `ResponseDto<Template[]>` 기준으로 처리
- `GET /api/user/templates/{tplSeq}`: 상세 화면이 생기면 같은 방식으로 처리

## Verification

- 백엔드 변경 후 우선 실행:

```bash
mvn test "-Dspring.profiles.active=local" "-Dtest=UserTemplateApiControllerTest"
mvn -B package -DskipTests
```

- 프론트 변경 후 우선 확인:
  - `npm run lint`
  - `npm run build`
  - `/api/user/templates` 목록 화면 수동 확인

## Risks

- 백엔드가 `ResponseDto<T>`로 감싸면 기존 raw array/object를 기대하던 프론트 호출부는 깨진다.
- `/api/user/**`는 anonymous 접근 허용 영역이므로 오류 메시지에 내부 정보나 개인정보를 노출하지 않는다.
- 프론트에서 `status`는 HTTP status가 아니라 백엔드 응답 body의 비즈니스 상태값이다.
