# 기술 명세 (springbootYeahn)

이 프로젝트는 Java 8과 Spring Boot 2.6.11을 기반으로 구축된 웹 관리 시스템의 기술적 구현 상세를 다룹니다. 기능적인 상세 요구 사항은 [spec.md](spec.md)를 참조하십시오.

## 문서 참조
- [기능 명세서 (spec.md)](spec.md): 비즈니스 로직 및 사용자 요구 사항 정의

## 주요 기술 스택

- **Backend:** Java 1.8, Spring Boot 2.6.11 (Maven)
- **Database:** MariaDB / MyBatis 3.4.6 (log4jdbc 기반 SQL 로깅)
- **Security:** Spring Security (WebSecurityConfigurerAdapter) / BCryptPasswordEncoder
- **UI/UX:** Mustache, Bootstrap 4, jQuery, jQuery UI (Sortable), jqGrid, C3.js
- **Storage:** IBM Cloud Object Storage (S3 API 호환)
- **Logging:** Logback + Log4jdbc (SQL 최적화)

### 개발 요구 사항
- JDK 1.8 / Maven 3.x 이상

## 구현 아키텍처 및 상세

### 1. 운동 템플릿 구현 (Exercise Template)
- **도메인 위치**: `com.yeahn.template.*`
- **테이블 구성**:
    - `TB_EXER_TPL`: 템플릿 마스터 (이름, 단계, 정렬 등)
    - `TB_EXER_ATTR`: 개별 운동 속성 (운동명, 종류, 카테고리 등)
    - `TB_EXER`: 템플릿(`TPL_SEQ`)과 운동 속성(`TPL_ATTR_SEQ`) 연계
- **트랜잭션 및 로직**:
    - `TemplateService.createTemplate`: [마스터 생성 -> 개별 운동 생성 -> 매핑] 과정을 한 트랜잭션으로 처리.
    - `TemplateService.updateTemplate`: ID(`tplAttrSeq`) 유무에 따른 Update/Insert 분기 및 Soft Delete(`DEL_YN = 'Y'`).
### 2. 운동량 관리 구현 (Exercise Plan)
- **도메인 위치**: `com.yeahn.plan.*`
- **테이블 구조(예정)**:
    - `TB_PLAN`: 마스터 정보 (일자, 제목, 유형 등)
    - `TB_PLAN_DETAIL`: 개별 수행 운동 (세트, 횟수, 시간, 메모 등)
- **UI/UX 구현**:
    - **Bootstrap 유틸리티 중심**: 커스텀 `<style>` 태그를 배제하고 Bootstrap 4의 유틸리티 클래스(`card`, `shadow-sm`, `rounded-pill` 등)를 사용하여 디자인 일관성 유지.
    - **동적 폼 제어**: jQuery 클로닝(`clone()`) 및 템플릿 리터럴을 활용하여 운동 항목 행(`exercise-item`)을 동적으로 추가 및 삭제 처리.
    - **날짜 검색 최적화**: jQuery UI Datepicker의 `beforeShow` 콜백과 실시간 좌표 계산을 통해 날짜 팝업의 우측 정렬 강제 구현.
    - **템플릿 라이브러리 연동**: `TemplateService`를 주입받아 등록된 템플릿과 개별 운동을 조회하고, 아코디언 인터랙션(`slideUp/Down`)을 통해 사용자 경험 개선.

## API 통신 규약
- **공통 경로**: `/api/exercise/templates/**`
- **기능별 API (POST)**:
    - `/create`: 템플릿 및 상세 리스트 저장
    - `/update`: 템플릿 마스터/상세 정보 수정
    - `/delete/{tplSeq}`: 정보 삭제 처리
    - `/reorder`: 정렬된 `tplAttrSeq` 및 `tplSortOrder` 기반 배치 업데이트

## 프로젝트 구조 및 개발 컨벤션

- **Controller Layer:** 일반 뷰(`@Controller`)와 JSON API(`@ResponseBody`) 동시 처리.
- **MyBatis 컨벤션:** 순서 변경 및 대량 수정 시 루프 대신 MyBatis `<foreach>`와 `CASE WHEN` 구문을 사용하여 단일 쿼리 성능 최적화.
- **Security:** `/login`, `/signUp` 제외 모든 경로 `ADMIN` 권한 강제 적용. Naver Lucy XSS Filter를 통한 보안 강화.
- **Testing:** 
  - 경로: `test/main/java` 소스 루트 사용 (표준 Maven 경로와 분리)
  - 전략: Mockito 기반 단위 테스트 및 `@SpringBootTest` 기반 통합 테스트 병행.
