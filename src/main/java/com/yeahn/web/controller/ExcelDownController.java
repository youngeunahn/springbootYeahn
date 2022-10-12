package com.yeahn.web.controller;

import com.yeahn.common.ExcelUtils;
import com.yeahn.model.ExcelTest;
import com.yeahn.web.service.YeTableService;
import org.apache.xmlbeans.impl.common.IOUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@RequestMapping("/excel")
@Controller
public class ExcelDownController {
    @Autowired
    private YeTableService yeTableService;

    @RequestMapping(value="/download", method= RequestMethod.GET)
    public void excelDownload(HttpServletResponse res) throws IOException {
        res.setHeader("Content-Disposition", "attachment;filename=test.xls");
        res.setContentType("application/octet-stream");

        // 엑셀 파일 헤더
        List<String> header = Arrays.asList("NO", "제목", "작성일", "작성자");
        // 엑셀 파일로 만들 리스트
        List excelTestList = new ArrayList<>();
        for (int j = 0; j<yeTableService.getYeahnTableList().size(); j++) {
            excelTestList.add(yeTableService.getYeahnTableList().get(j));
        }

        ByteArrayInputStream stream = ExcelUtils.createListToExcel(header, excelTestList);
        IOUtil.copyCompletely(stream, res.getOutputStream());
    }
}
