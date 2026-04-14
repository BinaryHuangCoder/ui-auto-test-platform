package com.uiauto.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 模型配置实体类
 *
 * @author huangzhiyong081439
 * @since 2026-04-14
 */
@Data
@TableName("sys_model")
public class Model {

    /**
     * 模型ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 模型地址
     */
    private String modelUrl;

    /**
     * API Key
     */
    private String apiKey;

    /**
     * 模型家族
     */
    private String modelFamily;

    /**
     * 状态：0-禁用，1-正常
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
