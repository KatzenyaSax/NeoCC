import request from '@/utils/request'

// 查询服务费记录列表（分页）
export function listServiceFeeRecord(query) {
  return request({
    url: '/serviceFeeRecord/page',
    method: 'get',
    params: query
  })
}

// 查询服务费记录详细
export function getServiceFeeRecord(id) {
  return request({
    url: '/serviceFeeRecord/' + id,
    method: 'get'
  })
}

// 按合同ID查询
export function listByContractId(contractId) {
  return request({
    url: '/serviceFeeRecord/listByContractId/' + contractId,
    method: 'get'
  })
}

// 新增服务费记录
export function addServiceFeeRecord(data) {
  return request({
    url: '/serviceFeeRecord',
    method: 'post',
    data: data
  })
}

// 修改服务费记录
export function updateServiceFeeRecord(data) {
  return request({
    url: '/serviceFeeRecord',
    method: 'put',
    data: data
  })
}

// 删除服务费记录
export function delServiceFeeRecord(id) {
  return request({
    url: '/serviceFeeRecord/' + id,
    method: 'delete'
  })
}

// 确认付款
export function confirmPay(id, data) {
  return request({
    url: '/serviceFeeRecord/' + id + '/pay',
    method: 'put',
    data: data
  })
}

// 获取最小未使用的ID
export function getMinUnusedServiceFeeRecordId() {
  return request({
    url: '/serviceFeeRecord/min-unused-id',
    method: 'get'
  })
}
