package com.yeahn.dao.Impl;

import com.yeahn.dao.YeTableMapper;
import com.yeahn.model.YeahnTable;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class YeTableMapperImpl implements YeTableMapper {
    @Autowired
    private SqlSessionTemplate sqlSession;

    @Override
    public List<YeahnTable> getYeahnTableList() {
        return sqlSession.selectList("YeTableMapper.getYeahnTableList");
    }
}
