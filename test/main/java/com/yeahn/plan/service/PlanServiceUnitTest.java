package com.yeahn.plan.service;

import com.yeahn.plan.dao.PlanMapper;
import com.yeahn.plan.dto.PlanDetailVo;
import com.yeahn.plan.dto.PlanVo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * PlanService 단위 테스트
 *
 * - @ExtendWith(MockitoExtension.class): Mockito의 엄격한 stub 검사를 활성화합니다.
 *   사용되지 않은 stub이 있으면 테스트가 실패하므로 lenient()는 꼭 필요한 경우에만 씁니다.
 * - DB나 Spring 컨텍스트 없이 PlanService 로직만 격리하여 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
class PlanServiceUnitTest {

    @Mock
    private PlanMapper planMapper;

    // @InjectMocks: planMapper를 PlanService 생성자/필드에 자동 주입합니다.
    @InjectMocks
    private PlanService planService;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("test_user", "password")
        );
        // CommonUtils.getIP()가 루프백 IP를 실제 호스트 IP로 치환하므로 비-루프백 IP를 기본값으로 둡니다.
        lenient().when(request.getRemoteAddr()).thenReturn("10.10.10.10");
    }

    @AfterEach
    void tearDown() {
        // 테스트 간 SecurityContext 오염 방지: 각 테스트 후 반드시 초기화합니다.
        SecurityContextHolder.clearContext();
    }

    // ────────────────────────────────────────────────────────────────
    // 조회 테스트
    // ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("운동 계획 목록을 조회한다")
    void getPlanList_Test() {
        // [Given]
        PlanVo planVo = new PlanVo();
        List<PlanVo> expectedList = Arrays.asList(new PlanVo(), new PlanVo());
        when(planMapper.getPlanList(planVo)).thenReturn(expectedList);

        // [When]
        List<PlanVo> result = planService.getPlanList(planVo);

        // [Then]
        assertEquals(expectedList.size(), result.size());
        verify(planMapper, times(1)).getPlanList(planVo);
    }

    @Test
    @DisplayName("운동 계획 상세와 상세 항목을 조회한다")
    void getPlanDetail_Success_Test() {
        // [Given]
        Long planSeq = 1L;
        PlanVo mockPlan = new PlanVo();
        mockPlan.setPlanSeq(planSeq);
        mockPlan.setPlanName("상세 테스트");

        List<PlanDetailVo> mockDetails = Arrays.asList(new PlanDetailVo(), new PlanDetailVo());

        when(planMapper.getPlan(planSeq)).thenReturn(mockPlan);
        when(planMapper.getPlanDetails(planSeq)).thenReturn(mockDetails);

        // [When]
        PlanVo result = planService.getPlanDetail(planSeq);

        // [Then]
        assertNotNull(result);
        assertEquals(planSeq, result.getPlanSeq());
        assertEquals(2, result.getDetails().size());
        verify(planMapper, times(1)).getPlan(planSeq);
        verify(planMapper, times(1)).getPlanDetails(planSeq);
    }

    @Test
    @DisplayName("운동 계획이 없으면 상세 조회에서 null을 반환한다")
    void getPlanDetail_NotFound_Test() {
        // [Given]
        Long planSeq = 999L;
        when(planMapper.getPlan(planSeq)).thenReturn(null);

        // [When]
        PlanVo result = planService.getPlanDetail(planSeq);

        // [Then]
        assertNull(result);
        verify(planMapper, times(1)).getPlan(planSeq);
        // getPlan이 null을 반환하면 getPlanDetails는 호출되지 않아야 합니다.
        verify(planMapper, never()).getPlanDetails(anyLong());
    }

    // ────────────────────────────────────────────────────────────────
    // 저장/삭제 테스트
    // ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("상세 항목이 있는 운동 계획을 신규 저장한다")
    void savePlan_Insert_Test() {
        // [Given]
        PlanVo planVo = new PlanVo();
        planVo.setPlanName("신규 계획");

        PlanDetailVo d1 = new PlanDetailVo();
        d1.setPlanExerName("푸쉬업");
        planVo.setDetails(Arrays.asList(d1));

        // insertPlan이 호출되면 planSeq를 100으로 세팅 (DB의 AUTO_INCREMENT 동작을 시뮬레이션)
        doAnswer(invocation -> {
            PlanVo vo = invocation.getArgument(0);
            vo.setPlanSeq(100L);
            return 1;
        }).when(planMapper).insertPlan(any(PlanVo.class));

        // [When]
        Long planSeq = planService.savePlan(planVo, request);

        // [Then] 반환 PK 확인
        assertEquals(100L, planSeq);
        // [Then] 정렬 순서가 1부터 자동 계산되었는지 확인
        assertEquals(1, d1.getPlanSortOrder());

        // ArgumentCaptor로 insertPlan에 실제 전달된 PlanVo 객체를 캡처합니다.
        // verify(planMapper).insertPlan(any())만으로는 내부 필드 값을 확인할 수 없습니다.
        ArgumentCaptor<PlanVo> planCaptor = ArgumentCaptor.forClass(PlanVo.class);
        verify(planMapper).insertPlan(planCaptor.capture());
        PlanVo captured = planCaptor.getValue();

        // 감사 필드: 서비스가 SecurityContext에서 읽어 자동으로 채워야 합니다.
        assertEquals("test_user", captured.getInsUserId(), "insertPlan에 전달된 insUserId가 로그인 사용자여야 합니다.");
        assertEquals("10.10.10.10", captured.getInsIp(), "insertPlan에 전달된 insIp가 요청 IP여야 합니다.");

        verify(planMapper, times(1)).insertPlanDetail(any(PlanDetailVo.class));
        verify(planMapper, never()).updatePlan(any());
        verify(planMapper, never()).deletePlanDetailsByPlanSeq(anyLong());
    }

    @Test
    @DisplayName("상세 항목 없이 운동 계획 마스터만 신규 저장한다")
    void savePlan_Insert_NoDetails_Test() {
        // [Given]
        PlanVo planVo = new PlanVo();
        planVo.setPlanName("상세 없는 계획");
        planVo.setDetails(null);

        doAnswer(invocation -> {
            PlanVo vo = invocation.getArgument(0);
            vo.setPlanSeq(500L);
            return 1;
        }).when(planMapper).insertPlan(any(PlanVo.class));

        // [When]
        Long planSeq = planService.savePlan(planVo, request);

        // [Then]
        assertEquals(500L, planSeq);
        verify(planMapper, times(1)).insertPlan(any(PlanVo.class));
        // 상세 항목이 null이면 insertPlanDetail은 절대 호출되면 안 됩니다.
        verify(planMapper, never()).insertPlanDetail(any());
    }

    @Test
    @DisplayName("기존 운동 계획 수정 시 상세 항목을 수정하고 추가한다")
    void savePlan_Update_MixedDetails_Test() {
        // [Given]
        Long planSeq = 200L;
        PlanVo planVo = new PlanVo();
        planVo.setPlanSeq(planSeq);
        planVo.setPlanName("수정된 계획");

        // planDetailSeq가 있으면 기존 항목(update 대상), 없으면 신규 항목(insert 대상)
        PlanDetailVo existingDetail = new PlanDetailVo();
        existingDetail.setPlanDetailSeq(50L);
        existingDetail.setPlanExerName("기존 운동 수정");

        PlanDetailVo newDetail = new PlanDetailVo();
        newDetail.setPlanExerName("신규 운동 추가");

        planVo.setDetails(Arrays.asList(existingDetail, newDetail));

        // [When]
        Long resultSeq = planService.savePlan(planVo, request);

        // [Then]
        assertEquals(planSeq, resultSeq);
        // 정렬 순서는 리스트 인덱스 순으로 1부터 재계산됩니다.
        assertEquals(1, existingDetail.getPlanSortOrder());
        assertEquals(2, newDetail.getPlanSortOrder());

        verify(planMapper, times(1)).updatePlan(any(PlanVo.class));
        // 수정 시에는 기존 상세를 전부 소프트 삭제한 뒤 재삽입하는 방식으로 동작합니다.
        verify(planMapper, times(1)).deletePlanDetailsByPlanSeq(planSeq);
        verify(planMapper, times(1)).updatePlanDetail(existingDetail);
        verify(planMapper, times(1)).insertPlanDetail(newDetail);
        verify(planMapper, never()).insertPlan(any());
    }

    @Test
    @DisplayName("기존 운동 계획 수정 시 상세 항목을 모두 제거한다")
    void savePlan_Update_ClearDetails_Test() {
        // [Given]
        Long planSeq = 600L;
        PlanVo planVo = new PlanVo();
        planVo.setPlanSeq(planSeq);
        planVo.setPlanName("상세 삭제 수정");
        planVo.setDetails(new ArrayList<>()); // 빈 리스트 = 모든 상세 삭제

        // [When]
        planService.savePlan(planVo, request);

        // [Then]
        verify(planMapper, times(1)).updatePlan(any(PlanVo.class));
        verify(planMapper, times(1)).deletePlanDetailsByPlanSeq(planSeq);
        // 상세가 없으므로 insert/update는 호출되면 안 됩니다.
        verify(planMapper, never()).insertPlanDetail(any());
        verify(planMapper, never()).updatePlanDetail(any());
    }

    @Test
    @DisplayName("운동 계획을 소프트 삭제한다")
    void deletePlan_Test() {
        // [Given]
        Long planSeq = 300L;

        // [When]
        planService.deletePlan(planSeq, request);

        // [Then] ArgumentCaptor로 deletePlan에 실제 전달된 PlanVo 캡처
        ArgumentCaptor<PlanVo> deleteCaptor = ArgumentCaptor.forClass(PlanVo.class);
        verify(planMapper).deletePlan(deleteCaptor.capture());
        PlanVo capturedDelete = deleteCaptor.getValue();

        assertEquals(planSeq, capturedDelete.getPlanSeq(), "삭제할 planSeq가 올바르게 전달되어야 합니다.");
        // 소프트 삭제 시 수정자 정보도 함께 기록되어야 합니다.
        assertEquals("test_user", capturedDelete.getUpdUserId(), "삭제 시 updUserId가 로그인 사용자여야 합니다.");
        assertEquals("10.10.10.10", capturedDelete.getUpdIp(), "삭제 시 updIp가 요청 IP여야 합니다.");

        // 마스터 삭제 시 연결된 상세 항목도 함께 소프트 삭제합니다.
        verify(planMapper, times(1)).deletePlanDetailsByPlanSeq(planSeq);
    }
}
