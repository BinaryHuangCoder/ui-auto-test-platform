package com.uiauto.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.uiauto.entity.Model;
import com.uiauto.mapper.ModelMapper;
import com.uiauto.service.ModelService;
import org.springframework.stereotype.Service;

@Service
public class ModelServiceImpl extends ServiceImpl<ModelMapper, Model> implements ModelService {
}
