# NeoCC × ruoyi-ui 接入开发文档

**项目**：NeoCC 后端 + 若依（ruoyi-ui v3/Vue3）
**版本**：v1.0
**日期**：2026-04-16
**目标**：废弃原生 JS 前端，采用 ruoyi-ui 框架，前后台完全对接

---

## 一、系统架构

```
浏览器 (localhost:3000)
        │
        │ GET/POST /dev-api/* → Vite Proxy (dev) → localhost:8086 (Gateway)
        │
        ▼
  Spring Cloud Gateway (:8086)
        │
        ├─ /auth/*   → auth-service (:8085)
        ├─ /system/* → system-service (:8082)
        ├─ /sales/*  → sales-service (:8083)
        └─ /finance/* → finance-service (:8084)
```

**现状**：
- ruoyi-ui Vite 代理已配置：`/dev-api` → `http://localhost:8086`
- Gateway 已配置 StripPrefix=1，路由到各微服务
- 后端已有完整业务 API 119 个（gateway 暴露）+ 4 个内部 Feign API

---

## 二、后端改造

### 2.1 新增 Auth 控制器

**文件**：`auth/src/main/java/com/dafuweng/auth/controller/AuthController.java`

RuoYi 前端依赖 3 个固定接口，NeoCC 后端目前均不存在，需新建。

```java
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
        String token = String.valueOf(user.getId());
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
    public Result<Map<String, Object>> getInfo(@RequestHeader(value="Authorization", required=false) String auth) {
        Long userId = extractUserId(auth);
        SysUserEntity user = sysUserService.getById(userId);
        List<String> roleCodes = sysRoleService.getRoleCodesByUserId(userId);
        List<String> permCodes = sysUserService.getPermCodesByUserId(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getId());
        data.put("userName", user.getUsername());
        data.put("nickName", user.getRealName());
        data.put("avatar", "");
        data.put("roles", roleCodes.stream().map(c -> "ROLE_" + c).collect(Collectors.toList()));
        data.put("permissions", permCodes);
        return Result.success(data);
    }

    /**
     * GET /api/auth/getRouters
     * 根据当前用户权限码，返回 Vue Router 格式菜单树
     * 响应: { code:200, data:[ { path, component, meta:{title,icon}, children:[...] } ] }
     */
    @GetMapping("/getRouters")
    public Result<List<Map<String, Object>>> getRouters(@RequestHeader(value="Authorization", required=false) String auth) {
        Long userId = extractUserId(auth);
        List<String> permCodes = sysUserService.getPermCodesByUserId(userId);
        List<SysPermissionEntity> allMenus = sysPermissionService.listByStatus((short)1);
        List<Map<String, Object>> routes = buildRouteTree(allMenus, permCodes);
        return Result.success(routes);
    }

    /** POST /api/auth/logout */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestBody Map<String, Long> req) {
        return Result.success();
    }

    /** 工具方法：从 Authorization: Bearer <token> 提取 userId */
    private Long extractUserId(String auth) {
        if (auth == null || !auth.startsWith("Bearer ")) return null;
        return Long.parseLong(auth.substring(7));
    }

    /** 构建 Vue Router JSON 树 */
    private List<Map<String, Object>> buildRouteTree(List<SysPermissionEntity> allMenus, List<String> permCodes) {
        Set<String> codeSet = new HashSet<>(permCodes);
        // 取 perm_type=1 的顶级菜单（parent_id=0）
        return allMenus.stream()
            .filter(m -> m.getDeleted() == 0 && m.getStatus() == 1)
            .filter(m -> m.getParentId() == 0 && codeSet.contains(m.getPermCode()))
            .map(parent -> {
                Map<String, Object> node = new HashMap<>();
                node.put("path", permCodeToRoutePath(parent.getPermCode()));
                node.put("component", "Layout");
                node.put("meta", Map.of(
                    "title", parent.getPermName(),
                    "icon", parent.getIcon() != null ? parent.getIcon() : ""
                ));
                // 子菜单
                List<Map<String, Object>> children = allMenus.stream()
                    .filter(m -> m.getParentId().equals(parent.getId()))
                    .filter(m -> m.getDeleted() == 0 && codeSet.contains(m.getPermCode()))
                    .map(child -> {
                        Map<String, Object> c = new HashMap<>();
                        c.put("path", child.getPermCode().toLowerCase().replace("_", "-"));
                        c.put("component", permCodeToComponent(child.getPermCode()));
                        c.put("name", child.getPermCode());
                        c.put("meta", Map.of("title", child.getPermName(), "icon", child.getIcon() != null ? child.getIcon() : ""));
                        return c;
                    }).collect(Collectors.toList());
                node.put("children", children);
                return node;
            }).collect(Collectors.toList());
    }

    /** perm_code → Vue Router path（如 SYSTEM_USER → system/user） */
    private String permCodeToRoutePath(String permCode) {
        return permCode.toLowerCase().replace("_", "-").replace("system-", "system/").replace("sales-", "sales/").replace("finance-", "finance/");
    }

    /** perm_code → Vue component 路径（如 SYSTEM_USER → system/user/index） */
    private String permCodeToComponent(String permCode) {
        String lower = permCode.toLowerCase().replace("_", "-");
        if (lower.startsWith("system-")) return "system/" + lower.replace("system-", "") + "/index";
        if (lower.startsWith("sales-")) return "sales/" + lower.replace("sales-", "") + "/index";
        if (lower.startsWith("finance-")) return "finance/" + lower.replace("finance-", "") + "/index";
        return lower + "/index";
    }
}
```

### 2.2 Gateway 路由确认

**文件**：`gateway/src/main/resources/application.yml`

当前已配置 `/auth/**` → auth:8085，StripPrefix=1。无需改动。

前端请求 `/dev-api/auth/getInfo` → Vite Proxy → Gateway → auth-service `/api/auth/getInfo`。**路由本身无需改动**，只需确认 auth-service 的 `/api/auth/*` 路径正确。

### 2.3 登录接口格式对比与改造

| 字段 | RuoYi 期望 | NeoCC 现状 | 改造 |
|------|----------|----------|------|
| 路径 | `POST /login` | `POST /api/sysUser/login` | 新增 `/api/auth/login` |
| 请求体 | `{ username, password }` | 一致 | 一致 ✓ |
| 响应 token 位置 | `data.token` (String) | `data.id` (Long) | 返回格式完全重写 ✓ |

### 2.4 getInfo 响应格式

RuoYi 前端从 `data.user.userId`、`data.roles[]`、`data.permissions[]` 取值。字段名与 NeoCC 的 `SysUserEntity` 有差异：

| RuoYi 期望 | NeoCC SysUserEntity 字段 |
|-----------|------------------------|
| `user.userId` | `user.id` |
| `user.userName` | `user.username` |
| `user.nickName` | `user.realName` |
| `user.avatar` | 无（返回空字符串）|
| `roles` | 从 `sys_role.role_code` 取，加 `ROLE_` 前缀 |
| `permissions` | 从 `sys_permission.perm_code` 取 |

### 2.5 getRouters 返回格式

RuoYi 前端期望 Vue Router JSON 格式：

```json
{
  "code": 200,
  "data": [
    {
      "path": "/system",
      "component": "Layout",
      "meta": { "title": "系统管理", "icon": "system" },
      "children": [
        {
          "path": "user",
          "component": "system/user/index",
          "name": "SYSTEM_USER",
          "meta": { "title": "用户管理", "icon": "user" }
        }
      ]
    },
    {
      "path": "/sales",
      "component": "Layout",
      "meta": { "title": "销售管理", "icon": "chart" },
      "children": [
        { "path": "customer", "component": "sales/customer/index", "name": "SALES_CUSTOMER_LIST", "meta": { "title": "客户列表" } },
        { "path": "contract", "component": "sales/contract/index", "name": "SALES_CONTRACT_LIST", "meta": { "title": "合同列表" } }
      ]
    },
    {
      "path": "/finance",
      "component": "Layout",
      "meta": { "title": "金融审核", "icon": "money" },
      "children": [
        { "path": "loan-audit", "component": "finance/loan-audit/index", "name": "FINANCE_LOAN_AUDIT", "meta": { "title": "贷款审核" } }
      ]
    }
  ]
}
```

**component 命名规则**：
- `"Layout"` — 主布局框架组件（固定值，ruoyi-ui 内置）
- `"system/user/index"` — `views/system/user/index.vue`（相对于 `src/views/` 的路径）
- 同理：`"sales/customer/index"`, `"finance/loan-audit/index"` 等

**perm_code 到 component 路径映射表**（在 `AuthController.buildRouteTree()` 中使用）：

| perm_code | component 路径 | title |
|-----------|--------------|-------|
| `SYSTEM` | 不渲染（父级） | 系统管理 |
| `SYSTEM_USER` | `system/user/index` | 用户管理 |
| `SYSTEM_ROLE` | `system/role/index` | 角色管理 |
| `SYSTEM_PERMISSION` | `system/permission/index` | 权限管理 |
| `SYSTEM_DEPT` | `system/department/index` | 部门管理 |
| `SYSTEM_ZONE` | `system/zone/index` | 战区管理 |
| `SALES` | 不渲染（父级） | 销售管理 |
| `SALES_CUSTOMER_LIST` | `sales/customer/index` | 客户列表 |
| `SALES_CONTRACT_LIST` | `sales/contract/index` | 合同列表 |
| `SALES_CONTACT` | `sales/contact/index` | 跟进记录 |
| `SALES_WORKLOG` | `sales/worklog/index` | 工作日志 |
| `FINANCE` | 不渲染（父级） | 金融审核 |
| `FINANCE_BANK` | `finance/bank/index` | 银行管理 |
| `FINANCE_PRODUCT` | `finance/product/index` | 金融产品 |
| `FINANCE_LOAN_AUDIT` | `finance/loan-audit/index` | 贷款审核 |

---

## 三、前端改造

### 3.1 Token 存储适配

**文件**：`src/utils/auth.js`

```js
// 将 Cookie 改为 localStorage，与现有 NeoCC 逻辑一致
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

### 3.2 request.js 适配 NeoCC Result 结构

**文件**：`src/utils/request.js`

NeoCC 通用响应格式：`{ "code": 200, "data": {...}, "message": "success" }`

当前 ru.js 动态 baseURL

**文件**：`src/utils/request.js`

根据请求路径前缀，动态设置目标微服务 baseURL：

```js
function getBaseURL(url) {
  if (url.startsWith('/auth/') || url.startsWith('/getInfo') || url.startsWith('/getRouters') || url.startsWith('/login') || url.startsWith('/logout')) {
    return '/auth/api'
  }
  if (url.startsWith('/sysUser') || url.startsWith('/sysRole') || url.startsWith('/sysPermission')) {
    return '/auth/api'
  }
  if (url.startsWith('/sysDept') || url.startsWith('/sysDict') || url.startsWith('/sysZone') || url.startsWith('/sysParam') || url.startsWith('/sysOperationLog')) {
    return '/system/api'
  }
  if (url.startsWith('/customer') || url.startsWith('/contract') || url.startsWith('/contactRecord') || url.startsWith('/workLog') || url.startsWith('/performanceRecord') || url.startsWith('/customerTransferLog') || url.startsWith('/contractAttachment')) {
    return '/sales/api'
  }
  if (url.startsWith('/bank') || url.startsWith('/financeProduct') || url.startsWith('/loanAudit') || url.startsWith('/loanAuditRecord') || url.startsWith('/commission') || url.startsWith('/serviceFee')) {
    return '/finance/api'
  }
  return '/dev-api'
}
```

然后在 axios 实例化时使用 `baseURL: getBaseURL(config.url)` 代替静态的 `VITE_APP_BASE_API`。

### 3.4 API 模块路径调整

| ruoyi-ui API 文件 | 当前请求路径 | 需改为 |
|-----------------|-----------|-------|
| `src/api/system/user.js` | `/system/user/*` | `/sysUser/*`（auth 模块）|
| `src/api/system/role.js` | `/system/role/*` | `/sysRole/*` |
| `src/api/system/permission.js` | `/system/permission/*` | `/sysPermission/*` |
| `src/api/system/dept.js` | `/system/dept/*` | `/sysDept/*` |
| `src/api/system/zone.js` | `/system/zone/*` | `/sysZone/*` |

在 `src/api/system/*.js` 中，将 `url: '/system/user/xxx'` 改为 `url: '/sysUser/xxx'`（直接对应当前后端 Controller 路径）。

### 3.5 删除冗余页面

保留以下目录，删除其余 RuoYi 默认页面：

```
src/views/
├── system/     ← 保留（7个页面）
├── sales/      ← 保留（6个页面）
├── finance/    ← 保留（5个页面）
└── login/      ← 保留（需适配 NeoCC 登录页）
```

**必须删除**：
- `src/views/monitor/` — 运维监控（RuoYi 内置）
- `src/views/tool/` — 系统工具
- `src/views/generator/` — 代码生成器

### 3.6 登录页适配

**文件**：`src/views/login/index.vue`

当前登录页是 RuoYi 默认风格，需改造：
1. Logo 和标题改为 NeoCC
2. 登录成功后将 token 写入 localStorage（`setToken()`）
3. 登录失败提示使用 NeoCC 后端返回的 `message`

---

## 四、页面开发清单

共需新建 **17 个 Vue 页面**，可分三批完成：

### 第一批（核心业务，3天）

| 页面 | 路径 | 对应 API |
|------|------|---------|
| `sales/customer/index.vue` | 客户列表+新增+编辑 | `/customer/page`, `/customer` POST/PUT |
| `sales/contract/index.vue` | 合同列表+新增+编辑 | `/contract/page`, `/contract` POST/PUT |
| `finance/loan-audit/index.vue` | 贷款审核工作流 | `/loanAudit/page`, `/{id}/receive/review/submit-bank/bank-result/approve/reject` |

### 第二批（配套业务，2天）

| 页面 | 路径 | 对应 API |
|------|------|---------|
| `sales/worklog/index.vue` | 工作日志 | `/workLog/page`, POST/PUT |
| `sales/contact/index.vue` | 跟进记录 | `/contactRecord/page`, POST/PUT |
| `finance/bank/index.vue` | 银行管理 | `/bank/page`, CRUD |
| `finance/product/index.vue` | 金融产品 | `/financeProduct/page`, CRUD |

### 第三批（系统管理，2天）

| 页面 | 路径 | 对应 API |
|------|------|---------|
| `system/user/index.vue` | 用户管理 | `/sysUser/page`, CRUD + `/sysUser/{id}/roles` |
| `system/role/index.vue` | 角色管理 | `/sysRole/page`, CRUD + `/{id}/permissions` |
| `system/department/index.vue` | 部门管理 | `/sysDept/page` |
| `system/zone/index.vue` | 战区管理 | `/sysZone/page`, `/sysZone/listAll` |
| `finance/commission/index.vue` | 提成记录 | `/commissionRecord/page`, `/{id}/confirm/grant` |
| `finance/service-fee/index.vue` | 服务费记录 | `/serviceFeeRecord/page`, `/{id}/pay` |
| `sales/transfer/index.vue` | 客户转移记录 | `/customerTransferLog/page` |
| `system/dict/index.vue` | 数据字典 | `/sysDict/page`, `/sysDict/listByDictType` |
| `system/param/index.vue` | 系统参数 | `/sysParam/page`, `/sysParam/value/{key}` |

---

## 五、接口映射总表

### 5.1 认证接口（新增）

| 前端请求 | Vite代理 → Gateway | 后端 Controller | 响应格式 |
|---------|-------------------|----------------|---------|
| `POST /dev-api/login` | → `/auth/login` | `AuthController.login` | `{ code:200, data:{ token, userId, expires } }` |
| `GET /dev-api/getInfo` | → `/auth/getInfo` | `AuthController.getInfo` | `{ code:200, data:{ user, roles, permissions } }` |
| `GET /dev-api/getRouters` | → `/auth/getRouters` | `AuthController.getRouters` | `{ code:200, data:[{ path, component, meta, children }] }` |
| `POST /dev-api/logout` | → `/auth/logout` | `AuthController.logout` | `{ code:200 }` |

### 5.2 业务接口（已有，验证路径一致）

| ruoyi-ui API 文件 | 请求路径 | 后端 Controller | 状态 |
|-----------------|---------|----------------|------|
| `sales/customer.js` | `/customer/page` | `CustomerController.pageList` | ✓ |
| `sales/contract.js` | `/contract/page` | `ContractController.pageList` | ✓ |
| `sales/workLog.js` | `/workLog/page` | `WorkLogController.pageList` | ✓ |
| `sales/contactRecord.js` | `/contactRecord/page` | `ContactRecordController.pageList` | ✓ |
| `finance/bank.js` | `/bank/page` | `BankController.pageList` | ✓ |
| `finance/financeProduct.js` | `/financeProduct/page` | `FinanceProductController.pageList` | ✓ |
| `finance/loanAudit.js` | `/loanAudit/page` | `LoanAuditController.pageList` | ✓ |
| `finance/commission.js` | `/commissionRecord/page` | `CommissionRecordController.pageList` | ✓ |
| `finance/serviceFeeRecord.js` | `/serviceFeeRecord/page` | `ServiceFeeRecordController.pageList` | ✓ |
| `system/dept.js` | `/sysDept/page` | `SysDepartmentController.pageList` | ✓ |
| `system/zone.js` | `/sysZone/page` | `SysZoneController.pageList` | ✓ |

---

## 六、Gateway 路由确认

**需确认事项**：

1. `/dev-api/login` → Gateway 需要有路由到 auth-service。目前 Gateway 只有 `/auth/**` 路由，`/login`（无前缀）可能 404。
   **解决方案**：在 Gateway 增加 `- Path=/login` 路由指向 auth-service，或前端 `login.js` 使用 `url: '/auth/api/login'`

2. `/dev-api/getInfo` → `/auth/getInfo` → auth-service 接收路径是 `/api/auth/getInfo`（因为 StripPrefix=1 去掉 `/auth`）。需确认 `AuthController` 的 `@RequestMapping("/api/auth")` 与转发后的路径匹配。

---

## 七、实施计划

| 阶段 | 任务 | 工期 | 产出 |
|------|------|------|------|
| 阶段一 | 后端 AuthController（3接口） | 0.5天 | `AuthController.java` + 单元测试 |
| 阶段二 | 前端基础适配（auth.js + request.js） | 0.5天 | 可登录、可调用业务接口 |
| 阶段三 | 删除冗余页面 + 登录页改造 | 0.5天 | 干净的 ruoyi-ui 项目 |
| 阶段四 | 第一批核心业务页面（3个） | 2-3天 | 客户+合同+贷款审核 |
| 阶段五 | 第二批配套业务页面（4个） | 1.5天 | 工作日志+跟进+银行+产品 |
| 阶段六 | 第三批系统管理页面（5个） | 1.5天 | 用户+角色+部门+战区+其他 |
| 阶段七 | 权限体系联调 | 1天 | 侧边栏+按钮权限全通 |
| **合计** | | **约 7-9 人天** | |

---

## 八、测试用例

| 用例 | 操作 | 期望结果 |
|------|------|---------|
| admin 登录 | `POST /auth/api/login` | 返回 token，data.token 非空 |
| admin 获取信息 | `GET /auth/api/getInfo` | roles 含 ROLE_SUPER_ADMIN，permissions 含 SYSTEM* |
| admin 获取路由 | `GET /auth/api/getRouters` | system/sales/finance 全部菜单，非空 children |
| lisi 登录获取路由 | `GET /auth/api/getRouters` | 只有 sales 相关菜单 |
| financeuser 登录获取路由 | `GET /auth/api/getRouters` | 只有 finance 相关菜单 |
| 无 token 访问 | `GET /auth/api/getInfo`（无 header）| 401 或抛出异常 |
| 登录失败 | 错误密码 | 返回错误 message |

---

## 九、文件清单

### 后端新增

```
auth/src/main/java/com/dafuweng/auth/
└── controller/AuthController.java     ← 新增（约 200 行）
```

### 前端改造（按优先级）

```
ruoyi-ui/src/
├── utils/auth.js                      ← 修改（localStorage）
├── utils/request.js                    ← 修改（Result适配 + 动态baseURL）
├── views/login/index.vue               ← 修改（适配 NeoCC）
├── api/login.js                        ← 确认（路径可能改）
├── api/menu.js                         ← 确认（路径）
└── views/
    ├── system/user/index.vue           ← 新增
    ├── system/role/index.vue           ← 新增
    ├── system/department/index.vue     ← 新增
    ├── system/zone/index.vue           ← 新增
    ├── system/dict/index.vue           ← 新增
    ├── system/param/index.vue          ← 新增
    ├── sales/customer/index.vue       ← 新增
    ├── sales/contract/index.vue        ← 新增
    ├── sales/worklog/index.vue         ← 新增
    ├── sales/contact/index.vue         ← 新增
    ├── sales/transfer/index.vue        ← 新增
    ├── finance/bank/index.vue          ← 新增
    ├── finance/product/index.vue       ← 新增
    ├── finance/loan-audit/index.vue   ← 新增
    ├── finance/commission/index.vue    ← 新增
    └── finance/service-fee/index.vue  ← 新增
```
