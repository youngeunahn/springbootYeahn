package com.yeahn.web.service;

import com.yeahn.model.YeahnTable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

public interface YeTableService {
    public List<YeahnTable> getYeahnTableList();
    public int editYeahnTable(YeahnTable model);
    public int insertYetable(@RequestParam Map<String, Object> params);
}
