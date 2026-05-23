package com.yeahn.plan.service;

import com.ibm.cloud.objectstorage.services.s3.AmazonS3;
import com.yeahn.Application;
import com.yeahn.plan.dto.PlanDetailVo;
import com.yeahn.plan.dto.PlanVo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
 *
 * - @SpringBootTest: 실제 Spring 컨텍스트를 전부 올려서 DB까지 연결합니다.
 * - @Transactional: 각 테스트 메서드 종료 후 DB를 자동 롤백해 테스트 간 데이터 오염을 방지합니다.
 * - @MockBean AmazonS3: S3Config가 컨텍스트 로딩 시 실제 COS 서버에 연결을 시도하므로
 *   Mock으로 교체해 CI 환경에서도 컨텍스트가 정상 기동되게 합니다.
 */
@SpringBootTest(classes = Application.class)
@Transactional
public class PlanServiceIntegrationTest {

    // @EnableCOS가 "client" 빈을, S3Config가 "amazonS3Client" 빈을 각각 등록합니다.
    // 타입만으로 @MockBean하면 두 빈 중 어느 것을 대체할지 알 수 없으므로 이름을 명시합니다.
    @MockBean(name = "amazonS3Client")
    private AmazonS3 amazonS3ClientMock;

    @MockBean(name = "client")
    private AmazonS3 cosClientMock;

    @Autowired
    private PlanService planService;

    // 서비스 계층을 통하지 않고 DB 상태를 직접 확인할 때 사용 (DEL_YN 등 내부 필드 검증)
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private MockHttpServletRequest request;
    private final String TEST_USER = "test_user_id";

    @BeforeEach
    void setUp() {
        // PlanService 내부에서 SecurityContextHolder.getContext().getAuthentication().getName()으로
        // 로그인 사용자 ID를 가져오기 때문에 테스트용 인증 정보를 미리 주입합니다.
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(TEST_USER, "password")
        );
        request = new MockHttpServletRequest();
        request.setRemoteAddr("10.10.10.10");
    }

    @AfterEach
    void tearDown() {
        // 테스트 격리: SecurityContext를 그대로 두면 다음 테스트의 인증 상태에 영향을 줄 수 있으므로
        // 매 테스트 후 반드시 초기화합니다.
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("운동 계획 생성 후 정렬 순서를 포함해 상세 조회한다")
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
        Long generatedSeq = planService.savePlan(createVo, request);

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
    @DisplayName("운동 계획 수정 시 상세 항목을 교체한다")
    void updatePlan_Integration_Test() {
        // [Given] 초기 데이터 저장
        PlanVo initVo = new PlanVo();
        initVo.setPlanName("초기 계획");
        initVo.setPlanDate("2026-04-01");
        PlanDetailVo detail = new PlanDetailVo();
        detail.setPlanExerName("초기 운동");
        initVo.setDetails(Arrays.asList(detail));
        Long planSeq = planService.savePlan(initVo, request);
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
    @DisplayName("운동 계획 수정 시 제외된 상세 항목을 소프트 삭제한다")
    void updatePlan_PartialDetailDelete_Integration_Test() {
        // [Given] 상세 항목 2개인 계획 생성
        PlanVo vo = new PlanVo();
        vo.setPlanName("부분 삭제 테스트");
        vo.setPlanDate("2026-04-10");
        PlanDetailVo d1 = new PlanDetailVo(); d1.setPlanExerName("운동1");
        PlanDetailVo d2 = new PlanDetailVo(); d2.setPlanExerName("운동2");
        vo.setDetails(Arrays.asList(d1, d2));
        Long planSeq = planService.savePlan(vo, request);

        // [When] 상세 항목을 1개만 남기고 수정
        PlanVo savedVo = planService.getPlanDetail(planSeq);
        PlanDetailVo keepDetail = savedVo.getDetails().get(0);
        savedVo.setDetails(Arrays.asList(keepDetail)); // d2 제외

        planService.savePlan(savedVo, request);

        // [Then] 서비스 계층: 남은 항목 수 확인
        PlanVo result = planService.getPlanDetail(planSeq);
        assertEquals(1, result.getDetails().size());
        assertEquals(keepDetail.getPlanExerName(), result.getDetails().get(0).getPlanExerName());

        // [Then] DB 직접 조회: 제외된 항목이 물리 삭제가 아닌 소프트 삭제(DEL_YN='Y') 되었는지 확인
        // 소프트 삭제는 데이터를 실제로 지우지 않고 DEL_YN 플래그만 변경합니다.
        Integer deletedCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM TB_PLAN_DETAIL WHERE PLAN_SEQ = ? AND DEL_YN = 'Y'",
                Integer.class, planSeq);
        assertEquals(1, deletedCount, "제외된 상세 항목은 DEL_YN='Y'여야 합니다.");
    }

    @Test
    @DisplayName("운동 계획 삭제 시 마스터와 상세 항목을 소프트 삭제한다")
    void deletePlan_Integration_Test() {
        // [Given] 삭제용 계획 생성
        PlanVo vo = new PlanVo();
        vo.setPlanName("삭제 대기");
        vo.setDetails(Arrays.asList(new PlanDetailVo()));
        Long planSeq = planService.savePlan(vo, request);

        // [When] 삭제 실행
        planService.deletePlan(planSeq, request);

        // [Then] DB 직접 조회: 마스터 및 상세 모두 소프트 삭제 확인
        String planDelYn = jdbcTemplate.queryForObject(
                "SELECT DEL_YN FROM TB_PLAN WHERE PLAN_SEQ = ?", String.class, planSeq);
        Integer activeDetailCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM TB_PLAN_DETAIL WHERE PLAN_SEQ = ? AND DEL_YN = 'N'",
                Integer.class, planSeq);

        assertEquals("Y", planDelYn);
        assertEquals(0, activeDetailCount);
    }

    @Test
    @DisplayName("삭제된 운동 계획은 상세 조회에서 null을 반환한다")
    void deletePlan_GetDetailReturnsNull_Test() {
        // [Given] 계획 생성
        PlanVo vo = new PlanVo();
        vo.setPlanName("삭제 후 조회 테스트");
        vo.setDetails(Arrays.asList(new PlanDetailVo()));
        Long planSeq = planService.savePlan(vo, request);

        // [When] 삭제 실행
        planService.deletePlan(planSeq, request);

        // [Then] 서비스 계층 확인: DEL_YN='Y'인 계획은 getPlanDetail 쿼리에서 걸러져야 합니다.
        // 쿼리에 "AND DEL_YN = 'N'" 조건이 없으면 이 테스트가 실패해 버그를 조기에 발견할 수 있습니다.
        PlanVo result = planService.getPlanDetail(planSeq);
        assertNull(result, "삭제된 계획은 상세 조회 시 null이어야 합니다.");
    }

    @Test
    @DisplayName("삭제된 운동 계획은 목록 조회에서 제외한다")
    void deletePlan_ListFiltering_Integration_Test() {
        // [Given] 계획 생성
        PlanVo vo = new PlanVo();
        vo.setPlanName("필터링 테스트");
        vo.setPlanDate("2026-04-10");
        Long planSeq = planService.savePlan(vo, request);

        // 삭제 전: 목록에 존재해야 합니다.
        List<PlanVo> listBefore = planService.getPlanList(new PlanVo());
        assertTrue(listBefore.stream().anyMatch(p -> p.getPlanSeq().equals(planSeq)));

        // [When] 삭제 실행
        planService.deletePlan(planSeq, request);

        // [Then] 삭제 후: 목록에서 제외되어야 합니다.
        List<PlanVo> listAfter = planService.getPlanList(new PlanVo());
        assertFalse(listAfter.stream().anyMatch(p -> p.getPlanSeq().equals(planSeq)),
                "삭제된 계획은 목록 조회 시 필터링되어야 합니다.");
    }
}
