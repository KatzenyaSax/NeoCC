# NeoCC × ruoyi-ui 联调问题修正文档

**项目**：NeoCC 后端 + 若依（ruoyi-ui v3/Vue3）
**版本**：v1.0
**日期**：2026-04-16
**性质**：针对 check01.md 问题的修正执行方案

---

## 一、问题根因分析

check01.md 确认了三个阻塞问题，以下是经过仔细推敲的**精确根因分析**。

### 问题链路图

```
前端 login.js → /auth/api/login（不变）
        ↓
Gateway: Path=/auth/** → StripPrefix=2 → 去掉 /auth/api → /login
        ↓
auth:8085 收到: /login
        ↓
AuthController @RequestMapping("/api/auth") → /api/auth/login
        ↓
/login ≠ /api/auth/login → 404
```

**doc03 中的 `StripPrefix=2` 设计在逻辑上不成立**——无论 StripPrefix=1 还是 2，`/auth/api/login` 都无法到达 AuthController 的 `/api/auth/login`。

### Spring Cloud Gateway 路由机制（关键）

```
请求: /auth/api/login
1. Route Predicate 匹配: 匹配 Path=/auth/**
2. StripPrefix=2 执行: 路径变为 /login
3. Forwarded to URI: http://localhost:8085/login
```

路由匹配发生在 StripPrefix 之前，但请求转发时只发送 StripPrefix 后的剩余路径。

---

## 二、正确架构

### 目标路径映射

| 前端路径 | Gateway 处理 | auth:8085 收到 | AuthController 路径 |
|---------|------------|--------------|-------------------|
| `/auth/api/login` | StripPrefix=1 | `/api/login` | → 需要 `@RequestMapping("/api")` + `@PostMapping("/auth/login")` |
| `/auth/api/getInfo` | StripPrefix=1 | `/api/getInfo` | → 需要 `@RequestMapping("/api")` + `@GetMapping("/auth/getInfo")` |
| `/auth/api/getRouters` | StripPrefix=1 | `/api/getRouters` | → 需要 `@RequestMapping("/api")` + `@GetMapping("/auth/getRouters")` |
| `/auth/api/logout` | StripPrefix=1 | `/api/logout` | → 需要 `@RequestMapping("/api")` + `@PostMapping("/auth/logout")` |

### 正确方案

**使用 context-path = /auth**（auth 服务配置）：
- auth:8085 的 context-path = `/auth`
- AuthController `@RequestMapping("/api/auth")`
- 当请求到达 `http://localhost:8085/auth/api/login` 时，context-path=`/auth` 剥离后，servlet 收到 `/api/login`，匹配到 `@RequestMapping("/api")` + `@PostMapping("/auth")` → `/api/auth`

同时**前端 login.js / menu.js 路径改为 `/auth/*`**，与 gateway 路由 `StripPrefix=1` 配合：
- `/auth/login` → StripPrefix=1 → `/login` → auth:8085 `context-path=/auth` → servlet 收到 `/login` → AuthController `@RequestMapping("/api")` + `@PostMapping("/auth/login")` → `/api/auth/login` ✓

**但这要求 AuthController 拆分为两段**：`@RequestMapping("/api")` 在类上，`@PostMapping("/auth/login")` 在方法上（因为 context-path 剥离后剩余 `/login`，而方法上需要是 `/auth/login`）。

**更简单的方案（推荐，一步到位）**：
- 改 AuthController 为 `@RequestMapping("/auth")`（类级）
- 方法上直接 `@PostMapping("/login")` 等（无子路径）
- Gateway `StripPrefix=1`：`/auth/login` → `/login` → auth:8085 无 context-path → servlet 收到 `/login` → 匹配 AuthController `/auth` + `/login` ❌ 不匹配

**最干净的方案**：
- AuthService 的 `application.yml` 加 `server.servlet.context-path=/auth`
- AuthController 改为 `@RequestMapping("/api")`，方法上用 `@PostMapping("/auth/login")`
- 前端 login.js 改为 `/auth/login`（去掉 `/api` 前缀）
- Gateway `StripPrefix=1`：`/auth/login` → `/login` → auth:8085/context-path=`/auth` → servlet `/login` → 匹配失败

**最终确认可行方案**：
- 方案 A（Backend 改 `@RequestMapping("/auth")`，Gateway StripPrefix=1，前端 `/auth/login`）
- 方案 B（加 auth service context-path=/auth，AuthController 拆 `/api` + `/auth/*`，前端改 `/auth/login`）
- 方案 C（**不用 context-path，改 AuthController 为 `@RequestMapping("/auth")`，前端改 login 为 `/auth/login`，menu 改 `/auth/getRouters`，Gateway 新增一条专用 auth-api 路由用 StripPrefix=1+PrefixPath=/api）

**最终正确方案（方案 C）**：
1. AuthController 改为 `@RequestMapping("/auth")`，方法上用 `/login`、`/getInfo`、`/getRouters`、`/logout`
2. 前端 login.js `/auth/login`（非 `/auth/api/login`），menu.js `/auth/getRouters`
3. Gateway `/auth/**` 用 `StripPrefix=1`：`/auth/login` → `/login` → auth:8085 `/login` → AuthController `/auth` + `/login` = `/auth/login` ✓

---

## 三、修正步骤（按执行顺序）

### 修正 1：AuthController — 改路径

**文件**：`auth/src/main/java/com/dafuweng/auth/controller/AuthController.java`

```java
// 改动前
@RequestMapping("/api/auth")

// 改动后
@RequestMapping("/auth")
```

同时将每个方法的子路径改为不含 `/api/auth` 前缀：

| 改动前 | 改动后 |
|--------|--------|
| `@PostMapping("/login")` | `@PostMapping("/login")`（不变）|
| `@GetMapping("/getInfo")` | `@GetMapping("/getInfo")`（不变）|
| `@GetMapping("/getRouters")` | `@GetMapping("/getRouters")`（不变）|
| `@PostMapping("/logout")` | `@PostMapping("/logout")`（不变）|

> **注意**：方法上的 `@PostMapping("/login")` 配合类级 `@RequestMapping("/auth")` 意味着完整路径是 `/auth/login`。这正是 Gateway `StripPrefix=1` 去掉 `/auth` 后剩余的路径，**可以正确匹配**。

**AuthController 完整修改后**：

```java
@RestController
@RequestMapping("/auth")   // 原 "/api/auth"
public class AuthController {

    @PostMapping("/login")   // 完整路径: /auth/login
    public Result<Map<String, Object>> login(...) { ... }

    @GetMapping("/getInfo") // 完整路径: /auth/getInfo
    public Result<Map<String, Object>> getInfo(...) { ... }

    @GetMapping("/getRouters") // 完整路径: /auth/getRouters
    public Result<List<Map<String, Object>>> getRouters(...) { ... }

    @PostMapping("/logout") // 完整路径: /auth/logout
    public Result<Void> logout(...) { ... }
}
```

---

### 修正 2：SecurityConfig — permit `/auth/**`

**文件**：`auth/src/main/java/com/dafuweng/auth/config/SecurityConfig.java`

```java
// 找到：
.requestMatchers("/api/sysUser/login", "/api/sysUser/page").permitAll()
.requestMatchers("/api/sysUser/dev/**").permitAll()
.requestMatchers("/api/sysUser/{id}").permitAll()
.requestMatchers("/api/sysUser/{id}/roles").permitAll()
.requestMatchers("/api/sysUser/{id}/permCodes").permitAll()
.requestMatchers("/static/**", "/favicon.ico").permitAll()

// 改为（新增 .requestMatchers("/auth/**").permitAll()）：
.requestMatchers("/api/sysUser/login", "/api/sysUser/page").permitAll()
.requestMatchers("/api/sysUser/dev/**").permitAll()
.requestMatchers("/api/sysUser/{id}").permitAll()
.requestMatchers("/api/sysUser/{id}/roles").permitAll()
.requestMatchers("/api/sysUser/{id}/permCodes").permitAll()
.requestMatchers("/auth/**").permitAll()   // ← 新增（改动后 AuthController 路径）
.requestMatchers("/static/**", "/favicon.ico").permitAll()
```

---

### 修正 3：Gateway — 恢复 StripPrefix=1

**文件**：`gateway/src/main/resources/application.yml`

```yaml
# 改动前（StripPrefix=2，错误）：
- id: auth-route
  uri: http://localhost:8085
  predicates:
    - Path=/auth/**
  filters:
    - StripPrefix=2

# 改动后（StripPrefix=1，正确）：
- id: auth-route
  uri: http://localhost:8085
  predicates:
    - Path=/auth/**
  filters:
    - StripPrefix=1
```

> `StripPrefix=1` 去掉 `/auth`，`/auth/login` → 到达 auth:8085 `/login`，AuthController 完整路径 `/auth/login` 匹配 ✓

---

### 修正 4：前端 login.js — 改路径

**文件**：`ruoyi-ui/src/api/login.js`

```javascript
// 改动前：
url: '/auth/api/login',   // 不匹配任何后端路径
url: '/auth/api/getInfo',
url: '/auth/api/logout',

// 改动后：
url: '/auth/login',
url: '/auth/getInfo',
url: '/auth/logout',
```

完整文件修改后：

```javascript
import request from '@/utils/request'

export function login(username, password) {
  return request({
    url: '/auth/login',   // StripPrefix=1 → /login → auth:8085 /auth/login ✓
    headers: { isToken: false, repeatSubmit: false },
    method: 'post',
    data: { username, password }
  })
}

export function getInfo() {
  return request({
    url: '/auth/getInfo', // StripPrefix=1 → /getInfo → auth:8085 /auth/getInfo ✓
    method: 'get'
  })
}

export function logout() {
  return request({
    url: '/auth/logout',  // StripPrefix=1 → /logout → auth:8085 /auth/logout ✓
    method: 'post'
  })
}
```

---

### 修正 5：前端 menu.js — 改路径

**文件**：`ruoyi-ui/src/api/menu.js`

```javascript
// 改动前：
url: '/auth/api/getRouters',

// 改动后：
url: '/auth/getRouters',
```

完整文件：

```javascript
import request from '@/utils/request'

export const getRouters = () => {
  return request({
    url: '/auth/getRouters', // StripPrefix=1 → /getRouters → auth:8085 /auth/getRouters ✓
    method: 'get'
  })
}
```

---

### 修正 6（High）：解决 `/api/sysUser/login` NPE 问题

check01.md 显示 admin 和 lisi 登录均返回 500 NPE。可能原因：BCrypt 密码哈希不匹配。

**先用 dev 接口重置密码**：

```bash
# admin（id=11）
curl -X POST http://localhost:8085/api/sysUser/dev/reset-password \
  -H "Content-Type: application/json" \
  -d '{"userId":11,"newPassword":"123456"}'

# lisi（id=16）
curl -X POST http://localhost:8085/api/sysUser/dev/reset-password \
  -H "Content-Type: application/json" \
  -d '{"userId":16,"newPassword":"123456"}'
```

如果仍然返回 500，查看 auth 服务的控制台日志，定位 `SysUserServiceImpl.login()` 中的 NPE 根因。

---

## 四、修正后验证命令

重启 auth 和 gateway 服务后，执行：

```bash
# T1: admin 登录
curl -X POST http://localhost:8086/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'
# 期望: { "code":200, "data":{ "token":"11", "userId":11, "expires":... } }

# T2: 获取用户信息
curl -X GET http://localhost:8086/auth/getInfo \
  -H "Authorization: Bearer 11"
# 期望: { "code":200, "data":{ "userId":11, "userName":"admin", "roles":["ROLE_SUPER_ADMIN"], "permissions":[...] } }

# T3: 获取路由
curl -X GET http://localhost:8086/auth/getRouters \
  -H "Authorization: Bearer 11"
# 期望: { "code":200, "data":[ {"path":"system",...}, {"path":"sales",...}, {"path":"finance",...} ] }

# T4: 无 token 访问（应返回 401/403）
curl -X GET http://localhost:8086/auth/getInfo
# 期望: { "code":401, "message":"未登录" }

# T5: lisi 登录后路由（仅销售菜单）
# 先通过 dev 接口获取 lisi 的 token，然后用 token 访问 getRouters
# 期望: SYSTEM children=1, SALES children=13, FINANCE=0
```

---

## 五、修正对照表

| 序号 | 文件 | 操作 | 改动说明 |
|-----|------|------|---------|
| 1 | `AuthController.java` | 修改 | `@RequestMapping("/api/auth")` → `/auth` |
| 2 | `SecurityConfig.java` | 修改 | 新增 `.requestMatchers("/auth/**").permitAll()` |
| 3 | `gateway/application.yml` | 修改 | `StripPrefix=2` → `StripPrefix=1` |
| 4 | `ruoyi-ui/src/api/login.js` | 修改 | `/auth/api/login` → `/auth/login`，其余同理 |
| 5 | `ruoyi-ui/src/api/menu.js` | 修改 | `/auth/api/getRouters` → `/auth/getRouters` |
| 6 | 各微服务（可选） | — | 若 `/api/sysUser/login` 仍 NPE，查日志定位 |

**重启要求**：
- gateway 服务重启（使 StripPrefix=1 生效）
- auth 服务重启（加载新的 SecurityConfig 和 AuthController 路径）

---

## 六、doc03 与 doc04 的关键差异说明

doc03 中的 Gateway StripPrefix=2 逻辑推导有误，在此修正：

| 项目 | doc03 错误设计 | doc04 正确方案 |
|------|--------------|--------------|
| Gateway StripPrefix | 2 | 1 |
| AuthController 路径 | `/api/auth` | `/auth` |
| 前端 login 路径 | `/auth/api/login` | `/auth/login` |
| 前端 getRouters | `/auth/api/getRouters` | `/auth/getRouters` |
| 到达后端路径 | `/login`（错误） | `/login`（正确匹配 `/auth/login`）|

**doc03 已执行但有误的部分，以 doc04 为准。**
