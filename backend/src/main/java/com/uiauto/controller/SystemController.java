package com.uiauto.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.uiauto.entity.System;
import com.uiauto.model.Result;
import com.uiauto.service.SystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 应用系统管理控制器
 *
 * @author huangzhiyong081439
 * @since 2026-04-14
 */
@RestController
@RequestMapping("/api/system")
@CrossOrigin
public class SystemController {

    @Autowired
    private SystemService systemService;

    /**
     * 分页查询应用系统列表
     *
     * @param pageNum 页码，默认1
     * @param pageSize 每页条数，默认10
     * @param keyword 关键词（系统编号/系统名称/系统简称）
     * @return 分页的应用系统列表
     */
    @GetMapping("/list")
    public Result<Page<System>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        Page<System> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<System> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w
                .like(System::getSystemNo, keyword)
                .or()
                .like(System::getSystemName, keyword)
                .or()
                .like(System::getSystemShortName, keyword)
            );
        }
        wrapper.orderByDesc(System::getCreateTime);
        Page<System> result = systemService.page(page, wrapper);
        return Result.success(result);
    }

    /**
     * 根据ID获取应用系统详情
     *
     * @param id 系统ID
     * @return 应用系统详情
     */
    @GetMapping("/{id}")
    public Result<System> getById(@PathVariable Long id) {
        System system = systemService.getById(id);
        return system != null ? Result.success(system) : Result.error("系统不存在");
    }

    /**
     * 新增应用系统
     *
     * @param system 应用系统信息
     * @return 操作结果
     */
    @PostMapping
    public Result<String> add(@RequestBody System system) {
        // 检查系统编号是否已存在
        System existSystem = systemService.getOne(
            new LambdaQueryWrapper<System>()
                .eq(System::getSystemNo, system.getSystemNo())
        );
        if (existSystem != null) {
            return Result.error("系统编号已存在");
        }
        system.setCreateTime(LocalDateTime.now());
        system.setStatus(system.getStatus() != null ? system.getStatus() : 1);
        system.setSource(system.getSource() != null ? system.getSource() : "manual");
        boolean success = systemService.save(system);
        if (success) {
            return Result.success("新增成功");
        } else {
            return Result.error("新增失败");
        }
    }

    /**
     * 更新应用系统
     *
     * @param system 应用系统信息
     * @return 操作结果
     */
    @PutMapping
    public Result<String> update(@RequestBody System system) {
        if (system.getId() == null) {
            return Result.error("系统ID不能为空");
        }
        System existSystem = systemService.getById(system.getId());
        if (existSystem == null) {
            return Result.error("系统不存在");
        }
        // 如果系统编号改变，检查是否已存在
        if (!existSystem.getSystemNo().equals(system.getSystemNo())) {
            System checkSystem = systemService.getOne(
                new LambdaQueryWrapper<System>()
                    .eq(System::getSystemNo, system.getSystemNo())
            );
            if (checkSystem != null) {
                return Result.error("系统编号已存在");
            }
        }
        system.setUpdateTime(LocalDateTime.now());
        boolean success = systemService.updateById(system);
        if (success) {
            return Result.success("修改成功");
        } else {
            return Result.error("修改失败");
        }
    }

    /**
     * 删除应用系统
     *
     * @param id 系统ID
     * @return 操作结果
     */
    /**
     * 获取所有应用系统列表（用于下拉框）
     *
     * @return 所有应用系统列表
     */
    @GetMapping("/all")
    public Result<java.util.List<System>> listAll() {
        LambdaQueryWrapper<System> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(System::getStatus, 1);
        wrapper.orderByAsc(System::getSystemNo);
        java.util.List<System> list = systemService.list(wrapper);
        return Result.success(list);
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        if (id == null) {
            return Result.error("系统ID不能为空");
        }
        System existSystem = systemService.getById(id);
        if (existSystem == null) {
            return Result.error("系统不存在");
        }
        boolean success = systemService.removeById(id);
        if (success) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }
}
