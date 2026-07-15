package com.yeahn.log.dao;

import com.yeahn.log.dto.AccessLogVo;
import com.yeahn.log.dto.LogOutboxVo;
import com.yeahn.log.dto.LoginLogVo;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    public int insertLogOutbox(LogOutboxVo vo) {
        return sqlSession.insert("LogMapper.insertLogOutbox", vo);
    }

    public List<Long> selectPendingLogOutboxSeqs(int limit, int maxRetryCount) {
        return sqlSession.selectList("LogMapper.selectPendingLogOutboxSeqs", Map.of(
                "limit", limit,
                "maxRetryCount", maxRetryCount
        ));
    }

    public int claimLogOutbox(Long outboxSeq) {
        return sqlSession.update("LogMapper.claimLogOutbox", outboxSeq);
    }

    public LogOutboxVo selectLogOutbox(Long outboxSeq) {
        return sqlSession.selectOne("LogMapper.selectLogOutbox", outboxSeq);
    }

    public int completeLogOutbox(Long outboxSeq) {
        return sqlSession.update("LogMapper.completeLogOutbox", outboxSeq);
    }

    public int failLogOutbox(Long outboxSeq, int maxRetryCount, String errorMessage) {
        return sqlSession.update("LogMapper.failLogOutbox", Map.of(
                "outboxSeq", outboxSeq,
                "maxRetryCount", maxRetryCount,
                "errorMessage", errorMessage
        ));
    }

    public int deleteCompletedLogOutbox(int retentionDays) {
        return sqlSession.delete("LogMapper.deleteCompletedLogOutbox", retentionDays);
    }
}
