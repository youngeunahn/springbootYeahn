package com.yeahn.plan.dto;

import lombok.Data;

@Data
public class PlanDetailVo {
    private Integer planDetailSeq;
    private Integer planSeq;
    private String planPhase;
    private String planExerName;
    private String planCategoryCode;
    private String planKindCode;
    private String planNote;
    private String planReps;
    private String planSets;
    private String planTime;
    private Integer planTotalDistance;
    private Integer planSortOrder;
    private String useYn;
    private String delYn;
    private String insDt;
    private String insIp;
    private String insUserId;
    private String updDt;
    private String updIp;
    private String updUserId;
}
