package com.yeahn.plan.controller;

import com.yeahn.config.GlobalExceptionHandler;
import com.yeahn.plan.dto.PlanDetailVo;
import com.yeahn.plan.dto.PlanVo;
import com.yeahn.plan.service.PlanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserPlanApiControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PlanService planService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new UserPlanApiController(planService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("사용자 운동계획 목록을 ResponseDto로 반환한다")
    void listPlans_ReturnsResponseDto() throws Exception {
        PlanDetailVo detail = new PlanDetailVo();
        detail.setPlanDetailSeq(10L);
        detail.setPlanExerName("Freestyle");

        PlanVo plan = new PlanVo();
        plan.setPlanSeq(1L);
        plan.setPlanName("Morning swim");
        plan.setPlanDate("2026-06-19");
        plan.setPlanTypeCode("SWIM");
        plan.setDetails(List.of(detail));

        when(planService.getPlanListWithDetails(any(PlanVo.class))).thenReturn(List.of(plan));

        mockMvc.perform(get("/api/user/plans")
                        .param("planTypeCode", "SWIM")
                        .principal(() -> "test_user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data[0].planSeq").value(1))
                .andExpect(jsonPath("$.data[0].planName").value("Morning swim"))
                .andExpect(jsonPath("$.data[0].planDate").value("2026-06-19"))
                .andExpect(jsonPath("$.data[0].planTypeCode").value("SWIM"))
                .andExpect(jsonPath("$.data[0].details[0].planDetailSeq").value(10))
                .andExpect(jsonPath("$.data[0].details[0].planExerName").value("Freestyle"));

        ArgumentCaptor<PlanVo> captor = ArgumentCaptor.forClass(PlanVo.class);
        verify(planService).getPlanListWithDetails(captor.capture());
        assertEquals("test_user", captor.getValue().getUserId());
        assertEquals("SWIM", captor.getValue().getPlanTypeCode());
    }
}
