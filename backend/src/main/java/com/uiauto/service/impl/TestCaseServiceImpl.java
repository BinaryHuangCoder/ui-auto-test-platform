package com.uiauto.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.uiauto.entity.TestCase;
import com.uiauto.mapper.TestCaseMapper;
import com.uiauto.service.TestCaseService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TestCaseServiceImpl extends ServiceImpl<TestCaseMapper, TestCase> implements TestCaseService {
    
    @Override
    public String generateCaseNo() {
        String prefix = "TC" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuidPart = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return prefix + uuidPart;
    }
    
    public boolean saveCase(TestCase testCase) {
        if (testCase.getCaseNo() == null || testCase.getCaseNo().isEmpty()) {
            testCase.setCaseNo(generateCaseNo());
        }
        if (testCase.getCreateTime() == null) {
            testCase.setCreateTime(LocalDateTime.now());
        }
        testCase.setUpdateTime(LocalDateTime.now());
        if (testCase.getStatus() == null) {
            testCase.setStatus(1);
        }
        return this.save(testCase);
    }
}
