import request from '@/utils/request'

// 查询部门列表（分页）
export function listDepartment(query) {
  return request({
    url: '/sysDepartment/page/with-details',
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

// 查询所有部门（下拉用）
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

// 获取最小未使用的部门ID
export function getMinUnusedDeptId() {
  return request({
    url: '/sysDepartment/min-unused-id',
    method: 'get'
  })
}
