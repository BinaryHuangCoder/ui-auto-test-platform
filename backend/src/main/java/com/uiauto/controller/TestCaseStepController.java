package com.uiauto.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.uiauto.entity.TestCaseStep;
import com.uiauto.model.Result;
import com.uiauto.service.TestCaseStepService;
import com.uiauto.util.ExcelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@RestController
@RequestMapping("/api/case-step")
public class TestCaseStepController {
    
    @Autowired
    private TestCaseStepService testCaseStepService;
    
    @Autowired
    private ExcelUtil excelUtil;
    
    /** 获取用例的所有步骤 */
    @GetMapping("/list/{caseId}")
    public Result<Page<TestCaseStep>> list(@PathVariable Long caseId, @RequestParam(defaultValue="1") Integer pageNum, @RequestParam(defaultValue="10") Integer pageSize) {
        Page<TestCaseStep> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<TestCaseStep> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TestCaseStep::getCaseId, caseId).orderByAsc(TestCaseStep::getStepNo);
        return Result.success(testCaseStepService.page(page, wrapper));
    }
    
    /** 获取步骤详情 */
    @GetMapping("/{id}")
    public Result<TestCaseStep> getById(@PathVariable Long id) {
        TestCaseStep step = testCaseStepService.getById(id);
        return step != null ? Result.success(step) : Result.error("步骤不存在");
    }
    
    /** 新增步骤 */
    @PostMapping
    public Result<TestCaseStep> add(@RequestBody TestCaseStep testCaseStep) {
        try {
            LambdaQueryWrapper<TestCaseStep> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TestCaseStep::getCaseId, testCaseStep.getCaseId())
                   .orderByDesc(TestCaseStep::getStepNo)
                   .last("LIMIT 1");
            TestCaseStep maxStep = testCaseStepService.getOne(wrapper);
            int nextNo = maxStep == null ? 1 : maxStep.getStepNo() + 1;
            testCaseStep.setStepNo(nextNo);
            
            if (testCaseStep.getStatus() == null) testCaseStep.setStatus(1);
            if (testCaseStep.getScreenshot() == null) testCaseStep.setScreenshot(1);
            testCaseStep.setCreateTime(LocalDateTime.now());
            testCaseStep.setUpdateTime(LocalDateTime.now());
            
            testCaseStepService.save(testCaseStep);
            return Result.success(testCaseStep);
        } catch (Exception e) {
            return Result.error("新增步骤失败: " + e.getMessage());
        }
    }
    
    /** 更新步骤 */
    @PutMapping
    public Result<TestCaseStep> update(@RequestBody TestCaseStep testCaseStep) {
        try {
            testCaseStep.setUpdateTime(LocalDateTime.now());
            testCaseStepService.updateById(testCaseStep);
            return Result.success(testCaseStep);
        } catch (Exception e) {
            return Result.error("更新步骤失败: " + e.getMessage());
        }
    }
    
    /** 删除步骤 */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        testCaseStepService.removeById(id);
        return Result.success("删除成功");
    }
    
    /** 批量删除步骤 */
    @DeleteMapping("/batch")
    public Result<String> batchDelete(@RequestBody List<Long> ids) {
        testCaseStepService.removeByIds(ids);
        return Result.success("批量删除成功");
    }
    
    /** 导出步骤到 Excel - 直接返回文件 */
    @GetMapping("/export/{caseId}")
    public ResponseEntity<byte[]> export(@PathVariable Long caseId) {
        try {
            LambdaQueryWrapper<TestCaseStep> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TestCaseStep::getCaseId, caseId).orderByAsc(TestCaseStep::getStepNo);
            List<TestCaseStep> steps = testCaseStepService.list(wrapper);
            
            ByteArrayOutputStream out = excelUtil.exportTestCaseSteps(steps);
            byte[] bytes = out.toByteArray();
            
            String filename = "用例步骤_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xlsx";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            
            return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /** 导入步骤从 Excel */
    @PostMapping("/import/{caseId}")
    public Result<String> importSteps(@PathVariable Long caseId, @RequestParam("file") MultipartFile file) {
        try {
            java.io.ByteArrayInputStream inputStream = new java.io.ByteArrayInputStream(file.getBytes());
            List<TestCaseStep> steps = excelUtil.importTestCaseSteps(inputStream, caseId);
            
            for (TestCaseStep step : steps) {
                step.setCreateTime(LocalDateTime.now());
                step.setUpdateTime(LocalDateTime.now());
            }
            testCaseStepService.saveBatch(steps);
            return Result.success("导入成功，共 " + steps.size() + " 条步骤");
        } catch (Exception e) {
            return Result.error("导入失败: " + e.getMessage());
        }
    }
}
