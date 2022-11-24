package com.yeahn.config.service;

import com.yeahn.dao.ConfigMapper;
import com.yeahn.model.MenuConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService{
    @Autowired
    private final ConfigMapper configMenuMapper;

    @Override
    public List<MenuConfig> getMenuList() {
        return configMenuMapper.getMenuList();
    }

    @Override
    public MenuConfig getMenuPage(String menuCode) {
        return configMenuMapper.getMenuPage(menuCode);
    }

    @Override
    public List<MenuConfig> getMenuConfigList() {
        List<MenuConfig> MenuList = configMenuMapper.getMenuConfigList();

        for (MenuConfig menulist : MenuList){
            if (menulist.getMENU_LEVEL()==1)
                menulist.setMENU_LEVEL_VIEW(true);
            else if (menulist.getMENU_LEVEL()==2)
                menulist.setMENU_LEVEL_VIEW(false);
            menulist.setMENU_GROUP(menulist.getMENU_CODE().split("_")[0]);
        }

        return MenuList;
    }

    @Override
    public List<MenuConfig> getMenuChildList(Map<String, Object> params){
        List<MenuConfig> MenuChildList = configMenuMapper.getMenuChildList((String) params.get("MENU_PARENT"));

        return MenuChildList;
    }

    @Override
    public Map<String, Object> getMenuDetail(@RequestParam Map<String, Object> params){
        Map<String, Object> MenuDetail = configMenuMapper.getMenuDetail(params.get("menuCode").toString());

        return MenuDetail;
    }
}
