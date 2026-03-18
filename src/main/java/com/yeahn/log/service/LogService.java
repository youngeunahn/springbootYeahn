package com.yeahn.log.service;

import com.yeahn.log.dto.AccessLogVo;
import com.yeahn.log.dto.LoginLogVo;
import com.yeahn.log.dao.LogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogService {

    private final LogMapper logMapper;

    public int saveAccessLog(AccessLogVo vo) {
        return logMapper.insertAccessLog(vo);
    }

    public int saveLoginLog(LoginLogVo vo) {
        return logMapper.insertLoginLog(vo);
    }
}
