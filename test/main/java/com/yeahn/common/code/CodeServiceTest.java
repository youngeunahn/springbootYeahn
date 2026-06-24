package com.yeahn.common.code;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CodeServiceTest {

    @Mock
    private CodeMapper codeMapper;

    @InjectMocks
    private CodeService codeService;

    @Test
    @DisplayName("PLAN_TYPE maps to TPL_TYPE_CODE")
    void getUserCodeOptions_MapsPlanType() {
        when(codeMapper.getCodeList(org.mockito.ArgumentMatchers.any(CodeDto.class)))
                .thenReturn(List.of(new CodeDto("SWIM", "Swim", 1)));

        List<CodeOptionDto> result = codeService.getUserCodeOptions("PLAN_TYPE");

        assertEquals(1, result.size());
        assertEquals("SWIM", result.get(0).getValue());
        assertEquals("Swim", result.get(0).getLabel());

        ArgumentCaptor<CodeDto> captor = ArgumentCaptor.forClass(CodeDto.class);
        verify(codeMapper).getCodeList(captor.capture());
        assertEquals("TPL_TYPE_CODE", captor.getValue().getTypeClass());
    }

    @Test
    @DisplayName("PLAN_CATEGORY maps to TPL_CATEGORY with typeCode as ref1")
    void getUserCodeOptions_CategoryTypeCodeFilter() {
        when(codeMapper.getCodeList(org.mockito.ArgumentMatchers.any(CodeDto.class)))
                .thenReturn(List.of(new CodeDto("FREESTYLE", "Freestyle", 1)));

        List<CodeOptionDto> result = codeService.getUserCodeOptions("PLAN_CATEGORY", "SWIM");

        assertEquals(1, result.size());
        assertEquals("FREESTYLE", result.get(0).getValue());
        assertEquals("Freestyle", result.get(0).getLabel());

        ArgumentCaptor<CodeDto> captor = ArgumentCaptor.forClass(CodeDto.class);
        verify(codeMapper).getCodeList(captor.capture());
        assertEquals("TPL_CATEGORY", captor.getValue().getTypeClass());
        assertEquals("SWIM", captor.getValue().getRef1());
    }

    @Test
    @DisplayName("Unsupported user code group throws exception")
    void getUserCodeOptions_UnsupportedGroup() {
        assertThrows(IllegalArgumentException.class,
                () -> codeService.getUserCodeOptions("UNKNOWN"));
    }
}
