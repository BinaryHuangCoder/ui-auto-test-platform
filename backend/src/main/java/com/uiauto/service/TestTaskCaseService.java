
package com.uiauto.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.uiauto.entity.TestTaskCase;

import java.util.List;

/**
 * 测试任务与用例关联Service接口
 *
 * @author huangzhiyong081439
 * @since 2026-04-13
 */
public interface TestTaskCaseService extends IService<TestTaskCase> {

    /**
     * 根据任务ID查询关联的用例ID列表
     *
     * @param taskId 任务ID
     * @return 用例ID列表
     */
    List<Long> getCaseIdsByTaskId(Long taskId);

    /**
     * 批量保存任务与用例的关联关系
     *
     * @param taskId  任务ID
     * @param caseIds 用例ID列表
     */
    void batchSave(Long taskId, List<Long> caseIds);

    /**
     * 删除任务的所有用例关联
     *
     * @param taskId 任务ID
     */
    void deleteByTaskId(Long taskId);
}

