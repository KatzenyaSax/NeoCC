import request from '@/utils/request'

// 查询部门列表（分页）
export function listDepartment(query) {
  return request({
    url: '/sysDepartment/page',
    method: 'get',
    params: query
  })
}

// 查询部门详细
export function getDepartment(id) {
  return request({
    url: '/sysDepartment/' + id,
    method: 'get'
  })
}

// 按父级ID查询
export function listByParentId(parentId) {
  return request({
    url: '/sysDepartment/listByParentId/' + parentId,
    method: 'get'
  })
}

// 按区域ID查询
export function listByZoneId(zoneId) {
  return request({
    url: '/sysDepartment/listByZoneId/' + zoneId,
    method: 'get'
  })
}

// 获取所有部门列表（用于下拉选择）
export function listAllDepartment() {
  return request({
    url: '/sysDepartment/listAll',
    method: 'get'
  })
}

// 新增部门
export function addDepartment(data) {
  return request({
    url: '/sysDepartment',
    method: 'post',
    data: data
  })
}

// 修改部门
export function updateDepartment(data) {
  return request({
    url: '/sysDepartment',
    method: 'put',
    data: data
  })
}

// 删除部门
export function delDepartment(id) {
  return request({
    url: '/sysDepartment/' + id,
    method: 'delete'
  })
}
