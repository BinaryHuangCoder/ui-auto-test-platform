package com.uiauto.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.uiauto.entity.TestTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 测试任务Mapper接口
 * 
 * @author huangzhiyong081439
 * @date 2026-04-10
 */
@Mapper
public interface TestTaskMapper extends BaseMapper<TestTask> {
    
}
