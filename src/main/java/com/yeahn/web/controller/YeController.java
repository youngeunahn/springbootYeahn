package com.yeahn.web.controller;

import com.yeahn.model.YeahnTable;
import com.yeahn.web.service.YeTableService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class YeController {

    @Autowired
    private YeTableService yeTableService;

    @RequestMapping("/yetable/list")
    public ModelAndView list(Model model){
        ModelAndView mv = new ModelAndView();

        List<YeahnTable> YeList = yeTableService.getYeahnTableList();

        mv.addObject("YeList", YeList);
        mv.setViewName("/yetable/list");
        return mv;
    }

    @RequestMapping("/yetable/list.json")
    @ResponseBody
    public List<YeahnTable> jsonList(Model model){
        List<YeahnTable> YeList = yeTableService.getYeahnTableList();

        return YeList;
    }

    @RequestMapping("/yetable/create")
    public ModelAndView index(Model model){
        ModelAndView mv = new ModelAndView();

        mv.setViewName("/yetable/create");
        return mv;
    }
}
