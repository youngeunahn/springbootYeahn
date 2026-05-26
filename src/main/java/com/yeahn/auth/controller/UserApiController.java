package com.yeahn.auth.controller;

import com.yeahn.auth.dto.UserVo;
import com.yeahn.auth.service.UserService;
import com.yeahn.common.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserApiController {

    private final UserService userService;

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
                return ResponseDto.fail("User ID and Password are required");
            }

            if (userService.isDuplicateId(userVo.getUserId())) {
                return ResponseDto.fail("Duplicate User ID");
            }
            
            userVo.setInsIp(request.getRemoteAddr());
            userService.joinUser(userVo, "ROLE_USER");
            
            return ResponseDto.success("Sign up successful", null);
        } catch (Exception e) {
            log.error("Sign up error: ", e);
            return ResponseDto.fail("Sign up failed: " + e.getMessage());
        }
    }
}
