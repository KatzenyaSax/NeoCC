// 登录页
// 路由：/#/login

import { login, getPermCodes } from '../../api/auth.js';
import store from '../../core/store.js';
import { navigate } from '../../core/router.js';
import { toast } from '../../component/toast.js';
import { renderHeader } from '../../component/header.js';
import { loadDicts } from '../../core/dict.js';

export function renderLoginPage() {
  const app = document.getElementById('app');

  app.innerHTML = `
    <div class="login-page">
      <div class="login-box">
        <h1 class="login-title">NeoCC 金融 CRM</h1>
        <form class="login-form" id="loginForm">
          <div class="form-group">
            <label>用户名</label>
            <input type="text" name="username" class="form-input" placeholder="请输入用户名" required />
          </div>
          <div class="form-group">
            <label>密码</label>
            <input type="password" name="password" class="form-input" placeholder="请输入密码" required />
          </div>
          <div class="form-error" id="loginError" style="display:none;"></div>
          <button type="submit" class="btn btn-primary btn-block" id="loginBtn">登录</button>
        </form>
      </div>
    </div>
  `;

  const form = document.getElementById('loginForm');
  const errorEl = document.getElementById('loginError');
  const loginBtn = document.getElementById('loginBtn');

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = form.username.value.trim();
    const password = form.password.value;

    if (!username || !password) {
      showError('请输入用户名和密码');
      return;
    }

    // 禁用按钮，显示 loading
    loginBtn.disabled = true;
    loginBtn.textContent = '登录中...';
    errorEl.style.display = 'none';

    try {
      const user = await login(username, password);

      // 存 currentUser
      store.set('currentUser', user);

      // 加载权限码
      try {
        const permCodes = await getPermCodes(user.id);
        store.set('permissions', permCodes || []);
      } catch (e) {
        console.warn('权限码加载失败', e);
        store.set('permissions', []);
      }

      // 加载字典
      try {
        await loadDicts();
      } catch (e) {
        console.warn('字典加载失败', e);
      }

      toast('登录成功', 'success');
      navigate('/dashboard');

    } catch (err) {
      showError(err.message || '登录失败，请检查用户名和密码');
    } finally {
      loginBtn.disabled = false;
      loginBtn.textContent = '登录';
    }
  });

  function showError(msg) {
    errorEl.textContent = msg;
    errorEl.style.display = 'block';
  }
}
