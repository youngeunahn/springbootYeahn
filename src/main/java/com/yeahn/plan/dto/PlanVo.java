package com.yeahn.plan.dto;

import lombok.Data;
import java.util.List;

@Data
public class PlanVo {
    private Integer planSeq;
    private String userId;
    private String planTypeCode;
    private String planName;
    private String planDate;
    private String useYn;
    private String delYn;
    private String insDt;
    private String insIp;
    private String insUserId;
    private String updDt;
    private String updIp;
    private String updUserId;

    // 상세 리스트 (N:1 관계)
    private List<PlanDetailVo> details;
}
