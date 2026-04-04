# Plan07: 项目实施现状评估与开发规范
**版本：** v1.0
**日期：** 2026-04-04
**目标：** 1. 代码层面评估业务实施条件；2. 当前进度量化评估；3. 制定开发规范与流程

---

## 一、代码层面业务实施条件评估

### 1.1 各模块代码就绪状态

#### common 模块

| 组件 | 状态 | 说明 |
|------|------|------|
| Result.java | ✅ 完成 | 统一响应结构（code/message/data） |
| PageRequest.java | ✅ 完成 | 分页请求 |
| PageResponse.java | ✅ 完成 | 分页响应 |
| GlobalExceptionHandler.java | ✅ 完成 | 全局异常处理（含 NPE/DuplicateKey） |
| PerformanceCreateDTO.java | ✅ 完成 | Plan05 创建，finance→sales 调用 |
| ContractVO.java | ✅ 完成 | Plan05 创建，sales→finance 调用 |
| pom.xml | ⚠️ 有问题 | 无 parent，版本管理分散在 5 个业务模块 |

**评估：** common 模块公共组件就绪，但版本管理架构有问题（P0 级架构债务，暂不阻断开发）。

---

#### auth 模块（认证授权）

| 组件 | 状态 | 说明 |
|------|------|------|
| 5 个 Entity（sys_user/role/permission/user_role/role_permission） | ✅ 完成 | 均含 `@TableLogic` |
| 5 个 DAO + 5 个 Mapper XML | ✅ 完成 | 含 BaseMapper + 自定义 SQL |
| SysUserService + Impl | ✅ 完成 | 13 个方法，含登录锁定逻辑 |
| SysRoleService + Impl | ✅ 完成 | 8 个方法 |
| SysPermissionService + Impl | ✅ 完成 | 8 个方法 |
| SysUserController | ✅ 完成 | 12 个端点（含 getRoleIds/getPermCodes） |
| SysRoleController | ✅ 完成 | 8 个端点 |
| SysPermissionController | ✅ 完成 | 9 个端点 |
| AuthApplication | ✅ 完成 | 含 `@EnableFeignClients` |
| AuthService（登录/登出） | ⚠️ 缺陷 | 密码明文比对，非 BCrypt |
| ShiroConfig + ShiroRealm | ❌ 未实现 | 无 Shiro 认证体系 |
| @OperationLog AOP 切面 | ❌ 未实现 | 操作日志切面无代码 |

**代码层面业务实施条件：**

- **可工作：** 用户 CRUD、角色 CRUD、权限 CRUD、数据查询
- **不可工作：** 统一登录（无 Shiro，无 Token 机制）、权限校验（无 ShiroRealm）
- **安全风险：** 密码明文存储（高危）

**结论：基础 CRUD 业务可实施，但无统一认证，gateway 无法完成 Token 校验。**

---

#### sales 模块（销售核心）

| 组件 | 状态 | 说明 |
|------|------|------|
| 7 个 Entity + DAO + Mapper XML | ✅ 完成 | Customer/ContactRecord/Contract/Attachment/WorkLog/Performance/TransferLog |
| 7 个 Service + Impl | ✅ 完成 | 含分页、查重、CRUD |
| CustomerController | ✅ 完成 | 含 checkDuplicate 等额外端点 |
| ContactRecordController | ✅ 完成 | 6 个标准接口 |
| ContractController | ✅ 完成 | 6 个标准接口 |
| ContractAttachmentController | ✅ 完成 | 6 个标准接口 |
| WorkLogController | ✅ 完成 | 6 个标准接口 |
| PerformanceRecordController | ✅ 完成 | 6 个标准接口 |
| CustomerTransferLogController | ✅ 完成 | 6 个标准接口 |
| InternalSalesController | ✅ 完成 | Plan06 创建，3 个内部接口 |
| PublicSeaTask（公海定时任务） | ❌ 未实现 | implementDetails.md 要求 |
| RabbitMQ 事件发送 | ❌ 未实现 | 合同签署事件无法发送 |
| sales → system Feign | ⚠️ 有缺陷 | SystemFeignClient 路径正确但 zoneId 字段在 ContractEntity 缺失 |
| sales → auth Feign | ✅ 完成 | AuthFeignClient 正确 |
| MetaObjectHandler | ⚠️ 缺失 | createdBy/updatedBy 均传 null |

**代码层面业务实施条件：**

- **可工作：** 所有 CRUD、查询、分页、基础业务逻辑
- **不可工作：** 公海自动转移（无定时任务）、合同签署事件无法通知 finance（无 RabbitMQ）、业绩计算 zoneId 缺失
- **业务流程断点：** 合同签署 → 通知金融部（RabbitMQ 未实现）→ 贷款审核 → 业绩创建（OpenFeign 已通但 zoneId=null）

**结论：基础 CRUD 完善，业务流程部分可跑通，但关键跨模块事件驱动链路断路。**

---

#### finance 模块（金融核心）

| 组件 | 状态 | 说明 |
|------|------|------|
| 6 个 Entity + DAO + Mapper XML | ✅ 完成 | Bank/FinanceProduct/LoanAudit/LoanAuditRecord/ServiceFeeRecord/CommissionRecord |
| 6 个 Service + Impl | ✅ 完成 | 含分页、业务查询 |
| BankController | ✅ 完成 | 6 个标准接口 |
| FinanceProductController | ✅ 完成 | 6 个标准接口 |
| LoanAuditController | ✅ 完成 | 6 个标准接口 |
| LoanAuditRecordController | ✅ 完成 | 6 个标准接口 |
| ServiceFeeRecordController | ✅ 完成 | 6 个标准接口 |
| CommissionRecordController | ✅ 完成 | 6 个标准接口 |
| LoanAuditServiceImpl.approve() | ✅ 完成 | Plan06 实现，触发 OpenFeign 创建业绩 |
| SalesFeignClient | ✅ 完成 | 3 个方法 |
| CommissionRecordService.grant() | ❌ 未实现 | 提成发放逻辑缺失 |
| ServiceFeeRecordService.confirmPay() | ❌ 未实现 | 确认收款逻辑缺失 |
| LoanAuditController 审核方法 | ⚠️ 部分实现 | review/submit-bank/bank-result/approve/reject 均未实现业务逻辑 |
| RabbitMQ 事件消费 | ❌ 未实现 | 无法接收 sales 合同签署事件 |
| MetaObjectHandler | ⚠️ 缺失 | createdBy/updatedBy 均传 null |

**代码层面业务实施条件：**

- **可工作：** 所有 CRUD、查询、分页
- **不可工作：** 贷款审核流程（审核状态流转无代码）、提成发放、服务费确认收款、Rabb

MQ 消费合同签署事件

**结论：基础 CRUD 完善，但核心贷款审核流程（最关键业务）未实现。**

---

#### system 模块（系统管理）

| 组件 | 状态 | 说明 |
|------|------|------|
| 5 个 Entity + DAO + Mapper XML | ✅ 完成 | Zone/Department/Param/OperationLog/Dict |
| 5 个 Service + Impl | ✅ 完成 | 含树形结构（Department） |
| SysZoneController | ✅ 完成 | 6 个标准接口 |
| SysDepartmentController | ✅ 完成 | 含 listByParentId 等额外端点 |
| SysParamController | ✅ 完成 | 6 个标准接口 |
| SysOperationLogController | ✅ 完成 | 3 个接口（append-only） |
| SysDictController | ✅ 完成 | 6 个标准接口 |
| Redis 缓存 | ❌ 未实现 | Param/Dict 查询无缓存 |
| MetaObjectHandler | ⚠️ 缺失 | createdBy/updatedBy 均传 null |

**代码层面业务实施条件：**
- **可工作：** 所有 CRUD、树形查询、分页
- **不可工作：** 系统参数和数据字典查询无 Redis 缓存，性能有隐患

**结论：CRUD 完整，业务可实施，缺少缓存层（低优先级）。**

---

#### gateway 模块（API 网关）

| 组件 | 状态 | 说明 |
|------|------|------|
| GatewayApplication | ✅ 完成 | 含 `@EnableFeignClients` |
| pom.xml | ⚠️ 有问题 | 含 `spring-boot-starter-web`（与 gateway reactive 冲突） |
| AuthFilter | ⚠️ 有缺陷 | StripPrefix 架构下登录路径检查永远不匹配 |
| AuthFeignClient | ❌ 路径错误 | `/auth/api/sysUser/{id}` 应为 `/api/sysUser/{id}` |
| CorsConfig | ❌ 未实现 | 跨域配置缺失 |
| application.yml 路由 | ⚠️ 缺陷 | 使用硬编码 `http://localhost:808x`，非 `lb://` 负载均衡 |
| Nacos 集成 | ❌ 未完成 | application.yml 有 nacos 配置但无实际服务注册 |

**代码层面业务实施条件：**

- **不可工作：** 所有经过 gateway 的请求（Feign 路径错、AuthFilter 登录检查失效、路由硬编码）
- gateway 本应是统一入口，当前状态下行请求无法正常流转

**结论：gateway 模块存在 P0 级缺陷，无法正常路由，是项目联调最大障碍。**

---

### 1.2 总体评估

| 维度 | 状态 | 说明 |
|------|------|------|
| 代码编译 | ✅ 可编译 | 全项目 mvn compile 通过 |
| 模块独立运行 | ⚠️ 部分可 | sales/finance/system 可独立启动；auth 可启动但无认证；gateway 有缺陷 |
| 业务 CRUD | ✅ 基本完善 | 各模块 CRUD 完整 |
| 跨模块调用 | ⚠️ 部分可 | finance→sales OpenFeign 可用（Check05 发现 3 个 P0 缺陷需修复）；sales→auth/system 路径正确但未充分使用 |
| 事件驱动 | ❌ 断路 | RabbitMQ 交换机/队列/生产者/消费者均未实现 |
| 统一认证 | ❌ 断路 | 无 Shiro、无 JWT、gateway Token 校验有缺陷 |
| 数据权限 | ❌ 断路 | 无 DataScopeInterceptor |
| 定时任务 | ❌ 缺失 | PublicSeaTask 未实现 |
| 缓存层 | ❌ 缺失 | Redis 缓存未集成到 Param/Dict 查询 |

**总体结论：代码基础设施基本就绪，可实施基础 CRUD 业务。但核心业务流程（认证授权、事件驱动、数据权限、gateway 路由）存在断路，不具备完整业务闭环运行条件。**

---

## 二、当前进度评估

### 2.1 模块维度完成度

基于 `implementDetails.md` 规定的功能范围，量化评估：

| 模块 | 规定工作量 | 已完成量 | 完成度 | 说明 |
|------|-----------|---------|--------|------|
| common | 4 公共组件 | 4 | 100% | Result/PageRequest/PageResponse/GlobalExceptionHandler |
| auth | AuthService + Shiro + 12 Controller 接口 | 29 接口 + 登录（明文）+ 锁定逻辑 | 65% | Shiro 未实现，BCrypt 未实现，@OperationLog 未实现 |
| system | 5 Controller + 28 接口 + Redis 缓存 | 5 Controller + 28 接口，无缓存 | 85% | 缓存层未实现 |
| sales | 7 Controller + 业务逻辑（公海/合同签署/RabbitMQ/定时任务）| 7 Controller + 基本 CRUD，内部接口 | 50% | 业务逻辑缺失多（见上表） |
| finance | 6 Controller + 审核流程 + OpenFeign + RabbitMQ | 6 Controller + approve() + 基本 CRUD | 45% | 审核流程大部分缺失，RabbitMQ 未实现 |
| gateway | 路由 + AuthFilter + CorsConfig + Nacos | 路由（硬编码）+ AuthFilter（有缺陷）+ AuthFeignClient（路径错）| 30% | 3 个 P0 缺陷 |
| OpenFeign 跨服务 | finance→sales + sales→auth + sales→system + gateway→auth | finance→sales 核心通，sales→auth/system 基本通，gateway→auth 有缺陷 | 50% | Check05 发现 P0 缺陷 |

### 2.2 业务维度完成度

| 业务能力 | 对应 implementDetails.md 章节 | 完成度 |
|---------|--------------------------|--------|
| 客户管理（CRUD+公海） | 第五章 5.1 | 50%（无公海定时任务） |
| 洽谈记录 | 第五章 5.2 | 100% |
| 合同管理（签署/发送金融部） | 第五章 5.3 | 30%（无 RabbitMQ 事件） |
| 工作日志 | 第五章 5.4 | 100% |
| 业绩查询 | 第五章 5.5 | 30%（无统计/Rank） |
| 客户转移 | 第五章 5.6 | 100% |
| 贷款审核全流程 | 第六章 6.3 | 20%（仅 CRUD，审核流转无代码）|
| 服务费收取 | 第六章 6.4 | 30%（仅 CRUD，确认收款无代码）|
| 提成发放 | 第六章 6.5 | 20%（仅 CRUD，发放确认无代码）|
| 统一认证授权 | 第三章 | 20%（无 Shiro/JWT）|
| 系统参数+字典+Redis缓存 | 第四章 | 85%（无 Redis 缓存）|
| 操作日志 AOP | 第四章 4.6 | 0% |
| API 网关路由+鉴权 | 第七章 | 30%（有缺陷）|
| 跨服务 OpenFeign | 第八章 | 50%（有 P0 缺陷）|
| RabbitMQ 事件驱动 | 第八章 8.2 | 0% |
| 公海定时任务 | 第九章 9.1 | 0% |
| 数据权限拦截器 | 第九章 9.3 | 0% |

### 2.3 整体百分比估算

| 维度 | 权重 | 完成度 | 加权得分 |
|------|------|--------|---------|
| 基础设施（common/pom/编译） | 10% | 95% | 9.5 |
| auth 模块 | 10% | 65% | 6.5 |
| system 模块 | 10% | 85% | 8.5 |
| sales 模块 | 20% | 50% | 10.0 |
| finance 模块 | 20% | 45% | 9.0 |
| gateway 模块 | 10% | 30% | 3.0 |
| OpenFeign 跨服务 | 10% | 50% | 5.0 |
| RabbitMQ 事件驱动 | 5% | 0% | 0.0 |
| 数据权限/定时任务 | 5% | 0% | 0.0 |

**整体完成度：约 51.5%**

**说明：** 当前状态约为整个 implementDetails.md 规定功能的 **50%**。基础 CRUD 层基本完成，但核心业务逻辑（认证授权、审核流程、事件驱动、数据权限）大部分未实现或存在缺陷。

---

## 三、业务开发规范

### 3.1 模块分层规范

每模块必须遵循以下四层结构，禁止跨层直接调用（禁止 Controller 直接操作 DAO）：

```
Controller（接受 HTTP 请求，参数校验）
  ↓ 调用
Service（业务逻辑，事务边界）
  ↓ 调用
DAO/Mapper（数据访问，SQL）
```

**额外层次（按需）：**
- `feign/` — OpenFeign 客户端定义（跨模块调用）
- `config/` — 配置类（ShiroConfig、RedisConfig 等）
- `task/` — 定时任务
- `mq/` — RabbitMQ 消息生产/消费
- `filter/` — gateway 过滤器

### 3.2 代码组织规范

#### 包路径规范

| 类型 | 路径格式 |
|------|---------|
| Controller | `{module}/controller/{EntityName}Controller.java` |
| Service 接口 | `{module}/service/{EntityName}Service.java` |
| Service 实现 | `{module}/service/impl/{EntityName}ServiceImpl.java` |
| DAO | `{module}/dao/{EntityName}Dao.java` |
| Entity | `{module}/entity/{EntityName}Entity.java` |
| Mapper XML | `resources/{module}/mapper/{EntityName}Dao.xml` |
| FeignClient | `{module}/feign/{TargetService}FeignClient.java` |
| DTO（入参） | `common/entity/dto/{Action}DTO.java` |
| VO（出参） | `common/entity/vo/{Entity}VO.java` |

#### 类命名规范

| 类型 | 命名规则 | 示例 |
|------|---------|------|
| Controller | `{Entity}Controller` | `CustomerController` |
| Service | `{Entity}Service` | `CustomerService` |
| ServiceImpl | `{Entity}ServiceImpl` | `CustomerServiceImpl` |
| DAO | `{Entity}Dao` | `CustomerDao` |
| Entity | `{Entity}Entity` | `CustomerEntity` |
| FeignClient | `{Service}FeignClient` | `SalesFeignClient` |
| Internal Controller | `Internal{Module}Controller` | `InternalSalesController` |

### 3.3 接口规范

#### 路径规范

| 类型 | 路径格式 | 示例 |
|------|---------|------|
| 外部 API | `/api/{entity}` | `/api/customer` |
| 内部 API | `/sales/internal/{entity}` | `/sales/internal/contracts/{id}/status` |
| 分页查询 | `GET /api/{entity}/page` | `GET /api/customer/page?page=1&size=10` |
| 详情查询 | `GET /api/{entity}/{id}` | `GET /api/customer/1` |
| 新增 | `POST /api/{entity}` | `POST /api/customer` |
| 修改 | `PUT /api/{entity}` | `PUT /api/customer` |
| 删除 | `DELETE /api/{entity}/{id}` | `DELETE /api/customer/1` |

#### HTTP 方法规范

| 方法 | 用途 | 是否 idempotent |
|------|------|----------------|
| GET | 查询 | 是 |
| POST | 新增 | 否 |
| PUT | 全量修改 | 是 |
| DELETE | 删除 | 是 |

#### 响应格式规范

所有接口统一返回 `Result<T>`：

```java
// 成功
return Result.success(data);           // 200
return Result.success();                // 200，无数据

// 失败
return Result.error400("参数错误");     // 400
return Result.error401("未登录");       // 401
return Result.error403("无权限");       // 403
return Result.error("服务器异常");      // 500
```

### 3.4 Service 层规范

#### 事务规范

所有**写操作**必须标注 `@Transactional`：

```java
@Transactional
public CustomerEntity save(CustomerEntity entity) { ... }

@Transactional
public void delete(Long id) { ... }
```

读操作不加 `@Transactional`（允许事务只读优化）。

#### 业务逻辑规范

- 禁止在 Service 中直接拼接 SQL（使用 LambdaQueryWrapper）
- 禁止在 Service 中处理 HTTP 请求/响应（这是 Controller 的职责）
- 业务校验（如查重、状态流转检查）必须在 Service 层
- 跨模块调用（Feign）在 Service 层触发，不在 Controller 层

### 3.5 DAO 层规范

- 所有 DAO 继承 `BaseMapper<T>`
- 自定义 SQL 写在 Mapper XML 中，不写在 Java 注解里
- XML 文件路径：`resources/{module}/mapper/{DaoName}Dao.xml`
- 禁止使用 `SELECT *`，必须明确列出字段

### 3.6 跨模块调用规范（OpenFeign）

#### FeignClient 定义位置

| 调用方向 | FeignClient 放在 | 示例 |
|---------|-----------------|------|
| A → B | A 模块的 `feign/` 包 | finance → sales → `finance/feign/SalesFeignClient` |
| gateway → auth | gateway 的 `feign/` 包 | `gateway/feign/AuthFeignClient` |

#### 路径规范

**关键原则：** Feign 直连时路径 = 目标服务 Controller 的实际 `@RequestMapping` 路径，**不要**包含 gateway 路由前缀（如 `/auth`、`/sales`）。

| 调用场景 | Feign 路径 | 理由 |
|---------|-----------|------|
| finance → sales（内部接口） | `/sales/internal/...` | InternalSalesController 实际路径 |
| sales → auth | `/api/sysUser/{id}` | SysUserController 实际路径（**不带** `/auth` 前缀） |
| sales → system | `/api/sysDepartment/{id}` | SysDepartmentController 实际路径 |
| gateway → auth | `/api/sysUser/{id}` | **不要用** `/auth/api/...` |

#### 接口幂等性

所有跨模块写操作接口必须实现幂等：

```java
// sales 内部接口幂等示例
@PostMapping("/performances/create")
public Result<?> createPerformance(@RequestBody PerformanceCreateDTO dto) {
    // 1. 先查是否存在
    PerformanceRecordEntity existing = performanceRecordService.getByContractId(dto.getContractId());
    if (existing != null) {
        return Result.error(400, "该合同已创建业绩记录，请勿重复提交");
    }
    // 2. 尝试插入，捕获 DuplicateKeyException
    try {
        performanceRecordService.save(entity);
        return Result.success(entity);
    } catch (DuplicateKeyException e) {
        return Result.error(400, "幂等冲突");
    }
}
```

### 3.7 事件驱动规范（RabbitMQ）

#### 事件类定义位置

所有事件类放在 `common/mq/event/` 包下：

```java
// common/mq/event/ContractSignedEvent.java
@Data
public class ContractSignedEvent implements Serializable {
    private Long contractId;
    private Long customerId;
    private Long salesRepId;
    private BigDecimal contractAmount;
    private Date signDate;
}
```

#### 生产者/消费者规范

| 角色 | 代码位置 | 触发时机 |
|------|---------|---------|
| 生产者（发送事件） | 调用方模块的 Service | 业务操作成功后立即发送（如 signContract 签署后） |
| 消费者（接收事件） | 接收方模块的 Service | RabbitMQ Listener 自动触发 |

### 3.8 数据库规范

#### 逻辑删除

所有业务表必须：
1. Entity 字段标注 `@TableLogic`
2. Mapper XML 中**不要**手动拼接 `deleted` 条件（MyBatis Plus 自动处理）
3. DAO 继承 `BaseMapper<T>`，所有继承方法自动带 `deleted=0` 条件

#### 乐观锁

核心业务表（customer/contract/loan_audit）使用 `@Version` 注解，更新时自动 version+1：

```java
// Entity 中
@Version
private Integer version;
```

#### 字段类型

| 字段类型 | Java 类型 | 说明 |
|---------|---------|------|
| TINYINT | Short | 状态/标记字段（deleted/status/payment_status） |
| DATETIME | Date | 时间字段 |
| DECIMAL | BigDecimal | 金额/利率 |
| JSON | Object + @TableField(typeHandler = JacksonTypeHandler.class) | annotation/requirements/documents |

### 3.9 配置规范

#### application.yml 规范

每个模块必须有独立的 `application.yml`，关键配置项：

```yaml
server:
  port: {port}

spring:
  application:
    name: {module}
  datasource:
    url: jdbc:mysql://localhost:3306/dafuweng_{module}?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false
    username: root
    password: 123456
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848

mybatis-plus:
  mapper-locations: classpath:{module}/mapper/*.xml
  type-aliases-package: com.dafuweng.{module}.entity
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  type-handlers-package: com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler
```

---

## 四、后续开发流程

### 4.1 当前阶段定位

当前项目处于 **Phase 1.5**（CRUD 基础层基本就绪，核心业务逻辑大量缺失），建议按以下优先级推进。

### 4.2 优先级排序

#### P0（立即处理，否则后续所有工作无效）

| 优先级 | 任务 | 涉及模块 | 说明 |
|--------|------|---------|------|
| P0-1 | 修复 gateway AuthFeignClient 路径（`/auth/api/...` → `/api/...`） | gateway | Check05 发现，Feign 调用必 404 |
| P0-2 | 修复 AuthFilter 登录路径检查（`contains` → `startsWith`） | gateway | Check05 发现，登录请求全部被拦截 |
| P0-3 | gateway pom.xml 移除 spring-boot-starter-web | gateway | 与 reactive gateway 框架冲突 |
| P0-4 | gateway application.yml 路由改为 `lb://` 负载均衡 | gateway | 当前硬编码 localhost，绕过了注册中心 |

#### P1（核心业务流程必须打通）

| 优先级 | 任务 | 涉及模块 | 说明 |
|--------|------|---------|------|
| P1-1 | 完整实现 LoanAuditController 审核方法 | finance | review/submit-bank/bank-result/approve/reject 业务逻辑 |
| P1-2 | RabbitMQ 交换机/队列配置 + sales 合同签署事件发送 | sales | 合同签署后触发 finance 创建审核记录 |
| P1-3 | RabbitMQ 消费者：finance 接收合同签署事件 | finance | 自动创建 loan_audit 记录 |
| P1-4 | 修复 Check05 发现的 gateway Feign 路径问题 | gateway | P0-1 已列 |
| P1-5 | 补充 CommissionRecordService.grant() + ServiceFeeRecordService.confirmPay() | finance | 提成发放和服务费确认逻辑 |
| P1-6 | Auth 模块 Shiro 集成（或 JWT 方案决策） | auth | BCrypt + ShiroRealm + Token 机制 |
| P1-7 | 数据权限拦截器 DataScopeInterceptor | common | 实现 implementDetails.md 第 9.3 节 |

#### P2（系统健壮性增强）

| 优先级 | 任务 | 涉及模块 | 说明 |
|--------|------|---------|------|
| P2-1 | PublicSeaTask 公海自动扫描定时任务 | sales | implementDetails.md 第 9.1 节 |
| P2-2 | Redis 缓存集成（SysParam/SysDict） | system | implementDetails.md 第 9.5 节 |
| P2-3 | 操作日志 @OperationLog AOP 切面 | system | implementDetails.md 第 4.6 节 |
| P2-4 | SalesFeignClient ProductFeignClient（查 commission_rate） | sales/finance | ZoneId 和 commissionRate 当前传 0，需后续完善 |
| P2-5 | MetaObjectHandler 自动填充 createdBy/updatedBy | 所有模块 | 依赖 auth 模块 Shiro Subject |

#### P3（优化项，可后期处理）

| 优先级 | 任务 | 说明 |
|--------|------|------|
| P3-1 | 业绩统计排名（PerformanceRecordController rank 接口） | sales 业绩维度 |
| P3-2 | Nacos 配置中心化 | 所有模块 application.yml 迁移到 Nacos |
| P3-3 | Sentinel 熔断配置 | OpenFeign 调用增加熔断保护 |
| P3-4 | 前端页面联调 | 当前仅后端 API |

### 4.3 实施顺序建议

```
第一步：P0 修复（1-2 天）
  └─ 修复 gateway 3 个 P0 缺陷
  └─ 验证 gateway 路由到 auth/sales/finance 正常

第二步：核心业务流程打通（3-5 天）
  └─ finance 审核流程完整实现（review/submit-bank/bank-result/approve/reject）
  └─ RabbitMQ 联通 sales → finance
  └─ LoanAuditRecord append-only 审核轨迹写入

第三步：auth 认证体系（3-5 天）
  └─ ShiroConfig + ShiroRealm + BCryptPasswordEncoder
  └─ JWT Token 签发（可选，若 Shiro Session 不足）
  └─ gateway AuthFilter 修复（若 P0 已修复则跳过）

第四步：数据权限 + 定时任务（2-3 天）
  └─ DataScopeInterceptor
  └─ PublicSeaTask
  └─ MetaObjectHandler 自动填充

第五步：系统增强（持续）
  └─ Redis 缓存
  └─ 操作日志 AOP
  └─ 业绩统计排名
```

### 4.4 评审流程

每个 Plan 执行完毕后，必须输出 CheckXX.md 评审报告，格式：

```
scripts/qa/Check{序号}.md
内容包含：
1. 总体评分（多维度，1-10 分制）
2. 逐项验收（Plan 要求 vs 实际代码）
3. 发现问题汇总（P0/P1/P2 分级）
4. 修复优先级建议
5. 综合评级
```

---

## 五、文件变更记录

| 文件 | 变更 |
|------|------|
| scripts/plan-eng-review/Plan07.md | 新建 |

---

**评审时间：** 2026-04-04
**评审人：** 资深后端架构审查员
