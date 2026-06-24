package com.yeahn.common.code;

import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    public List<CodeOptionDto> getUserCodeOptions(String groupCode) {
        return getUserCodeOptions(groupCode, null);
    }

    public List<CodeOptionDto> getUserCodeOptions(String groupCode, @Nullable String typeCode) {
        if (groupCode == null || groupCode.isBlank()) {
            throw new IllegalArgumentException("groupCode를 입력해 주세요.");
        }

        List<CodeOptionDto> options = new ArrayList<>();
        for (String typeClass : resolveUserCodeTypeClasses(groupCode)) {
            CodeDto search = new CodeDto(typeClass);
            if ("PLAN_CATEGORY".equals(groupCode) && typeCode != null && !typeCode.isBlank()) {
                search.setRef1(typeCode);
            }
            List<CodeDto> codes = codeMapper.getCodeList(search);
            for (CodeDto code : codes) {
                options.add(new CodeOptionDto(code.getTypeCode(), code.getCodeDesc()));
            }
            if (!options.isEmpty()) {
                return options;
            }
        }
        return options;
    }

    private List<String> resolveUserCodeTypeClasses(String groupCode) {
        return switch (groupCode) {
            case "PLAN_PHASE" -> List.of("TPL_PHASE");
            case "PLAN_CATEGORY" -> List.of("TPL_CATEGORY");
            case "PLAN_TYPE" -> List.of("TPL_TYPE_CODE");
            default -> throw new IllegalArgumentException("지원하지 않는 코드 그룹입니다.");
        };
    }
}
