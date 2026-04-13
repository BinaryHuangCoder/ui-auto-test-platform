package com.uiauto.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 测试任务执行记录实体类
 * 记录测试任务的每次执行，用于查看历史执行记录
 * 
 * @author huangzhiyong081439
 * @date 2026-04-10
 */
@Data
@TableName("test_task_execution")
public class TestTaskExecution {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 任务ID
     */
    private Long taskId;
    
    /**
     * 任务名称（非数据库字段，用于前端显示）
     */
    @TableField(exist = false)
    private String taskName;
    
    /**
     * 任务编号（非数据库字段，用于前端显示）
     */
    @TableField(exist = false)
    private String taskNo;
    
    /**
     * 测试用例ID
     */
    private Long caseId;
    
    /**
     * 测试用例名称（非数据库字段，用于前端显示）
     */
    @TableField(exist = false)
    private String caseName;
    
    /**
     * 测试用例执行记录ID（关联test_case_execution表）
     */
    private Long executionId;
    
    /**
     * 执行人用户名
     */
    private String executor;
    
    /**
     * 执行人昵称（非数据库字段，用于前端显示）
     */
    @TableField(exist = false)
    private String executorNickname;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 执行耗时（毫秒）
     */
    private Long duration;
    
    /**
     * 执行状态（running/success/failed）
     */
    private String status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}