package com.uiauto.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.uiauto.entity.TestTaskExecution;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 测试任务执行记录Mapper接口
 * 
 * @author huangzhiyong081439
 * @date 2026-04-10
 */
@Mapper
public interface TestTaskExecutionMapper extends BaseMapper<TestTaskExecution> {
    
    /**
     * 分页查询任务执行记录（带关联信息）
     * 
     * @param page 分页对象
     * @param taskId 任务ID（可选）
     * @return 分页结果
     */
    IPage<TestTaskExecution> selectPageWithDetails(Page<?> page, @Param("taskId") Long taskId);
    
    /**
     * 根据任务ID查询执行记录数量
     * 
     * @param taskId 任务ID
     * @return 执行记录数量
     */
    int countByTaskId(@Param("taskId") Long taskId);
}