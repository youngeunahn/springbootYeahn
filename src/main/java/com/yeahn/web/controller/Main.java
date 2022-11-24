package com.yeahn.web.controller;

import com.yeahn.model.YeahnTable;
import com.yeahn.web.service.YeTableService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RequiredArgsConstructor
@Controller
public class Main {
    @Autowired
    private YeTableService yeTableService;

    @RequestMapping({"","/","/index"})
    public ModelAndView index(Model model){
        // mustache 를 사용하게 되면 앞에 경로(src/main/resources/templates)와
        // 뒤에 확장자(.mustache)는 생략을 한 순수한 파일의 이름만 반환하면 된다.
        // 아래와 같이 index 를 반환하게 되면
        // src/main/resources/templates/index.mustache 로 변환되어 View Resolver 가 처리하게 된다.
        ModelAndView mv = new ModelAndView();

        List<YeahnTable> YeList = yeTableService.getYeahnTableList();

        mv.addObject("YeList", YeList);
        mv.addObject("ObjectTest","테스트트트트");
        mv.addObject("menuCode", "MENU");
        mv.setViewName("index");
        return mv;
    }

}
