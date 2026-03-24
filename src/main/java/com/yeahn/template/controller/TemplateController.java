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
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/exercise/templates")
public class TemplateController {

    @Autowired
    private TemplateService templateService;
    @Autowired
    private CodeService codeService;

    @GetMapping
    public String list(
            @RequestParam String menuCode,
            @RequestParam(required = false, defaultValue = "SWIM") String typeCode,
            @RequestParam(required = false, defaultValue = "") String tplPhase,
            Model model) {

        // 메뉴코드
        model.addAttribute("menuCode", menuCode);

        // 코드리스트 (검색 필터)
        model.addAttribute("typeCode", codeService.getCodeList(new CodeDto("TPL_TYPE_CODE"), typeCode));
        model.addAttribute("tplPhase", codeService.getCodeList(new CodeDto("TPL_PHASE"), null));
        model.addAttribute("tplCategory", codeService.getCodeList(new CodeDto("TPL_CATEGORY", typeCode), null));

        // 템플릿 리스트
        TemplateDto tplDto = new TemplateDto();
        tplDto.setTplTypeCode(typeCode);
        model.addAttribute("tplList", templateService.getTplList(tplDto));

        return "exercise/template/list";
    }
}