package com.uiauto.service;

import com.uiauto.entity.Department;
import com.uiauto.mapper.DepartmentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

/**
 * 部门管理服务类
 * 提供部门的增删改查及树形结构构建功能
 * 
 * @author huangzhiyong081439
 * @date 2026-04-07
 */
@Service
public class DepartmentService {
    
    @Autowired
    private DepartmentMapper departmentMapper;
    
    /**
     * 获取部门列表（树形结构）
     * @param name 部门名称（模糊查询，可为空）
     * @param status 部门状态（1-正常，0-禁用，可为空）
     * @return List<Department> 树形结构的部门列表
     */
    public List<Department> list(String name, Integer status) {
        List<Department> allList = departmentMapper.list(name, status);
        return buildTree(allList);
    }
    
    /**
     * 根据ID获取部门信息
     * @param id 部门ID
     * @return Department 部门实体，不存在返回null
     */
    public Department getById(Long id) {
        return departmentMapper.getById(id);
    }
    
    /**
     * 添加部门
     * @param department 部门实体对象
     * @return int 影响行数
     */
    public int add(Department department) {
        return departmentMapper.insert(department);
    }
    
    /**
     * 更新部门信息
     * @param department 部门实体对象
     * @return int 影响行数
     */
    public int update(Department department) {
        return departmentMapper.update(department);
    }
    
    /**
     * 删除部门
     * @param id 部门ID
     * @return int 影响行数
     * @throws RuntimeException 如果部门下存在子部门则抛出异常
     */
    public int delete(Long id) {
        // 检查是否有子部门
        if (departmentMapper.countByParentId(id) > 0) {
            throw new RuntimeException("该部门下存在子部门，无法删除");
        }
        return departmentMapper.delete(id);
    }
    
    /**
     * 批量删除部门
     * @param ids 部门ID列表
     * @return int 影响行数
     * @throws RuntimeException 如果任何部门下存在子部门则抛出异常
     */
    public int batchDelete(List<Long> ids) {
        // 检查是否有子部门
        for (Long id : ids) {
            if (departmentMapper.countByParentId(id) > 0) {
                throw new RuntimeException("部门 ID " + id + " 下存在子部门，无法删除");
            }
        }
        return departmentMapper.batchDelete(ids);
    }
    
    /**
     * 构建树形结构
     * @param list 扁平化的部门列表
     * @return List<Department> 树形结构的部门列表
     */
    private List<Department> buildTree(List<Department> list) {
        List<Department> result = new ArrayList<>();
        // 先找所有根节点
        for (Department dept : list) {
            if (dept.getParentId() == null || dept.getParentId() == 0) {
                result.add(dept);
            }
        }
        // 递归构建子树
        for (Department dept : result) {
            dept.setChildren(getChildren(dept.getId(), list));
        }
        return result;
    }
    
    /**
     * 递归获取子部门
     * @param parentId 父部门ID
     * @param allList 所有部门列表
     * @return List<Department> 子部门列表
     */
    private List<Department> getChildren(Long parentId, List<Department> allList) {
        List<Department> children = new ArrayList<>();
        for (Department dept : allList) {
            if (parentId.equals(dept.getParentId())) {
                dept.setChildren(getChildren(dept.getId(), allList));
                children.add(dept);
            }
        }
        return children;
    }
}