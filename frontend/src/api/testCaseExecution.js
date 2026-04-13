import request from './request'

export function getExecutionList(params) {
  return request({
    url: '/execution/list',
    method: 'get',
    params
  })
}

export function getExecutionDetail(id) {
  return request({
    url: `/execution/${id}`,
    method: 'get'
  })
}

export function getStepExecutions(executionId, pageNum = 1, pageSize = 5) {
  return request({
    url: `/execution/${executionId}/steps`,
    method: 'get',
    params: { pageNum, pageSize }
  })
}

export function runCase(caseId) {
  return request({
    url: `/execution/run/${caseId}`,
    method: 'post'
  })
}

export function runBatchCase(caseIds) {
  return request({
    url: '/execution/run/batch',
    method: 'post',
    data: caseIds
  })
}
