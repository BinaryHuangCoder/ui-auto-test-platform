
package com.uiauto.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 测试任务执行步骤详情实体类
 *
 * @author huangzhiyong081439
 * @since 2026-04-13
 */
@Data
@TableName("test_task_step_execution")
public class TestTaskStepExecution {

    /**
     * 步骤执行ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务执行记录ID
     */
    private Long taskExecutionId;

    /**
     * 用例执行记录ID
     */
    private Long caseExecutionId;

    /**
     * 步骤ID
     */
    private Long stepId;

    /**
     * 步骤序号
     */
    private Integer stepNo;

    /**
     * 步骤描述
     */
    private String stepDescription;

    /**
     * 操作类型
     */
    private String action;

    /**
     * 执行状态
     */
    private String status;

    /**
     * 断言状态
     */
    private String assertionStatus;

    /**
     * 断言描述
     */
    private String assertionDescription;

    /**
     * 单步AI token消耗量
     */
    private Long aiTokenUsed;

    /**
     * AI断言结果
     */
    private String aiResult;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 执行耗时(毫秒)
     */
    private Long duration;

    /**
     * 输入数据
     */
    private String inputData;

    /**
     * 预期结果
     */
    private String expectedResult;

    /**
     * 实际结果
     */
    private String actualResult;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 截图路径
     */
    private String screenshot;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

