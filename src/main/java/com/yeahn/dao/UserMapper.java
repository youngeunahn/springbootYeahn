package com.yeahn.dao;

import com.yeahn.model.UserVo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    // 로그인
    UserVo getUserAccount(String userId);

    // 회원가입
    void saveUser(UserVo userVo);
}
