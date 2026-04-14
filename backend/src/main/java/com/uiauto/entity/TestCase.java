package com.uiauto.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 测试用例实体类
 * 
 * @author huangzhiyong081439
 * @date 2026-04-08
 */
@Data
@TableName("test_case")
public class TestCase {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用例编号（自动生成唯一编号）
     */
    private String caseNo;
    
    /**
     * 用例名称
     */
    private String name;
    
    /**
     * 用例描述
     */
    private String description;
    
    /**
     * 设计者（用户名）
     */
    private String designer;
    
    /**
     * 设计者昵称（非数据库字段，用于前端显示）
     */
    @TableField(exist = false)
    private String designerNickname;
    
    /**
     * 关联系统ID
     */
    private Long systemId;
    
    /**
     * 关联系统名称（非数据库字段，用于前端显示）
     */
    @TableField(exist = false)
    private String systemName;
    
    /**
     * 用例性质：positive-正例，negative-反例
     */
    private String caseType;
    
    /**
     * 状态：0-禁用，1-启用
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
