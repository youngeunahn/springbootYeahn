package com.yeahn.plan.controller;

import com.yeahn.common.dto.ResponseDto;
import com.yeahn.plan.dto.PlanVo;
import com.yeahn.plan.service.PlanService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/plans")
public class UserPlanApiController {

    private final PlanService planService;

    @PostMapping
    public ResponseDto<Long> savePlan(@RequestBody PlanVo planVo, HttpServletRequest request) {
        Long planSeq = planService.savePlan(planVo, request);
        return ResponseDto.success("운동계획이 저장되었습니다.", planSeq);
    }
}
