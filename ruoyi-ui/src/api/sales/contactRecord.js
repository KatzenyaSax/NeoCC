import request from '@/utils/request'

// 查询跟进记录列表（分页）
export function listContactRecord(query) {
  return request({
    url: '/api/contactRecord/page',
    method: 'get',
    params: query
  })
}

// 查询跟进记录详细
export function getContactRecord(id) {
  return request({
    url: '/api/contactRecord/' + id,
    method: 'get'
  })
}

// 按客户ID查询
export function listByCustomerId(customerId) {
  return request({
    url: '/api/contactRecord/listByCustomerId/' + customerId,
    method: 'get'
  })
}

// 按销售代表ID查询
export function listBySalesRepId(salesRepId) {
  return request({
    url: '/api/contactRecord/listBySalesRepId/' + salesRepId,
    method: 'get'
  })
}

// 新增跟进记录
export function addContactRecord(data) {
  return request({
    url: '/api/contactRecord',
    method: 'post',
    data: data
  })
}

// 修改跟进记录
export function updateContactRecord(data) {
  return request({
    url: '/api/contactRecord',
    method: 'put',
    data: data
  })
}

// 删除跟进记录
export function delContactRecord(id) {
  return request({
    url: '/api/contactRecord/' + id,
    method: 'delete'
  })
}
