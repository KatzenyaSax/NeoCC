# Phase 1 前端基础设施开发文档

**Phase:** 1 / 5
**目标:** 搭建项目骨架，实现登录 + 仪表盘，使前后端联调通道打通
**约束:** 原生 JavaScript（ES6+），无框架依赖，所有模块通过 `<script type="module">` 直接加载

---

## 一、交付物清单

| 任务 | 文件路径 | 验收标准 |
|------|---------|---------|
| P1.1 | `package.json`, `serve.json`, 目录结构 | `npm install && npm run dev` 可运行 |
| P1.2 | `src/api/client.js` | 所有 HTTP 请求经过此 client，403 自动跳转登录 |
| P1.3 | `src/core/router.js` | `/#/login` 和 `/#/dashboard` 可正常切换 |
| P1.4 | `src/core/store.js`, `src/core/dict.js` | 状态全局可用，字典加载成功 |
| P1.5 | `src/component/` 组件集 | sidebar / header / table / modal / select / toast 可渲染 |
| P1.6 | `src/page/auth/login.js` + 模板 | 登录成功跳转 dashboard |
| P1.7 | `src/page/dashboard.js` | 仪表盘数据展示 |

**验收总标准:** 打开 `http://localhost:3000` → 显示登录页 → 登录成功 → 展示仪表盘 + 侧边栏

---

## 二、目录结构

```
frontEnd/
├── index.html              # SPA 入口，唯一 HTML 文件
├── package.json            # 开发依赖（仅 serve，无构建工具）
├── serve.json              # serve 静态服务器配置
└── src/
    ├── main.js             # 入口脚本：初始化路由 + 渲染布局
    ├── api/
    │   ├── client.js       # 统一 HTTP 客户端
    │   ├── auth.js         # 认证 API
    │   ├── system.js       # system 模块 API（ Phase2 ）
    │   ├── sales.js        # sales 模块 API（ Phase3 ）
    │   └── finance.js      # finance 模块 API（ Phase4 ）
    ├── core/
    │   ├── router.js       # Hash 路由
    │   ├── store.js        # 极简状态管理
    │   ├── auth.js         # 认证中间件（路由守卫）
    │   └── dict.js          # 字典缓存
    ├── component/
    │   ├── sidebar.js      # 侧边栏
    │   ├── header.js       # 顶部栏
    │   ├── table.js        # 通用数据表格
    │   ├── modal.js        # 弹窗封装
    │   ├── select.js       # 字典下拉选择
    │   └── toast.js        # 操作反馈提示
    ├── page/
    │   ├── auth/
    │   │   └── login.js    # 登录页
    │   └── dashboard.js    # 仪表盘
    └── assets/
        └── css/
            ├── variables.css   # CSS 变量（主题色、间距）
            ├── base.css        # 重置样式 + 字体
            ├── layout.css      # 布局（sidebar + main）
            └── component.css   # 通用组件样式
```

---

## 三、文件实现

### P1.1 — `package.json`

```json
{
  "name": "neocc-frontend",
  "version": "1.0.0",
  "description": "大富翁金融 CRM 前端（原生 JS）",
  "scripts": {
    "dev": "serve . -l 3000 --cors",
    "start": "npm run dev"
  },
  "devDependencies": {
    "serve": "^14.2.0"
  }
}
```

### P1.1 — `serve.json`

```json
{
  "public": ".",
  "clean-urls": false,
  "single": false
}
```

### P1.1 — `index.html`

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>NeoCC 金融 CRM</title>
  <link rel="stylesheet" href="/src/assets/css/variables.css" />
  <link rel="stylesheet" href="/src/assets/css/base.css" />
  <link rel="stylesheet" href="/src/assets/css/layout.css" />
  <link rel="stylesheet" href="/src/assets/css/component.css" />
</head>
<body>
  <!-- 侧边栏渲染到这里 -->
  <div id="sidebar"></div>

  <!-- 主区域 -->
  <div class="main">
    <!-- 顶部栏渲染到这里 -->
    <div id="header"></div>
    <!-- 页面内容渲染到这里 -->
    <div id="app" class="content"></div>
  </div>

  <script type="module" src="/src/main.js"></script>
</body>
</html>
```

---

### P1.2 — `src/api/client.js`

```javascript
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
```

### P1.2 — `src/api/auth.js`

```javascript
// 认证相关 API
import { post } from './client.js';

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
  const { request } = await import('./client.js');
  return request(`/sysUser/${userId}/permCodes`);
}
```

---

### P1.3 — `src/core/router.js`

```javascript
// Hash 路由实现
// 路由表：path → render 函数
// 使用方式：navigate('/dashboard')

const routes = {};

// 外部注册路由
export function registerRoute(path, fn) {
  routes[path] = fn;
}

// 路由跳转
export function navigate(path) {
  location.hash = '#' + path;
}

// 初始化路由监听
export function initRouter() {
  window.addEventListener('hashchange', handleRoute);
  // 触发首次路由
  handleRoute();
}

// 处理路由
function handleRoute() {
  const hash = location.hash.slice(1) || '/login';

  // 1. 精确匹配
  if (routes[hash]) {
    clearApp();
    routes[hash]();
    return;
  }

  // 2. 带参数匹配（如 /sales/customer/:id）
  for (const pattern of Object.keys(routes)) {
    const { regex, paramNames } = buildRegex(pattern);
    const match = hash.match(regex);
    if (match) {
      clearApp();
      const params = paramNames.map((_, i) => match[i + 1]);
      routes[pattern](...params);
      return;
    }
  }

  // 3. 404
  clearApp();
  document.getElementById('app').innerHTML = '<p>404 Not Found</p>';
}

function clearApp() {
  document.getElementById('app').innerHTML = '';
}

// 缓存正则表达式，避免重复构建
const regexCache = new Map();

function buildRegex(pattern) {
  if (regexCache.has(pattern)) return regexCache.get(pattern);

  const paramNames = [];
  const regexStr = pattern.replace(/:([^/]+)/g, (_, name) => {
    paramNames.push(name);
    return '([^/]+)';
  });

  const result = { regex: new RegExp(`^${regexStr}$`), paramNames };
  regexCache.set(pattern, result);
  return result;
}
```

### P1.3 — `src/core/auth.js`

```javascript
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
```

---

### P1.4 — `src/core/store.js`

```javascript
// 极简 pub/sub 状态管理
// 使用方式：
//   store.get('token')
//   store.set('token', 'abc')
//   store.subscribe(state => console.log(state))

const store = {
  state: {
    token: localStorage.getItem('token') || null,
    currentUser: null,          // 当前登录用户对象
    dicts: {},                   // { dictType: [ {dictLabel, dictValue}, ... ] }
    permissions: [],            // 当前用户权限码列表
    menuItems: [],               // 侧边栏菜单项
  },
  listeners: [],

  get(key) {
    return this.state[key];
  },

  set(key, value) {
    this.state[key] = value;
    this.listeners.forEach(fn => fn(this.state));
  },

  subscribe(fn) {
    this.listeners.push(fn);
    return () => {
      this.listeners = this.listeners.filter(f => f !== fn);
    };
  },
};

export default store;
```

### P1.4 — `src/core/dict.js`

```javascript
// 字典加载与查询
// 从后端 system 模块加载常用字典，存入 store

import store from './store.js';
import { get } from '../api/client.js';

const COMMON_DICTS = [
  'customer_status',   // 客户状态
  'customer_type',      // 客户类型
  'intention_level',    // 意向等级
  'contract_status',    // 合同状态
  'audit_status',       // 贷款审核状态
  'commission_status',  // 提成状态
  'service_fee_type',         // 服务费类型
  'service_fee_payment_status', // 服务费支付状态
  'performance_status', // 业绩状态
  'contact_type',       // 跟进类型
];

// 加载所有常用字典
export async function loadDicts() {
  const promises = COMMON_DICTS.map(dictType =>
    get(`/sysDict/listByDictType/${dictType}`)
      .then(items => ({ [dictType]: items }))
      .catch(() => ({ [dictType]: [] }))
  );

  const results = await Promise.all(promises);
  const allDicts = { ...store.get('dicts') };

  results.forEach(r => {
    const [key, value] = Object.entries(r)[0];
    allDicts[key] = value;
  });

  store.set('dicts', allDicts);
}

// 根据字典值查标签
export function getDictLabel(dictType, dictValue) {
  const items = store.get('dicts')[dictType] || [];
  const item = items.find(i => String(i.dictValue) === String(dictValue));
  return item ? item.dictLabel : dictValue ?? '';
}
```

---

### P1.5 — `src/component/toast.js`

```javascript
// Toast 提示组件
// 用法：toast('操作成功', 'success')
// type: info | success | error | warning

export function toast(message, type = 'info') {
  const el = document.createElement('div');
  el.className = `toast toast-${type}`;
  el.textContent = message;
  document.body.appendChild(el);

  // 3 秒后自动移除
  setTimeout(() => {
    el.classList.add('toast-fade-out');
    setTimeout(() => el.remove(), 300);
  }, 3000);
}
```

### P1.5 — `src/component/sidebar.js`

```javascript
// 侧边栏组件
// 将渲染结果写入 #sidebar DOM 元素

import store from '../core/store.js';
import { navigate } from '../core/router.js';

export function renderSidebar() {
  const container = document.getElementById('sidebar');
  if (!container) return;

  const menuItems = store.get('menuItems') || getDefaultMenu();

  container.innerHTML = `
    <div class="sidebar-logo">
      <span>NeoCC</span>
    </div>
    <nav class="sidebar-nav">
      ${menuItems.map(item => `
        <div class="sidebar-group">
          ${item.children
            ? `<div class="sidebar-group-title">${item.label}</div>
               ${item.children.map(child => `
                 <a class="sidebar-link" data-path="${child.path}" href="#${child.path}">
                   ${child.label}
                 </a>
               `).join('')}`
            : `<a class="sidebar-link" data-path="${item.path}" href="#${item.path}">
                 ${item.label}
               </a>`
          }
        </div>
      `).join('')}
    </nav>
  `;

  // 绑定点击事件（事件委托）
  container.addEventListener('click', (e) => {
    const link = e.target.closest('.sidebar-link');
    if (link) {
      e.preventDefault();
      const path = link.dataset.path;
      if (path) navigate(path);
    }
  });
}

function getDefaultMenu() {
  return [
    { label: '工作台', path: '/dashboard' },
    {
      label: '系统管理',
      children: [
        { label: '用户管理', path: '/system/user' },
        { label: '角色管理', path: '/system/role' },
        { label: '权限管理', path: '/system/permission' },
        { label: '部门管理', path: '/system/department' },
        { label: '战区管理', path: '/system/zone' },
        { label: '数据字典', path: '/system/dict' },
        { label: '系统参数', path: '/system/param' },
        { label: '操作日志', path: '/system/log' },
      ],
    },
    {
      label: '销售管理',
      children: [
        { label: '客户管理', path: '/sales/customer' },
        { label: '合同管理', path: '/sales/contract' },
        { label: '工作日志', path: '/sales/worklog' },
      ],
    },
    {
      label: '金融审核',
      children: [
        { label: '银行管理', path: '/finance/bank' },
        { label: '金融产品', path: '/finance/product' },
        { label: '贷款审核', path: '/finance/loan-audit' },
        { label: '提成记录', path: '/finance/commission' },
        { label: '服务费记录', path: '/finance/service-fee' },
      ],
    },
  ];
}
```

### P1.5 — `src/component/header.js`

```javascript
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
```

### P1.5 — `src/component/table.js`

```javascript
// 通用数据表格组件
// 用法：
//   renderTable(container, columns, records, pagination, onPageChange)
//   columns: [ { label, field, render?: (val, row) => string } ]
//   pagination: { total, page, size } 或 null（无分页）

export function renderTable(container, columns, records, pagination, onPageChange) {
  if (typeof container === 'string') {
    container = document.querySelector(container);
  }

  // 表头
  const thead = `
    <thead>
      <tr>
        ${columns.map(col => `<th>${col.label}</th>`).join('')}
      </tr>
    </thead>
  `;

  // 数据行
  const tbody = `
    <tbody>
      ${records.length === 0
        ? `<tr><td colspan="${columns.length}" class="table-empty">暂无数据</td></tr>`
        : records.map(row => `
          <tr>
            ${columns.map(col => {
              const val = row[col.field];
              const text = col.render ? col.render(val, row) : (val ?? '');
              return `<td>${text}</td>`;
            }).join('')}
          </tr>
        `).join('')
      }
    </tbody>
  `;

  // 分页
  let paginationHtml = '';
  if (pagination) {
    const { total, page, size } = pagination;
    const totalPages = Math.ceil(total / size);
    paginationHtml = `
      <div class="pagination">
        <span class="pagination-info">共 ${total} 条</span>
        <button class="btn btn-sm" ${page <= 1 ? 'disabled' : ''} data-page="${page - 1}">上一页</button>
        <span class="pagination-current">第 ${page} / ${totalPages} 页</span>
        <button class="btn btn-sm" ${page >= totalPages ? 'disabled' : ''} data-page="${page + 1}">下一页</button>
      </div>
    `;
  }

  container.innerHTML = `
    <table class="data-table">${thead}${tbody}</table>
    ${paginationHtml}
  `;

  // 绑定分页事件
  if (pagination && onPageChange) {
    container.querySelectorAll('.pagination button[data-page]').forEach(btn => {
      btn.addEventListener('click', () => {
        const newPage = parseInt(btn.dataset.page);
        if (!btn.disabled) onPageChange(newPage);
      });
    });
  }
}
```

### P1.5 — `src/component/modal.js`

```javascript
// 弹窗封装
// 用法：
//   modal({ title, content, onConfirm, confirmText: '确定', onCancel })
//   返回 close() 函数，可手动关闭

export function modal({ title, content, onConfirm, confirmText = '确定', onCancel }) {
  // 遮罩
  const overlay = document.createElement('div');
  overlay.className = 'modal-overlay';

  overlay.innerHTML = `
    <div class="modal-box">
      <div class="modal-header">
        <span class="modal-title">${title}</span>
        <button class="modal-close" id="modalCloseBtn">&times;</button>
      </div>
      <div class="modal-body">${content}</div>
      <div class="modal-footer">
        <button class="btn" id="modalCancelBtn">取消</button>
        <button class="btn btn-primary" id="modalConfirmBtn">${confirmText}</button>
      </div>
    </div>
  `;

  document.body.appendChild(overlay);

  const close = () => {
    overlay.remove();
  };

  overlay.querySelector('#modalCloseBtn').addEventListener('click', close);
  overlay.querySelector('#modalCancelBtn').addEventListener('click', () => {
    if (onCancel) onCancel();
    close();
  });
  overlay.querySelector('#modalConfirmBtn').addEventListener('click', () => {
    if (onConfirm) onConfirm();
    close();
  });

  // 点击遮罩关闭
  overlay.addEventListener('click', (e) => {
    if (e.target === overlay) {
      if (onCancel) onCancel();
      close();
    }
  });

  return { close };
}
```

### P1.5 — `src/component/select.js`

```javascript
// 字典下拉选择框
// 用法：renderDictSelect(el, 'customer_status', { value: '1', onChange })
// el: DOM 元素或选择器字符串

import store from '../core/store.js';

export function renderDictSelect(el, dictType, { value = '', placeholder = '请选择', onChange } = {}) {
  if (typeof el === 'string') {
    el = document.querySelector(el);
  }
  if (!el) return;

  const items = store.get('dicts')[dictType] || [];

  el.innerHTML = `
    <select class="dict-select" data-dict-type="${dictType}">
      <option value="">${placeholder}</option>
      ${items.map(item => `
        <option value="${item.dictValue}" ${String(item.dictValue) === String(value) ? 'selected' : ''}>
          ${item.dictLabel}
        </option>
      `).join('')}
    </select>
  `;

  const select = el.querySelector('select');
  select.addEventListener('change', () => {
    if (onChange) onChange(select.value);
  });
}
```

---

### P1.6 — `src/page/auth/login.js`

```javascript
// 登录页
// 路由：/#/login

import { login } from '../../api/auth.js';
import { getPermCodes } from '../../api/auth.js';
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
```

### P1.7 — `src/page/dashboard.js`

```javascript
// 仪表盘页
// 路由：/#/dashboard

import { requireAuth } from '../core/auth.js';
import { get } from '../api/client.js';
import { renderHeader } from '../component/header.js';

export function renderDashboardPage() {
  if (!requireAuth()) return;

  renderHeader();

  const app = document.getElementById('app');
  app.innerHTML = `
    <div class="page-header">
      <h2>工作台</h2>
    </div>
    <div class="dashboard-loading" id="dashboardLoading">加载中...</div>
    <div class="dashboard-content" id="dashboardContent" style="display:none;">
      <div class="stat-cards" id="statCards"></div>
      <div class="dashboard-recent" id="recentLists"></div>
    </div>
    <div class="dashboard-error" id="dashboardError" style="display:none;"></div>
  `;

  loadDashboardData();
}

async function loadDashboardData() {
  const loadingEl = document.getElementById('dashboardLoading');
  const contentEl = document.getElementById('dashboardContent');
  const errorEl = document.getElementById('dashboardError');

  try {
    // 并行请求多个数据
    const [customerStats, contractStats] = await Promise.all([
      get('/customer/listByStatus').catch(() => []),
      get('/contract/listByStatus').catch(() => []),
    ]);

    loadingEl.style.display = 'none';
    contentEl.style.display = 'block';

    renderStatCards(customerStats, contractStats);

  } catch (err) {
    loadingEl.style.display = 'none';
    errorEl.style.display = 'block';
    errorEl.textContent = '数据加载失败：' + err.message;
  }
}

function renderStatCards(customerStats, contractStats) {
  const container = document.getElementById('statCards');
  if (!container) return;

  // 计算合计
  const customerTotal = Array.isArray(customerStats)
    ? customerStats.reduce((sum, s) => sum + (s.count || 0), 0)
    : 0;
  const contractTotal = Array.isArray(contractStats)
    ? contractStats.reduce((sum, s) => sum + (s.count || 0), 0)
    : 0;

  container.innerHTML = `
    <div class="stat-card">
      <div class="stat-label">客户总数</div>
      <div class="stat-value">${customerTotal}</div>
    </div>
    <div class="stat-card">
      <div class="stat-label">合同总数</div>
      <div class="stat-value">${contractTotal}</div>
    </div>
    <div class="stat-card">
      <div class="stat-label">今日日期</div>
      <div class="stat-value">${new Date().toLocaleDateString()}</div>
    </div>
  `;
}
```

---

### P1.4 — `src/main.js`（入口脚本）

```javascript
// 入口脚本
// 1. 初始化 store（从 localStorage 恢复登录状态）
// 2. 初始化路由
// 3. 渲染侧边栏
// 4. 根据 hash 渲染对应页面

import { initRouter } from './core/router.js';
import { registerRoute } from './core/router.js';
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
```

---

### CSS 文件

#### `src/assets/css/variables.css`

```css
/* CSS 变量 - 主题配置 */
:root {
  --color-primary: #1890ff;
  --color-success: #52c41a;
  --color-error: #ff4d4f;
  --color-warning: #faad14;
  --color-bg: #f5f5f5;
  --color-text: #333;
  --color-text-secondary: #999;
  --color-border: #e8e8e8;
  --color-white: #fff;

  --sidebar-width: 220px;
  --header-height: 56px;

  --radius: 4px;
  --shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  --transition: all 0.2s ease;
}
```

#### `src/assets/css/base.css`

```css
/* 重置 + 字体 */
*, *::before, *::after { box-sizing: border-box; }

body {
  margin: 0;
  font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Microsoft YaHei', sans-serif;
  font-size: 14px;
  color: var(--color-text);
  background: var(--color-bg);
}

a { text-decoration: none; color: inherit; }
button { cursor: pointer; border: none; outline: none; }
input, select, textarea { outline: none; }

/* 通用工具类 */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 16px;
  border-radius: var(--radius);
  font-size: 14px;
  transition: var(--transition);
  background: var(--color-white);
  border: 1px solid var(--color-border);
  color: var(--color-text);
}
.btn:hover { opacity: 0.85; }
.btn:disabled { opacity: 0.5; cursor: not-allowed; }
.btn-primary { background: var(--color-primary); color: #fff; border-color: var(--color-primary); }
.btn-text { background: transparent; border: none; color: var(--color-text-secondary); }
.btn-sm { padding: 4px 12px; font-size: 12px; }
.btn-block { width: 100%; }

.form-group { margin-bottom: 16px; }
.form-group label { display: block; margin-bottom: 6px; font-weight: 500; }
.form-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  font-size: 14px;
  transition: var(--transition);
}
.form-input:focus { border-color: var(--color-primary); }
.form-error { color: var(--color-error); font-size: 12px; margin-bottom: 8px; }
```

#### `src/assets/css/layout.css`

```css
/* 布局 */
#app { display: flex; min-height: 100vh; }

/* 侧边栏 */
.sidebar {
  width: var(--sidebar-width);
  background: #001529;
  position: fixed;
  top: 0; left: 0; bottom: 0;
  overflow-y: auto;
  z-index: 100;
}
.sidebar-logo {
  height: var(--header-height);
  display: flex;
  align-items: center;
  padding: 0 20px;
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}
.sidebar-nav { padding: 12px 0; }
.sidebar-group { margin-bottom: 4px; }
.sidebar-group-title {
  padding: 8px 20px;
  color: rgba(255,255,255,0.45);
  font-size: 12px;
}
.sidebar-link {
  display: block;
  padding: 10px 20px;
  color: rgba(255,255,255,0.85);
  font-size: 14px;
  transition: var(--transition);
}
.sidebar-link:hover { background: rgba(255,255,255,0.08); color: #fff; }
.sidebar-link.active { background: var(--color-primary); color: #fff; }

/* 主区域 */
.main { margin-left: var(--sidebar-width); flex: 1; display: flex; flex-direction: column; min-height: 100vh; }

/* 顶部栏 */
.header {
  height: var(--header-height);
  background: var(--color-white);
  border-bottom: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 50;
}
.header-title { font-size: 16px; font-weight: 600; }
.header-user { display: flex; align-items: center; gap: 12px; }
.header-username { color: var(--color-text-secondary); }

/* 内容区 */
.content { padding: 24px; flex: 1; }

/* 页面标题 */
.page-header { margin-bottom: 20px; }
.page-header h2 { margin: 0; font-size: 20px; font-weight: 600; }
```

#### `src/assets/css/component.css`

```css
/* 通用组件样式 */

/* Toast */
.toast {
  position: fixed;
  top: 20px;
  right: 20px;
  padding: 12px 20px;
  border-radius: var(--radius);
  z-index: 9999;
  font-size: 14px;
  box-shadow: var(--shadow);
  animation: toastIn 0.3s ease;
}
.toast-fade-out { animation: toastOut 0.3s ease forwards; }
@keyframes toastIn { from { opacity: 0; transform: translateY(-10px); } }
@keyframes toastOut { to { opacity: 0; transform: translateY(-10px); } }
.toast-success { background: var(--color-success); color: #fff; }
.toast-error { background: var(--color-error); color: #fff; }
.toast-info { background: var(--color-primary); color: #fff; }
.toast-warning { background: var(--color-warning); color: #fff; }

/* 登录页 */
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #001529 0%, #1890ff 100%);
}
.login-box {
  width: 380px;
  padding: 40px;
  background: var(--color-white);
  border-radius: 8px;
  box-shadow: var(--shadow);
}
.login-title {
  text-align: center;
  margin: 0 0 32px;
  font-size: 24px;
  font-weight: 600;
  color: var(--color-text);
}

/* 数据表格 */
.data-table {
  width: 100%;
  border-collapse: collapse;
  background: var(--color-white);
  border-radius: var(--radius);
  overflow: hidden;
  box-shadow: var(--shadow);
}
.data-table th,
.data-table td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid var(--color-border);
}
.data-table th {
  background: #fafafa;
  font-weight: 600;
  font-size: 13px;
  color: var(--color-text-secondary);
}
.data-table tr:last-child td { border-bottom: none; }
.data-table tr:hover td { background: #f5f5f5; }
.table-empty { text-align: center; color: var(--color-text-secondary); padding: 40px !important; }

/* 分页 */
.pagination {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 16px;
  justify-content: flex-end;
}
.pagination-info { color: var(--color-text-secondary); font-size: 13px; }
.pagination-current { color: var(--color-text-secondary); font-size: 13px; }

/* Modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.modal-box {
  background: var(--color-white);
  border-radius: 8px;
  min-width: 400px;
  max-width: 600px;
  box-shadow: var(--shadow);
}
.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--color-border);
}
.modal-title { font-size: 16px; font-weight: 600; }
.modal-close { background: none; border: none; font-size: 20px; color: var(--color-text-secondary); }
.modal-body { padding: 20px; }
.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid var(--color-border);
}

/* 字典 Select */
.dict-select {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  font-size: 14px;
  background: var(--color-white);
  cursor: pointer;
}
.dict-select:focus { border-color: var(--color-primary); }

/* 仪表盘 */
.stat-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}
.stat-card {
  background: var(--color-white);
  padding: 20px;
  border-radius: var(--radius);
  box-shadow: var(--shadow);
}
.stat-label { color: var(--color-text-secondary); font-size: 13px; margin-bottom: 8px; }
.stat-value { font-size: 28px; font-weight: 600; color: var(--color-text); }
.dashboard-loading,
.dashboard-error { padding: 40px; text-align: center; }
.dashboard-error { color: var(--color-error); }
```

---

## 四、Phase 1 启动步骤

### 4.1 环境准备

```bash
cd frontEnd
npm install
```

### 4.2 启动开发服务器

```bash
npm run dev
# 输出：> serve . -l 3000 --cors
# 访问 http://localhost:3000
```

### 4.3 后端联调准备

确保以下后端服务已启动：

| 服务 | 端口 | 用途 |
|------|------|------|
| MySQL | 3306 | 主数据库 |
| Redis | 6379 | 缓存 |
| Nacos | 8848 | 服务注册 |
| RabbitMQ | 5672/15672 | MQ |
| dafuweng-gateway | 8086 | API 网关 |
| dafuweng-auth | 8081 | 认证服务 |
| dafuweng-system | 8082 | 系统服务 |
| dafuweng-sales | 8083 | 销售服务 |
| dafuweng-finance | 8084 | 金融服务 |

### 4.4 登录联调验证

1. 打开 `http://localhost:3000`，自动跳转 `/#/login`
2. 输入后端已有用户（默认测试账号需提前插入数据库）
3. 点击登录，成功则跳转 `/#/dashboard`，侧边栏和顶部栏正常渲染
4. 点击顶部"退出"，返回登录页

---

## 五、Phase 1 验收标准

| # | 检查项 | 预期结果 |
|---|--------|---------|
| 1 | `npm install` 无报错 | node_modules 安装完成 |
| 2 | `npm run dev` 启动成功 | `localhost:3000` 可访问 |
| 3 | 打开首页自动跳转登录页 | URL 变为 `/#/login`，显示登录框 |
| 4 | 输入错误密码 | 显示错误提示，不跳转 |
| 5 | 输入正确密码登录 | 跳转 `/#/dashboard`，无 console error |
| 6 | 仪表盘显示数据卡片 | 3 个 stat-card 正常渲染（可为 0） |
| 7 | 侧边栏显示菜单 | 4 个菜单组（工作台/系统管理/销售管理/金融审核）|
| 8 | 点击侧边栏菜单项 | URL 变化，内容区显示对应页面（Phase 2 后完整） |
| 9 | 点击顶部"退出" | 返回登录页，localStorage 清空 |
| 10 | F12 控制台 | 无 Error 级别报错 |

---

## 六、已知限制（Phase 1 不可解决，标注给前端人员）

| 限制 | 说明 | 备注 |
|------|------|------|
| L-01 | `POST /contract/:id/sign` 无并发幂等 | Phase 3 合同签署时处理 |
| L-04 | 数据权限 DAO 未接入 | Phase 5 验证 |

---

## 七、Phase 2 预告

Phase 2 将实现系统管理模块（用户/角色/权限/部门/战区/字典/参数/日志），基于 Phase 1 的组件库直接开发。
