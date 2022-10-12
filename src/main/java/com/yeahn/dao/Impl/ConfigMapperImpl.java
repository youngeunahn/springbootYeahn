package com.yeahn.dao.Impl;

import com.yeahn.dao.ConfigMapper;
import com.yeahn.model.MenuConfig;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ConfigMapperImpl implements ConfigMapper {
    @Autowired
    private SqlSessionTemplate sqlSession;

    @Override
    public List<MenuConfig> getMenuList() {
        return sqlSession.selectList("ConfigMapper.getMenuList");
    }

    @Override
    public MenuConfig getMenuPage(String menuCode) {
        return sqlSession.selectOne("ConfigMapper.getMenuPage", menuCode);
    }
}
