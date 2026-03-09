package com.yeahn.security.dao.impl;

import com.yeahn.security.dao.LogMapper;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
@RequiredArgsConstructor
public class LogMapperImpl implements LogMapper {
    @Autowired
    private SqlSessionTemplate sqlSession;

    @Override
    public int insertLoginLog(Map<String, Object> params) {
        return sqlSession.insert("LogMapper.insertLoginLog", params);
    }
}
