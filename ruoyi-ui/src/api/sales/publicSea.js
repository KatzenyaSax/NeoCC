import request from '@/utils/request'

// 公海客户分页列表
export function getPublicSeaPage(query) {
  return request({
    url: '/customer/public-sea/page',
    method: 'get',
    params: query
  })
}

// 转移公海客户
export function transferCustomer(data) {
  return request({
    url: '/customer/public-sea/transfer',
    method: 'put',
    data: data
  })
}

// 获取销售代表列表（下拉用）
export function listSalesReps() {
  return request({
    url: '/customer/sales-reps',
    method: 'get'
  })
}
