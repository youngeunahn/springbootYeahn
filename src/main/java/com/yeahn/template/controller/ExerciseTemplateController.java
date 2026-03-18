package com.yeahn.template.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/exercise/templates")
public class ExerciseTemplateController {

    // 템플릿 관리
    @GetMapping
    public String list(Model model) {
        model.addAttribute("templates", "");
        return "exercise/template/list";
    }
}