package com.yeahn.web.service;

import com.yeahn.dao.ConfigMapper;
import com.yeahn.model.MenuConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
