package com.uiauto.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.uiauto.entity.TestCase;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.UUID;

public interface TestCaseService extends IService<TestCase> {
    
    /** 生成唯一用例编号 - 使用UUID确保唯一 */
    default String generateCaseNo() {
        String prefix = "TC" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        // 使用UUID最后6位确保唯一
        String uuidPart = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return prefix + uuidPart;
    }
    
    /** 保存用例并自动生成编号 */
    boolean saveCase(TestCase testCase);
}
