package com.yeahn.auth.controller;

import com.yeahn.auth.dto.UserVo;
import com.yeahn.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;

@Slf4j
@RequiredArgsConstructor
@Controller
public class LoginController {

    @Autowired
    UserService userService;

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public ModelAndView list(){
        ModelAndView mv = new ModelAndView();
        mv.setViewName("/login/login");
        return mv;
    }

    @RequestMapping("/access_denied")
    public ModelAndView accessDenied(){
        ModelAndView mv = new ModelAndView();
        mv.setViewName("/login/access_denied");
        return mv;
    }

    @RequestMapping(value = "/signUp", method = RequestMethod.GET)
    public ModelAndView signUp(){
        ModelAndView mv = new ModelAndView();
        mv.setViewName("/login/sign_up");
        return mv;
    }

    @RequestMapping(value = "/signUp", method = RequestMethod.POST)
    public String signUpProcess(@RequestParam HashMap<String, Object> param, UserVo uservo){
        uservo.setUserPwd(param.get("password").toString());
        userService.joinUser(uservo);
        return "redirect:/login";
    }
}
