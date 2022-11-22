package com.yeahn.web.controller;

import com.yeahn.config.service.ConfigService;
import com.yeahn.model.MenuConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class MenuController {

    @Autowired
    private ConfigService configService;

    @RequestMapping("/*")
    public ModelAndView Menupage(HttpServletRequest request) {
//        final String url = request.getRequestURI();
//
        ModelAndView mv = new ModelAndView();
//
//        List<MenuConfig> MenuList = configService.getMenuList();
//        for (MenuConfig menulist : MenuList){
//            if (menulist.getMENU_LEVEL()==1)
//                menulist.setMENU_LEVEL_VIEW(true);
//            else if (menulist.getMENU_LEVEL()==2)
//                menulist.setMENU_LEVEL_VIEW(false);
//            menulist.setMENU_GROUP(menulist.getMENU_CODE().split("_")[0]);
//
//        mv.addObject("MenuList", MenuList);
//
//            if(request.getParameter("menuCode") != null){
//                MenuConfig MenuPage = configService.getMenuPage(request.getParameter("menuCode").toString());
//                mv.addObject("MenuPage", MenuPage);
//            }
//        }
//        mv.setViewName("layout/gnb");
        return mv;
    }
}
