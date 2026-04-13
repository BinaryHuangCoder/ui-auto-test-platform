package com.uiauto.controller;

import com.uiauto.entity.Department;
import com.uiauto.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/**
 * 部门管理控制器
 * 提供部门的增删改查接口
 * 
 * @author huangzhiyong081439
 * @date 2026-04-07
 */
@RestController
@RequestMapping("/api/department")
@CrossOrigin
public class DepartmentController {
    
    @Autowired
    private DepartmentService departmentService;
    
    /**
     * 获取部门列表（树形结构）
     * @param name 部门名称（可选，模糊查询）
     * @param status 部门状态（可选，1-正常，0-禁用）
     * @return ResponseEntity<Map<String, Object>> 包含部门列表的响应
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "status", required = false) Integer status) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Department> list = departmentService.list(name, status);
            result.put("code", 200);
            result.put("data", list);
            result.put("message", "查询成功");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }
    
    /**
     * 根据ID获取部门详情
     * @param id 部门ID
     * @return ResponseEntity<Map<String, Object>> 包含部门信息的响应
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            Department department = departmentService.getById(id);
            result.put("code", 200);
            result.put("data", department);
            result.put("message", "查询成功");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }
    
    /**
     * 新增部门
     * @param department 部门实体对象
     * @return ResponseEntity<Map<String, Object>> 操作结果响应
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> add(@RequestBody Department department) {
        Map<String, Object> result = new HashMap<>();
        try {
            departmentService.add(department);
            result.put("code", 200);
            result.put("message", "新增成功");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }
    
    /**
     * 编辑部门
     * @param department 部门实体对象
     * @return ResponseEntity<Map<String, Object>> 操作结果响应
     */
    @PutMapping
    public ResponseEntity<Map<String, Object>> update(@RequestBody Department department) {
        Map<String, Object> result = new HashMap<>();
        try {
            departmentService.update(department);
            result.put("code", 200);
            result.put("message", "编辑成功");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }
    
    /**
     * 删除部门
     * @param id 部门ID
     * @return ResponseEntity<Map<String, Object>> 操作结果响应
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            departmentService.delete(id);
            result.put("code", 200);
            result.put("message", "删除成功");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }
    
    /**
     * 批量删除部门
     * @param ids 部门ID列表
     * @return ResponseEntity<Map<String, Object>> 操作结果响应
     */
    @DeleteMapping("/batch")
    public ResponseEntity<Map<String, Object>> batchDelete(@RequestBody List<Long> ids) {
        Map<String, Object> result = new HashMap<>();
        try {
            departmentService.batchDelete(ids);
            result.put("code", 200);
            result.put("message", "批量删除成功");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }
}