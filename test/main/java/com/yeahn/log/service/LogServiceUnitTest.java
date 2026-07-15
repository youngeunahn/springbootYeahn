package com.yeahn.log.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yeahn.log.dao.LogMapper;
import com.yeahn.log.dto.AccessLogVo;
import com.yeahn.log.dto.LogOutboxVo;
import com.yeahn.log.dto.LoginLogVo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogServiceUnitTest {

    @Mock
    private LogMapper logMapper;

    @Test
    void saveAccessLogEnqueuesSerializedEvent() {
        LogService logService = new LogService(logMapper, new ObjectMapper());
        AccessLogVo log = new AccessLogVo();
        log.setAccessUri("/api/user/plans");
        when(logMapper.insertLogOutbox(org.mockito.ArgumentMatchers.any())).thenReturn(1);

        int result = logService.saveAccessLog(log);

        ArgumentCaptor<LogOutboxVo> captor = ArgumentCaptor.forClass(LogOutboxVo.class);
        verify(logMapper).insertLogOutbox(captor.capture());
        assertEquals(1, result);
        assertEquals("ACCESS", captor.getValue().getLogType());
        assertTrue(captor.getValue().getPayload().contains("/api/user/plans"));
    }

    @Test
    void saveLoginLogEnqueuesSerializedEvent() {
        LogService logService = new LogService(logMapper, new ObjectMapper());
        LoginLogVo log = new LoginLogVo();
        log.setUserId("user01");
        log.setLoginSuccess("Y");
        when(logMapper.insertLogOutbox(org.mockito.ArgumentMatchers.any())).thenReturn(1);

        int result = logService.saveLoginLog(log);

        ArgumentCaptor<LogOutboxVo> captor = ArgumentCaptor.forClass(LogOutboxVo.class);
        verify(logMapper).insertLogOutbox(captor.capture());
        assertEquals(1, result);
        assertEquals("LOGIN", captor.getValue().getLogType());
        assertTrue(captor.getValue().getPayload().contains("user01"));
    }
}
