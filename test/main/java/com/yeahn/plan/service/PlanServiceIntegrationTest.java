package main.java.com.yeahn.plan.service;

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
    @DisplayName("운동 계획 생성 및 상세 조회 통합 테스트")
    void createAndGetPlan_Integration_Test() {
        // [Given] 테스트 데이터 준비
        String planName = "통합 테스트 계획_" + UUID.randomUUID().toString().substring(0, 5);
        PlanVo createVo = new PlanVo();
        createVo.setPlanName(planName);
        createVo.setPlanDate("2026-04-08 10:00:00");
        createVo.setPlanTypeCode("SWIM");

        PlanDetailVo d1 = new PlanDetailVo();
        d1.setPlanExerName("자유형 500m");
        d1.setPlanPhase("MAIN");
        
        PlanDetailVo d2 = new PlanDetailVo();
        d2.setPlanExerName("접영 200m");
        d2.setPlanPhase("MAIN");

        createVo.setDetails(Arrays.asList(d1, d2));

        // [When] 저장 실행
        Integer generatedSeq = planService.savePlan(createVo, request);
        
        // [Then] PK 생성 확인
        assertNotNull(generatedSeq, "저장 후 생성된 PLAN_SEQ가 null입니다. MyBatis useGeneratedKeys 설정을 확인하세요.");

        // [Then] 상세 조회 검증
        PlanVo result = planService.getPlanDetail(generatedSeq);
        assertNotNull(result, "조회된 PlanVo가 null입니다. DB에 데이터가 정상적으로 인서트되었는지 확인하세요.");
        assertEquals(planName, result.getPlanName());
        assertEquals(TEST_USER, result.getUserId());
        assertEquals(2, result.getDetails().size());
    }

    @Test
    @DisplayName("운동 계획 수정 통합 테스트")
    void updatePlan_Integration_Test() {
        PlanVo initVo = new PlanVo();
        initVo.setPlanName("초기 계획");
        initVo.setPlanDate("2026-04-01 10:00:00");
        initVo.setDetails(Arrays.asList(new PlanDetailVo()));
        Integer planSeq = planService.savePlan(initVo, request);

        PlanVo updateVo = new PlanVo();
        updateVo.setPlanSeq(planSeq);
        updateVo.setPlanName("수정된 계획명");
        updateVo.setPlanDate("2026-04-08 11:00:00");
        
        PlanDetailVo newDetail = new PlanDetailVo();
        newDetail.setPlanExerName("새로운 운동 항목");
        updateVo.setDetails(Arrays.asList(newDetail));

        planService.savePlan(updateVo, request);

        PlanVo result = planService.getPlanDetail(planSeq);
        assertNotNull(result);
        assertEquals("수정된 계획명", result.getPlanName());
        assertEquals("새로운 운동 항목", result.getDetails().get(0).getPlanExerName());
    }

    @Test
    @DisplayName("운동 계획 삭제 통합 테스트")
    void deletePlan_Integration_Test() {
        PlanVo vo = new PlanVo();
        vo.setPlanName("삭제 대기");
        vo.setDetails(Arrays.asList(new PlanDetailVo()));
        Integer planSeq = planService.savePlan(vo, request);

        planService.deletePlan(planSeq, request);

        String planDelYn = jdbcTemplate.queryForObject(
                "SELECT DEL_YN FROM TB_PLAN WHERE PLAN_SEQ = ?", String.class, planSeq);
        assertEquals("Y", planDelYn);
    }
}
