package com.yeahn.auth.service;

import com.yeahn.Application;
import com.yeahn.auth.dto.UserVo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = Application.class)
@Transactional
@org.springframework.test.context.jdbc.Sql(scripts = "/test-data.sql", executionPhase = org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    @DisplayName("가입 전후 아이디 중복 여부를 판별한다")
    void checkDuplicateId_Test() {
        // [Given] 테스트용 아이디 생성
        String userId = "test_" + UUID.randomUUID().toString().substring(0, 8);
        
        // [When] 가입 전 아이디 중복 여부 확인
        boolean duplicateBeforeJoin = userService.isDuplicateId(userId);

        // [Then] 가입 전에는 중복이 아님
        assertFalse(duplicateBeforeJoin);

        // [When] 가입 처리
        UserVo vo = new UserVo();
        vo.setUserId(userId);
        vo.setUserPwd("password123");
        userService.joinUser(vo, "ROLE_USER");

        // [Then] 가입 후에는 중복으로 판별됨
        assertTrue(userService.isDuplicateId(userId));
    }

    @Test
    @DisplayName("회원가입 시 비밀번호를 BCrypt로 암호화한다")
    void joinUser_Encryption_Test() {
        // [Given]
        String userId = "user_" + UUID.randomUUID().toString().substring(0, 8);
        String rawPassword = "myPassword!@#";

        UserVo vo = new UserVo();
        vo.setUserId(userId);
        vo.setUserPwd(rawPassword);

        // [When] 가입 실행
        userService.joinUser(vo, "ROLE_USER");

        // [Then] 비밀번호가 암호화되어 저장되었는지 확인
        UserVo savedUser = (UserVo) userService.loadUserByUsername(userId);
        assertNotNull(savedUser);
        assertNotEquals(rawPassword, savedUser.getPassword());
        
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        assertTrue(encoder.matches(rawPassword, savedUser.getPassword()));
    }
}
