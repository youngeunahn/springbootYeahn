package com.yeahn.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.cloud.objectstorage.services.s3.AmazonS3;
import com.yeahn.Application;
import com.yeahn.auth.dto.UserLoginRequest;
import com.yeahn.auth.dto.UserVo;
import com.yeahn.auth.service.UserService;
import com.yeahn.common.UserAgentInfo;
import com.yeahn.common.UserAgentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
class JwtApiSecurityTest {

    @MockitoBean(name = "amazonS3Client")
    private AmazonS3 amazonS3ClientMock;

    @MockitoBean(name = "client")
    private AmazonS3 cosClientMock;

    @MockitoBean
    private UserAgentService userAgentService;

    @MockitoBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        UserAgentInfo mockInfo = new UserAgentInfo("TEST_BROWSER", "TEST_OS", "TEST_DEVICE");
        Mockito.when(userAgentService.parse(anyString())).thenReturn(mockInfo);
    }

    @Test
    @DisplayName("보호 API는 JWT가 없으면 ResponseDto 401을 반환한다")
    void protectedApiRequiresJwt() throws Exception {
        // [When] JWT 없이 보호 API 호출
        // [Then] 401과 실패 ResponseDto를 반환
        mockMvc.perform(get("/api/user/me").header("User-Agent", "Mozilla/5.0"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("FAIL"))
                .andExpect(jsonPath("$.message").value("로그인이 필요합니다."));
    }

    @Test
    @DisplayName("보호 API는 유효한 JWT를 허용한다")
    void protectedApiAcceptsValidJwt() throws Exception {
        // [Given] 유효한 사용자 정보와 JWT 발급
        UserVo userVo = user("api_user", "rawPassword!", "ROLE_USER");
        Mockito.when(userService.loadUserByUsername("api_user")).thenReturn(userVo);
        String token = jwtService.createAccessToken("api_user", "ROLE_USER");

        // [When] Bearer token으로 보호 API 호출
        // [Then] 사용자 프로필 ResponseDto를 반환
        mockMvc.perform(get("/api/user/me")
                        .header("Authorization", "Bearer " + token)
                        .header("User-Agent", "Mozilla/5.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.userId").value("api_user"))
                .andExpect(jsonPath("$.data.role").value("ROLE_USER"));
    }

    @Test
    @DisplayName("로그인 성공 시 JWT ResponseDto를 반환한다")
    void loginIssuesJwt() throws Exception {
        // [Given] 비밀번호가 일치하는 로그인 사용자
        Mockito.when(userService.loadUserByUsername("api_user"))
                .thenReturn(user("api_user", "rawPassword!", "ROLE_USER"));

        UserLoginRequest request = new UserLoginRequest();
        request.setUserId("api_user");
        request.setPassword("rawPassword!");

        // [When] 로그인 API 호출
        // [Then] JWT access token을 포함한 SUCCESS ResponseDto를 반환
        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("User-Agent", "Mozilla/5.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.userId").value("api_user"));
    }

    @Test
    @DisplayName("로그인 실패 시 401 ResponseDto를 반환한다")
    void loginRejectsInvalidPassword() throws Exception {
        // [Given] 저장된 비밀번호와 다른 비밀번호로 로그인 요청 준비
        Mockito.when(userService.loadUserByUsername("api_user"))
                .thenReturn(user("api_user", "rawPassword!", "ROLE_USER"));

        UserLoginRequest request = new UserLoginRequest();
        request.setUserId("api_user");
        request.setPassword("wrongPassword!");

        // [When] 로그인 API 호출
        // [Then] 401과 실패 ResponseDto를 반환
        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("User-Agent", "Mozilla/5.0"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("FAIL"))
                .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호가 올바르지 않습니다."));
    }

    @Test
    @DisplayName("로그인 요청 body가 null이면 400을 반환한다")
    void loginRejectsNullBody() throws Exception {
        // [When] JSON null body로 로그인 API 호출
        // [Then] 400과 실패 ResponseDto를 반환
        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("null")
                        .header("User-Agent", "Mozilla/5.0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAIL"));
    }

    @Test
    @DisplayName("공개 API는 JWT 검증 없이 처리된다")
    void publicApiIgnoresInvalidJwt() throws Exception {
        // [Given] 아이디 중복 체크 결과 mock 설정
        Mockito.when(userService.isDuplicateId("api_user")).thenReturn(false);

        // [When] 공개 API에 잘못된 JWT를 포함해 호출
        // [Then] JWT 검증 실패와 무관하게 공개 API 응답을 반환
        mockMvc.perform(get("/api/user/check-id")
                        .param("userId", "api_user")
                        .header("Authorization", "Bearer invalid-token")
                        .header("User-Agent", "Mozilla/5.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data").value(false));
    }

    @Test
    @DisplayName("POST 템플릿 API는 JWT 인증이 필요하다")
    void postTemplateApiRequiresJwt() throws Exception {
        // [When] JWT 없이 POST 템플릿 API 호출
        // [Then] 공개 조회 API가 아니므로 401을 반환
        mockMvc.perform(post("/api/user/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .header("User-Agent", "Mozilla/5.0"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("FAIL"));
    }

    @Test
    @DisplayName("명시되지 않은 사용자 API는 기본적으로 JWT 인증이 필요하다")
    void unspecifiedUserApiRequiresJwtByDefault() throws Exception {
        // [When] 공개 API로 등록되지 않은 /api/user 경로 호출
        // [Then] 기본 보호 정책에 따라 401을 반환
        mockMvc.perform(get("/api/user/private-placeholder")
                        .header("User-Agent", "Mozilla/5.0"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("FAIL"));
    }

    private UserVo user(String userId, String rawPassword, String role) {
        UserVo userVo = new UserVo();
        userVo.setUserId(userId);
        userVo.setUserName("API User");
        userVo.setUserPwd(passwordEncoder.encode(rawPassword));
        userVo.setUserAuth(role);
        userVo.setGrpAuth(role);
        return userVo;
    }
}
