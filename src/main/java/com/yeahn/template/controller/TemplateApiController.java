package com.yeahn.template.controller;

import com.yeahn.common.code.CodeDto;
import com.yeahn.common.code.CodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class TemplateApiController {

    @Autowired
    private CodeService codeService;

    @PostMapping("/exercise/templates")
    public Map<String, List<CodeDto>> list(@RequestBody CodeDto dto) {
        Map result = new HashMap();

        CodeDto cateDto = new CodeDto();
        cateDto.setTypeClass("TPL_CATEGORY");
        cateDto.setTypeCode(dto.getTypeCode());
        cateDto.setRef1(dto.getRef1());
        result.put("tplCate", codeService.getCodeList(cateDto, dto.getTypeCode()));

        if (dto.getRef1().equals("GYM")){
            CodeDto kindDto = new CodeDto();
            kindDto.setTypeClass("TPL_KIND_CODE");
            kindDto.setTypeCode(dto.getTypeCode());
            result.put("tplKind", codeService.getCodeList(kindDto, dto.getTypeCode()));
        }

        return result;
    }
}