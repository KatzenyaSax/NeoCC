import request from '@/utils/request'

// 分页查询审核中合同（status=4）
export function pageContractAudit(query) {
  return request({
    url: '/contractAudit/page',
    method: 'get',
    params: query
  })
}

// 获取合同详情（含关联信息）
export function getContractAuditDetail(contractId) {
  return request({
    url: '/contractAudit/' + contractId + '/detail',
    method: 'get'
  })
}

// 通过审核
export function approveContractAudit(contractId, data) {
  return request({
    url: '/contractAudit/' + contractId + '/approve',
    method: 'post',
    data: data
  })
}

// 拒绝审核
export function rejectContractAudit(contractId, data) {
  return request({
    url: '/contractAudit/' + contractId + '/reject',
    method: 'post',
    data: data
  })
}
