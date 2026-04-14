// 路由守卫：检查是否已登录
// 未登录 → 跳转登录页
import { getCurrentUser } from '../api/auth.js';
import { navigate } from './router.js';

export function requireAuth() {
  const user = getCurrentUser();
  if (!user) {
    navigate('/login');
    return false;
  }
  return true;
}
