import request from './request'

// 获取部门列表
export const getDepartmentList = (params) => {
  return request({
    url: '/department/list',
    method: 'get',
    params
  })
}

// 新增部门
export const addDepartment = (data) => {
  return request({
    url: '/department',
    method: 'post',
    data
  })
}

// 编辑部门
export const updateDepartment = (data) => {
  return request({
    url: '/department',
    method: 'put',
    data
  })
}

// 删除部门
export const deleteDepartment = (id) => {
  return request({
    url: '/department/' + id,
    method: 'delete'
  })
}

// 批量删除部门
export const batchDeleteDepartment = (ids) => {
  return request({
    url: '/department/batch',
    method: 'delete',
    data: ids
  })
}
