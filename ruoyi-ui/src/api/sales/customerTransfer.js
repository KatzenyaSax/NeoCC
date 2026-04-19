import request from '@/utils/request'

// 查询客户转移记录列表（分页）
export function listCustomerTransfer(query) {
  return request({
    url: '/customerTransferLog/page',
    method: 'get',
    params: query
  })
}

// 查询客户转移记录详细
export function getCustomerTransfer(id) {
  return request({
    url: '/customerTransferLog/' + id,
    method: 'get'
  })
}

// 按客户ID查询
export function listTransferByCustomerId(customerId) {
  return request({
    url: '/customerTransferLog/listByCustomerId/' + customerId,
    method: 'get'
  })
}

// 新增客户转移记录
export function addCustomerTransfer(data) {
  return request({
    url: '/customerTransferLog',
    method: 'post',
    data: data
  })
}

// 修改客户转移记录
export function updateCustomerTransfer(data) {
  return request({
    url: '/customerTransferLog',
    method: 'put',
    data: data
  })
}

// 删除客户转移记录
export function delCustomerTransfer(id) {
  return request({
    url: '/customerTransferLog/' + id,
    method: 'delete'
  })
}
