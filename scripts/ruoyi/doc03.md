# NeoCC × ruoyi-ui 接入执行文档（可直接落地）

**项目**：NeoCC 后端 + 若依（ruoyi-ui v3/Vue3）
**版本**：v1.0
**日期**：2026-04-16
**前提**：Phase 1（后端）必须先完成，Phase 2-8（前端）依赖 Phase 1 的 API 可用

---

## 路由架构（重要先搞懂）

```
浏览器 (localhost:3000)
        │
        │ 登录相关: /auth/api/*    → Vite → Gateway → auth-service (:8085)
        │ 系统管理: /system/api/*  → Vite → Gateway → system (:8082)
        │ 销售业务: /sales/api/*   → Vite → Gateway → sales (:8083)
        │ 金融业务: /finance/api/*  → Vite → Gateway → finance (:8084)
        ▼
  Spring Cloud Gateway (:8086)
        │
        ├─ /auth/**   → auth:8085  (StripPrefix=2, 即去掉 /auth/api → /api)
        ├─ /system/** → system:8082 (StripPrefix=1, 即去掉 /system → /)
        ├─ /sales/**  → sales:8083  (StripPrefix=1)
        └─ /finance/** → finance:8084 (StripPrefix=1)
```

**关键**：auth 路由 StripPrefix=2，前端必须使用 `/auth/api/xxx` 前缀。
business 微服务路由 StripPrefix=1，前端使用 `/sales/api/xxx` 等。

---

## Phase 1：后端改造

### 1.1 修改 Gateway StripPrefix

**文件**：`gateway/src/main/resources/application.yml`

```yaml
        - id: auth-route
          uri: http://localhost:8085
          predicates:
            - Path=/auth/**
          filters:
            - StripPrefix=2   # 原来是 1，改为 2
```

> **为什么改**：前端 login.js 改用 `/auth/api/login` 后，StripPrefix=2 去掉 `/auth/api` 前缀，auth 服务收到 `/api/login`，AuthController 在 `@RequestMapping("/api/auth")` 下即可匹配。

### 1.2 新增 SysRoleService.getRoleCodesByUserId

**文件**：`auth/src/main/java/com/dafuweng/auth/service/SysRoleService.java`

在 interface 最后添加：

```java
    /**
     * 根据用户ID查询角色编码列表
     * @param userId 用户ID
     * @return 角色编码列表，如 ["SUPER_ADMIN", "ZONE_DIRECTOR"]
     */
    List<String> getRoleCodesByUserId(Long userId);
```

**文件**：`auth/src/main/java/com/dafuweng/auth/service/impl/SysRoleServiceImpl.java`

```java
    @Autowired
    private SysRoleDao sysRoleDao;
    @Autowired
    private SysUserRoleDao sysUserRoleDao;

    @Override
    public List<String> getRoleCodesByUserId(Long userId) {
        List<Long> roleIds = sysUserRoleDao.selectRoleIdsByUserId(userId);
        if (roleIds == null || roleIds.isEmpty()) return new ArrayList<>();
        return roleIds.stream()
            .map(id -> {
                SysRoleEntity role = sysRoleDao.selectById(id);
                return role != null ? role.getRoleCode() : null;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
```

> 需添加 import：`import com.dafuweng.auth.dao.SysUserRoleDao;` `import com.dafuweng.auth.entity.SysRoleEntity;` `import java.util.Objects;`

### 1.3 新增 AuthController

**文件**：`auth/src/main/java/com/dafuweng/auth/controller/AuthController.java`

完整文件内容：

```java
package com.dafuweng.auth.controller;

import com.dafuweng.auth.entity.SysPermissionEntity;
import com.dafuweng.auth.entity.SysUserEntity;
import com.dafuweng.auth.service.SysPermissionService;
import com.dafuweng.auth.service.SysRoleService;
import com.dafuweng.auth.service.SysUserService;
import com.dafuweng.common.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private SysUserService sysUserService;
    @Autowired private SysPermissionService sysPermissionService;
    @Autowired private SysRoleService sysRoleService;

    /**
     * POST /api/auth/login
     * 请求: { username, password }
     * 响应: { code:200, data:{ token:"...", userId:Long, expires:Long } }
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> req) {
        String username = req.get("username");
        String password = req.get("password");
        SysUserEntity user = sysUserService.login(username, password, "127.0.0.1");
        String token = String.valueOf(user.getId()); // userId 即 token
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("expires", System.currentTimeMillis() + 7200000L);
        return Result.success(data);
    }

    /**
     * GET /api/auth/getInfo
     * Header: Authorization: Bearer <token(userId)>
     * 响应: { code:200, data:{ user:{userId,userName,nickName,avatar}, roles:[], permissions:[] } }
     */
    @GetMapping("/getInfo")
    public Result<Map<String, Object>> getInfo(@RequestHeader(value = "Authorization", required = false) String auth) {
        Long userId = extractUserId(auth);
        if (userId == null) return Result.fail("未登录");
        SysUserEntity user = sysUserService.getById(userId);
        if (user == null) return Result.fail("用户不存在");

        List<String> roleCodes = sysRoleService.getRoleCodesByUserId(userId);
        List<String> permCodes = sysUserService.getPermCodesByUserId(userId);

        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getId());
        data.put("userName", user.getUsername());
        data.put("nickName", user.getRealName());
        data.put("avatar", "");
        // RuoYi 前端期望 "ROLE_" 前缀
        data.put("roles", roleCodes.stream().map(c -> "ROLE_" + c).collect(Collectors.toList()));
        data.put("permissions", permCodes);
        return Result.success(data);
    }

    /**
     * GET /api/auth/getRouters
     * 根据用户权限码，返回 Vue Router 格式菜单树
     * 响应: { code:200, data:[ { path, component, name, meta:{title,icon}, children:[...] } ] }
     */
    @GetMapping("/getRouters")
    public Result<List<Map<String, Object>>> getRouters(@RequestHeader(value = "Authorization", required = false) String auth) {
        Long userId = extractUserId(auth);
        if (userId == null) return Result.fail("未登录");

        List<String> permCodes = sysUserService.getPermCodesByUserId(userId);
        List<SysPermissionEntity> allMenus = sysPermissionService.listByStatus((short) 1);

        List<Map<String, Object>> routes = buildRouteTree(allMenus, permCodes);
        return Result.success(routes);
    }

    /** POST /api/auth/logout */
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }

    // ========== 私有工具方法 ==========

    private Long extractUserId(String auth) {
        if (auth == null || !auth.startsWith("Bearer ")) return null;
        try {
            return Long.parseLong(auth.substring(7).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** 构建 Vue Router JSON 树，仅返回有权限的菜单 */
    private List<Map<String, Object>> buildRouteTree(List<SysPermissionEntity> allMenus, List<String> permCodes) {
        Set<String> codeSet = new HashSet<>(permCodes);
        return allMenus.stream()
            .filter(m -> m.getDeleted() == 0 && m.getStatus() == 1)
            .filter(m -> m.getParentId() == 0 && codeSet.contains(m.getPermCode()))
            .map(parent -> {
                Map<String, Object> node = new HashMap<>();
                node.put("path", permCodeToRoutePath(parent.getPermCode()));
                node.put("component", "Layout");
                node.put("name", parent.getPermCode());
                node.put("meta", Map.of(
                    "title", parent.getPermName(),
                    "icon", parent.getIcon() != null ? parent.getIcon() : ""
                ));
                List<Map<String, Object>> children = allMenus.stream()
                    .filter(m -> Objects.equals(m.getParentId(), parent.getId()))
                    .filter(m -> m.getDeleted() == 0 && codeSet.contains(m.getPermCode()))
                    .map(child -> {
                        Map<String, Object> c = new HashMap<>();
                        c.put("path", child.getPermCode().toLowerCase().replace("_", "-"));
                        c.put("component", permCodeToComponent(child.getPermCode()));
                        c.put("name", child.getPermCode());
                        c.put("meta", Map.of(
                            "title", child.getPermName(),
                            "icon", child.getIcon() != null ? child.getIcon() : ""
                        ));
                        return c;
                    }).collect(Collectors.toList());
                node.put("children", children);
                return node;
            }).collect(Collectors.toList());
    }

    /** perm_code → Vue Router path */
    private String permCodeToRoutePath(String permCode) {
        String lower = permCode.toLowerCase();
        if (lower.startsWith("system")) return "system";
        if (lower.startsWith("sales")) return "sales";
        if (lower.startsWith("finance")) return "finance";
        return lower.replace("_", "-");
    }

    /** perm_code → Vue component 路径（相对于 src/views/） */
    private String permCodeToComponent(String permCode) {
        String lower = permCode.toLowerCase().replace("_", "-");
        if (lower.startsWith("system")) return "system/" + lower.replace("system-", "") + "/index";
        if (lower.startsWith("sales")) return "sales/" + lower.replace("sales-", "") + "/index";
        if (lower.startsWith("finance")) return "finance/" + lower.replace("finance-", "") + "/index";
        return lower + "/index";
    }
}
```

### 1.4 后端 Phase 1 自测命令

```bash
# 1. admin 登录（id=11，密码 123456）
curl -X POST http://localhost:8086/auth/api/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'
# 期望: { "code":200, "data":{ "token":"11", "userId":11, "expires":... } }

# 2. 获取用户信息（使用上面返回的 token）
curl -X GET http://localhost:8086/auth/api/getInfo \
  -H "Authorization: Bearer 11"
# 期望: { "code":200, "data":{ "userId":11, "userName":"admin", "roles":["ROLE_SUPER_ADMIN"], "permissions":["SYSTEM","SYSTEM_USER",...] } }

# 3. 获取路由（admin 应看到 system/sales/finance 全部菜单）
curl -X GET http://localhost:8086/auth/api/getRouters \
  -H "Authorization: Bearer 11"
# 期望: { "code":200, "data":[ {"path":"system", "children":[...]}, {"path":"sales",...}, {"path":"finance",...} ] }

# 4. 用 lisi 登录（id=16，密码 123456），验证只有销售菜单
curl -X POST http://localhost:8086/auth/api/login \
  -H "Content-Type: application/json" \
  -d '{"username":"lisi","password":"123456"}'
# 取 token 后调用 /getRouters，应只有 sales 菜单

# 5. 无 token 访问 getInfo
curl -X GET http://localhost:8086/auth/api/getInfo
# 期望: { "code":500, "message":"未登录" }
```

---

## Phase 2：前端 Token 存储改造

### 2.1 auth.js — Cookie → localStorage

**文件**：`ruoyi-ui/src/utils/auth.js`

完整文件替换为：

```javascript
import Cookies from 'js-cookie'

const TokenKey = 'Admin-Token'

export function getToken() {
  return localStorage.getItem(TokenKey) || Cookies.get(TokenKey)
}

export function setToken(token) {
  localStorage.setItem(TokenKey, token)
  Cookies.set(TokenKey, token)
}

export function removeToken() {
  localStorage.removeItem(TokenKey)
  Cookies.remove(TokenKey)
}
```

> **改动说明**：保留 Cookies 作为兼容 fallback，主要使用 localStorage。

### 2.2 request.js — 动态 baseURL + message 字段适配

**文件**：`ruoyi-ui/src/utils/request.js`

**改动 1**：axios 实例化前添加 `getBaseURL()` 函数，替换静态 `baseURL`。

在 `axios.defaults.headers['Content-Type']` 之后、`const service = axios.create()` 之前插入：

```javascript
// NeoCC 微服务路径 → Gateway 路由前缀
function getBaseURL(url) {
  if (!url || url.startsWith('http')) return import.meta.env.VITE_APP_BASE_API
  // 认证接口（login/getInfo/getRouters/logout）
  if (url.startsWith('/login') || url.startsWith('/logout') ||
      url.startsWith('/getInfo') || url.startsWith('/getRouters')) {
    return '/auth/api'
  }
  // auth 模块业务接口（sysUser/sysRole/sysPermission/sysDept/sysZone）
  if (url.startsWith('/sysUser') || url.startsWith('/sysRole') ||
      url.startsWith('/sysPermission') || url.startsWith('/sysDept') ||
      url.startsWith('/sysZone') || url.startsWith('/sysDict') ||
      url.startsWith('/sysParam') || url.startsWith('/sysOperationLog')) {
    return '/auth/api'
  }
  // sales 模块
  if (url.startsWith('/customer') || url.startsWith('/contract') ||
      url.startsWith('/contactRecord') || url.startsWith('/workLog') ||
      url.startsWith('/performanceRecord') || url.startsWith('/customerTransferLog') ||
      url.startsWith('/contractAttachment')) {
    return '/sales/api'
  }
  // finance 模块
  if (url.startsWith('/bank') || url.startsWith('/financeProduct') ||
      url.startsWith('/loanAudit') || url.startsWith('/loanAuditRecord') ||
      url.startsWith('/commission') || url.startsWith('/serviceFee')) {
    return '/finance/api'
  }
  // system 模块（直接路由）
  if (url.startsWith('/system')) return '/system/api'
  return '/dev-api'
}
```

**改动 2**：替换 `const service = axios.create({ baseURL: import.meta.env.VITE_APP_BASE_API, ... })` 为：

```javascript
const service = axios.create({
  baseURL: getBaseURL(import.meta.env.VITE_APP_BASE_API),  // 初始化时用 VITE_APP_BASE_API
  timeout: 10000
})
```

但 axios.create 的 baseURL 在实例创建时就定了，动态切换需在拦截器里处理。更正确的改法：

```javascript
// 改 const service = axios.create({...}) 的 baseURL 为空字符串
const service = axios.create({
  baseURL: '',
  timeout: 10000
})
```

然后在 `request拦截器` 开头（`config` 上方）添加：

```javascript
  service.interceptors.request.use(config => {
    // NeoCC 动态 baseURL
    config.baseURL = getBaseURL(config.url)
    // ... 后面原有逻辑不变 ...
```

**改动 3**：message 字段顺序调整。找到：

```javascript
    const msg = errorCode[code] || res.data.msg || res.data.message || errorCode['default']
```

改为（NeoCC 用 `message` 而非 `msg`）：

```javascript
    const msg = errorCode[code] || res.data.message || res.data.msg || errorCode['default']
```

### 2.3 login.js — 修正登录路径

**文件**：`ruoyi-ui/src/api/login.js`

```javascript
// 改动前
    url: '/login',

// 改动后
    url: '/auth/api/login',
```

完整文件：

```javascript
import request from '@/utils/request'

export function login(username, password) {
  return request({
    url: '/auth/api/login',  // Gateway /auth/** → auth:8085，StripPrefix=2 → /api/login
    headers: {
      isToken: false,
      repeatSubmit: false
    },
    method: 'post',
    data: { username, password }
  })
}

export function getInfo() {
  return request({
    url: '/auth/api/getInfo',  // 不走 Vite 代理前缀，由 getBaseURL() 路由
    method: 'get'
  })
}

export function logout() {
  return request({
    url: '/auth/api/logout',
    method: 'post'
  })
}
```

### 2.4 menu.js — 修正路由路径

**文件**：`ruoyi-ui/src/api/menu.js`

```javascript
// 改动前
    url: '/getRouters',

// 改动后
    url: '/auth/api/getRouters',
```

完整文件：

```javascript
import request from '@/utils/request'

export const getRouters = () => {
  return request({
    url: '/auth/api/getRouters',
    method: 'get'
  })
}
```

---

## Phase 3：前端 API 模块路径修正

### 3.1 system/user.js

**文件**：`ruoyi-ui/src/api/system/user.js`

路径对照：

| 改动前 | 改动后 | 对应后端 |
|--------|--------|---------|
| `/system/user/*` | `/sysUser/*` | SysUserController |

所有 `url: '/system/user/xxx'` 改为 `url: '/sysUser/xxx'`。

### 3.2 system/role.js

**文件**：`ruoyi-ui/src/api/system/role.js`

| 改动前 | 改动后 | 对应后端 |
|--------|--------|---------|
| `/system/role/*` | `/sysRole/*` | SysRoleController |

所有 `url: '/system/role/xxx'` 改为 `url: '/sysRole/xxx'`。

### 3.3 system/permission.js

**文件**：`ruoyi-ui/src/api/system/permission.js`

| 改动前 | 改动后 | 对应后端 |
|--------|--------|---------|
| `/system/permission/*` | `/sysPermission/*` | SysPermissionController |

所有 `url: '/system/permission/xxx'` 改为 `url: '/sysPermission/xxx'`。

### 3.4 system/dept.js

**文件**：`ruoyi-ui/src/api/system/dept.js`

| 改动前 | 改动后 | 对应后端 |
|--------|--------|---------|
| `/system/dept/*` | `/sysDept/*` | SysDepartmentController |

所有 `url: '/system/dept/xxx'` 改为 `url: '/sysDept/xxx'`。

### 3.5 system/zone.js

**文件**：`ruoyi-ui/src/api/system/zone.js`

| 改动前 | 改动后 | 对应后端 |
|--------|--------|---------|
| `/system/zone/*` | `/sysZone/*` | SysZoneController |

所有 `url: '/system/zone/xxx'` 改为 `url: '/sysZone/xxx'`。

---

## Phase 4：删除冗余页面

**保留**：
```
src/views/system/    ← 7个页面（user/role/permission/dept/zone/dict/param）
src/views/sales/     ← 6个页面（customer/contract/worklog/contact/transfer/performance）
src/views/finance/   ← 5个页面（bank/product/loan-audit/commission/service-fee）
src/views/login/     ← 1个页面（登录页，需改造）
```

**必须删除以下三个目录**：

```bash
# 在 ruoyi-ui/src/views/ 下执行
rm -rf monitor/
rm -rf tool/
rm -rf generator/
```

或直接在文件管理器中删除 `src/views/monitor/`、`src/views/tool/`、`src/views/generator/` 三个目录。

---

## Phase 5：登录页适配

**文件**：`ruoyi-ui/src/views/login/index.vue`

改动点：

1. **标题/Logo**：将所有"若依"字样替换为"NeoCC"
2. **登录失败提示**：确认使用 `res.message`（后端返回字段）显示错误信息
3. **确认 setToken 已调用**：login.js 已改为 `setToken(token)` 写入 localStorage，无需额外改动

---

## Phase 6：新建 Vue 页面（17个）

以下 17 个页面需新建，按三批交付。**每批开发完成后即可自测。**

### 6.1 第一批（核心业务，3天）

| # | 页面路径 | 主要 API 调用 | 关键组件 |
|---|---------|-------------|---------|
| 1 | `src/views/sales/customer/index.vue` | `/customer/page` 列表, `POST/PUT /customer` 新增编辑, `DELETE /customer/{id}` 删除 | el-table, el-pagination, el-dialog |
| 2 | `src/views/sales/contract/index.vue` | `/contract/page`, `POST/PUT /contract`, `GET /contract/{id}`, `GET /contract/{id}/attachments` | el-table, el-pagination, file upload |
| 3 | `src/views/finance/loan-audit/index.vue` | `/loanAudit/page`, `POST /{id}/receive`, `POST /{id}/review`, `POST /{id}/submit-bank`, `POST /{id}/bank-result`, `POST /{id}/approve`, `POST /{id}/reject` | el-table, el-steps, audit dialog |

**loan-audit/index.vue** 是最复杂页面，需实现完整状态机：

```
1 待接收 → receive() → 2 初审中 → review() → 3 已提交银行
→ bank-result(approved) → 6 终审通过
→ bank-result(!approved) → 5 银行拒绝 [terminal]
→ approve() → 6 终审通过 [terminal]
→ reject() → 7 终审拒绝 [terminal]
```

审核按钮根据当前 audit_status 动态显示（参考 `audit_status` 字段值）。

### 6.2 第二批（配套业务，2天）

| # | 页面路径 | 主要 API |
|---|---------|---------|
| 4 | `src/views/sales/worklog/index.vue` | `/workLog/page`, `POST/PUT /workLog` |
| 5 | `src/views/sales/contact/index.vue` | `/contactRecord/page`, `POST/PUT /contactRecord` |
| 6 | `src/views/finance/bank/index.vue` | `/bank/page`, `POST/PUT /bank`, `DELETE /bank/{id}` |
| 7 | `src/views/finance/product/index.vue` | `/financeProduct/page`, `POST/PUT /financeProduct`, `DELETE /financeProduct/{id}` |

### 6.3 第三批（系统管理 + 辅助模块，2天）

| # | 页面路径 | 主要 API |
|---|---------|---------|
| 8 | `src/views/system/user/index.vue` | `/sysUser/page`, `POST/PUT /sysUser`, `DELETE /sysUser/{id}`, `GET /sysUser/{id}/roles`, `PUT /sysUser/{id}/roles` |
| 9 | `src/views/system/role/index.vue` | `/sysRole/page`, `POST/PUT /sysRole`, `DELETE /sysRole/{id}`, `GET /sysRole/{id}/permissions`, `PUT /sysRole/{id}/permissions` |
| 10 | `src/views/system/department/index.vue` | `/sysDept/page` |
| 11 | `src/views/system/zone/index.vue` | `/sysZone/page`, `/sysZone/listAll` |
| 12 | `src/views/system/dict/index.vue` | `/sysDict/page`, `/sysDict/listByDictType` |
| 13 | `src/views/system/param/index.vue` | `/sysParam/page`, `GET /sysParam/value/{key}`, `PUT /sysParam` |
| 14 | `src/views/finance/commission/index.vue` | `/commissionRecord/page`, `POST /{id}/confirm`, `POST /{id}/grant` |
| 15 | `src/views/finance/service-fee/index.vue` | `/serviceFeeRecord/page`, `POST /{id}/pay` |
| 16 | `src/views/sales/transfer/index.vue` | `/customerTransferLog/page` |
| 17 | `src/views/sales/performance/index.vue` | `/performanceRecord/page` |

> **注意**：销售员只能看到自己创建的客户/合同/工作日志，列表查询需带上当前用户 ID（从 store 获取）。

---

## Phase 7：权限体系联调

完成后端 `AuthController` 和前端 `auth.js`/`request.js` 改造后：

1. 用 **admin** 登录 → 侧边栏应出现 system + sales + finance 全部菜单
2. 用 **lisi**（sales rep）登录 → 侧边栏应只有 sales 相关菜单
3. 用 **financeuser** 登录 → 侧边栏应只有 finance 相关菜单
4. 刷新页面后菜单不丢失（动态路由已持久化到 router）

按钮级权限（`v-if="hasPerm('SALES_CUSTOMER_ADD')"`）在 Phase 3 页面开发中逐步接入，非 Phase 7 重点。

---

## Phase 8：集成测试用例

### 8.1 认证流程

```bash
# T1: admin 登录成功
curl -X POST http://localhost:8086/auth/api/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'
# 断言: code=200, data.token 非空

# T2: admin 获取完整路由
TOKEN=11
curl -X GET http://localhost:8086/auth/api/getRouters \
  -H "Authorization: Bearer $TOKEN"
# 断言: code=200, data 包含 system/sales/finance 三个顶级节点

# T3: lisi 只能看到 sales 菜单
curl -X POST http://localhost:8086/auth/api/login \
  -H "Content-Type: application/json" \
  -d '{"username":"lisi","password":"123456"}'
# 取 token 后调用 getRouters，断言只有 sales 子菜单

# T4: 未登录访问受限接口
curl -X GET http://localhost:8086/auth/api/getInfo
# 断言: code=500, message 含"未登录"
```

### 8.2 业务接口验证

```bash
# T5: admin 创建客户
curl -X POST http://localhost:8086/sales/api/customer \
  -H "Authorization: Bearer 11" \
  -H "Content-Type: application/json" \
  -d '{"customerName":"测试客户","contactPhone":"13800138000","zoneId":1}'
# 断言: code=200

# T6: 无 token 创建客户
curl -X POST http://localhost:8086/sales/api/customer \
  -H "Content-Type: application/json" \
  -d '{"customerName":"测试客户"}'
# 断言: 401

# T7: 分页查询客户
curl "http://localhost:8086/sales/api/customer/page?page=1&size=10" \
  -H "Authorization: Bearer 11"
# 断言: code=200, data.list 非空数组
```

---

## 文件改动总表

| # | 文件 | 操作 | 改动说明 |
|---|------|------|---------|
| 1 | `gateway/src/main/resources/application.yml` | 修改 | StripPrefix=1 → 2 |
| 2 | `auth/.../SysRoleService.java` | 新增方法 | `getRoleCodesByUserId(Long)` |
| 3 | `auth/.../SysRoleServiceImpl.java` | 实现方法 | 查询 sys_role 表返回 role_code |
| 4 | `auth/.../AuthController.java` | 新建 | login/getInfo/getRouters/logout |
| 5 | `ruoyi-ui/src/utils/auth.js` | 修改 | Cookie → localStorage |
| 6 | `ruoyi-ui/src/utils/request.js` | 修改 | getBaseURL() + message 字段顺序 |
| 7 | `ruoyi-ui/src/api/login.js` | 修改 | url: '/login' → '/auth/api/login' 等 |
| 8 | `ruoyi-ui/src/api/menu.js` | 修改 | url: '/getRouters' → '/auth/api/getRouters' |
| 9 | `ruoyi-ui/src/api/system/user.js` | 修改 | `/system/user/*` → `/sysUser/*` |
| 10 | `ruoyi-ui/src/api/system/role.js` | 修改 | `/system/role/*` → `/sysRole/*` |
| 11 | `ruoyi-ui/src/api/system/permission.js` | 修改 | `/system/permission/*` → `/sysPermission/*` |
| 12 | `ruoyi-ui/src/api/system/dept.js` | 修改 | `/system/dept/*` → `/sysDept/*` |
| 13 | `ruoyi-ui/src/api/system/zone.js` | 修改 | `/system/zone/*` → `/sysZone/*` |
| 14 | `ruoyi-ui/src/views/monitor/` | 删除 | 运维监控 |
| 15 | `ruoyi-ui/src/views/tool/` | 删除 | 系统工具 |
| 16 | `ruoyi-ui/src/views/generator/` | 删除 | 代码生成器 |
| 17 | `ruoyi-ui/src/views/login/index.vue` | 修改 | 标题改为 NeoCC |
| 18-34 | `ruoyi-ui/src/views/sales/customer/index.vue` 等17个 | 新建 | 页面开发 |

---

## 执行顺序（严格按此顺序）

```
Phase 1.1  →  修改 Gateway StripPrefix（重启 gateway 服务）
Phase 1.2  →  SysRoleService 新增 getRoleCodesByUserId（重启 auth 服务）
Phase 1.3  →  新建 AuthController（重启 auth 服务）
Phase 1.4  →  自测 5 个 curl 命令，全部通过再继续

Phase 2.1  →  修改 auth.js
Phase 2.2  →  修改 request.js（两处改动）
Phase 2.3  →  修改 login.js
Phase 2.4  →  修改 menu.js

Phase 3    →  修正 5 个 API module 路径

Phase 4    →  删除 3 个冗余目录

Phase 5    →  登录页标题改 NeoCC

Phase 6    →  新建 17 个 Vue 页面（可分三批）

Phase 7    →  权限体系联调自测（admin/lisi/financeuser 三账号验证）

Phase 8    →  集成测试用例（T1-T7）
```

**合计约 7-9 人天**（Phase 1 后端 0.5 天 + Phase 2-5 前端基础 1.5 天 + Phase 6 页面开发 4-5 天 + Phase 7-8 测试 1 天）。
