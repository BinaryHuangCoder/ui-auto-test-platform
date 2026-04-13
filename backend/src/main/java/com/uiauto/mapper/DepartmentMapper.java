package com.uiauto.mapper;

import com.uiauto.entity.Department;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 部门管理Mapper接口
 * 提供部门数据的增删改查操作
 * 
 * @author huangzhiyong081439
 * @date 2026-04-07
 */
@Mapper
public interface DepartmentMapper {
    
    /**
     * 查询部门列表
     * @param name 部门名称（可选，模糊查询）
     * @param status 部门状态（可选，1-正常，0-禁用）
     * @return List<Department> 部门列表
     */
    @Select("<script>" +
            "SELECT * FROM department WHERE 1=1 " +
            "<if test='name != null and name != \"\"'> AND name LIKE CONCAT('%', #{name}, '%') </if>" +
            "<if test='status != null'> AND status = #{status} </if>" +
            " ORDER BY sort_order ASC, id ASC" +
            "</script>")
    List<Department> list(@Param("name") String name, @Param("status") Integer status);
    
    /**
     * 根据父部门ID查询子部门
     * @param parentId 父部门ID
     * @return List<Department> 子部门列表
     */
    @Select("SELECT * FROM department WHERE parent_id = #{parentId} ORDER BY sort_order ASC, id ASC")
    List<Department> listByParentId(@Param("parentId") Long parentId);
    
    /**
     * 根据ID查询部门
     * @param id 部门ID
     * @return Department 部门实体
     */
    @Select("SELECT * FROM department WHERE id = #{id}")
    Department getById(@Param("id") Long id);
    
    /**
     * 新增部门
     * @param department 部门实体对象
     * @return int 影响行数
     */
    @Insert("INSERT INTO department (parent_id, name, sort_order, leader, status, create_time) " +
            "VALUES (#{parentId}, #{name}, #{sort}, #{leader}, #{status}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Department department);
    
    /**
     * 更新部门
     * @param department 部门实体对象
     * @return int 影响行数
     */
    @Update("UPDATE department SET name = #{name}, sort_order = #{sort}, leader = #{leader}, status = #{status} WHERE id = #{id}")
    int update(Department department);
    
    /**
     * 删除部门
     * @param id 部门ID
     * @return int 影响行数
     */
    @Delete("DELETE FROM department WHERE id = #{id}")
    int delete(@Param("id") Long id);
    
    /**
     * 批量删除部门
     * @param ids 部门ID列表
     * @return int 影响行数
     */
    @Delete("<script>" +
            "DELETE FROM department WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    int batchDelete(@Param("ids") List<Long> ids);
    
    /**
     * 统计子部门数量
     * @param parentId 父部门ID
     * @return int 子部门数量
     */
    @Select("SELECT COUNT(*) FROM department WHERE parent_id = #{parentId}")
    int countByParentId(@Param("parentId") Long parentId);
}