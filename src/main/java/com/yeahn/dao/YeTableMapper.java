package com.yeahn.dao;

import com.yeahn.model.YeahnTable;

import java.util.List;
import java.util.Map;

public interface YeTableMapper {
    List<YeahnTable> getYeahnTableList();

    int editYeahnTable(YeahnTable model);

    int insertYetable(Map<String, Object> params);
}
