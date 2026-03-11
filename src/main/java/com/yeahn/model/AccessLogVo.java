package com.yeahn.model;

import lombok.Data;

@Data
public class AccessLogVo {
    private String accessMenuSeq;
    private String accessUri;
    private String accessQueryString;
    private String accessFormData;
    private String accessMethod;
    private String accessIp;
    private String accessDevice;
    private String accessBrowser;
    private String accessLanguage;
    private String accessOs;
    private String accessSessionId;
    private String accessReferrer;
    private String uaOrigin;
}
