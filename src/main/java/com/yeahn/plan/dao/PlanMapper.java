package com.yeahn.plan.dao;

import com.yeahn.plan.dto.PlanDetailVo;
import com.yeahn.plan.dto.PlanVo;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface PlanMapper {
    // 운동 계획 목록 조회
    List<PlanVo> getPlanList(PlanVo planVo);

    // 운동 계획 상세 조회 (Master)
    PlanVo getPlan(Long planSeq);

    // 운동 계획 상세 목록 조회 (Detail)
    List<PlanDetailVo> getPlanDetails(Long planSeq);

    // 운동 계획 마스터 저장
    int insertPlan(PlanVo planVo);

    // 운동 계획 마스터 수정
    int updatePlan(PlanVo planVo);

    // 운동 계획 마스터 삭제 (Soft Delete)
    int deletePlan(PlanVo planVo);

    // 운동 계획 상세 저장
    int insertPlanDetail(PlanDetailVo detailVo);

    // 운동 계획 상세 수정
    int updatePlanDetail(PlanDetailVo detailVo);

    // 특정 계획의 상세 목록 삭제 (물리 삭제 또는 Soft Delete)
    // 일반적으로 대량 수정 시 상세를 전체 삭제 후 재등록하는 방식이 편리합니다.
    int deletePlanDetailsByPlanSeq(Long planSeq);
}
