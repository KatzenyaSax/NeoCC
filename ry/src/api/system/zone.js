import request from '@/utils/request'

// 查询区域列表（分页）
export function listZone(query) {
  return request({
    url: '/sysZone/page',
    method: 'get',
    params: query
  })
}

// 查询区域详细
export function getZone(id) {
  return request({
    url: '/sysZone/' + id,
    method: 'get'
  })
}

// 查询全部区域
export function listAllZone() {
  return request({
    url: '/sysZone/listAll',
    method: 'get'
  })
}

// 按状态查询
export function listZoneByStatus(status) {
  return request({
    url: '/sysZone/listByStatus',
    method: 'get',
    params: { status }
  })
}

// 新增区域
export function addZone(data) {
  return request({
    url: '/sysZone',
    method: 'post',
    data: data
  })
}

// 修改区域
export function updateZone(data) {
  return request({
    url: '/sysZone',
    method: 'put',
    data: data
  })
}

// 删除区域
export function delZone(id) {
  return request({
    url: '/sysZone/' + id,
    method: 'delete'
  })
}
