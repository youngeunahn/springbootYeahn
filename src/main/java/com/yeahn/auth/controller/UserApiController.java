package com.yeahn.auth.controller;

import com.yeahn.auth.dto.UserVo;
import com.yeahn.auth.dto.UserLoginRequest;
import com.yeahn.auth.dto.UserProfileResponse;
import com.yeahn.auth.dto.UserTokenResponse;
import com.yeahn.auth.service.UserService;
import com.yeahn.common.dto.ResponseDto;
import com.yeahn.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserApiController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /**
     * 아이디 중복 체크
     */
    @GetMapping("/check-id")
    public ResponseDto<Boolean> checkId(@RequestParam String userId) {
        boolean isDuplicate = userService.isDuplicateId(userId);
        return ResponseDto.success(isDuplicate);
    }

    /**
     * 회원 가입
     */
    @PostMapping("/signUp")
    public ResponseDto<String> signUp(@RequestBody UserVo userVo, HttpServletRequest request) {
        try {
            log.info("Sign up request for user: {}", userVo.getUserId());
            
            if (userVo.getUserId() == null || userVo.getUserPwd() == null) {
                return ResponseDto.fail("아이디와 비밀번호를 입력해 주세요.");
            }

            if (userService.isDuplicateId(userVo.getUserId())) {
                return ResponseDto.fail("이미 사용 중인 아이디입니다.");
            }
            
            userVo.setInsIp(request.getRemoteAddr());
            userService.joinUser(userVo, "ROLE_USER");
            
            return ResponseDto.success("회원가입이 완료되었습니다.", null);
        } catch (Exception e) {
            log.error("Sign up error: ", e);
            return ResponseDto.fail("회원가입에 실패했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseDto<UserTokenResponse>> login(@RequestBody UserLoginRequest loginRequest) {
        if (loginRequest == null || loginRequest.getUserId() == null || loginRequest.getPassword() == null) {
            return ResponseEntity.badRequest()
                    .body(ResponseDto.fail("아이디와 비밀번호를 입력해 주세요."));
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUserId(), loginRequest.getPassword()));
            UserVo userVo = (UserVo) authentication.getPrincipal();
            String accessToken = jwtService.createAccessToken(userVo.getUserId(), userVo.getUserAuth());

            UserTokenResponse tokenResponse = UserTokenResponse.builder()
                    .tokenType("Bearer")
                    .accessToken(accessToken)
                    .expiresIn(jwtService.getExpirationSeconds())
                    .userId(userVo.getUserId())
                    .userName(userVo.getDisplayName())
                    .role(userVo.getUserAuth())
                    .build();

            return ResponseEntity.ok(ResponseDto.success("로그인이 완료되었습니다.", tokenResponse));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ResponseDto.fail("아이디 또는 비밀번호가 올바르지 않습니다."));
        }
    }

    @GetMapping("/me")
    public ResponseDto<UserProfileResponse> me(Principal principal) {
        UserVo userVo = (UserVo) userService.loadUserByUsername(principal.getName());
        UserProfileResponse response = UserProfileResponse.builder()
                .userId(userVo.getUserId())
                .userName(userVo.getDisplayName())
                .role(userVo.getUserAuth())
                .build();
        return ResponseDto.success(response);
    }
}
