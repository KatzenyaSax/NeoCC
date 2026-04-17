import request from '@/utils/request'

// 查询贷款审核列表（分页）
export function listLoanAudit(query) {
  return request({
    url: '/api/loanAudit/page',
    method: 'get',
    params: query
  })
}

// 查询贷款审核详细
export function getLoanAudit(id) {
  return request({
    url: '/api/loanAudit/' + id,
    method: 'get'
  })
}

// 根据合同ID查询
export function getLoanAuditByContractId(contractId) {
  return request({
    url: '/api/loanAudit/getByContractId/' + contractId,
    method: 'get'
  })
}

// 新增贷款审核
export function addLoanAudit(data) {
  return request({
    url: '/api/loanAudit',
    method: 'post',
    data: data
  })
}

// 修改贷款审核
export function updateLoanAudit(data) {
  return request({
    url: '/api/loanAudit',
    method: 'put',
    data: data
  })
}

// 删除贷款审核
export function delLoanAudit(id) {
  return request({
    url: '/api/loanAudit/' + id,
    method: 'delete'
  })
}

// 接收申请
export function receiveLoanAudit(id, data) {
  return request({
    url: '/api/loanAudit/' + id + '/receive',
    method: 'post',
    data: data
  })
}

// 审核
export function reviewLoanAudit(id, data) {
  return request({
    url: '/api/loanAudit/' + id + '/review',
    method: 'post',
    data: data
  })
}

// 提交银行
export function submitBankLoanAudit(id, data) {
  return request({
    url: '/api/loanAudit/' + id + '/submit-bank',
    method: 'post',
    data: data
  })
}

// 银行结果
export function bankResultLoanAudit(id, data) {
  return request({
    url: '/api/loanAudit/' + id + '/bank-result',
    method: 'post',
    data: data
  })
}

// 批准
export function approveLoanAudit(id, data) {
  return request({
    url: '/api/loanAudit/' + id + '/approve',
    method: 'post',
    data: data
  })
}

// 拒绝
export function rejectLoanAudit(id, data) {
  return request({
    url: '/api/loanAudit/' + id + '/reject',
    method: 'post',
    data: data
  })
}
