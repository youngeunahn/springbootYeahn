package com.yeahn.menu.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yeahn.menu.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Controller
public class MenuController {

    @Autowired
    private MenuService menuService;

    @Autowired
    private ObjectMapper objectMapper;

    @RequestMapping("/conf/menu")
    public ModelAndView list(Model model) throws Exception {
        ModelAndView mv = new ModelAndView();

        mv.addObject("menuList", menuService.getMenuConfigList());
        mv.addObject("menuDataJson", objectMapper.writeValueAsString(menuService.getMenuConfigTree()));
        mv.setViewName("conf/menu");
        return mv;
    }

    @RequestMapping("/ajax/conf/menuDetail")
    @ResponseBody
    public Map<String, Object> getMenuDetail(@RequestParam Map<String, Object> params){
        Map<String, Object> menuDetail = menuService.getMenuDetail(params);

        return menuDetail;
    }

    @RequestMapping("/ajax/conf/menuUpdate")
    @ResponseBody
    public int updateMenu(@RequestParam Map<String, Object> params){
        int result = menuService.updateMenu(params);

        return result;
    }

    @RequestMapping("/ajax/conf/menuInsert")
    @ResponseBody
    public int insertMenu(@RequestParam Map<String, Object> params){
        int result = menuService.insertMenu(params);

        return result;
    }
}