package com.yeahn.common;

import lombok.Data;

@Data
public class UserAgentInfo {
    private String device;
    private String browser;
    private String os;
    private String ua;

    public UserAgentInfo(String browser, String os, String device) {
        this.browser = browser;
        this.os = os;
        this.device = device;
    }
}
