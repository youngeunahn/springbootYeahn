package com.yeahn.common.code;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CodeController {

    private final CodeService codeService;

    @GetMapping("/api/codes/{typeClass}")
    @ResponseBody
    public List<CodeDto> getCodes(@PathVariable String typeClass) {
        return codeService.getCodeList(new CodeDto(typeClass), null);
    }
}
