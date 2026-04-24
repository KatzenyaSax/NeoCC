import request from '@/utils/request'

// 分页查询用户列表
export function listUser(params) {
  return request({ url: '/sysUser/page/with-dept', method: 'get', params })
}

// 获取用户详情
export function getUser(id) {
  return request({ url: '/sysUser/' + id, method: 'get' })
}

// 新增用户
export function addUser(data) {
  return request({ url: '/sysUser', method: 'post', data })
}

// 修改用户
export function updateUser(data) {
  return request({ url: '/sysUser', method: 'put', data })
}

// 删除用户
export function delUser(id) {
  return request({ url: '/sysUser/' + id, method: 'delete' })
}

// 获取用户的角色ID列表
export function getUserRoles(id) {
  return request({ url: '/sysUser/' + id + '/roles', method: 'get' })
}

// 分配角色
export function assignUserRoles(id, roleIds) {
  return request({ url: '/sysUser/' + id + '/roles', method: 'put', data: { roleIds } })
}

// 修改密码
export function changePassword(id, data) {
  return request({ url: '/sysUser/' + id + '/password', method: 'put', data })
}

// 解锁用户
export function unlockUser(id) {
  return request({ url: '/sysUser/' + id + '/unlock', method: 'put' })
}

// 按部门ID查询用户ID列表
export function listUserIdsByDeptId(deptId) {
  return request({ url: '/sysUser/ids-by-dept/' + deptId, method: 'get' })
}

// 按战区ID查询用户ID列表
export function listUserIdsByZoneId(zoneId) {
  return request({ url: '/sysUser/ids-by-zone/' + zoneId, method: 'get' })
}

// 按角色ID列表查询用户（下拉用）
export function listUsersByRoleIds(data) {
  return request({ url: '/sysUser/by-role-ids', method: 'post', data })
}

// 获取最小可用用户ID
export function getMinAvailableId() {
  return request({ url: '/sysUser/min-available-id', method: 'get' })
}

// 获取销售代表列表
export function listSalesReps() {
  return request({ url: '/sysUser/sales-reps', method: 'get' })
}
