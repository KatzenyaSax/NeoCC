import request from '@/utils/request'

// 查询业绩记录列表（分页）
export function listPerformanceRecord(query) {
  return request({
    url: '/api/performanceRecord/page',
    method: 'get',
    params: query
  })
}

// 查询业绩记录详细
export function getPerformanceRecord(id) {
  return request({
    url: '/api/performanceRecord/' + id,
    method: 'get'
  })
}

// 按销售代表ID查询
export function listPerformanceBySalesRepId(salesRepId) {
  return request({
    url: '/api/performanceRecord/listBySalesRepId/' + salesRepId,
    method: 'get'
  })
}

// 新增业绩记录
export function addPerformanceRecord(data) {
  return request({
    url: '/api/performanceRecord',
    method: 'post',
    data: data
  })
}

// 修改业绩记录
export function updatePerformanceRecord(data) {
  return request({
    url: '/api/performanceRecord',
    method: 'put',
    data: data
  })
}

// 删除业绩记录
export function delPerformanceRecord(id) {
  return request({
    url: '/api/performanceRecord/' + id,
    method: 'delete'
  })
}
