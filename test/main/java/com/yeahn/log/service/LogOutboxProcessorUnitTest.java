package com.yeahn.log.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yeahn.log.dao.LogMapper;
import com.yeahn.log.dto.LogOutboxVo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogOutboxProcessorUnitTest {

    @Mock
    private LogMapper logMapper;

    @Test
    void processWritesAccessLogAndCompletesOutbox() throws Exception {
        LogOutboxProcessor processor = new LogOutboxProcessor(logMapper, new ObjectMapper());
        LogOutboxVo outbox = outbox("ACCESS", "{\"accessUri\":\"/api/user/plans\",\"responseTime\":15}");
        when(logMapper.claimLogOutbox(1L)).thenReturn(1);
        when(logMapper.selectLogOutbox(1L)).thenReturn(outbox);
        when(logMapper.completeLogOutbox(1L)).thenReturn(1);

        processor.process(1L);

        verify(logMapper).insertAccessLog(any());
        verify(logMapper).completeLogOutbox(1L);
    }

    @Test
    void processWritesLoginLogAndCompletesOutbox() throws Exception {
        LogOutboxProcessor processor = new LogOutboxProcessor(logMapper, new ObjectMapper());
        LogOutboxVo outbox = outbox("LOGIN", "{\"userId\":\"user01\",\"loginSuccess\":\"N\"}");
        when(logMapper.claimLogOutbox(2L)).thenReturn(1);
        when(logMapper.selectLogOutbox(2L)).thenReturn(outbox);
        when(logMapper.completeLogOutbox(2L)).thenReturn(1);

        processor.process(2L);

        verify(logMapper).insertLoginLog(any());
        verify(logMapper).completeLogOutbox(2L);
    }

    @Test
    void processSkipsEventClaimedByAnotherWorker() throws Exception {
        LogOutboxProcessor processor = new LogOutboxProcessor(logMapper, new ObjectMapper());
        when(logMapper.claimLogOutbox(3L)).thenReturn(0);

        processor.process(3L);

        verify(logMapper, never()).selectLogOutbox(3L);
        verify(logMapper, never()).completeLogOutbox(3L);
    }

    @Test
    void processRejectsUnsupportedLogType() {
        LogOutboxProcessor processor = new LogOutboxProcessor(logMapper, new ObjectMapper());
        when(logMapper.claimLogOutbox(4L)).thenReturn(1);
        when(logMapper.selectLogOutbox(4L)).thenReturn(outbox("UNKNOWN", "{}"));

        assertThrows(IllegalArgumentException.class, () -> processor.process(4L));

        verify(logMapper, never()).completeLogOutbox(4L);
    }

    private LogOutboxVo outbox(String logType, String payload) {
        LogOutboxVo outbox = new LogOutboxVo();
        outbox.setLogType(logType);
        outbox.setPayload(payload);
        return outbox;
    }
}
