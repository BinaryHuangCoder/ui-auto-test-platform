package com.uiauto.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 测试步骤执行记录实体类
 * 
 * @author huangzhiyong081439
 * @date 2026-04-07
 */
@Data
@TableName("test_step_execution")
public class TestStepExecution {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 执行记录ID
     */
    private Long executionId;
    
    /**
     * 步骤序号
     */
    private Integer stepNo;
    
    /**
     * 步骤描述
     */
    private String stepDescription;
    
    /**
     * 执行耗时（毫秒）
     */
    private Long duration;
    
    /**
     * 执行状态（success/failed）
     */
    private String status;
    
    /**
     * 断言状态（success/failed/none）
     */
    private String assertionStatus;
    
    /**
     * 断言描述/AI断言结果
     */
    private String assertionDescription;
    
    /**
     * 单步AI token消耗量
     */
    private Long aiTokenUsed;
    
    /**
     * AI断言结果（详细信息）
     */
    private String aiResult;
    
    /**
     * 步骤执行截图（文件路径）
     */
    private String screenshot;
    
    /**
     * 执行失败描述（失败原因）
     */
    private String errorMessage;
    
    /**
     * 测试数据
     */
    private String testData;
    
    /**
     * 融合后的步骤描述
     */
    private String fusedStepDescription;
    
    /**
     * 步骤数据融合耗时（毫秒）
     */
    private Long stepFusionDuration;
    
    /**
     * 页面操作耗时（毫秒）
     */
    private Long pageOperationDuration;
    
    /**
     * AI断言耗时（毫秒）
     */
    private Long assertionDuration;
    
    /**
     * 步骤数据融合token消耗
     */
    private Long stepFusionTokenUsed;
    
    /**
     * 页面操作token消耗
     */
    private Long pageOperationTokenUsed;
    
    /**
     * AI断言token消耗
     */
    private Long assertionTokenUsed;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
}