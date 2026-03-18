package com.yeahn.yetable.dao;

import com.yeahn.yetable.dto.YeahnTable;

import java.util.List;
import java.util.Map;

public interface YeTableMapper {
    List<YeahnTable> getYeahnTableList();

    int editYeahnTable(YeahnTable model);

    int insertYetable(Map<String, Object> params);

    YeahnTable getYeahnTable(Map<String, Object> params);
}
