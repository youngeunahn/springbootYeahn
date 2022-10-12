package com.yeahn.dao;

import com.yeahn.model.MenuConfig;

import java.util.List;

public interface ConfigMapper {
    List<MenuConfig> getMenuList();
    MenuConfig getMenuPage(String menuCode);
}
