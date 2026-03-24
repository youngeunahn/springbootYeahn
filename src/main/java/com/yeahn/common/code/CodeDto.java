package com.yeahn.common.code;

import lombok.Data;

@Data
public class CodeDto {
    private String typeClass;
    private String typeCode;
    private String codeDesc;
    private Integer sortOrder;
    private String ref1;
    private boolean isChecked;

    public CodeDto(String typeClass) {
        this.typeClass = typeClass;
    }

    public CodeDto(String typeClass, String ref1) {
        this.typeClass = typeClass;
        this.ref1 = ref1;
    }

    public CodeDto(String typeCode, String codeDesc, Integer sortOrder) {
        this.typeCode = typeCode;
        this.codeDesc = codeDesc;
        this.sortOrder = sortOrder;
    }
}
