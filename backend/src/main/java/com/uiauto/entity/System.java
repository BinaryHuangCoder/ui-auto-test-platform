package com.uiauto.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 应用系统实体类
 *
 * @author huangzhiyong081439
 * @since 2026-04-14
 */
@Data
@TableName("sys_system")
public class System {

    /**
     * 系统ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 系统编号
     */
    private String systemNo;

    /**
     * 系统名称
     */
    private String systemName;

    /**
     * 系统简称
     */
    private String systemShortName;

    /**
     * 状态：0-禁用，1-正常
     */
    private Integer status;

    /**
     * 系统来源：manual-手工新增，external-外部同步
     */
    private String source;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
