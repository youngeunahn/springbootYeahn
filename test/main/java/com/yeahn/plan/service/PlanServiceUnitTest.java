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
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        // SecurityContextHolder에 Mock SecurityContext를 등록합니다.
        // stub(when)은 각 테스트에서 필요한 경우에만 선언합니다.
        // → 사용되지 않는 stub이 있으면 MockitoExtension이 에러를 발생시켜 불필요한 설정을 방지합니다.
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        // 테스트 간 SecurityContext 오염 방지: 각 테스트 후 반드시 초기화합니다.
        SecurityContextHolder.clearContext();
    }

    // ────────────────────────────────────────────────────────────────
    // 조회 테스트 (SecurityContext stub 불필요)
    // ────────────────────────────────────────────────────────────────

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
        // getPlan이 null을 반환하면 getPlanDetails는 호출되지 않아야 합니다.
        verify(planMapper, never()).getPlanDetails(anyInt());
    }

    // ────────────────────────────────────────────────────────────────
    // 저장/삭제 테스트 (SecurityContext stub 필요)
    // ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("운동 계획 저장 테스트 - 신규 저장 (상세 항목 포함)")
    void savePlan_Insert_Test() {
        // given
        // savePlan 내부에서 로그인 사용자 ID와 IP를 감사 필드에 기록하므로 stub이 필요합니다.
        // 127.0.0.1(루프백)을 반환하면 CommonUtils.getIP()가 InetAddress.getLocalHost()로
        // 교체하므로 비-루프백 IP를 사용해야 stub 값 그대로 감사 필드에 기록됩니다.
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test_user");
        when(request.getRemoteAddr()).thenReturn("10.10.10.10");

        PlanVo planVo = new PlanVo();
        planVo.setPlanName("신규 계획");

        PlanDetailVo d1 = new PlanDetailVo();
        d1.setPlanExerName("푸쉬업");
        planVo.setDetails(Arrays.asList(d1));

        // insertPlan이 호출되면 planSeq를 100으로 세팅 (DB의 AUTO_INCREMENT 동작을 시뮬레이션)
        doAnswer(invocation -> {
            PlanVo vo = invocation.getArgument(0);
            vo.setPlanSeq(100);
            return 1;
        }).when(planMapper).insertPlan(any(PlanVo.class));

        // when
        Integer planSeq = planService.savePlan(planVo, request);

        // then: 반환 PK 확인
        assertEquals(100, planSeq);
        // then: 정렬 순서가 1부터 자동 계산되었는지 확인
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
        verify(planMapper, never()).deletePlanDetailsByPlanSeq(anyInt());
    }

    @Test
    @DisplayName("운동 계획 저장 테스트 - 상세 항목 없이 마스터만 신규 저장")
    void savePlan_Insert_NoDetails_Test() {
        // given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test_user");
        when(request.getRemoteAddr()).thenReturn("10.10.10.10");

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
        // 상세 항목이 null이면 insertPlanDetail은 절대 호출되면 안 됩니다.
        verify(planMapper, never()).insertPlanDetail(any());
    }

    @Test
    @DisplayName("운동 계획 저장 테스트 - 기존 수정 (상세 항목 수정 및 추가 혼합)")
    void savePlan_Update_MixedDetails_Test() {
        // given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test_user");
        when(request.getRemoteAddr()).thenReturn("10.10.10.10");

        Integer planSeq = 200;
        PlanVo planVo = new PlanVo();
        planVo.setPlanSeq(planSeq);
        planVo.setPlanName("수정된 계획");

        // planDetailSeq가 있으면 기존 항목(update 대상), 없으면 신규 항목(insert 대상)
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
    @DisplayName("운동 계획 저장 테스트 - 기존 수정 시 상세 항목을 모두 제거")
    void savePlan_Update_ClearDetails_Test() {
        // given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test_user");
        when(request.getRemoteAddr()).thenReturn("10.10.10.10");

        Integer planSeq = 600;
        PlanVo planVo = new PlanVo();
        planVo.setPlanSeq(planSeq);
        planVo.setPlanName("상세 삭제 수정");
        planVo.setDetails(new ArrayList<>()); // 빈 리스트 = 모든 상세 삭제

        // when
        planService.savePlan(planVo, request);

        // then
        verify(planMapper, times(1)).updatePlan(any(PlanVo.class));
        verify(planMapper, times(1)).deletePlanDetailsByPlanSeq(planSeq);
        // 상세가 없으므로 insert/update는 호출되면 안 됩니다.
        verify(planMapper, never()).insertPlanDetail(any());
        verify(planMapper, never()).updatePlanDetail(any());
    }

    @Test
    @DisplayName("운동 계획 삭제 테스트 (Soft Delete)")
    void deletePlan_Test() {
        // given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test_user");
        when(request.getRemoteAddr()).thenReturn("10.10.10.10");

        Integer planSeq = 300;

        // when
        planService.deletePlan(planSeq, request);

        // then: ArgumentCaptor로 deletePlan에 실제 전달된 PlanVo 캡처
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
