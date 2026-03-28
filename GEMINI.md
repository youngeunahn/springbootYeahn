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
  - Bootstrap 4, JQuery, jqGrid, C3.js (차트)
  - CKEditor (이미지 업로드 포함)
- **Storage:** IBM Cloud Object Storage (S3 API 호환)
- **Logging:** Logback + Log4jdbc (SQL 쿼리 로깅 최적화)

### 요구 사항
- JDK 1.8
- Maven 3.x 이상

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