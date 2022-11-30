package com.yeahn.dao.Impl;

import com.yeahn.dao.YeTableMapper;
import com.yeahn.model.YeahnTable;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class YeTableMapperImpl implements YeTableMapper {
    @Autowired
    private SqlSessionTemplate sqlSession;

    @Override
    public List<YeahnTable> getYeahnTableList() {
        return sqlSession.selectList("YeTableMapper.getYeahnTableList");
    }

    @Override
    public int editYeahnTable(YeahnTable model) {
        return sqlSession.update("YeTableMapper.editYeahnTable", model);
    }

    @Override
    public int insertYetable(Map<String, Object> params) {
        return sqlSession.insert("YeTableMapper.insertYetable", params);
    }

    @Override
    public YeahnTable getYeahnTable(Map<String, Object> params) {
        return sqlSession.selectOne("YeTableMapper.getYeahnTable", params);
    }
}
