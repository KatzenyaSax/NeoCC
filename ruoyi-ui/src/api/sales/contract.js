import request from '@/utils/request'

// 查询合同列表（分页）
export function listContract(query) {
  return request({
    url: '/contract/page',
    method: 'get',
    params: query
  })
}

// 查询合同详细
export function getContract(id) {
  return request({
    url: '/contract/' + id,
    method: 'get'
  })
}

// 根据合同编号查询
export function getContractByNo(contractNo) {
  return request({
    url: '/contract/getByContractNo/' + contractNo,
    method: 'get'
  })
}

// 新增合同
export function addContract(data) {
  return request({
    url: '/contract',
    method: 'post',
    data: data
  })
}

// 修改合同
export function updateContract(data) {
  return request({
    url: '/contract',
    method: 'put',
    data: data
  })
}

// 删除合同
export function delContract(id) {
  return request({
    url: '/contract/' + id,
    method: 'delete'
  })
}

// 签署合同
export function signContract(id) {
  return request({
    url: '/contract/' + id + '/sign',
    method: 'post'
  })
}

// 生成合同编号
export function generateNo() {
  return request({
    url: '/contract/generateNo',
    method: 'get'
  })
}

// 获取合同详情
export function getContractDetail(id) {
  return request({
    url: '/contract/' + id + '/detail',
    method: 'get'
  })
}

// 获取合同详情（含关联名称）
export function getContractDetailWithNames(id) {
  return request({
    url: '/contract/' + id + '/detail-with-names',
    method: 'get'
  })
}
