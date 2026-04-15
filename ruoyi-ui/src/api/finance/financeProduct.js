import request from '@/utils/request'

// 查询金融产品列表（分页）
export function listFinanceProduct(query) {
  return request({
    url: '/api/financeProduct/page',
    method: 'get',
    params: query
  })
}

// 查询金融产品详细
export function getFinanceProduct(id) {
  return request({
    url: '/api/financeProduct/' + id,
    method: 'get'
  })
}

// 按银行ID查询金融产品
export function listByBankId(bankId) {
  return request({
    url: '/api/financeProduct/listByBankId/' + bankId,
    method: 'get'
  })
}

// 按状态查询
export function listFinanceProductByStatus(status) {
  return request({
    url: '/api/financeProduct/listByStatus',
    method: 'get',
    params: { status }
  })
}

// 新增金融产品
export function addFinanceProduct(data) {
  return request({
    url: '/api/financeProduct',
    method: 'post',
    data: data
  })
}

// 修改金融产品
export function updateFinanceProduct(data) {
  return request({
    url: '/api/financeProduct',
    method: 'put',
    data: data
  })
}

// 删除金融产品
export function delFinanceProduct(id) {
  return request({
    url: '/api/financeProduct/' + id,
    method: 'delete'
  })
}
