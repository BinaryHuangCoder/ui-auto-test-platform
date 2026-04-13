
package com.uiauto.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 测试任务执行历史实体类
 *
 * @author huangzhiyong081439
 * @since 2026-04-13
 */
@Data
@TableName("test_task_execution")
public class TestTaskExecution {

    /**
     * 执行ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 任务编号
     */
    private String taskNo;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 执行人
     */
    private String executor;

    /**
     * 执行时间
     */
    private LocalDateTime executeTime;

    /**
     * 执行状态
     */
    private String status;

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
     * 总用例数
     */
    private Integer totalCount;

    /**
     * 通过数
     */
    private Integer passedCount;

    /**
     * 失败数
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

