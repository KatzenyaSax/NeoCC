import request from '@/utils/request'

// 分页查询角色列表
export function listRole(params) {
  return request({ url: '/api/sysRole/page', method: 'get', params })
}

// 获取角色详情
export function getRole(id) {
  return request({ url: '/api/sysRole/' + id, method: 'get' })
}

// 新增角色
export function addRole(data) {
  return request({ url: '/api/sysRole', method: 'post', data })
}

// 修改角色
export function updateRole(data) {
  return request({ url: '/api/sysRole', method: 'put', data })
}

// 删除角色
export function delRole(id) {
  return request({ url: '/api/sysRole/' + id, method: 'delete' })
}

// 获取所有启用的角色（下拉用）
export function listAllRoles() {
  return request({ url: '/api/sysRole/listByStatus', method: 'get', params: { status: 1 } })
}

// 获取角色的权限ID列表
export function getRolePermissions(id) {
  return request({ url: '/api/sysRole/' + id + '/permissions', method: 'get' })
}

// 分配权限
export function assignRolePermissions(id, permissionIds) {
  return request({ url: '/api/sysRole/' + id + '/permissions', method: 'put', data: { permissionIds } })
}
