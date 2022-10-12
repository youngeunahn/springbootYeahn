package com.yeahn.common;

import com.yeahn.model.ExcelTest;
import com.yeahn.model.YeahnTable;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class ExcelUtils {

    public static ByteArrayInputStream createListToExcel(List<String> excelHeader, List<YeahnTable> excelTestList) {
        try (Workbook workbook = new HSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("excelDownloadTest");
            Row row;
            Cell cell;
            int rowNo = 0;

            int headerSize = excelHeader.size();

            //테이블 헤더 스타일 설정
            CellStyle headStyle = workbook.createCellStyle();
            //경계선 설정
            headStyle.setBorderTop(BorderStyle.THIN);
            headStyle.setBorderBottom(BorderStyle.THIN);
            headStyle.setBorderLeft(BorderStyle.THIN);
            headStyle.setBorderRight(BorderStyle.THIN);
            //색상
            headStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.YELLOW.getIndex());
            headStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            //헤더 가운데 정렬
            headStyle.setAlignment(HorizontalAlignment.CENTER);

            //헤더 생성
            row = sheet.createRow(rowNo++);
            for (int i = 0; i<headerSize; i++) {
                cell = row.createCell(i);
                cell.setCellStyle(headStyle);
                cell.setCellValue(excelHeader.get(i));
            }

            //내용 생성
            for (int j = 0; j<excelTestList.size(); j++) {
                Row dataRow = sheet.createRow(j + 1);
                dataRow.createCell(0).setCellValue(excelTestList.get(j).getNO());
                dataRow.createCell(1).setCellValue(excelTestList.get(j).getTITLE());
                dataRow.createCell(2).setCellValue(excelTestList.get(j).getREG_DATE());
                dataRow.createCell(3).setCellValue(excelTestList.get(j).getREG_ID());
            }

            //Making size of column auto resize to fit with data
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            sheet.autoSizeColumn(2);
            sheet.autoSizeColumn(3);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
