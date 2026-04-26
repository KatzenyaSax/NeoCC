import request from '@/utils/request'

export const getRouters = () => {
  return request({
    url: '/auth/getRouters',
    method: 'get'
  })
}
