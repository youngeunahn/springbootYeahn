package com.yeahn.config.service;

import com.yeahn.model.MenuConfig;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

public interface ConfigService {
    public List<MenuConfig> getMenuList(String menuList);
    public List<MenuConfig> getMenuList();
    public MenuConfig getMenuPage(String menuCode);
    public List<MenuConfig> getMenuConfigList();
    public List<MenuConfig> getMenuChildList(Map<String, Object> params);
    public Map<String, Object> getMenuDetail(Map<String, Object> params);
    public int updateMenu(Map<String, Object> params);
    public int insertMenu(Map<String, Object> params);
}
