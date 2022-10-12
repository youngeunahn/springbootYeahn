package com.yeahn.web.service;

import com.yeahn.model.MenuConfig;

import java.util.List;

public interface ConfigService {
    public List<MenuConfig> getMenuList();
    public MenuConfig getMenuPage(String menuCode);
}
