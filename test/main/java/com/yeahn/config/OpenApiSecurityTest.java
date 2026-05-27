package com.yeahn.config;

import com.ibm.cloud.objectstorage.services.s3.AmazonS3;
import com.yeahn.Application;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
public class OpenApiSecurityTest {

    @MockitoBean(name = "amazonS3Client")
    private AmazonS3 amazonS3ClientMock;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("비로그인 사용자는 OpenAPI JSON 접근 시 로그인으로 이동한다")
    void openApiJsonRequiresLogin() throws Exception {
        mockMvc.perform(get("/v3/api-docs").header("User-Agent", "Mozilla/5.0"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("일반 사용자는 OpenAPI JSON 접근 시 403을 반환한다")
    void openApiJsonRejectsNonAdminUser() throws Exception {
        mockMvc.perform(get("/v3/api-docs").header("User-Agent", "Mozilla/5.0"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("관리자는 OpenAPI JSON을 조회하고 회원가입 API는 문서에서 제외된다")
    void openApiJsonIsAdminOnlyAndExcludesUserSignUpApis() throws Exception {
        mockMvc.perform(get("/v3/api-docs").header("User-Agent", "Mozilla/5.0"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.openapi").exists())
            .andExpect(jsonPath("$.info.title").value("Yeahn API"))
            .andExpect(jsonPath("$.paths").value(hasKey("/api/user/templates")))
            .andExpect(jsonPath("$.paths").value(not(hasKey("/api/user/check-id"))))
            .andExpect(jsonPath("$.paths").value(not(hasKey("/api/user/signUp"))))
            .andExpect(jsonPath("$.paths").value(not(hasKey("/admin/signUp"))));
    }

    @Test
    @DisplayName("비로그인 사용자는 Swagger UI 접근 시 로그인으로 이동한다")
    void swaggerUiRequiresLogin() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html").header("User-Agent", "Mozilla/5.0"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("관리자는 Swagger UI를 조회한다")
    void swaggerUiIsAdminOnly() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html").header("User-Agent", "Mozilla/5.0"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("text/html"));
    }
}
