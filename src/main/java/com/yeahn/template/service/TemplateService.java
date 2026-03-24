package com.yeahn.template.service;

import com.yeahn.template.dao.TemplateMapper;
import com.yeahn.template.dto.TemplateDto;
import com.yeahn.template.dto.TemplateSearchDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
