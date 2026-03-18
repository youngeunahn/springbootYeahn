package com.yeahn.auth.dao;

import com.yeahn.auth.dto.UserVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    // 로그인
    UserVo getUserAccount(@Param("userId") String userId);

    // 회원가입
    void saveUser(UserVo userVo);
}
