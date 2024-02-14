package com.yeahn.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RequiredArgsConstructor
@Controller
public class LoginController {

    @RequestMapping("/login")
    public ModelAndView list(){
        ModelAndView mv = new ModelAndView();
        mv.setViewName("/login/login");
        return mv;
    }
}
