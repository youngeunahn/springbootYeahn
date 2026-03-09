package com.yeahn.security.dao;

import java.util.Map;

public interface LogMapper {
    int insertLoginLog(Map<String, Object> params);
}
