package com.yeahn.common.code;

import com.yeahn.common.dto.ResponseDto;
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

    @GetMapping("/api/user/codes")
    public ResponseDto<List<CodeOptionDto>> getUserCodes(
            @RequestParam String groupCode,
            @RequestParam(required = false) String typeCode) {
        return ResponseDto.success("조회되었습니다.", codeService.getUserCodeOptions(groupCode, typeCode));
    }
}
