import request from './request'

// 获取模型配置列表（带分页）
export function getModelList(params) {
  return request({
    url: '/model/list',
    method: 'get',
    params
  })
}

// 获取模型配置详情
export function getModelDetail(id) {
  return request({
    url: `/model/${id}`,
    method: 'get'
  })
}

// 新增模型配置
export function addModel(data) {
  return request({
    url: '/model',
    method: 'post',
    data
  })
}

// 更新模型配置
export function updateModel(data) {
  return request({
    url: '/model',
    method: 'put',
    data
  })
}

// 删除模型配置
export function deleteModel(id) {
  return request({
    url: `/model/${id}`,
    method: 'delete'
  })
}

// 测试模型连接
export function testModelConnection(data) {
  return request({
    url: '/model/test',
    method: 'post',
    data
  })
}

// 获取所有模型使用场景
export function getModelScenarios() {
  return request({
    url: '/model/scenarios',
    method: 'get'
  })
}

// 更新场景关联的模型
export function updateModelScenario(scenarioCode, data) {
  return request({
    url: `/model/scenarios/${scenarioCode}`,
    method: 'put',
    data
  })
}
