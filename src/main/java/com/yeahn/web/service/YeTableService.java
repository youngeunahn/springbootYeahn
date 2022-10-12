package com.yeahn.web.service;

import com.yeahn.model.YeahnTable;

import java.util.List;

public interface YeTableService {
    public List<YeahnTable> getYeahnTableList();
    public int editYeahnTable(YeahnTable model);
}
