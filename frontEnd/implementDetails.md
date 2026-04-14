# 大富翁金融 CRM — 前端实施文档

**版本:** v1.0
**日期:** 2026-04-14
**状态:** 可开发
**技术栈:** 原生 JavaScript（ES6+）+ HTML5 + CSS3（无框架）

---

## 一、概述

本文档规划 NeoCC 系统前端开发框架，基于后端已实现的全部 REST API（见 `scripts/frontend/Interfaces.md`）和后端完成度评估（见 `scripts/plan-eng-review/Plan13.md`）。

**后端现状摘要：**
- auth / system / sales / finance 四个模块 API 已就绪，核心业务流程完整
- 统一响应格式：`{ code, message, data }`，分页格式：`{ total, records, page, size }`
- 认证方式：Bearer Token（值为用户 ID 字符串），无状态
- 贷款审核状态机完整，RabbitMQ 事件驱动合同签署→金融审核已打通
- 已知限制：提成发放（grant）和服务费确认收款（pay）框架存在但具体逻辑待完善，不阻断前端接入

**前端约束：**
- 原生 JavaScript，无 Vue/React/Angular 等框架依赖
- 不要求构建工具（可使用轻量级开发服务器如 `serve` 或 `http-server`）
- CSS 变量实现主题管理，支持后续 UI 框架渐进引入
- 模块化组织，ES6 模块（`import/export`）通过 `<script type="module">` 直接使用

---

## 二、页面清单

按模块分组，前端需实现以下页面。

### 2.1 认证模块（auth，端口 8085）

| 页面 | 路由 | 说明 |
|------|------|------|
| 登录页 | `#/login` | 用户名+密码，错误锁定提示 |
| 首页/仪表盘 | `#/dashboard` | 登录后落地页，展示关键数据统计 |

**说明：** auth 模块主要功能是登录，其他用户/角色/权限管理功能放在系统管理模块。

### 2.2 系统管理模块（system，端口 8082）

| 页面 | 路由 | 说明 |
|------|------|------|
| 部门管理 | `#/system/department` | 树形列表，支持新增/编辑/删除 |
| 战区管理 | `#/system/zone` | 列表，支持新增/编辑/删除 |
| 数据字典 | `#/system/dict` | 按类型查询字典项，支持新增/编辑/删除 |
| 系统参数 | `#/system/param` | Key-Value 配置，支持新增/编辑/删除 |
| 操作日志 | `#/system/log` | 只读分页列表，支持按用户/模块/时间过滤 |

### 2.3 销售管理模块（sales，端口 8083）

| 页面 | 路由 | 说明 |
|------|------|------|
| 客户列表 | `#/sales/customer` | 分页+状态过滤，含公海客户入口 |
| 客户详情 | `#/sales/customer/:id` | 客户信息+跟进记录+合同列表 |
| 客户新增/编辑 | `#/sales/customer/edit/:id?` | 表单（新建或编辑） |
| 公海客户 | `#/sales/customer/public-sea` | 公海客户列表，支持领取 |
| 跟进记录 | `#/sales/contact` | 按客户查列表，支持新增/编辑 |
| 合同列表 | `#/sales/contract` | 分页+状态过滤 |
| 合同详情 | `#/sales/contract/:id` | 合同信息+附件+状态操作 |
| 合同新增/编辑 | `#/sales/contract/edit/:id?` | 表单 |
| 合同签署 | `#/sales/contract/:id/sign` | 签署确认页（触发 MQ 事件） |
| 工作日志 | `#/sales/worklog` | 日报列表，支持新增/编辑 |
| 客户转移记录 | `#/sales/transfer` | 转让历史查询 |

### 2.4 金融审核模块（finance，端口 8084）

| 页面 | 路由 | 说明 |
|------|------|------|
| 银行管理 | `#/finance/bank` | 合作银行 CRUD |
| 金融产品 | `#/finance/product` | 产品列表，含 requirements/documents 展示 |
| 贷款审核列表 | `#/finance/loan-audit` | 分页+状态过滤，展示状态机进度 |
| 贷款审核详情 | `#/finance/loan-audit/:id` | 审核信息+操作轨迹+下一步操作按钮 |
| 审核操作（接收/初审/提交银行/银行反馈/终审/拒绝） | `#/finance/loan-audit/:id/:action` | 操作确认表单 |
| 提成记录 | `#/finance/commission` | 列表+确认+发放（发放逻辑待完善） |
| 服务费记录 | `#/finance/service-fee` | 列表+确认收款（收款逻辑待完善） |

### 2.5 用户/角色/权限管理（auth 模块扩展）

| 页面 | 路由 | 说明 |
|------|------|------|
| 用户管理 | `#/system/user` | 用户 CRUD+角色分配 |
| 角色管理 | `#/system/role` | 角色 CRUD+权限分配 |
| 权限管理 | `#/system/permission` | 权限树菜单（增删改） |

---

## 三、项目结构

```
frontEnd/
├── index.html              # 入口 HTML，<div id="app"></div>
├── assets/
│   ├── css/
│   │   ├── variables.css   # CSS 变量（颜色、间距、字体）
│   │   ├── base.css        # 重置样式、全局规则
│   │   ├── layout.css      # 布局（侧边栏、主内容区）
│   │   ├── component.css    # 通用组件（表格、表单、按钮、弹窗）
│   │   └── page/           # 页面级样式
│   │       ├── auth.css
│   │       ├── system.css
│   │       ├── sales.css
│   │       └── finance.css
│   └── images/             # 图标、图片资源
├── src/
│   ├── main.js             # 入口脚本，初始化路由+渲染侧边栏
│   ├── api/
│   │   ├── client.js       # 统一 HTTP 客户端（fetch 封装）
│   │   ├── auth.js         # 认证相关 API（登录、登出）
│   │   ├── system.js       # system 模块 API（部门、字典、参数、日志）
│   │   ├── sales.js         # sales 模块 API（客户、合同、跟进等）
│   │   └── finance.js       # finance 模块 API（贷款审核、提成、服务费）
│   ├── core/
│   │   ├── router.js       # 简单 Hash 路由（解析 #/path → 渲染对应组件）
│   │   ├── store.js         # 极简状态管理（存放 token、currentUser、菜单树）
│   │   ├── auth.js          # 认证中间件（检查 token，403 时跳转登录）
│   │   └── dict.js          # 字典缓存（从 system 加载并缓存常用字典）
│   ├── component/
│   │   ├── sidebar.js       # 侧边栏组件
│   │   ├── header.js        # 顶部栏（当前用户信息、退出）
│   │   ├── table.js         # 通用数据表格（支持分页、排序）
│   │   ├── form.js          # 通用表单组件
│   │   ├── modal.js         # 弹窗封装
│   │   ├── select.js        # 字典下拉选择框
│   │   ├── pagination.js    # 分页控件
│   │   └── toast.js          # 操作反馈提示
│   └── page/
│       ├── auth/
│       │   └── login.js     # 登录页
│       ├── dashboard.js     # 仪表盘
│       ├── system/
│       │   ├── user.js      # 用户管理
│       │   ├── role.js      # 角色管理
│       │   ├── permission.js # 权限管理
│       │   ├── department.js # 部门管理
│       │   ├── zone.js      # 战区管理
│       │   ├── dict.js      # 数据字典
│       │   ├── param.js     # 系统参数
│       │   └── log.js       # 操作日志
│       ├── sales/
│       │   ├── customer.js   # 客户列表+详情
│       │   ├── customerEdit.js # 客户新增/编辑
│       │   ├── contact.js    # 跟进记录
│       │   ├── contract.js   # 合同列表+详情
│       │   ├── contractEdit.js # 合同新增/编辑
│       │   ├── worklog.js    # 工作日志
│       │   └── transfer.js   # 客户转移记录
│       └── finance/
│           ├── bank.js       # 银行管理
│           ├── product.js    # 金融产品
│           ├── loanAudit.js   # 贷款审核列表
│           ├── loanAuditDetail.js # 审核详情+操作轨迹
│           ├── commission.js  # 提成记录
│           └── serviceFee.js  # 服务费记录
├── package.json             # 依赖（仅用于开发服务器，非构建工具）
└── serve.json               # serve 静态服务器配置
```

---

## 四、API 集成层

### 4.1 统一 HTTP 客户端（`src/api/client.js`）

```javascript
// 所有请求经过此 client，自动携带 Token，处理 403 重定向登录页
const API_BASE = 'http://localhost:8086/api'; // 网关入口，或直连各服务

export async function request(path, options = {}) {
  const token = localStorage.getItem('token');
  const headers = {
    'Content-Type': 'application/json',
    ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
    ...options.headers,
  };

  const res = await fetch(`${API_BASE}${path}`, { ...options, headers });

  // 403 → Token 无效或过期，跳转登录
  if (res.status === 403) {
    localStorage.removeItem('token');
    location.hash = '#/login';
    throw new Error('Unauthorized');
  }

  const json = await res.json();

  if (json.code !== 200) {
    throw new Error(json.message || 'Request failed');
  }

  return json.data;
}
```

### 4.2 认证

```javascript
// POST /api/sysUser/login
export async function login(username, password) {
  const data = await request('/sysUser/login', {
    method: 'POST',
    body: JSON.stringify({ username, password }),
  });
  // data.id 即为 token
  localStorage.setItem('token', data.id);
  localStorage.setItem('currentUser', JSON.stringify(data));
  return data;
}

export function logout() {
  localStorage.removeItem('token');
  localStorage.removeItem('currentUser');
  location.hash = '#/login';
}

export function getCurrentUser() {
  const raw = localStorage.getItem('currentUser');
  return raw ? JSON.parse(raw) : null;
}
```

### 4.3 分页查询约定

所有列表页分页参数通过 URL Query 传递：

```
GET /api/customer/page?page=1&size=10&sortField=createdAt&sortOrder=desc
```

后端返回：

```json
{
  "code": 200,
  "data": {
    "total": 100,
    "records": [...],
    "page": 1,
    "size": 10
  }
}
```

前端分页控件直接使用 `total` 和 `page` 渲染。

---

## 五、路由与渲染

### 5.1 Hash 路由（`src/core/router.js`）

```javascript
// 路由表：path → render 函数
const routes = {
  '/login': () => renderLogin(),
  '/dashboard': () => renderDashboard(),
  '/system/user': () => renderUserPage(),
  '/system/role': () => renderRolePage(),
  // ... 其他路由
  '/sales/customer': () => renderCustomerPage(),
  '/sales/customer/:id': (id) => renderCustomerDetail(id),
  '/finance/loan-audit': () => renderLoanAuditPage(),
  '/finance/loan-audit/:id': (id) => renderLoanAuditDetail(id),
};

export function navigate(path) {
  location.hash = '#' + path;
}

export function initRouter() {
  window.addEventListener('hashchange', handleRoute);

  // 初始路由
  handleRoute();
}

function handleRoute() {
  const hash = location.hash.slice(1) || '/login';
  // 匹配路由，支持 :id 参数
  const matched = matchRoute(hash, routes);
  if (matched) {
    const app = document.getElementById('app');
    app.innerHTML = '';
    matched.fn(...matched.params);
  } else {
    document.getElementById('app').innerHTML = '<p>404 Not Found</p>';
  }
}

function matchRoute(hash, routes) {
  // 简单实现：先精确匹配，再匹配带 :id 的模式
  if (routes[hash]) return { fn: routes[hash], params: [] };

  for (const pattern of Object.keys(routes)) {
    const paramNames = [];
    const regexStr = pattern.replace(/:([^/]+)/g, (_, name) => {
      paramNames.push(name);
      return '([^/]+)';
    });
    const regex = new RegExp(`^${regexStr}$`);
    const match = hash.match(regex);
    if (match) {
      return { fn: routes[pattern], params: match.slice(1) };
    }
  }
  return null;
}
```

---

## 六、状态管理

### 6.1 极简 Store（`src/core/store.js`）

```javascript
// 简单 pub/sub 状态容器
const store = {
  state: {
    token: localStorage.getItem('token') || null,
    currentUser: null,
    dicts: {},        // { dictType: [ {dictLabel, dictValue}, ... ] }
    permissions: [], // 当前用户权限码列表
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

### 6.2 字典加载（`src/core/dict.js`）

```javascript
import store from './store.js';
import { request } from '../api/client.js';

const COMMON_DICTS = [
  'customer_status', 'customer_type', 'intention_level',
  'contract_status', 'audit_status', 'commission_status',
  'service_fee_type', 'service_fee_payment_status',
  'performance_status', 'contact_type',
];

export async function loadDicts() {
  const results = await Promise.all(
    COMMON_DICTS.map(dictType =>
      request(`/sysDict/listByDictType?dictType=${dictType}`)
        .then(items => ({ [dictType]: items }))
        .catch(() => ({ [dictType]: [] }))
    )
  );
  results.forEach(r => {
    const [key, value] = Object.entries(r)[0];
    store.set('dicts', { ...store.get('dicts'), [key]: value });
  });
}

export function getDictLabel(dictType, dictValue) {
  const items = store.get('dicts')[dictType] || [];
  const item = items.find(i => String(i.dictValue) === String(dictValue));
  return item ? item.dictLabel : dictValue;
}
```

---

## 七、通用组件

### 7.1 Toast 提示（`src/component/toast.js`）

```javascript
export function toast(message, type = 'info') {
  // type: info / success / error / warning
  const el = document.createElement('div');
  el.className = `toast toast-${type}`;
  el.textContent = message;
  document.body.appendChild(el);
  setTimeout(() => el.remove(), 3000);
}
```

对应 CSS：

```css
/* assets/css/component.css */
.toast { position: fixed; top: 20px; right: 20px; padding: 12px 20px; border-radius: 4px; z-index: 9999; }
.toast-success { background: #4caf50; color: white; }
.toast-error { background: #f44336; color: white; }
.toast-info { background: #2196f3; color: white; }
.toast-warning { background: #ff9800; color: white; }
```

### 7.2 数据表格（`src/component/table.js`）

```javascript
// renderTable(columns, data, pagination, onPageChange)
// columns: [ { label, field, render?: (val, row) => string } ]
// pagination: { total, page, size }
// onPageChange: (page) => void
export function renderTable(container, columns, records, pagination, onPageChange) {
  // 渲染 <table> + <div.pagination>
  // 分页控件绑定 onPageChange
}
```

### 7.3 弹窗 Modal（`src/component/modal.js`）

```javascript
export function modal({ title, content, onConfirm, confirmText = '确定', onCancel }) {
  // 渲染遮罩 + 弹窗框
  // onConfirm 和 onCancel 自动关闭弹窗
}
```

### 7.4 字典下拉 Select（`src/component/select.js`）

```javascript
// 渲染 <select>，从 store.dicts 读取字典项
export function renderDictSelect(el, dictType, { value, placeholder, onChange }) {
  const items = store.get('dicts')[dictType] || [];
  // 渲染 <option>，默认选中 value，onChange 触发 onChange
}
```

---

## 八、页面实现要点

### 8.1 登录页（`/login`）

- 收集 username + password，调用 `POST /api/sysUser/login`
- 错误处理：展示后端返回的 message（包含锁定时间提示）
- 登录成功后跳转 `#/dashboard`，同时请求 `/api/sysUser/{id}/permCodes` 存入 store.permissions 用于前端权限控制

### 8.2 仪表盘（`/dashboard`）

- 调用多个接口聚合数据：客户总数（按状态分组）、合同总数（按状态分组）、本月业绩汇总
- 可使用 `Promise.all` 并行请求

### 8.3 客户列表（`/sales/customer`）

- 分页+过滤：status / salesRepId / intentionLevel
- 列表每行展示客户状态（用 `getDictLabel('customer_status', row.status)` 转换）
- 点击行跳转详情页 `#/sales/customer/:id`

### 8.4 合同签署（`/sales/contract/:id/sign`）

- 显示合同信息，确认后调用 `POST /api/contract/:id/sign`
- **前端防重复提交：** 按钮点击后立即 disable，loading 状态显示 "签署中..."
- 签署成功后展示成功提示，3秒后跳转合同详情

### 8.5 贷款审核详情（`/finance/loan-audit/:id`）

- 上半部分：审核信息（含 auditStatus 状态展示）
- 下半部分：操作轨迹（`GET /api/loanAuditRecord/listByLoanAuditId/:id`）
- 右侧/底部：根据当前 auditStatus 显示可操作按钮（receive/review/submit-bank/bank-result/approve/reject）
- 状态进度可视化：横向步骤条，1→2→3→4→5/6→7，每步注明当前状态

### 8.6 状态翻译

所有枚举值前端通过字典接口动态翻译，不硬编码中文：

```javascript
import { getDictLabel } from '../core/dict.js';

// 用法
getDictLabel('customer_status', row.status)   // '1' → '待跟进'
getDictLabel('contract_status', row.status)    // '2' → '已签署'
getDictLabel('audit_status', row.auditStatus)  // '6' → '终审通过'
```

---

## 九、权限控制

### 9.1 前端权限码

登录后获取用户权限码列表：

```
GET /api/sysUser/{id}/permCodes → ["customer:view", "customer:edit", "contract:sign", ...]
```

存入 `store.permissions`。

### 9.2 权限指令

```javascript
// 检查当前用户是否有某权限码
export function hasPermission(permCode) {
  return store.get('permissions').includes(permCode);
}

// 在渲染侧边栏或按钮时过滤
if (!hasPermission('customer:edit')) {
  // 隐藏编辑按钮
}
```

### 9.3 数据权限

后端 DataScope 拦截器（T13）目前基础设施就绪但 DAO 层未接入，所有用户查询全量数据。前端暂不处理，TODO 标注后端接入后验证。

---

## 十、CSS 架构

### 10.1 CSS 变量（`assets/css/variables.css`）

```css
:root {
  --color-primary: #1890ff;
  --color-success: #52c41a;
  --color-error: #ff4d4f;
  --color-warning: #faad14;
  --color-bg: #f5f5f5;
  --color-text: #333;
  --color-text-secondary: #999;
  --color-border: #e8e8e8;
  --sidebar-width: 220px;
  --header-height: 56px;
  --radius: 4px;
}
```

### 10.2 布局（`assets/css/layout.css`）

```css
body { margin: 0; font-family: -apple-system, BlinkMacSystemFont, sans-serif; }
#app { display: flex; min-height: 100vh; }
.sidebar { width: var(--sidebar-width); background: #001529; position: fixed; top: 0; left: 0; bottom: 0; overflow-y: auto; }
.main { margin-left: var(--sidebar-width); flex: 1; display: flex; flex-direction: column; }
.header { height: var(--header-height); background: white; border-bottom: 1px solid var(--color-border); display: flex; align-items: center; padding: 0 24px; }
.content { padding: 24px; background: var(--color-bg); flex: 1; }
```

---

## 十一、开发与构建

### 11.1 开发依赖

```json
// package.json
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

运行 `npm install && npm run dev` 即可在 `localhost:3000` 启动开发服务器。

### 11.2 生产部署

直接 `npm run build`（若后续引入打包工具）或 `serve . -s`（单页应用需配置 SPA fallback，所有路由返回 `index.html`）。

---

## 十二、实施优先级与阶段

### Phase 1 — 基础设施（第1周）

| 任务 | 说明 |
|------|------|
| P1.1 | 搭建 `frontEnd/` 目录结构，配置 `package.json` + `serve` |
| P1.2 | 实现 `src/api/client.js`（统一 HTTP 客户端） |
| P1.3 | 实现 `src/core/router.js`（Hash 路由） |
| P1.4 | 实现 `src/core/store.js` + `src/core/dict.js` |
| P1.5 | 实现 `src/component/` 通用组件（sidebar/header/table/modal/select/toast） |
| P1.6 | 实现登录页 `/#/login`，对接 `POST /api/sysUser/login` |
| P1.7 | 实现仪表盘 `/#/dashboard` |

**验收标准：** 能登录、能跳转侧边栏菜单、页面间导航正常。

### Phase 2 — 系统管理（第2周）

| 任务 | 说明 |
|------|------|
| P2.1 | 用户管理页面（含角色分配） |
| P2.2 | 角色管理页面（含权限分配） |
| P2.3 | 权限管理页面（树形菜单） |
| P2.4 | 部门管理（树形列表） |
| P2.5 | 战区管理 |
| P2.6 | 数据字典管理 |
| P2.7 | 系统参数管理 |
| P2.8 | 操作日志查询（只读） |

### Phase 3 — 销售管理（第3周）

| 任务 | 说明 |
|------|------|
| P3.1 | 客户列表+分页+过滤+新增/编辑 |
| P3.2 | 客户详情页（跟进记录+合同列表） |
| P3.3 | 公海客户+领取功能 |
| P3.4 | 跟进记录 CRUD |
| P3.5 | 合同列表+分页+新增/编辑 |
| P3.6 | 合同签署（含防重复提交） |
| P3.7 | 工作日志 |
| P3.8 | 客户转移记录查询 |

### Phase 4 — 金融审核（第4周）

| 任务 | 说明 |
|------|------|
| P4.1 | 银行管理 |
| P4.2 | 金融产品（含 requirements/documents JSON 展示） |
| P4.3 | 贷款审核列表（状态进度条） |
| P4.4 | 贷款审核详情（操作轨迹） |
| P4.5 | 审核操作表单（receive/review/submit-bank/bank-result/approve/reject） |
| P4.6 | 提成记录（确认+发放，发放逻辑待后端完善） |
| P4.7 | 服务费记录（确认收款，收款逻辑待后端完善） |

### Phase 5 — 完善（视情况）

| 任务 | 说明 |
|------|------|
| P5.1 | 权限控制（按钮/菜单显隐） |
| P5.2 | 数据权限验证（后端 T13 接入后） |
| P5.3 | 错误处理增强（网络异常重试、空状态页） |
| P5.4 | 响应式适配（移动端） |

---

## 十三、后端已知限制对前端的影响

| 限制编号 | 说明 | 前端处理方式 |
|---------|------|------------|
| L-01 | `POST /api/contract/:id/sign` 无并发幂等保护 | 签署按钮点击后 disable，loading 状态防止重复提交 |
| L-02 | `POST /api/commissionRecord/:id/grant` 具体转账逻辑未完成 | 发放按钮可点，但展示"功能待完善"提示 |
| L-03 | `PUT /api/serviceFeeRecord/:id/pay` 支付核验流程未完成 | 确认收款按钮可点，但展示"功能待完善"提示 |
| L-04 | 数据权限过滤 DAO 层未接入 | 列表展示全量数据，TODO 标注后端接入后验证 |
| L-05 | `/api/sysUser/dev/reset-password` 调试接口暴露 | 生产环境勿调用，本地开发调试用 |
| L-06 | Redis 缓存无 TTL | 字典/参数依赖后端 evict，前端直接用无影响 |

---

## 十四、接口数据样例

### 14.1 登录

```
POST /api/sysUser/login
Body: { "username": "admin", "password": "123456" }
Response: {
  "code": 200,
  "data": {
    "id": 1001,
    "username": "admin",
    "realName": "管理员",
    "deptId": 10,
    "zoneId": 1,
    "status": 1
  }
}
```

登录响应的 `data.id` 即为 token，存储至 `localStorage.token`，后续请求携带 `Authorization: Bearer {token}`。

### 14.2 客户列表

```
GET /api/customer/page?page=1&size=10
Response: {
  "code": 200,
  "data": {
    "total": 50,
    "records": [
      { "id": 1, "name": "张三", "phone": "13800138000", "status": 2, "intentionLevel": 1, ... }
    ],
    "page": 1,
    "size": 10
  }
}
```

### 14.3 贷款审核详情

```
GET /api/loanAudit/{id}
Response: {
  "code": 200,
  "data": {
    "id": 1,
    "contractId": 100,
    "auditStatus": 2,
    "financeSpecialistId": 2001,
    "approvedAmount": 500000,
    "bankId": 1,
    ...
  }
}
```

---

**文档版本：** v1.0
**编写日期：** 2026-04-14
**依据文档：** `scripts/frontend/Interfaces.md`（后端接口）、`scripts/plan-eng-review/Plan13.md`（后端完成度评估）
