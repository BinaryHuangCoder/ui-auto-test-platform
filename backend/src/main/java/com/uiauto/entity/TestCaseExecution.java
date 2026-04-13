package com.uiauto.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 测试用例执行记录实体类
 * 
 * @author huangzhiyong081439
 * @date 2026-04-08
 * @update 2026-04-13 添加AI token消耗字段
 */
@Data
@TableName("test_case_execution")
public class TestCaseExecution {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用例ID
     */
    private Long caseId;
    
    /**
     * 用例编号
     */
    private String caseNo;
    
    /**
     * 用例名称
     */
    private String caseName;
    
    /**
     * 用例描述
     */
    private String description;
    
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
     * 执行时间
     */
    private LocalDateTime executeTime;
    
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
     * 步骤总数
     */
    private Integer totalCount;
    
    /**
     * 成功步骤数
     */
    private Integer passedCount;
    
    /**
     * 失败步骤数
     */
    private Integer failedCount;
    
    /**
     * AI总token消耗量
     */
    private Long aiTotalTokenUsed;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

