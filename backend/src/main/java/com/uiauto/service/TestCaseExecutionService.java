package com.uiauto.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.uiauto.entity.TestCaseExecution;

public interface TestCaseExecutionService extends IService<TestCaseExecution> {
    /**
     * 执行单个测试用例
     * @param caseId 测试用例ID
     * @param executor 执行人
     * @param saveTokenMode 保存token模式（"standard" 或 "save"）
     * @param taskExecutionId 任务执行ID（可选，为null时表示测试用例直接执行）
     * @return 用例执行记录的AI总token消耗
     */
    Long executeTestCase(Long caseId, String executor, String saveTokenMode, Long taskExecutionId);
}
