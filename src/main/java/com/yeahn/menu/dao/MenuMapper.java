package com.yeahn.menu.dao;

import com.yeahn.menu.dto.MenuConfig;
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
public class MenuMapper {
    @Autowired
    private SqlSessionTemplate sqlSession;
    
    @Cacheable(cacheNames = "getMenuList", key="#menuList")
    public List<MenuConfig> getMenuList(String menuList) {
        return sqlSession.selectList("ConfigMapper.getMenuList", menuList);
    }
    
    public List<MenuConfig> getMenuList() {
        return sqlSession.selectList("ConfigMapper.getMenuList");
    }
    
    public MenuConfig getMenuPage(String menuCode) {
        return sqlSession.selectOne("ConfigMapper.getMenuPage", menuCode);
    }
    
    public List<MenuConfig> getMenuConfigList() {
        return sqlSession.selectList("ConfigMapper.getMenuConfigList");
    }
    
public Map<String, Object> getMenuDetail(String menuCode) {
        return sqlSession.selectOne("ConfigMapper.getMenuDetail", menuCode);
    }
    
    @CacheEvict(value = "getMenuList", allEntries = true)
    public int updateMenu(Map<String, Object> params) {
        return sqlSession.update("ConfigMapper.updateMenu", params);
    }
    
    public int insertMenu(Map<String, Object> params) {
        return sqlSession.insert("ConfigMapper.insertMenu", params);
    }
}
