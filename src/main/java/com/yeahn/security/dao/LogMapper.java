package com.yeahn.security.dao;

import com.yeahn.model.AccessLogVo;
import com.yeahn.model.LoginLogVo;
import com.yeahn.security.dao.LogMapper;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
@RequiredArgsConstructor
public class LogMapper {

    @Autowired
    private SqlSessionTemplate sqlSession;

    public int insertAccessLog(AccessLogVo vo) {
        return sqlSession.insert("LogMapper.insertAccessLog", vo);
    }

    public int insertLoginLog(LoginLogVo vo) {
        return sqlSession.insert("LogMapper.insertLoginLog", vo);
    }
}
