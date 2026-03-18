package com.yeahn.menu.service;

import com.yeahn.menu.dao.MenuMapper;
import com.yeahn.menu.dto.MenuConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MenuService {
    @Autowired
    private final MenuMapper MenuMapper;

    public List<MenuConfig> getMenuList(String menuList) {
        return MenuMapper.getMenuList(menuList);
    }

    public List<MenuConfig> getMenuList() {
        return MenuMapper.getMenuList();
    }

    public MenuConfig getMenuPage(String menuCode) {
        return MenuMapper.getMenuPage(menuCode);
    }

    public List<MenuConfig> getMenuConfigList() {
        List<MenuConfig> MenuList = MenuMapper.getMenuConfigList();

        for (MenuConfig menulist : MenuList){
            if (menulist.getMENU_LEVEL()==1)
                menulist.setMENU_LEVEL_VIEW(true);
            else if (menulist.getMENU_LEVEL()==2)
                menulist.setMENU_LEVEL_VIEW(false);
            menulist.setMENU_GROUP(menulist.getMENU_CODE().split("_")[0]);
        }

        return MenuList;
    }

    public List<MenuConfig> getMenuChildList(Map<String, Object> params){
        List<MenuConfig> MenuChildList = MenuMapper.getMenuChildList((String) params.get("MENU_PARENT"));

        return MenuChildList;
    }

    public Map<String, Object> getMenuDetail(Map<String, Object> params){
        Map<String, Object> MenuDetail = MenuMapper.getMenuDetail(params.get("menuCode").toString());

        return MenuDetail;
    }

    public int updateMenu(Map<String, Object> params){
        int result = MenuMapper.updateMenu(params);

        return result;
    }

    public int insertMenu(Map<String, Object> params){
        int result = MenuMapper.insertMenu(params);

        return result;
    }
}
