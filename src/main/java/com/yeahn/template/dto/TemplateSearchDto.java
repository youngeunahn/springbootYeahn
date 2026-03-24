package com.yeahn.template.dto;

import lombok.Data;

import java.util.List;

@Data
public class TemplateSearchDto {
    private String tplType;
    private String tplName;
    private List<String> tplPhase;
    private List<String> tplCategory;
}