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

    public void updateExercise(TemplateDto exerDto) {
        sqlSession.update("ExerciseTemplateMapper.updateExercise", exerDto);
    }

    public void deleteExerciseBySeq(Long tplAttrSeq) {
        sqlSession.update("ExerciseTemplateMapper.deleteExerciseBySeq", tplAttrSeq);
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

    public void updateTemplate(TemplateDto tplDto) {
        sqlSession.update("ExerciseTemplateMapper.updateTemplate", tplDto);
    }

    public void deleteTemplate(TemplateDto tplDto) {
        sqlSession.update("ExerciseTemplateMapper.deleteTemplate", tplDto);
    }

    public void deleteRelationByTplSeq(Long tplSeq) {
        sqlSession.delete("ExerciseTemplateMapper.deleteRelationByTplSeq", tplSeq);
    }

    public void deleteExerciseByTplSeq(TemplateDto tplDto) {
        sqlSession.update("ExerciseTemplateMapper.deleteExerciseByTplSeq", tplDto);
    }
}
