package com.yeahn.plan.service;

import com.yeahn.plan.dao.PlanMapper;
import com.yeahn.plan.dto.PlanDetailVo;
import com.yeahn.plan.dto.PlanVo;
import com.yeahn.plan.service.PlanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlanServiceUnitTest {

    @Mock
    private PlanMapper planMapper;

    @InjectMocks
    private PlanService planService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn("test_user");
        lenient().when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    }

    @Test
    @DisplayName("운동 계획 목록 조회 테스트")
    void getPlanList_Test() {
        // given
        PlanVo planVo = new PlanVo();
        List<PlanVo> expectedList = Arrays.asList(new PlanVo(), new PlanVo());
        when(planMapper.getPlanList(planVo)).thenReturn(expectedList);

        // when
        List<PlanVo> result = planService.getPlanList(planVo);

        // then
        assertEquals(expectedList.size(), result.size());
        verify(planMapper, times(1)).getPlanList(planVo);
    }

    @Test
    @DisplayName("운동 계획 상세 조회 테스트 - 성공")
    void getPlanDetail_Success_Test() {
        // given
        Integer planSeq = 1;
        PlanVo mockPlan = new PlanVo();
        mockPlan.setPlanSeq(planSeq);
        mockPlan.setPlanName("상세 테스트");

        List<PlanDetailVo> mockDetails = Arrays.asList(new PlanDetailVo(), new PlanDetailVo());
        
        when(planMapper.getPlan(planSeq)).thenReturn(mockPlan);
        when(planMapper.getPlanDetails(planSeq)).thenReturn(mockDetails);

        // when
        PlanVo result = planService.getPlanDetail(planSeq);

        // then
        assertNotNull(result);
        assertEquals(planSeq, result.getPlanSeq());
        assertEquals(2, result.getDetails().size());
        verify(planMapper, times(1)).getPlan(planSeq);
        verify(planMapper, times(1)).getPlanDetails(planSeq);
    }

    @Test
    @DisplayName("운동 계획 상세 조회 테스트 - 데이터 없음")
    void getPlanDetail_NotFound_Test() {
        // given
        Integer planSeq = 999;
        when(planMapper.getPlan(planSeq)).thenReturn(null);

        // when
        PlanVo result = planService.getPlanDetail(planSeq);

        // then
        assertNull(result);
        verify(planMapper, times(1)).getPlan(planSeq);
        verify(planMapper, never()).getPlanDetails(anyInt());
    }

    @Test
    @DisplayName("운동 계획 저장 테스트 - 신규 저장 (상세 항목 포함)")
    void savePlan_Insert_Test() {
        // given
        PlanVo planVo = new PlanVo();
        planVo.setPlanName("신규 계획");
        
        PlanDetailVo d1 = new PlanDetailVo();
        d1.setPlanExerName("푸쉬업");
        planVo.setDetails(Arrays.asList(d1));

        doAnswer(invocation -> {
            PlanVo vo = invocation.getArgument(0);
            vo.setPlanSeq(100);
            return 1;
        }).when(planMapper).insertPlan(any(PlanVo.class));

        // when
        Integer planSeq = planService.savePlan(planVo, request);

        // then
        assertEquals(100, planSeq);
        assertEquals(1, d1.getPlanSortOrder());
        verify(planMapper, times(1)).insertPlan(any(PlanVo.class));
        verify(planMapper, times(1)).insertPlanDetail(any(PlanDetailVo.class));
        verify(planMapper, never()).updatePlan(any());
        verify(planMapper, never()).deletePlanDetailsByPlanSeq(anyInt());
    }

    @Test
    @DisplayName("운동 계획 저장 테스트 - 상세 항목 없이 마스터만 신규 저장")
    void savePlan_Insert_NoDetails_Test() {
        // given
        PlanVo planVo = new PlanVo();
        planVo.setPlanName("상세 없는 계획");
        planVo.setDetails(null);

        doAnswer(invocation -> {
            PlanVo vo = invocation.getArgument(0);
            vo.setPlanSeq(500);
            return 1;
        }).when(planMapper).insertPlan(any(PlanVo.class));

        // when
        Integer planSeq = planService.savePlan(planVo, request);

        // then
        assertEquals(500, planSeq);
        verify(planMapper, times(1)).insertPlan(any(PlanVo.class));
        verify(planMapper, never()).insertPlanDetail(any());
    }

    @Test
    @DisplayName("운동 계획 저장 테스트 - 기존 수정 (상세 항목 수정 및 추가 혼합)")
    void savePlan_Update_MixedDetails_Test() {
        // given
        Integer planSeq = 200;
        PlanVo planVo = new PlanVo();
        planVo.setPlanSeq(planSeq);
        planVo.setPlanName("수정된 계획");
        
        PlanDetailVo existingDetail = new PlanDetailVo();
        existingDetail.setPlanDetailSeq(50);
        existingDetail.setPlanExerName("기존 운동 수정");

        PlanDetailVo newDetail = new PlanDetailVo();
        newDetail.setPlanExerName("신규 운동 추가");

        planVo.setDetails(Arrays.asList(existingDetail, newDetail));

        // when
        Integer resultSeq = planService.savePlan(planVo, request);

        // then
        assertEquals(planSeq, resultSeq);
        assertEquals(1, existingDetail.getPlanSortOrder());
        assertEquals(2, newDetail.getPlanSortOrder());
        
        verify(planMapper, times(1)).updatePlan(any(PlanVo.class));
        verify(planMapper, times(1)).deletePlanDetailsByPlanSeq(planSeq);
        verify(planMapper, times(1)).updatePlanDetail(existingDetail);
        verify(planMapper, times(1)).insertPlanDetail(newDetail);
        verify(planMapper, never()).insertPlan(any());
    }

    @Test
    @DisplayName("운동 계획 저장 테스트 - 기존 수정 시 상세 항목을 모두 제거")
    void savePlan_Update_ClearDetails_Test() {
        // given
        Integer planSeq = 600;
        PlanVo planVo = new PlanVo();
        planVo.setPlanSeq(planSeq);
        planVo.setPlanName("상세 삭제 수정");
        planVo.setDetails(new ArrayList<>()); // 빈 리스트

        // when
        planService.savePlan(planVo, request);

        // then
        verify(planMapper, times(1)).updatePlan(any(PlanVo.class));
        verify(planMapper, times(1)).deletePlanDetailsByPlanSeq(planSeq);
        verify(planMapper, never()).insertPlanDetail(any());
        verify(planMapper, never()).updatePlanDetail(any());
    }

    @Test
    @DisplayName("운동 계획 삭제 테스트 (Soft Delete)")
    void deletePlan_Test() {
        // given
        Integer planSeq = 300;

        // when
        planService.deletePlan(planSeq, request);

        // then
        verify(planMapper, times(1)).deletePlan(argThat(vo -> planSeq.equals(vo.getPlanSeq())));
        verify(planMapper, times(1)).deletePlanDetailsByPlanSeq(planSeq);
    }
}
