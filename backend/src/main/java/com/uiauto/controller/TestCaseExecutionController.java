
package com.uiauto.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.uiauto.entity.TestCaseExecution;
import com.uiauto.entity.TestStepExecution;
import com.uiauto.entity.TestCase;
import com.uiauto.entity.TestCaseStep;
import com.uiauto.model.Result;
import com.uiauto.service.TestCaseExecutionService;
import com.uiauto.service.TestCaseService;
import com.uiauto.service.TestCaseStepService;
import com.uiauto.service.TestStepExecutionService;
import com.uiauto.service.UserService;
import com.uiauto.service.ModelService;
import com.uiauto.service.ModelScenarioService;
import com.uiauto.entity.User;
import com.uiauto.entity.Model;
import com.uiauto.entity.ModelScenario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * 测试用例执行控制器
 * 
 * @author system
 * @date 2026-04-09
 */
@RestController
@RequestMapping("/api/execution")
public class TestCaseExecutionController {
    
    @Autowired
    private TestCaseExecutionService executionService;
    
    @Autowired
    private TestStepExecutionService stepExecutionService;
    
    @Autowired
    private TestCaseService testCaseService;
    
    @Autowired
    private TestCaseStepService testCaseStepService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ModelService modelService;
    
    @Autowired
    private ModelScenarioService modelScenarioService;
    
    @Value("${executor.node-path:/usr/local/bin/node}")
    private String nodePath;
    
    @Value("${executor.scripts-dir:/opt/ui-auto-test-platform/scripts}")
    private String scriptsDir;
    
    @Value("${executor.executor-script:executor.js}")
    private String executorScript;
    
    @Value("${executor.playwright-browsers-path:/root/.cache/ms-playwright}")
    private String playwrightBrowsersPath;
    
    /**
     * 执行测试用例（批量执行模式）
     * 
     * @param caseId 测试用例ID
     * @param taskExecutionId 任务执行记录ID（可选）
     * @param saveTokenMode 是否使用节约token模式（可选，默认false）
     * @return 执行结果，包含执行ID
     */
    @PostMapping("/run/{caseId}")
    public Result<String> executeTestCase(
            @PathVariable Long caseId,
            @RequestParam(required = false) Long taskExecutionId,
            @RequestParam(required = false, defaultValue = "false") Boolean saveTokenMode) {
        TestCase testCase = testCaseService.getById(caseId);
        if (testCase == null) {
            return Result.error("用例不存在");
        }
        
        List<TestCaseStep> steps = testCaseStepService.list(
            new LambdaQueryWrapper<TestCaseStep>()
                .eq(TestCaseStep::getCaseId, caseId)
                .orderByAsc(TestCaseStep::getStepNo)
        );
        
        if (steps.isEmpty()) {
            return Result.error("该用例没有配置步骤");
        }

        // 异步执行测试用例
        CompletableFuture.runAsync(() -> {
            executionService.executeTestCase(caseId, "admin", saveTokenMode ? "save" : "standard", taskExecutionId);
        });
        
        return Result.success("用例开始执行");
    }
    
    /**
     * 解析单个步骤的结果并更新步骤执行状态
     * 
     * @param stepJson 步骤结果JSON字符串
     * @param executionId 执行记录ID
     * @param steps 测试步骤列表
     * @return 该步骤的AI token消耗
     */
    private long parseAndUpdateSingleStepResult(String stepJson, Long executionId, List<TestCaseStep> steps) {
        long stepAiTokenUsed = 0;
        
        try {
            // 提取stepNumber
            String stepNumberStr = extractJsonValue(stepJson, "stepNumber");
            if (stepNumberStr == null || stepNumberStr.isEmpty()) {
                System.err.println("[WARN] 无法找到stepNumber");
                return 0;
            }
            
            int stepNo = Integer.parseInt(stepNumberStr);
            
            // 找到对应的TestCaseStep
            TestCaseStep step = null;
            for (TestCaseStep s : steps) {
                if (s.getStepNo() == stepNo) {
                    step = s;
                    break;
                }
            }
            
            if (step == null) {
                System.err.println("[WARN] 未找到步骤" + stepNo + "的TestCaseStep");
                return 0;
            }
            
            // 查询该步骤的执行记录
            TestStepExecution stepExecution = stepExecutionService.getOne(
                new LambdaQueryWrapper<TestStepExecution>()
                    .eq(TestStepExecution::getExecutionId, executionId)
                    .eq(TestStepExecution::getStepNo, stepNo)
            );
            
            if (stepExecution == null) {
                System.err.println("[WARN] 未找到步骤" + stepNo + "的执行记录");
                return 0;
            }
            
            // 解析执行状态
            String executionStatus = extractJsonValue(stepJson, "executionStatus");
            String assertionStatus = extractJsonValue(stepJson, "assertionStatus");
            String message = extractJsonValue(stepJson, "message");
            // 优先使用字符串格式的executionTimeStr，如果不存在则回退到executionTime
            String executionTime = extractJsonValue(stepJson, "executionTimeStr");
            if (executionTime == null || executionTime.isEmpty()) {
                executionTime = extractJsonValue(stepJson, "executionTime");
            }
            
            // 设置状态和失败描述
            if ("成功".equals(executionStatus)) {
                stepExecution.setStatus("success");
                stepExecution.setErrorMessage("无");
            } else {
                stepExecution.setStatus("failed");
                stepExecution.setErrorMessage(message != null ? message : "执行失败");
            }
            
            // 设置断言状态
            if ("通过".equals(assertionStatus) || "成功".equals(assertionStatus)) {
                stepExecution.setAssertionStatus("success");
                stepExecution.setAiResult("✅ " + message);
            } else if ("失败".equals(assertionStatus)) {
                stepExecution.setAssertionStatus("failed");
                stepExecution.setAiResult("❌ " + message);
            } else if ("无".equals(assertionStatus)) {
                stepExecution.setAssertionStatus("none");
                stepExecution.setAiResult("- 无断言要求");
            } else {
                stepExecution.setAssertionStatus("none");
            }
            
            // 设置开始时间
            String stepStartTime = extractJsonValue(stepJson, "startTime");
            System.err.println("[DEBUG] 步骤" + stepNo + " - startTime值: " + stepStartTime);
            if (stepStartTime != null && !stepStartTime.isEmpty()) {
                try {
                    // 先尝试解析为时间戳（毫秒）
                    long timestamp = Long.parseLong(stepStartTime);
                    stepExecution.setStartTime(LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochMilli(timestamp),
                        java.time.ZoneId.systemDefault()
                    ));
                } catch (Exception e) {
                    // 如果不是时间戳，尝试解析为日期字符串
                    try {
                        stepExecution.setStartTime(LocalDateTime.parse(stepStartTime));
                    } catch (Exception ex) {
                        stepExecution.setStartTime(LocalDateTime.now());
                    }
                }
            } else {
                stepExecution.setStartTime(LocalDateTime.now());
            }
            
            // 设置耗时
            System.err.println("[DEBUG] 步骤" + stepNo + " - executionTime值: " + executionTime);
            try {
                if (executionTime != null && !executionTime.isEmpty()) {
                    stepExecution.setDuration(Long.parseLong(executionTime));
                } else {
                    stepExecution.setDuration(0L);
                }
            } catch (Exception e) {
                stepExecution.setDuration(0L);
                System.err.println("[WARN] 解析executionTime失败: " + e.getMessage());
            }
            
            // 设置截图 - 读取screenshotBase64（file:开头的路径），去掉file:前缀，并转换为URL路径
            String screenshotBase64 = extractJsonValue(stepJson, "screenshotBase64");
            System.err.println("[DEBUG] 步骤" + stepNo + " - screenshotBase64值: " + screenshotBase64);
            if (screenshotBase64 != null && screenshotBase64.startsWith("file:")) {
                String fullPath = screenshotBase64.substring(5); // 去掉"file:"
                // 转换为URL路径：支持本地路径和服务器路径
                String localScreenshotDir = System.getProperty("user.home") + "/.openclaw/workspace/ui-auto-test-platform/screenshots/";
                if (fullPath.startsWith(localScreenshotDir)) {
                    stepExecution.setScreenshot("/screenshots/" + fullPath.substring(localScreenshotDir.length()));
                } else if (fullPath.startsWith("/opt/ui-auto-test-platform/screenshots/")) {
                    stepExecution.setScreenshot("/screenshots/" + fullPath.substring("/opt/ui-auto-test-platform/screenshots/".length()));
                } else {
                    stepExecution.setScreenshot(fullPath);
                }
            }
            
            // 设置AI token消耗
            String aiTokenUsedStr = extractJsonValue(stepJson, "aiTokenUsed");
            if (aiTokenUsedStr != null && !aiTokenUsedStr.isEmpty()) {
                try {
                    stepAiTokenUsed = Long.parseLong(aiTokenUsedStr);
                    stepExecution.setAiTokenUsed(stepAiTokenUsed);
                    System.err.println("[DEBUG] 步骤" + stepNo + " - aiTokenUsed值: " + stepAiTokenUsed);
                } catch (Exception e) {
                    System.err.println("[WARN] 解析aiTokenUsed失败: " + e.getMessage());
                }
            }
            
            // 设置步骤数据融合耗时
            String stepFusionDurationStr = extractJsonValue(stepJson, "stepFusionDuration");
            if (stepFusionDurationStr != null && !stepFusionDurationStr.isEmpty()) {
                try {
                    stepExecution.setStepFusionDuration(Long.parseLong(stepFusionDurationStr));
                    System.err.println("[DEBUG] 步骤" + stepNo + " - stepFusionDuration值: " + stepFusionDurationStr);
                } catch (Exception e) {
                    System.err.println("[WARN] 解析stepFusionDuration失败: " + e.getMessage());
                }
            }
            
            // 设置页面操作耗时
            String pageOperationDurationStr = extractJsonValue(stepJson, "pageOperationDuration");
            if (pageOperationDurationStr != null && !pageOperationDurationStr.isEmpty()) {
                try {
                    stepExecution.setPageOperationDuration(Long.parseLong(pageOperationDurationStr));
                    System.err.println("[DEBUG] 步骤" + stepNo + " - pageOperationDuration值: " + pageOperationDurationStr);
                } catch (Exception e) {
                    System.err.println("[WARN] 解析pageOperationDuration失败: " + e.getMessage());
                }
            }
            
            // 设置AI断言耗时
            String assertionDurationStr = extractJsonValue(stepJson, "assertionDuration");
            if (assertionDurationStr != null && !assertionDurationStr.isEmpty()) {
                try {
                    stepExecution.setAssertionDuration(Long.parseLong(assertionDurationStr));
                    System.err.println("[DEBUG] 步骤" + stepNo + " - assertionDuration值: " + assertionDurationStr);
                } catch (Exception e) {
                    System.err.println("[WARN] 解析assertionDuration失败: " + e.getMessage());
                }
            }
            
            // 设置步骤数据融合token消耗
            String stepFusionTokenUsedStr = extractJsonValue(stepJson, "stepFusionTokenUsed");
            if (stepFusionTokenUsedStr != null && !stepFusionTokenUsedStr.isEmpty()) {
                try {
                    stepExecution.setStepFusionTokenUsed(Long.parseLong(stepFusionTokenUsedStr));
                    System.err.println("[DEBUG] 步骤" + stepNo + " - stepFusionTokenUsed值: " + stepFusionTokenUsedStr);
                } catch (Exception e) {
                    System.err.println("[WARN] 解析stepFusionTokenUsed失败: " + e.getMessage());
                }
            }
            
            // 设置页面操作token消耗
            String pageOperationTokenUsedStr = extractJsonValue(stepJson, "pageOperationTokenUsed");
            if (pageOperationTokenUsedStr != null && !pageOperationTokenUsedStr.isEmpty()) {
                try {
                    stepExecution.setPageOperationTokenUsed(Long.parseLong(pageOperationTokenUsedStr));
                    System.err.println("[DEBUG] 步骤" + stepNo + " - pageOperationTokenUsed值: " + pageOperationTokenUsedStr);
                } catch (Exception e) {
                    System.err.println("[WARN] 解析pageOperationTokenUsed失败: " + e.getMessage());
                }
            }
            
            // 设置AI断言token消耗
            String assertionTokenUsedStr = extractJsonValue(stepJson, "assertionTokenUsed");
            if (assertionTokenUsedStr != null && !assertionTokenUsedStr.isEmpty()) {
                try {
                    stepExecution.setAssertionTokenUsed(Long.parseLong(assertionTokenUsedStr));
                    System.err.println("[DEBUG] 步骤" + stepNo + " - assertionTokenUsed值: " + assertionTokenUsedStr);
                } catch (Exception e) {
                    System.err.println("[WARN] 解析assertionTokenUsed失败: " + e.getMessage());
                }
            }
            
            // 设置融合后的步骤描述
            String fusedStepDescription = extractJsonValue(stepJson, "fusedStepDescription");
            if (fusedStepDescription != null && !fusedStepDescription.isEmpty()) {
                stepExecution.setFusedStepDescription(fusedStepDescription);
                System.err.println("[DEBUG] 步骤" + stepNo + " - fusedStepDescription值: " + fusedStepDescription);
            }
            
            stepExecutionService.updateById(stepExecution);
            System.err.println("[INFO] 步骤 " + stepNo + " 执行完成：" + executionStatus + ", 耗时：" + stepExecution.getDuration() + "ms, 开始时间：" + stepExecution.getStartTime() + ", AI token消耗：" + stepAiTokenUsed);
            
            // 实时更新执行记录的进度（passedCount 和 failedCount）
            updateExecutionProgress(executionId);
            
        } catch (Exception e) {
            System.err.println("[ERROR] 解析单个步骤结果失败：" + e.getMessage());
            e.printStackTrace();
        }
        
        return stepAiTokenUsed;
    }
    
    /**
     * 实时更新执行记录的进度（passedCount 和 failedCount）
     * 
     * @param executionId 执行记录ID
     */
    private void updateExecutionProgress(Long executionId) {
        try {
            // 查询该执行记录下所有步骤的执行状态
            List<TestStepExecution> allSteps = stepExecutionService.list(
                new LambdaQueryWrapper<TestStepExecution>()
                    .eq(TestStepExecution::getExecutionId, executionId)
            );
            
            int passedCount = 0;
            int failedCount = 0;
            
            for (TestStepExecution step : allSteps) {
                if ("success".equals(step.getStatus())) {
                    passedCount++;
                } else if ("failed".equals(step.getStatus())) {
                    failedCount++;
                }
            }
            
            // 更新执行记录
            TestCaseExecution execution = executionService.getById(executionId);
            if (execution != null) {
                execution.setPassedCount(passedCount);
                execution.setFailedCount(failedCount);
                executionService.updateById(execution);
                System.err.println("[INFO] 实时更新执行进度: passed=" + passedCount + ", failed=" + failedCount + ", total=" + execution.getTotalCount());
            }
        } catch (Exception e) {
            System.err.println("[WARN] 更新执行进度失败: " + e.getMessage());
        }
    }
    
    /**
     * 解析批量执行结果并更新步骤执行记录
     * 
     * @param batchResult 执行器返回的批量执行结果
     * @param executionId 执行记录ID
     * @param steps 测试步骤列表
     * @return 结果数组，index 0表示是否成功（1成功0失败），index 1表示总AI token消耗
     */
    private long[] parseAndUpdateResults(String batchResult, Long executionId, List<TestCaseStep> steps) {
        boolean allSuccess = true;
        long totalAiTokenUsed = 0;
        int passedCount = 0;
        int failedCount = 0;
        
        try {
            // 添加调试日志
            System.err.println("[DEBUG] 执行器返回结果长度: " + batchResult.length());
            System.err.println("[DEBUG] 执行器返回结果前200字符: " + batchResult.substring(0, Math.min(200, batchResult.length())));
            System.err.println("[DEBUG] 执行器返回结果后200字符: " + batchResult.substring(Math.max(0, batchResult.length() - 200)));
            
            // 找到JSON数组的位置 - 从最后往前找，找到{"results":[
            int resultsStart = -1;
            int searchPos = batchResult.length() - 1;
            while (searchPos >= 0) {
                int pos = batchResult.lastIndexOf("{\"results\":[", searchPos);
                if (pos >= 0) {
                    // 验证这个位置后面是否有匹配的]
                    int testBraceCount = 0;
                    boolean foundValid = false;
                    for (int i = pos; i < batchResult.length(); i++) {
                        char c = batchResult.charAt(i);
                        if (c == '{') testBraceCount++;
                        if (c == '}') {
                            testBraceCount--;
                            if (testBraceCount == 0) {
                                foundValid = true;
                                break;
                            }
                        }
                    }
                    if (foundValid) {
                        resultsStart = pos;
                        break;
                    }
                    searchPos = pos - 1;
                } else {
                    break;
                }
            }
            
            if (resultsStart < 0) {
                System.err.println("[ERROR] 无法找到{\"results\":[标记");
                return new long[]{0, totalAiTokenUsed};
            }
            
            // 从resultsStart开始，找到整个JSON对象的结束位置
            int jsonEnd = -1;
            int braceCount = 0;
            for (int i = resultsStart; i < batchResult.length(); i++) {
                char c = batchResult.charAt(i);
                if (c == '{') braceCount++;
                if (c == '}') {
                    braceCount--;
                    if (braceCount == 0) {
                        jsonEnd = i;
                        break;
                    }
                }
            }
            if (jsonEnd < 0) {
                System.err.println("[ERROR] 无法找到JSON结束位置");
                return new long[]{0, totalAiTokenUsed};
            }
            
            // 提取完整的JSON对象：{"results":[...]}
            String fullJson = batchResult.substring(resultsStart, jsonEnd + 1);
            System.err.println("[DEBUG] 完整JSON: " + fullJson);
            
            // 提取results数组内容：找到第一个[和最后一个]
            int arrayStart = fullJson.indexOf('[');
            int arrayEnd = fullJson.lastIndexOf(']');
            if (arrayStart < 0 || arrayEnd < 0) {
                System.err.println("[ERROR] 无法找到results数组");
                return new long[]{0, totalAiTokenUsed};
            }
            
            String resultsArray = fullJson.substring(arrayStart, arrayEnd + 1);
            System.err.println("[DEBUG] 步骤结果数组: " + resultsArray);
            
            // 检查results数组是否为空
            if (resultsArray.length() <= 2) { // "[]"
                System.err.println("[ERROR] results数组为空，执行失败");
                return new long[]{0, totalAiTokenUsed};
            }
            
            // 最简单可靠的方法：先找到所有步骤对象的位置
            List<Integer> stepStarts = new ArrayList<>();
            List<Integer> stepEnds = new ArrayList<>();
            
            // 遍历resultsArray，找到所有顶层{的位置（每个步骤的开始）
            int tempBracketCount = 0;
            for (int i = 0; i < resultsArray.length(); i++) {
                char c = resultsArray.charAt(i);
                if (c == '{') {
                    if (tempBracketCount == 0) {
                        stepStarts.add(i); // 这是一个步骤对象的开始
                    }
                    tempBracketCount++;
                }
                if (c == '}') {
                    tempBracketCount--;
                    if (tempBracketCount == 0 && !stepStarts.isEmpty()) {
                        stepEnds.add(i); // 这是一个步骤对象的结束
                    }
                }
            }
            
            System.err.println("[DEBUG] 找到" + stepStarts.size() + "个步骤对象");
            
            // 如果没有找到任何步骤对象，返回失败
            if (stepStarts.isEmpty()) {
                System.err.println("[ERROR] 没有找到任何步骤结果对象，执行失败");
                return new long[]{0, totalAiTokenUsed};
            }
            
            // 现在为每个步骤匹配对应的JSON并更新
            int foundCount = 0;
            for (TestCaseStep step : steps) {
                String stepKey = "\"stepNumber\":" + step.getStepNo();
                boolean found = false;
                
                for (int i = 0; i < stepStarts.size(); i++) {
                    int start = stepStarts.get(i);
                    int end = stepEnds.get(i);
                    String candidate = resultsArray.substring(start, end + 1);
                    
                    if (candidate.contains(stepKey)) {
                        // 找到匹配的步骤
                        String stepJson = candidate;
                        System.err.println("[DEBUG] 步骤" + step.getStepNo() + "的JSON: " + stepJson);
                        found = true;
                        foundCount++;
                        
                        // 查询该步骤的执行记录
                        TestStepExecution stepExecution = stepExecutionService.getOne(
                            new LambdaQueryWrapper<TestStepExecution>()
                                .eq(TestStepExecution::getExecutionId, executionId)
                                .eq(TestStepExecution::getStepNo, step.getStepNo())
                        );
                        
                        if (stepExecution == null) {
                            System.err.println("[WARN] 未找到步骤" + step.getStepNo() + "的执行记录");
                            continue;
                        }
                        
                        // 解析执行状态
                        String executionStatus = extractJsonValue(stepJson, "executionStatus");
                        String assertionStatus = extractJsonValue(stepJson, "assertionStatus");
                        String message = extractJsonValue(stepJson, "message");
                        // 优先使用字符串格式的executionTimeStr，如果不存在则回退到executionTime
                        String executionTime = extractJsonValue(stepJson, "executionTimeStr");
                        if (executionTime == null || executionTime.isEmpty()) {
                            executionTime = extractJsonValue(stepJson, "executionTime");
                        }
                        
                        // 设置状态和失败描述
                        if ("成功".equals(executionStatus)) {
                            stepExecution.setStatus("success");
                            stepExecution.setErrorMessage("无");
                            passedCount++;
                        } else {
                            stepExecution.setStatus("failed");
                            stepExecution.setErrorMessage(message != null ? message : "执行失败");
                            allSuccess = false;
                            failedCount++;
                        }
                        
                        // 设置断言状态
                        if ("通过".equals(assertionStatus) || "成功".equals(assertionStatus)) {
                            stepExecution.setAssertionStatus("success");
                            stepExecution.setAiResult("✅ " + message);
                        } else if ("失败".equals(assertionStatus)) {
                            stepExecution.setAssertionStatus("failed");
                            stepExecution.setAiResult("❌ " + message);
                            allSuccess = false;
                        } else if ("无".equals(assertionStatus)) {
                            stepExecution.setAssertionStatus("none");
                            stepExecution.setAiResult("- 无断言要求");
                        } else {
                            stepExecution.setAssertionStatus("none");
                        }
                        
                        // 设置开始时间
                        String stepStartTime = extractJsonValue(stepJson, "startTime");
                        System.err.println("[DEBUG] 步骤" + step.getStepNo() + " - startTime值: " + stepStartTime);
                        if (stepStartTime != null && !stepStartTime.isEmpty()) {
                            try {
                                // 先尝试解析为时间戳（毫秒）
                                long timestamp = Long.parseLong(stepStartTime);
                                stepExecution.setStartTime(LocalDateTime.ofInstant(
                                    java.time.Instant.ofEpochMilli(timestamp),
                                    java.time.ZoneId.systemDefault()
                                ));
                            } catch (Exception e) {
                                // 如果不是时间戳，尝试解析为日期字符串
                                try {
                                    stepExecution.setStartTime(LocalDateTime.parse(stepStartTime));
                                } catch (Exception ex) {
                                    stepExecution.setStartTime(LocalDateTime.now());
                                }
                            }
                        } else {
                            stepExecution.setStartTime(LocalDateTime.now());
                        }
                        
                        // 设置耗时
                        System.err.println("[DEBUG] 步骤" + step.getStepNo() + " - executionTime值: " + executionTime);
                        try {
                            if (executionTime != null && !executionTime.isEmpty()) {
                                stepExecution.setDuration(Long.parseLong(executionTime));
                            } else {
                                stepExecution.setDuration(0L);
                            }
                        } catch (Exception e) {
                            stepExecution.setDuration(0L);
                            System.err.println("[WARN] 解析executionTime失败: " + e.getMessage());
                        }
                        
                        // 设置截图 - 读取screenshotBase64（file:开头的路径），去掉file:前缀，并转换为URL路径
                        String screenshotBase64 = extractJsonValue(stepJson, "screenshotBase64");
                        System.err.println("[DEBUG] 步骤" + step.getStepNo() + " - screenshotBase64值: " + screenshotBase64);
                        if (screenshotBase64 != null && screenshotBase64.startsWith("file:")) {
                            String fullPath = screenshotBase64.substring(5); // 去掉"file:"
                            // 转换为URL路径：支持本地路径和服务器路径
                            String localScreenshotDir = System.getProperty("user.home") + "/.openclaw/workspace/ui-auto-test-platform/screenshots/";
                            if (fullPath.startsWith(localScreenshotDir)) {
                                stepExecution.setScreenshot("/screenshots/" + fullPath.substring(localScreenshotDir.length()));
                            } else if (fullPath.startsWith("/opt/ui-auto-test-platform/screenshots/")) {
                                stepExecution.setScreenshot("/screenshots/" + fullPath.substring("/opt/ui-auto-test-platform/screenshots/".length()));
                            } else {
                                stepExecution.setScreenshot(fullPath);
                            }
                        }
                        
                        // 设置AI token消耗
                        String aiTokenUsedStr = extractJsonValue(stepJson, "aiTokenUsed");
                        long stepAiTokenUsed = 0;
                        if (aiTokenUsedStr != null && !aiTokenUsedStr.isEmpty()) {
                            try {
                                stepAiTokenUsed = Long.parseLong(aiTokenUsedStr);
                                stepExecution.setAiTokenUsed(stepAiTokenUsed);
                                totalAiTokenUsed += stepAiTokenUsed;
                                System.err.println("[DEBUG] 步骤" + step.getStepNo() + " - aiTokenUsed值: " + stepAiTokenUsed);
                            } catch (Exception e) {
                                System.err.println("[WARN] 解析aiTokenUsed失败: " + e.getMessage());
                            }
                        }
                        
                        // 设置步骤数据融合耗时
                        String stepFusionDurationStr = extractJsonValue(stepJson, "stepFusionDuration");
                        if (stepFusionDurationStr != null && !stepFusionDurationStr.isEmpty()) {
                            try {
                                stepExecution.setStepFusionDuration(Long.parseLong(stepFusionDurationStr));
                                System.err.println("[DEBUG] 步骤" + step.getStepNo() + " - stepFusionDuration值: " + stepFusionDurationStr);
                            } catch (Exception e) {
                                System.err.println("[WARN] 解析stepFusionDuration失败: " + e.getMessage());
                            }
                        }
                        
                        // 设置页面操作耗时
                        String pageOperationDurationStr = extractJsonValue(stepJson, "pageOperationDuration");
                        if (pageOperationDurationStr != null && !pageOperationDurationStr.isEmpty()) {
                            try {
                                stepExecution.setPageOperationDuration(Long.parseLong(pageOperationDurationStr));
                                System.err.println("[DEBUG] 步骤" + step.getStepNo() + " - pageOperationDuration值: " + pageOperationDurationStr);
                            } catch (Exception e) {
                                System.err.println("[WARN] 解析pageOperationDuration失败: " + e.getMessage());
                            }
                        }
                        
                        // 设置AI断言耗时
                        String assertionDurationStr = extractJsonValue(stepJson, "assertionDuration");
                        if (assertionDurationStr != null && !assertionDurationStr.isEmpty()) {
                            try {
                                stepExecution.setAssertionDuration(Long.parseLong(assertionDurationStr));
                                System.err.println("[DEBUG] 步骤" + step.getStepNo() + " - assertionDuration值: " + assertionDurationStr);
                            } catch (Exception e) {
                                System.err.println("[WARN] 解析assertionDuration失败: " + e.getMessage());
                            }
                        }
                        
                        // 设置步骤数据融合token消耗
                        String stepFusionTokenUsedStr = extractJsonValue(stepJson, "stepFusionTokenUsed");
                        if (stepFusionTokenUsedStr != null && !stepFusionTokenUsedStr.isEmpty()) {
                            try {
                                stepExecution.setStepFusionTokenUsed(Long.parseLong(stepFusionTokenUsedStr));
                                System.err.println("[DEBUG] 步骤" + step.getStepNo() + " - stepFusionTokenUsed值: " + stepFusionTokenUsedStr);
                            } catch (Exception e) {
                                System.err.println("[WARN] 解析stepFusionTokenUsed失败: " + e.getMessage());
                            }
                        }
                        
                        // 设置页面操作token消耗
                        String pageOperationTokenUsedStr = extractJsonValue(stepJson, "pageOperationTokenUsed");
                        if (pageOperationTokenUsedStr != null && !pageOperationTokenUsedStr.isEmpty()) {
                            try {
                                stepExecution.setPageOperationTokenUsed(Long.parseLong(pageOperationTokenUsedStr));
                                System.err.println("[DEBUG] 步骤" + step.getStepNo() + " - pageOperationTokenUsed值: " + pageOperationTokenUsedStr);
                            } catch (Exception e) {
                                System.err.println("[WARN] 解析pageOperationTokenUsed失败: " + e.getMessage());
                            }
                        }
                        
                        // 设置AI断言token消耗
                        String assertionTokenUsedStr = extractJsonValue(stepJson, "assertionTokenUsed");
                        if (assertionTokenUsedStr != null && !assertionTokenUsedStr.isEmpty()) {
                            try {
                                stepExecution.setAssertionTokenUsed(Long.parseLong(assertionTokenUsedStr));
                                System.err.println("[DEBUG] 步骤" + step.getStepNo() + " - assertionTokenUsed值: " + assertionTokenUsedStr);
                            } catch (Exception e) {
                                System.err.println("[WARN] 解析assertionTokenUsed失败: " + e.getMessage());
                            }
                        }
                        
                        // 设置融合后的步骤描述
                        String fusedStepDescription = extractJsonValue(stepJson, "fusedStepDescription");
                        if (fusedStepDescription != null && !fusedStepDescription.isEmpty()) {
                            stepExecution.setFusedStepDescription(fusedStepDescription);
                            System.err.println("[DEBUG] 步骤" + step.getStepNo() + " - fusedStepDescription值: " + fusedStepDescription);
                        }
                        
                        stepExecutionService.updateById(stepExecution);
                        System.err.println("[INFO] 步骤 " + step.getStepNo() + " 执行完成：" + executionStatus + ", 耗时：" + stepExecution.getDuration() + "ms, 开始时间：" + stepExecution.getStartTime() + ", AI token消耗：" + stepAiTokenUsed);
                        
                        break;
                    }
                }
                
                if (!found) {
                    System.err.println("[WARN] 未找到步骤" + step.getStepNo() + "的结果");
                }
            }
            
            // 检查是否所有步骤都找到了结果
            System.err.println("[DEBUG] 找到" + foundCount + "个步骤结果，共" + steps.size() + "个步骤");
            if (foundCount != steps.size()) {
                System.err.println("[ERROR] 步骤结果数量不匹配，执行失败");
                allSuccess = false;
            }
            
        } catch (Exception e) {
            System.err.println("[ERROR] 解析结果失败：" + e.getMessage());
            e.printStackTrace();
            allSuccess = false;
        }
        
        // 返回结果：index 0表示是否成功，index 1表示总AI token消耗，index 2表示成功步骤数，index 3表示失败步骤数
        return new long[]{allSuccess ? 1 : 0, totalAiTokenUsed, passedCount, failedCount};
    }
    
    /**
     * 将所有未完成的步骤更新为failed状态
     * 
     * @param executionId 执行记录ID
     * @param errorMessage 失败原因描述
     */
    private void updatePendingStepsToFailed(Long executionId, String errorMessage) {
        List<TestStepExecution> pendingSteps = stepExecutionService.list(
            new LambdaQueryWrapper<TestStepExecution>()
                .eq(TestStepExecution::getExecutionId, executionId)
                .ne(TestStepExecution::getStatus, "success")
        );
        
        System.err.println("[DEBUG] 找到 " + pendingSteps.size() + " 个未完成步骤需要更新");
        
        for (TestStepExecution step : pendingSteps) {
            System.err.println("[DEBUG] 更新步骤 " + step.getStepNo() + ": status=" + step.getStatus() + ", errorMessage=" + errorMessage);
            step.setStatus("failed");
            step.setAiResult("- " + errorMessage);
            step.setErrorMessage(errorMessage);
            stepExecutionService.updateById(step);
        }
    }
    
    /**
     * 从 JSON 字符串中提取值
     * 
     * @param json JSON字符串
     * @param key 要提取的键
     * @return 提取到的值，如果未找到则返回null
     */
    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int keyPos = json.lastIndexOf(searchKey); // 找最后一个匹配的键
        if (keyPos < 0) return null;
        
        int valueStart = keyPos + searchKey.length();
        
        // 跳过空格
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }
        
        if (valueStart >= json.length()) return null;
        
        char firstChar = json.charAt(valueStart);
        
        if (firstChar == '"') {
            // 字符串值
            valueStart++;
            int valueEnd = valueStart;
            while (valueEnd < json.length()) {
                if (json.charAt(valueEnd) == '"' && json.charAt(valueEnd - 1) != '\\') {
                    break;
                }
                valueEnd++;
            }
            return json.substring(valueStart, valueEnd).replace("\\\"", "\"").replace("\\\\", "\\");
        } else {
            // 数字或其他值
            int valueEnd = valueStart;
            while (valueEnd < json.length() && json.charAt(valueEnd) != ',' && json.charAt(valueEnd) != '}') {
                valueEnd++;
            }
            return json.substring(valueStart, valueEnd).trim();
        }
    }
    
    /**
     * 批量执行步骤
     * 
     * @param executorPath 执行器脚本路径
     * @param stepsJson 步骤JSON数组字符串
     * @param saveTokenMode 是否使用节约token模式
     * @param caseId 测试用例ID
     * @param useCache 是否使用缓存
     * @param executionId 执行记录ID
     * @param steps 测试步骤列表
     * @return 执行器返回的结果
     * @throws Exception 执行失败时抛出异常
     */
    private String executeStepsBatch(String executorPath, String stepsJson, Boolean saveTokenMode, Long caseId, Boolean useCache, Long executionId, List<TestCaseStep> steps) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
            nodePath,
            executorPath,
            stepsJson
        );
        pb.directory(new java.io.File(scriptsDir));
        pb.environment().put("PLAYWRIGHT_BROWSERS_PATH", playwrightBrowsersPath);
        pb.environment().put("SAVE_TOKEN_MODE", saveTokenMode != null && saveTokenMode ? "true" : "false");
        if (caseId != null) {
            pb.environment().put("TEST_CASE_ID", String.valueOf(caseId));
        }
        pb.environment().put("USE_CACHE", useCache != null && useCache ? "true" : "false");
        System.err.println("[INFO] USE_CACHE 环境变量: " + (useCache != null && useCache ? "true" : "false"));
        pb.redirectErrorStream(true);
        
        Process process = pb.start();
        StringBuilder output = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                System.err.println("[Executor Output] " + line);
                
                // 检查是否是 stepResult 的 JSON 格式
                if (line.contains("\"stepResult\":")) {
                    try {
                        // 提取 stepResult 的值
                        int stepResultStart = line.indexOf("\"stepResult\":");
                        if (stepResultStart >= 0) {
                            String jsonPart = line.substring(stepResultStart + 13); // 去掉"stepResult":
                            // 找到完整的 JSON 对象
                            int braceCount = 0;
                            int jsonEnd = -1;
                            for (int i = 0; i < jsonPart.length(); i++) {
                                char c = jsonPart.charAt(i);
                                if (c == '{') braceCount++;
                                if (c == '}') {
                                    braceCount--;
                                    if (braceCount == 0) {
                                        jsonEnd = i;
                                        break;
                                    }
                                }
                            }
                            if (jsonEnd >= 0) {
                                String stepResultJson = jsonPart.substring(0, jsonEnd + 1);
                                System.err.println("[INFO] 解析到单个步骤结果: " + stepResultJson);
                                // 更新该步骤的执行状态
                                parseAndUpdateSingleStepResult(stepResultJson, executionId, steps);
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("[WARN] 解析实时步骤结果失败: " + e.getMessage());
                    }
                }
            }
        }
        
        int exitCode = process.waitFor();
        System.err.println("[Executor Exit Code] " + exitCode);
        
        if (exitCode != 0) {
            throw new Exception("执行器执行失败，退出码：" + exitCode + ", 输出：" + output.toString());
        }
        
        return output.toString();
    }
    
    /**
     * 转义 JSON 特殊字符
     * 
     * @param str 要转义的字符串
     * @return 转义后的字符串
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    /**
     * 分页查询用例执行记录
     * 
     * @param pageNum 页码，默认1
     * @param pageSize 每页条数，默认10
     * @param caseId 用例ID（可选）
     * @param taskExecutionId 任务执行记录ID（可选）
     * @param status 执行状态（可选）
     * @return 分页的执行记录列表，包含执行人昵称
     */
    @GetMapping("/list")
    public Result<Page<TestCaseExecution>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long caseId,
            @RequestParam(required = false) Long taskExecutionId,
            @RequestParam(required = false) String status) {
        Page<TestCaseExecution> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<TestCaseExecution> wrapper = new LambdaQueryWrapper<>();
        if (caseId != null) {
            wrapper.eq(TestCaseExecution::getCaseId, caseId);
        }
        if (taskExecutionId != null) {
            wrapper.eq(TestCaseExecution::getTaskExecutionId, taskExecutionId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(TestCaseExecution::getStatus, status);
        }
        wrapper.orderByDesc(TestCaseExecution::getStartTime);
        Page<TestCaseExecution> result = executionService.page(page, wrapper);
        
        // 为每条执行记录填充执行人昵称
        if (result.getRecords() != null && !result.getRecords().isEmpty()) {
            for (TestCaseExecution execution : result.getRecords()) {
                if (execution.getExecutor() != null && !execution.getExecutor().isEmpty()) {
                    User user = userService.getOne(
                        new LambdaQueryWrapper<User>()
                            .eq(User::getUsername, execution.getExecutor())
                    );
                    if (user != null && user.getNickname() != null) {
                        execution.setExecutorNickname(user.getNickname());
                    } else {
                        execution.setExecutorNickname(execution.getExecutor());
                    }
                }
            }
        }
        
        return Result.success(result);
    }
    
    /**
     * 根据执行记录 ID 获取执行详情
     * 
     * @param id 执行记录ID
     * @return 执行记录详情
     */
    @GetMapping("/{id}")
    public Result<TestCaseExecution> getById(@PathVariable Long id) {
        TestCaseExecution execution = executionService.getById(id);
        return execution != null ? Result.success(execution) : Result.error("执行记录不存在");
    }
    
    /**
     * 获取步骤执行详情列表
     * 
     * @param executionId 执行记录ID
     * @param pageNum 页码，默认1
     * @param pageSize 每页条数，默认10
     * @return 步骤执行详情列表
     */
    @GetMapping("/{executionId}/steps")
    public Result<Page<TestStepExecution>> getSteps(
            @PathVariable Long executionId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<TestStepExecution> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<TestStepExecution> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TestStepExecution::getExecutionId, executionId);
        wrapper.orderByAsc(TestStepExecution::getStepNo);
        Page<TestStepExecution> result = stepExecutionService.page(page, wrapper);
        // 处理截图路径，将绝对路径转换为相对路径
        String basePath = System.getProperty("user.home") + "/.openclaw/workspace/ui-auto-test-platform/screenshots/";
        if (result.getRecords() != null) {
            for (TestStepExecution step : result.getRecords()) {
                String screenshot = step.getScreenshot();
                if (screenshot != null && screenshot.startsWith(basePath)) {
                    step.setScreenshot("/screenshots/" + screenshot.substring(basePath.length()));
                }
            }
        }
        return Result.success(result);
    }
}
