package com.yeahn.template.dao;

import com.yeahn.template.dto.TemplateDto;
import com.yeahn.template.dto.TemplateSearchDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TemplateMapper {

    List<TemplateDto> getTplList(TemplateDto dto);

    List<TemplateDto> searchTplList(TemplateSearchDto dto);

    TemplateDto getTplDetail(Long tplSeq);

    List<TemplateDto> getExerList(Long tplSeq);

    int insertTemplate(TemplateDto tplDto);

    int insertExercise(TemplateDto exerDto);

    int updateExercise(TemplateDto exerDto);

    int deleteExerciseBySeq(Long tplAttrSeq);

    int insertRelation(@Param("tplSeq") Long tplSeq, @Param("tplAttrSeq") Long tplAttrSeq);

    int updateExerciseOrders(List<TemplateDto> list);

    int updateTemplate(TemplateDto tplDto);

    int deleteTemplate(TemplateDto tplDto);

    int deleteRelationByTplSeq(Long tplSeq);

    int deleteExerciseByTplSeq(TemplateDto tplDto);
}
