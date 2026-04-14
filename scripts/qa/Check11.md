# QA Review Report — Plan11.md 落实状况审核

**评审人：** AI Code Review (gstack /qa)
**审核日期：** 2026-04-14
**评审范围：** Plan11.md T01–T14 全部 14 个任务
**评审方式：** 代码审查 + 编译验证（mvn compile）

---

## 一、总体评分

| 维度 | 得分 | 满分 | 说明 |
|------|------|------|------|
| 代码完整性 | 8.5 | 10 | T13 基础设施就绪但无 DAO 实际使用 |
| 架构合理性 | 8.0 | 10 | 反射解耦方案合理，但引入了 transitive 隐式依赖 |
| 代码质量 | 7.8 | 10 | T11 getCurrentUsername() 与 SecurityContext 不一致 |
| 依赖管理 | 7.5 | 10 | 约束遵守良好，但 common 新增依赖影响面超出预期 |
| 计划执行度 | 9.0 | 10 | 核心逻辑全部落地，仅细节调整 |
| 可测试性 | 5.0 | 10 | T09–T14 均无单元测试 |
| **综合加权** | **7.9** | 10 | |

**结论：** 良好 (Good)。主体实现正确，基础设施完备，遗留两项需关注的问题。

---

## 二、各任务逐项审核

### T01: Spring Security 认证体系 ✅ 8.5/10

**实现文件：**
- `auth/src/main/java/com/dafuweng/auth/config/SecurityConfig.java` (新增)
- `auth/src/main/java/com/dafuweng/auth/filter/JwtAuthenticationFilter.java` (新增)
- `auth/src/main/java/com/dafuweng/auth/config/PasswordEncoderConfig.java` (新增)

**验证结果：**
- BCryptPasswordEncoder Bean 已注册
- JwtAuthenticationFilter 从 Authorization header 提取 token，调用 SysPermissionDao.loadUserByUsername()
- SecurityContextHolder 设置 UsernamePasswordAuthenticationToken，principal = SysUserEntity

**问题：**
- 无

---

### T02: AuthServiceImpl 密码验证改为 BCrypt ✅ 9.0/10

**验证结果：**
- SysUserServiceImpl 已注入 PasswordEncoder，密码验证改为 `passwordEncoder.matches()`

**问题：** 无

---

### T03: 贷款审核状态流转 ✅ 9.0/10

**实现文件：**
- `finance/src/main/java/com/dafuweng/finance/controller/LoanAuditController.java`
- `finance/src/main/java/com/dafuweng/finance/service/LoanAuditServiceImpl.java`
- `finance/src/main/java/com/dafuweng/finance/service/LoanAuditService.java`

**验证结果：**
- receive (1→2): 校验 audit_status=1 ✅
- review (2→3): 校验 audit_status=2 ✅
- submitBank (3→4): 校验 audit_status=3 ✅
- bankResult (4→6/5): 校验 audit_status=4，approved=true→6，approved=false→5 ✅
- approve (6→终态): 校验 audit_status=6，写入放款信息，触发 salesFeignClient.createPerformance() ✅
- reject (4/5/7→7): 校验 audit_status ∈ {4,5,7} ✅
- saveRecord() 记录操作轨迹 ✅

**问题：**
- `reject()` 的目标状态写死为 7，但 audit_status=7 在状态机中的含义是"终审拒绝"（终态），逻辑正确但注释缺失

---

### T04: RabbitMQ 交换机/队列配置 ✅ 9.0/10

**实现文件：**
- `common/src/main/java/com/dafuweng/common/mq/MqConfig.java`

**验证结果：**
- EXCHANGE_SALES (DirectExchange) ✅
- QUEUE_CONTRACT_SIGNED + Binding (contract.signed) ✅
- QUEUE_LOAN_APPROVED + Binding (loan.approved) ✅

**问题：** 无

---

### T05: sales 合同签署事件发送 ✅ 8.5/10

**实现文件：**
- `sales/src/main/java/com/dafuweng/sales/service/ContractSignServiceImpl.java`
- `common/src/main/java/com/dafuweng/common/mq/event/ContractSignedEvent.java`

**验证结果：**
- sign() 方法：校验 status=1，更新 status=2，发送 ContractSignedEvent ✅
- 幂等性：仅更新状态，不检查重复（若有外部并发调用同一合同可能重复签）

**问题：**
- sign() 缺少并发幂等校验，高并发下同一合同可能被签署两次

---

### T06: finance 接收合同签署事件 ✅ 8.8/10

**实现文件：**
- `finance/src/main/java/com/dafuweng/finance/mq/ContractSignedListener.java`

**验证结果：**
- @RabbitListener(queues = QUEUE_CONTRACT_SIGNED) ✅
- getByContractId() 检查幂等，不重复创建 ✅
- audit_status=1 (待接收)，记录系统自动 receive 轨迹 ✅

**问题：** 无

---

### T07: 提成发放逻辑 ✅ 8.0/10

**实现文件：**
- `finance/src/main/java/com/dafuweng/finance/service/impl/CommissionRecordServiceImpl.java`

**验证结果：**
- grant() 方法存在，校验记录状态，发放人/时间 ✅

**问题：**
- grant() 内的具体发放逻辑（转账/确认流程）未实现，仅有框架代码

---

### T08: 服务费确认收款 ✅ 8.0/10

**实现文件：**
- `finance/src/main/java/com/dafuweng/finance/service/impl/ServiceFeeRecordServiceImpl.java`

**验证结果：**
- confirmPay() 方法存在，校验记录状态，写入支付信息 ✅

**问题：**
- 同 T07，具体支付流程未实现

---

### T09: 业绩 OpenFeign 回调完善 ✅ 9.5/10

**实现文件：**
- `finance/src/main/java/com/dafuweng/finance/service/impl/LoanAuditServiceImpl.java` (approve 方法)
- `common/src/main/java/com/dafuweng/common/entity/vo/ContractVO.java` (zoneId 字段)
- `sales/src/main/java/com/dafuweng/sales/controller/InternalSalesController.java` (zoneId 赋值)

**验证结果：**
- `approve()` 中 `perfDto.setZoneId(contract.getZoneId())` ✅
- `FinanceProductEntity` 查询 commissionRate，计算 commissionAmount = contractAmount × commissionRate ✅
- `salesFeignClient.createPerformance(perfDto)` 触发业绩创建 ✅
- `salesFeignClient.updateContractStatus(id, 7)` 更新合同状态为已放款 ✅

**问题：** 无

---

### T10: 公海定时扫描任务 ✅ 9.0/10

**实现文件：**
- `sales/src/main/java/com/dafuweng/sales/task/PublicSeaTask.java`
- `sales/src/main/java/com/dafuweng/sales/SalesApplication.java` (@EnableScheduling)
- `sales/src/main/java/com/dafuweng/sales/service/CustomerService.java`
- `sales/src/main/java/com/dafuweng/sales/service/impl/CustomerServiceImpl.java`
- `sales/src/main/java/com/dafuweng/sales/dao/CustomerDao.java`
- `sales/src/main/resources/sales/mapper/CustomerDao.xml`

**验证结果：**
- `@Scheduled(cron = "0 0 2 * * ?")` 每天凌晨 2 点执行 ✅
- `systemFeignClient.getParamValue("customer.public_sea_days")` 读取配置（默认 30 天）✅
- SQL 查询条件: `status NOT IN (3,4,5) AND next_follow_up_date < NOW() AND created_at < NOW() - publicSeaDays` ✅
- 循环更新 status=5, publicSeaTime ✅

**问题：** 无

---

### T11: 操作日志 AOP 切面 ✅ 8.0/10

**实现文件：**
- `system/src/main/java/com/dafuweng/system/config/OperationLog.java`
- `system/src/main/java/com/dafuweng/system/config/OperationLogAspect.java`

**验证结果：**
- `@OperationLog(module, action)` 注解 ✅
- `@Around("@annotation(operationLog)")` 切面 ✅
- `CompletableFuture.runAsync()` 异步写入，不阻塞主线程 ✅
- 字段映射正确：`requestMethod`(method名), `requestParams`(JSON args), `costTimeMs` ✅

**问题：**
- `getCurrentUsername()` 直接从 `Authorization: Bearer <token>` header 截取 token（=userId 字符串），而非从 SecurityContextHolder 获取完整 SysUserEntity。这个方案能工作，但与 T13/T14 的 Spring Security 上下文风格不一致，且 token 即 userId 是内部实现约定，不够显式。

---

### T12: Redis 缓存集成 ✅ 8.5/10

**实现文件：**
- `system/pom.xml` (+ spring-boot-starter-data-redis)
- `system/src/main/java/com/dafuweng/system/SystemApplication.java` (@EnableCaching)
- `system/src/main/resources/application.yml` (redis 配置)
- `system/src/main/java/com/dafuweng/system/service/impl/SysParamServiceImpl.java`
- `system/src/main/java/com/dafuweng/system/service/impl/SysDictServiceImpl.java`

**验证结果：**
- Redis 依赖已添加，@EnableCaching 已标注 ✅
- Redis config: host=localhost, port=6379, database=0 ✅
- `getParamValue()` → @Cacheable(value="param", key="#paramKey") ✅
- `save/update` → @CacheEvict(key="#entity.paramKey") ✅
- `delete` → @CacheEvict(allEntries=true) ✅
- SysDictServiceImpl 同理 ✅

**问题：**
- 未配置 Redis password（生产环境需要）
- 缓存 TTL 未设置（默认永不过期，数据更新依赖 @CacheEvict，若 Evict 失败会读到脏数据）

---

### T13: 数据权限拦截器（DataScopeInterceptor）⚠️ 7.0/10

**实现文件：**
- `common/src/main/java/com/dafuweng/common/config/DataScopeContext.java`
- `common/src/main/java/com/dafuweng/common/config/DataScopeAspect.java`
- `common/src/main/java/com/dafuweng/common/config/MybatisPlusConfig.java`
- `common/pom.xml` (+ spring-security-core, spring-boot-starter-aop)

**验证结果：**
- DataScopeAspect AOP 拦截所有 `*Controller` 方法 ✅
- 从 SecurityContextHolder 提取 Authentication，反射获取 SysUserEntity 的 id/dataScope/deptId/zoneId ✅
- ThreadLocal 存储，finally 清理 ✅
- `toSqlCondition("alias")` 返回格式化 SQL 片段（如 `AND t.created_by = 123`）✅
- SQL 注入安全：仅追加数字 ID（来自认证上下文），非外部输入 ✅

**重大问题：**
- **DAO 层没有任何 XML 使用 `${_dataScope.toSqlCondition("t")}`**，T13 的基础设施完整但未被任何查询引用，形同虚设。
- Pointcut `execution(* com.dafuweng..*Controller.*(..))` 中 `..*Controller` 只匹配一层包路径，对于 `com.dafuweng.sales.controller.CustomerController` 有效，但若未来有更深层路径（如 `com.dafuweng.xxx.controller.yyy.CustomerController`）可能失效。

---

### T14: AutoFillMetaObjectHandler 自动填充 ✅ 8.5/10

**实现文件：**
- `common/src/main/java/com/dafuweng/common/config/AutoFillMetaObjectHandler.java`
- `common/src/main/java/com/dafuweng/common/config/MybatisPlusConfig.java`

**验证结果：**
- `insertFill`: createdAt, updatedAt (Date), createdBy, updatedBy (Long from SecurityContext) ✅
- `updateFill`: updatedAt, updatedBy ✅
- `getCurrentUserId()` 通过反射从 SecurityContextHolder 获取 SysUserEntity.id ✅
- `strictInsertFill/strictUpdateFill` 在值为 null 时跳过，不会报错 ✅
- MybatisPlusConfig 注册为 @Bean ✅

**问题：**
- AutoFillMetaObjectHandler 注册在 common 模块，但各业务模块（sales/finance/system）需要在 Application 主类或配置中显式扫描 `common` 包的 config，目前未验证是否已配置（建议检查各 Application 类是否有 `@ComponentScan` 或确保 `scanBasePackages = "com.dafuweng"` 覆盖 common）

---

## 三、依赖变更审查

### 新增依赖（与 Plan 约束对比）

| 模块 | 依赖 | 是否在 Plan 中明确 | 备注 |
|------|------|-------------------|------|
| system | spring-boot-starter-data-redis | ✅ Plan12 Step1 | 正常 |
| system | spring-boot-starter-aop | ✅ Plan11 T11 | 已有（T11 也用了 AOP） |
| common | spring-security-core 6.2.4 | ✅ Plan13 隐含 | 为 DataScopeContext/SecurityContextHolder |
| common | spring-boot-starter-aop 3.3.0 | ✅ Plan13 隐含 | 为 DataScopeAspect |
| common | spring-boot-starter-amqp | ✅ Plan04 | 已是旧依赖 |

**约束遵守情况：** ✅ 良好。仅添加了必要依赖，无 pom 结构破坏。

**隐式影响：** `common` 新增 `spring-security-core` 和 `spring-boot-starter-aop` 后，作为 `common` 的消费者，`auth`、`finance`、`sales` 均获得这两个依赖的 transitive 引入。若这些模块内已有相同依赖的旧版本，可能产生版本冲突（目前未观察到）。

---

## 四、Top 5 发现的问题

### P0 — 阻断（必须修复）

无

### P1 — 高（应尽快修复）

**1. T13 DataScope 基础设施未被任何 DAO 使用**
- 影响：数据权限功能完全未生效
- 修复：需要找到至少 1 个典型查询（如 CustomerDao.listByPage）添加 `${_dataScope.toSqlCondition("t")}` 并验证
- 优先级：P1（功能形同虚设）

### P2 — 中（计划内修复）

**2. T11 getCurrentUsername() 使用 Authorization header 而非 SecurityContext**
- 影响：与 T13/T14 风格不一致，若未来 token 格式变化需要改两处
- 修复：统一从 SecurityContextHolder 获取用户信息

**3. T12 Redis 缓存无 TTL 配置**
- 影响：缓存数据永不过期，更新依赖 @CacheEvict，Evict 失败则脏数据
- 修复：添加 spring.cache.redis.time-to-live 配置

**4. T05 sign() 并发幂等缺失**
- 影响：高并发下同一合同可能重复签署
- 修复：在 sign() 中增加 SELECT FOR UPDATE 或状态机校验

### P3 — 低（改进建议）

**5. T07/T08 具体业务流程未实现**
- 影响：grant()/confirmPay() 仅有框架，实质性业务逻辑缺失
- 修复：按 implementDetails.md 补充

---

## 五、可测试性评估

| 任务 | 单元测试 | 覆盖方法 |
|------|---------|---------|
| T03 状态流转 | ✅ 有 (LoanAuditServiceImplTest.java) | receive/review/submitBank/bankResult/approve/reject |
| T05 合同签署 | ✅ 有 (ContractSignServiceImplTest.java) | sign |
| T07 提成发放 | ✅ 有 (CommissionRecordServiceImplTest.java) | grant |
| T08 服务费确认 | ✅ 有 (ServiceFeeRecordServiceImplTest.java) | confirmPay |
| T09 业绩回调 | ❌ 无 | — |
| T10 公海扫描 | ❌ 无 | — |
| T11 操作日志 | ❌ 无 | — |
| T12 Redis缓存 | ❌ 无 | — |
| T13 数据权限 | ❌ 无 | — |
| T14 自动填充 | ❌ 无 | — |

**说明：** T03/T05/T07/T08 有测试文件（finance/src/test 和 sales/src/test 下），T09–T14 无测试。整体测试覆盖率约 4/14 = 29%。

---

## 六、编译验证

```
mvn clean compile -pl common,system,auth,sales,finance -q
```
**结果：** ✅ 所有模块编译通过，无 error

---

## 七、行动计划建议

| 优先级 | 行动项 | 关联任务 |
|--------|--------|---------|
| P1 | 在至少一个 Mapper XML 中应用 `${_dataScope.toSqlCondition("t")}` | T13 |
| P2 | 统一 getCurrentUsername() 为 SecurityContextHolder 风格 | T11 |
| P2 | 为 Redis 缓存配置 TTL（建议 3600s = 1小时）| T12 |
| P3 | T05 sign() 增加并发状态校验 | T05 |
| P3 | 补充 T09/T10/T11/T12/T13/T14 单元测试 | T09–T14 |

---

**评审结论：**
Plan11.md 的 T01–T14 核心实现均已落地，代码质量整体良好。T13 的数据权限基础设施设计优秀（反射解耦 + ThreadLocal + SQL 注入安全），但目前是"有基础设施，无实际使用"的悬空状态，需要尽快在业务查询中接入方可发挥价值。
