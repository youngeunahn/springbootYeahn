package com.yeahn.model;

import lombok.Data;

@Data
public class AccessLogVo {
    private String access_menu_seq;
    private String access_uri;
    private String access_query_string;
    private String access_form_data;
    private String access_method;
    private String acecss_ip;
    private String access_device;
    private String access_browser;
    private String access_language;
    private String access_os;
    private String access_session_id;
    private String access_referrer;
    private String ua_origin;
}
