package com.uiauto.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.uiauto.entity.TestTask;
import com.uiauto.service.TestTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 测试任务定时调度器
 * 每分钟检查一次需要执行的定时任务
 */
@Component
public class TestTaskScheduler {

    private static final Logger logger = LoggerFactory.getLogger(TestTaskScheduler.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private TestTaskService testTaskService;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 每分钟执行一次，检查是否有需要执行的定时任务
     * cron: 0 * * * * ? 表示每分钟的第0秒执行
     */
    @Scheduled(cron = "0 * * * * ?")
    public void checkAndExecuteScheduledTasks() {
        logger.info("定时任务检查开始 - {}", LocalDateTime.now().format(FORMATTER));
        
        try {
            // 查询所有启用且有 cron 表达式的任务
            LambdaQueryWrapper<TestTask> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(TestTask::getStatus, 1)
                       .isNotNull(TestTask::getCronExpression)
                       .ne(TestTask::getCronExpression, "");
            
            List<TestTask> tasks = testTaskService.list(queryWrapper);
            
            if (tasks.isEmpty()) {
                logger.info("没有需要执行的定时任务");
                return;
            }
            
            logger.info("找到 {} 个定时任务，开始检查执行时间...", tasks.size());
            
            // 对于每个任务，我们使用一个简单的方式：每分钟检查一次，如果当前分钟匹配 cron 表达式的分钟部分，就执行
            // 注意：这是一个简化实现，实际生产环境应该使用更完善的 cron 表达式解析库
            LocalDateTime now = LocalDateTime.now();
            int currentMinute = now.getMinute();
            int currentHour = now.getHour();
            
            for (TestTask task : tasks) {
                try {
                    if (shouldExecuteNow(task.getCronExpression(), currentMinute, currentHour)) {
                        logger.info("执行定时任务: {} ({})", task.getTaskName(), task.getTaskNo());
                        executeTask(task);
                    }
                } catch (Exception e) {
                    logger.error("执行定时任务失败: {} - {}", task.getTaskName(), e.getMessage(), e);
                }
            }
            
        } catch (Exception e) {
            logger.error("定时任务检查失败", e);
        }
        
        logger.info("定时任务检查结束");
    }

    /**
     * 简化的 cron 表达式检查
     * 只支持简单的分钟和小时检查
     * 格式: 秒 分 时 日 月 周
     */
    private boolean shouldExecuteNow(String cronExpression, int currentMinute, int currentHour) {
        try {
            String[] parts = cronExpression.split("\\s+");
            if (parts.length < 3) {
                return false;
            }
            
            String minutePart = parts[1];
            String hourPart = parts[2];
            
            // 检查分钟
            boolean minuteMatch = matchesCronField(minutePart, currentMinute);
            // 检查小时
            boolean hourMatch = matchesCronField(hourPart, currentHour);
            
            return minuteMatch && hourMatch;
            
        } catch (Exception e) {
            logger.warn("解析 cron 表达式失败: {}", cronExpression, e);
            return false;
        }
    }

    /**
     * 检查 cron 字段是否匹配当前值
     * 支持: * (任意), 具体数字, 0/5 (间隔)
     */
    private boolean matchesCronField(String field, int value) {
        if ("*".equals(field)) {
            return true;
        }
        
        // 处理间隔格式: 0/5
        if (field.contains("/")) {
            String[] intervalParts = field.split("/");
            if (intervalParts.length == 2) {
                try {
                    int start = Integer.parseInt(intervalParts[0]);
                    int interval = Integer.parseInt(intervalParts[1]);
                    return (value - start) % interval == 0;
                } catch (NumberFormatException e) {
                    // 解析失败，尝试直接比较
                }
            }
        }
        
        // 处理具体数字
        try {
            int fieldValue = Integer.parseInt(field);
            return fieldValue == value;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 执行任务
     * 调用后端 API 执行任务
     */
    private void executeTask(TestTask task) {
        try {
            String url = "http://localhost:8088/api/test-task/run/" + task.getId() + "?executionMode=scheduled";
            logger.info("调用任务执行 API: {}", url);
            
            // 发送 POST 请求执行任务
            restTemplate.postForObject(url, null, String.class);
            
            logger.info("任务执行请求已发送: {}", task.getTaskName());
            
        } catch (Exception e) {
            logger.error("执行任务失败: {} - {}", task.getTaskName(), e.getMessage(), e);
        }
    }
}
