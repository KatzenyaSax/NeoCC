import request from '@/utils/request'

// 查询客户列表（分页）
export function listCustomer(query) {
  return request({
    url: '/customer/page',
    method: 'get',
    params: query
  })
}

// 查询客户详细
export function getCustomer(id) {
  return request({
    url: '/customer/' + id,
    method: 'get'
  })
}

// 根据销售代表ID查询客户列表
export function listBySalesRepId(salesRepId) {
  return request({
    url: '/customer/listBySalesRepId/' + salesRepId,
    method: 'get'
  })
}

// 新增客户
export function addCustomer(data) {
  return request({
    url: '/customer',
    method: 'post',
    data: data
  })
}

// 修改客户
export function updateCustomer(data) {
  return request({
    url: '/customer',
    method: 'put',
    data: data
  })
}

// 删除客户
export function delCustomer(id) {
  return request({
    url: '/customer/' + id,
    method: 'delete'
  })
}

// 获取客户详情（聚合所有相关数据）
export function getCustomerView(id) {
  return request({
    url: '/customer/view/' + id,
    method: 'get'
  })
}

// 批量查询用户姓名
export function getUserNamesByIds(ids) {
  return request({
    url: '/sysUser/names/by-ids',
    method: 'post',
    data: ids
  })
}
