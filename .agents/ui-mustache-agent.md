# ui-mustache-agent

## Role

이 에이전트는 Spring Boot Mustache, Bootstrap 4, jQuery 기반 UI 변경을 담당한다.

주요 범위는 `src/main/resources/templates`와 `src/main/resources/static`이다.

## Good Tasks

- Mustache 화면 수정
- Bootstrap 4 기반 폼, 카드, 테이블, 모달 수정
- jQuery AJAX 및 이벤트 바인딩 점검
- jqGrid, jqTree, CKEditor, datepicker 연동 화면 점검
- 공통 레이아웃 영향 범위 검토

## Project Structure

- 공통 레이아웃:
  - `layout/header.mustache`
  - `layout/leftmenu.mustache`
  - `layout/gnb.mustache`
  - `layout/footer.mustache`
- 업무 화면:
  - `exercise/plan`
  - `exercise/template`
  - `yetable`
  - `conf`
  - `login`
  - `pay`
- 공통 CSS:
  - `css/bootstrap.min.css`
  - `css/style.css`
  - `css/jquery-ui.css`
  - `css/ui.jqgrid-bootstrap.css`
- 공통 JS:
  - `js/jquery.js`
  - `bootstrap.bundle.js`
  - `jquery-ui.min.js`
  - `jquery.jqGrid.js`
  - `main-js.js`

## Project Patterns

- 대부분 화면은 상단에 `{{> layout/header }}`, `{{> layout/leftmenu }}`, `{{> layout/gnb }}` partial을 포함한다.
- Bootstrap 4 기반의 `card`, `row`, `col-*`, `btn`, `form-control`, `table`, `list-group`, `modal`, `alert` 패턴을 많이 쓴다.
- 화면별 JS는 별도 파일보다 Mustache 안의 `<script>`에 직접 작성하는 방식이 많다.
- jQuery 이벤트 바인딩, `$.ajax`, `$.get`, `$.post`, `location.href` 기반 화면 전환이 많다.
- `yetable/list`는 jqGrid를 사용한다.
- `conf/menu`는 jqTree와 Bootstrap modal을 사용한다.
- `exercise/template`은 list-group, sortable, 동적 폼 clone을 사용한다.
- `exercise/plan`은 jQuery UI datepicker, 동적 DOM 생성, 좌/중/우 3패널 카드 UI를 사용한다.
- 로그인/회원가입은 사이드바 없이 `layout/header`만 사용하고 중앙 카드형 폼으로 구성된다.

## Risks

- `layout/header.mustache`는 거의 모든 화면에 전역 CSS/JS를 로드하므로 수정 영향이 크다.
- `layout/leftmenu.mustache`는 메뉴 렌더링, 사이드바 footer, 모바일 토글에 영향이 있다.
- `style.css` 수정은 전체 대시보드, 카드, 버튼, 폼, 테이블, 로그인 화면까지 번질 수 있다.
- `exercise/plan/list.mustache`와 `exercise/template/list.mustache`는 JS가 길고 동적 DOM을 많이 생성하므로 HTML class/id 변경 시 이벤트 바인딩이 쉽게 깨진다.
- `yetable/create.mustache`는 CKEditor와 `UploadAdapter.js`에 의존한다.
- `conf/menu.mustache`는 jqTree node id, modal id, form id가 AJAX 로직과 직접 연결되어 있다.
- CSRF 토큰 처리 흔적이 화면/정적 JS에 없을 수 있으므로 Spring Security 설정과 함께 확인한다.
- UI 문구 작업 시 UTF-8을 보존한다.

## Verification

- 공통 레이아웃 변경 후 로그인, 메인 대시보드, 좌측 메뉴, 모바일 navbar collapse를 확인한다.
- 폼 변경 후 submit, reset/cancel, Enter 키 처리, 필수값 alert를 확인한다.
- AJAX 화면 변경 후 성공/실패 콜백, JSON payload, URL, `data-*` 속성을 확인한다.
- 동적 DOM 화면 변경 후 delegated event selector를 확인한다.
- `exercise/plan`: 계획 생성/수정/삭제, 날짜 선택, 타입 전환, 검색/필터, 템플릿/기록 탭 확인
- `exercise/template`: 검색, 상세 조회, 추가/수정/삭제, 운동 순서 변경 sortable 확인
- `yetable`: jqGrid 목록, 더블클릭 상세 이동, CKEditor 저장/업로드 확인
- `conf/menu`: 트리 선택, 상세 조회, 수정, 하위 메뉴 modal 생성 확인
- 화면 크기별 Bootstrap grid가 깨지지 않는지 확인한다.

