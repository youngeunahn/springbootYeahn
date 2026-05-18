package com.yeahn.template.controller;

import com.yeahn.template.dto.TemplateDto;
import com.yeahn.template.dto.TemplateSearchDto;
import com.yeahn.template.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/templates")
public class UserTemplateApiController {

    private final TemplateService templateService;

    @GetMapping
    public List<TemplateDto> list(
            @RequestParam(required = false, defaultValue = "SWIM") String typeCode,
            @RequestParam(required = false) String keyword) {

        TemplateSearchDto searchDto = new TemplateSearchDto();
        searchDto.setTplType(typeCode);
        searchDto.setTplName(keyword);
        return templateService.searchTplList(searchDto);
    }

    @GetMapping("/{tplSeq}")
    public TemplateDto detail(@PathVariable Long tplSeq) {
        return templateService.getTplDetail(tplSeq);
    }
}
