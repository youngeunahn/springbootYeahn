package com.yeahn.security.service;

import com.yeahn.security.dao.LogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogService {

    private final LogMapper logMapper;
}
