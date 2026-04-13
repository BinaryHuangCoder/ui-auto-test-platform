import request from './request'

/**
 * 获取用户列表（带分页）
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页条数
 * @returns {Promise} 请求结果
 */
export function listUsers(params) {
  return request({
    url: '/user/list',
    method: 'get',
    params
  })
}

/**
 * 获取所有用户列表（用于下拉框）
 * @returns {Promise} 请求结果
 */
export function listAllUsers() {
  return request({
    url: '/user/all',
    method: 'get'
  })
}

/**
 * 添加用户
 * @param {Object} data - 用户数据
 * @returns {Promise} 请求结果
 */
export function addUser(data) {
  return request({
    url: '/user/add',
    method: 'post',
    data
  })
}

/**
 * 更新用户
 * @param {Object} data - 用户数据
 * @returns {Promise} 请求结果
 */
export function updateUser(data) {
  return request({
    url: '/user/update',
    method: 'put',
    data
  })
}

/**
 * 删除用户
 * @param {number} id - 用户ID
 * @returns {Promise} 请求结果
 */
export function deleteUser(id) {
  return request({
    url: '/user/delete/' + id,
    method: 'delete'
  })
}

export function resetPassword(data) {
  return request({
    url: '/user/reset-password',
    method: 'post',
    data
  })
}

export function changePassword(data) {
  return request({
    url: '/user/change-password',
    method: 'post',
    data
  })
}

export function getUserInfo() {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

export function updateUserProfile(data) {
  return request({
    url: '/user/profile',
    method: 'put',
    data
  })
}