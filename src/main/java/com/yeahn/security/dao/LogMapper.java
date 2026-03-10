package com.yeahn.security.dao;

import java.util.Map;

public interface LogMapper {
    int insertAccessLog(Map<String, Object> params);
    int insertLoginLog(Map<String, Object> params);
}
