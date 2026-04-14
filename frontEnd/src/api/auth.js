// 认证相关 API
import { post, request } from './client.js';

// 登录
// POST /api/sysUser/login
// 返回 data.id 即为 token，data 为用户信息
export async function login(username, password) {
  const data = await post('/sysUser/login', { username, password });
  // data.id = userId（内部约定的 Token 格式）
  localStorage.setItem('token', data.id);
  localStorage.setItem('currentUser', JSON.stringify(data));
  return data;
}

// 登出
export function logout() {
  localStorage.removeItem('token');
  localStorage.removeItem('currentUser');
  location.hash = '#/login';
}

// 获取当前用户
export function getCurrentUser() {
  const raw = localStorage.getItem('currentUser');
  return raw ? JSON.parse(raw) : null;
}

// 获取当前用户权限码列表
// GET /api/sysUser/{id}/permCodes
export async function getPermCodes(userId) {
  return request(`/sysUser/${userId}/permCodes`);
}
