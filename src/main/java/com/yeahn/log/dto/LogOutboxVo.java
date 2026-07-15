package com.yeahn.log.dto;

import lombok.Data;

@Data
public class LogOutboxVo {
    private Long logOutboxSeq;
    private String logType;
    private String payload;
    private String status;
    private int retryCount;
}
