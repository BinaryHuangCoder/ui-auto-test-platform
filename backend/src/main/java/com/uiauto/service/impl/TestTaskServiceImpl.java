package com.uiauto.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.uiauto.entity.TestTask;
import com.uiauto.entity.TestTaskExecution;
import com.uiauto.entity.User;
import com.uiauto.mapper.TestTaskMapper;
import com.uiauto.service.TestTaskExecutionService;
import com.uiauto.service.TestTaskService;
import com.uiauto.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 测试任务Service实现类
 * 
 * @author huangzhiyong081439
 * @date 2026-04-10
 */
@Service
public class TestTaskServiceImpl extends ServiceImpl<TestTaskMapper, TestTask> implements TestTaskService {
    
    @Autowired
    private UserService userService;

    @Autowired
    private TestTaskExecutionService testTaskExecutionService;
    
    @Override
    public Page<TestTask> pageQuery(Integer pageNum, Integer pageSize, String keyword) {
        Page<TestTask> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<TestTask> wrapper = new LambdaQueryWrapper<>();
        
        // 关键词搜索
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w
                .like(TestTask::getTaskNo, keyword)
                .or()
                .like(TestTask::getTaskName, keyword)
                .or()
                .like(TestTask::getCreator, keyword)
            );
        }
        
        // 按创建时间倒序排列
        wrapper.orderByDesc(TestTask::getCreateTime);
        
        Page<TestTask> resultPage = this.page(page, wrapper);
        
        // 填充创建人昵称和最新执行时间
        List<TestTask> records = resultPage.getRecords();
        if (records != null && !records.isEmpty()) {
            // 填充创建人昵称
            List<String> usernames = records.stream()
                .map(TestTask::getCreator)
                .filter(username -> username != null && !username.isEmpty())
                .distinct()
                .collect(Collectors.toList());
            
            if (!usernames.isEmpty()) {
                LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
                userWrapper.in(User::getUsername, usernames);
                List<User> users = userService.list(userWrapper);
                
                Map<String, String> usernameToNickname = users.stream()
                    .collect(Collectors.toMap(User::getUsername, User::getNickname, (v1, v2) -> v1));
                
                for (TestTask task : records) {
                    if (task.getCreator() != null) {
                        task.setCreatorNickname(usernameToNickname.get(task.getCreator()));
                    }
                }
            }

            // 填充最新执行时间
            List<Long> taskIds = records.stream()
                .map(TestTask::getId)
                .filter(id -> id != null)
                .collect(Collectors.toList());

            if (!taskIds.isEmpty()) {
                // 为每个任务获取最新的执行记录
                LambdaQueryWrapper<TestTaskExecution> executionWrapper = new LambdaQueryWrapper<>();
                executionWrapper.in(TestTaskExecution::getTaskId, taskIds);
                executionWrapper.orderByDesc(TestTaskExecution::getExecuteTime);
                List<TestTaskExecution> executions = testTaskExecutionService.list(executionWrapper);

                // 按 taskId 分组，取每个 taskId 的第一条（最新的）
                Map<Long, TestTaskExecution> latestExecutionMap = executions.stream()
                    .collect(Collectors.toMap(
                        TestTaskExecution::getTaskId,
                        e -> e,
                        (existing, replacement) -> existing // 保留第一条
                    ));

                for (TestTask task : records) {
                    TestTaskExecution latestExecution = latestExecutionMap.get(task.getId());
                    if (latestExecution != null) {
                        task.setLatestExecuteTime(latestExecution.getExecuteTime());
                    }
                }
            }
        }
        
        return resultPage;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String generateTaskNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "TASK" + dateStr;
        
        // 查询今天已有的任务数量
        LambdaQueryWrapper<TestTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(TestTask::getTaskNo, prefix);
        wrapper.orderByDesc(TestTask::getTaskNo);
        wrapper.last("LIMIT 1");
        
        TestTask lastTask = this.getOne(wrapper);
        
        int sequence = 1;
        if (lastTask != null && lastTask.getTaskNo() != null) {
            String lastNo = lastTask.getTaskNo();
            String lastSeq = lastNo.substring(prefix.length());
            try {
                sequence = Integer.parseInt(lastSeq) + 1;
            } catch (NumberFormatException e) {
                sequence = 1;
            }
        }
        
        return prefix + String.format("%04d", sequence);
    }
}
