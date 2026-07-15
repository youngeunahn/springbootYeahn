package com.yeahn.log.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yeahn.log.dao.LogMapper;
import com.yeahn.log.dto.AccessLogVo;
import com.yeahn.log.dto.LogOutboxVo;
import com.yeahn.log.dto.LoginLogVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogOutboxProcessor {

    private static final String ACCESS_LOG_TYPE = "ACCESS";
    private static final String LOGIN_LOG_TYPE = "LOGIN";

    private final LogMapper logMapper;
    private final ObjectMapper objectMapper;

    @Transactional(rollbackFor = Exception.class)
    public void process(Long outboxSeq) throws Exception {
        if (logMapper.claimLogOutbox(outboxSeq) == 0) {
            return;
        }

        LogOutboxVo outbox = logMapper.selectLogOutbox(outboxSeq);
        if (outbox == null) {
            throw new IllegalStateException("로그 Outbox 데이터를 찾을 수 없습니다: " + outboxSeq);
        }

        switch (outbox.getLogType()) {
            case ACCESS_LOG_TYPE -> logMapper.insertAccessLog(
                    objectMapper.readValue(outbox.getPayload(), AccessLogVo.class)
            );
            case LOGIN_LOG_TYPE -> logMapper.insertLoginLog(
                    objectMapper.readValue(outbox.getPayload(), LoginLogVo.class)
            );
            default -> throw new IllegalArgumentException("지원하지 않는 로그 유형입니다: " + outbox.getLogType());
        }

        if (logMapper.completeLogOutbox(outboxSeq) != 1) {
            throw new IllegalStateException("로그 Outbox 완료 처리에 실패했습니다: " + outboxSeq);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Long outboxSeq, int maxRetryCount, String errorMessage) {
        logMapper.failLogOutbox(outboxSeq, maxRetryCount, truncate(errorMessage));
    }

    private String truncate(String value) {
        if (value == null) {
            return "알 수 없는 오류";
        }
        return value.length() <= 1000 ? value : value.substring(0, 1000);
    }
}
