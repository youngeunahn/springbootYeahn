package com.yeahn.common.code;

import com.yeahn.config.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CodeControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CodeService codeService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new CodeController(codeService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("사용자 코드 목록을 ResponseDto value/label 형식으로 반환한다")
    void getUserCodes_ReturnsResponseDto() throws Exception {
        when(codeService.getUserCodeOptions("PLAN_PHASE", null))
                .thenReturn(List.of(new CodeOptionDto("MAIN", "MAIN")));

        mockMvc.perform(get("/api/user/codes")
                        .param("groupCode", "PLAN_PHASE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("조회되었습니다."))
                .andExpect(jsonPath("$.data[0].value").value("MAIN"))
                .andExpect(jsonPath("$.data[0].label").value("MAIN"));
    }

    @Test
    @DisplayName("사용자 카테고리 코드는 typeCode 조건을 전달한다")
    void getUserCodes_WithTypeCode() throws Exception {
        when(codeService.getUserCodeOptions("PLAN_CATEGORY", "SWIM"))
                .thenReturn(List.of(new CodeOptionDto("FREESTYLE", "자유형")));

        mockMvc.perform(get("/api/user/codes")
                        .param("groupCode", "PLAN_CATEGORY")
                        .param("typeCode", "SWIM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data[0].value").value("FREESTYLE"))
                .andExpect(jsonPath("$.data[0].label").value("자유형"));
    }
}
