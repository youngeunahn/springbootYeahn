package com.yeahn.template.dao;

import com.yeahn.template.dto.TemplateDto;
import com.yeahn.template.dto.TemplateSearchDto;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class TemplateMapper {
    @Autowired
    private SqlSessionTemplate sqlSession;

    public List<TemplateDto> getTplList(TemplateDto dto) {
        return sqlSession.selectList("ExerciseTemplateMapper.getTplList", dto);
    }

    public List<TemplateDto> searchTplList(TemplateSearchDto dto) {
        return sqlSession.selectList("ExerciseTemplateMapper.searchTplList", dto);
    }

    public TemplateDto getTplDetail(Long tplSeq) {
        return sqlSession.selectOne("ExerciseTemplateMapper.getTplDetail", tplSeq);
    }

    public List<TemplateDto> getExerList(Long tplSeq) {
        return sqlSession.selectList("ExerciseTemplateMapper.getExerList", tplSeq);
    }

    public void insertTemplate(TemplateDto tplDto) {
        sqlSession.insert("ExerciseTemplateMapper.insertTemplate", tplDto);
    }

    public void insertExercise(TemplateDto exerDto) {
        sqlSession.insert("ExerciseTemplateMapper.insertExercise", exerDto);
    }

    public void insertRelation(Long tplSeq, Long tplAttrSeq) {
        Map<String, Object> param = new HashMap<>();
        param.put("tplSeq", tplSeq);
        param.put("tplAttrSeq", tplAttrSeq);

        sqlSession.insert("ExerciseTemplateMapper.insertRelation", param);
    }

    public void updateExerciseOrders(List<TemplateDto> list) {
        sqlSession.update("ExerciseTemplateMapper.updateExerciseOrders", list);
    }
}
