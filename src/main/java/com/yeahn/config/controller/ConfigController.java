package com.yeahn.config.controller;

import com.yeahn.config.service.ConfigService;
import com.yeahn.model.MenuConfig;
import com.yeahn.model.YeahnTable;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Controller
public class ConfigController {

    @Autowired
    private ConfigService configService;

    @RequestMapping("/conf/menu")
    public ModelAndView list(Model model) {
        ModelAndView mv = new ModelAndView();

        List<MenuConfig> menuList = configService.getMenuConfigList();

        mv.addObject("menuList", menuList);
        mv.setViewName("/conf/menu");
        return mv;
    }

    @RequestMapping("/ajax/conf/menuChildList")
    @ResponseBody
    public List<MenuConfig> getMenuChildList(@RequestParam Map<String, Object> params){
        List<MenuConfig> menuChildList = configService.getMenuChildList(params);

        return menuChildList;
    }

    @RequestMapping("/ajax/conf/menuDetail")
    @ResponseBody
    public Map<String, Object> getMenuDetail(@RequestParam Map<String, Object> params){
        Map<String, Object> MenuDetail = configService.getMenuDetail(params);

        return MenuDetail;
    }
}