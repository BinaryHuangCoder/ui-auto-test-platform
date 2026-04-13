package com.uiauto.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.uiauto.entity.TestStepExecution;
import com.uiauto.mapper.TestStepExecutionMapper;
import com.uiauto.service.TestStepExecutionService;
import org.springframework.stereotype.Service;

@Service
public class TestStepExecutionServiceImpl extends ServiceImpl<TestStepExecutionMapper, TestStepExecution> implements TestStepExecutionService {
}
