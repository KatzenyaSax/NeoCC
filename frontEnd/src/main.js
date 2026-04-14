// 入口脚本
// 1. 初始化 store（从 localStorage 恢复登录状态）
// 2. 初始化路由
// 3. 渲染侧边栏
// 4. 根据 hash 渲染对应页面

import { initRouter, registerRoute } from './core/router.js';
import { renderSidebar } from './component/sidebar.js';
import { renderLoginPage } from './page/auth/login.js';
import { renderDashboardPage } from './page/dashboard.js';
import { getCurrentUser } from './api/auth.js';
import { navigate } from './core/router.js';
import store from './core/store.js';

// 恢复登录状态
const savedUser = getCurrentUser();
if (savedUser) {
  store.set('currentUser', savedUser);
}

// 注册 Phase 1 页面路由
registerRoute('/login', renderLoginPage);
registerRoute('/dashboard', renderDashboardPage);

// 渲染侧边栏（登录页也需要渲染，以便登录后侧边栏已就绪）
renderSidebar();

// 初始化路由（触发首次渲染）
initRouter();

// 登录状态检查：已登录用户访问 /login 时直接跳转 dashboard
if (!location.hash || location.hash === '#/login') {
  if (getCurrentUser()) {
    navigate('/dashboard');
  }
}
