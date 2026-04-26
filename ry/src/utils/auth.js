import Cookies from 'js-cookie'

const TokenKey = 'Admin-Token'

export function getToken() {
  return localStorage.getItem(TokenKey) || Cookies.get(TokenKey)
}

export function setToken(token) {
  localStorage.setItem(TokenKey, token)
  Cookies.set(TokenKey, token)
}

export function removeToken() {
  localStorage.removeItem(TokenKey)
  Cookies.remove(TokenKey)
}
