package com.yeahn.model;

import lombok.Data;

@Data
public class LoginLogVo {
    private String user_seq;
    private String login_success;
    private String status_code;
    private String fail_reason;
    private String login_req_method;
    private String login_req_ip;
    private String login_req_device;
    private String login_req_browser;
    private String login_req_language;
    private String login_req_os;
    private String login_req_session_id;
    private String login_req_referrer;
    private String login_req_ua_grigin;
}
