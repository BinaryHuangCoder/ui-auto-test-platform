package com.uiauto.util;

import com.uiauto.entity.TestCase;
import com.uiauto.entity.TestCaseStep;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class ExcelUtil {
    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /** 导出测试用例到 Excel */
    public ByteArrayOutputStream exportTestCases(List<TestCase> cases) throws Exception {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("测试用例");
            
            // 表头 - 与用例列表一致
            Row headerRow = sheet.createRow(0);
            String[] headers = {"用例编号", "用例名称", "用例描述", "设计者", "用例性质", "状态", "创建时间"};
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // 数据行
            for (int i = 0; i < cases.size(); i++) {
                TestCase tc = cases.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(tc.getCaseNo() != null ? tc.getCaseNo() : "");
                row.createCell(1).setCellValue(tc.getName() != null ? tc.getName() : "");
                row.createCell(2).setCellValue(tc.getDescription() != null ? tc.getDescription() : "");
                row.createCell(3).setCellValue(tc.getDesigner() != null ? tc.getDesigner() : "");
                row.createCell(4).setCellValue("positive".equals(tc.getCaseType()) ? "正例" : "反例");
                row.createCell(5).setCellValue(tc.getStatus() != null && tc.getStatus() == 1 ? "启用" : "禁用");
                row.createCell(6).setCellValue(tc.getCreateTime() != null ? tc.getCreateTime().format(DATE_FORMAT) : "");
            }
            
            // 自动列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            return out;
        }
    }
    
    /** 从 Excel 导入测试用例 */
    public List<TestCase> importTestCases(ByteArrayInputStream inputStream) throws Exception {
        List<TestCase> cases = new ArrayList<>();
        
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                TestCase tc = new TestCase();
                tc.setCaseNo(getCellValue(row.getCell(0)));
                tc.setName(getCellValue(row.getCell(1)));
                tc.setDescription(getCellValue(row.getCell(2)));
                tc.setDesigner(getCellValue(row.getCell(3)));
                String caseType = getCellValue(row.getCell(4));
                tc.setCaseType("正例".equals(caseType) ? "positive" : "negative");
                String status = getCellValue(row.getCell(5));
                tc.setStatus("启用".equals(status) ? 1 : 0);
                
                if (tc.getName() != null && !tc.getName().isEmpty()) {
                    cases.add(tc);
                }
            }
        }
        return cases;
    }
    
    /** 导出用例步骤到 Excel */
    public ByteArrayOutputStream exportTestCaseSteps(List<TestCaseStep> steps) throws Exception {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("用例步骤");
            
            // 表头 - 与步骤列表一致
            Row headerRow = sheet.createRow(0);
            String[] headers = {"步骤序号", "步骤描述", "断言描述", "是否启用", "是否截图", "创建时间"};
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // 数据行
            for (int i = 0; i < steps.size(); i++) {
                TestCaseStep step = steps.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(step.getStepNo() != null ? step.getStepNo() : 0);
                row.createCell(1).setCellValue(step.getStepDescription() != null ? step.getStepDescription() : "");
                row.createCell(2).setCellValue(step.getAssertionDescription() != null ? step.getAssertionDescription() : "");
                row.createCell(3).setCellValue(step.getStatus() != null && step.getStatus() == 1 ? "启用" : "禁用");
                row.createCell(4).setCellValue(step.getScreenshot() != null && step.getScreenshot() == 1 ? "是" : "否");
                row.createCell(5).setCellValue(step.getCreateTime() != null ? step.getCreateTime().format(DATE_FORMAT) : "");
            }
            
            // 自动列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            return out;
        }
    }
    
    /** 从 Excel 导入用例步骤 */
    public List<TestCaseStep> importTestCaseSteps(ByteArrayInputStream inputStream, Long caseId) throws Exception {
        List<TestCaseStep> steps = new ArrayList<>();
        
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                TestCaseStep step = new TestCaseStep();
                step.setCaseId(caseId);
                step.setStepNo(i);
                step.setStepDescription(getCellValue(row.getCell(1)));
                step.setAssertionDescription(getCellValue(row.getCell(2)));
                String status = getCellValue(row.getCell(3));
                step.setStatus("启用".equals(status) ? 1 : 0);
                String screenshot = getCellValue(row.getCell(4));
                step.setScreenshot("是".equals(screenshot) ? 1 : 0);
                
                if (step.getStepDescription() != null && !step.getStepDescription().isEmpty()) {
                    steps.add(step);
                }
            }
        }
        return steps;
    }
    
    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC: return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN: return cell.getBooleanCellValue() ? "是" : "否";
            default: return "";
        }
    }
}
