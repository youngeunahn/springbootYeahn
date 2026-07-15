package com.yeahn.log.service;

import com.yeahn.log.dao.LogMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogOutboxWorkerUnitTest {

    @Mock
    private LogMapper logMapper;

    @Mock
    private LogOutboxProcessor processor;

    @Test
    void processPendingLogsProcessesEverySelectedEvent() throws Exception {
        LogOutboxWorker worker = worker();
        when(logMapper.selectPendingLogOutboxSeqs(100, 5)).thenReturn(List.of(1L, 2L));

        worker.processPendingLogs();

        verify(processor).process(1L);
        verify(processor).process(2L);
    }

    @Test
    void processPendingLogsMarksFailedEventAndContinues() throws Exception {
        LogOutboxWorker worker = worker();
        when(logMapper.selectPendingLogOutboxSeqs(100, 5)).thenReturn(List.of(1L, 2L));
        doThrow(new IllegalStateException("DB 오류")).when(processor).process(1L);

        worker.processPendingLogs();

        verify(processor).markFailed(1L, 5, "DB 오류");
        verify(processor).process(2L);
    }

    private LogOutboxWorker worker() {
        LogOutboxWorker worker = new LogOutboxWorker(logMapper, processor);
        ReflectionTestUtils.setField(worker, "batchSize", 100);
        ReflectionTestUtils.setField(worker, "maxRetryCount", 5);
        return worker;
    }
}
