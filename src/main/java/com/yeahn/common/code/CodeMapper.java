package com.yeahn.common.code;

import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CodeMapper {
    @Autowired
    private SqlSessionTemplate sqlSession;

    public List<CodeDto> getCodeList(CodeDto codeDto) {
        return sqlSession.selectList("CommonMapper.selectCodeList", codeDto);
    }
}
