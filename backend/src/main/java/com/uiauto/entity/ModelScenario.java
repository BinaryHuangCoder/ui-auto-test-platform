package com.uiauto.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 模型使用场景配置实体类
 *
 * @author BinaryHuang
 */
@Data
@TableName("sys_model_scenario")
public class ModelScenario {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 场景编码（唯一）: image_assertion-图像断言检查, step_fusion-步骤数据融合
     */
    private String scenarioCode;

    /**
     * 场景名称
     */
    private String scenarioName;

    /**
     * 关联的模型ID
     */
    private Long modelId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
