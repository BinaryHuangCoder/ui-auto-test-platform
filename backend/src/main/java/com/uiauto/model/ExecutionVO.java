package com.uiauto.model;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 执行记录视图对象
 * 用于返回执行人昵称等扩展信息
 * 
 * @author huangzhiyong081439
 * @date 2026-04-08
 */
@Data
public class ExecutionVO {
    
    /**
     * 执行记录ID
     */
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
     * 执行人昵称
     */
    private String executorNickname;
    
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
     * 通过数
     */
    private Integer passedCount;
    
    /**
     * 失败数
     */
    private Integer failedCount;
    
    /**
     * 总数
     */
    private Integer totalCount;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}