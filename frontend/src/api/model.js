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
