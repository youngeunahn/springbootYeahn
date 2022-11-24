package com.yeahn.dao;

import com.yeahn.model.MenuConfig;

import java.util.List;
import java.util.Map;

public interface ConfigMapper {
    List<MenuConfig> getMenuList();
    MenuConfig getMenuPage(String menuCode);

    List<MenuConfig> getMenuConfigList();
    List<MenuConfig> getMenuChildList(String menuCode);
    Map<String, Object> getMenuDetail(String menuCode);
}
