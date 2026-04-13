
package com.uiauto.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.uiauto.entity.TestTaskCase;
import com.uiauto.mapper.TestTaskCaseMapper;
import com.uiauto.service.TestTaskCaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 测试任务与用例关联Service实现类
 *
 * @author huangzhiyong081439
 * @since 2026-04-13
 */
@Service
public class TestTaskCaseServiceImpl extends ServiceImpl<TestTaskCaseMapper, TestTaskCase> implements TestTaskCaseService {

    @Override
    public List<Long> getCaseIdsByTaskId(Long taskId) {
        LambdaQueryWrapper<TestTaskCase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TestTaskCase::getTaskId, taskId);
        List<TestTaskCase> list = this.list(wrapper);
        return list.stream().map(TestTaskCase::getCaseId).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSave(Long taskId, List<Long> caseIds) {
        // 先删除旧的关联
        deleteByTaskId(taskId);

        // 批量保存新的关联
        if (caseIds != null && !caseIds.isEmpty()) {
            List<TestTaskCase> taskCaseList = caseIds.stream().map(caseId -> {
                TestTaskCase taskCase = new TestTaskCase();
                taskCase.setTaskId(taskId);
                taskCase.setCaseId(caseId);
                return taskCase;
            }).collect(Collectors.toList());
            this.saveBatch(taskCaseList);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByTaskId(Long taskId) {
        LambdaQueryWrapper<TestTaskCase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TestTaskCase::getTaskId, taskId);
        this.remove(wrapper);
    }
}

