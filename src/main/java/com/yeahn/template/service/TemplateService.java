package com.yeahn.template.service;

import com.yeahn.template.dao.TemplateMapper;
import com.yeahn.template.dto.TemplateDto;
import com.yeahn.template.dto.TemplateSearchDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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

    @Transactional(rollbackFor = Exception.class)
    public Long createTemplate(TemplateDto request, HttpServletRequest req) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        String ip = getIP(req);

        TemplateDto tplDto = new TemplateDto();
        tplDto.setTplName(request.getTplName());
        tplDto.setTplPhase(request.getTplPhase());
        tplDto.setTplSortOrder(request.getTplSortOrder());
        tplDto.setUseYn("Y");
        tplDto.setDelYn("N");
        tplDto.setInsUserId(userId);
        tplDto.setInsIp(ip);

        templateMapper.insertTemplate(tplDto);
        Long tplSeq = tplDto.getTplSeq();

        if (request.getExercises() != null) {
            for (TemplateDto ex : request.getExercises()) {
                ex.setInsUserId(userId);
                ex.setInsIp(ip);
                templateMapper.insertExercise(ex);
                templateMapper.insertRelation(tplSeq, ex.getTplAttrSeq());
            }
        }

        return tplSeq;
    }
}
