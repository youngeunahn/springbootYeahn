package com.yeahn.dao.Impl;

import com.yeahn.dao.UserMapper;
import com.yeahn.model.UserVo;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserMapperImpl implements UserMapper {

    @Autowired
    private SqlSessionTemplate sqlSession;

    @Override
    public UserVo getUserAccount(String userId) {
        return sqlSession.selectOne("UserMapper.getUserAccount", userId);
    }

    @Override
    public void saveUser(UserVo userVo) {
        sqlSession.insert("UserMapper.saveUser", userVo);
    }
}
