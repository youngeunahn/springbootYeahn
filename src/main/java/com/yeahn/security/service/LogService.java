package com.yeahn.security.service;

import com.yeahn.model.AccessLogVo;
import com.yeahn.model.LoginLogVo;
import com.yeahn.security.dao.LogMapper;
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
