# doc06: Dashboard 加载慢问题修复

## 问题现象

登录成功后，浏览器只请求了 `getInfo` 和 `getRouters`，Dashboard 页面几乎不加载任何内容，加载极其缓慢。

## 根因分析

Dashboard 首页 `index.vue` 的 `loadStats()` 函数发起 4 个统计 API 请求：

| API 函数 | 请求路径 | 期望代理 |
|----------|----------|----------|
| `listCustomer` | `/api/customer/page` | → `/sales/api` |
| `listContract` | `/api/contract/page` | → `/sales/api` |
| `listLoanAudit` | `/api/loanAudit/page` | → `/finance/api` |
| `listRole` | `/api/sysRole/page` | → `/auth/api` |

**问题链路**：`getBaseURL()` 对 `/api/customer/page` 等路径返回 `/dev-api`，但 `/dev-api` 代理到 Gateway 后，Gateway 没有 `/api/**` 的路由，导致返回 500 "No static resource api/xxx/page"。

同时，stat API 返回的错误响应（HTML 页面而非 JSON）被 axios 拦截器按 500 处理，进一步阻塞页面渲染。

## 修复内容

### 1. vite.config.js — 添加微服务 API 代理

**文件**：`ruoyi-ui/vite.config.js`

添加了 `/sales/api`、`/finance/api`、`/system/api` 三个 Vite proxy 条目（不走 rewrite，保持完整路径）：

```javascript
// NeoCC 微服务 API 代理（不走 rewrite，保持完整路径）
// 请求 /sales/api/customer/page → Gateway /sales/** → StripPrefix=1 → /api/customer/page 到达 sales:8083
'/sales/api': {
  target: baseUrl,  // http://localhost:8086
  changeOrigin: true,
},
'/finance/api': {
  target: baseUrl,
  changeOrigin: true,
},
'/system/api': {
  target: baseUrl,
  changeOrigin: true,
},
```

**说明**：之前添加的 rewrite 规则（`p.replace(/^\/sales\/api/, '/sales')`）会把 `/sales/api/customer/page` 重写为 `/sales/customer/page`，再经 Gateway `StripPrefix=1` 后变成 `customer/page`（缺少 `/api` 前缀），导致 sales 服务 500 报错。去掉了 rewrite，保持完整路径。

### 2. request.js getBaseURL() — 添加 /api 前缀映射

**文件**：`ruoyi-ui/src/utils/request.js`

在 `getBaseURL()` 函数中添加了 stat API 路径的映射：

```javascript
// sales 模块（stat API 用 /api 前缀）
if (url.startsWith('/customer') || url.startsWith('/contract') ||
    url.startsWith('/contactRecord') || url.startsWith('/workLog') ||
    url.startsWith('/performanceRecord') || url.startsWith('/customerTransferLog') ||
    url.startsWith('/contractAttachment') ||
    url.startsWith('/api/customer') || url.startsWith('/api/contract')) {
  return '/sales/api'
}

// finance 模块（stat API 用 /api 前缀）
if (url.startsWith('/bank') || url.startsWith('/financeProduct') ||
    url.startsWith('/loanAudit') || url.startsWith('/loanAuditRecord') ||
    url.startsWith('/commission') || url.startsWith('/serviceFee') ||
    url.startsWith('/api/loanAudit') || url.startsWith('/api/commission')) {
  return '/finance/api'
}

// auth 模块（role/stat 等走 /auth/api 代理到 Gateway /api/sysRole/** 再路由到 auth:8085）
if (url.startsWith('/api/role')) return '/auth/api'
```

### 3. gateway application.yml — 添加 API 层路由

**文件**：`gateway/src/main/resources/application.yml`

添加了 4 个新的 API 路由，使 Gateway 能正确处理 `/api/**` 路径的请求：

```yaml
# /auth/api/** → auth:8085（路径重写：/auth/api/sysRole/page → /api/sysRole/page）
- id: auth-api-rewrite-route
  uri: http://localhost:8085
  predicates:
    - Path=/auth/api/**
  filters:
    - RewritePath=/auth/api/(?<remaining>.*), /$\{remaining}

# /api/sysUser/**、/api/sysRole/** 等 → auth:8085
- id: auth-api-route
  uri: http://localhost:8085
  predicates:
    - Path=/api/sysUser/**
    - Path=/api/sysRole/**
    - Path=/api/sysPermission/**
    - Path=/api/sysDept/**
    - Path=/api/sysZone/**
    - Path=/api/sysOperationLog/**
  filters:
    - StripPrefix=0

# /api/customer/**、/api/contract/** 等 → sales:8083
- id: sales-api-route
  uri: http://localhost:8083
  predicates:
    - Path=/api/customer/**
    - Path=/api/contract/**
    - Path=/api/contactRecord/**
    - Path=/api/workLog/**
  filters:
    - StripPrefix=0

# /api/loanAudit/**、/api/bank/** 等 → finance:8084
- id: finance-api-route
  uri: http://localhost:8084
  predicates:
    - Path=/api/loanAudit/**
    - Path=/api/bank/**
    - Path=/api/financeProduct/**
    - Path=/api/commission/**
    - Path=/api/serviceFee/**
  filters:
    - StripPrefix=0
```

**路由匹配优先级**：Gateway 按配置顺序从上到下匹配，更具体的路径（如 `/auth/api/**`）在前面。

## API 路由映射表

| 前端请求路径 | getBaseURL() | Vite Proxy | Gateway 路由 | 后端服务 |
|---|---|---|---|---|
| `/auth/login` | `''` | `/auth` → Gateway | `auth-route` `/auth/**` | auth:8085 |
| `/auth/getInfo` | `''` | `/auth` → Gateway | `auth-route` `/auth/**` | auth:8085 |
| `/auth/getRouters` | `''` | `/auth` → Gateway | `auth-route` `/auth/**` | auth:8085 |
| `/auth/api/sysRole/page` | `/auth/api` | `/auth/api` → Gateway | `auth-api-rewrite-route` → 重写为 `/api/sysRole/page` | auth:8085 |
| `/api/sysRole/page` | `/auth/api` | `/auth/api` → Gateway | `auth-api-route` `/api/sysRole/**` | auth:8085 |
| `/api/customer/page` | `/sales/api` | `/sales/api` → Gateway | `sales-api-route` `/api/customer/**` | sales:8083 |
| `/api/contract/page` | `/sales/api` | `/sales/api` → Gateway | `sales-api-route` `/api/contract/**` | sales:8083 |
| `/api/loanAudit/page` | `/finance/api` | `/finance/api` → Gateway | `finance-api-route` `/api/loanAudit/**` | finance:8084 |
| `/api/bank/page` | `/finance/api` | `/finance/api` → Gateway | `finance-api-route` `/api/bank/**` | finance:8084 |
| `/sales/**` | `/dev-api` | `/dev-api` → Gateway | `sales-route` `/sales/**` + StripPrefix=1 | sales:8083 |
| `/finance/**` | `/dev-api` | `/dev-api` → Gateway | `finance-route` `/finance/**` + StripPrefix=1 | finance:8084 |

## 完整请求链路示例

以 Dashboard 的 `listCustomer({ pageNum: 1, pageSize: 1 })` 为例：

```
浏览器
  ↓ axios 请求 /api/customer/page
request.js getBaseURL('/api/customer/page') → 返回 '/sales/api'
  ↓ axios config.baseURL = '/sales/api', url = '/api/customer/page'
Vite proxy '/sales/api' → target: http://localhost:8086
  ↓ 代理到 Gateway:8086/sales/api/customer/page?pageNum=1&pageSize=1
Gateway 'sales-api-route' 匹配 Path=/api/customer/**
  ↓ StripPrefix=0，转发完整路径
sales:8083 收到 /api/customer/page?pageNum=1&pageSize=1
  ↓ 响应 {"code":200,"data":{"total":0,...}}
```

## 自测验证命令

重启 Gateway 后执行：

```bash
# 1. 登录获取 token
TOKEN=$(curl -s -X POST http://localhost:3001/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

# 2. 验证客户统计 API
curl "http://localhost:3001/sales/api/customer/page?pageNum=1&pageSize=1" \
  -H "Authorization: Bearer $TOKEN"

# 3. 验证合同统计 API
curl "http://localhost:3001/sales/api/contract/page?pageNum=1&pageSize=1" \
  -H "Authorization: Bearer $TOKEN"

# 4. 验证贷款审核统计 API
curl "http://localhost:3001/finance/api/loanAudit/page?pageNum=1&pageSize=1" \
  -H "Authorization: Bearer $TOKEN"

# 5. 验证角色统计 API
curl "http://localhost:3001/auth/api/sysRole/page?pageNum=1&pageSize=1" \
  -H "Authorization: Bearer $TOKEN"
```

预期：全部返回 `{"code":200,"message":"success","data":{"total":...}}`

## 注意事项

1. **Gateway 必须重启**：application.yml 修改后需要重启 Gateway 服务才能生效
2. **auth-api-rewrite-route 必须放在 auth-route 之前**：否则 `/auth/api/**` 会被 `/auth/**` 先匹配到（不带 StripPrefix），导致路径重写失效
3. **Vite proxy 不要加 rewrite**：之前错误地 rewrite `/sales/api` → `/sales`，导致路径缺失 `/api` 前缀
