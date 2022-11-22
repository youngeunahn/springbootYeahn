package com.yeahn.config.service;

import com.yeahn.model.MenuConfig;

import java.util.List;
import java.util.Map;

public interface ConfigService {
    public List<MenuConfig> getMenuList();
    public MenuConfig getMenuPage(String menuCode);
    public List<MenuConfig> getMenuConfigList();
    public List<MenuConfig> getMenuChildList(Map<String, Object> params);
}
