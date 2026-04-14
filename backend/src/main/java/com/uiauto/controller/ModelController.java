package com.uiauto.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uiauto.entity.Model;
import com.uiauto.entity.ModelScenario;
import com.uiauto.model.Result;
import com.uiauto.service.ModelService;
import com.uiauto.service.ModelScenarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private ModelScenarioService modelScenarioService;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

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

    /**
     * 测试模型连接
     *
     * @param model 模型信息
     * @return 测试结果
     */
    @PostMapping("/test")
    public Result<String> testConnection(@RequestBody Model model) {
        try {
            String url = model.getModelUrl();
            String apiKey = model.getApiKey();
            
            if (url == null || url.isEmpty()) {
                return Result.error("模型地址不能为空");
            }
            if (apiKey == null || apiKey.isEmpty()) {
                return Result.error("API Key不能为空");
            }
            
            // 简化测试：先只验证基本配置，不实际调用模型API
            // 后续可以根据具体模型提供商调整测试逻辑
            return Result.success("连接配置验证通过");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("连接失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有模型使用场景
     *
     * @return 场景列表
     */
    @GetMapping("/scenarios")
    public Result<List<ModelScenario>> getScenarios() {
        List<ModelScenario> scenarios = modelScenarioService.getAllScenarios();
        return Result.success(scenarios);
    }

    /**
     * 更新场景关联的模型
     *
     * @param scenarioCode 场景编码
     * @param scenario     场景信息（包含modelId）
     * @return 操作结果
     */
    @PutMapping("/scenarios/{scenarioCode}")
    public Result<String> updateScenarioModel(
            @PathVariable String scenarioCode,
            @RequestBody ModelScenario scenario) {
        boolean success = modelScenarioService.updateScenarioModel(scenarioCode, scenario.getModelId());
        if (success) {
            return Result.success("更新成功");
        } else {
            return Result.error("更新失败");
        }
    }

    /**
     * 根据场景编码获取关联的模型配置
     *
     * @param scenarioCode 场景编码
     * @return 模型配置
     */
    @GetMapping("/scenarios/{scenarioCode}/model")
    public Result<Model> getModelByScenarioCode(@PathVariable String scenarioCode) {
        ModelScenario scenario = modelScenarioService.getByScenarioCode(scenarioCode);
        if (scenario == null || scenario.getModelId() == null) {
            return Result.error("场景未配置模型");
        }
        Model model = modelService.getById(scenario.getModelId());
        if (model == null) {
            return Result.error("模型不存在");
        }
        return Result.success(model);
    }
}
