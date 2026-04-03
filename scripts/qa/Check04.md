# NeoCC 实施评估报告 — Check04

**评审时间：** 2026-04-03
**评审人：** 资深后端架构审查员
**评估对象：** implementsDetails01.md + 超前工作实施结果
**评估依据：** implementsDetails01.md、Plan03.md、Plan04.md 方案文档 vs 实际代码

---

## 一、总体评分

| 维度 | 得分 | 说明 |
|------|------|------|
| **Phase 1 完成度** | 9/10 | sales/finance/system CRUD 全部落地，106接口，框架规范 |
| **Plan03 执行** | 7/10 | common 重构完成，但 common 无 parent 导致版本管理失效 |
| **Plan04 执行** | 9/10 | auth Service/Controller 完整落地，29接口，偏离度小 |
| **超前工作质量** | 4/10 | gateway 仅有骨架，Nacos 依赖引入但未实际使用 |
| **代码质量** | 7/10 | IntelliJ 注释残留、依赖重复定义、gateway 测试路由 |
| **架构合规性** | 7/10 | 模块拆分合理，但 common 无 parent 引入版本管理问题 |

**综合评分：7.2 / 10**

**评级：B（良好，有多项需要注意的架构和质量问题）**

---

## 二、Phase 1 实施验收（implementsDetails01.md）

### 2.1 公共基础设施

| 计划项 | 路径（计划） | 实际路径 | 状态 |
|--------|------------|---------|------|
| Result.java | `common/.../common/entity/Result.java` | `common/.../common/entity/Result.java` | ✅ 含 error500 |
| GlobalExceptionHandler.java | `common/.../exception/GlobalExceptionHandler.java` | `common/.../exception/GlobalExceptionHandler.java` | ✅ 含 NPE handler |
| PageRequest.java | `common/.../entity/PageRequest.java` | `common/.../entity/PageRequest.java` | ✅ |
| PageResponse.java | `common/.../entity/PageResponse.java` | `common/.../entity/PageResponse.java` | ✅ 含 of() 工厂方法 |

**注：** common 模块路径从 `com.dafuweng.framework` 迁移到 `com.dafuweng.common`，符合 Plan03 要求。但 common 自身无 parent pom，这引入了新问题（见第三节）。

### 2.2 Sales 模块（7 Controller × 6 接口 = 42 接口）

| 检查项 | 计划 | 实际 | 状态 |
|--------|------|------|------|
| SalesApplication | `com.dafuweng.sales.SalesApplication` | `com.dafuweng.sales.SalesApplication` | ✅ |
| application.yml | 端口8083，DB dafuweng_sales | 端口8083，DB正确 | ✅ |
| Service 接口 | 7个 | Customer/ContactRecord/Contract/ContractAttachment/WorkLog/PerformanceRecord/CustomerTransferLog | ✅ |
| Controller | 7个 | 全部 REST 风格，Result<T> 统一响应 | ✅ |
| 接口总数 | 42 | 42+（部分额外接口如 checkDuplicate） | ✅ |

### 2.3 Finance 模块（6 Controller × 6 接口 = 36 接口）

| 检查项 | 计划 | 实际 | 状态 |
|--------|------|------|------|
| FinanceApplication | `com.dafuweng.finance.FinanceApplication` | `com.dafuweng.finance.FinanceApplication` | ✅ |
| application.yml | 端口8084，DB dafuweng_finance | 端口8084，DB正确 | ✅ |
| Service 接口 | 6个 | Bank/FinanceProduct/LoanAudit/LoanAuditRecord/ServiceFeeRecord/CommissionRecord | ✅ |
| Controller | 6个 | 全部 REST 风格 | ✅ |
| LoanAuditRecord | 仅3接口（无update/delete） | 实现正确（append-only） | ✅ |

### 2.4 System 模块（5 Controller，共 28 接口）

| 检查项 | 计划 | 实际 | 状态 |
|--------|------|------|------|
| SystemApplication | `com.dafuweng.system.SystemApplication` | `com.dafuweng.system.SystemApplication` | ✅ |
| application.yml | 端口8082，DB dafuweng_system | 端口8082，DB正确 | ✅ |
| Service 接口 | 5个 | SysZone/SysDepartment/SysParam/SysOperationLog/SysDict | ✅ |
| Controller | 5个 | 全部 REST 风格 | ✅ |
| SysOperationLog | 仅3接口（无update/delete） | 实现正确（append-only） | ✅ |

**Phase 1 验收结论：✅ 全部通过**

---

## 三、Plan03 执行验收（common 模块重构）

### 3.1 模块结构变更

| 检查项 | Plan03 要求 | 实际 | 状态 |
|--------|------------|------|------|
| 根 pom modules | 移除 framework | 无 framework 模块 | ✅ |
| common 打包类型 | `<packaging>jar</packaging>` | 无 `<packaging>`（默认jar） | ✅ |
| framework 目录 | 删除 | 已删除 | ✅ |
| common Java 类路径 | `com.dafuweng.common.*` | `com.dafuweng.common.*` | ✅ |
| import 路径统一 | 全部改为 common | 全部从 common 引用 | ✅ |

### 3.2 严重问题：common 无 parent 导致版本管理失效

**问题：** `common/pom.xml` 既没有继承 `spring-boot-starter-parent`，也没有 `<dependencyManagement>`。所有依赖均以硬编码版本直接声明：

```xml
<!-- common/pom.xml 当前状态 -->
<dependencies>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-tx</artifactId>
        <version>6.1.6</version>    <!-- 硬编码版本 -->
    </dependency>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-web</artifactId>
        <version>6.1.6</version>    <!-- 硬编码版本 -->
    </dependency>
</dependencies>
```

**影响：**
- Spring Boot 3.2.5 自带 spring-web 6.1.5，common 强制引入 6.1.6，可能产生类冲突
- mybatis-plus-core 3.5.7 由 common 管理，但 sales/finance/system/auth 各自也在 `<dependencyManagement>` 中重复声明版本
- 版本管理分散在 5 个模块中，维护成本高，潜在版本不一致风险

**Plan03 原始方案（推荐）：** common 继承 `spring-boot-starter-parent`，使用 `<dependencyManagement>` 统一版本，业务模块继承 common。

**实际执行方案：** 业务模块继承 `spring-boot-starter-parent` 直接管理版本，common 只作为共享类 jar。

**评估：** 方案可行，但不够干净。版本管理碎片化问题会随模块增加而加剧。

### 3.3 架构偏差

| 偏差 | 严重度 | 说明 |
|------|--------|------|
| common 无 parent | 中 | 版本管理碎片化，潜在冲突风险 |
| sales/pom.xml 重复 spring-web | 低 | 同时引入 starter-web（内含 transitive spring-web）和显式 spring-web:6.1.6 |

---

## 四、Plan04 执行验收（auth 模块 Service/Controller）

### 4.1 P0 Bug 修复

| 问题 | Plan04 要求 | 实际 | 状态 |
|------|------------|------|------|
| SysUserEntity @TableName | 修正为 `"sys_user"` | `@TableName("sys_user")` | ✅ |
| application.yml mapper-locations | 修正为 `classpath:auth/mapper/*.xml` | `classpath:auth/mapper/*.xml` | ✅ |
| type-aliases-package | 修正为 `com.dafuweng.auth.entity` | `com.dafuweng.auth.entity` | ✅ |

### 4.2 Service 层

| 检查项 | Plan04 要求 | 实际 | 状态 |
|--------|-----------|------|------|
| SysUserService 方法数 | 13个 | 13个 | ✅ |
| SysRoleService 方法数 | 8个 | 8个 | ✅ |
| SysPermissionService 方法数 | 8个 | 8个 | ✅ |
| @Transactional 标注 | save/update/delete/assignRoles 等 | 全部标注 | ✅ |
| 登录锁定机制 | 5次错误锁定30分钟 | 实现正确（Calendar.MINUTE, 30） | ✅ |
| getPermCodesByUserId | 遍历角色汇总权限码 | 实现正确 | ✅ |

### 4.3 Controller 层

| Controller | Plan04 端点数 | 实际端点数 | 状态 |
|------------|------------|----------|------|
| SysUserController | 12 | 12 | ✅ |
| SysRoleController | 8 | 8 | ✅ |
| SysPermissionController | 9 | 9 | ✅ |
| **合计** | **29** | **29** | ✅ |

### 4.4 已知偏离项

| 偏离项 | Plan04 要求 | 实际 | 严重度 |
|--------|-----------|------|--------|
| 密码加密 | BCryptPasswordEncoder | 明文比对 | 已接受（用户指令跳过） |
| treeList | 嵌套树形结构 | 扁平列表 | 中（Check03 已记录） |

### 4.5 新发现问题

**AuthApplication.java 含开发残留注释：**
```java
//TIP 要<b>运行</b>代码，请按 <shortcut actionId="Run"/> 或
// 点击装订区域中的 <icon src="AllIcons.Actions.Execute"/> 图标。
```

**影响：** IDE 生成的模板注释，生产代码不应包含此类开发提示。

---

## 五、超前工作评估（超出 implementsDetails01.md 范围）

### 5.1 gateway 模块

| 检查项 | 状态 | 说明 |
|--------|------|------|
| pom.xml | ✅ | 包含 spring-cloud-starter-gateway + nacos-discovery |
| application.yml | ⚠️ | 只有测试路由到 bilibili.com，无实际服务路由 |
| GatewayApplication | ✅ | 主启动类正常 |
| AuthFilter | ❌ | 不存在 |
| CorsConfig | ❌ | 不存在 |
| 实际路由规则 | ❌ | 无 `/api/auth/**` → `lb://auth` 等 |

**gateway 实际只做了 POM 依赖引入 + 一个测试路由，不能算作"超前完成 gateway 工作"。**

### 5.2 Spring Cloud Alibaba 依赖引入

| 检查项 | 状态 | 说明 |
|--------|------|------|
| Nacos Discovery | ⚠️ | pom.xml 声明依赖，application.yml 配置了 discovery | 但代码中无 `@EnableDiscoveryClient` 实际生效 |
| gateway nacos 配置 | ⚠️ | 配置了 namespace，但无服务注册/发现实际运行证据 |

**所有模块 pom.xml 均已引入 `spring-cloud-starter-alibaba-nacos-discovery`，但代码中无任何使用 Nacos 服务发现的实际逻辑。依赖引入不等于功能实现。**

### 5.3 超前工作评分

**评分：4/10**

理由：gateway 模块仅有骨架，无实际路由；Nacos 依赖引入但无实际使用。这不算完成了"下一阶段工作"，只是搭了个架子。

---

## 六、代码质量

### 6.1 做得好

1. **MyBatis-Plus 最佳实践** — LambdaQueryWrapper 防注入、@TableLogic 逻辑删除、@Version 乐观锁全部正确使用
2. **RESTful 规范** — GET/POST/PUT/DELETE 语义正确，Result<T> 统一响应
3. **@Transactional 覆盖完整** — 所有写操作均标注事务
4. **分页实现标准** — PageRequest + PageResponse + IPage 模式正确
5. **登录锁定机制完整** — 错误计数、30分钟自动解锁、错误信息模糊化
6. **Phase 1 无编译错误** — 所有模块 mvn compile 通过

### 6.2 需要关注的问题

| # | 问题 | 严重度 | 说明 |
|---|------|--------|------|
| 1 | **IntelliJ TIP 注释残留** | 低 | AuthApplication.java 含 IDE 模板注释，生产代码应删除 |
| 2 | **gateway 测试路由** | 低 | gateway 路由到 bilibili.com，不是实际服务，且 `/api/sys/**` 等路径前缀混乱 |
| 3 | **sales/pom.xml 重复依赖** | 低 | 同时引入 spring-boot-starter-web 和显式 spring-web:6.1.6 |
| 4 | **common 无 parent** | 中 | 版本管理碎片化，未来维护成本高 |
| 5 | **treeList 扁平列表** | 中 | Plan04 偏离，前端需自行构建树（Check03 已记录） |
| 6 | **密码明文存储** | 高（安全） | BCrypt 延期，但明文密码已上线存在安全隐患（用户已知） |
| 7 | **无参数校验** | 低 | Controller 缺少 @Valid/@Validated，非法参数直接进 Service |
| 8 | **无操作日志** | 低 | 敏感操作（login/logout/assignRoles）无审计日志 |
| 9 | **gateway main web type** | 低 | gateway application.yml 设置 `main.web-application-type: reactive`，与 `@EnableDiscoveryClient` 混用可能有问题 |
| 10 | **未使用 common-jar** | 低 | common-jar 模块已从 pom.xml modules 中移除，但 Plan03 讨论中曾作为备选方案；不影响功能，仅说明文档可能过时 |

---

## 七、甲方要求对照

基于 `甲方要求.md`（大富翁贷款公司业务需求）：

| 业务需求 | 对应模块 | 实现状态 |
|---------|---------|---------|
| 销售部客户管理、洽谈、合同 | sales 模块 | ✅ CRUD 完整 |
| 合同签署、服务费 | sales + finance | ✅ 接口就绪 |
| 金融部贷款审核、发放 | finance 模块 | ✅ CRUD 完整 |
| 部门管理、战区 | system 模块 | ✅ CRUD 完整 |
| 系统参数、数据字典 | system 模块 | ✅ CRUD 完整 |
| 操作日志 | system 模块 | ✅ 查询接口完整 |
| 统一认证授权 | auth 模块 | ⚠️ 接口完成，认证机制未实现（明文密码） |
| API 网关 | gateway 模块 | ⚠️ 仅有骨架，无实际路由 |

**业务核心流程支撑度：75%**

说明：CRUD 接口均已落地，但跨模块调用（OpenFeign）、事件驱动（RabbitMQ）、统一认证（Shiro/JWT）尚未实现，完整业务流程尚不能跑通。

---

## 八、综合评级

| 维度 | Plan03 | Plan04 | Phase 1 | 超前工作 | 综合 |
|------|--------|--------|---------|---------|------|
| 功能完整性 | 7/10 | 9/10 | 9/10 | 4/10 | 7.2/10 |
| 代码质量 | 7/10 | 8/10 | 8/10 | 4/10 | 7.0/10 |
| 架构合规 | 7/10 | 9/10 | 9/10 | 3/10 | 7.5/10 |
| 技术合规 | 8/10 | 8/10 | 8/10 | 5/10 | 7.5/10 |
| 编译验证 | 9/10 | 10/10 | 10/10 | 8/10 | 9.3/10 |

**综合评分：7.2 / 10**
**总体评级：B（良好）**

---

## 九、结论与建议

### 9.1 结论

**Phase 1 实施质量评定：优秀**

implementsDetails01.md 规定的所有内容均已完成且质量良好：
- ✅ 3个业务模块（sales/finance/system）CRUD 全部落地
- ✅ 约106个 REST API 端点
- ✅ common 模块重构完成（framework 消除）
- ✅ Plan04 auth 模块超额完成（29接口）
- ✅ 全模块编译通过

**Plan04 实施质量评定：优秀**

auth 模块 Service/Controller 完整实现，29个 API 端点全部落地，登录锁定机制完整，两个已知偏离项均为已记录的可接受偏差。

**超前工作评定：不及格**

gateway 模块仅有 POM 依赖和测试路由，不算完成 gateway 功能。Nacos 依赖引入但无实际使用。不应计入工作完成量。

### 9.2 立即处理项

| 优先级 | 问题 | 影响 | 处理方案 |
|--------|------|------|---------|
| P0 | common 无 parent | 版本管理碎片，潜在冲突 | 建议 common 补充继承 spring-boot-starter-parent，清理硬编码版本 |
| P1 | gateway 测试路由 | 请求无法路由到实际服务 | 补充真实路由规则：auth→8081, system→8082, sales→8083, finance→8084 |
| P2 | AuthApplication TIP 注释 | 代码规范性 | 删除 IDE 模板注释 |

### 9.3 遗留清单（不在本次实施范围）

| 问题 | 优先级 | 说明 |
|------|--------|------|
| 密码明文（BCrypt） | Phase 2 | 用户已知，已接受明文方案 |
| treeList 树形结构 | Phase 2 | 前端可接受扁平列表 |
| @Valid 参数校验 | Phase 2 | Controller 补充校验注解 |
| 操作审计日志 | Phase 2 | AOP 切面记录敏感操作 |
| OpenFeign 跨模块调用 | Phase 2 | sales↔finance 等跨库调用 |
| RabbitMQ 事件驱动 | Phase 2 | 合同签署→金融部通知 |
| Nacos 配置中心 | Phase 2 | 统一配置管理 |
| DataScopeInterceptor | Phase 2 | 数据权限拦截器 |

### 9.4 下一步建议

1. **立即（本次）：** gateway 补充真实路由规则，清理 AuthApplication 注释，修复 common 版本管理问题
2. **Phase 2 前置：** 制定 Phase 2 详细实施方案（auth 认证完善、gateway 路由、OpenFeign、RabbitMQ）
3. **Phase 2：** 按方案依次实施剩余功能

---

## 十、文件变更清单（总览）

| 模块 | 新建文件 | 修改文件 | 状态 |
|------|---------|---------|------|
| common | 4个（Result/PageRequest/PageResponse/GlobalExceptionHandler） | common/pom.xml | ✅ |
| sales | 14个（7 Service + 7 Controller + impl） + application.yml | pom.xml, SalesApplication.java | ✅ |
| finance | 12个（6 Service + 6 Controller + impl） + application.yml | pom.xml, FinanceApplication.java | ✅ |
| system | 10个（5 Service + 5 Controller + impl） + application.yml | pom.xml, SystemApplication.java | ✅ |
| auth | 9个（3 Service + 3 Controller + impl） + AuthApplication + pom.xml | SysUserEntity @TableName | ✅ |
| gateway | 3个（GatewayApplication + pom.xml + application.yml） | — | ⚠️ 仅骨架 |

**注：** 总计约 52 个新文件，质量整体良好。
