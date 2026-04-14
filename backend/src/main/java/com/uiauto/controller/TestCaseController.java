package com.uiauto.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.uiauto.entity.TestCase;
import com.uiauto.entity.System;
import com.uiauto.model.Result;
import com.uiauto.service.TestCaseService;
import com.uiauto.service.UserService;
import com.uiauto.service.SystemService;
import com.uiauto.entity.User;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/case")
public class TestCaseController {
    
    @Autowired
    private TestCaseService testCaseService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private SystemService systemService;
    
    @Autowired
    private ExcelUtil excelUtil;
    
    /** 生成唯一用例编号 */
    private String generateCaseNo() {
        String prefix = "TC" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuidPart = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return prefix + uuidPart;
    }
    
    /**
     * 分页查询用例列表
     * 
     * @param pageNum 页码，默认1
     * @param pageSize 每页条数，默认10
     * @param keyword 搜索关键字（用例名称/编号/设计者）
     * @return 分页的用例列表，包含设计者昵称
     */
    @GetMapping("/list")
    public Result<Page<TestCase>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        Page<TestCase> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<TestCase> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(TestCase::getName, keyword)
                   .or().like(TestCase::getCaseNo, keyword)
                   .or().like(TestCase::getDesigner, keyword);
        }
        wrapper.orderByDesc(TestCase::getCreateTime);
        Page<TestCase> result = testCaseService.page(page, wrapper);
        
        // 为每条用例填充设计者昵称和系统名称
        if (result.getRecords() != null && !result.getRecords().isEmpty()) {
            for (TestCase testCase : result.getRecords()) {
                // 填充设计者昵称
                if (testCase.getDesigner() != null && !testCase.getDesigner().isEmpty()) {
                    User user = userService.getOne(
                        new LambdaQueryWrapper<User>()
                            .eq(User::getUsername, testCase.getDesigner())
                    );
                    if (user != null && user.getNickname() != null) {
                        testCase.setDesignerNickname(user.getNickname());
                    } else {
                        testCase.setDesignerNickname(testCase.getDesigner());
                    }
                }
                // 填充系统名称
                if (testCase.getSystemId() != null) {
                    System system = systemService.getById(testCase.getSystemId());
                    if (system != null) {
                        testCase.setSystemName(system.getSystemName());
                    }
                }
            }
        }
        
        return Result.success(result);
    }
    
    /** 获取所有用例 */
    @GetMapping("/all")
    public Result<List<TestCase>> getAll() {
        LambdaQueryWrapper<TestCase> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(TestCase::getCreateTime);
        return Result.success(testCaseService.list(wrapper));
    }
    
    /** 获取用例详情 */
    @GetMapping("/{id}")
    public Result<TestCase> getById(@PathVariable Long id) {
        TestCase testCase = testCaseService.getById(id);
        if (testCase != null) {
            // 填充系统名称
            if (testCase.getSystemId() != null) {
                System system = systemService.getById(testCase.getSystemId());
                if (system != null) {
                    testCase.setSystemName(system.getSystemName());
                }
            }
        }
        return testCase != null ? Result.success(testCase) : Result.error("用例不存在");
    }
    
    /** 新增用例 */
    @PostMapping
    public Result<TestCase> add(@RequestBody TestCase testCase) {
        try {
            // 直接生成唯一编号，不使用接口默认方法
            if (testCase.getCaseNo() == null || testCase.getCaseNo().isEmpty()) {
                testCase.setCaseNo(generateCaseNo());
            }
            if (testCase.getStatus() == null) testCase.setStatus(1);
            if (testCase.getCaseType() == null) testCase.setCaseType("positive");
            testCase.setCreateTime(LocalDateTime.now());
            testCase.setUpdateTime(LocalDateTime.now());
            testCaseService.save(testCase);
            return Result.success(testCase);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("新增失败: " + e.getMessage());
        }
    }
    
    /** 更新用例 */
    @PutMapping
    public Result<TestCase> update(@RequestBody TestCase testCase) {
        try {
            testCase.setUpdateTime(LocalDateTime.now());
            testCaseService.updateById(testCase);
            return Result.success(testCase);
        } catch (Exception e) {
            return Result.error("更新失败: " + e.getMessage());
        }
    }
    
    /** 删除用例 */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        testCaseService.removeById(id);
        return Result.success("删除成功");
    }
    
    /** 批量删除用例 */
    @DeleteMapping("/batch")
    public Result<String> batchDelete(@RequestBody List<Long> ids) {
        testCaseService.removeByIds(ids);
        return Result.success("批量删除成功");
    }
    
    /** 导出用例到 Excel */
    @GetMapping("/export")
    public ResponseEntity<byte[]> export() {
        try {
            LambdaQueryWrapper<TestCase> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(TestCase::getCreateTime);
            List<TestCase> cases = testCaseService.list(wrapper);
            
            ByteArrayOutputStream out = excelUtil.exportTestCases(cases);
            byte[] bytes = out.toByteArray();
            
            String filename = "测试用例_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xlsx";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            
            return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /** 导入用例从 Excel */
    @PostMapping("/import")
    public Result<String> importCases(@RequestParam("file") MultipartFile file) {
        try {
            java.io.ByteArrayInputStream inputStream = new java.io.ByteArrayInputStream(file.getBytes());
            List<TestCase> cases = excelUtil.importTestCases(inputStream);
            
            for (TestCase tc : cases) {
                if (tc.getCaseNo() == null || tc.getCaseNo().isEmpty()) {
                    tc.setCaseNo(generateCaseNo());
                }
                if (tc.getStatus() == null) tc.setStatus(1);
                if (tc.getCaseType() == null) tc.setCaseType("positive");
                tc.setCreateTime(LocalDateTime.now());
                tc.setUpdateTime(LocalDateTime.now());
            }
            testCaseService.saveBatch(cases);
            return Result.success("导入成功，共 " + cases.size() + " 条用例");
        } catch (Exception e) {
            return Result.error("导入失败: " + e.getMessage());
        }
    }
}
