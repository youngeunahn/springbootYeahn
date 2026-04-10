package com.yeahn.plan.service;

import com.yeahn.common.CommonUtils;
import com.yeahn.plan.dao.PlanMapper;
import com.yeahn.plan.dto.PlanDetailVo;
import com.yeahn.plan.dto.PlanVo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanMapper planMapper;

    /**
     * 운동 계획 목록 조회
     */
    public List<PlanVo> getPlanList(PlanVo planVo) {
        return planMapper.getPlanList(planVo);
    }

    /**
     * 운동 계획 상세 조회 (마스터 + 상세 목록)
     */
    public PlanVo getPlanDetail(Integer planSeq) {
        PlanVo plan = planMapper.getPlan(planSeq);
        if (plan != null) {
            plan.setDetails(planMapper.getPlanDetails(planSeq));
        }
        return plan;
    }

    /**
     * 운동 계획 저장 (신규/수정 통합 처리)
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer savePlan(PlanVo planVo, HttpServletRequest request) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        String ip = CommonUtils.getIP(request);

        if (planVo.getPlanSeq() == null) {
            // 신규 저장
            planVo.setUserId(userId);
            planVo.setInsIp(ip);
            planVo.setInsUserId(userId);
            planMapper.insertPlan(planVo);
        } else {
            // 기존 마스터 수정
            planVo.setUpdIp(ip);
            planVo.setUpdUserId(userId);
            planMapper.updatePlan(planVo);

            // 기존 상세 목록 Soft Delete 처리
            planMapper.deletePlanDetailsByPlanSeq(planVo.getPlanSeq());
        }

        // 상세 목록 저장
        if (planVo.getDetails() != null && !planVo.getDetails().isEmpty()) {
            for (int i = 0; i < planVo.getDetails().size(); i++) {
                PlanDetailVo detail = planVo.getDetails().get(i);
                detail.setPlanSeq(planVo.getPlanSeq());
                detail.setPlanSortOrder(i + 1); // 리스트 순서대로 정렬 순서 부여
                
                if (detail.getPlanDetailSeq() != null) {
                    // 기존 기록 수정 (DEL_YN = 'N' 처리는 Mapper에서 수행)
                    detail.setUpdIp(ip);
                    detail.setUpdUserId(userId);
                    planMapper.updatePlanDetail(detail);
                } else {
                    // 신규 기록 저장
                    detail.setInsIp(ip);
                    detail.setInsUserId(userId);
                    planMapper.insertPlanDetail(detail);
                }
            }
        }

        return planVo.getPlanSeq();
    }

    /**
     * 운동 계획 삭제 (Soft Delete)
     */
    @Transactional(rollbackFor = Exception.class)
    public void deletePlan(Integer planSeq, HttpServletRequest request) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        String ip = CommonUtils.getIP(request);

        PlanVo planVo = new PlanVo();
        planVo.setPlanSeq(planSeq);
        planVo.setUpdIp(ip);
        planVo.setUpdUserId(userId);

        // 마스터 삭제
        planMapper.deletePlan(planVo);
        // 상세 삭제
        planMapper.deletePlanDetailsByPlanSeq(planSeq);
    }
}
