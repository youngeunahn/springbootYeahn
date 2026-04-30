package com.yeahn.auth.service;

import com.yeahn.auth.dao.UserMapper;
import com.yeahn.auth.dto.UserVo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserMapper userMapper;

    public boolean isDuplicateId(String userId) {
        return userMapper.checkUserId(userId) > 0;
    }

    @Transactional
    public void joinUser(UserVo uservo){
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        uservo.setUserPwd(passwordEncoder.encode(uservo.getPassword()));
        uservo.setUserAuth("ROLE_ADMIN");
        uservo.setGrpAuth("ROLE_ADMIN");
        userMapper.saveUser(uservo);
    }

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        //여기서 받은 유저 패스워드와 비교하여 로그인 인증
        UserVo userVo = userMapper.getUserAccount(userId);
        if (userVo == null){
            throw new UsernameNotFoundException("User not authorized.");
        }
        return userVo;
    }
}
