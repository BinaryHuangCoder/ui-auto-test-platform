package com.uiauto.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 测试任务实体类
 * 
 * @author huangzhiyong081439
 * @date 2026-04-10
 * @update 2026-04-13 完善字段，对齐数据库
 */
@Data
@TableName("test_task")
public class TestTask {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 任务编号（自动生成唯一编号）
     */
    private String taskNo;
    
    /**
     * 任务名称
     */
    private String taskName;
    
    /**
     * 任务配置（关联用例、执行参数等）
     */
    private String taskConfig;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 创建人（用户名）
     */
    private String creator;
    
    /**
     * 创建人昵称（非数据库字段，用于前端显示）
     */
    @TableField(exist = false)
    private String creatorNickname;

    /**
     * 最新执行时间（非数据库字段，用于前端显示）
     */
    @TableField(exist = false)
    private LocalDateTime latestExecuteTime;
    
    /**
     * 定时策略（cron表达式）
     */
    private String cronExpression;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 并发数：1-10，默认为1
     */
    private Integer concurrency;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
