
package com.uiauto.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.uiauto.model.Result;
import com.uiauto.entity.TestTaskExecution;
import com.uiauto.entity.TestTaskStepExecution;
import com.uiauto.service.TestTaskExecutionService;
import com.uiauto.service.TestTaskStepExecutionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 测试任务执行历史Controller
 *
 * @author huangzhiyong081439
 * @since 2026-04-13
 */
@Api(tags = "测试任务执行历史管理")
@RestController
@RequestMapping("/api/test-task-execution")
public class TestTaskExecutionController {

    @Autowired
    private TestTaskExecutionService testTaskExecutionService;

    @Autowired
    private TestTaskStepExecutionService testTaskStepExecutionService;

    /**
     * 分页查询任务执行历史
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @param taskId   任务ID（可选）
     * @param keyword  关键词（可选）
     * @return 分页结果
     */
    @ApiOperation("分页查询任务执行历史")
    @GetMapping("/page")
    public Result<Page<TestTaskExecution>> pageQuery(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long taskId,
            @RequestParam(required = false) String keyword) {

        Page<TestTaskExecution> page = testTaskExecutionService.pageQuery(pageNum, pageSize, taskId, keyword);
        return Result.success(page);
    }

    /**
     * 获取任务执行详情
     *
     * @param id 执行记录ID
     * @return 执行记录详情
     */
    @ApiOperation("获取任务执行详情")
    @GetMapping("/{id}")
    public Result<TestTaskExecution> getById(@PathVariable Long id) {
        TestTaskExecution execution = testTaskExecutionService.getById(id);
        if (execution == null) {
            return Result.error("执行记录不存在");
        }
        return Result.success(execution);
    }

    /**
     * 根据任务执行ID查询步骤执行列表
     *
     * @param taskExecutionId 任务执行ID
     * @return 步骤执行列表
     */
    @ApiOperation("根据任务执行ID查询步骤执行列表")
    @GetMapping("/{taskExecutionId}/steps")
    public Result<List<TestTaskStepExecution>> listStepsByTaskExecutionId(@PathVariable Long taskExecutionId) {
        List<TestTaskStepExecution> steps = testTaskStepExecutionService.listByTaskExecutionId(taskExecutionId);
        return Result.success(steps);
    }
}

