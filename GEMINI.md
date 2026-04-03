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
    - `src/main/resources/templates/exercise/template/list.mustache` 파일에서 상세 운동 구성 리스트에 jQuery UI Sortable을 활용한 드래그앤드롭 재정렬 기능을 구현함.
    - **순서 변경 반영**: Sortable의 `update` 이벤트 시 정렬된 `tplAttrSeq` 리스트를 서버로 전송하여 DB의 `TPL_SORT_ORDER`를 즉시 업데이트함. (성공 시 하단 Toast 알림 제공)
    - **템플릿 수정**: `TemplateService.updateTemplate`에서 마스터 정보를 수정하고, 상세 운동 리스트는 ID(`tplAttrSeq`) 유무에 따라 **Update/Insert**를 분기 처리함. 리스트에서 제외된 항목은 `DEL_YN = 'Y'`로 Soft Delete 처리함.
    - **템플릿 삭제**: `TemplateService.deleteTemplate`에서 마스터(`TB_EXER_TPL`)와 상세 속성(`TB_EXER_ATTR`)은 Soft Delete(`DEL_YN = 'Y'`) 처리하고, 매핑 테이블(`TB_EXER`)의 관계 데이터는 물리 삭제함.
- **UI 레이아웃**:
    - **상세 뷰**: 각 운동 항목은 한 줄로 표시되며, `[번호] [카테고리|종류] [운동명]` 순서로 배치함. 메모는 항목 하단에 들여쓰기된 별도 블록으로 표시. 명칭 표시 시 코드값 대신 DB에서 조회한 `Desc` 값을 사용함.
    - **화면 전환**: 템플릿 추가 또는 수정 클릭 시 기존 상세 조회 화면(`#templateView`)은 숨기고 등록/수정 폼(`#templateForm`)을 활성화함. 수정 시에는 기존 데이터를 폼에 로드하여 표시함.

## API 컨벤션
- **경로**: `/api/exercise/templates/**`
- **조회 (`POST`)**: 운동 종류별 카테고리 및 하위 코드 리스트 반환.
- **등록 (`POST`)**: `/api/exercise/templates/create` - `TemplateDto` 객체를 통해 템플릿과 포함된 운동 리스트를 한 번에 저장.
- **수정 (`POST`)**: `/api/exercise/templates/update` - 변경된 템플릿 마스터 정보 및 운동 리스트를 받아 업데이트 및 항목별 추가/삭제 처리.
- **삭제 (`POST`)**: `/api/exercise/templates/delete/{tplSeq}` - 대상 템플릿 및 관련 정보를 삭제 처리.
- **순서 변경 (`POST`)**: `/api/exercise/templates/reorder` - 변경된 운동 목록(`exercises`)의 `tplAttrSeq`와 `tplSortOrder`를 받아 배치 업데이트 수행.

## 프로젝트 구조 및 개발 컨벤션

- **Controller Layer:** `com.yeahn.*.controller` 패키지에 위치하며, `@Controller` 및 `@ResponseBody`를 사용하여 일반 뷰와 JSON API를 처리합니다.
- **Service Layer:** 비즈니스 로직을 처리하며, 주로 인터페이스 기반으로 구현되어 있습니다.
- **DAO/DTO:** 
  - `com.yeahn.*.dao`: MyBatis와 연동되는 인터페이스
  - `com.yeahn.*.dto`: 데이터 전달 객체 (Lombok 활용)
- **Persistence (MyBatis) 컨벤션:**
  - **Batch Update:** 다량의 데이터나 순서(Order) 변경 시, 루프를 통한 개별 쿼리 대신 MyBatis의 `<foreach>`와 `CASE WHEN` 구문을 사용하여 단일 쿼리로 처리하여 성능을 최적화합니다.
- **Security:**
  - `SecurityConfig.java`: 인증/인가 설정. 기본적으로 `/login`, `/signUp`을 제외한 모든 경로는 `ADMIN` 권한이 필요합니다.
  - `BCryptPasswordEncoder`를 사용하여 비밀번호를 암호화합니다.
- **Testing:**
  - **구조:** 표준 Maven 경로 대신 `test/main/java` 폴더를 소스 루트로 사용합니다.
  - **패키지 명명:** 테스트 클래스는 반드시 `package main.java.com.yeahn...`으로 시작해야 IDE 및 Spring Context 로드 시 충돌이 발생하지 않습니다.
  - **전략:** 로직만 검증할 때는 `Mockito` 기반의 단위 테스트를, DB 연동이 포함된 흐름은 `@SpringBootTest` 기반의 통합 테스트를 수행합니다.
