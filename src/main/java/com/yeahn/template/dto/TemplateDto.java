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
    private String tplCategoryCode;
    private String tplKindCode;       // 근력/유산소/스트레칭
    private Integer tplMeasure1;      // 세트 / 거리 등
    private Integer tplMeasure2;      // 횟수 / 바퀴 등
    private Integer tplTotalDistance; // 수영 전용 총 거리
    private String tplNote;           // 추가 메모 / 설명
    private Integer tplSortOrder;     // 운동 순서 정렬용

    private String useYn;
    private String delYn;
    private String insIp;
    private String insUserId;
    private String updIp;
    private String updUserId;
}
