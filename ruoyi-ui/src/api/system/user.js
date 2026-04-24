import request from '@/utils/request'

// 分页查询用户列表（/sysUser 路由到 auth 服务）
export function listUser(params) {
  return request({ url: '/sysUser/page', method: 'get', params })
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

// 获取所有部门列表（用于下拉选择）
export function listAllDepartment() {
  return request({ url: '/sysDepartment/listAll', method: 'get' })
}
