package com.uiauto.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.uiauto.model.Result;
import com.uiauto.entity.TestTask;
import com.uiauto.service.TestTaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

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
}
