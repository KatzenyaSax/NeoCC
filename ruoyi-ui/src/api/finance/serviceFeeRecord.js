import request from '@/utils/request'

// 查询服务费记录列表（分页）
export function listServiceFeeRecord(query) {
  return request({
    url: '/api/serviceFeeRecord/page',
    method: 'get',
    params: query
  })
}

// 查询服务费记录详细
export function getServiceFeeRecord(id) {
  return request({
    url: '/api/serviceFeeRecord/' + id,
    method: 'get'
  })
}

// 按合同ID查询
export function listByContractId(contractId) {
  return request({
    url: '/api/serviceFeeRecord/listByContractId/' + contractId,
    method: 'get'
  })
}

// 新增服务费记录
export function addServiceFeeRecord(data) {
  return request({
    url: '/api/serviceFeeRecord',
    method: 'post',
    data: data
  })
}

// 修改服务费记录
export function updateServiceFeeRecord(data) {
  return request({
    url: '/api/serviceFeeRecord',
    method: 'put',
    data: data
  })
}

// 删除服务费记录
export function delServiceFeeRecord(id) {
  return request({
    url: '/api/serviceFeeRecord/' + id,
    method: 'delete'
  })
}

// 确认付款
export function confirmPay(id, data) {
  return request({
    url: '/api/serviceFeeRecord/' + id + '/pay',
    method: 'put',
    data: data
  })
}
