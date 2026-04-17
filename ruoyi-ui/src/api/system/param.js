import request from '@/utils/request'

// 分页查询参数列表
export function listParam(query) {
  return request({
    url: '/sysParam/page',
    method: 'get',
    params: query
  })
}

// 获取参数详情
export function getParam(id) {
  return request({
    url: '/sysParam/' + id,
    method: 'get'
  })
}

// 根据参数键查询参数值
export function getParamValue(paramKey) {
  return request({
    url: '/sysParam/value/' + paramKey,
    method: 'get'
  })
}

// 根据参数键查询参数实体
export function getParamByKey(paramKey) {
  return request({
    url: '/sysParam/getByParamKey',
    method: 'get',
    params: { paramKey }
  })
}

// 查询指定分组的参数列表
export function listByParamGroup(paramGroup) {
  return request({
    url: '/sysParam/listByParamGroup',
    method: 'get',
    params: { paramGroup }
  })
}

// 新增参数
export function addParam(data) {
  return request({
    url: '/sysParam',
    method: 'post',
    data: data
  })
}

// 修改参数
export function updateParam(data) {
  return request({
    url: '/sysParam',
    method: 'put',
    data: data
  })
}

// 删除参数
export function delParam(id) {
  return request({
    url: '/sysParam/' + id,
    method: 'delete'
  })
}
