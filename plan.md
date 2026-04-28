# 테스트 코드 개선 구현 계획

## STEP 1 — S3/COS Mock 분리

### 대상 파일
- `test/main/java/com/yeahn/plan/service/PlanServiceIntegrationTest.java`
- `test/main/java/com/yeahn/template/service/TemplateServiceIntegrationTest.java`

### 수정 내용
두 파일 모두 `@SpringBootTest` 전체 컨텍스트 로딩 시 `AmazonS3` 빈이 실제 COS 연결을 시도합니다.
클래스 레벨에 아래 필드를 추가합니다.

```java
// import 추가
import com.amazonaws.services.s3.AmazonS3;
import org.springframework.boot.test.mock.mockito.MockBean;

// 클래스 내 필드
@MockBean
private AmazonS3 amazonS3Client;
```

---

## STEP 2 — SecurityContext @AfterEach 추가

### 대상 파일
- `PlanServiceIntegrationTest.java` — `@AfterEach` 자체가 없음 (42행 `setUp`만 존재)
- `TemplateServiceUnitTest.java` — `setUp`에서 `setContext` 하지만 clear 없음 (45행)

### 수정 내용

**PlanServiceIntegrationTest** — `@AfterEach` 메서드 추가:
```java
import org.junit.jupiter.api.AfterEach;

@AfterEach
void tearDown() {
    SecurityContextHolder.clearContext();
}
```

**TemplateServiceUnitTest** — 동일하게 `@AfterEach` 추가:
```java
import org.junit.jupiter.api.AfterEach;

@AfterEach
void tearDown() {
    SecurityContextHolder.clearContext();
}
```

---

## STEP 3 — 감사 필드 검증 추가 (단위 테스트)

### 대상 파일
- `PlanServiceUnitTest.java` — `savePlan_Insert_Test` (130행), `deletePlan_Test` (224행)
- `TemplateServiceUnitTest.java` — 생성/수정/삭제 관련 테스트

### 수정 내용

**PlanServiceUnitTest** — `ArgumentCaptor` 도입:
```java
import org.mockito.ArgumentCaptor;

// savePlan_Insert_Test 내 then 절 추가
ArgumentCaptor<PlanVo> planCaptor = ArgumentCaptor.forClass(PlanVo.class);
verify(planMapper).insertPlan(planCaptor.capture());
PlanVo captured = planCaptor.getValue();
assertEquals("test_user", captured.getInsUserId());
assertEquals("127.0.0.1", captured.getInsIp());

// deletePlan_Test 내 then 절 추가
ArgumentCaptor<PlanVo> deleteCaptor = ArgumentCaptor.forClass(PlanVo.class);
verify(planMapper).deletePlan(deleteCaptor.capture());
assertEquals("test_user", deleteCaptor.getValue().getUpdUserId());
assertEquals("127.0.0.1", deleteCaptor.getValue().getUpdIp());
```

**TemplateServiceUnitTest** — setUp에서 stub 추가 후 ArgumentCaptor 사용:
```java
// setUp에 추가 (현재 stub 없음)
lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
lenient().when(authentication.getName()).thenReturn("test_user");

// 생성 테스트 then 절에 추가
ArgumentCaptor<TemplateDto> captor = ArgumentCaptor.forClass(TemplateDto.class);
verify(templateMapper).insertTpl(captor.capture());
assertEquals("test_user", captor.getValue().getInsUserId());
assertEquals("N", captor.getValue().getDelYn());
```

---

## STEP 4 — lenient() 전역 사용 제거

### 대상 파일
- `PlanServiceUnitTest.java` — 46-49행

### 수정 내용
`setUp`에서 `lenient()` 제거 후, 실제로 stub이 필요한 테스트 케이스에만 `when(...)` 직접 배치합니다.

```java
// 변경 전 (setUp)
lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
lenient().when(authentication.getName()).thenReturn("test_user");
lenient().when(request.getRemoteAddr()).thenReturn("127.0.0.1");

// 변경 후: setUp에서 제거하고 필요한 테스트마다 선언
// savePlan_Insert_Test, savePlan_Update_*, deletePlan_Test 내 given 절에 추가
when(securityContext.getAuthentication()).thenReturn(authentication);
when(authentication.getName()).thenReturn("test_user");
when(request.getRemoteAddr()).thenReturn("127.0.0.1");
// 목록/상세 조회 테스트는 SecurityContext stub 불필요하므로 생략
```

---

## STEP 5 — Soft Delete 후 getPlanDetail null 반환 검증

### 대상 파일
- `PlanServiceIntegrationTest.java`

### 수정 내용
기존 `deletePlan_Integration_Test` 이후 또는 새 테스트로 추가:

```java
@Test
@DisplayName("삭제된 계획은 상세 조회 시 null 반환 검증")
void deletePlan_GetDetailReturnsNull_Test() {
    // given
    PlanVo vo = new PlanVo();
    vo.setPlanName("삭제 후 조회 테스트");
    vo.setDetails(Arrays.asList(new PlanDetailVo()));
    Integer planSeq = planService.savePlan(vo, request);

    // when
    planService.deletePlan(planSeq, request);

    // then
    PlanVo result = planService.getPlanDetail(planSeq);
    assertNull(result, "삭제된 계획은 상세 조회 시 null이어야 합니다.");
}
```

---

## STEP 6 — TemplateServiceIntegrationTest 삭제 필터 및 relation 검증 추가

### 대상 파일
- `TemplateServiceIntegrationTest.java` — 123-127행, 238-259행

### 수정 내용

**삭제 후 목록 제외 검증** (123-127행 테스트 then 절 보강):
```java
// 삭제 실행
templateService.deleteTpl(tplSeq, request);

// 삭제 후 목록에서 제외 확인
List<TemplateDto> listAfter = templateService.getTplList(new TemplateDto());
assertFalse(listAfter.stream().anyMatch(t -> tplSeq.equals(t.getTplSeq())),
    "삭제된 템플릿은 목록 조회에서 제외되어야 합니다.");

// DB 직접: DEL_YN = 'Y' 확인
String delYn = jdbcTemplate.queryForObject(
    "SELECT DEL_YN FROM TB_TEMPLATE WHERE TPL_SEQ = ?", String.class, tplSeq);
assertEquals("Y", delYn);
```

**수정 후 relation 상태 검증** (238-259행 then 절 보강):
```java
// 유지된 relation: DEL_YN = 'N'
Integer keptCount = jdbcTemplate.queryForObject(
    "SELECT COUNT(*) FROM TB_EXER WHERE TPL_SEQ = ? AND DEL_YN = 'N'",
    Integer.class, tplSeq);
assertEquals(기대값, keptCount, "유지된 운동 수가 맞아야 합니다.");

// 제거된 relation: DEL_YN = 'Y'
Integer deletedCount = jdbcTemplate.queryForObject(
    "SELECT COUNT(*) FROM TB_EXER WHERE TPL_SEQ = ? AND DEL_YN = 'Y'",
    Integer.class, tplSeq);
assertEquals(기대값, deletedCount, "삭제된 운동은 soft delete 처리되어야 합니다.");
```

---

## 작업 순서 요약

| STEP | 작업 | 파일 |
|------|------|------|
| 1 | `@MockBean AmazonS3` 추가 | PlanIntegration, TemplateIntegration |
| 2 | `@AfterEach clearContext()` 추가 | PlanIntegration, TemplateUnit |
| 3 | `ArgumentCaptor` 감사 필드 검증 | PlanUnit, TemplateUnit |
| 4 | `lenient()` 전역 → 로컬 이동 | PlanUnit |
| 5 | 삭제 후 `getPlanDetail` null 검증 | PlanIntegration |
| 6 | 삭제 필터 + relation 상태 검증 | TemplateIntegration |
