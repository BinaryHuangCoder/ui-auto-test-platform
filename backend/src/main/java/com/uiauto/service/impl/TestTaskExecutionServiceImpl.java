
package com.uiauto.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.uiauto.entity.TestTaskExecution;
import com.uiauto.mapper.TestTaskExecutionMapper;
import com.uiauto.service.TestTaskExecutionService;
import org.springframework.stereotype.Service;

/**
 * 测试任务执行历史Service实现类
 *
 * @author huangzhiyong081439
 * @since 2026-04-13
 */
@Service
public class TestTaskExecutionServiceImpl extends ServiceImpl<TestTaskExecutionMapper, TestTaskExecution> implements TestTaskExecutionService {

    @Override
    public Page<TestTaskExecution> pageQuery(Integer pageNum, Integer pageSize, Long taskId, String keyword) {
        Page<TestTaskExecution> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<TestTaskExecution> wrapper = new LambdaQueryWrapper<>();

        // 任务ID过滤
        if (taskId != null) {
            wrapper.eq(TestTaskExecution::getTaskId, taskId);
        }

        // 关键词搜索
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w
                .like(TestTaskExecution::getTaskNo, keyword)
                .or()
                .like(TestTaskExecution::getTaskName, keyword)
                .or()
                .like(TestTaskExecution::getExecutor, keyword)
            );
        }

        // 按创建时间倒序排列
        wrapper.orderByDesc(TestTaskExecution::getCreateTime);

        return this.page(page, wrapper);
    }
}

