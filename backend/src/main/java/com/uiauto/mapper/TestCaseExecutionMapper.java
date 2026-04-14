package com.uiauto.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.uiauto.entity.TestCaseExecution;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TestCaseExecutionMapper extends BaseMapper<TestCaseExecution> {

    /**
     * 获取每个测试用例的最新执行记录
     * @return 最新执行记录列表
     */
    @Select("SELECT t1.* FROM test_case_execution t1 " +
            "INNER JOIN ( " +
            "    SELECT case_id, MAX(id) as max_id " +
            "    FROM test_case_execution " +
            "    GROUP BY case_id " +
            ") t2 ON t1.id = t2.max_id")
    List<TestCaseExecution> getLatestExecutions();
}
