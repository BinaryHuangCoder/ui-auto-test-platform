import request from './request'

export function getCaseList(params) {
  return request({
    url: '/case/list',
    method: 'get',
    params
  })
}

export function getAllCases() {
  return request({
    url: '/case/all',
    method: 'get'
  })
}

export function getCaseDetail(id) {
  return request({
    url: `/case/${id}`,
    method: 'get'
  })
}

export function addCase(data) {
  return request({
    url: '/case',
    method: 'post',
    data
  })
}

export function updateCase(data) {
  return request({
    url: '/case',
    method: 'put',
    data
  })
}

export function deleteCase(id) {
  return request({
    url: `/case/${id}`,
    method: 'delete'
  })
}

export function batchDeleteCase(ids) {
  return request({
    url: '/case/batch',
    method: 'delete',
    data: ids
  })
}
