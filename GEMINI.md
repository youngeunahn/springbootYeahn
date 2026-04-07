# springbootYeahn 기술 명세

이 문서는 프로젝트의 핵심 아키텍처와 기술 스택, 개발 컨벤션을 정의합니다. 
상세한 구현 로직은 각 전문 서브에이전트(`springboot-expert`, `frontend-expert`, `mybatis-expert`, `test-expert`)의 지침을 따르십시오.

## 핵심 기술 스택

- **Backend:** Java 1.8 (JDK 1.8), Spring Boot 2.6.11 (Maven)
- **Database:** MariaDB / MyBatis 3.4.6 (log4jdbc 기반 SQL 로깅)
- **Security:** Spring Security (WebSecurityConfigurerAdapter) / BCrypt
- **UI/UX:** Mustache, Bootstrap 4, jQuery, jQuery UI, jqGrid, C3.js
- **Storage:** IBM Cloud Object Storage (S3 API 호환)
- **Logging:** Logback + Log4jdbc

## 개발 컨벤션 및 가이드라인

### 1. 프로젝트 구조
- **Controller Layer:** 일반 뷰(@Controller)와 JSON API(@ResponseBody)를 목적에 맞게 병행 사용.
- **Testing:** **중요!** `test/main/java` 소스 루트를 사용하여 테스트 코드를 관리함 (표준 Maven 경로와 다름).
- **Security:** `/login`, `/signUp`을 제외한 모든 경로는 `ADMIN` 권한을 강제하며, Naver Lucy XSS Filter를 적용.

### 2. 데이터 처리 및 UI 원칙
- **Soft Delete:** 물리적 삭제 대신 `DEL_YN = 'Y'` 처리를 기본으로 함.
- **SQL 최적화:** 대량 업데이트 시 MyBatis `<foreach>`와 `CASE WHEN` 구문을 사용하여 단일 쿼리로 처리.
- **UI 일관성:** 커스텀 스타일 지양, **Bootstrap 4 유틸리티 클래스**를 최우선으로 활용하여 일관된 디자인 유지.
- **운동 계획 도메인 규칙:** 
    - 수영(SWIM) 유형의 경우 페이즈(Phase)별로 'Cycle(시간)포함' 옵션을 제공하며, 활성화 시 `cycle` 필드(인터벌 시간)가 데이터 모델에 추가됨.
    - 수영 모드의 표준 UI는 상황에 따라 4컬럼(기본) 또는 5컬럼(Cycle 포함)으로 동적 전환되는 그리드 체계를 따름.

## API 통신 규약
- **공통 경로**: `/api/exercise/templates/**`
- **주요 기능**: 생성(`/create`), 수정(`/update`), 삭제(`/delete/{seq}`), 정렬(`/reorder`) 등의 API 패턴 준수.

---
상세 비즈니스 로직 및 UI/UX 구현 상세는 서브에이전트에게 문의하십시오.
