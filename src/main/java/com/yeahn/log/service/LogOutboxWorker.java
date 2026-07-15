package com.yeahn.log.service;

import com.yeahn.log.dao.LogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogOutboxWorker {

    private final LogMapper logMapper;
    private final LogOutboxProcessor processor;

    @Value("${app.log-outbox.batch-size:100}")
    private int batchSize;

    @Value("${app.log-outbox.max-retry-count:5}")
    private int maxRetryCount;

    @Value("${app.log-outbox.retention-days:7}")
    private int retentionDays;

    @Scheduled(fixedDelayString = "${app.log-outbox.poll-interval-ms:1000}")
    public void processPendingLogs() {
        List<Long> outboxSeqs = logMapper.selectPendingLogOutboxSeqs(batchSize, maxRetryCount);
        for (Long outboxSeq : outboxSeqs) {
            try {
                processor.process(outboxSeq);
            } catch (Exception e) {
                log.error("로그 Outbox 처리에 실패했습니다. outboxSeq={}", outboxSeq, e);
                try {
                    processor.markFailed(outboxSeq, maxRetryCount, e.getMessage());
                } catch (Exception markFailedException) {
                    log.error("로그 Outbox 실패 상태 기록에 실패했습니다. outboxSeq={}", outboxSeq, markFailedException);
                }
            }
        }
    }

    @Scheduled(cron = "${app.log-outbox.cleanup-cron:0 30 3 * * *}")
    public void cleanupCompletedLogs() {
        int deleted = logMapper.deleteCompletedLogOutbox(retentionDays);
        if (deleted > 0) {
            log.info("완료된 로그 Outbox {}건을 정리했습니다.", deleted);
        }
    }
}
