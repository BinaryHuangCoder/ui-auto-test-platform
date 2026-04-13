
package com.uiauto.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.uiauto.entity.TestTaskCase;
import org.apache.ibatis.annotations.Mapper;

/**
 * 测试任务与用例关联Mapper接口
 *
 * @author huangzhiyong081439
 * @since 2026-04-13
 */
@Mapper
public interface TestTaskCaseMapper extends BaseMapper<TestTaskCase> {
}

