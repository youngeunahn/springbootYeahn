package com.yeahn.plan.service;

import com.yeahn.Application;
import com.yeahn.plan.dto.PlanDetailVo;
import com.yeahn.plan.dto.PlanVo;
import com.yeahn.plan.service.PlanService;
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
    @DisplayName("운동 계획 생성 및 상세 조회 통합 테스트")
    void createAndGetPlan_Integration_Test() {
        String planName = "TEST_" + UUID.randomUUID().toString().substring(0, 5);
        PlanVo vo = new PlanVo();
        vo.setPlanName(planName);
        vo.setPlanDate("2026-04-10");
        
        PlanDetailVo d1 = new PlanDetailVo();
        d1.setPlanExerName("EXER1");
        vo.setDetails(Arrays.asList(d1));

        Integer seq = planService.savePlan(vo, request);
        assertNotNull(seq);

        PlanVo result = planService.getPlanDetail(seq);
        assertNotNull(result);
        assertEquals(planName, result.getPlanName());
    }

    @Test
    @DisplayName("운동 계획 수정 통합 테스트")
    void updatePlan_Integration_Test() {
        // 1. Initial Save
        PlanVo vo = new PlanVo();
        vo.setPlanName("INIT");
        vo.setPlanDate("2026-04-01");
        PlanDetailVo d = new PlanDetailVo();
        d.setPlanExerName("INIT_EXER");
        vo.setDetails(Arrays.asList(d));

        Integer seq = planService.savePlan(vo, request);
        assertNotNull(seq);

        // 2. Load and Update
        PlanVo updateVo = planService.getPlanDetail(seq);
        assertNotNull(updateVo);
        updateVo.setPlanName("UPDATED");
        
        PlanDetailVo d2 = new PlanDetailVo();
        d2.setPlanExerName("UPDATED_EXER");
        updateVo.setDetails(Arrays.asList(d2));

        planService.savePlan(updateVo, request);

        // 3. Verify
        PlanVo result = planService.getPlanDetail(seq);
        assertEquals("UPDATED", result.getPlanName());
        assertEquals(1, result.getDetails().size());
    }

    @Test
    @DisplayName("운동 계획 수정 시 일부 상세 항목 삭제 검증")
    void updatePlan_PartialDetailDelete_Integration_Test() {
        PlanVo vo = new PlanVo();
        vo.setPlanName("PARTIAL");
        vo.setPlanDate("2026-04-10");
        PlanDetailVo d1 = new PlanDetailVo(); d1.setPlanExerName("E1");
        PlanDetailVo d2 = new PlanDetailVo(); d2.setPlanExerName("E2");
        vo.setDetails(Arrays.asList(d1, d2));
        Integer seq = planService.savePlan(vo, request);

        PlanVo saved = planService.getPlanDetail(seq);
        saved.setDetails(Arrays.asList(saved.getDetails().get(0)));

        planService.savePlan(saved, request);

        PlanVo result = planService.getPlanDetail(seq);
        assertEquals(1, result.getDetails().size());
    }

    @Test
    @DisplayName("운동 계획 삭제 통합 테스트")
    void deletePlan_Integration_Test() {
        PlanVo vo = new PlanVo();
        vo.setPlanName("DELETE");
        vo.setDetails(Arrays.asList(new PlanDetailVo()));
        Integer seq = planService.savePlan(vo, request);

        planService.deletePlan(seq, request);

        String delYn = jdbcTemplate.queryForObject("SELECT DEL_YN FROM TB_PLAN WHERE PLAN_SEQ = ?", String.class, seq);
        assertEquals("Y", delYn);
    }

    @Test
    @DisplayName("운동 계획 삭제 후 목록 조회 필터링 검증")
    void deletePlan_ListFiltering_Integration_Test() {
        PlanVo vo = new PlanVo();
        vo.setPlanName("FILTER");
        vo.setPlanDate("2026-04-10");
        Integer seq = planService.savePlan(vo, request);

        planService.deletePlan(seq, request);

        List<PlanVo> list = planService.getPlanList(new PlanVo());
        assertFalse(list.stream().anyMatch(p -> p.getPlanSeq().equals(seq)));
    }
}
