
package com.uiauto.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.uiauto.entity.TestTaskExecution;
import com.uiauto.entity.User;
import com.uiauto.mapper.TestTaskExecutionMapper;
import com.uiauto.service.TestTaskExecutionService;
import com.uiauto.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 测试任务执行历史Service实现类
 *
 * @author huangzhiyong081439
 * @since 2026-04-13
 */
@Service
public class TestTaskExecutionServiceImpl extends ServiceImpl<TestTaskExecutionMapper, TestTaskExecution> implements TestTaskExecutionService {

    @Autowired
    private UserService userService;

    @Override
    public Page<TestTaskExecution> pageQuery(Integer pageNum, Integer pageSize, Long taskId, String keyword) {
        Page<TestTaskExecution> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<TestTaskExecution> wrapper = new LambdaQueryWrapper<>();

        // 任务ID过滤
        if (taskId != null) {
            wrapper.eq(TestTaskExecution::getTaskId, taskId);
        }

        // 关键词搜索
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w
                .like(TestTaskExecution::getTaskNo, keyword)
                .or()
                .like(TestTaskExecution::getTaskName, keyword)
                .or()
                .like(TestTaskExecution::getExecutor, keyword)
            );
        }

        // 按id倒序排列（id自增，最新的在前）
        wrapper.orderByDesc(TestTaskExecution::getId);

        Page<TestTaskExecution> resultPage = this.page(page, wrapper);

        // 填充执行人昵称
        List<TestTaskExecution> records = resultPage.getRecords();
        if (records != null && !records.isEmpty()) {
            List<String> usernames = records.stream()
                .map(TestTaskExecution::getExecutor)
                .filter(username -> username != null && !username.isEmpty())
                .distinct()
                .collect(Collectors.toList());

            if (!usernames.isEmpty()) {
                LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
                userWrapper.in(User::getUsername, usernames);
                List<User> users = userService.list(userWrapper);

                Map<String, String> usernameToNickname = users.stream()
                    .collect(Collectors.toMap(User::getUsername, User::getNickname, (v1, v2) -> v1));

                for (TestTaskExecution execution : records) {
                    if (execution.getExecutor() != null) {
                        execution.setExecutorNickname(usernameToNickname.get(execution.getExecutor()));
                    }
                }
            }
        }

        return resultPage;
    }
}

