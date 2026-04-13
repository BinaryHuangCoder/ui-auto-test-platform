package com.uiauto.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.uiauto.entity.TestTaskCase;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 测试任务关联测试用例Mapper接口
 * 
 * @author huangzhiyong081439
 * @date 2026-04-10
 */
@Mapper
public interface TestTaskCaseMapper extends BaseMapper<TestTaskCase> {
    
    /**
     * 根据任务ID查询关联的测试用例ID列表
     * 
     * @param taskId 任务ID
     * @return 测试用例ID列表
     */
    List<Long> selectCaseIdsByTaskId(@Param("taskId") Long taskId);
    
    /**
     * 根据任务ID删除所有关联关系
     * 
     * @param taskId 任务ID
     * @return 删除数量
     */
    int deleteByTaskId(@Param("taskId") Long taskId);
    
    /**
     * 检查用例是否已关联到任务
     * 
     * @param taskId 任务ID
     * @param caseId 用例ID
     * @return 数量
     */
    int countByTaskIdAndCaseId(@Param("taskId") Long taskId, @Param("caseId") Long caseId);
}