import request from '@/utils/request'

// 查询银行列表（分页）
export function listBank(query) {
  return request({
    url: '/bank/page',
    method: 'get',
    params: query
  })
}

// 查询银行详细
export function getBank(id) {
  return request({
    url: '/bank/' + id,
    method: 'get'
  })
}

// 按状态查询银行列表
export function listBankByStatus(status) {
  return request({
    url: '/bank/listByStatus',
    method: 'get',
    params: { status }
  })
}

// 新增银行
export function addBank(data) {
  return request({
    url: '/bank',
    method: 'post',
    data: data
  })
}

// 修改银行
export function updateBank(data) {
  return request({
    url: '/bank',
    method: 'put',
    data: data
  })
}

// 删除银行
export function delBank(id) {
  return request({
    url: '/bank/' + id,
    method: 'delete'
  })
}

// 获取最小未使用的ID
export function getMinUnusedBankId() {
  return request({
    url: '/bank/min-unused-id',
    method: 'get'
  })
}
