import request from '@/utils/request'

// 获取客户总数
export function countCustomer() {
  return request({
    url: '/customer/count',
    method: 'get'
  })
}

// 获取合同总数
export function countContract() {
  return request({
    url: '/contract/count',
    method: 'get'
  })
}

// 按状态获取合同数量（用于待审贷款等）
export function countContractByStatus(status) {
  return request({
    url: '/contract/count-by-status',
    method: 'get',
    params: { status }
  })
}
