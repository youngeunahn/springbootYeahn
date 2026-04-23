package com.yeahn.plan.service;

import com.yeahn.Application;
import com.yeahn.plan.dto.PlanDetailVo;
import com.yeahn.plan.dto.PlanVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PlanService 통합 테스트
 * 데이터베이스 연동을 통한 Master-Detail CRUD 및 데이터 정합성을 검증합니다.
 */
@SpringBootTest(classes = Application.class)
@Transactional
public class PlanServiceIntegrationTest {

    @Autowired
    private PlanService planService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private MockHttpServletRequest request;
    private final String TEST_USER = "test_user_id";

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(TEST_USER, "password")
        );
        request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
    }

    @Test
    @DisplayName("운동 계획 생성 및 상세 조회 통합 테스트 (정렬 순서 포함)")
    void createAndGetPlan_Integration_Test() {
        // [Given] 테스트 데이터 준비
        String planName = "통합 테스트 계획_" + UUID.randomUUID().toString().substring(0, 5);
        PlanVo createVo = new PlanVo();
        createVo.setPlanName(planName);
        createVo.setPlanDate("2026-04-10");
        createVo.setPlanTypeCode("SWIM");

        PlanDetailVo d1 = new PlanDetailVo();
        d1.setPlanExerName("자유형 500m");
        d1.setPlanPhase("WARM-UP");
        
        PlanDetailVo d2 = new PlanDetailVo();
        d2.setPlanExerName("접영 200m");
        d2.setPlanPhase("MAIN");

        createVo.setDetails(Arrays.asList(d1, d2));

        // [When] 저장 실행
        Integer generatedSeq = planService.savePlan(createVo, request);
        
        // [Then] PK 생성 확인
        assertNotNull(generatedSeq, "저장 후 생성된 PLAN_SEQ가 null입니다.");

        // [Then] 상세 조회 검증
        PlanVo result = planService.getPlanDetail(generatedSeq);
        assertNotNull(result);
        assertEquals(planName, result.getPlanName());
        assertEquals(TEST_USER, result.getUserId());
        assertEquals(2, result.getDetails().size());
        
        // 정렬 순서 및 데이터 정합성 검증
        assertEquals(1, result.getDetails().get(0).getPlanSortOrder());
        assertEquals("자유형 500m", result.getDetails().get(0).getPlanExerName());
        assertEquals(2, result.getDetails().get(1).getPlanSortOrder());
        assertEquals("접영 200m", result.getDetails().get(1).getPlanExerName());
    }

    @Test
    @DisplayName("운동 계획 수정 통합 테스트 (상세 항목 교체)")
    void updatePlan_Integration_Test() {
        // [Given] 초기 데이터 저장
        PlanVo initVo = new PlanVo();
        initVo.setPlanName("초기 계획");
        initVo.setPlanDate("2026-04-01");
        initVo.setDetails(Arrays.asList(new PlanDetailVo()));
        PlanDetailVo detail = new PlanDetailVo();
        detail.setPlanExerName("초기 운동");
        initVo.setDetails(Arrays.asList(detail)); // 상세 항목을 명확히 추가
        Integer planSeq = planService.savePlan(initVo, request);
        assertNotNull(planSeq, "초기 저장 시 planSeq가 생성되어야 합니다.");

        // [When] 데이터 수정 (기존 상세 대체)
        PlanVo updateVo = planService.getPlanDetail(planSeq);
        assertNotNull(updateVo, "저장된 계획을 불러오지 못했습니다.");
        updateVo.setPlanName("수정된 계획명");
        
        PlanDetailVo newDetail = new PlanDetailVo();
        newDetail.setPlanExerName("수정 시 추가된 운동");
        updateVo.setDetails(Arrays.asList(newDetail));

        planService.savePlan(updateVo, request);

        // [Then] 수정 결과 검증
        PlanVo result = planService.getPlanDetail(planSeq);
        assertNotNull(result);
        assertEquals("수정된 계획명", result.getPlanName());
        assertEquals(1, result.getDetails().size());
        assertEquals("수정 시 추가된 운동", result.getDetails().get(0).getPlanExerName());
    }

    @Test
    @DisplayName("운동 계획 수정 시 일부 상세 항목 삭제 검증")
    void updatePlan_PartialDetailDelete_Integration_Test() {
        // [Given] 상세 항목 2개인 계획 생성
        PlanVo vo = new PlanVo();
        vo.setPlanName("부분 삭제 테스트");
        vo.setPlanDate("2026-04-10");
        PlanDetailVo d1 = new PlanDetailVo(); d1.setPlanExerName("운동1");
        PlanDetailVo d2 = new PlanDetailVo(); d2.setPlanExerName("운동2");
        vo.setDetails(Arrays.asList(d1, d2));
        Integer planSeq = planService.savePlan(vo, request);

        // [When] 상세 항목을 1개만 남기고 수정
        PlanVo savedVo = planService.getPlanDetail(planSeq);
        PlanDetailVo keepDetail = savedVo.getDetails().get(0);
        savedVo.setDetails(Arrays.asList(keepDetail)); // d2 제외

        planService.savePlan(savedVo, request);

        // [Then] 삭제 결과 및 DB 상태 검증
        PlanVo result = planService.getPlanDetail(planSeq);
        assertEquals(1, result.getDetails().size());
        assertEquals(keepDetail.getPlanExerName(), result.getDetails().get(0).getPlanExerName());

        // 제외된 데이터가 DB상에서 Soft Delete 되었는지 확인
        Integer deletedCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM TB_PLAN_DETAIL WHERE PLAN_SEQ = ? AND DEL_YN = 'Y'", 
                Integer.class, planSeq);
        assertEquals(1, deletedCount, "제외된 상세 항목은 DEL_YN='Y'여야 합니다.");
    }

    @Test
    @DisplayName("운동 계획 삭제 통합 테스트 (Soft Delete)")
    void deletePlan_Integration_Test() {
        // [Given] 삭제용 계획 생성
        PlanVo vo = new PlanVo();
        vo.setPlanName("삭제 대기");
        vo.setDetails(Arrays.asList(new PlanDetailVo()));
        Integer planSeq = planService.savePlan(vo, request);

        // [When] 삭제 실행
        planService.deletePlan(planSeq, request);

        // [Then] 마스터 및 상세 Soft Delete 확인
        String planDelYn = jdbcTemplate.queryForObject(
                "SELECT DEL_YN FROM TB_PLAN WHERE PLAN_SEQ = ?", String.class, planSeq);
        Integer activeDetailCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM TB_PLAN_DETAIL WHERE PLAN_SEQ = ? AND DEL_YN = 'N'", 
                Integer.class, planSeq);

        assertEquals("Y", planDelYn);
        assertEquals(0, activeDetailCount);
    }

    @Test
    @DisplayName("운동 계획 삭제 후 목록 조회 필터링 검증")
    void deletePlan_ListFiltering_Integration_Test() {
        // [Given] 계획 생성
        PlanVo vo = new PlanVo();
        vo.setPlanName("필터링 테스트");
        vo.setPlanDate("2026-04-10");
        Integer planSeq = planService.savePlan(vo, request);

        // [When] 삭제 전 목록 확인
        List<PlanVo> listBefore = planService.getPlanList(new PlanVo());
        assertTrue(listBefore.stream().anyMatch(p -> p.getPlanSeq().equals(planSeq)));

        // [When] 삭제 실행
        planService.deletePlan(planSeq, request);

        // [Then] 삭제 후 목록에서 제외되었는지 확인
        List<PlanVo> listAfter = planService.getPlanList(new PlanVo());
        assertFalse(listAfter.stream().anyMatch(p -> p.getPlanSeq().equals(planSeq)), 
                "삭제된 계획은 목록 조회 시 필터링되어야 합니다.");
    }
}
