# 后端完成度评估报告

**角色：** 后端架构分析师
**评审日期：** 2026-04-14
**评审范围：** NeoCC 全部后端模块（auth / system / sales / finance / gateway / common）
**评审目的：** 摸清当前后端实际完成状态，为前端接入提供准确的基准参考

---

## 一、总体完成度

| 模块 | API 完整度 | 业务逻辑完整度 | 基础设施就绪 | 可接入程度 |
|------|-----------|--------------|-------------|----------|
| auth | 95% | 90% | ✅ | **可接入** |
| system | 90% | 85% | ✅ | **可接入** |
| sales | 85% | 80% | ✅ | **可接入（部分逻辑待完善）** |
| finance | 75% | 65% | ✅ | **核心流程可接入，发放逻辑待完善** |
| gateway | 70% | 70% | ⚠️ | **需 Nacos 运行** |
| common | — | — | ✅ | 支撑模块 |

**综合结论：** 后端基础设施完整，核心业务流程（认证、合同签署、贷款审核状态机、业绩创建、跨服务通信）均已落地。可以开始前端集成开发，但需注意以下几点限制。

---

## 二、模块详细评估

### 2.1 auth 模块

**服务名：** `dafuweng-auth`，默认端口 8081（需确认）
**Spring Security：** ✅ JWT 无状态认证，BCrypt 密码加密

#### 已实现 API（`/api/sysUser`、`/api/sysRole`、`/api/sysPermission`）

| 端点 | 方法 | 状态 | 说明 |
|------|------|------|------|
| `POST /api/sysUser/login` | 登录 | ✅ | 返回 SysUserEntity（含 token 字段） |
| `POST /api/sysUser/logout` | 登出 | ✅ | 清理登录状态 |
| `GET /api/sysUser/{id}` | 查用户 | ✅ | |
| `GET /api/sysUser/page` | 分页 | ✅ | |
| `GET /api/sysUser/{id}/roles` | 用户角色 | ✅ | 返回 roleId 列表 |
| `GET /api/sysUser/{id}/permCodes` | 用户权限码 | ✅ | 返回权限码字符串列表 |
| `POST /api/sysUser` | 新增用户 | ✅ | |
| `PUT /api/sysUser` | 更新用户 | ✅ | |
| `PUT /api/sysUser/{id}/roles` | 分配角色 | ✅ | |
| `PUT /api/sysUser/{id}/unlock` | 解锁 | ✅ | |
| `PUT /api/sysUser/{id}/password` | 修改密码 | ✅ | |
| `DELETE /api/sysUser/{id}` | 删除 | ✅ | |
| `POST /api/sysUser/dev/reset-password` | 重置密码（调试） | ⚠️ | 生产上线前需删除 |
| `GET /api/sysRole/{id}` | 查角色 | ✅ | |
| `GET /api/sysRole/page` | 分页 | ✅ | |
| `GET /api/sysRole/listByStatus` | 按状态列出 | ✅ | |
| `GET /api/sysRole/{id}/permissions` | 角色权限 | ✅ | |
| `POST /api/sysRole` | 新增角色 | ✅ | |
| `PUT /api/sysRole` | 更新角色 | ✅ | |
| `PUT /api/sysRole/{id}/permissions` | 分配权限 | ✅ | |
| `DELETE /api/sysRole/{id}` | 删除 | ✅ | |
| `GET /api/sysPermission/{id}` | 查权限 | ✅ | |
| `GET /api/sysPermission/page` | 分页 | ✅ | |
| `GET /api/sysPermission/tree` | 权限树 | ✅ | 前端菜单渲染用 |
| `POST /api/sysPermission` | 新增 | ✅ | |
| `PUT /api/sysPermission` | 更新 | ✅ | |
| `DELETE /api/sysPermission/{id}` | 删除 | ✅ | |

**认证机制说明（前端对接要点）：**
- 登录后在 `Authorization: Bearer <token>` 请求头携带 token
- token 内容 = userId 字符串（内部实现约定，非 JWT 标准格式）
- 未认证请求返回 403，前端需统一拦截处理
- `/api/sysUser/login` 和 `/api/sysUser/page` 已配置为 permitAll，无需 token

**已知问题：**
- `dev/reset-password` 调试接口暴露在外，上线前必须移除或加 profile 保护

---

### 2.2 system 模块

**服务名：** `dafuweng-system`，依赖 Nacos + Redis
**Redis 缓存：** ✅ 已接入（`@Cacheable` 缓存参数和字典），无 TTL（默认永不过期）

| 端点 | 方法 | 状态 | 说明 |
|------|------|------|------|
| `GET /api/sysParam/{paramKey}` | 查参数值 | ✅ | Redis 缓存加速 |
| `POST /api/sysParam` | 新增参数 | ✅ | |
| `PUT /api/sysParam` | 更新参数 | ✅ | 同时 evict 缓存 |
| `DELETE /api/sysParam/{id}` | 删除 | ✅ | allEntries=true |
| `GET /api/sysDict/listByDictType/{dictType}` | 查字典 | ✅ | Redis 缓存 |
| `POST /api/sysDict` | 新增字典 | ✅ | |
| `PUT /api/sysDict` | 更新字典 | ✅ | |
| `DELETE /api/sysDict/{id}` | 删除 | ✅ | |
| `GET /api/sysDept/{id}` | 查部门 | ✅ | |
| `GET /api/sysDept/tree` | 部门树 | ✅ | 前端组织架构用 |
| `GET /api/sysDept/page` | 分页 | ✅ | |
| `POST /api/sysDept` | 新增 | ✅ | |
| `PUT /api/sysDept` | 更新 | ✅ | |
| `DELETE /api/sysDept/{id}` | 删除 | ✅ | |
| `GET /api/sysZone/{id}` | 查区域 | ✅ | |
| `GET /api/sysZone/page` | 分页 | ✅ | |
| `GET /api/sysZone/tree` | 区域树 | ✅ | |
| `POST /api/sysZone` | 新增 | ✅ | |
| `PUT /api/sysZone` | 更新 | ✅ | |
| `DELETE /api/sysZone/{id}` | 删除 | ✅ | |
| `GET /api/sysOperationLog/page` | 操作日志分页 | ✅ | |

**前端对接要点：**
- 字典数据建议前端缓存（后端 Redis 无 TTL，但 evict 逻辑完整，数据一致性有保障）
- 部门树和区域树可直接用于选择器组件

**已知问题：**
- Redis 无密码配置（`spring.redis.password` 为空），生产环境需补充
- 缓存无 TTL，若 `@CacheEvict` 调用异常可能残留脏数据

---

### 2.3 sales 模块

**服务名：** `dafuweng-sales`，含 `@EnableScheduling`（公海扫描任务）

#### 客户管理（`/api/customer`）

| 端点 | 方法 | 状态 | 说明 |
|------|------|------|------|
| `GET /api/customer/{id}` | 查客户 | ✅ | |
| `GET /api/customer/page` | 分页 | ✅ | |
| `GET /api/customer/listBySalesRepId/{id}` | 按销售查 | ✅ | |
| `GET /api/customer/listByStatus` | 按状态查 | ✅ | |
| `POST /api/customer` | 新增 | ✅ | |
| `PUT /api/customer` | 更新 | ✅ | |
| `DELETE /api/customer/{id}` | 删除 | ✅ | |

**备注：** 公海自动转移逻辑已实现（`PublicSeaTask`，每天凌晨2点，读 `customer.public_sea_days` 参数，默认30天）。前端无需特殊处理，后台静默执行。

#### 合同管理（`/api/contract`）

| 端点 | 方法 | 状态 | 说明 |
|------|------|------|------|
| `GET /api/contract/{id}` | 查合同 | ✅ | |
| `GET /api/contract/getByContractNo/{no}` | 按合同号查 | ✅ | |
| `GET /api/contract/page` | 分页 | ✅ | |
| `GET /api/contract/listBySalesRepId/{id}` | 按销售查 | ✅ | |
| `GET /api/contract/listByStatus` | 按状态查 | ✅ | |
| `POST /api/contract` | 新增 | ✅ | |
| `PUT /api/contract` | 更新 | ✅ | |
| `DELETE /api/contract/{id}` | 删除 | ✅ | |
| `POST /api/contract/{id}/sign` | **签署合同** | ✅ | 发送 MQ 事件，触发金融部审核流程 |

**sign() 注意事项：**
- 调用前合同 status 必须为 1（已创建）
- 签署成功后 status→2，同时发送 `ContractSignedEvent` 至 RabbitMQ
- 并发幂等保护**未实现**（高并发场景同一合同可能签署两次）

#### 业绩与其他销售数据

| 端点前缀 | 状态 | 说明 |
|---------|------|------|
| `/api/performanceRecord` | ✅ | CRUD，getByContractId |
| `/api/contactRecord` | ✅ | 客户跟进记录 CRUD |
| `/api/contractAttachment` | ✅ | 合同附件 CRUD |
| `/api/workLog` | ✅ | 工作日志 CRUD |
| `/api/customerTransferLog` | ✅ | 客户转让记录 CRUD |

#### 内部服务接口（`/sales/internal`，供 finance 调用）

| 端点 | 方法 | 状态 | 说明 |
|------|------|------|------|
| `POST /sales/internal/performances/create` | 创建业绩 | ✅ | 含幂等保护（contract_id 唯一索引）|
| `GET /sales/internal/contracts/{id}` | 查合同详情 | ✅ | 返回 ContractVO（含 zoneId）|
| `PUT /sales/internal/contracts/{id}/status` | 更新合同状态 | ✅ | |
| `PUT /sales/internal/contracts/{id}/service-fee-paid` | 更新服务费支付状态 | ✅ | |

**前端说明：** `/sales/internal` 前缀的接口为服务间内部调用，**前端不应直接调用**。

---

### 2.4 finance 模块

**服务名：** `dafuweng-finance`，依赖 RabbitMQ + OpenFeign 调用 sales

#### 贷款审核（`/api/loanAudit`）—— 核心流程

**状态机：**
```
1(待接收) →[系统自动/receive]→ 2(初审中)
         →[review]→ 3(已提交银行)
         →[submitBank]→ 4(银行审核中)
         →[bankResult approved=true]→ 6(终审通过待审)
         →[bankResult approved=false]→ 5(银行拒绝)
         →[approve]→ 终态（放款完成）
         →[reject]→ 7(终审拒绝)
```

| 端点 | 方法 | 状态 | 触发条件 |
|------|------|------|---------|
| `GET /api/loanAudit/{id}` | 查记录 | ✅ | |
| `GET /api/loanAudit/getByContractId/{id}` | 按合同查 | ✅ | |
| `GET /api/loanAudit/page` | 分页 | ✅ | |
| `GET /api/loanAudit/listByFinanceSpecialistId/{id}` | 按金融专员查 | ✅ | |
| `POST /api/loanAudit/{id}/receive` | 接收 | ✅ | 需 status=1，系统自动已触发 |
| `POST /api/loanAudit/{id}/review` | 初审 | ✅ | 需 status=2 |
| `POST /api/loanAudit/{id}/submit-bank` | 提交银行 | ✅ | 需 status=3 |
| `POST /api/loanAudit/{id}/bank-result` | 银行反馈 | ✅ | 需 status=4，approved 字段 |
| `POST /api/loanAudit/{id}/approve` | 终审放款 | ✅ | 需 status=6，触发业绩创建 |
| `POST /api/loanAudit/{id}/reject` | 拒绝 | ✅ | 需 status∈{4,5,7} |

**approve() 触发副作用（前端需知）：**
1. 计算提成金额（`contractAmount × commissionRate`）并调用 `salesFeignClient.createPerformance()`
2. 调用 `salesFeignClient.updateContractStatus(id, 7)` 将合同状态改为已放款
3. 写入 `loanAuditRecord` 操作轨迹

#### 提成记录（`/api/commissionRecord`）

| 端点 | 方法 | 状态 | 说明 |
|------|------|------|------|
| `GET /api/commissionRecord/{id}` | 查 | ✅ | |
| `GET /api/commissionRecord/page` | 分页 | ✅ | |
| `GET /api/commissionRecord/listBySalesRepId/{id}` | 按销售查 | ✅ | |
| `POST /api/commissionRecord/{id}/confirm` | 确认提成 | ✅ | |
| `POST /api/commissionRecord/{id}/grant` | **发放提成** | ⚠️ | 框架存在，具体转账逻辑未实现 |

#### 服务费记录（`/api/serviceFeeRecord`）

| 端点 | 方法 | 状态 | 说明 |
|------|------|------|------|
| `GET /api/serviceFeeRecord/{id}` | 查 | ✅ | |
| `GET /api/serviceFeeRecord/page` | 分页 | ✅ | |
| `GET /api/serviceFeeRecord/listByContractId/{id}` | 按合同查 | ✅ | |
| `PUT /api/serviceFeeRecord/{id}/pay` | **确认收款** | ⚠️ | 框架存在，具体支付流程未实现 |

#### 其他金融数据

| 端点前缀 | 状态 | 说明 |
|---------|------|------|
| `/api/bank` | ✅ | 银行 CRUD |
| `/api/financeProduct` | ✅ | 金融产品 CRUD（含 commissionRate 字段，approve 时使用）|
| `/api/loanAuditRecord` | ✅ | 审核操作轨迹 CRUD（只读查询为主）|

---

### 2.5 gateway 模块

**服务名：** `dafuweng-gateway`，Spring Cloud Gateway
**功能：** 请求路由、Token 验证（调用 auth Feign）、黑名单检查

**状态：**
- 路由规则已配置（需确认具体 application.yml）
- AuthFeignClient 已实现，用于 token 校验
- 依赖 Nacos 服务发现，**Nacos 未运行时无法启动**（已知环境问题）

---

## 三、跨模块通信现状

### 3.1 RabbitMQ 事件流

| 事件 | 生产者 | 消费者 | 状态 |
|------|--------|--------|------|
| `ContractSignedEvent` (contract.signed) | sales.ContractSignServiceImpl | finance.ContractSignedListener | ✅ 完整 |
| `LoanApprovedEvent` (loan.approved) | 配置已存在 | **无消费者** | ⚠️ 队列声明但未监听 |

**说明：** `LoanApprovedEvent` 的 MQ 交换机和队列已在 `MqConfig.java` 中声明，但 finance 模块目前在 `approve()` 中直接通过 OpenFeign 同步调用 sales（`createPerformance` + `updateContractStatus`），没有发送 MQ 事件。`LoanApprovedEvent` 是为未来异步解耦预留的扩展点，当前未启用。

### 3.2 OpenFeign 调用链

```
finance → sales (SalesFeignClient)
    ├── POST /sales/internal/performances/create
    ├── PUT  /sales/internal/contracts/{id}/status
    ├── GET  /sales/internal/contracts/{id}
    └── PUT  /sales/internal/contracts/{id}/service-fee-paid

gateway → auth (AuthFeignClient)
    └── 用于 Token 验证

sales → auth (AuthFeignClient)
    └── 用于权限校验

sales → system (SystemFeignClient)
    └── GET /sys/internal/param/{key}  (读取系统参数，如公海天数)
```

---

## 四、基础设施完成状态

### T01–T14 落地清单（承接 Check11.md 审查结论）

| 任务 | 描述 | 状态 | 备注 |
|------|------|------|------|
| T01 | Spring Security JWT 认证 | ✅ | |
| T02 | BCrypt 密码验证 | ✅ | |
| T03 | 贷款审核状态机 | ✅ | receive/review/submitBank/bankResult/approve/reject |
| T04 | RabbitMQ 配置 | ✅ | 两个交换机+两个队列+绑定 |
| T05 | 合同签署 MQ 事件 | ✅ | 并发幂等待补充 |
| T06 | finance 接收合同签署事件 | ✅ | 含幂等保护 |
| T07 | 提成发放 | ⚠️ | grant() 框架存在，具体转账逻辑未实现 |
| T08 | 服务费确认收款 | ⚠️ | confirmPay() 框架存在，具体支付逻辑未实现 |
| T09 | 业绩 OpenFeign 回调 | ✅ | approve() 时自动触发，含 zoneId、commissionAmount |
| T10 | 公海定时扫描 | ✅ | 每天凌晨2点，读参数配置 |
| T11 | 操作日志 AOP | ✅ | 异步写入，getCurrentUsername() 用 header（待统一）|
| T12 | Redis 缓存 | ✅ | 无 TTL 配置 |
| T13 | 数据权限拦截器 | ⚠️ | 基础设施完整，**DAO XML 无实际使用** |
| T14 | MyBatis-Plus 自动填充 | ✅ | insertFill/updateFill，反射获取 userId |

---

## 五、前端接入准备清单

### 5.1 可立即接入（无阻断问题）

- 用户/角色/权限管理（auth 模块全部 CRUD）
- 部门/区域/字典/参数管理（system 模块）
- 客户管理（sales）
- 合同 CRUD（sales）
- 合同签署（`POST /api/contract/{id}/sign`）
- 贷款审核全流程（finance，状态机完整）
- 提成记录查询和确认（finance，发放逻辑待后续完善）
- 服务费记录查询（finance，confirmPay 框架可用）

### 5.2 需要运行时基础设施

| 基础设施 | 用于 | 当前状态 |
|---------|------|---------|
| Nacos (8848) | 所有模块服务注册/发现 | **必须先启动** |
| RabbitMQ | 合同签署→金融审核异步事件 | **必须先启动** |
| Redis (6379) | system 模块字典/参数缓存 | 可选（降级为无缓存） |
| MySQL | 全部 | **必须先启动** |

### 5.3 已知前端对接注意事项

1. **Token 格式：** `Authorization: Bearer {userId字符串}`（非标准 JWT，内部约定）
2. **未认证返回：** HTTP 403（由 Spring Security 返回），前端统一跳转登录页
3. **合同签署并发：** 高并发场景需前端防重复提交（后端无幂等保护）
4. **贷款审核状态说明：**
   - 1 = 待接收（系统创建时）
   - 2 = 初审中
   - 3 = 已提交银行
   - 4 = 银行审核中
   - 5 = 银行拒绝（可再次提交或拒绝）
   - 6 = 终审通过待放款
   - 7 = 终审拒绝（终态）
   - 放款完成 = approve() 调用成功后，合同状态→7（已放款），不在 audit_status 里单独标记
5. **提成发放 `grant()` 和服务费 `confirmPay()`：** 接口可调用，但实际转账/支付操作为空壳，功能上已阻断

### 5.4 调试接口（上线前需清理）

- `POST /api/sysUser/dev/reset-password`：需在生产上线前删除或通过 `@Profile("dev")` 保护

---

## 六、剩余工作优先级

### P1（影响主流程）

| 项目 | 影响 | 建议行动 |
|------|------|---------|
| T13 DataScope 接入 DAO | 数据权限完全未生效，所有用户可见全量数据 | 在 CustomerDao.xml 等关键查询添加 `${_dataScope.toSqlCondition("t")}` |
| T07 grant() 具体逻辑 | 提成发放功能不可用 | 按 implementDetails.md 补充转账/确认流程 |
| T08 confirmPay() 具体逻辑 | 服务费收款功能不可用 | 同上 |

### P2（影响质量）

| 项目 | 影响 | 建议行动 |
|------|------|---------|
| Redis TTL | 缓存永不过期，evict 失败时脏数据 | 添加 `spring.cache.redis.time-to-live=3600000` |
| T11 getCurrentUsername() 不一致 | 与 T13/T14 风格不一致 | 统一用 SecurityContextHolder |
| T05 sign() 并发幂等 | 高并发下重复签署 | 加 SELECT FOR UPDATE |

### P3（完善项）

| 项目 | 建议 |
|------|------|
| T09-T14 单元测试 | 补充 6 个测试类，覆盖核心逻辑 |
| dev/reset-password 接口 | 加 `@Profile("dev")` 保护或删除 |
| Redis password 配置 | 生产环境必须设置 |
| LoanApprovedEvent 监听者 | 若需异步解耦，补充 MQ 消费者 |

---

## 七、模块端口参考

> 以下端口为推测值，以各模块 `application.yml` 实际配置为准。

| 模块 | 服务名 | 推测端口 |
|------|--------|---------|
| gateway | dafuweng-gateway | 8080 |
| auth | dafuweng-auth | 8081 |
| finance | dafuweng-finance | 8082 |
| sales | dafuweng-sales | 8083 |
| system | dafuweng-system | 8084 |
| Nacos | — | 8848 |
| RabbitMQ | — | 5672 / 15672 |
| Redis | — | 6379 |
| MySQL | — | 3306 |

---

**结论：** 后端主体功能可交付前端集成。优先保障 Nacos + RabbitMQ + Redis + MySQL 环境就绪，T07/T08 发放逻辑和 T13 数据权限接入为上线前必须完成的剩余工作。其余问题不阻断集成开发，可与前端并行推进。
