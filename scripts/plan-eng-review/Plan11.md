# Plan11: 单一业务开发流程规范与待办分解

**版本：** v1.0
**日期：** 2026-04-13
**目标：** 将剩余未实现的单一业务整理为原子化任务，给出每个业务的标准化开发流程

---

## 一、当前进度总览

### 1.1 已完成（Gateway 基础设施就绪）

| 组件 | 状态 | 说明 |
|------|------|------|
| Gateway 循环依赖 | ✅ Plan08 | @Lazy + ObjectProvider 解决 |
| AuthFilter 路径 | ✅ Plan10 | `/auth/api/sysUser` 入口路径 |
| AuthFeignClient 路径 | ✅ Plan10 | Feign 路径改为 `/api/sysUser` |
| Gateway CORS | ✅ Plan10 | globalcors 声明式配置 |
| Gateway 路由 | ✅ | lb:// 负载均衡已就绪 |

### 1.2 剩余未实现业务（来自 implementDetails.md + Plan07）

| # | 业务 | 模块 | 优先级 | 依赖关系 |
|---|------|------|--------|----------|
| T01 | Shiro 认证体系（BCrypt + ShiroRealm） | auth | P0 | 无，是其他一切的基石 |
| T02 | AuthServiceImpl 密码验证改为 BCrypt | auth | P0 | 依赖 T01 |
| T03 | 贷款审核状态流转（review/submit-bank/bank-result/approve/reject） | finance | P1 | 依赖 auth 认证（T01） |
| T04 | RabbitMQ 交换机/队列配置（common/mq/MqConfig） | common | P1 | 无 |
| T05 | sales 合同签署事件发送（ContractSignedEvent） | sales | P1 | 依赖 T04 |
| T06 | finance 接收合同签署事件并创建 LoanAudit | finance | P1 | 依赖 T04 + T05 |
| T07 | 提成发放逻辑（CommissionRecordService.grant()） | finance | P1 | 依赖 T03 |
| T08 | 服务费确认收款（ServiceFeeRecordService.confirmPay()） | finance | P1 | 依赖 T03 |
| T09 | 业绩 OpenFeign 回调接口完善（zoneId/commissionRate） | sales/finance | P2 | 依赖 T03 |
| T10 | 公海定时扫描任务（PublicSeaTask） | sales | P2 | 依赖 auth（T01 完成 MetaObjectHandler 才能自动填充销售） |
| T11 | 操作日志 AOP 切面（@OperationLog + Aspect） | system | P2 | 无 |
| T12 | Redis 缓存集成（SysParam/SysDict） | system | P2 | 无 |
| T13 | 数据权限拦截器（DataScopeInterceptor） | common | P2 | 依赖 T01（取 currentUserId） |
| T14 | MetaObjectHandler 自动填充 createdBy/updatedBy | common | P2 | 依赖 T01（Shiro Subject） |

---

## 二、标准化开发流程

每个单一业务开发必须遵循以下 6 个步骤，禁止跳过。

### 开发流程模板

```
Step 1: 理解业务需求
  └─ 理解用户故事和验收标准
  └─ 理解上下游依赖（谁调用我，我调用谁）

Step 2: 设计接口（Controller 层）
  └─ 确定 HTTP 方法/路径/请求/响应格式
  └─ 参照 implementDetails.md 接口规范
  └─ 遵循 Plan07 的 REST 路径规范（/api/{entity}）
  └─ 写入 @OperationLog 注解（业务操作类接口）

Step 3: 实现 Service 层
  └─ 业务逻辑（状态流转/计算/校验）
  └─ 事务边界（@Transactional 标注写操作）
  └─ 跨模块调用（Feign 在 Service 层调用，不在 Controller 层）
  └─ 幂等性保证（重复调用不破坏数据）

Step 4: 实现 DAO/Mapper 层
  └─ 复杂查询写在 Mapper XML
  └─ 简单 CRUD 使用 BaseMapper
  └─ 禁止 SELECT *

Step 5: 自测验证
  └─ 单元测试覆盖所有分支
  └─ Postman/curl 调用验证接口
  └─ 边界条件测试（null/空列表/超长字符串/负数）

Step 6: 代码审查
  └─ 检查 DRY 违规
  └─ 检查异常处理是否完整
  └─ 检查事务是否正确
  └─ 确认无硬编码/幻数
```

---

## 三、原子化任务详情

### T01: Shiro 认证体系

**模块：** auth
**优先级：** P0（是一切的基础）
**涉及文件：** ShiroConfig.java, ShiroRealm.java, SysPermissionDao.java
**依赖：** 无

**工作内容：**
1. 创建 `auth/src/main/java/com/dafuweng/auth/config/ShiroConfig.java`
   - 配置 SecurityManager + ShiroRealm
   - 配置 ShiroFilterFactoryBean，放行 `/api/sysUser/login` 和 `/api/sysUser/page`
   - 注册 BCryptPasswordEncoder Bean
   - 配置 DefaultAdvisorAutoProxyCreator + AuthorizationAttributeSourceAdvisor

2. 创建 `auth/src/main/java/com/dafuweng/auth/config/ShiroRealm.java`
   - 实现 `doGetAuthenticationInfo`：查询用户，验证密码
   - 实现 `doGetAuthorizationInfo`：加载角色和权限码
   - SysPermissionDao 添加 `selectPermCodesByRoleId()` 方法

3. BCryptPasswordEncoder Bean 已在 ShiroConfig 中注册

**Step 1-6 详细说明：**

```
Step 1: 用户故事
  - 用户登录时 Shiro 进行身份验证
  - 用户访问受保护资源时 Shiro 进行权限校验
  - 验收：登录成功返回用户信息，访问无权限接口返回 403

Step 2: Controller 接口
  - POST /auth/login (已有 SysUserController.login)
  - GET /auth/currentUser (已有)
  - 需在 ShiroFilterChain 中配置 /auth/api/sysUser/login 和 /auth/api/sysUser/page 为 anon

Step 3: ShiroConfig.java 实现
  - SecurityManager 配置 Realm
  - ShiroFilterFactoryBean 设置 filterChainDefinition
  - BCryptPasswordEncoder Bean

Step 4: ShiroRealm.java 实现
  - doGetAuthenticationInfo: 调用 sysUserDao.selectByUsername
  - doGetAuthorizationInfo: 调用 sysUserRoleDao.selectRoleIdsByUserId
  - SysPermissionDao.selectPermCodesByRoleId() SQL

Step 5: 自测
  - POST /auth/api/sysUser/login 带用户名密码
  - GET 其他接口不带 Token 应返回 401
  - GET 其他接口带正确 Token 应返回业务数据
```

---

### T02: AuthServiceImpl 密码验证改为 BCrypt

**模块：** auth
**优先级：** P0
**涉及文件：** AuthServiceImpl.java, SysUserDao.xml
**依赖：** T01（ShiroConfig 中的 BCryptPasswordEncoder Bean）

**工作内容：**
- 删除 `SALT` 常量（BCrypt 不需要固定盐）
- 注入 `BCryptPasswordEncoder`
- 登录验证改为 `passwordEncoder.matches(rawPassword, user.getPassword())`
- 将 `user.getDeleted() == 1` 改为 `Objects.equals(user.getDeleted(), (short) 1)`

**关键代码片段：**

```java
// 修复前（SHA-256，错误）
String hashedPassword = new SimpleHash("SHA-256", rawPassword, ByteSource.Util.bytes(SALT), 2).toString();
if (!hashedPassword.equals(user.getPassword())) {

// 修复后（BCrypt，正确）
@Autowired
private BCryptPasswordEncoder passwordEncoder;

if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
    throw new IllegalArgumentException("用户名或密码错误");
}
```

---

### T03: 贷款审核状态流转

**模块：** finance
**优先级：** P1
**涉及文件：** LoanAuditController.java, LoanAuditServiceImpl.java, LoanAuditRecordServiceImpl.java
**依赖：** T01（Shiro 认证后获取当前用户）

**工作内容：**

```
审核状态机：
  receive（接收）→ review（初审）→ submit_bank（提交银行）
  → bank_result（银行反馈）→ approve（终审通过）/ reject（终审拒绝）

各状态含义：
  audit_status=1: 待接收  audit_status=2: 初审中  audit_status=3: 已提交银行
  audit_status=4: 银行通过  audit_status=5: 银行拒绝
  audit_status=6: 终审通过  audit_status=7: 终审拒绝
```

**Controller 接口：**

```java
// POST /finance/audits/{id}/review  初审
@PostMapping("/{id}/review")
public Result<?> review(@PathVariable Long id, @RequestBody Map<String, Object> req);

// POST /finance/audits/{id}/submit-bank  提交银行
@PostMapping("/{id}/submit-bank")
public Result<?> submitBank(@PathVariable Long id, @RequestBody Map<String, Object> req);

// POST /finance/audits/{id}/bank-result  银行反馈
@PostMapping("/{id}/bank-result")
public Result<?> bankResult(@PathVariable Long id, @RequestBody Map<String, Object> req);

// POST /finance/audits/{id}/approve  终审通过
@PostMapping("/{id}/approve")
public Result<?> approve(@PathVariable Long id, @RequestBody Map<String, Object> req);

// POST /finance/audits/{id}/reject  终审拒绝
@PostMapping("/{id}/reject")
public Result<?> reject(@PathVariable Long id, @RequestBody Map<String, Object> req);
```

**Service 层核心逻辑：**

```java
@Transactional
public void review(Long id, Long auditorId, String comment) {
    LoanAuditEntity audit = getById(id);
    if (audit.getAuditStatus() != 1) {
        throw new IllegalStateException("当前状态不允许初审");
    }
    audit.setAuditStatus((short) 2);
    audit.setReviewerId(auditorId);
    updateById(audit);
    // 写入 loan_audit_record，action='review'
    saveRecord(id, "review", auditorId, comment);
}

@Transactional
public void approve(Long id, Long approverId, String comment) {
    LoanAuditEntity audit = getById(id);
    if (audit.getAuditStatus() != 4) {  // 必须是银行通过后才能终审
        throw new IllegalStateException("当前状态不允许终审");
    }
    audit.setAuditStatus((short) 6);
    updateById(audit);
    // 写入 loan_audit_record，action='approve'
    saveRecord(id, "approve", approverId, comment);
    // 触发 OpenFeign 通知 sales 创建业绩
    // 触发创建 commission_record
}
```

**Step 4 LoanAuditRecordService：** append-only 写入审核轨迹，每步必记录：

```java
@Transactional
public void saveRecord(Long auditId, String action, Long operatorId, String comment) {
    LoanAuditRecordEntity record = new LoanAuditRecordEntity();
    record.setAuditId(auditId);
    record.setAction(action);   // receive/review/submit_bank/bank_result/approve/reject
    record.setOperatorId(operatorId);
    record.setComment(comment);
    record.setOperateTime(new Date());
    baseMapper.insert(record);  // append-only，禁止 update/delete
}
```

---

### T04: RabbitMQ 交换机/队列配置

**模块：** common
**优先级：** P1（跨模块事件驱动的基石）
**涉及文件：** common/mq/MqConfig.java, common/mq/event/ContractSignedEvent.java, common/mq/event/LoanApprovedEvent.java
**依赖：** 无（pom 中 spring-boot-starter-amqp 已就绪）

**工作内容：**

```java
// common/src/main/java/com/dafuweng/common/mq/MqConfig.java
@Configuration
public class MqConfig {
    public static final String EXCHANGE_SALES = "sales.exchange";
    public static final String QUEUE_CONTRACT_SIGNED = "contract.signed.queue";
    public static final String ROUTING_CONTRACT_SIGNED = "contract.signed";

    @Bean
    public DirectExchange salesExchange() {
        return new DirectExchange(EXCHANGE_SALES);
    }

    @Bean
    public Queue contractSignedQueue() {
        return QueueBuilder.durable(QUEUE_CONTRACT_SIGNED).build();
    }

    @Bean
    public Binding contractSignedBinding() {
        return BindingBuilder.bind(contractSignedQueue())
                .to(salesExchange())
                .with(ROUTING_CONTRACT_SIGNED);
    }
}
```

事件类：
```java
// common/src/main/java/com/dafuweng/common/mq/event/ContractSignedEvent.java
@Data
public class ContractSignedEvent implements Serializable {
    private Long contractId;
    private Long customerId;
    private Long salesRepId;
    private BigDecimal contractAmount;
    private Date signDate;
}
```

---

### T05: sales 合同签署事件发送

**模块：** sales
**优先级：** P1
**涉及文件：** ContractServiceImpl.java, MqConfig.java, ContractSignedEvent.java
**依赖：** T04（RabbitMQ 配置）

**工作内容：**

```java
// sales ContractServiceImpl.signContract() 中添加：
@Autowired
private RabbitTemplate rabbitTemplate;

@Transactional
public void signContract(Long contractId) {
    ContractEntity contract = getById(contractId);
    if (contract.getStatus() != 1) {
        throw new IllegalStateException("当前状态不允许签署");
    }
    contract.setStatus((short) 2);  // 已签署
    contract.setSignDate(new Date());
    updateById(contract);

    // 发送 RabbitMQ 事件
    ContractSignedEvent event = new ContractSignedEvent();
    event.setContractId(contractId);
    event.setCustomerId(contract.getCustomerId());
    event.setSalesRepId(contract.getSalesRepId());
    event.setContractAmount(contract.getContractAmount());
    event.setSignDate(contract.getSignDate());
    rabbitTemplate.convertAndSend(
        MqConfig.EXCHANGE_SALES,
        MqConfig.ROUTING_CONTRACT_SIGNED,
        event
    );
}
```

---

### T06: finance 接收合同签署事件

**模块：** finance
**优先级：** P1
**涉及文件：** LoanAuditMqListener.java（新建）, LoanAuditServiceImpl.java
**依赖：** T04 + T05

**工作内容：**

```java
// finance/src/main/java/com/dafuweng/finance/mq/ContractSignedListener.java
@RabbitListener(queues = MqConfig.QUEUE_CONTRACT_SIGNED)
public void onContractSigned(ContractSignedEvent event) {
    // 自动创建 loan_audit 记录，status=1（待审核）
    LoanAuditEntity audit = new LoanAuditEntity();
    audit.setContractId(event.getContractId());
    audit.setCustomerId(event.getCustomerId());
    audit.setSalesRepId(event.getSalesRepId());
    audit.setContractAmount(event.getContractAmount());
    audit.setAuditStatus((short) 1);
    audit.setBankAuditStatus((short) 0);
    loanAuditService.save(audit);

    // 写入审核轨迹，action='receive'
    loanAuditRecordService.saveRecord(audit.getId(), "receive", null, "系统自动接收合同");
}
```

---

### T07: 提成发放逻辑（CommissionRecordService.grant()）

**模块：** finance
**优先级：** P1
**涉及文件：** CommissionRecordServiceImpl.java
**依赖：** T03（终审通过后才能发放提成）

**工作内容：**

```java
// finance/CommissionRecordServiceImpl.grant()
@Transactional
public void grant(Long recordId, Long granterId) {
    CommissionRecordEntity record = getById(recordId);
    if (record.getStatus() != 2) {  // 2=已确认，3=已发放
        throw new IllegalStateException("当前状态不允许发放");
    }
    record.setStatus((short) 3);  // 已发放
    record.setGrantTime(new Date());
    record.setGranterId(granterId);
    updateById(record);
}
```

---

### T08: 服务费确认收款（ServiceFeeRecordService.confirmPay()）

**模块：** finance
**优先级：** P1
**涉及文件：** ServiceFeeRecordServiceImpl.java
**依赖：** T03

**工作内容：**

```java
// finance/ServiceFeeRecordServiceImpl.confirmPay()
@Transactional
public void confirmPay(Long recordId, Long accountantId) {
    ServiceFeeRecordEntity record = getById(recordId);
    if (record.getPaymentStatus() != 0) {  // 0=未支付
        throw new IllegalStateException("当前状态不允许确认收款");
    }
    record.setPaymentStatus((short) 1);  // 已支付
    record.setConfirmPayTime(new Date());
    record.setConfirmPayBy(accountantId);
    updateById(record);
}
```

---

### T09: 业绩 OpenFeign 回调完善（zoneId/commissionRate）

**模块：** sales + finance
**优先级：** P2
**涉及文件：** sales InternalSalesController.java, finance LoanAuditServiceImpl.java
**依赖：** T03（T03 的 approve 触发调用）

**问题根因：** 当前 `PerformanceCreateDTO` 中 zoneId 和 commissionRate 传 0，因为 sales 无法查到这两个字段。

**解决方案：** approve 终审通过时，finance 已有完整的 contract 信息（包含 zoneId），直接通过 DTO 传入：

```java
// finance LoanAuditServiceImpl.approve() 中：
PerformanceCreateDTO dto = new PerformanceCreateDTO();
dto.setContractId(contract.getId());
dto.setCustomerId(contract.getCustomerId());
dto.setSalesRepId(contract.getSalesRepId());
dto.setZoneId(contract.getZoneId());  // 已有，从 contract 传入
dto.setCommissionRate(product.getCommissionRate());  // 从 FinanceProduct 查询
dto.setContractAmount(contract.getContractAmount());
// 调用 salesFeignClient.createPerformance(dto)
```

---

### T10: 公海定时扫描任务

**模块：** sales
**优先级：** P2
**涉及文件：** sales/task/PublicSeaTask.java
**依赖：** T01（MetaObjectHandler 需要 Shiro Subject，createdBy 才有效）

**工作内容：**

```java
// sales/src/main/java/com/dafuweng/sales/task/PublicSeaTask.java
@Scheduled(cron = "0 0 2 * * ?")  // 每天凌晨2点
public void scanPublicSeaCustomers() {
    // 从 sys_param 读取 customer.public_sea_days（默认30天）
    Integer publicSeaDays = getPublicSeaDays();

    // 查询条件：status NOT IN (3,4,5) AND next_follow_up_date < now
    // AND created_at < now - publicSeaDays
    List<CustomerEntity> toPublicSea = customerDao.selectToPublicSea(publicSeaDays);

    // 批量更新 status = 5, public_sea_time = now
    for (CustomerEntity c : toPublicSea) {
        c.setStatus((short) 5);
        c.setPublicSeaTime(new Date());
        customerDao.updateById(c);
    }
}
```

---

### T11: 操作日志 AOP 切面

**模块：** system
**优先级：** P2
**涉及文件：** system/config/OperationLog.java（注解）, system/config/OperationLogAspect.java
**依赖：** 无

**工作内容：**

```java
// system/config/OperationLog.java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLog {
    String module();
    String action();
}

// system/config/OperationLogAspect.java
@Aspect
@Component
public class OperationLogAspect {
    @Autowired
    private SysOperationLogDao sysOperationLogDao;

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long cost = System.currentTimeMillis() - start;

        // 异步写入日志（避免阻塞主线程）
        CompletableFuture.runAsync(() -> {
            SysOperationLogEntity log = new SysOperationLogEntity();
            log.setUsername(getCurrentUsername());
            log.setModule(operationLog.module());
            log.setAction(operationLog.action());
            log.setMethod(joinPoint.getSignature().getName());
            log.setParams(JSON.toJSONString(joinPoint.getArgs()));
            log.setCostTime((int) cost);
            log.setResult(result instanceof Result ? ((Result<?>) result).getCode() : 200);
            sysOperationLogDao.insert(log);
        });

        return result;
    }
}
```

---

### T12: Redis 缓存集成（SysParam/SysDict）

**模块：** system
**优先级：** P2
**涉及文件：** system/service/impl/SysParamServiceImpl.java, system/service/impl/SysDictServiceImpl.java
**依赖：** pom 中 spring-boot-starter-data-redis 已就绪

**工作内容：**

```java
// SysParamServiceImpl 查询时：
@Cacheable(value = "param", key = "#paramKey")
public String getParamValue(String paramKey) {
    SysParamEntity param = sysParamDao.selectByParamKey(paramKey);
    return param != null ? param.getParamValue() : null;
}

// 修改时删除缓存：
@CacheEvict(value = "param", key = "#paramKey")
public void updateParam(String paramKey, String paramValue) {
    // ... 更新逻辑
    cacheManager.getCache("param").evict(paramKey);
}
```

---

### T13: 数据权限拦截器

**模块：** common
**优先级：** P2
**涉及文件：** common/config/DataScopeInterceptor.java
**依赖：** T01（Shiro Subject 取 currentUserId）

**工作内容：**

```java
// common/config/DataScopeInterceptor.java
@Intercepts({
    @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
})
public class DataScopeInterceptor implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 从 Shiro Subject 获取当前用户
        SysUserEntity user = (SysUserEntity) SecurityUtils.getSubject().getPrincipal();
        Long userId = user.getId();
        Short dataScope = user.getDataScope();  // 1=本人 2=本部门 3=本战区 4=全部

        String originalSql = ...;  // 拦截 SQL，动态拼接 WHERE 条件
        switch (dataScope) {
            case 1: sql += " AND created_by = " + userId; break;
            case 2: sql += " AND dept_id = " + user.getDeptId(); break;
            case 3: sql += " AND zone_id = " + user.getZoneId(); break;
            case 4: /* 不过滤 */ break;
        }
        return invocation.proceed();
    }
}
```

---

### T14: MetaObjectHandler 自动填充 createdBy/updatedBy

**模块：** common
**优先级：** P2
**涉及文件：** common/config/AutoFillMetaObjectHandler.java
**依赖：** T01（Shiro Subject 取 currentUserId）

**工作内容：**

```java
// common/config/AutoFillMetaObjectHandler.java
@Component
public class AutoFillMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createdAt", Date.class, new Date());
        this.strictInsertFill(metaObject, "updatedAt", Date.class, new Date());
        Long userId = getCurrentUserId();  // 从 Shiro Subject 获取
        if (userId != null) {
            this.strictInsertFill(metaObject, "createdBy", Long.class, userId);
            this.strictInsertFill(metaObject, "updatedBy", Long.class, userId);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedAt", Date.class, new Date());
        Long userId = getCurrentUserId();
        if (userId != null) {
            this.strictUpdateFill(metaObject, "updatedBy", Long.class, userId);
        }
    }

    private Long getCurrentUserId() {
        try {
            Subject subject = SecurityUtils.getSubject();
            if (subject.isAuthenticated()) {
                SysUserEntity user = (SysUserEntity) subject.getPrincipal();
                return user.getId();
            }
        } catch (Exception e) { /* 无登录上下文 */ }
        return null;
    }
}
```

---

## 四、标准化 Review Checklist

每个任务完成后，对照以下清单自检：

| # | 检查项 | 说明 |
|---|--------|------|
| 1 | Controller 方法有 @OperationLog 注解（业务操作类） | 记录谁在什么时间做了什么 |
| 2 | Service 写方法有 @Transactional | 事务边界正确 |
| 3 | 跨模块调用在 Service 层，不在 Controller 层 | FeignClient 在 Service 注入和调用 |
| 4 | 无 SELECT * | XML 中明确列出字段 |
| 5 | 状态流转有校验 | 当前状态允许目标操作，否则抛异常 |
| 6 | 幂等性保证 | 重复调用不破坏数据（如 create 前先查是否已存在）|
| 7 | 异常有处理 | 业务异常有明确 message，不应该 500 |
| 8 | 无硬编码幻数 | 魔法数字定义常量或从配置读取 |
| 9 | Result 返回格式统一 | 成功 Result.success()，失败 Result.error(code, msg) |
| 10 | 接口路径符合 REST 规范 | 参考 implementDetails.md |

---

## 五、NOT in Scope

- pom 依赖调整（已明确不可更改）
- 前端页面开发
- XXL-JOB 分布式任务调度（先用 @Scheduled 单机）
- Sentinel 熔断配置（框架已引入，规则后续在 Nacos 配置）
- Nacos 配置中心化（envs.md 环境变量方案）

## 六、文件变更记录

| 文件 | 变更 |
|------|------|
| scripts/plan-eng-review/Plan11.md | 新建 |

---

**计划时间：** 2026-04-13
**评审人：** 资深后端架构审查员
