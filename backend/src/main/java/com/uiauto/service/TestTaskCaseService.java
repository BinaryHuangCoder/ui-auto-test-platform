package com.uiauto.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.uiauto.entity.TestTaskCase;
import java.util.List;

/**
 * 测试任务关联测试用例Service接口
 * 
 * @author huangzhiyong081439
 * @date 2026-04-10
 */
public interface TestTaskCaseService extends IService<TestTaskCase> {
    
    /**
     * 批量添加测试用例到任务
     * 
     * @param taskId 任务ID
     * @param caseIds 测试用例ID列表
     * @return 是否成功
     */
    boolean batchAddCases(Long taskId, List<Long> caseIds);
    
    /**
     * 批量删除任务关联的测试用例
     * 
     * @param taskId 任务ID
     * @return 是否成功
     */
    boolean batchRemoveCases(Long taskId);
    
    /**
     * 获取任务关联的测试用例ID列表
     * 
     * @param taskId 任务ID
     * @return 测试用例ID列表
     */
    List<Long> getCaseIdsByTaskId(Long taskId);
    
    /**
     * 获取任务关联的测试用例列表（带用例名称）
     * 
     * @param taskId 任务ID
     * @return 测试用例列表
     */
    List<TestTaskCase> getCaseListByTaskId(Long taskId);
}