package com.yeahn.dao;

import com.yeahn.model.YeahnTable;

import java.util.List;

public interface YeTableMapper {
    List<YeahnTable> getYeahnTableList();

    int editYeahnTable(YeahnTable model);
}
