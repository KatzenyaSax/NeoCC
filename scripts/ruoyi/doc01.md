# ruoyi-ui 接入评估报告

**项目**：NeoCC × 若依（ruoyi-ui v3/Vue3）
**评估日期**：2026-04-15
**结论**：框架可用，但需改造接入层，核心工作在后端

---

## 一、技术栈对照

| 维度 | ruoyi-ui | NeoCC 当前前端 |
|------|---------|--------------|
| 框架 | Vue 3.5 + Vite 6 | 原生 JS（ES6 Modules）+ Hash Router |
| UI 库 | Element Plus 2.13 | 自定义 CSS（少量） |
| 状态管理 | Pinia 3 | 极简 pub/sub store |
| HTTP 客户端 | Axios | 原生 fetch |
| 构建工具 | Vite 6 | 无（直接 serve） |
| 项目规模 | ~50 个页面组件 | ~15 个页面（Phase 1-3） |
| 路由模式 | Web History (`/`路径) | Hash History (`/#/path`) |
| Token 存储 | Cookies（`Admin-Token`） | localStorage（`token`） |
| 权限模型 | 动态路由 + 按钮级 | 菜单级（侧边栏过滤） |

**结论**：ruoyi-ui 架构更完整、扩展性更强，是成熟的企业级后台方案。接入不会破坏现有 NeoCC 前端（两个独立项目），但两套前端不能并存于同一端口。

---

## 二、API 适配分析

ruoyi-ui 依赖 3 个固定的后端认证接口，**NeoCC 后端目前均不满足**：

### 2.1 `POST /login`

| 项目 | ruoyi-ui 期望 | NeoCC 后端现状 |
|------|-------------|--------------|
| 路径 | `/login` | `/api/sysUser/login` ✓（路径可改） |
| 请求体 | `{ username, password }` | 完全一致 ✓ |
| 响应体 | `{ code: 200, data: { token: "..." } }` | ❌ `{ code: 200, data: SysUserEntity }`，其中 `data.id` 充当 token |

**差异**：RuoYi 登录只返回 token 字符串；NeoCC 返回完整用户对象，`id` 字段即 token。

**改造方案**：新增一个 `/api/auth/login` 返回 `{ token, expires }` 格式，或改写 `SysUserController.login` 返回值。

---

### 2.2 `GET /getInfo`

| 项目 | ruoyi-ui 期望 | NeoCC 后端现状 |
|------|-------------|--------------|
| 路径 | `/getInfo` | ❌ 不存在 |
| 响应体 | `{ code: 200, data: { user: {...}, roles: [...], permissions: [...] } }` | — |

**作用**：获取当前用户信息 + 角色列表 + 权限码列表，驱动动态路由生成和按钮级权限控制。

**改造方案**：在 auth 模块新增 `GET /api/auth/userinfo` 接口，聚合 `SysUserService.getById` + `getRoleIdsByUserId` + `getPermCodesByUserId`。

---

### 2.3 `GET /getRouters`

| 项目 | ruoyi-ui 期望 | NeoCC 后端现状 |
|------|-------------|--------------|
| 路径 | `/getRouters` | ❌ 不存在 |
| 响应体 | `{ code: 200, data: [{ path, component, meta: { title, icon }}] }` | — |

**作用**：返回完整菜单树，前端 Vue Router 据此动态注册路由，侧边栏据此渲染。

**改造方案**：在 auth 模块新增 `GET /api/auth/routers`，查询 `sys_permission` 表（`perm_type=1` 且 `status=1`），返回 Vue Router 格式的菜单树。

---

## 三、前端适配分析

### 3.1 Token 机制

| 项目 | ruoyi-ui | NeoCC |
|------|---------|-------|
| Header 名 | `Authorization: Bearer <token>` | `Authorization: Bearer <token>` |
| 存储位置 | Cookies (`Admin-Token`) | localStorage (`token`) |
| 刷新机制 | 无自动刷新，有 401 重定向登录 | 无自动刷新，401 跳转登录页 |

**兼容性**：Header 名称一致，Cookie → localStorage 仅是存储层差异，修改 `@/utils/auth.js` 的 `setToken/getToken/removeToken` 即可。

---

### 3.2 Vite 代理配置

ruoyi-ui 的 `vite.config.js` 已配置：

```js
proxy: {
  '/dev-api': {
    target: 'http://localhost:8086',  // NeoCC Gateway ✓
    changeOrigin: true,
  }
}
```

路径 `/dev-api/*` → Gateway → 各微服务，与 NeoCC 架构完全匹配。**无需改动**。

---

### 3.3 现有 API 模块

ruoyi-ui 的 `src/api/` 目录下已有对齐 NeoCC 后端微服务的模块：

| API 模块 | 路径 | 对应 NeoCC 后端 |
|---------|------|---------------|
| `system/user.js` | `/system/user/*` | auth 服务 `/api/sysUser/*` |
| `system/role.js` | `/system/role/*` | auth 服务 `/api/sysRole/*` |
| `system/permission.js` | `/system/permission/*` | auth 服务 `/api/sysPermission/*` |
| `finance/bank.js` | `/bank/*` | finance 服务 `/api/bank/*` |
| `finance/loanAudit.js` | `/loanAudit/*` | finance 服务 `/api/loanAudit/*` |
| `finance/commission.js` | `/commission/*` | finance 服务 `/api/commission/*` |
| `finance/serviceFeeRecord.js` | `/serviceFee/*` | finance 服务 `/api/serviceFee/*` |
| `sales/customer.js` | `/customer/*` | sales 服务 `/api/customer/*` |
| `sales/contract.js` | `/contract/*` | sales 服务 `/api/contract/*` |
| `sales/workLog.js` | `/workLog/*` | sales 服务 `/api/workLog/*` |

**结论**：业务 API 层基本已对齐，改造工作量集中在认证层（login/getInfo/getRouters）。

---

## 四、后端需新增的接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/auth/login` | POST | 登录，返回 `{ token, userId, expires }` |
| `/api/auth/userinfo` | GET | 返回 `{ user, roles[], permissions[] }` |
| `/api/auth/routers` | GET | 返回 Vue Router 格式菜单树 |
| `/api/auth/logout` | POST | 退出登录（NeoCC 已有 `POST /api/sysUser/logout`，但需对齐格式）|

---

## 五、工作量估算

| 改造项 | 工作量 | 难度 |
|--------|-------|------|
| 后端新增 3 个认证接口 | 中（新建 `AuthController`） | 低 |
| 前端 `utils/auth.js` 适配 localStorage | 0.5h | 低 |
| 前端 `utils/request.js` 适配 NeoCC `Result` 结构 | 1h | 低 |
| 前端删除冗余页面（保留 finance/sales/system 部分）| 2-3h | 低 |
| 动态路由注册逻辑改造（`getRouters` 接入）| 4-6h | 中 |
| 按钮级权限控制（`v-if="hasPerm(permCode)"`）| 8-16h | 中 |
| Element Plus 主题样式适配 | 4-8h | 中 |
| **合计** | **约 2-3 人天** | — |

---

## 六、可接入性结论

```
结论：可以接入，但需要后端先补充 3 个认证接口，
     前端适配工作约 2-3 人天。
```

**前置条件**（必须完成）：
1. 后端 auth 模块新增 `/api/auth/login`、`/api/auth/userinfo`、`/api/auth/routers` 三个接口
2. 后端 `sys_permission` 表已录入完整的菜单权限数据（dataRole.sql 已完成 ✓）

**推荐行动**：
- 若 NeoCC 后端计划长期使用 RuoYi 前端体系 → 按上述接口改造后端，前端直接使用 ruoyi-ui
- 若 NeoCC 后端不计划改动 → 当前原生 JS 前端（`frontEnd/`）继续沿用，无需接入 ruoyi-ui
- ruoyi-ui 和当前 `frontEnd/` 可**同时存在**（两个独立服务，端口不同），互不影响

---

## 七、附：ruoyi-ui 核心文件清单

| 文件 | 作用 | 是否需修改 |
|------|------|---------|
| `src/utils/request.js` | Axios 封装 | 需适配 NeoCC `Result{code,data,message}` 结构 |
| `src/utils/auth.js` | Token 读写 | 需将 Cookies 改为 localStorage |
| `src/store/modules/user.js` | 用户状态 | 需适配 NeoCC userinfo 格式 |
| `src/permission.js` | 路由守卫 | 需适配 NeoCC 路由模式（可选） |
| `.env.development` | 环境变量 | 无需修改（代理已配置） |
| `src/api/login.js` | 认证 API | 需新增 localStorage token 写入逻辑 |
| `src/api/menu.js` | 动态菜单 API | 需重写 `/getRouters` 对接后端 |
| `vite.config.js` | Vite 配置 | 无需修改 ✓ |
