import request from '@/utils/request'

// 分页查询字典列表
export function listDict(query) {
  return request({
    url: '/sysDict/page',
    method: 'get',
    params: query
  })
}

// 获取字典详情
export function getDict(id) {
  return request({
    url: '/sysDict/' + id,
    method: 'get'
  })
}

// 根据字典类型查询字典项
export function listByDictType(dictType) {
  return request({
    url: '/sysDict/listByDictType',
    method: 'get',
    params: { dictType }
  })
}

// 新增字典
export function addDict(data) {
  return request({
    url: '/sysDict',
    method: 'post',
    data: data
  })
}

// 修改字典
export function updateDict(data) {
  return request({
    url: '/sysDict',
    method: 'put',
    data: data
  })
}

// 删除字典
export function delDict(id) {
  return request({
    url: '/sysDict/' + id,
    method: 'delete'
  })
}

// 获取最小未使用的字典ID
export function getMinUnusedDictId() {
  return request({
    url: '/sysDict/min-unused-id',
    method: 'get'
  })
}
