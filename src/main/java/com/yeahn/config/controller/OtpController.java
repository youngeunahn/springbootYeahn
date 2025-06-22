package com.yeahn.config.controller;

import com.yeahn.config.OtpUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@Controller
public class OtpController {
    @GetMapping("/otp")
    public ModelAndView otp() throws Exception {
        ModelAndView mv = new ModelAndView();

        String secretKey = OtpUtil.generateSecretKey();

        mv.addObject("key", OtpUtil.generateSecretKey());
        mv.addObject("qr", OtpUtil.getQrCodeUrl("test3", secretKey));
        mv.setViewName("/conf/otp");

        return mv;
    }

    @PostMapping("/check")
    public ModelAndView check(
            @RequestParam(value = "secretKey") String secretKey,
            @RequestParam(value = "otp") String otp) {
        ModelAndView mv = new ModelAndView();
        mv.addObject("result", OtpUtil.checkOtp(secretKey, otp));
        return mv;
    }
}
