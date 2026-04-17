import request from './request'

export function getStepList(caseId) {
  return request({
    url: `/case-step/list/${caseId}`,
    method: 'get'
  })
}

export function getStepDetail(id) {
  return request({
    url: `/case-step/${id}`,
    method: 'get'
  })
}

export function addStep(data) {
  return request({
    url: '/case-step',
    method: 'post',
    data
  })
}

export function updateStep(data) {
  return request({
    url: '/case-step',
    method: 'put',
    data
  })
}

export function deleteStep(id) {
  return request({
    url: `/case-step/${id}`,
    method: 'delete'
  })
}

export function batchDeleteStep(ids) {
  return request({
    url: '/case-step/batch',
    method: 'delete',
    data: ids
  })
}

export function exportSteps(caseId) {
  return request({
    url: `/case-step/export/${caseId}`,
    method: 'get',
    responseType: 'blob'
  })
}

export function importSteps(caseId, file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: `/case-step/import/${caseId}`,
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
