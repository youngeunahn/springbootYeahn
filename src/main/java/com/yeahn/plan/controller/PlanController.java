package com.yeahn.plan.controller;

import com.yeahn.common.code.CodeDto;
import com.yeahn.common.code.CodeService;
import com.yeahn.template.dto.TemplateDto;
import com.yeahn.template.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/exercise/plan")
public class PlanController {

    private final CodeService codeService;
    private final TemplateService templateService;

    @GetMapping
    public String list(
            @RequestParam String menuCode,
            @RequestParam(required = false, defaultValue = "SWIM") String typeCode,
            Model model) {

        model.addAttribute("menuCode", menuCode);
        
        // 유형 코드 리스트 (GYM, SWIM 등)
        model.addAttribute("typeCode", codeService.getCodeList(new CodeDto("TPL_TYPE_CODE"), typeCode));

        // 운동 단계 코드 리스트 (Warm-up, Main, Cool-down 등)
        model.addAttribute("phaseCodes", codeService.getCodeList(new CodeDto("TPL_PHASE"), null));
        
        // 등록된 템플릿 리스트 조회
        TemplateDto tplDto = new TemplateDto();
        tplDto.setTplTypeCode(typeCode);
        model.addAttribute("tplList", templateService.getTplList(tplDto));
        
        // 빈 리스트 전달 (추후 DB 연동 시 교체)
        model.addAttribute("planList", new ArrayList<>());

        return "exercise/plan/list";
    }

    // 개별 운동 검색 API (라이브러리용)
    @GetMapping("/search-exercises")
    @ResponseBody
    public List<TemplateDto> searchExercises(@RequestParam String keyword, @RequestParam String typeCode) {
        // 실제로는 DB에서 tplExerName LIKE %keyword% 로 조회해야 합니다.
        // 현재는 구현 단계이므로 빈 리스트 또는 간단한 로직만 연결합니다.
        TemplateDto searchDto = new TemplateDto();
        searchDto.setTplTypeCode(typeCode);
        searchDto.setTplExerName(keyword);
        // 임시로 전체 리스트 반환 로직 연결 (실제 Mapper 작업 시 쿼리 변경 필요)
        return templateService.getTplList(searchDto);
    }
}
