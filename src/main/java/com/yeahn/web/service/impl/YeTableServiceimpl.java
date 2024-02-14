package com.yeahn.web.service.impl;

import com.yeahn.dao.YeTableMapper;
import com.yeahn.model.YeahnTable;
import com.yeahn.web.service.YeTableService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class YeTableServiceimpl implements YeTableService {
    @Autowired
    private final YeTableMapper yetableMapper;

    @Override
    public List<YeahnTable> getYeahnTableList() {
        return yetableMapper.getYeahnTableList();
    }

    @Override
    public int editYeahnTable(YeahnTable model) {
        return yetableMapper.editYeahnTable(model);
    }

    @Override
    public int insertYetable(Map<String, Object> params) {
        return yetableMapper.insertYetable(params);
    };

    @Override
    public YeahnTable getYeahnTableData(@RequestParam Map<String, Object> params) {
        return yetableMapper.getYeahnTable(params);
    }
}
