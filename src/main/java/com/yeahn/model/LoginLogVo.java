package com.yeahn.model;

import lombok.Data;

@Data
public class LoginLogVo {
    private String userId;
    private String loginSuccess;
    private String statusCode;
    private String failReason;
    private String loginReqMethod;
    private String loginReqIp;
    private String loginReqDevice;
    private String loginReqBrowser;
    private String loginReqLanguage;
    private String loginReqOs;
    private String loginReqSessionId;
    private String loginReqReferrer;
    private String loginReqUaOrigin;
}
