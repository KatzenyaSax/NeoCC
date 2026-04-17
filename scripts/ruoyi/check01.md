# NeoCC × ruoyi-ui 前后端联调报告

**日期**：2026-04-16
**测试环境**：Windows 11，backend 4 服务已启动（gateway:8086, auth:8085, sales:8083, finance:8084）

---

## 一、服务状态概览

| 服务 | 端口 | 状态 | 说明 |
|------|------|------|------|
| Gateway | 8086 | ✅ 正常 | 路由正常 |
| Auth Service | 8085 | ✅ 正常 | 新 AuthController 已注册 |
| Sales Service | 8083 | ✅ 正常 | 业务 API 正常 |
| Finance Service | 8084 | ✅ 正常 | 业务 API 正常 |

---

## 二、Phase 1 后端接口测试结果

### 2.1 `/api/auth/login` — ❌ 失败

**直接调用 auth:8085**：
```
POST http://localhost:8085/api/auth/login
HTTP 403 Forbidden
```
**原因**：`SecurityConfig` 的 `permitAll()` 只含 `/api/sysUser/**` 路径，`/api/auth/**` 未加入，导致 Spring Security 拦截。

**修复**：在 `SecurityConfig.java` 的 `permitAll()` 中添加：
```java
.requestMatchers("/api/auth/**").permitAll()
```

---

### 2.2 `/api/auth/getInfo` — ✅ 正常

**直接调用 auth:8085（带 token）**：
```
GET http://localhost:8085/api/auth/getInfo
Authorization: Bearer 11
HTTP 200
{
  "code": 200,
  "data": {
    "userId": 11, "userName": "admin",
    "nickName": "超级管理员",
    "roles": ["ROLE_SUPER_ADMIN"],
    "permissions": ["DASHBOARD","SYSTEM","SALES","FINANCE","PERFORMANCE", ...全部58个...]
  }
}
```
**说明**：AuthController 已注册并正常工作。admin 拥有所有权限（符合 SUPER_ADMIN 角色定义）。

---

### 2.3 `/api/auth/getRouters` — ✅ 正常

**直接调用 auth:8085（带 token=11，admin）**：
```
GET http://localhost:8085/api/auth/getRouters
Authorization: Bearer 11
HTTP 200
[
  { "name": "DASHBOARD", "children": [] },
  { "name": "SYSTEM", "children": 8 },
  { "name": "SALES", "children": 13 },
  { "name": "FINANCE", "children": 5 },
  { "name": "PERFORMANCE", "children": 2 }
]
```

**lisi（sales rep，token=16）路由测试**：
```
GET http://localhost:8085/api/auth/getRouters
Authorization: Bearer 16
HTTP 200
[
  { "name": "DASHBOARD", "children": [] },
  { "name": "SYSTEM", "children": 1 },   ← 有1个系统菜单（可能是只读权限）
  { "name": "SALES", "children": 13 }     ← 13个销售菜单，符合预期
]
```
**权限过滤正常** ✅，但 lisi 有 SYSTEM 菜单（可能是 `dataRole.sql` 中 SALES_REP 被分配了 SYSTEM 只读权限，属于数据层面而非代码层面问题）。

---

### 2.4 现有 `/api/sysUser/login` — ❌ 失败

```
POST http://localhost:8085/api/sysUser/login
{ "username": "admin", "password": "123456" }
HTTP 200 { "code": 500, "message": "系统异常: null" }
```
**现象**：endpoint 可达，但内部抛出 NullPointerException（message=null）。

**可能原因**：
1. BCrypt 密码哈希不匹配（"123456" 与数据库 hash 不一致）
2. `SysUserServiceImpl.login()` 内部某字段为空导致 NPE

**建议**：通过 `SysUserController.dev/reset-password` 重置 admin 密码，或检查 `SysUserServiceImpl.login()` 的异常堆栈。

---

## 三、Gateway 路由问题

### 3.1 现状：`StripPrefix=2`

**文件** `gateway/src/main/resources/application.yml`（已由 doc03 修改）：
```yaml
- id: auth-route
  uri: http://localhost:8085
  predicates:
    - Path=/auth/**
  filters:
    - StripPrefix=2   # 已修改，但服务尚未重启生效
```

### 3.2 Gateway 路由分析

| 前端路径 | StripPrefix | 到达 auth:8085 路径 | AuthController 匹配 | 结果 |
|---------|------------|-------------------|-------------------|------|
| `/auth/api/login` | 2 | `/login` | `/api/auth/login` | ❌ 404/401 |
| `/auth/api/login` | 1（重启前） | `/api/login` | `/api/auth/login` | ❌ 404 |

**结论**：当前配置无论 StripPrefix=1 还是 2，`/auth/api/login` 都无法到达 AuthController。

### 3.3 根因分析

doc03 设计逻辑（错误）：
- 前端：`/auth/api/login`（login.js 中已改好）
- Gateway StripPrefix=2：去掉 `/auth/api` → 剩余 `/login`
- AuthController：`@RequestMapping("/api/auth")` → 匹配 `/api/auth/login`
- **问题**：`/login` ≠ `/api/auth/login`，路径永远对不上

**正确架构（需要修复）**：
- 方案 A（推荐）：`StripPrefix=1` + AuthController 改为 `@RequestMapping("/auth")`
- 方案 B：`StripPrefix=1` + 前端改为 `/auth/login`（但 ruin-ui 固定用 `/login`）

---

## 四、Gateway 业务路由 — ✅ 正常

业务服务（sales/finance）路由经 gateway 验证**正常**：

| 路径 | 结果 | 数据 |
|------|------|------|
| `GET /sales/api/customer/page` | ✅ 200 | 返回 8 条客户数据 |
| `GET /finance/api/bank/page` | ✅ 200 | 返回 5 条银行数据 |

---

## 五、前端问题

### 5.1 Vite 启动 ✅
- `npm run dev` 成功，监听端口 81（80 被占用）
- `vite-plugin-svg-icons` 已升级到 v2.0.0 解决 ESM 兼容问题
- proxy 配置正确：`/dev-api/*` → `http://localhost:8086`

### 5.2 前端无法登录（Gateway 路由不通）

**流程**：
1. 前端 `login.js` → `/auth/api/login`（Vite proxy）→ Gateway `/auth/api/login`
2. Gateway StripPrefix=2 → `/login` → auth:8085
3. `SecurityConfig` 拦截 → **401/403**

**必须修复的问题**（见第六节）。

---

## 六、修改建议

### 问题 1（Critical）：`SecurityConfig` 未 permit AuthController 路径

**文件**：`auth/src/main/java/com/dafuweng/auth/config/SecurityConfig.java`

```java
// 第 38-46 行，当前：
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/sysUser/login", "/api/sysUser/page").permitAll()
    .requestMatchers("/api/sysUser/dev/**").permitAll()
    .requestMatchers("/api/sysUser/{id}").permitAll()
    .requestMatchers("/api/sysUser/{id}/roles").permitAll()
    .requestMatchers("/api/sysUser/{id}/permCodes").permitAll()
    .requestMatchers("/static/**", "/favicon.ico").permitAll()
    .anyRequest().authenticated()
)
```

**修改为**：
```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/sysUser/login", "/api/sysUser/page").permitAll()
    .requestMatchers("/api/sysUser/dev/**").permitAll()
    .requestMatchers("/api/sysUser/{id}").permitAll()
    .requestMatchers("/api/sysUser/{id}/roles").permitAll()
    .requestMatchers("/api/sysUser/{id}/permCodes").permitAll()
    .requestMatchers("/api/auth/**").permitAll()    // ← 新增
    .requestMatchers("/static/**", "/favicon.ico").permitAll()
    .anyRequest().authenticated()
)
```

---

### 问题 2（Critical）：Gateway StripPrefix 与 AuthController 路径不匹配

doc03 中的 `StripPrefix=2` 逻辑有误。当前 gateway config 虽已改，但服务未重启。

**推荐方案**（最简，无需改 AuthController 路径）：

**文件**：`gateway/src/main/resources/application.yml`

```yaml
- id: auth-route
  uri: http://localhost:8085
  predicates:
    - Path=/auth/**
  filters:
    - StripPrefix=1    # 恢复为 1
```

AuthController 保持 `@RequestMapping("/api/auth")`：
- 前端 `/auth/api/login` → StripPrefix=1 → `/api/login` 到达 auth:8085

**同时需要新增路由**（处理 AuthController 的 `/api/auth` 前缀）：

由于 auth:8085 内部没有 `/api/auth` 前缀路由，需在 gateway 增加 `PrefixPath` 或改 AuthController 路径。

**最简单的正确方案**：将 AuthController 的 `@RequestMapping("/api/auth")` 改为 `/auth`，然后 StripPrefix=1：
- 前端 `/auth/api/login` → StripPrefix=1 → `/api/login` → 不到 AuthController

实际上，当前 gateway 路由 `/auth/** → auth:8085`，去掉 `/auth` 后，auth:8085 收到 `/api/login`...
auth:8085 上没有 `/api/login` 这个路径（只有 `/api/sysUser/login` 或 `/api/auth/login`）。

**最干净的做法**：StripPrefix 保持 1，不改 AuthController，新增一条精确路由：

```yaml
- id: auth-route
  uri: http://localhost:8085
  predicates:
    - Path=/auth/**
  filters:
    - StripPrefix=1
- id: auth-api-route
  uri: http://localhost:8085/auth
  predicates:
    - Path=/auth/api/**
  # 不加 StripPrefix，直接透传 /auth/api/* → auth:8085/auth/api/*
```

或更简单：**把 AuthController 的 `@RequestMapping("/api/auth")` 改为 `/auth`**，同时 `StripPrefix=1`：
- 前端 `/auth/api/login` → StripPrefix=1 → `/api/login` 仍然不对...

**最终正确方案**：
1. StripPrefix=1
2. AuthController `@RequestMapping("/auth")`
3. AuthController 内路径改为 `/login`, `/getInfo`, `/getRouters`, `/logout`
4. 前端 login.js 路径改为 `/auth/login`

---

### 问题 3（High）：现有 `/api/sysUser/login` 返回 500

admin 和 lisi 登录均返回 500 NullPointerException。需要：
1. 检查 `SysUserServiceImpl.login()` 的异常信息（需在 auth 服务日志中查看完整堆栈）
2. 或使用 `POST /api/sysUser/dev/reset-password` 重置密码后重试

---

## 七、测试命令（重启服务后使用）

```bash
# T1: admin 登录（修复后）
curl -X POST http://localhost:8086/auth/api/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'

# T2: 获取用户信息
curl -X GET http://localhost:8086/auth/api/getInfo \
  -H "Authorization: Bearer 11"

# T3: 获取路由
curl -X GET http://localhost:8086/auth/api/getRouters \
  -H "Authorization: Bearer 11"

# T4: 销售员 lisi 路由
curl -X POST http://localhost:8085/api/sysUser/login \
  -H "Content-Type: application/json" \
  -d '{"username":"lisi","password":"123456"}'  # 获取 token
# 用返回的 userId 作为 token 调用 getRouters

# T5: 业务接口测试
curl -X GET "http://localhost:8086/sales/api/customer/page?page=1&size=10" \
  -H "Authorization: Bearer 11"
```

---

## 八、结论

| 模块 | 状态 | 说明 |
|------|------|------|
| AuthController 代码 | ✅ | 代码正确，已注册 |
| getInfo / getRouters | ✅ | 直接调用正常 |
| Gateway 业务路由 | ✅ | sales/finance 正常 |
| AuthController 路径 | ❌ | 与 gateway 路由不匹配 |
| SecurityConfig | ❌ | 缺少 `/api/auth/**` permitAll |
| 现有 sysUser/login | ❌ | NPE，密码或服务问题 |
| 前端 Vite | ✅ | 启动正常 |
| 前端登录流程 | ❌ | Gateway 路由不通 |

**核心阻塞问题**：
1. `SecurityConfig` 添加 `.requestMatchers("/api/auth/**").permitAll()`
2. Gateway `AuthController` 路径与 gateway 路由对齐（建议：改 AuthController 为 `/auth`，gateway StripPrefix=1）
3. 解决 admin/lisi 的 `/api/sysUser/login` 500 问题（查看 auth 服务日志）
