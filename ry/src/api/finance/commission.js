import request from '@/utils/request'

// 查询佣金记录列表（分页）
export function listCommission(query) {
  return request({
    url: '/commissionRecord/page',
    method: 'get',
    params: query
  })
}

// 查询佣金记录详细
export function getCommission(id) {
  return request({
    url: '/commissionRecord/' + id,
    method: 'get'
  })
}

// 新增佣金记录
export function addCommission(data) {
  return request({
    url: '/commissionRecord',
    method: 'post',
    data: data
  })
}

// 修改佣金记录
export function updateCommission(data) {
  return request({
    url: '/commissionRecord',
    method: 'put',
    data: data
  })
}

// 删除佣金记录
export function delCommission(id) {
  return request({
    url: '/commissionRecord/' + id,
    method: 'delete'
  })
}

// 确认佣金
export function confirmCommission(id) {
  return request({
    url: '/commissionRecord/' + id + '/confirm',
    method: 'post'
  })
}

// 发放佣金
export function grantCommission(id, data) {
  return request({
    url: '/commissionRecord/' + id + '/grant',
    method: 'post',
    data: data
  })
}
