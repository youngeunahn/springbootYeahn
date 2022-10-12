package com.yeahn.web.service;

import com.yeahn.dao.YeTableMapper;
import com.yeahn.model.YeahnTable;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class YeTableServiceimpl implements YeTableService {
    @Autowired
    private final YeTableMapper yetableMapper;

    @Override
    public List<YeahnTable> getYeahnTableList() {
        return yetableMapper.getYeahnTableList();
    }
}
