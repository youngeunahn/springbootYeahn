package com.yeahn.menu.service;

import com.yeahn.menu.dao.MenuMapper;
import com.yeahn.menu.dto.MenuConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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

    public List<Map<String, Object>> getMenuConfigTree() {
        List<MenuConfig> flatList = MenuMapper.getMenuConfigList();

        Map<String, Map<String, Object>> nodeMap = new LinkedHashMap<>();
        for (MenuConfig menu : flatList) {
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("id", menu.getMENU_CODE());
            node.put("name", menu.getMENU_TITLE() != null ? menu.getMENU_TITLE() : "");
            node.put("children", new ArrayList<>());
            nodeMap.put(menu.getMENU_CODE(), node);
        }

        for (MenuConfig menu : flatList) {
            String parent = menu.getMENU_PARENT();
            if (parent != null && !parent.isEmpty() && nodeMap.containsKey(parent)) {
                ((List<Object>) nodeMap.get(parent).get("children")).add(nodeMap.get(menu.getMENU_CODE()));
            }
        }

        Map<String, Object> rootNode = nodeMap.get("ROOT");
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("id", "ROOT");
        root.put("name", "심심할때 쓰는 개발일기");
        root.put("children", rootNode != null ? rootNode.get("children") : new ArrayList<>());

        List<Map<String, Object>> tree = new ArrayList<>();
        tree.add(root);
        return tree;
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
