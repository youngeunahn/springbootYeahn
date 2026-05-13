package com.yeahn.yetable.service.impl;

import com.yeahn.yetable.dto.YeahnTable;
import com.yeahn.yetable.entity.YeahnTableEntity;
import com.yeahn.yetable.repository.YeahnTableRepository;
import com.yeahn.yetable.service.YeTableService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class YeTableServiceimpl implements YeTableService {
    private final YeahnTableRepository yeahnTableRepository;

    @Override
    public List<YeahnTable> getYeahnTableList() {
        List<YeahnTable> result = new ArrayList<>();
        for (YeahnTableEntity entity : yeahnTableRepository.findAll()) {
            result.add(toDto(entity));
        }
        return result;
    }

    @Override
    @Transactional
    public int editYeahnTable(YeahnTable model) {
        if (model == null || model.getNO() == null) {
            return 0;
        }

        YeahnTableEntity entity = yeahnTableRepository.findById(model.getNO()).orElse(null);
        if (entity == null) {
            return 0;
        }

        entity.setTitle(model.getTITLE());
        return 1;
    }

    @Override
    @Transactional
    public int insertYetable(Map<String, Object> params) {
        YeahnTableEntity entity = new YeahnTableEntity();
        entity.setTitle(toNullableString(params.get("TITLE")));
        entity.setContent(toNullableString(params.get("CONTENT")));
        entity.setRegDate(toNullableString(params.get("REG_DATE")));
        entity.setRegId(toNullableString(params.get("REG_ID")));
        yeahnTableRepository.save(entity);
        return 1;
    }

    @Override
    public YeahnTable getYeahnTableData(@RequestParam Map<String, Object> params) {
        Integer no = toNullableInteger(params.get("NO"));
        if (no == null) {
            return null;
        }
        return yeahnTableRepository.findById(no)
                .map(this::toDto)
                .orElse(null);
    }

    private YeahnTable toDto(YeahnTableEntity entity) {
        YeahnTable dto = new YeahnTable();
        dto.setNO(entity.getNo());
        dto.setTITLE(entity.getTitle());
        dto.setCONTENT(entity.getContent());
        dto.setREG_DATE(entity.getRegDate());
        dto.setREG_ID(entity.getRegId());
        return dto;
    }

    private String toNullableString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Integer toNullableInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        return Integer.valueOf(String.valueOf(value));
    }
}
