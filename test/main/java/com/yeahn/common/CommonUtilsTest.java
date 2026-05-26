package com.yeahn.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class CommonUtilsTest {

    @Test
    @DisplayName("XSS 위험 문자열을 정제한다")
    void paramCleanXSS_Sanitize_Test() {
        Map<String, Object> params = new HashMap<>();
        params.put("TITLE", "<script>alert('xss')</script>");
        params.put("CONTENT", "<img src=x onerror=alert(1)>hello");

        Map<String, Object> result = CommonUtils.paramCleanXSS(params);

        assertFalse(String.valueOf(result.get("TITLE")).contains("<script"));
        assertFalse(String.valueOf(result.get("CONTENT")).contains("<img"));
        assertNotEquals(params.get("CONTENT"), result.get("CONTENT"));
    }

    @Test
    @DisplayName("비밀번호 계열 파라미터는 XSS 정제 대상에서 제외한다")
    void paramCleanXSS_ExcludedPassword_Test() {
        Map<String, Object> params = new HashMap<>();
        params.put("password", "<script>raw</script>");

        Map<String, Object> result = CommonUtils.paramCleanXSS(params);

        assertEquals("<script>raw</script>", result.get("password"));
    }
}
