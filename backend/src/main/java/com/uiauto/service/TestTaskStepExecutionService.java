
package com.uiauto.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.uiauto.entity.TestTaskStepExecution;

import java.util.List;

/**
 * 测试任务执行步骤详情Service接口
 *
 * @author huangzhiyong081439
 * @since 2026-04-13
 */
public interface TestTaskStepExecutionService extends IService<TestTaskStepExecution> {

    /**
     * 根据任务执行ID查询步骤执行列表
     *
     * @param taskExecutionId 任务执行ID
     * @return 步骤执行列表
     */
    List<TestTaskStepExecution> listByTaskExecutionId(Long taskExecutionId);
}

