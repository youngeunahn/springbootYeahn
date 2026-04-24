package com.yeahn.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

//@Controller
public class ViewController {

    @RequestMapping("/*")
    public ModelAndView Menupage() {
        ModelAndView mv = new ModelAndView();
        return mv;
    }
}
