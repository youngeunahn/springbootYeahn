package com.yeahn.log.dao;

import com.yeahn.log.dto.AccessLogVo;
import com.yeahn.log.dto.LoginLogVo;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

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
