package com.yeahn.common.code;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CodeService {
    @Autowired
    private final CodeMapper codeMapper;

    public List<CodeDto> getCodeList(CodeDto dto) {
        List<CodeDto> typeCodeList = codeMapper.getCodeList(dto);
        for (CodeDto typeCodeDto : typeCodeList) {
            if (typeCodeDto.getSortOrder() == 1) {
                typeCodeDto.setFirst(true);
            }
        }
        return typeCodeList;
    }
}
