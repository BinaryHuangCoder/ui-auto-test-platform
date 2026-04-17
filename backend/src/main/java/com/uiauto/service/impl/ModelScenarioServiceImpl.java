package com.uiauto.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.uiauto.entity.ModelScenario;
import com.uiauto.mapper.ModelScenarioMapper;
import com.uiauto.service.ModelScenarioService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 模型使用场景配置服务实现类
 *
 * @author BinaryHuang
 */
@Service
public class ModelScenarioServiceImpl extends ServiceImpl<ModelScenarioMapper, ModelScenario> implements ModelScenarioService {

    /**
     * 获取所有模型使用场景配置
     *
     * @return 场景配置列表
     */
    @Override
    public List<ModelScenario> getAllScenarios() {
        return list();
    }

    /**
     * 根据场景编码获取场景配置
     *
     * @param scenarioCode 场景编码
     * @return 场景配置
     */
    @Override
    public ModelScenario getByScenarioCode(String scenarioCode) {
        LambdaQueryWrapper<ModelScenario> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelScenario::getScenarioCode, scenarioCode);
        return getOne(wrapper);
    }

    /**
     * 更新场景关联的模型
     *
     * @param scenarioCode 场景编码
     * @param modelId      模型ID
     * @return 是否更新成功
     */
    @Override
    public boolean updateScenarioModel(String scenarioCode, Long modelId) {
        ModelScenario scenario = getByScenarioCode(scenarioCode);
        if (scenario == null) {
            return false;
        }
        scenario.setModelId(modelId);
        return updateById(scenario);
    }
}
