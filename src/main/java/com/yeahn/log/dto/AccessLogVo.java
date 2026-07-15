package com.yeahn.log.dto;

import lombok.Data;

@Data
public class AccessLogVo {
    private String accessUri;
    private String accessMethod;
    private String accessIp;
    private String accessDevice;
    private String accessBrowser;
    private String accessLanguage;
    private String accessOs;
    private String accessSessionId;
    private String accessReferrer;
    private String uaOrigin;
    private String accessStatusCode;
    private long responseTime;
}
