# 테스트 코드 개선 항목 (Codex 리뷰 결과)

## 우선순위 1 — S3/COS Mock 분리

- [ ] `PlanServiceIntegrationTest`, `TemplateServiceIntegrationTest`에 `@MockBean AmazonS3 amazonS3Client` 추가
  - 전체 `@SpringBootTest` 컨텍스트 로딩 시 실 S3 의존 제거

## 우선순위 2 — SecurityContext 정리

- [ ] `PlanServiceIntegrationTest` — `@AfterEach SecurityContextHolder.clearContext()` 추가 (현재 누락, 42행)
- [ ] `TemplateServiceUnitTest` — `@AfterEach SecurityContextHolder.clearContext()` 추가 (현재 누락, 45행)

## 우선순위 3 — 감사 필드 검증 강화

- [ ] `PlanServiceUnitTest` — `ArgumentCaptor<PlanVo>` / `ArgumentCaptor<PlanDetailVo>`로 mapper 전달 객체의 `insUserId`, `insIp`, `updUserId`, `updIp`, 정렬순서, FK 검증 추가 (130-135, 224-225)
- [ ] `TemplateServiceUnitTest` — `ArgumentCaptor<TemplateDto>`로 `useYn`, `delYn`, `insUserId`, `tplTypeCode` 전파 검증 추가

## 우선순위 4 — Soft Delete / 목록 필터 검증

- [ ] `PlanServiceIntegrationTest` — soft delete 후 `getPlanDetail()` 빈 결과 반환 검증 추가
- [ ] `TemplateServiceIntegrationTest` — 삭제 후 `getTplList()` / `searchTplList()`에서 삭제 데이터 제외 여부 검증 (123-127)
- [ ] `TemplateServiceIntegrationTest` — 템플릿 수정 시 기존 relation 유지·삭제·신규 생성 DB 직접 검증 (238-259)

## 우선순위 5 — 기타 단위 테스트 개선

- [ ] `PlanServiceUnitTest` — `lenient()` 전역 사용 제거 또는 필요한 테스트에만 국소 적용 (46-49)
- [ ] `PlanServiceUnitTest` — 목록 조회 시 size 외 반환 객체 필드 보존 검증 추가
- [ ] `TemplateServiceUnitTest` — `request.getExercises()`가 빈 리스트인 경우, 기존 운동 전체 삭제 시나리오 테스트 추가
