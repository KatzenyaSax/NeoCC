import request from '@/utils/request'

export function login(username, password) {
  return request({
    url: '/auth/login',
    headers: {
      isToken: false,
      repeatSubmit: false
    },
    method: 'post',
    data: { username, password }
  })
}

export function getInfo() {
  return request({
    url: '/auth/getInfo',
    method: 'get'
  })
}

export function logout() {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}
