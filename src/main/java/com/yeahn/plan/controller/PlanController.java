package com.yeahn.plan.controller;

import com.yeahn.common.code.CodeDto;
import com.yeahn.common.code.CodeService;
import com.yeahn.plan.dto.PlanVo;
import com.yeahn.plan.service.PlanService;
import com.yeahn.template.dto.TemplateDto;
import com.yeahn.template.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/exercise/plan")
public class PlanController {

    private final CodeService codeService;
    private final TemplateService templateService;
    private final PlanService planService;

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

        // 운동 카테고리 코드 리스트 (Chest, Back, Legs 등)
        model.addAttribute("categoryCodes", codeService.getCodeList(new CodeDto("TPL_CATEGORY"), null));
        
        // 등록된 템플릿 리스트 조회
        TemplateDto tplDto = new TemplateDto();
        tplDto.setTplTypeCode(typeCode);
        model.addAttribute("tplList", templateService.getTplList(tplDto));
        
        // 실제 등록된 운동 계획 리스트 조회 (로그인 유저 본인 것만)
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        PlanVo planVo = new PlanVo();
        planVo.setUserId(userId);
        planVo.setPlanTypeCode(typeCode);
        model.addAttribute("planList", planService.getPlanList(planVo));

        return "exercise/plan/list";
    }

    // 운동 계획 상세 조회 (JSON)
    @GetMapping("/{planSeq}")
    @ResponseBody
    public PlanVo getPlanDetail(@PathVariable Long planSeq) {
        return planService.getPlanDetail(planSeq);
    }

    // 운동 계획 저장 (신규/수정)
    @PostMapping("/save")
    @ResponseBody
    public Long savePlan(@RequestBody PlanVo planVo, HttpServletRequest request) {
        return planService.savePlan(planVo, request);
    }

    // 운동 계획 삭제
    @PostMapping("/delete/{planSeq}")
    @ResponseBody
    public String deletePlan(@PathVariable Long planSeq, HttpServletRequest request) {
        planService.deletePlan(planSeq, request);
        return "SUCCESS";
    }

    // 운동 계획 검색 API (기간 + 이름 필터)
    @GetMapping("/search")
    @ResponseBody
    public List<PlanVo> searchPlanList(@ModelAttribute PlanVo planVo) {
        planVo.setUserId(SecurityContextHolder.getContext().getAuthentication().getName());
        return planService.getPlanList(planVo);
    }

    // 개별 운동 검색 API (라이브러리용)
    @GetMapping("/search-exercises")
    @ResponseBody
    public List<TemplateDto> searchExercises(@RequestParam String keyword, @RequestParam String typeCode) {
        TemplateDto searchDto = new TemplateDto();
        searchDto.setTplTypeCode(typeCode);
        searchDto.setTplExerName(keyword);
        return templateService.getTplList(searchDto);
    }
}
