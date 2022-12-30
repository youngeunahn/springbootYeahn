package com.yeahn.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@RequiredArgsConstructor
@Controller
public class PayController {

    @RequestMapping("/pay/create")
    public ModelAndView list(Model model){
        ModelAndView mv = new ModelAndView();

        mv.setViewName("/pay/create");
        return mv;
    }
}
