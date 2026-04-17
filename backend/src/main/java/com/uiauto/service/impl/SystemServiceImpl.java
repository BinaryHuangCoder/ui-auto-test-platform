package com.uiauto.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.uiauto.entity.System;
import com.uiauto.mapper.SystemMapper;
import com.uiauto.service.SystemService;
import org.springframework.stereotype.Service;

/**
 * 应用系统Service实现类
 *
 * @author huangzhiyong081439
 * @since 2026-04-14
 */
@Service
public class SystemServiceImpl extends ServiceImpl<SystemMapper, System> implements SystemService {
}
