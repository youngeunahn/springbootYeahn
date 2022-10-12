package com.yeahn.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class YeahnTable {
    private Integer NO;
    private String TITLE;
    private String CONTENT;
    private String REG_DATE;
    private String REG_ID;

    public YeahnTable() {}
}
