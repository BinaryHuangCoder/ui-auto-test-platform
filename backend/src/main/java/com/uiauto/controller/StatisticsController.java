
package com.uiauto.controller;

import com.uiauto.entity.TestCase;
import com.uiauto.entity.TestCaseExecution;
import com.uiauto.entity.TestTask;
import com.uiauto.mapper.TestCaseExecutionMapper;
import com.uiauto.service.TestCaseExecutionService;
import com.uiauto.service.TestCaseService;
import com.uiauto.service.TestTaskService;
import com.uiauto.model.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计数据控制器
 * 
 * @author system
 * @since 2026-04-14
 */
@Api(tags = "统计数据管理")
@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Autowired
    private TestCaseService testCaseService;

    @Autowired
    private TestCaseExecutionService testCaseExecutionService;

    @Autowired
    private TestCaseExecutionMapper testCaseExecutionMapper;

    @Autowired
    private TestTaskService testTaskService;

    /**
     * 获取首页统计数据
     * 
     * @return 统计数据
     */
    @ApiOperation("获取首页统计数据")
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // 测试用例总数
        long totalCaseCount = testCaseService.count();
        stats.put("totalCaseCount", totalCaseCount);

        // 获取每个用例的最新执行记录
        List<TestCaseExecution> latestExecutions = testCaseExecutionMapper.getLatestExecutions();

        // 通过用例数（最近一次执行成功的用例数）
        long successCaseCount = latestExecutions.stream()
                .filter(e -> "success".equals(e.getStatus()))
                .count();
        stats.put("successCaseCount", successCaseCount);

        // 失败用例数（最近一次执行失败的用例数）
        long failedCaseCount = latestExecutions.stream()
                .filter(e -> "failed".equals(e.getStatus()))
                .count();
        stats.put("failedCaseCount", failedCaseCount);

        // 运行中任务数（暂时用启用的任务数）
        long runningTaskCount = testTaskService.count(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TestTask>()
                .eq(TestTask::getStatus, 1)
        );
        stats.put("runningTaskCount", runningTaskCount);

        return Result.success(stats);
    }
}

