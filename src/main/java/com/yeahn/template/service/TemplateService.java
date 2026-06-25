package com.yeahn.template.service;

import com.yeahn.template.dao.TemplateMapper;
import com.yeahn.template.dto.TemplateDto;
import com.yeahn.template.dto.TemplateSearchDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.yeahn.common.CommonUtils.getIP;

@Service
@RequiredArgsConstructor
public class TemplateService {

    @Autowired
    private final TemplateMapper templateMapper;
    public List<TemplateDto> getTplList(TemplateDto dto) {
        return templateMapper.getTplList(dto);
    }

    public List<TemplateDto> searchTplList(TemplateSearchDto dto) {
        return templateMapper.searchTplList(dto);
    }

    public List<TemplateDto> searchExerciseList(TemplateDto dto) {
        return templateMapper.searchExerciseList(dto);
    }

    public TemplateDto getTplDetail(Long tplSeq) {
        TemplateDto tpl = templateMapper.getTplDetail(tplSeq);
        if (tpl != null) {
            tpl.setExercises(templateMapper.getExerList(tplSeq));
        }
        return tpl;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createTemplate(TemplateDto request, HttpServletRequest req) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        String ip = getIP(req);

        TemplateDto tplDto = new TemplateDto();
        tplDto.setTplName(request.getTplName());
        tplDto.setTplPhase(request.getTplPhase());
        tplDto.setTplTypeCode(request.getTplTypeCode());
        tplDto.setTplSortOrder(request.getTplSortOrder());
        tplDto.setUseYn("Y");
        tplDto.setDelYn("N");
        tplDto.setInsUserId(userId);
        tplDto.setInsIp(ip);

        templateMapper.insertTemplate(tplDto);
        Long tplSeq = tplDto.getTplSeq();

        if (request.getExercises() != null) {
            for (TemplateDto ex : request.getExercises()) {
                if (tplDto.getTplTypeCode() != null) {
                    ex.setTplTypeCode(tplDto.getTplTypeCode());
                }
                ex.setInsUserId(userId);
                ex.setInsIp(ip);
                templateMapper.insertExercise(ex);
                templateMapper.insertRelation(tplSeq, ex.getTplAttrSeq());
            }
        }

        return tplSeq;
    }

    @Transactional(rollbackFor = Exception.class)
    public void reorderExercises(TemplateDto request, HttpServletRequest req) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        String ip = getIP(req);

        if (request.getExercises() != null) {
            for (TemplateDto ex : request.getExercises()) {
                ex.setUpdUserId(userId);
                ex.setUpdIp(ip);
            }
            templateMapper.updateExerciseOrders(request.getExercises());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateTemplate(TemplateDto request, HttpServletRequest req) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        String ip = getIP(req);

        request.setUpdUserId(userId);
        request.setUpdIp(ip);

        // 1. 마스터 정보 수정
        templateMapper.updateTemplate(request);

        // 2. 기존 상세 운동 목록 조회
        List<TemplateDto> currentExers = templateMapper.getExerList(request.getTplSeq());
        List<Long> currentIds = currentExers.stream()
                .map(TemplateDto::getTplAttrSeq)
                .collect(Collectors.toList());

        // 3. 전달받은 리스트 처리
        List<Long> keptIds = new ArrayList<>();
        if (request.getExercises() != null) {
            for (TemplateDto ex : request.getExercises()) {
                ex.setTplTypeCode(request.getTplTypeCode());
                ex.setInsUserId(userId);
                ex.setInsIp(ip);
                ex.setUpdUserId(userId);
                ex.setUpdIp(ip);

                if (ex.getTplAttrSeq() != null && currentIds.contains(ex.getTplAttrSeq())) {
                    templateMapper.updateExercise(ex);
                    keptIds.add(ex.getTplAttrSeq());
                } else {
                    templateMapper.insertExercise(ex);
                    templateMapper.insertRelation(request.getTplSeq(), ex.getTplAttrSeq());
                }
            }
        }

        // 4. 제외된 항목 삭제 처리
        for (Long oldId : currentIds) {
            if (!keptIds.contains(oldId)) {
                templateMapper.deleteExerciseBySeq(oldId);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteTemplate(Long tplSeq, HttpServletRequest req) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        String ip = getIP(req);

        TemplateDto tplDto = new TemplateDto();
        tplDto.setTplSeq(tplSeq);
        tplDto.setUpdUserId(userId);
        tplDto.setUpdIp(ip);

        // 1. 마스터 정보 삭제 처리 (Soft Delete)
        templateMapper.deleteTemplate(tplDto);

        // 2. 상세 운동 삭제 처리 (Soft Delete) - 관계 데이터가 있어야 JOIN 가능하므로 먼저 실행
        templateMapper.deleteExerciseByTplSeq(tplDto);

        // 3. 관계 정보 삭제 처리 (Physical Delete)
        templateMapper.deleteRelationByTplSeq(tplSeq);
    }
}
