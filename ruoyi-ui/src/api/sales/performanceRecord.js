import request from '@/utils/request'

// 查询业绩记录列表（分页）
export function listPerformanceRecord(query) {
  return request({
    url: '/performanceRecord/page',
    method: 'get',
    params: query
  })
}

// 查询业绩记录详细
export function getPerformanceRecord(id) {
  return request({
    url: '/performanceRecord/' + id,
    method: 'get'
  })
}

// 按销售代表ID查询
export function listPerformanceBySalesRepId(salesRepId) {
  return request({
    url: '/performanceRecord/listBySalesRepId/' + salesRepId,
    method: 'get'
  })
}

// 新增业绩记录
export function addPerformanceRecord(data) {
  return request({
    url: '/performanceRecord',
    method: 'post',
    data: data
  })
}

// 修改业绩记录
export function updatePerformanceRecord(data) {
  return request({
    url: '/performanceRecord',
    method: 'put',
    data: data
  })
}

// 删除业绩记录
export function delPerformanceRecord(id) {
  return request({
    url: '/performanceRecord/' + id,
    method: 'delete'
  })
}
