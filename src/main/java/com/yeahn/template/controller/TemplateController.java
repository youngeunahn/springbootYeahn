package com.yeahn.template.controller;

import com.yeahn.common.code.CodeDto;
import com.yeahn.common.code.CodeService;
import com.yeahn.template.dto.TemplateDto;
import com.yeahn.template.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/exercise/templates")
public class TemplateController {

    @Autowired
    private TemplateService templateService;
    @Autowired
    private CodeService codeService;

    // 템플릿 관리
    @GetMapping
    public String list(Model model) {
        CodeDto CodeDto = new CodeDto("TPL_TYPE_CODE");
        model.addAttribute("typeCode", codeService.getCodeList(CodeDto));

        CodeDto = new CodeDto("TPL_PHASE");
        model.addAttribute("tplPhase", codeService.getCodeList(CodeDto));

        CodeDto = new CodeDto("TPL_CATEGORY", "SWIM");
        model.addAttribute("tplCategory", codeService.getCodeList(CodeDto));

        TemplateDto tplDto = new TemplateDto();
        tplDto.setTplTypeCode("SWIM");
        model.addAttribute("tplList", templateService.getTplList(tplDto));
        return "exercise/template/list";
    }
}