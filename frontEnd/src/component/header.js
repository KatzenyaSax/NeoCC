// 顶部栏组件
// 渲染当前用户信息 + 退出按钮到 #header

import store from '../core/store.js';
import { logout } from '../api/auth.js';

export function renderHeader() {
  const container = document.getElementById('header');
  if (!container) return;

  const user = store.get('currentUser');
  const realName = user ? user.realName || user.username : '未登录';

  container.innerHTML = `
    <div class="header-title">NeoCC 金融 CRM</div>
    <div class="header-user">
      <span class="header-username">${realName}</span>
      <button class="btn btn-text" id="logoutBtn">退出</button>
    </div>
  `;

  document.getElementById('logoutBtn').addEventListener('click', () => {
    logout();
  });
}
