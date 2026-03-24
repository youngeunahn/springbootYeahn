package com.yeahn.template.dto;

import lombok.Data;

@Data
public class TemplateDto {
    private Integer tplSeq;
    private String tplName;
    private String tplPhase;

    private Integer tplAttrSeq;
    private String tplTypeCode;
    private String tplExerName;
    private String tplExerDesc;
    private String tplExerMajor;
    private String tplExerMiddle;
    private String tplExerMinor;

    private String useYn;
    private String delYn;
    private String insIp;
    private String insUserId;
    private String updIp;
    private String updUserId;
}
