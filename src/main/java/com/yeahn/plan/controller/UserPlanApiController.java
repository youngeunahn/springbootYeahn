package com.yeahn.plan.controller;

import com.yeahn.common.dto.ResponseDto;
import com.yeahn.plan.dto.PlanVo;
import com.yeahn.plan.service.PlanService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/plans")
public class UserPlanApiController {

    private final PlanService planService;

    @GetMapping
    public ResponseDto<List<PlanVo>> listPlans(@ModelAttribute PlanVo planVo, Principal principal) {
        planVo.setUserId(principal.getName());
        return ResponseDto.success(planService.getPlanListWithDetails(planVo));
    }

    @PostMapping
    public ResponseDto<Long> savePlan(@RequestBody PlanVo planVo, HttpServletRequest request) {
        Long planSeq = planService.savePlan(planVo, request);
        return ResponseDto.success("운동계획이 저장되었습니다.", planSeq);
    }
}
