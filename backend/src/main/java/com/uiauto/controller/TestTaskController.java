
package com.uiauto.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.uiauto.entity.TestCase;
import com.uiauto.entity.TestCaseExecution;
import com.uiauto.entity.TestCaseStep;
import com.uiauto.entity.TestStepExecution;
import com.uiauto.entity.TestTask;
import com.uiauto.entity.TestTaskCase;
import com.uiauto.entity.TestTaskExecution;
import com.uiauto.entity.User;
import com.uiauto.model.Result;
import com.uiauto.service.TestCaseExecutionService;
import com.uiauto.service.TestCaseService;
import com.uiauto.service.TestCaseStepService;
import com.uiauto.service.TestStepExecutionService;
import com.uiauto.service.TestTaskCaseService;
import com.uiauto.service.ModelScenarioService;
import com.uiauto.service.ModelService;
import com.uiauto.service.TestTaskExecutionService;
import com.uiauto.service.TestTaskService;
import com.uiauto.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 测试任务Controller
 * 
 * @author huangzhiyong081439
 * @date 2026-04-10
 */
@Api(tags = "测试任务管理")
@RestController
@RequestMapping("/api/test-task")
public class TestTaskController {
    
    @Autowired
    private TestTaskService testTaskService;
    
    @Autowired
    private TestTaskCaseService testTaskCaseService;
    
    @Autowired
    private TestCaseService testCaseService;
    
    @Autowired
    private TestCaseStepService testCaseStepService;
    
    @Autowired
    private TestCaseExecutionService testCaseExecutionService;
    
    @Autowired
    private TestStepExecutionService testStepExecutionService;
    
    @Autowired
    private TestTaskExecutionService testTaskExecutionService;
    
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
     * 分页查询测试任务
     */
    @ApiOperation("分页查询测试任务")
    @GetMapping("/page")
    public Result<Page<TestTask>> pageQuery(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        
        Page<TestTask> page = testTaskService.pageQuery(pageNum, pageSize, keyword);
        return Result.success(page);
    }
    
    /**
     * 获取单个测试任务详情
     */
    @ApiOperation("获取测试任务详情")
    @GetMapping("/{id}")
    public Result<TestTask> getById(@PathVariable Long id) {
        TestTask task = testTaskService.getById(id);
        if (task == null) {
            return Result.error("任务不存在");
        }
        return Result.success(task);
    }
    
    /**
     * 新增测试任务
     */
    @ApiOperation("新增测试任务")
    @PostMapping
    public Result<TestTask> add(@RequestBody TestTask testTask) {
        // 生成唯一任务编号
        String taskNo = testTaskService.generateTaskNo();
        testTask.setTaskNo(taskNo);
        
        // 设置创建人
        testTask.setCreator("admin");
        
        // 设置状态默认为启用
        if (testTask.getStatus() == null) {
            testTask.setStatus(1);
        }
        
        // 设置并发数默认为1，范围1-10
        if (testTask.getConcurrency() == null) {
            testTask.setConcurrency(1);
        } else if (testTask.getConcurrency() < 1) {
            testTask.setConcurrency(1);
        } else if (testTask.getConcurrency() > 10) {
            testTask.setConcurrency(10);
        }
        
        // 设置时间
        LocalDateTime now = LocalDateTime.now();
        testTask.setCreateTime(now);
        testTask.setUpdateTime(now);
        
        boolean success = testTaskService.save(testTask);
        if (success) {
            return Result.success(testTask);
        } else {
            return Result.error("新增失败");
        }
    }
    
    /**
     * 更新测试任务
     */
    @ApiOperation("更新测试任务")
    @PutMapping
    public Result<TestTask> update(@RequestBody TestTask testTask) {
        if (testTask.getId() == null) {
            return Result.error("ID不能为空");
        }
        
        // 设置更新时间
        testTask.setUpdateTime(LocalDateTime.now());
        
        // 不允许修改任务编号和创建人
        testTask.setTaskNo(null);
        testTask.setCreator(null);
        
        // 验证并发数范围1-10
        if (testTask.getConcurrency() != null) {
            if (testTask.getConcurrency() < 1) {
                testTask.setConcurrency(1);
            } else if (testTask.getConcurrency() > 10) {
                testTask.setConcurrency(10);
            }
        }
        
        boolean success = testTaskService.updateById(testTask);
        if (success) {
            return Result.success(testTaskService.getById(testTask.getId()));
        } else {
            return Result.error("更新失败");
        }
    }
    
    /**
     * 删除测试任务
     */
    @ApiOperation("删除测试任务")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = testTaskService.removeById(id);
        if (success) {
            return Result.success();
        } else {
            return Result.error("删除失败");
        }
    }
    
    /**
     * 批量删除测试任务
     */
    @ApiOperation("批量删除测试任务")
    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.error("请选择要删除的任务");
        }
        
        boolean success = testTaskService.removeByIds(ids);
        if (success) {
            return Result.success();
        } else {
            return Result.error("批量删除失败");
        }
    }

    /**
     * 获取任务关联的用例ID列表
     *
     * @param taskId 任务ID
     * @return 用例ID列表
     */
    @ApiOperation("获取任务关联的用例ID列表")
    @GetMapping("/{taskId}/case-ids")
    public Result<List<Long>> getCaseIdsByTaskId(@PathVariable Long taskId) {
        List<Long> caseIds = testTaskCaseService.getCaseIdsByTaskId(taskId);
        return Result.success(caseIds);
    }

    /**
     * 批量保存任务与用例的关联关系
     *
     * @param taskId  任务ID
     * @param caseIds 用例ID列表
     * @return 操作结果
     */
    @ApiOperation("批量保存任务与用例的关联关系")
    @PostMapping("/{taskId}/case-ids")
    public Result<Void> batchSaveCaseIds(
            @PathVariable Long taskId,
            @RequestBody List<Long> caseIds) {
        testTaskCaseService.batchSave(taskId, caseIds);
        return Result.success();
    }

    /**
     * 立刻执行单个测试任务
     *
     * @param taskId 任务ID
     * @param executionMode 执行方式：manual-手工触发，scheduled-定时触发（可选，默认manual）
     * @return 执行结果
     */
    @ApiOperation("立刻执行单个测试任务")
    @PostMapping("/run/{taskId}")
    public Result<Void> runTask(@PathVariable Long taskId, @RequestParam(required = false, defaultValue = "manual") String executionMode) {
        TestTask task = testTaskService.getById(taskId);
        if (task == null) {
            return Result.error("任务不存在");
        }

        List<Long> caseIds = testTaskCaseService.getCaseIdsByTaskId(taskId);
        if (caseIds == null || caseIds.isEmpty()) {
            return Result.error("任务没有关联用例");
        }
        
        // 日志调试
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(TestTaskController.class);
        logger.info("执行任务: {}, 并发数: {}, 关联用例数: {}, 执行方式: {}", 
            task.getTaskName(), task.getConcurrency(), caseIds.size(), executionMode);

        // 创建任务执行记录
        TestTaskExecution taskExecution = new TestTaskExecution();
        taskExecution.setTaskId(taskId);
        taskExecution.setTaskName(task.getTaskName());
        taskExecution.setExecutor("admin");
        taskExecution.setExecutionMode(executionMode);
        taskExecution.setExecuteTime(LocalDateTime.now());
        taskExecution.setStartTime(LocalDateTime.now());
        taskExecution.setStatus("running");
        taskExecution.setTotalCount(caseIds.size());
        taskExecution.setPassedCount(0);
        taskExecution.setFailedCount(0);
        testTaskExecutionService.save(taskExecution);

        // 创建一个 effectively final 的引用
        final TestTaskExecution finalTaskExecutionRef = taskExecution;
        final List<Long> finalCaseIds = caseIds;
        
        // 获取并发数，默认为1
        int concurrency = task.getConcurrency() != null ? task.getConcurrency() : 1;
        if (concurrency < 1) concurrency = 1;
        if (concurrency > 10) concurrency = 10;
        final int finalConcurrency = concurrency;
        
        // 异步执行所有关联用例（支持并发）
        CompletableFuture.runAsync(() -> {
            // 用于累加所有用例的AI总token消耗
            AtomicLong totalTaskAiTokenUsed = new AtomicLong(0);
            // 用于统计通过和失败的用例数
            AtomicInteger passedCount = new AtomicInteger(0);
            AtomicInteger failedCount = new AtomicInteger(0);
            
            // 创建线程池，使用固定大小的线程池
            ExecutorService executorService = Executors.newFixedThreadPool(finalConcurrency);
            
            try {
                // 提交所有用例到线程池并发执行
                List<CompletableFuture<Void>> futures = new ArrayList<>();
                for (Long caseId : finalCaseIds) {
                    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                        try {
                            Long caseAiTokenUsed = testCaseExecutionService.executeTestCase(
                                caseId, "admin", "standard", finalTaskExecutionRef.getId());
                            totalTaskAiTokenUsed.addAndGet(caseAiTokenUsed);
                            passedCount.incrementAndGet();
                        } catch (Exception e) {
                            e.printStackTrace();
                            failedCount.incrementAndGet();
                        }
                        // 实时更新任务执行记录的进度
                        finalTaskExecutionRef.setPassedCount(passedCount.get());
                        finalTaskExecutionRef.setFailedCount(failedCount.get());
                        testTaskExecutionService.updateById(finalTaskExecutionRef);
                    }, executorService);
                    futures.add(future);
                }
                
                // 等待所有用例执行完成
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

                // 更新任务执行记录为成功
                finalTaskExecutionRef.setEndTime(LocalDateTime.now());
                finalTaskExecutionRef.setDuration(java.time.Duration.between(
                    finalTaskExecutionRef.getStartTime(), finalTaskExecutionRef.getEndTime()).toMillis());
                finalTaskExecutionRef.setStatus("success");
                finalTaskExecutionRef.setAiTotalTokenUsed(totalTaskAiTokenUsed.get());
                testTaskExecutionService.updateById(finalTaskExecutionRef);

            } catch (Exception e) {
                e.printStackTrace();
                // 更新任务执行记录为失败
                finalTaskExecutionRef.setEndTime(LocalDateTime.now());
                finalTaskExecutionRef.setDuration(java.time.Duration.between(
                    finalTaskExecutionRef.getStartTime(), finalTaskExecutionRef.getEndTime()).toMillis());
                finalTaskExecutionRef.setStatus("failed");
                finalTaskExecutionRef.setAiTotalTokenUsed(totalTaskAiTokenUsed.get());
                testTaskExecutionService.updateById(finalTaskExecutionRef);
            } finally {
                // 关闭线程池
                executorService.shutdown();
                try {
                    if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                        executorService.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    executorService.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
        });

        return Result.success();
    }

    /**
     * 批量立刻执行测试任务
     *
     * @param taskIds 任务ID列表
     * @return 执行结果
     */
    @ApiOperation("批量立刻执行测试任务")
    @PostMapping("/run/batch")
    public Result<Void> runBatchTasks(@RequestBody List<Long> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) {
            return Result.error("请选择要执行的任务");
        }

        for (Long taskId : taskIds) {
            CompletableFuture.runAsync(() -> {
                try {
                    runTask(taskId, "manual");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        return Result.success();
    }

    /**
     * 将所有pending状态的步骤更新为failed状态
     * 
     * @param executionId 执行记录ID
     */
    private void updatePendingStepsToFailed(Long executionId) {
        List<TestStepExecution> pendingSteps = testStepExecutionService.list(
            new LambdaQueryWrapper<TestStepExecution>()
                .eq(TestStepExecution::getExecutionId, executionId)
                .eq(TestStepExecution::getStatus, "pending")
        );
        
        for (TestStepExecution step : pendingSteps) {
            step.setStatus("failed");
            step.setAiResult("- 执行异常，未完成");
            testStepExecutionService.updateById(step);
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
        int keyPos = json.indexOf(searchKey);
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
     * @return 执行器返回的结果
     * @throws Exception 执行失败时抛出异常
     */
    private String executeStepsBatch(String executorPath, String stepsJson) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
            nodePath,
            executorPath,
            stepsJson
        );
        pb.directory(new java.io.File(scriptsDir));
        pb.environment().put("PLAYWRIGHT_BROWSERS_PATH", playwrightBrowsersPath);
        pb.redirectErrorStream(true);
        
        Process process = pb.start();
        StringBuilder output = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                System.err.println("[Executor Output] " + line);
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
                        TestStepExecution stepExecution = testStepExecutionService.getOne(
                            new LambdaQueryWrapper<TestStepExecution>()
                                .eq(TestStepExecution::getExecutionId, executionId)
                                .eq(TestStepExecution::getStepNo, step.getStepNo())
                        );
                        
                        if (stepExecution == null) {
                            System.err.println("[WARN] 未找到步骤" + step.getStepNo() + "的执行记录");
                            continue;
                        }
                        
                        // 提取并更新字段
                        String executionStatus = extractJsonValue(stepJson, "executionStatus");
                        String assertionStatus = extractJsonValue(stepJson, "assertionStatus");
                        String message = extractJsonValue(stepJson, "message");
                        String screenshot = extractJsonValue(stepJson, "screenshot");
                        String aiTokenUsedStr = extractJsonValue(stepJson, "aiTokenUsed");
                        String executionTimeStr = extractJsonValue(stepJson, "executionTime");
                        String startTimeStr = extractJsonValue(stepJson, "startTime");
                        String endTimeStr = extractJsonValue(stepJson, "endTime");
                        
                        // 更新状态
                        if ("成功".equals(executionStatus)) {
                            stepExecution.setStatus("success");
                        } else if ("失败".equals(executionStatus)) {
                            stepExecution.setStatus("failed");
                            allSuccess = false;
                        } else if ("执行中".equals(executionStatus)) {
                            stepExecution.setStatus("running");
                        } else {
                            stepExecution.setStatus("failed");
                            allSuccess = false;
                        }
                        
                        // 更新断言状态
                        if ("成功".equals(assertionStatus)) {
                            stepExecution.setAssertionStatus("success");
                        } else if ("失败".equals(assertionStatus)) {
                            stepExecution.setAssertionStatus("failed");
                        } else if ("警告".equals(assertionStatus)) {
                            stepExecution.setAssertionStatus("warning");
                        } else {
                            stepExecution.setAssertionStatus("none");
                        }
                        
                        // 更新其他字段
                        if (message != null) {
                            stepExecution.setAiResult(message);
                        }
                        if (aiTokenUsedStr != null) {
                            try {
                                long tokens = Long.parseLong(aiTokenUsedStr);
                                stepExecution.setAiTokenUsed(tokens);
                                totalAiTokenUsed += tokens;
                            } catch (NumberFormatException e) {
                                // 忽略
                            }
                        }
                        if (executionTimeStr != null && !executionTimeStr.isEmpty()) {
                            try {
                                stepExecution.setDuration(Long.parseLong(executionTimeStr));
                            } catch (Exception e) {
                                stepExecution.setDuration(0L);
                            }
                        } else {
                            stepExecution.setDuration(0L);
                        }
                        if (startTimeStr != null) {
                            try {
                                stepExecution.setStartTime(java.time.LocalDateTime.ofInstant(
                                    java.time.Instant.ofEpochMilli(Long.parseLong(startTimeStr)),
                                    java.time.ZoneId.systemDefault()));
                            } catch (NumberFormatException e) {
                                // 忽略
                            }
                        }
                        if (endTimeStr != null) {
                            try {
                                stepExecution.setEndTime(java.time.LocalDateTime.ofInstant(
                                    java.time.Instant.ofEpochMilli(Long.parseLong(endTimeStr)),
                                    java.time.ZoneId.systemDefault()));
                            } catch (NumberFormatException e) {
                                // 忽略
                            }
                        }
                        // 设置截图 - 读取screenshotBase64（file:开头的路径），去掉file:前缀，并转换为URL路径
                        String screenshotBase64 = extractJsonValue(stepJson, "screenshotBase64");
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
                        
                        // 保存更新
                        testStepExecutionService.updateById(stepExecution);
                        System.err.println("[INFO] 步骤" + step.getStepNo() + "更新完成，状态：" + stepExecution.getStatus());
                        
                        break;
                    }
                }
                
                if (!found) {
                    System.err.println("[WARN] 未找到步骤" + step.getStepNo() + "的结果，标记为失败");
                    // 未找到该步骤结果，标记为失败
                    TestStepExecution stepExecution = testStepExecutionService.getOne(
                        new LambdaQueryWrapper<TestStepExecution>()
                            .eq(TestStepExecution::getExecutionId, executionId)
                            .eq(TestStepExecution::getStepNo, step.getStepNo())
                    );
                    if (stepExecution != null) {
                        stepExecution.setStatus("failed");
                        stepExecution.setAiResult("- 未找到执行结果");
                        testStepExecutionService.updateById(stepExecution);
                    }
                    allSuccess = false;
                }
            }
            
            System.err.println("[INFO] 共更新" + foundCount + "个步骤的结果");
            
        } catch (Exception e) {
            System.err.println("[ERROR] 解析执行结果失败：" + e.getMessage());
            e.printStackTrace();
            allSuccess = false;
        }
        
        return new long[]{allSuccess ? 1 : 0, totalAiTokenUsed};
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
}

