package com.yeahn.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yeahn.Application;
import com.yeahn.auth.dto.UserVo;
import com.yeahn.auth.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@org.springframework.test.context.jdbc.Sql(scripts = "/test-data.sql", executionPhase = org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class UserApiControllerTest {

    @MockBean
    private com.yeahn.common.UserAgentService userAgentService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;
    @BeforeEach
    void setUp() {
        com.yeahn.common.UserAgentInfo mockInfo = new com.yeahn.common.UserAgentInfo("TEST_BROWSER", "TEST_OS", "TEST_DEVICE");
        Mockito.when(userAgentService.parse(anyString())).thenReturn(mockInfo);
    }

    @Test
    @DisplayName("아이디 중복 여부를 ResponseDto로 반환한다")
    void checkIdApi_Test() throws Exception {
        // [Given] 가입되지 않은 신규 아이디
        String userId = "api_test_" + UUID.randomUUID().toString().substring(0, 5);

        // [When] 아이디 중복 체크 API 호출
        // [Then] SUCCESS와 false를 반환
        mockMvc.perform(get("/api/user/check-id")
                .param("userId", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data").value(false)); // 아직 가입 전이므로 false
    }

    @Test
    @DisplayName("회원가입 요청이 성공하면 SUCCESS를 반환한다")
    void signUpApi_Success_Test() throws Exception {
        // [Given] 신규 회원가입 요청 데이터
        String userId = "new_user_" + UUID.randomUUID().toString().substring(0, 5);
        UserVo signUpVo = new UserVo();
        signUpVo.setUserId(userId);
        signUpVo.setUserPwd("password123!");
        signUpVo.setUserName("테스터");

        // [When] 회원가입 API 호출
        // [Then] SUCCESS를 반환
        mockMvc.perform(post("/api/user/signUp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpVo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Sign up successful"));
    }

    @Test
    @DisplayName("중복 아이디로 회원가입하면 FAIL을 반환한다")
    void signUpApi_Duplicate_Fail_Test() throws Exception {
        // [Given] 미리 가입된 사용자
        String userId = "duplicate_user";
        UserVo vo = new UserVo();
        vo.setUserId(userId);
        vo.setUserPwd("pwd");
        userService.joinUser(vo, "ROLE_USER");

        // [When] 같은 아이디로 가입 시도
        UserVo signUpVo = new UserVo();
        signUpVo.setUserId(userId);
        signUpVo.setUserPwd("newPwd");

        // [Then] FAIL 응답 확인
        mockMvc.perform(post("/api/user/signUp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpVo)))
                .andExpect(status().isOk()) // 응답 규격은 200이지만 내부는 FAIL
                .andExpect(jsonPath("$.status").value("FAIL"))
                .andExpect(jsonPath("$.message").value("Duplicate User ID"));
    }
}
