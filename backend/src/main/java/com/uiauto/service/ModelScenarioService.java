package com.uiauto.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.uiauto.entity.ModelScenario;

import java.util.List;

/**
 * 模型使用场景配置服务接口
 *
 * @author BinaryHuang
 */
public interface ModelScenarioService extends IService<ModelScenario> {

    /**
     * 获取所有模型使用场景配置
     *
     * @return 场景配置列表
     */
    List<ModelScenario> getAllScenarios();

    /**
     * 根据场景编码获取场景配置
     *
     * @param scenarioCode 场景编码
     * @return 场景配置
     */
    ModelScenario getByScenarioCode(String scenarioCode);

    /**
     * 更新场景关联的模型
     *
     * @param scenarioCode 场景编码
     * @param modelId      模型ID
     * @return 是否更新成功
     */
    boolean updateScenarioModel(String scenarioCode, Long modelId);
}
