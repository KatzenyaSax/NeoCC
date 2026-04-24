import request from '@/utils/request'

// 获取用户总数
export function countUser() {
  return request({
    url: '/sysUser/count',
    method: 'get'
  })
}
