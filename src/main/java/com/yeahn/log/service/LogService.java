package com.yeahn.log.service;

import com.yeahn.log.dto.AccessLogVo;
import com.yeahn.log.dto.LoginLogVo;
import com.yeahn.log.dao.LogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogService {

    private final LogMapper logMapper;

    public int saveAccessLog(AccessLogVo vo) {
        long start = System.nanoTime();
        try {
            return logMapper.insertAccessLog(vo);
        } finally {
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            log.info("saveAccessLog elapsedMs={}, uri={}", elapsedMs, vo.getAccessUri());
        }
    }

    public int saveLoginLog(LoginLogVo vo) {
        long start = System.nanoTime();
        try {
            return logMapper.insertLoginLog(vo);
        } finally {
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            log.info("saveLoginLog elapsedMs={}, success={}", elapsedMs, vo.getLoginSuccess());
        }
    }
}
