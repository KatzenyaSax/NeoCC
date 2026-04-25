import request from '@/utils/request'

// 分页查询权限列表
export function listPermission(params) {
  return request({ url: '/sysPermission/page', method: 'get', params })
}

// 获取权限树（全部）
export function treePermission() {
  return request({ url: '/sysPermission/tree', method: 'get' })
}

// 获取权限详情
export function getPermission(id) {
  return request({ url: '/sysPermission/' + id, method: 'get' })
}

// 新增权限
export function addPermission(data) {
  return request({ url: '/sysPermission', method: 'post', data })
}

// 修改权限
export function updatePermission(data) {
  return request({ url: '/sysPermission', method: 'put', data })
}

// 删除权限
export function delPermission(id) {
  return request({ url: '/sysPermission/' + id, method: 'delete' })
}

// 获取最小未使用的权限ID
export function getMinUnusedPermissionId() {
  return request({ url: '/sysPermission/min-unused-id', method: 'get' })
}
