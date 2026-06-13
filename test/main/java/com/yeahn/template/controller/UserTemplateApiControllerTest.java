package com.yeahn.template.controller;

import com.yeahn.config.GlobalExceptionHandler;
import com.yeahn.template.dto.TemplateDto;
import com.yeahn.template.dto.TemplateSearchDto;
import com.yeahn.template.service.TemplateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class UserTemplateApiControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TemplateService templateService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new UserTemplateApiController(templateService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("사용자 템플릿 목록을 ResponseDto로 반환한다")
    void list_ReturnsResponseDto() throws Exception {
        // [Given] 사용자 템플릿 목록 조회 결과
        TemplateDto template = new TemplateDto();
        template.setTplSeq(1L);
        template.setTplName("수영 템플릿");
        template.setTplTypeCode("SWIM");

        // [When] 목록 API 호출
        when(templateService.searchTplList(any(TemplateSearchDto.class)))
                .thenReturn(Arrays.asList(template));

        // [Then] ResponseDto 형식으로 반환
        mockMvc.perform(get("/api/user/templates")
                        .param("typeCode", "SWIM")
                        .param("keyword", "수영"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("요청이 성공적으로 처리되었습니다."))
                .andExpect(jsonPath("$.data[0].tplSeq").value(1))
                .andExpect(jsonPath("$.data[0].tplName").value("수영 템플릿"));

        // [Then] 검색 조건이 서비스에 전달됨
        ArgumentCaptor<TemplateSearchDto> captor = ArgumentCaptor.forClass(TemplateSearchDto.class);
        verify(templateService).searchTplList(captor.capture());
        assertEquals("SWIM", captor.getValue().getTplType());
        assertEquals("수영", captor.getValue().getTplName());
    }

    @Test
    @DisplayName("사용자 템플릿 상세를 ResponseDto로 반환한다")
    void detail_ReturnsResponseDto() throws Exception {
        // [Given] 사용자 템플릿 상세 조회 결과
        TemplateDto template = new TemplateDto();
        template.setTplSeq(10L);
        template.setTplName("상세 템플릿");

        // [When] 상세 API 호출
        when(templateService.getTplDetail(eq(10L))).thenReturn(template);

        // [Then] ResponseDto 형식으로 반환
        mockMvc.perform(get("/api/user/templates/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.tplSeq").value(10))
                .andExpect(jsonPath("$.data.tplName").value("상세 템플릿"));
    }

    @Test
    @DisplayName("사용자 템플릿 상세가 없으면 FAIL을 반환한다")
    void detail_NotFound_ReturnsFail() throws Exception {
        // [Given] 사용자 템플릿 상세 조회 결과가 없음
        // [When] 상세 API 호출
        when(templateService.getTplDetail(eq(999L))).thenReturn(null);

        // [Then] FAIL을 반환
        mockMvc.perform(get("/api/user/templates/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAIL"))
                .andExpect(jsonPath("$.message").value("템플릿을 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("사용자 템플릿 목록 예외를 ResponseDto FAIL로 반환한다")
    void list_Exception_ReturnsResponseDtoFail() throws Exception {
        // [Given] 템플릿 목록 조회 중 예외 발생
        // [When] 목록 API 호출
        doThrow(new IllegalArgumentException("Invalid template search condition"))
                .when(templateService).searchTplList(any(TemplateSearchDto.class));

        // [Then] GlobalExceptionHandler가 ResponseDto FAIL로 변환
        mockMvc.perform(get("/api/user/templates"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAIL"))
                .andExpect(jsonPath("$.message").value("Invalid template search condition"));
    }
}
