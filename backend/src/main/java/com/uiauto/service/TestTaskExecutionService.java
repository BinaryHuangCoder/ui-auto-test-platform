
package com.uiauto.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.uiauto.entity.TestTaskExecution;

/**
 * 测试任务执行历史Service接口
 *
 * @author huangzhiyong081439
 * @since 2026-04-13
 */
public interface TestTaskExecutionService extends IService<TestTaskExecution> {

    /**
     * 分页查询任务执行历史
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @param taskId   任务ID（可选）
     * @param keyword  关键词（可选）
     * @return 分页结果
     */
    Page<TestTaskExecution> pageQuery(Integer pageNum, Integer pageSize, Long taskId, String keyword);
}

