# NeoCC Plan06 实施质量评审报告 — Check05

**评审时间：** 2026-04-04
**评审人：** 资深后端架构审查员
**评估对象：** `scripts/plan-eng-review/Plan06.md` 方案 vs 实际代码
**评估依据：** Plan06.md 方案文档、实际代码文件

---

## 一、总体评分

| 维度 | 得分 | 说明 |
|------|------|------|
| **finance → sales 核心闭环** | 9/10 | InternalSalesController + Feign 调用链路完整，approve() 符合设计 |
| **gateway → auth Feign 客户端** | 3/10 | 路径设计与实际 auth 路由不兼容，AuthFilter 登录路径检查逻辑有严重缺陷 |
| **sales → auth Feign 客户端** | 10/10 | 路径正确，实现完整 |
| **sales → system Feign 客户端** | 10/10 | 路径正确，实现完整 |
| **编译验证** | 10/10 | 全项目 mvn compile 一次通过，无警告无错误 |
| **方案忠实度** | 7/10 | 核心完整落地；但 gateway 侧实现与 Plan06 存在偏差；Plan06 自身部分路径设计有误 |

**综合评分：7.5 / 10**

**评级：B（良好，但存在需立即修复的严重缺陷）**

---

## 二、逐项验收

### 2.1 核心：finance → sales Feign 闭环（Plan06 第二章）

#### 2.1.1 sales 内部接口

| Plan06 要求 | 实际代码 | 状态 |
|-------------|---------|------|
| POST `/sales/internal/performances/create` | `InternalSalesController.createPerformance()` ✅ | ✅ |
| GET `/sales/internal/contracts/{id}` | `InternalSalesController.getContract()` ✅ | ✅ |
| PUT `/sales/internal/contracts/{id}/status` | `InternalSalesController.updateContractStatus()` ✅ | ✅ |
| `getByContractId()` 在 Service 接口 | `PerformanceRecordService.getByContractId()` ✅ | ✅ |
| `getByContractId()` 实现 | `PerformanceRecordServiceImpl.getByContractId()` ✅（含 `@Wrapper` + deleted=0） | ✅ |
| `PerformanceRecordDao.selectOne(Wrapper)` | `PerformanceRecordDao.selectOne()` ✅ | ✅ |
| XML `selectOne` | `PerformanceRecordDao.xml` 有 `selectOne` ✅ | ✅ |
| 幂等保护逻辑 | 有（DuplicateKeyException 捕获）✅ | ✅ |

#### 2.1.2 LoanAuditServiceImpl Feign 调用

| Plan06 要求 | 实际代码 | 状态 |
|-------------|---------|------|
| 注入 `SalesFeignClient` | `@Autowired private SalesFeignClient salesFeignClient` ✅ | ✅ |
| `approve()` 方法声明在接口 | `LoanAuditService.approve()` ✅ | ✅ |
| `approve()` 实现（含 `@Override`） | `LoanAuditServiceImpl.approve()` ✅ | ✅ |
| 更新 loan_audit 状态 | `loanAudit.setAuditStatus((short) 5)` ✅ | ✅ |
| 调用 `updateContractStatus(id, 7)` | `salesFeignClient.updateContractStatus(..., (short) 7)` ✅ | ✅ |
| 调用 `getContract()` 获取合同信息 | `salesFeignClient.getContract(...)` ✅ | ✅ |
| 调用 `createPerformance(perfDto)` | `salesFeignClient.createPerformance(...)` ✅ | ✅ |
| 错误处理（null/非200检查） | 有 ✅ | ✅ |
| zoneId=null, commissionRate=0 | 符合 Plan06 注脚 ✅ | ✅ |

**注：** Plan06 第 2.2.3 节提到"在其他审核状态流转处（发送金融部、银行反馈）触发 `updateContractStatus`"，但未提供具体方法签名和调用入口。实现仅在 `approve()` 中有 `updateContractStatus(7)`，发送金融部（status=4）和银行反馈（status=5）的触发本次未实现。评估：不是缺陷，是 Plan06 该部分描述不完整。

**finance → sales 核心闭环评分：9/10**
扣分原因：审核状态流转触发（2.2.3）不完整，但 Plan06 自身对该部分的接口签名描述也不完整，不算执行偏差。

---

### 2.2 gateway → auth（Plan06 第三章）

#### 2.2.1 gateway AuthFeignClient

| 检查项 | Plan06 要求 | 实际情况 | 状态 |
|--------|-----------|---------|------|
| `@FeignClient(name="dafuweng-auth")` | 是 | 是 ✅ | ✅ |
| `@GetMapping("/auth/api/sysUser/{id}")` | Plan06 写的是 `/auth/api/sysUser/{id}` | 实际代码：`/auth/api/sysUser/{id}` ✅ | ⚠️ 见下方说明 |
| `@GetMapping("/auth/api/sysUser/{id}/roles")` | Plan06 写的是 `/auth/api/sysUser/{id}/roles` | 实际代码：`/auth/api/sysUser/{id}/roles` ✅ | ⚠️ 见下方说明 |
| `@GetMapping("/auth/api/sysUser/{id}/permCodes")` | Plan06 写的是 `/auth/api/sysUser/{id}/permCodes` | 实际代码：`/auth/api/sysUser/{id}/permCodes` ✅ | ⚠️ 见下方说明 |
| `getUserByUsername` | Plan06 有，代码没有 | 代码中无此方法 | ⚠️ Plan06 超范围设计（未在正文描述中要求） |

**⚠️ 严重问题：gateway AuthFeignClient 路径与 auth 实际接口不兼容**

auth 的 `SysUserController` 实际路径是：
- `@RequestMapping("/api/sysUser")` + `@GetMapping("/{id}")` → **实际路径：`/api/sysUser/{id}`**
- 不是 `/auth/api/sysUser/{id}`

gateway 的 Feign Client 绕过了 gateway 路由，直接通过 LoadBalancer 连接 auth 服务实例网络地址。因此 Feign 调用目标 URL 为：`http://dafuweng-auth/api/sysUser/{id}`（正确），而不是通过 gateway 路由。

但 gateway AuthFeignClient 写的是 `/auth/api/sysUser/{id}`，这意味着 Feign 会请求 `http://dafuweng-auth/auth/api/sysUser/{id}`——路径中多了 `/auth` 前缀，**auth 服务没有这个路径，会返回 404**。

**结论：gateway AuthFeignClient 的路径全部写错了，实际无法正常工作。**

**正确路径应为：`/api/sysUser/{id}`（与 sales AuthFeignClient 相同）**

Plan06 的这段设计本身有误，不仅是被执行歪了。

#### 2.2.2 gateway AuthFilter

| 检查项 | Plan06 要求 | 实际情况 | 状态 |
|--------|-----------|---------|------|
| 提取 `Authorization: Bearer <token>` | Plan06 描述 | 代码：`authHeader.substring(7)` ✅ | ✅ |
| token = userId 字符串解析 | Plan06 描述 | 代码：`Long.parseLong(token)` ✅ | ✅ |
| 调用 `authFeignClient.getUserById()` 验证 | Plan06 描述 | 代码中有 try/catch ✅ | ✅ |
| 401/503 错误码返回 | Plan06 描述 | 代码有 ✅ | ✅ |
| `X-User-Id` header 传递给下游 | Plan06 描述 | 代码有 ✅ | ✅ |
| `X-User-Roles` header 传递 | Plan06 描述（username 也提了） | 代码有 roles ✅ | ✅ |
| 登录路径跳过检查 | Plan06 描述 | **代码检查：`path.contains("/api/sysUser/login")`** | ❌ 严重缺陷 |

**❌ 严重缺陷：AuthFilter 登录路径检查逻辑错误**

gateway 路由配置：`/auth/** → localhost:8085 (StripPrefix=1)`

外部请求 `GET /auth/api/sysUser/login` 到达 AuthFilter 时，`exchange.getRequest().getURI().getPath()` 返回的是 **`/auth/api/sysUser/login`**（原始请求路径，gateway 路由变换发生在 filter 链之后）。

Filter 中检查：
```java
if (path.contains("/api/sysUser/login")) {
    return chain.filter(exchange);  // 跳过认证
}
```

`path = "/auth/api/sysUser/login"`，不包含 `"/api/sysUser/login"`，条件为 false，**登录请求不会被跳过，会执行完整的 token 验证逻辑**。但登录请求根本没有 token，会立刻返回 401。

实际上，登录请求应该跳过 AuthFilter 直接放行。正确检查应为：
```java
if (path.startsWith("/auth/api/sysUser/login")) { ... }
```

**结论：gateway AuthFilter 的登录路径检查在 gateway StripPrefix 架构下永远无法匹配，所有登录请求均会被拦截。**

---

### 2.3 sales → auth（Plan06 第四章）

| 检查项 | Plan06 要求 | 实际代码 | 状态 |
|--------|-----------|---------|------|
| `contextId = "authClientForSales"` | 是 | 是 ✅ | ✅ |
| `@GetMapping("/api/sysUser/{id}")` | Plan06 写 `/auth/api/...`，但正文说明矛盾 | 实际：`/api/sysUser/{id}` ✅ | ✅ |
| `@GetMapping("/api/sysUser/{id}/permCodes")` | Plan06 写 `/auth/api/...` | 实际：`/api/sysUser/{id}/permCodes` ✅ | ✅ |
| `SalesApplication @EnableFeignClients` | 是 | 是 ✅ | ✅ |

**注：** Plan06 正文中说"gateway 路由 StripPrefix=1 会去掉 `/auth` 前缀，Feign 路径应为 `/auth/api/sysUser`"，这个说法对 gateway 路由场景是正确的，但对 sales 直连 auth 的 Feign 场景不适用。sales FeignClient 使用 LoadBalancer 直连 auth，不经过 gateway 路由，所以应该用 `/api/sysUser/{id}`。**实际实现路径正确**，但 Plan06 描述有歧义误导。

---

### 2.4 sales → system（Plan06 第五章）

| 检查项 | Plan06 要求 | 实际代码 | 状态 |
|--------|-----------|---------|------|
| `contextId = "systemClientForSales"` | 是 | 是 ✅ | ✅ |
| `@GetMapping("/system/api/sysDepartment/{id}")` | Plan06 写 `/system/api/...` | 实际：`/api/sysDepartment/{id}` ✅ | ✅ |
| `@GetMapping("/system/api/sysZone/{id}")` | Plan06 写 `/system/api/...` | 实际：`/api/sysZone/{id}` ✅ | ✅ |

**注：** Plan06 第五章 SystemFeignClient 路径设计与第二章 finance SalesFeignClient 的路径逻辑完全一致——sales 服务在 gateway 中的路由前缀是 `/sales/**`，Feign 直连时路径应与 controller 路径一致。Plan06 对 SystemFeignClient 的路径描述（`/system/api/sysDepartment/{id}`）是错误的，实际实现（`/api/sysDepartment/{id}`）是正确的。

---

### 2.5 POM 依赖与 Application 注解

| 模块 | Plan06 要求 | 实际代码 | 状态 |
|------|-----------|---------|------|
| sales + openfeign | Step 1 要求添加 | `spring-cloud-starter-openfeign` ✅ | ✅ |
| sales + loadbalancer | Step 1 要求添加 | `spring-cloud-starter-loadbalancer` ✅（已存在于 pom，早于本次） | ✅ |
| sales @EnableFeignClients | Step 6 要求 | `@EnableFeignClients(basePackages = "com.dafuweng.sales.feign")` ✅ | ✅ |
| gateway + openfeign | Step 7 要求 | `spring-cloud-starter-openfeign` ✅ | ✅ |
| gateway @EnableFeignClients | Step 7 要求 | `@EnableFeignClients(basePackages = "com.dafuweng.gateway.feign")` ✅ | ✅ |

**注意：** gateway/pom.xml 中 `spring-boot-starter-web` 与 `spring-cloud-starter-gateway` 并存存在潜在技术冲突（gateway 是 reactive，starter-web 是 servlet）。Spring Cloud BOM 通常有冲突处理机制，但生产环境应验证是否有类加载警告。

---

## 三、问题汇总

### P0 — 立即修复（严重缺陷）

| # | 问题 | 位置 | 影响 |
|---|------|------|------|
| 1 | **gateway AuthFeignClient 路径错误** | `gateway/feign/AuthFeignClient.java` | 所有方法路径均多了 `/auth` 前缀，auth 服务无此路径，Feign 调用必返回 404 |
| 2 | **AuthFilter 登录路径检查失效** | `gateway/filter/AuthFilter.java:36` | `path.contains("/api/sysUser/login")` 在 gateway StripPrefix 架构下永远为 false，登录请求会被错误拦截返回 401 |
| 3 | **gateway spring-boot-starter-web 与 gateway 混用** | `gateway/pom.xml` | reactive/servlet 框架冲突风险，可能产生类加载异常 |

### P1 — 下个迭代修复

| # | 问题 | 位置 | 影响 |
|---|------|------|------|
| 4 | 审核状态流转触发不完整 | `LoanAuditServiceImpl` | 仅有终审（status=7）触发，status=4（发送金融部）/ status=5（银行反馈）无 Feign 调用 |
| 5 | `gateway AuthFeignClient.getUserByUsername()` 缺失 | `gateway/feign/AuthFeignClient.java` | Plan06 有设计但未实现，不过该方法非核心 |

### P2 — 改进建议

| # | 问题 | 位置 | 说明 |
|---|------|------|------|
| 6 | `AuthFilter` ObjectMapper 反序列化 roles | `gateway/filter/AuthFilter.java:74` | 用 Jackson 硬转换 Result<?> 的 data 字段，若 auth 返回结构变化会抛异常（被 catch 忽略） |
| 7 | `InternalSalesController` 未鉴权 | `sales/controller/InternalSalesController.java` | `/sales/internal/**` 面向内部但无 IP 白名单或 auth 验证，生产暴露风险 |
| 8 | gateway Filter 无全局顺序配置注释 | `gateway/filter/AuthFilter.java:98` | `@Order(-100)` 已设，但 gateway 全局 filter 顺序与 RouteToRequestUrlFilter 的关系需验证 |

---

## 四、综合评级

| 维度 | 得分 | 说明 |
|------|------|------|
| finance → sales 核心闭环 | 9/10 | 完整实现，符合 Plan06，approve() 逻辑正确 |
| gateway → auth | 3/10 | Feign 路径错误 + AuthFilter 登录路径检查失效，两个严重缺陷 |
| sales → auth | 10/10 | 路径正确，实现完整 |
| sales → system | 10/10 | 路径正确，实现完整 |
| 编译通过 | 10/10 | 全项目 BUILD SUCCESS |
| 方案忠实度 | 7/10 | 核心功能忠实；gateway 部分实现与 Plan06 均存在偏差（部分为 Plan06 自身设计错误） |

**综合评分：7.5 / 10**
**总体评级：B（良好，存在 3 个 P0 严重缺陷必须修复）**

---

## 五、修复优先级建议

### 立即修复（P0）

**1. gateway AuthFeignClient 路径修正：**
```java
// 错误（当前）
@GetMapping("/auth/api/sysUser/{id}")

// 正确（应改为）
@GetMapping("/api/sysUser/{id}")
```

**2. AuthFilter 登录路径检查修正：**
```java
// 错误（当前）
if (path.contains("/api/sysUser/login"))

// 正确（应改为）
if (path.startsWith("/auth/api/sysUser/login") || path.contains("/api/sysUser/login"))
```

**3. gateway pom.xml 移除 spring-boot-starter-web：**
gateway 是纯 reactive 应用，移除 `spring-boot-starter-web` 依赖以避免框架冲突。

### 本次不要求修复的后续工作

| 后续工作 | 说明 |
|---------|------|
| 审核状态流转（status=4/5）触发 | 需 finance 侧增加对应业务方法，Plan06 对此描述不完整 |
| InternalSalesController 鉴权 | 建议增加 IP 白名单或内部 token 验证机制 |
| gateway AuthFilter username 传递 | 非核心功能，可后续补充 |

---

## 六、Plan06 自身设计问题（不受执行影响，需关注）

| # | 问题 | Plan06 位置 | 说明 |
|---|------|------------|------|
| 1 | SystemFeignClient 路径歧义 | 第五章 5.1 | `/system/api/sysDepartment/{id}` 在 Feign 直连时不正确（应为 `/api/sysDepartment/{id}`），实际执行已自行修正 |
| 2 | gateway AuthFeignClient 路径设计错误 | 第三章 3.2 | `/auth/api/sysUser/{id}` 对 Feign 直连不正确，但正文注脚有矛盾说明 |
| 3 | 2.2.3 审核状态流转描述不完整 | 第2.2.3节 | 描述了要触发 `updateContractStatus(id, 4/5/7)`，但未给出 finance 侧调用入口和接口签名 |
| 4 | 第三章 AuthFilter 路径检查设计疏漏 | 第3.3节 | 未考虑 gateway StripPrefix 架构下 filter 接收的是原始路径而非路由后路径 |

---

## 七、文件变更总清单

| 模块 | 新建文件 | 修改文件 | 状态 |
|------|---------|---------|------|
| sales | `controller/InternalSalesController.java` | `service/PerformanceRecordService.java`（+getByContractId） | ✅ |
| sales | `feign/AuthFeignClient.java` | `service/impl/PerformanceRecordServiceImpl.java`（+getByContractId impl） | ✅ |
| sales | `feign/SystemFeignClient.java` | `dao/PerformanceRecordDao.java`（+selectOne） | ✅ |
| sales | — | `SalesApplication.java`（+@EnableFeignClients） | ✅ |
| sales | — | `pom.xml`（+spring-cloud-starter-openfeign） | ✅ |
| finance | — | `LoanAuditService.java`（+approve()） | ✅ |
| finance | — | `LoanAuditServiceImpl.java`（+SalesFeignClient + approve()） | ✅ |
| gateway | `feign/AuthFeignClient.java` | `GatewayApplication.java`（+@EnableFeignClients） | ⚠️ 路径需修正 |
| gateway | `filter/AuthFilter.java` | `pom.xml`（+spring-cloud-starter-openfeign） | ⚠️ 登录路径检查需修正 |
