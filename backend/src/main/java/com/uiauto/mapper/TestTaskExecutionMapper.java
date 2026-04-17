
package com.uiauto.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.uiauto.entity.TestTaskExecution;
import org.apache.ibatis.annotations.Mapper;

/**
 * 测试任务执行历史Mapper接口
 *
 * @author huangzhiyong081439
 * @since 2026-04-13
 */
@Mapper
public interface TestTaskExecutionMapper extends BaseMapper<TestTaskExecution> {
}

