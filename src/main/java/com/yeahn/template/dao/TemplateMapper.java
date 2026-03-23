package com.yeahn.template.dao;

import com.yeahn.template.dto.TemplateDto;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TemplateMapper {
    @Autowired
    private SqlSessionTemplate sqlSession;

    public List<TemplateDto> getTplList(TemplateDto dto) {
        return sqlSession.selectList("ExerciseTemplateMapper.selectTplList", dto);
    }
}
