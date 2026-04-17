package com.uiauto.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.uiauto.entity.TestCase;
import com.uiauto.entity.TestCaseExecution;
import com.uiauto.entity.TestCaseStep;
import com.uiauto.entity.TestStepExecution;
import com.uiauto.entity.Model;
import com.uiauto.mapper.TestCaseExecutionMapper;
import com.uiauto.service.TestCaseExecutionService;
import com.uiauto.service.TestCaseService;
import com.uiauto.service.TestCaseStepService;
import com.uiauto.service.TestStepExecutionService;
import com.uiauto.service.ModelService;
import com.uiauto.service.ModelScenarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class TestCaseExecutionServiceImpl extends ServiceImpl<TestCaseExecutionMapper, TestCaseExecution> implements TestCaseExecutionService {

    @Autowired
    private TestStepExecutionService stepExecutionService;

    @Autowired
    private TestCaseService testCaseService;

    @Autowired
    private TestCaseStepService testCaseStepService;

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

    @Override
    public Long executeTestCase(Long caseId, String executor, String saveTokenMode, Long taskExecutionId) {
        long caseAiTokenUsed = 0;
        try {
            TestCase testCase = testCaseService.getById(caseId);
            if (testCase == null) return 0L;

            List<TestCaseStep> steps = testCaseStepService.list(
                new LambdaQueryWrapper<TestCaseStep>()
                    .eq(TestCaseStep::getCaseId, caseId)
                    .orderByAsc(TestCaseStep::getStepNo)
            );
            if (steps.isEmpty()) return 0L;

            // 获取图像断言和数据融合场景对应的模型
            String imageAssertionModelName = null;
            String stepFusionModelName = null;
            try {
                com.uiauto.entity.ModelScenario imageAssertionScenario = modelScenarioService.getByScenarioCode("image_assertion");
                if (imageAssertionScenario != null && imageAssertionScenario.getModelId() != null) {
                    Model imageAssertionModel = modelService.getById(imageAssertionScenario.getModelId());
                    if (imageAssertionModel != null) {
                        imageAssertionModelName = imageAssertionModel.getModelName();
                    }
                }
                com.uiauto.entity.ModelScenario stepFusionScenario = modelScenarioService.getByScenarioCode("step_fusion");
                if (stepFusionScenario != null && stepFusionScenario.getModelId() != null) {
                    Model stepFusionModel = modelService.getById(stepFusionScenario.getModelId());
                    if (stepFusionModel != null) {
                        stepFusionModelName = stepFusionModel.getModelName();
                    }
                }
            } catch (Exception e) {
                System.err.println("[WARN] 获取模型配置失败: " + e.getMessage());
            }

            // 提前获取该用例的历史执行记录（用于缓存判断）
            List<TestCaseExecution> lastExecutions = list(
                new LambdaQueryWrapper<TestCaseExecution>()
                    .eq(TestCaseExecution::getCaseId, caseId)
                    .orderByDesc(TestCaseExecution::getCreateTime)
                    .last("LIMIT 10")
            );

            // 创建执行记录
            TestCaseExecution execution = new TestCaseExecution();
            execution.setCaseId(caseId);
            execution.setTaskExecutionId(taskExecutionId);
            execution.setCaseNo(testCase.getCaseNo());
            execution.setCaseName(testCase.getName());
            execution.setDescription(testCase.getDescription());
            execution.setExecutor(executor);
            execution.setStartTime(LocalDateTime.now());
            execution.setStatus("running");
            execution.setImageAssertionModel(imageAssertionModelName);
            execution.setStepFusionModel(stepFusionModelName);
            execution.setTotalCount(steps.size());
            execution.setPassedCount(0);
            execution.setFailedCount(0);
            save(execution);

            // 先创建所有步骤执行记录，状态设为pending（未执行）
            for (TestCaseStep step : steps) {
                TestStepExecution stepExecution = new TestStepExecution();
                stepExecution.setExecutionId(execution.getId());
                stepExecution.setStepNo(step.getStepNo());
                stepExecution.setStepDescription(step.getStepDescription());
                stepExecution.setAssertionDescription(step.getAssertionDescription());
                stepExecution.setTestData(step.getTestData());
                stepExecution.setStatus("pending");
                stepExecution.setAssertionStatus("none");
                stepExecution.setAiResult("- 等待执行");
                stepExecution.setErrorMessage("无");
                stepExecution.setCreateTime(LocalDateTime.now());
                stepExecutionService.save(stepExecution);
            }

            // 异步执行步骤（批量模式）
            long start = System.currentTimeMillis();
            long totalAiTokenUsed = 0;

            try {
                String executorPath = scriptsDir + java.io.File.separator + executorScript;

                // 构建步骤 JSON 数组（批量执行）- 使用字符串拼接
                StringBuilder stepsJson = new StringBuilder("[");
                for (int i = 0; i < steps.size(); i++) {
                    TestCaseStep step = steps.get(i);
                    if (i > 0) stepsJson.append(",");
                    stepsJson.append("{");
                    // 首先添加原始字段
                    stepsJson.append("\"originalAction\":\"").append(escapeJson(step.getStepDescription())).append("\"");
                    // 构建用于 Midscene 的 action（使用原始步骤描述 + 测试数据，确保缓存正常工作）
                    String midsceneAction = step.getStepDescription();
                    if (step.getTestData() != null && !step.getTestData().isEmpty()) {
                        midsceneAction = step.getStepDescription() + " | " + step.getTestData();
                    }
                    stepsJson.append(",\"action\":\"").append(escapeJson(midsceneAction)).append("\"");
                    stepsJson.append(",\"assertion\":\"").append(escapeJson(step.getAssertionDescription() != null ? step.getAssertionDescription() : "")).append("\"");
                    stepsJson.append(",\"stepNumber\":").append(step.getStepNo());
                    // 添加测试数据字段，方便调试
                    if (step.getTestData() != null && !step.getTestData().isEmpty()) {
                        stepsJson.append(",\"testData\":\"").append(escapeJson(step.getTestData())).append("\"");
                    }
                    // 检查是否有历史融合结果
                    if (lastExecutions != null && !lastExecutions.isEmpty()) {
                        for (TestCaseExecution lastExec : lastExecutions) {
                            if (lastExec.getId().equals(execution.getId())) {
                                continue; // 跳过当前这条刚创建的
                            }
                            // 查找该用例历史执行中的该步骤的执行记录
                            List<TestStepExecution> lastStepExecutions = stepExecutionService.list(
                                new LambdaQueryWrapper<TestStepExecution>()
                                    .eq(TestStepExecution::getExecutionId, lastExec.getId())
                                    .eq(TestStepExecution::getStepNo, step.getStepNo())
                                    .orderByDesc(TestStepExecution::getCreateTime)
                                    .last("LIMIT 5")
                            );
                            if (lastStepExecutions != null && !lastStepExecutions.isEmpty()) {
                                TestStepExecution lastStepExecution = lastStepExecutions.get(0);
                                // 检查步骤描述和测试数据是否都没有变化
                                boolean stepDescriptionMatch = step.getStepDescription().equals(lastStepExecution.getStepDescription());
                                boolean testDataMatch = (step.getTestData() == null && lastStepExecution.getTestData() == null) || 
                                    (step.getTestData() != null && step.getTestData().equals(lastStepExecution.getTestData()));
                                if (stepDescriptionMatch && testDataMatch && lastStepExecution.getFusedStepDescription() != null && !lastStepExecution.getFusedStepDescription().isEmpty()) {
                                    // 传递融合后的步骤描述
                                    stepsJson.append(",\"fusedStepDescription\":\"").append(escapeJson(lastStepExecution.getFusedStepDescription())).append("\"");
                                    System.err.println("[INFO] 找到步骤 " + step.getStepNo() + " 的历史融合结果，直接使用: " + lastStepExecution.getFusedStepDescription());
                                    break; // 找到第一个成功的就可以了
                                }
                            }
                        }
                    }
                    stepsJson.append("}");
                }
                stepsJson.append("]");

                System.err.println("[INFO] 开始批量执行，共 " + steps.size() + " 个步骤");

                // 检查是否有任何成功的历史执行记录
                boolean useCache = false;
                if (lastExecutions != null && !lastExecutions.isEmpty()) {
                    // 遍历所有历史执行记录，找到第一条成功的（排除当前这条刚创建的）
                    for (TestCaseExecution exec : lastExecutions) {
                        // 排除当前这条刚创建的记录（ID相同）
                        if (!exec.getId().equals(execution.getId()) && "success".equals(exec.getStatus())) {
                            useCache = true;
                            System.err.println("[INFO] 找到成功的历史执行记录，启用 Midscene 缓存，执行 ID: " + exec.getId());
                            break;
                        }
                    }
                    if (!useCache) {
                        System.err.println("[INFO] 没有找到成功的历史执行记录，禁用 Midscene 缓存");
                    }
                } else {
                    System.err.println("[INFO] 没有找到历史执行记录，禁用 Midscene 缓存");
                }

                // 批量执行所有步骤
                String batchResult = executeStepsBatch(executorPath, stepsJson.toString(), saveTokenMode, caseId, useCache, execution.getId(), steps);

                // 解析结果并更新步骤执行记录，返回总AI token消耗
                long[] result = parseAndUpdateResults(batchResult, execution.getId(), steps);
                boolean allSuccess = (result[0] == 1);
                totalAiTokenUsed = result[1];
                caseAiTokenUsed = totalAiTokenUsed;
                int passedCount = (int) result[2];
                int failedCount = (int) result[3];

                // 更新执行记录状态
                long end = System.currentTimeMillis();
                execution.setDuration(end - start);
                execution.setEndTime(LocalDateTime.now());
                execution.setStatus(allSuccess ? "success" : "failed");
                execution.setAiTotalTokenUsed(totalAiTokenUsed);
                execution.setPassedCount(passedCount);
                execution.setFailedCount(failedCount);
                updateById(execution);

                // 如果执行失败，更新所有未完成的步骤状态为失败
                if (!allSuccess) {
                    updatePendingStepsToFailed(execution.getId(), "执行失败");
                }

                System.err.println("[INFO] 用例执行完成，总耗时：" + (end - start) + "ms, 状态：" + execution.getStatus());

            } catch (Exception e) {
                System.err.println("[ERROR] 用例执行失败：" + e.getMessage());
                e.printStackTrace();

                // 更新执行记录状态为失败
                execution.setStatus("failed");
                execution.setEndTime(LocalDateTime.now());
                execution.setDescription(execution.getDescription() + " [执行失败：" + e.getMessage() + "");
                updateById(execution);

                // 更新所有未完成的步骤状态为失败，并设置失败原因
                updatePendingStepsToFailed(execution.getId(), "执行失败：" + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("[ERROR] 执行用例失败：" + e.getMessage());
            e.printStackTrace();
        }
        return caseAiTokenUsed;
    }

    /**
     * 批量执行步骤
     */
    private String executeStepsBatch(String executorPath, String stepsJson, String saveTokenMode, Long caseId, boolean useCache, Long executionId, List<TestCaseStep> steps) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(nodePath, executorPath, stepsJson, String.valueOf(caseId), useCache ? "1" : "0");
        pb.environment().put("PLAYWRIGHT_BROWSERS_PATH", playwrightBrowsersPath);
        pb.environment().put("SAVE_TOKEN_MODE", saveTokenMode);
        pb.environment().put("USE_CACHE", useCache ? "true" : "false");
        pb.environment().put("TEST_CASE_ID", String.valueOf(caseId));
        pb.redirectErrorStream(true);
        Process process = pb.start();

        StringBuilder result = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.err.println("[EXEC] " + line);
            result.append(line).append("\n");
            // 实时检查是否有单个步骤的结果
            if (line.contains("\"stepResult\":")) {
                try {
                    parseAndUpdateSingleStepResult(line, executionId, steps);
                } catch (Exception e) {
                    System.err.println("[WARN] 解析单个步骤结果失败: " + e.getMessage());
                }
            }
        }
        int exitCode = process.waitFor();
        System.err.println("[INFO] 执行器退出码: " + exitCode);
        return result.toString();
    }

    /**
     * 解析单个步骤的结果并更新步骤执行状态
     */
    private long parseAndUpdateSingleStepResult(String stepJson, Long executionId, List<TestCaseStep> steps) {
        long stepAiTokenUsed = 0;
        int stepNo = -1; // 定义在 try 块外面

        try {
            // 提取stepNumber
            String stepNumberStr = extractJsonValue(stepJson, "stepNumber");
            if (stepNumberStr == null || stepNumberStr.isEmpty()) {
                System.err.println("[WARN] 无法找到stepNumber");
                return 0;
            }

            stepNo = Integer.parseInt(stepNumberStr);

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

            // 解析AI token消耗
            String aiTokenUsed = extractJsonValue(stepJson, "aiTokenUsed");
            if (aiTokenUsed != null && !aiTokenUsed.isEmpty()) {
                try {
                    stepAiTokenUsed = Long.parseLong(aiTokenUsed);
                    stepExecution.setAiTokenUsed(stepAiTokenUsed);
                } catch (Exception e) {
                    stepAiTokenUsed = 0;
                    System.err.println("[WARN] 解析aiTokenUsed失败: " + e.getMessage());
                }
            }

            // 解析测试数据
            String testData = extractJsonValue(stepJson, "testData");
            if (testData != null && !testData.isEmpty()) {
                stepExecution.setTestData(testData);
            }

            // 解析步骤数据融合耗时
            String stepFusionDuration = extractJsonValue(stepJson, "stepFusionDuration");
            if (stepFusionDuration != null && !stepFusionDuration.isEmpty()) {
                try {
                    stepExecution.setStepFusionDuration(Long.parseLong(stepFusionDuration));
                } catch (Exception e) {
                    System.err.println("[WARN] 解析stepFusionDuration失败: " + e.getMessage());
                }
            }

            // 解析页面操作耗时
            String pageOperationDuration = extractJsonValue(stepJson, "pageOperationDuration");
            if (pageOperationDuration != null && !pageOperationDuration.isEmpty()) {
                try {
                    stepExecution.setPageOperationDuration(Long.parseLong(pageOperationDuration));
                } catch (Exception e) {
                    System.err.println("[WARN] 解析pageOperationDuration失败: " + e.getMessage());
                }
            }

            // 解析AI断言耗时
            String assertionDuration = extractJsonValue(stepJson, "assertionDuration");
            if (assertionDuration != null && !assertionDuration.isEmpty()) {
                try {
                    stepExecution.setAssertionDuration(Long.parseLong(assertionDuration));
                } catch (Exception e) {
                    System.err.println("[WARN] 解析assertionDuration失败: " + e.getMessage());
                }
            }

            // 解析数据融合token消耗
            String stepFusionTokenUsed = extractJsonValue(stepJson, "stepFusionTokenUsed");
            if (stepFusionTokenUsed != null && !stepFusionTokenUsed.isEmpty()) {
                try {
                    stepExecution.setStepFusionTokenUsed(Long.parseLong(stepFusionTokenUsed));
                } catch (Exception e) {
                    System.err.println("[WARN] 解析stepFusionTokenUsed失败: " + e.getMessage());
                }
            }

            // 解析页面操作token消耗
            String pageOperationTokenUsed = extractJsonValue(stepJson, "pageOperationTokenUsed");
            if (pageOperationTokenUsed != null && !pageOperationTokenUsed.isEmpty()) {
                try {
                    stepExecution.setPageOperationTokenUsed(Long.parseLong(pageOperationTokenUsed));
                } catch (Exception e) {
                    System.err.println("[WARN] 解析pageOperationTokenUsed失败: " + e.getMessage());
                }
            }

            // 解析AI断言token消耗
            String assertionTokenUsed = extractJsonValue(stepJson, "assertionTokenUsed");
            if (assertionTokenUsed != null && !assertionTokenUsed.isEmpty()) {
                try {
                    stepExecution.setAssertionTokenUsed(Long.parseLong(assertionTokenUsed));
                } catch (Exception e) {
                    System.err.println("[WARN] 解析assertionTokenUsed失败: " + e.getMessage());
                }
            }

            // 解析融合后的步骤描述
            String fusedStepDescription = extractJsonValue(stepJson, "fusedStepDescription");
            if (fusedStepDescription != null && !fusedStepDescription.isEmpty()) {
                stepExecution.setFusedStepDescription(fusedStepDescription);
            }

            // 解析截图并转换为相对路径
            String screenshot = extractJsonValue(stepJson, "screenshot");
            if (screenshot != null && !screenshot.isEmpty()) {
                // 将绝对路径转换为相对路径
                String basePath = System.getProperty("user.home") + "/.openclaw/workspace/ui-auto-test-platform/screenshots/";
                if (screenshot.startsWith(basePath)) {
                    screenshot = "/screenshots/" + screenshot.substring(basePath.length());
                }
                stepExecution.setScreenshot(screenshot);
            }

            // 更新执行记录
            stepExecutionService.updateById(stepExecution);
            System.err.println("[INFO] 步骤" + stepNo + "执行状态更新完成");

            // 更新用例执行记录中的进度统计
            updateExecutionProgress(executionId);

        } catch (Exception e) {
            System.err.println("[ERROR] 更新步骤" + stepNo + "执行状态失败：" + e.getMessage());
            e.printStackTrace();
        }

        return stepAiTokenUsed;
    }

    /**
     * 更新用例执行记录中的进度统计
     */
    private void updateExecutionProgress(Long executionId) {
        try {
            // 查询该执行记录下的所有步骤执行记录
            List<TestStepExecution> allStepExecutions = stepExecutionService.list(
                new LambdaQueryWrapper<TestStepExecution>()
                    .eq(TestStepExecution::getExecutionId, executionId)
            );

            if (allStepExecutions == null || allStepExecutions.isEmpty()) {
                return;
            }

            int passedCount = 0;
            int failedCount = 0;
            for (TestStepExecution stepExec : allStepExecutions) {
                if ("success".equals(stepExec.getStatus())) {
                    passedCount++;
                } else if ("failed".equals(stepExec.getStatus())) {
                    failedCount++;
                }
            }

            // 更新用例执行记录
            TestCaseExecution execution = getById(executionId);
            if (execution != null) {
                execution.setPassedCount(passedCount);
                execution.setFailedCount(failedCount);
                updateById(execution);
            }
        } catch (Exception e) {
            System.err.println("[WARN] 更新执行进度失败: " + e.getMessage());
        }
    }

    /**
     * 解析批量执行结果并更新步骤执行记录
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
                        } else {
                            stepExecution.setStatus("failed");
                            stepExecution.setErrorMessage(message != null ? message : "执行失败");
                            allSuccess = false;
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
                        try {
                            if (executionTime != null && !executionTime.isEmpty()) {
                                stepExecution.setDuration(Long.parseLong(executionTime));
                            } else {
                                stepExecution.setDuration(0L);
                            }
                        } catch (Exception e) {
                            stepExecution.setDuration(0L);
                        }

                        // 解析AI token消耗
                        String aiTokenUsed = extractJsonValue(stepJson, "aiTokenUsed");
                        if (aiTokenUsed != null && !aiTokenUsed.isEmpty()) {
                            try {
                                long stepAiTokenUsed = Long.parseLong(aiTokenUsed);
                                stepExecution.setAiTokenUsed(stepAiTokenUsed);
                                totalAiTokenUsed += stepAiTokenUsed;
                            } catch (Exception e) {
                                System.err.println("[WARN] 解析aiTokenUsed失败: " + e.getMessage());
                            }
                        }

                        // 解析测试数据
                        String testData = extractJsonValue(stepJson, "testData");
                        if (testData != null && !testData.isEmpty()) {
                            stepExecution.setTestData(testData);
                        }

                        // 解析步骤数据融合耗时
                        String stepFusionDuration = extractJsonValue(stepJson, "stepFusionDuration");
                        if (stepFusionDuration != null && !stepFusionDuration.isEmpty()) {
                            try {
                                stepExecution.setStepFusionDuration(Long.parseLong(stepFusionDuration));
                            } catch (Exception e) {
                                System.err.println("[WARN] 解析stepFusionDuration失败: " + e.getMessage());
                            }
                        }

                        // 解析页面操作耗时
                        String pageOperationDuration = extractJsonValue(stepJson, "pageOperationDuration");
                        if (pageOperationDuration != null && !pageOperationDuration.isEmpty()) {
                            try {
                                stepExecution.setPageOperationDuration(Long.parseLong(pageOperationDuration));
                            } catch (Exception e) {
                                System.err.println("[WARN] 解析pageOperationDuration失败: " + e.getMessage());
                            }
                        }

                        // 解析AI断言耗时
                        String assertionDuration = extractJsonValue(stepJson, "assertionDuration");
                        if (assertionDuration != null && !assertionDuration.isEmpty()) {
                            try {
                                stepExecution.setAssertionDuration(Long.parseLong(assertionDuration));
                            } catch (Exception e) {
                                System.err.println("[WARN] 解析assertionDuration失败: " + e.getMessage());
                            }
                        }

                        // 解析数据融合token消耗
                        String stepFusionTokenUsed = extractJsonValue(stepJson, "stepFusionTokenUsed");
                        if (stepFusionTokenUsed != null && !stepFusionTokenUsed.isEmpty()) {
                            try {
                                stepExecution.setStepFusionTokenUsed(Long.parseLong(stepFusionTokenUsed));
                            } catch (Exception e) {
                                System.err.println("[WARN] 解析stepFusionTokenUsed失败: " + e.getMessage());
                            }
                        }

                        // 解析页面操作token消耗
                        String pageOperationTokenUsed = extractJsonValue(stepJson, "pageOperationTokenUsed");
                        if (pageOperationTokenUsed != null && !pageOperationTokenUsed.isEmpty()) {
                            try {
                                stepExecution.setPageOperationTokenUsed(Long.parseLong(pageOperationTokenUsed));
                            } catch (Exception e) {
                                System.err.println("[WARN] 解析pageOperationTokenUsed失败: " + e.getMessage());
                            }
                        }

                        // 解析AI断言token消耗
                        String assertionTokenUsed = extractJsonValue(stepJson, "assertionTokenUsed");
                        if (assertionTokenUsed != null && !assertionTokenUsed.isEmpty()) {
                            try {
                                stepExecution.setAssertionTokenUsed(Long.parseLong(assertionTokenUsed));
                            } catch (Exception e) {
                                System.err.println("[WARN] 解析assertionTokenUsed失败: " + e.getMessage());
                            }
                        }

                        // 解析融合后的步骤描述
                        String fusedStepDescription = extractJsonValue(stepJson, "fusedStepDescription");
                        if (fusedStepDescription != null && !fusedStepDescription.isEmpty()) {
                            stepExecution.setFusedStepDescription(fusedStepDescription);
                        }

                        // 解析截图并转换为相对路径
                        String screenshot = extractJsonValue(stepJson, "screenshot");
                        if (screenshot != null && !screenshot.isEmpty()) {
                            // 将绝对路径转换为相对路径
                            String basePath = System.getProperty("user.home") + "/.openclaw/workspace/ui-auto-test-platform/screenshots/";
                            if (screenshot.startsWith(basePath)) {
                                screenshot = "/screenshots/" + screenshot.substring(basePath.length());
                            }
                            stepExecution.setScreenshot(screenshot);
                        }

                        // 更新执行记录
                        stepExecutionService.updateById(stepExecution);

                        // 统计通过和失败数量
                        if ("success".equals(stepExecution.getStatus())) {
                            passedCount++;
                        } else if ("failed".equals(stepExecution.getStatus())) {
                            failedCount++;
                        }

                        break;
                    }
                }
            }

            System.err.println("[INFO] 共匹配到" + foundCount + "个步骤的执行结果");

        } catch (Exception e) {
            System.err.println("[ERROR] 解析批量执行结果失败：" + e.getMessage());
            e.printStackTrace();
            allSuccess = false;
        }

        return new long[]{allSuccess ? 1 : 0, totalAiTokenUsed, passedCount, failedCount};
    }

    /**
     * 更新所有未完成的步骤状态为失败
     */
    private void updatePendingStepsToFailed(Long executionId, String errorMessage) {
        try {
            List<TestStepExecution> pendingSteps = stepExecutionService.list(
                new LambdaQueryWrapper<TestStepExecution>()
                    .eq(TestStepExecution::getExecutionId, executionId)
                    .and(wrapper -> wrapper
                        .eq(TestStepExecution::getStatus, "pending")
                        .or()
                        .eq(TestStepExecution::getStatus, "running")
                    )
            );
            if (pendingSteps != null && !pendingSteps.isEmpty()) {
                for (TestStepExecution step : pendingSteps) {
                    step.setStatus("failed");
                    step.setErrorMessage(errorMessage);
                    stepExecutionService.updateById(step);
                }
            }
        } catch (Exception e) {
            System.err.println("[WARN] 更新待执行步骤状态失败: " + e.getMessage());
        }
    }

    /**
     * 从JSON字符串中提取指定字段的值
     */
    private String extractJsonValue(String json, String fieldName) {
        String searchPattern = "\"" + fieldName + "\":";
        int fieldIndex = json.indexOf(searchPattern);
        if (fieldIndex == -1) {
            return null;
        }

        int valueStart = fieldIndex + searchPattern.length();
        // 跳过可能的空格
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }

        if (valueStart >= json.length()) {
            return null;
        }

        char firstChar = json.charAt(valueStart);
        if (firstChar == '"') {
            // 字符串值
            int valueEnd = json.indexOf('"', valueStart + 1);
            if (valueEnd == -1) {
                return null;
            }
            // 处理转义字符
            StringBuilder result = new StringBuilder();
            for (int i = valueStart + 1; i < valueEnd; i++) {
                char c = json.charAt(i);
                if (c == '\\' && i + 1 < valueEnd) {
                    char next = json.charAt(i + 1);
                    if (next == '"') {
                        result.append('"');
                        i++;
                    } else if (next == '\\') {
                        result.append('\\');
                        i++;
                    } else if (next == '/') {
                        result.append('/');
                        i++;
                    } else if (next == 'b') {
                        result.append('\b');
                        i++;
                    } else if (next == 'f') {
                        result.append('\f');
                        i++;
                    } else if (next == 'n') {
                        result.append('\n');
                        i++;
                    } else if (next == 'r') {
                        result.append('\r');
                        i++;
                    } else if (next == 't') {
                        result.append('\t');
                        i++;
                    } else {
                        result.append(c);
                    }
                } else {
                    result.append(c);
                }
            }
            return result.toString();
        } else if (firstChar == '{' || firstChar == '[') {
            // 对象或数组，找到匹配的结束
            int braceCount = 0;
            int bracketCount = 0;
            int i = valueStart;
            while (i < json.length()) {
                char c = json.charAt(i);
                if (c == '{') braceCount++;
                if (c == '}') braceCount--;
                if (c == '[') bracketCount++;
                if (c == ']') bracketCount--;
                if (braceCount == 0 && bracketCount == 0) {
                    break;
                }
                i++;
            }
            return json.substring(valueStart, i + 1);
        } else {
            // 数值或布尔值，找到逗号或}或]
            int valueEnd = valueStart;
            while (valueEnd < json.length()) {
                char c = json.charAt(valueEnd);
                if (c == ',' || c == '}' || c == ']') {
                    break;
                }
                valueEnd++;
            }
            return json.substring(valueStart, valueEnd).trim();
        }
    }

    /**
     * 转义JSON字符串中的特殊字符
     */
    private String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t")
                  .replace("\b", "\\b")
                  .replace("\f", "\\f");
    }
}
