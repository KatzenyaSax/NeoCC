// 统一 HTTP 客户端，所有请求经过此模块
// 自动携带 Authorization Token，403 自动跳转登录页

const API_BASE = 'http://localhost:8086/api';

// 通用请求方法
export async function request(path, options = {}) {
  const token = localStorage.getItem('token');
  const headers = {
    'Content-Type': 'application/json',
    ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
    ...options.headers,
  };

  const res = await fetch(`${API_BASE}${path}`, { ...options, headers });

  // 403 → Token 无效或过期，跳转登录页
  if (res.status === 403) {
    localStorage.removeItem('token');
    localStorage.removeItem('currentUser');
    location.hash = '#/login';
    throw new Error('Unauthorized');
  }

  const json = await res.json();

  if (json.code !== 200) {
    throw new Error(json.message || 'Request failed');
  }

  return json.data;
}

// GET 简写
export async function get(path) {
  return request(path);
}

// POST 简写
export async function post(path, body) {
  return request(path, {
    method: 'POST',
    body: JSON.stringify(body),
  });
}

// PUT 简写
export async function put(path, body) {
  return request(path, {
    method: 'PUT',
    body: JSON.stringify(body),
  });
}

// DELETE 简写
export async function del(path) {
  return request(path, { method: 'DELETE' });
}
