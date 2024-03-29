package com.yeahn.dao.Impl;

import com.yeahn.dao.ConfigMapper;
import com.yeahn.model.MenuConfig;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ConfigMapperImpl implements ConfigMapper {
    @Autowired
    private SqlSessionTemplate sqlSession;

    @Override
    @Cacheable(cacheNames = "getMenuList", key="#menuList")
    public List<MenuConfig> getMenuList(String menuList) {
        return sqlSession.selectList("ConfigMapper.getMenuList", menuList);
    }

    @Override
    public List<MenuConfig> getMenuList() {
        return sqlSession.selectList("ConfigMapper.getMenuList");
    }

    @Override
    public MenuConfig getMenuPage(String menuCode) {
        return sqlSession.selectOne("ConfigMapper.getMenuPage", menuCode);
    }

    @Override
    public List<MenuConfig> getMenuConfigList() {
        return sqlSession.selectList("ConfigMapper.getMenuConfigList");
    }

    @Override
    public List<MenuConfig> getMenuChildList(String menuCode) {
        return sqlSession.selectList("ConfigMapper.getMenuChildList", menuCode);
    }

    @Override
    public Map<String, Object> getMenuDetail(String menuCode) {
        return sqlSession.selectOne("ConfigMapper.getMenuDetail", menuCode);
    }

    @Override
    @CacheEvict(value = "getMenuList", allEntries = true)
    public int updateMenu(Map<String, Object> params) {
        return sqlSession.update("ConfigMapper.updateMenu", params);
    }

    @Override
    public int insertMenu(Map<String, Object> params) {
        return sqlSession.insert("ConfigMapper.insertMenu", params);
    }
}
