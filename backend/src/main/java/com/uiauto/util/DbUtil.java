package com.uiauto.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import com.uiauto.model.Result;

/**
 * 数据库工具类 - 用于执行数据库维护操作
 * 
 * <p>提供临时的数据库维护接口，仅用于系统初始化和调试</p>
 *
 * @author huangzhiyong081439
 * @date 2026-04-05
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/db")
public class DbUtil {
    
    /** JDBC模板，用于执行SQL */
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    /**
     * 执行SQL更新语句
     * 
     * <p>注意：此接口仅用于系统初始化和调试，生产环境应关闭此接口</p>
     *
     * @param sql 要执行的SQL语句
     * @return Result<Integer> 影响的行数
     * @author huangzhiyong081439
     * @date 2026-04-05
     * @since 1.0.0
     */
    @PostMapping("/execute")
    public Result<Integer> executeSql(@RequestParam String sql) {
        try {
            int rows = jdbcTemplate.update(sql);
            return Result.success(rows);
        } catch (Exception e) {
            return Result.error("执行失败: " + e.getMessage());
        }
    }
}
