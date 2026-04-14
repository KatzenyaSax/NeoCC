# Plan11: 单一业务开发流程规范与待办分解

**版本：** v2.0
**日期：** 2026-04-13
**目标：** 将剩余未实现的单一业务整理为原子化任务，给出每个业务的标准化开发流程；本次更新：T01 从 Shiro 切换为 Spring Security

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
| T01 | Spring Security 认证体系（UserDetailsService + JWT Filter） | auth | P0 | ✅ 已完成 |
| T02 | AuthServiceImpl 密码验证改为 BCrypt | auth | P0 | ✅ 已完成 |
| T03 | 贷款审核状态流转（review/submit-bank/bank-result/approve/reject） | finance | P1 | ✅ 已完成 |
| T04 | RabbitMQ 交换机/队列配置（common/mq/MqConfig） | common | P1 | ✅ 已完成 |
| T05 | sales 合同签署事件发送（ContractSignedEvent） | sales | P1 | ✅ 已完成 |
| T06 | finance 接收合同签署事件并创建 LoanAudit | finance | P1 | ✅ 已完成 |
| T07 | 提成发放逻辑（CommissionRecordService.grant()） | finance | P1 | ✅ 已完成 |
| T08 | 服务费确认收款（ServiceFeeRecordService.confirmPay()） | finance | P1 | ✅ 已完成 |
| T09 | 业绩 OpenFeign 回调接口完善（zoneId/commissionRate） | sales/finance | P2 | 依赖 T03 |
| T10 | 公海定时扫描任务（PublicSeaTask） | sales | P2 | 依赖 auth（T01 完成 MetaObjectHandler 才能自动填充销售） |
| T11 | 操作日志 AOP 切面（@OperationLog + Aspect） | system | P2 | 无 |
| T12 | Redis 缓存集成（SysParam/SysDict） | system | P2 | 无 |
| T13 | 数据权限拦截器（DataScopeInterceptor） | common | P2 | 依赖 T01（Spring Security SecurityContext 取 currentUserId） |
| T14 | MetaObjectHandler 自动填充 createdBy/updatedBy | common | P2 | 依赖 T01（Spring Security SecurityContext） |

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

### T01: Spring Security 认证体系

**模块：** auth
**优先级：** P0（是一切的基础）
**涉及文件：** SecurityConfig.java, JwtAuthenticationFilter.java, SysPermissionDao.java
**依赖：** 无

**迁移背景：**
原计划使用 Shiro 1.13.0 / 2.0.0，但 Shiro 2.0 的 `shiro-web` 模块内部硬依赖 `javax.servlet.Filter`（旧版），与 Spring Boot 3.x 的 `jakarta.servlet` 不兼容。已尝试排除自动配置、换用 `shiro-spring-boot-starter` 等方案均失败。改用 Spring Security（原生支持 Jakarta EE 9 / Spring Boot 3.x）。

**当前已就绪（来自前次 T01/T02 执行）：**
- BCryptPasswordEncoder Bean 已注册（ShiroConfig → SecurityConfig 继承）
- SysUserServiceImpl 已注入 BCryptPasswordEncoder，密码验证已改为 `passwordEncoder.matches()`
- SysPermissionDao.selectPermCodesByRoleId() 已实现

**迁移后删除文件：**
- `auth/src/main/java/com/dafuweng/auth/config/ShiroConfig.java`（BCryptPasswordEncoder 除外）
- `auth/src/main/java/com/dafuweng/auth/config/ShiroRealm.java`
- `auth/src/main/java/com/dafuweng/auth/filter/ShiroAuthenticationFilter.java`
- `AuthApplication.java` 中的 Shiro exclude 注解
- `application.yml` 中的 `shiro.enabled: false`

---

#### Step 1: 修改 pom.xml（依赖替换）

**移除：**
```xml
<!-- 删除 -->
<dependency>
    <groupId>org.apache.shiro</groupId>
    <artifactId>shiro-spring-boot-web-starter</artifactId>
    <version>2.0.0</version>
</dependency>
```

**新增：**
```xml
<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

**说明：** `spring-boot-starter-security` 内部传递依赖 `spring-security-crypto`（BCryptPasswordEncoder 所在包），无需单独引入。

---

#### Step 2: 新建 SecurityConfig.java（替换 ShiroConfig）

**路径：** `auth/src/main/java/com/dafuweng/auth/config/SecurityConfig.java`

```java
package com.dafuweng.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/sysUser/login", "/api/sysUser/page").permitAll()
                .requestMatchers("/static/**", "/favicon.ico").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

**关键点：**
- `SessionCreationPolicy.STATELESS` — 无状态，JWT 每次请求都要验证
- `jwtAuthenticationFilter` 在 `UsernamePasswordAuthenticationFilter` 之前执行
- `csrf.disable()` — API 服务不需要 CSRF 防护（由 Gateway 处理）
- `@EnableMethodSecurity` — 启用方法级 `@PreAuthorize` 注解（如需要）

---

#### Step 3: 新建 JwtAuthenticationFilter.java（替换 ShiroAuthenticationFilter）

**路径：** `auth/src/main/java/com/dafuweng/auth/filter/JwtAuthenticationFilter.java`

```java
package com.dafuweng.auth.filter;

import com.dafuweng.auth.service.SysUserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final SysUserService sysUserService;

    public JwtAuthenticationFilter(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // 放行公开路径
        if (path.contains("/api/sysUser/login") || path.contains("/api/sysUser/page")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            // 当前设计：token = userId 字符串（与 Plan08 Gateway AuthFilter 一致）
            Long userId = Long.parseLong(token);

            // 加载用户信息
            var user = sysUserService.getById(userId);
            if (user == null || user.getDeleted() == 1) {
                filterChain.doFilter(request, response);
                return;
            }

            // 加载角色和权限码
            List<String> roleIds = sysUserService.getRoleIdsByUserId(userId)
                    .stream().map(String::valueOf).collect(Collectors.toList());
            List<String> permCodes = sysUserService.getPermCodesByUserId(userId);

            List<SimpleGrantedAuthority> authorities = roleIds.stream()
                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                    .collect(Collectors.toList());
            permCodes.forEach(p -> authorities.add(new SimpleGrantedAuthority(p)));

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(user, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception ignored) {
            // token 无效或解析失败，继续过滤链（最终被 Spring Security 拦截）
        }

        filterChain.doFilter(request, response);
    }
}
```

**与原 ShiroAuthenticationFilter 对比：**

| 对比项 | ShiroAuthenticationFilter | JwtAuthenticationFilter |
|--------|--------------------------|-------------------------|
| 父类 | `OncePerRequestFilter`（shiro，`javax.servlet`） | `OncePerRequestFilter`（spring，`jakarta.servlet`） |
| 认证对象 | Shiro `Subject.login()` | Spring Security `SecurityContext` |
| 角色注入 | `info.addRoles(roleIds)` | `authorities.add(ROLE_xxx)` |
| 权限码注入 | `info.addStringPermissions(permCodes)` | `authorities.add(permCode)` |
| 失败处理 | 返回 401 JSON | 继续过滤链，由 SecurityFilterChain 处理 |

---

#### Step 4: 修改 AuthApplication.java

**移除：**
```java
// 删除 import
import org.apache.shiro.spring.boot.autoconfigure.ShiroBeanAutoConfiguration;

// 删除 exclude（Shiro 全部配置已移除）
exclude = {
    ShiroBeanAutoConfiguration.class,
    org.apache.shiro.spring.config.web.autoconfigure.ShiroWebAutoConfiguration.class,
    org.apache.shiro.spring.config.web.autoconfigure.ShiroWebFilterConfiguration.class,
    org.apache.shiro.spring.boot.autoconfigure.ShiroAutoConfiguration.class,
    org.apache.shiro.spring.boot.autoconfigure.ShiroAnnotationProcessorAutoConfiguration.class
}
```

**结果：**
```java
@SpringBootApplication(scanBasePackages = "com.dafuweng")
@EnableDiscoveryClient
@MapperScan("com.dafuweng.auth.dao")
public class AuthApplication { ... }
```

---

#### Step 5: 修改 application.yml

**删除：**
```yaml
shiro:
  enabled: false
```

---

#### Step 6: 自测验证

```
1. POST /api/sysUser/login 带 {username, password}
   - 成功返回 200 + user 信息
   - 失败返回 401

2. GET /api/sysUser/1/roles 不带 Token
   - 返回 401（Spring Security 默认行为）

3. GET /api/sysUser/1/roles 带正确 Bearer token
   - 返回 200 + 角色列表

4. 带错误 Token
   - 返回 403 Forbidden
```

**验收标准：**
- Spring Security Filter 链正常启动，无 `NoClassDefFoundError`
- 登录接口 `/api/sysUser/login` 和分页接口 `/api/sysUser/page` 免认证
- 其他接口带有效 Token 返回业务数据
- 其他接口不带 Token 或 Token 无效返回 401/403

---

### T02: AuthServiceImpl 密码验证改为 BCrypt

**模块：** auth
**优先级：** P0
**涉及文件：** AuthServiceImpl.java
**依赖：** T01（已完成）

**工作内容：**
- 注入 `BCryptPasswordEncoder`
- 登录验证改为 `passwordEncoder.matches(rawPassword, user.getPassword())`
- 将 `user.getDeleted() == 1` 改为 `Objects.equals(user.getDeleted(), (short) 1)`
- `changePassword` 使用 `passwordEncoder.encode(newPassword)`

**关键代码片段：**

```java
@Autowired
private BCryptPasswordEncoder passwordEncoder;

@Override
@Transactional
public SysUserEntity login(String username, String password, String loginIp) {
    SysUserEntity user = sysUserDao.selectByUsername(username);
    if (user == null) {
        throw new IllegalArgumentException("用户名或密码错误");
    }
    if (user.getLockTime() != null && user.getLockTime().after(new Date())) {
        throw new IllegalArgumentException("账号已锁定，请稍后再试");
    }
    if (Objects.equals(user.getDeleted(), (short) 1)) {
        throw new IllegalArgumentException("账号已删除");
    }
    if (!passwordEncoder.matches(password, user.getPassword())) {
        // ... 登录失败逻辑
        throw new IllegalArgumentException("用户名或密码错误");
    }
    // ... 登录成功逻辑
}
```

**状态：已实现** ✅

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
**涉及文件：** system/service/impl/SysParamServiceImpl.java, system/service/impl/SysDictServiceImpl.java, system/pom.xml
**依赖：** 无（需新增 spring-boot-starter-data-redis）

#### Step 1: 添加 Redis 依赖

system/pom.xml 中新增：

```xml
<!-- Redis 缓存 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

#### Step 2: 配置 Redis 连接

application.yml 中添加（如尚未配置）：

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      database: ${REDIS_DB:0}
      timeout: 5000
```

#### Step 3: 启用缓存注解

SystemApplication.java 添加 `@EnableCaching`：

```java
@SpringBootApplication(scanBasePackages = "com.dafuweng")
@EnableCaching
@MapperScan("com.dafuweng.system.dao")
public class SystemApplication { ... }
```

#### Step 4: SysParamServiceImpl 缓存实现

```java
@Service
public class SysParamServiceImpl implements SysParamService {

    @Autowired
    private SysParamDao sysParamDao;

    // 已有方法：getParamValue(String paramKey) — 新增 @Cacheable
    @Cacheable(value = "param", key = "#paramKey")
    public String getParamValue(String paramKey) {
        SysParamEntity entity = sysParamDao.selectByParamKey(paramKey);
        return entity != null ? entity.getParamValue() : null;
    }

    @CacheEvict(value = "param", key = "#paramKey")
    public void updateParam(String paramKey, String paramValue) {
        // ... 更新逻辑
    }

    @CacheEvict(value = "param", key = "#paramKey")
    public void deleteParam(String paramKey) {
        // ... 删除逻辑
    }
}
```

#### Step 5: SysDictServiceImpl 缓存实现

同样模式，对 `getDictValue(dictGroup, dictKey)` 等查询方法加 `@Cacheable`。

#### 缓存键设计

| 表 | 缓存 value | key 格式 | 过期时间 |
|----|-----------|----------|----------|
| sys_param | param | param:{paramKey} | 1小时 |
| sys_dict | dict | dict:{group}:{key} | 1小时 |

#### 自测验证

```
1. GET /api/sysParam/value/customer.public_sea_days
   - 首次访问：查 DB，缓存 miss
   - 再次访问：命中缓存（查看 debug 日志 cache hit）

2. PUT /api/sysParam（更新）
   - 验证缓存已 evict
   - 再次 GET 返回新值

3. Redis 不可用时：
   - 配置 RedisTemplate 设置 fallback 策略
   - 缓存异常不影响主流程（fallback to DB）
```

---

### T13: 数据权限拦截器

**模块：** common
**优先级：** P2
**涉及文件：** common/config/DataScopeInterceptor.java
**依赖：** T01（Spring Security SecurityContext 取 currentUserId + dataScope）

#### 技术方案变更说明

原方案使用 Shiro 的 `SecurityUtils.getSubject()`，现改为 Spring Security 的 `SecurityContextHolder`。当前认证上下文为：

```
JwtAuthenticationFilter.doFilterInternal()
  → SecurityContextHolder.getContext().setAuthentication(
      new UsernamePasswordAuthenticationToken(
          SysUserEntity (principal), null, authorities))
```

在任意 Service/Component 中通过以下方式获取当前用户：

```java
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
if (auth != null && auth.isAuthenticated()) {
    SysUserEntity user = (SysUserEntity) auth.getPrincipal();
    Long userId = user.getId();
    Short dataScope = user.getDataScope();
    Long deptId = user.getDeptId();
    Long zoneId = user.getZoneId();
}
```

#### 工作内容

```java
// common/config/DataScopeInterceptor.java
@Intercepts({
    @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
})
public class DataScopeInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 从 Spring Security SecurityContext 获取当前用户
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return invocation.proceed();  // 未登录，不过滤
        }

        Object principal = auth.getPrincipal();
        if (!(principal instanceof SysUserEntity)) {
            return invocation.proceed();
        }

        SysUserEntity user = (SysUserEntity) principal;
        Long userId = user.getId();
        Short dataScope = user.getDataScope();

        // 获取原始 SQL 并动态拼接数据权限条件
        String originalSql = ...;  // 拦截 SQL，动态拼接 WHERE 条件

        switch (dataScope != null ? dataScope : 4) {
            case 1: sql += " AND created_by = " + userId; break;      // 本人
            case 2: sql += " AND dept_id = " + user.getDeptId(); break;  // 本部门
            case 3: sql += " AND zone_id = " + user.getZoneId(); break;  // 本战区
            case 4: /* 不过滤 */ break;  // 全部
            default: /* 不过滤 */ break;
        }

        // 注意：需防止 SQL 注入，上述为示意。实际使用 LambdaQueryWrapper 或参数化查询。
        return invocation.proceed();
    }
}
```

#### 数据权限级别说明

| dataScope 值 | 名称 | 过滤条件 |
|-------------|------|----------|
| 1 | 本人 | `created_by = currentUserId` |
| 2 | 本部门 | `dept_id = currentUser.deptId` |
| 3 | 本战区 | `zone_id = currentUser.zoneId` |
| 4 | 全部 | 不过滤 |
| null | 默认 | 不过滤（安全默认值） |

#### 注册拦截器

在 common 模块的 Config 类中注册 MyBatis-Plus Interceptor：

```java
// common/src/main/java/com/dafuweng/common/config/MybatisPlusConfig.java
@Bean
public DataScopeInterceptor dataScopeInterceptor() {
    return new DataScopeInterceptor();
}
```

---

### T14: MetaObjectHandler 自动填充 createdBy/updatedBy

**模块：** common
**优先级：** P2
**涉及文件：** common/config/AutoFillMetaObjectHandler.java
**依赖：** T01（Spring Security SecurityContext）

#### 技术方案变更说明

同 T13，原使用 Shiro 的 `SecurityUtils.getSubject()`，现改为 Spring Security 的 `SecurityContextHolder`。

#### 工作内容

```java
// common/src/main/java/com/dafuweng/common/config/AutoFillMetaObjectHandler.java
@Component
public class AutoFillMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createdAt", Date.class, new Date());
        this.strictInsertFill(metaObject, "updatedAt", Date.class, new Date());
        Long userId = getCurrentUserId();
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
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()
                    && auth.getPrincipal() instanceof SysUserEntity) {
                return ((SysUserEntity) auth.getPrincipal()).getId();
            }
        } catch (Exception e) {
            // 无登录上下文（如定时任务、测试环境、Redis 缓存未命中导致匿名用户）
        }
        return null;
    }
}
```

#### 关键设计决策

1. **getCurrentUserId() 返回 null 是安全的**：`strictInsertFill` 在值为 null 时跳过填充，不会报错。这是正确的设计，因为定时任务和系统内部调用没有用户上下文。
2. **依赖顺序**：T13（DataScopeInterceptor）和 T14（AutoFillMetaObjectHandler）都依赖 T01 的 Spring Security 认证上下文建立后才能工作。
3. **SysUserEntity 需实现 UserDetails**：当前 `SysUserEntity` 作为 `UsernamePasswordAuthenticationToken` 的 principal 传入，需实现 Spring Security 的 `UserDetails` 接口（或强制转型）。建议在 `SysUserEntity` 中添加 `getAuthorities()` / `isAccountNonExpired()` 等方法的默认实现，避免 ClassCastException。

#### 自测验证

```
1. 插入操作（insertFill 验证）
   - 用有效 Bearer token 调用任意写接口
   - 查看数据库 created_by 字段是否为当前用户 id

2. 无 token 调用
   - 返回 401 或降级为 null 填充（created_by = null）
   - 验证系统不报错

3. 定时任务场景
   - PublicSeaTask 等无用户上下文调用
   - 验证 created_by = null 且无异常
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
