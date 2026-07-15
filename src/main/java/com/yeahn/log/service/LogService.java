package com.yeahn.log.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yeahn.log.dto.LogOutboxVo;
import com.yeahn.log.dto.AccessLogVo;
import com.yeahn.log.dto.LoginLogVo;
import com.yeahn.log.dao.LogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogService {

    private final LogMapper logMapper;
    private final ObjectMapper objectMapper;

    public int saveAccessLog(AccessLogVo vo) {
        return enqueue("ACCESS", vo);
    }

    public int saveLoginLog(LoginLogVo vo) {
        return enqueue("LOGIN", vo);
    }

    private int enqueue(String logType, Object payload) {
        LogOutboxVo outbox = new LogOutboxVo();
        outbox.setLogType(logType);
        outbox.setPayload(toJson(payload));
        return logMapper.insertLogOutbox(outbox);
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("로그 이벤트 직렬화에 실패했습니다.", e);
        }
    }
}
