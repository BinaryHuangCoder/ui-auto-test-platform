
package com.uiauto.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.uiauto.entity.TestTaskStepExecution;
import com.uiauto.mapper.TestTaskStepExecutionMapper;
import com.uiauto.service.TestTaskStepExecutionService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 测试任务执行步骤详情Service实现类
 *
 * @author huangzhiyong081439
 * @since 2026-04-13
 */
@Service
public class TestTaskStepExecutionServiceImpl extends ServiceImpl<TestTaskStepExecutionMapper, TestTaskStepExecution> implements TestTaskStepExecutionService {

    @Override
    public List<TestTaskStepExecution> listByTaskExecutionId(Long taskExecutionId) {
        LambdaQueryWrapper<TestTaskStepExecution> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TestTaskStepExecution::getTaskExecutionId, taskExecutionId);
        wrapper.orderByAsc(TestTaskStepExecution::getStepNo);
        return this.list(wrapper);
    }
}

