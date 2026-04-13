package com.uiauto.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 测试任务关联测试用例实体类
 * 用于关联测试任务和测试用例，支持批量添加
 * 
 * @author huangzhiyong081439
 * @date 2026-04-10
 */
@Data
@TableName("test_task_case")
public class TestTaskCase {
    
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
     * 测试用例ID
     */
    private Long caseId;
    
    /**
     * 测试用例名称（非数据库字段，用于前端显示）
     */
    @TableField(exist = false)
    private String caseName;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}