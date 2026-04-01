package com.yeahn.template.controller;

import com.yeahn.common.code.CodeDto;
import com.yeahn.common.code.CodeService;
import com.yeahn.template.dto.TemplateDto;
import com.yeahn.template.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class TemplateApiController {

    @Autowired
    private CodeService codeService;

    @Autowired
    private TemplateService templateService;

    @PostMapping("/exercise/templates")
    public Map<String, List<CodeDto>> list(@RequestBody CodeDto dto) {
        Map<String, List<CodeDto>> result = new HashMap<>();

        CodeDto cateDto = new CodeDto();
        cateDto.setTypeClass("TPL_CATEGORY");
        cateDto.setTypeCode(dto.getTypeCode());
        cateDto.setRef1(dto.getRef1());
        result.put("tplCate", codeService.getCodeList(cateDto, dto.getTypeCode()));

        if ("GYM".equals(dto.getRef1())) {
            CodeDto kindDto = new CodeDto();
            kindDto.setTypeClass("TPL_KIND_CODE");
            kindDto.setTypeCode(dto.getTypeCode());
            result.put("tplKind", codeService.getCodeList(kindDto, dto.getTypeCode()));
        } else {
            result.put("tplKind", new ArrayList<>());
        }

        return result;
    }

    @PostMapping("/exercise/templates/create")
    public Long create(@RequestBody TemplateDto request, HttpServletRequest req) {
        return templateService.createTemplate(request, req);
    }

    @PostMapping("/exercise/templates/reorder")
    public void reorder(@RequestBody TemplateDto request, HttpServletRequest req) {
        templateService.reorderExercises(request, req);
    }

    @GetMapping("/exercise/templates/{tplSeq}")
    public TemplateDto detail(@PathVariable Long tplSeq) {
        return templateService.getTplDetail(tplSeq);
    }
}