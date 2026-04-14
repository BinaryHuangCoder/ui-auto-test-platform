package com.uiauto.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("test_case_step")
public class TestCaseStep {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 用例 ID */
    private Long caseId;
    
    /** 步骤序号 */
    private Integer stepNo;
    
    /** 步骤描述 */
    private String stepDescription;
    
    /** 断言描述 */
    private String assertionDescription;
    
    /** 步骤名称 */
    private String stepName;
    
    /** 操作类型：click/input/wait/assert等 */
    private String action;
    
    /** 定位器类型：xpath/css/id/等 */
    private String locatorType;
    
    /** 定位器值 */
    private String locatorValue;
    
    /** 输入数据 */
    private String inputData;
    
    /** 测试数据 */
    private String testData;
    
    /** 预期结果 */
    private String expectedResult;
    
    /** 实际结果 */
    private String actualResult;
    
    /** 状态：0-禁用，1-启用 */
    private Integer status;
    
    /** 是否截图：0-否，1-是 */
    private Integer screenshot;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    /** 更新时间 */
    private LocalDateTime updateTime;
}
