package main.java.com.yeahn.plan.service;

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
    @DisplayName("운동 계획 저장 테스트 - 신규 저장")
    void savePlan_Insert_Test() {
        // given
        PlanVo planVo = new PlanVo();
        planVo.setPlanName("신규 계획");
        
        PlanDetailVo detail = new PlanDetailVo();
        detail.setPlanExerName("푸쉬업");
        planVo.setDetails(Arrays.asList(detail));

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testUser");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        doAnswer(invocation -> {
            PlanVo vo = invocation.getArgument(0);
            vo.setPlanSeq(100);
            return null;
        }).when(planMapper).insertPlan(any(PlanVo.class));

        // when
        Integer planSeq = planService.savePlan(planVo, request);

        // then
        assertEquals(100, planSeq);
        verify(planMapper, times(1)).insertPlan(any(PlanVo.class));
        verify(planMapper, times(1)).insertPlanDetail(any(PlanDetailVo.class));
        verify(planMapper, never()).updatePlan(any());
        verify(planMapper, never()).deletePlanDetailsByPlanSeq(anyInt());
    }

    @Test
    @DisplayName("운동 계획 저장 테스트 - 기존 수정")
    void savePlan_Update_Test() {
        // given
        Integer planSeq = 200;
        PlanVo planVo = new PlanVo();
        planVo.setPlanSeq(planSeq);
        planVo.setPlanName("수정된 계획");
        
        PlanDetailVo detail = new PlanDetailVo();
        detail.setPlanExerName("벤치 프레스");
        planVo.setDetails(Arrays.asList(detail));

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("updateUser");
        when(request.getRemoteAddr()).thenReturn("192.168.0.1");

        // when
        Integer resultSeq = planService.savePlan(planVo, request);

        // then
        assertEquals(planSeq, resultSeq);
        verify(planMapper, times(1)).updatePlan(any(PlanVo.class));
        verify(planMapper, times(1)).deletePlanDetailsByPlanSeq(planSeq);
        verify(planMapper, times(1)).insertPlanDetail(any(PlanDetailVo.class));
        verify(planMapper, never()).insertPlan(any());
    }

    @Test
    @DisplayName("운동 계획 삭제 테스트")
    void deletePlan_Test() {
        // given
        Integer planSeq = 300;
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("deleteUser");
        when(request.getRemoteAddr()).thenReturn("1.1.1.1");

        // when
        planService.deletePlan(planSeq, request);

        // then
        verify(planMapper, times(1)).deletePlan(any(PlanVo.class));
        verify(planMapper, times(1)).deletePlanDetailsByPlanSeq(planSeq);
    }
}
