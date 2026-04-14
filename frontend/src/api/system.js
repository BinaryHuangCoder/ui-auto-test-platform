import request from './request'

// 获取应用系统列表（带分页）
export function getSystemList(params) {
  return request({
    url: '/system/list',
    method: 'get',
    params
  })
}

// 获取应用系统详情
export function getSystemDetail(id) {
  return request({
    url: `/system/${id}`,
    method: 'get'
  })
}

// 新增应用系统
export function addSystem(data) {
  return request({
    url: '/system',
    method: 'post',
    data
  })
}

// 更新应用系统
export function updateSystem(data) {
  return request({
    url: '/system',
    method: 'put',
    data
  })
}

// 删除应用系统
export function deleteSystem(id) {
  return request({
    url: `/system/${id}`,
    method: 'delete'
  })
}
