package com.uiauto.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.uiauto.entity.TestCaseExecution;
import com.uiauto.mapper.TestCaseExecutionMapper;
import com.uiauto.service.TestCaseExecutionService;
import org.springframework.stereotype.Service;

@Service
public class TestCaseExecutionServiceImpl extends ServiceImpl<TestCaseExecutionMapper, TestCaseExecution> implements TestCaseExecutionService {
}
