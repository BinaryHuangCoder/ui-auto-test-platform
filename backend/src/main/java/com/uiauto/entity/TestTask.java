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
    private String name;
    
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
     * 定时策略（cron表达式）
     */
    private String cronExpression;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
