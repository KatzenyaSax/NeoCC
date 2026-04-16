import request from '@/utils/request'

// 分页查询用户列表
export function listUser(params) {
  return request({ url: '/api/sysUser/page', method: 'get', params })
}

// 获取用户详情
export function getUser(id) {
  return request({ url: '/api/sysUser/' + id, method: 'get' })
}

// 新增用户
export function addUser(data) {
  return request({ url: '/api/sysUser', method: 'post', data })
}

// 修改用户
export function updateUser(data) {
  return request({ url: '/api/sysUser', method: 'put', data })
}

// 删除用户
export function delUser(id) {
  return request({ url: '/api/sysUser/' + id, method: 'delete' })
}

// 获取用户的角色ID列表
export function getUserRoles(id) {
  return request({ url: '/api/sysUser/' + id + '/roles', method: 'get' })
}

// 分配角色
export function assignUserRoles(id, roleIds) {
  return request({ url: '/api/sysUser/' + id + '/roles', method: 'put', data: { roleIds } })
}

// 修改密码
export function changePassword(id, data) {
  return request({ url: '/api/sysUser/' + id + '/password', method: 'put', data })
}

// 解锁用户
export function unlockUser(id) {
  return request({ url: '/api/sysUser/' + id + '/unlock', method: 'put' })
}
