package com.uiauto.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.uiauto.entity.TestCaseStep;
import com.uiauto.mapper.TestCaseStepMapper;
import com.uiauto.service.TestCaseStepService;
import org.springframework.stereotype.Service;

@Service
public class TestCaseStepServiceImpl extends ServiceImpl<TestCaseStepMapper, TestCaseStep> implements TestCaseStepService {
}
