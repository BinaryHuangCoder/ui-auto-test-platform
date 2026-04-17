
package com.uiauto.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 测试任务与用例关联实体类
 *
 * @author huangzhiyong081439
 * @since 2026-04-13
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
     * 用例ID
     */
    private Long caseId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}

