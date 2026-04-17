import request from '@/utils/request'

// 查询工作日志列表（分页）
export function listWorkLog(query) {
  return request({
    url: '/api/workLog/page',
    method: 'get',
    params: query
  })
}

// 查询工作日志详细
export function getWorkLog(id) {
  return request({
    url: '/api/workLog/' + id,
    method: 'get'
  })
}

// 按销售代表ID查询
export function listWorkLogBySalesRepId(salesRepId) {
  return request({
    url: '/api/workLog/listBySalesRepId/' + salesRepId,
    method: 'get'
  })
}

// 检查是否重复（同一销售代表同一日期）
export function checkDuplicate(salesRepId, logDate) {
  return request({
    url: '/api/workLog/checkDuplicate',
    method: 'get',
    params: { salesRepId, logDate }
  })
}

// 新增工作日志
export function addWorkLog(data) {
  return request({
    url: '/api/workLog',
    method: 'post',
    data: data
  })
}

// 修改工作日志
export function updateWorkLog(data) {
  return request({
    url: '/api/workLog',
    method: 'put',
    data: data
  })
}

// 删除工作日志
export function delWorkLog(id) {
  return request({
    url: '/api/workLog/' + id,
    method: 'delete'
  })
}
