package com.uiauto.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.uiauto.entity.TestTask;

/**
 * 测试任务Service接口
 * 
 * @author huangzhiyong081439
 * @date 2026-04-10
 */
public interface TestTaskService extends IService<TestTask> {
    
    /**
     * 分页查询测试任务
     * 
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param keyword 搜索关键词
     * @return 分页结果
     */
    Page<TestTask> pageQuery(Integer pageNum, Integer pageSize, String keyword);
    
    /**
     * 生成唯一任务编号
     * 
     * @return 任务编号
     */
    String generateTaskNo();
}
