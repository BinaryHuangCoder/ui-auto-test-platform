package com.uiauto.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.uiauto.entity.Model;
import com.uiauto.model.Result;
import com.uiauto.service.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 模型配置管理控制器
 *
 * @author huangzhiyong081439
 * @since 2026-04-14
 */
@RestController
@RequestMapping("/api/model")
@CrossOrigin
public class ModelController {

    @Autowired
    private ModelService modelService;

    /**
     * 分页查询模型配置列表
     *
     * @param pageNum 页码，默认1
     * @param pageSize 每页条数，默认10
     * @param keyword 关键词（模型名称/模型家族）
     * @return 分页的模型配置列表
     */
    @GetMapping("/list")
    public Result<Page<Model>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        Page<Model> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Model> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w
                .like(Model::getModelName, keyword)
                .or()
                .like(Model::getModelFamily, keyword)
            );
        }
        wrapper.orderByDesc(Model::getCreateTime);
        Page<Model> result = modelService.page(page, wrapper);
        return Result.success(result);
    }

    /**
     * 根据ID获取模型配置详情
     *
     * @param id 模型ID
     * @return 模型配置详情
     */
    @GetMapping("/{id}")
    public Result<Model> getById(@PathVariable Long id) {
        Model model = modelService.getById(id);
        return model != null ? Result.success(model) : Result.error("模型不存在");
    }

    /**
     * 新增模型配置
     *
     * @param model 模型配置信息
     * @return 操作结果
     */
    @PostMapping
    public Result<String> add(@RequestBody Model model) {
        model.setCreateTime(LocalDateTime.now());
        model.setStatus(model.getStatus() != null ? model.getStatus() : 1);
        boolean success = modelService.save(model);
        if (success) {
            return Result.success("新增成功");
        } else {
            return Result.error("新增失败");
        }
    }

    /**
     * 更新模型配置
     *
     * @param model 模型配置信息
     * @return 操作结果
     */
    @PutMapping
    public Result<String> update(@RequestBody Model model) {
        if (model.getId() == null) {
            return Result.error("模型ID不能为空");
        }
        Model existModel = modelService.getById(model.getId());
        if (existModel == null) {
            return Result.error("模型不存在");
        }
        model.setUpdateTime(LocalDateTime.now());
        boolean success = modelService.updateById(model);
        if (success) {
            return Result.success("修改成功");
        } else {
            return Result.error("修改失败");
        }
    }

    /**
     * 删除模型配置
     *
     * @param id 模型ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        if (id == null) {
            return Result.error("模型ID不能为空");
        }
        Model existModel = modelService.getById(id);
        if (existModel == null) {
            return Result.error("模型不存在");
        }
        boolean success = modelService.removeById(id);
        if (success) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }
}
