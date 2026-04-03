# PRD: 운동 템플릿 관리 기능 고도화 (수정/삭제) - 완료

## 1. 개요
현재 `springbootYeahn` 프로젝트의 운동 템플릿 관리 시스템에 기존에 없던 수정 및 삭제 기능을 구현하여 관리 효율성을 높였습니다.

## 2. 목표 (달성)
- **템플릿 수정**: 기존 템플릿의 마스터 정보 및 연결된 상세 운동 목록을 유지/변경/삭제할 수 있는 기능 구현 완료.
- **템플릿 삭제**: 사용하지 않는 템플릿을 안전하게 Soft Delete 처리하는 기능 구현 완료.

## 3. 상세 요구사항 및 구현 내용

### 3.1 템플릿 수정 (Template Edit)
- **UI/UX**: 
    - 상세 보기(`templateView`)에서 '수정' 버튼 클릭 시 `openTemplateForm` 호출을 통해 수정 모드로 전환.
    - 기존 템플릿 이름, 단계, 운동 목록(카테고리, 종류, 이름, 메모 등)이 정확히 로드됨.
    - 각 운동 항목의 ID(`tplAttrSeq`)를 유지하여 불필요한 데이터 증식을 방지.
- **Backend API**: 
    - `POST /api/exercise/templates/update`: 
        - 마스터 정보(`TB_EXER_TPL`) 업데이트.
        - 운동 상세(`TB_EXER_ATTR`)는 ID 존재 시 **Update**, 부재 시 **Insert** 처리.
        - 요청에서 제외된 기존 항목은 `DEL_YN = 'Y'` 처리.

### 3.2 템플릿 삭제 (Template Delete)
- **UI/UX**: 
    - '삭제' 버튼 클릭 시 브라우저 컨펌창을 통해 확인 절차 수행.
    - 삭제 완료 후 목록 갱신 및 초기 화면 이동.
- **Backend API**: 
    - `POST /api/exercise/templates/delete/{tplSeq}`:
        - 마스터 정보 및 연결된 모든 운동 상세를 **Soft Delete** 처리.
        - 매핑 관계(`TB_EXER`)는 **물리 삭제**하여 데이터 정합성 유지.

## 4. 기술적 해결 사항
- **데이터 정합성**: 마스터-상세 관계를 고려하여 상세 데이터를 먼저 처리한 후 관계를 정리하는 순서로 로직 최적화.
- **MyBatis 문법**: MySQL의 `UPDATE ... JOIN` 문법을 활용하여 효율적인 다중 테이블 상태 변경 처리.
- **테스트 보장**: `TemplateServiceUnitTest` 및 `TemplateServiceIntegrationTest`를 통해 업데이트/추가/삭제 복합 시나리오 검증 완료.
