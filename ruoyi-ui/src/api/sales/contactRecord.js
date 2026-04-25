import request from '@/utils/request'

// 查询跟进记录列表（分页）
export function listContactRecord(query) {
  return request({
    url: '/contactRecord/page',
    method: 'get',
    params: query
  })
}

// 按角色条件查询跟进记录列表
export function listContactRecordByRoleConditions(params) {
  return request({
    url: '/contactRecord/list-by-role',
    method: 'get',
    params
  })
}

// 查询跟进记录详细
export function getContactRecord(id) {
  return request({
    url: '/contactRecord/' + id,
    method: 'get'
  })
}

// 按客户ID查询
export function listByCustomerId(customerId) {
  return request({
    url: '/contactRecord/listByCustomerId/' + customerId,
    method: 'get'
  })
}

// 按销售代表ID查询
export function listBySalesRepId(salesRepId) {
  return request({
    url: '/contactRecord/listBySalesRepId/' + salesRepId,
    method: 'get'
  })
}

// 根据销售代表ID列表查询跟进记录
export function listBySalesRepIds(salesRepIds) {
  return request({
    url: '/contactRecord/listBySalesRepIds',
    method: 'get',
    params: { salesRepIds }
  })
}

// 新增跟进记录
export function addContactRecord(data) {
  return request({
    url: '/contactRecord',
    method: 'post',
    data: data
  })
}

// 修改跟进记录
export function updateContactRecord(data) {
  return request({
    url: '/contactRecord',
    method: 'put',
    data: data
  })
}

// 删除跟进记录
export function delContactRecord(id) {
  return request({
    url: '/contactRecord/' + id,
    method: 'delete'
  })
}

// 获取最小未使用ID
export function getMinUnusedIdContactRecord() {
  return request({
    url: '/contactRecord/min-unused-id',
    method: 'get'
  })
}
