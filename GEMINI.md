# 프로젝트 개요 (springbootYeahn)

이 프로젝트는 Java 8과 Spring Boot 2.6.11을 기반으로 구축된 웹 기반 관리 시스템입니다. 주요 비즈니스 로직은 `yetable` 도메인을 중심으로 구성되어 있으며, 사용자 인증, 권한 관리, 데이터 그리드 노출, 엑셀 다운로드, 이미지 업로드(S3 연동) 등의 기능을 제공합니다.

## 주요 기술 스택

- **Backend:** Java 1.8, Spring Boot 2.6.11
- **Build Tool:** Maven (pom.xml 기반)
- **Database:** MariaDB
- **Persistence:** MyBatis 3.4.6 (MyBatis Spring Boot Starter 2.1.3 사용)
- **Security:** 
  - Spring Security (WebSecurityConfigurerAdapter 기반)
  - Naver Lucy XSS Filter (XSS 방지)
- **Frontend/UI:** 
  - Mustache 템플릿 엔진
  - Bootstrap 4, JQuery, jQuery UI (Sortable 기능 활용), jqGrid, C3.js (차트)
  - CKEditor (이미지 업로드 포함)
- **Storage:** IBM Cloud Object Storage (S3 API 호환)
- **Logging:** Logback + Log4jdbc (SQL 쿼리 로깅 최적화)

### 요구 사항
- JDK 1.8
- Maven 3.x 이상

## 주요 비즈니스 도메인

### 1. 운동 템플릿 관리 (Exercise Template)
- **도메인 위치**: `com.yeahn.template.*`
- **테이블 구조**:
    - `TB_EXER_TPL`: 템플릿의 마스터 정보 (이름, 단계, 정렬 순서 등)를 저장.
    - `TB_EXER_ATTR`: 개별 운동의 속성 (운동명, 종류, 카테고리, 메모 등)을 저장. `TPL_TYPE_CODE`로 운동군(수영, 헬스 등)을 구분.
    - `TB_EXER`: 템플릿(`TPL_SEQ`)과 운동 속성(`TPL_ATTR_SEQ`)을 연결하는 맵핑 테이블.
- **주요 로직**:
    - 템플릿 등록 시 `TemplateService.createTemplate`에서 **트랜잭션**을 통해 [템플릿 생성 -> 개별 운동 생성 -> 관계 연결] 순서로 처리함.
    - `SORT_ORDER`는 시스템 내에서 자동으로 관리됨.
    - `src/main/resources/templates/exercise/template/list.mustache` 파일에서 상세 운동 구성 리스트에 jQuery UI Sortable을 활용한 드래그앤드롭 재정렬 기능을 추가함. (UI에서만 적용)
- **UI 레이아웃**:
    - **상세 뷰**: 각 운동 항목은 한 줄로 표시되며, `[번호] [카테고리|종류] [운동명]` 순서로 배치함. 메모는 항목 하단에 들여쓰기된 별도 블록으로 표시.
    - **화면 전환**: 템플릿 추가 클릭 시 기존 상세 조회 화면(`#templateView`)은 숨기고 등록 폼(`#templateForm`)을 활성화함. (추가 버튼은 계속 노출 유지)

## API 컨벤션
- **경로**: `/api/exercise/templates/**`
- **조회 (`POST`)**: 운동 종류별 카테고리 및 하위 코드 리스트 반환.
- **등록 (`POST`)**: `/api/exercise/templates/create` - `TemplateDto` 객체를 통해 템플릿과 포함된 운동 리스트를 한 번에 저장.

## 프로젝트 구조 및 개발 컨벤션

- **Controller Layer:** `com.yeahn.*.controller` 패키지에 위치하며, `@Controller` 및 `@ResponseBody`를 사용하여 일반 뷰와 JSON API를 처리합니다.
- **Service Layer:** 비즈니스 로직을 처리하며, 주로 인터페이스 기반으로 구현되어 있습니다.
- **DAO/DTO:** 
  - `com.yeahn.*.dao`: MyBatis와 연동되는 인터페이스
  - `com.yeahn.*.dto`: 데이터 전달 객체 (Lombok 활용)
- **Security:**
  - `SecurityConfig.java`: 인증/인가 설정. 기본적으로 `/login`, `/signUp`을 제외한 모든 경로는 `ADMIN` 권한이 필요합니다.
  - `BCryptPasswordEncoder`를 사용하여 비밀번호를 암호화합니다.
- **Testing:**
  - **구조:** 표준 Maven 경로 대신 `test/main/java` 폴더를 소스 루트로 사용합니다.
  - **패키지 명명:** 테스트 클래스는 반드시 `package main.java.com.yeahn...`으로 시작해야 IDE 및 Spring Context 로드 시 충돌이 발생하지 않습니다.
  - **전략:** 로직만 검증할 때는 `Mockito` 기반의 단위 테스트를, DB 연동이 포함된 흐름은 `@SpringBootTest` 기반의 통합 테스트를 수행합니다.
