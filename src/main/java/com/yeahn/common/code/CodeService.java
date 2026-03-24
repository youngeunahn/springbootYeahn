package com.yeahn.common.code;

import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CodeService {
    @Autowired
    private final CodeMapper codeMapper;

    public List<CodeDto> getCodeList(CodeDto dto, @Nullable String selectedTypeCode) {
        List<CodeDto> typeCodeList = codeMapper.getCodeList(dto);

        if (typeCodeList.isEmpty()) { return typeCodeList; }

        boolean selectedFound = false;

        for (CodeDto typeCodeDto : typeCodeList) {
            if (selectedTypeCode != null && typeCodeDto.getTypeCode().equals(selectedTypeCode)) {
                typeCodeDto.setChecked(true);
                selectedFound = true;
            } else {
                typeCodeDto.setChecked(false);
            }
        }

        if (!selectedFound) {
            for (CodeDto typeCodeDto : typeCodeList) {
                if (typeCodeDto.getSortOrder() == 1) {
                    typeCodeDto.setChecked(true);
                    break;
                }
            }
        }

        return typeCodeList;
    }
}
