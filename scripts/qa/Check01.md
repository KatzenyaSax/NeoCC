# QA 检查报告：NeoCC 项目实施就绪评估

**检查时间**：2026-04-01
**检查人**：gstack /qa skill
**评估文件**：`implementDetails.md`
**当前代码库**：NeoCC（Maven 多模块项目）

---

## 一、总体结论

**结论**：后端核心服务（sales、finance、system）骨架已具备，但 **gateway 和 auth 模块缺失**，且 implementDetails.md 中描述的完整技术栈（Nacos、Sentinel、OpenFeign、Shiro、RabbitMQ）在当前代码库中 **完全未引入**。当前代码库状态与实施方案存在重大差距，**不能直接开始实施**，需先完成架构对齐。

---

## 二、现有模块评估

### 2.1 common 模块 — 部分就绪

| 检查项 | 状态 | 说明 |
|--------|------|------|
| POM 继承关系 | ✅ | 继承 spring-boot-starter-parent:3.2.5 |
| dependencyManagement | ✅ | 已配置 mybatis-plus:3.5.7、mysql-connector-j:8.3.0 |
| lombok | ✅ | optional:true，符合可复用设计 |
| 实际内容 | ⚠️ | 仅有 pom.xml，无具体 Java 代码 |

**结论**：骨架正确，可作为依赖管理中心。

---

### 2.2 sales 模块 — 基本就绪

| 检查项 | 状态 | 说明 |
|--------|------|------|
| Spring Boot 引入 | ✅ | spring-boot-starter-web 已引入 |
| MyBatis-Plus | ✅ | mybatis-plus-spring-boot3-starter 已引入 |
| application.yml | ✅ | 已配置数据源、mybatis-plus 全局配置 |
| MetaObjectHandler | ❌ | implementDetails.md 要求的自动填充（创建时间、更新时间）未实现 |
| JacksonTypeHandler | ❌ | implementDetails.md 要求的 JSON 类型处理器（customer.annotation）未实现 |
| @MapperScan | ⚠️ | 存在，但 SalesApplication.java 包路径存疑 |

**关键问题**：当前 `SalesApplication.java` 位于 `com.dafuweng` 包下（而非 `com.dafuweng.sales`），与 implementDetails.md 中描述的包结构不一致。

**结论**：可启动，但缺少自动填充和 JSON 处理能力。

---

### 2.3 finance 模块 — 仅有目录骨架

| 检查项 | 状态 | 说明 |
|--------|------|------|
| pom.xml | ✅ | 存在，内容待确认 |
| src 结构 | ⚠️ | 存在 src/ 目录，内容未验证 |
| Spring Boot 依赖 | ❌ | 未引入任何 Spring Boot 依赖 |
| MyBatis-Plus | ❌ | 未引入 |

**结论**：不能启动，依赖关系未建立。

---

### 2.4 system 模块 — 仅有目录骨架

| 检查项 | 状态 | 说明 |
|--------|------|------|
| pom.xml | ✅ | 存在，内容待确认 |
| src 结构 | ⚠️ | 存在 src/ 目录，内容未验证 |
| Spring Boot 依赖 | ❌ | 未引入任何 Spring Boot 依赖 |
| MyBatis-Plus | ❌ | 未引入 |

**结论**：不能启动，依赖关系未建立。

---

## 三、缺失模块（关键阻断项）

### 3.1 gateway 模块 — 完全缺失 ❌

| 检查项 | 状态 |
|--------|------|
| 目录存在 | ❌ 不存在 |
| POM 存在 | ❌ 不存在 |
| 代码存在 | ❌ 不存在 |

implementDetails.md 第 3 章要求 `dafuweng-gateway` 作为 Spring Cloud Gateway 网关，负责 token 校验和路由转发（`lb://dafuweng-sales`）。**当前代码库中无任何 gateway 痕迹。**

**影响**：微服务间通信和统一入口无法建立。

---

### 3.2 auth 模块 — 完全缺失 ❌

| 检查项 | 状态 |
|--------|------|
| 目录存在 | ❌ 不存在 |
| POM 存在 | ❌ 不存在 |
| Shiro 依赖 | ❌ 不存在 |

implementDetails.md 第 4 章要求 `dafuweng-auth` 作为认证中心，使用 Shiro 进行密码加密（BCrypt）和 JWT 签发。**当前代码库中无任何 auth 模块。**

**影响**：用户认证体系无法建立。

**备注**：implementDetails.md 自身存在 P0 级错误——文档中写的是 SHA-256 加密，但正确做法应为 BCrypt。

---

## 四、技术栈对齐分析

### 4.1 implementDetails.md 描述的技术栈 vs 当前状态

| 组件 | implementDetails.md | 当前代码库 | 状态 |
|------|-------------------|-----------|------|
| Spring Boot | 3.1.3 | 3.2.5 | ⚠️ 版本差异 |
| Spring Cloud Alibaba | 2022.0.0.0 | 未引入 | ❌ |
| Nacos | 配置中心 | 未引入 | ❌ |
| OpenFeign | 服务调用 | 未引入 | ❌ |
| Sentinel | 流控 | 未引入 | ❌ |
| RabbitMQ | 异步事件 | 未引入 | ❌ |
| Shiro | 认证 | 未引入 | ❌ |
| MyBatis-Plus | 3.5.7 | 3.5.7 | ✅ |
| MySQL | 8.x | 8.x | ✅ |

**结论**：基础框架（Spring Boot + MyBatis-Plus + MySQL）可用，但微服务治理组件（80% 的核心能力）完全缺失。

---

## 五、架构设计问题

### 5.1 Root POM 违背 Plan02 设计原则

**问题**：Root POM（`pom.xml`）中直接声明了依赖：

```xml
<dependencies>
    <dependency>
        <groupId>org.mybatis</groupId>
        <artifactId>mybatis-spring</artifactId>
        <version>3.0.3</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot</artifactId>
        <version>3.2.5</version>
    </dependency>
    ...
</dependencies>
```

**Plan02 原文要求**："root pom 仅用于标识子 module，不参与实际的依赖关系。"

当前 root pom 既是 aggregator（通过 modules），又直接管理依赖，与 Plan02 原则冲突。

---

### 5.2 模块命名不一致

implementDetails.md 使用 `dafuweng-*` 前缀：

- `dafuweng-common`
- `dafuweng-auth`
- `dafuweng-sales`
- `dafuweng-finance`
- `dafuweng-system`
- `dafuweng-gateway`

实际代码库使用：

- `common`
- `sales`
- `finance`
- `system`

**影响**：文档与代码不匹配，团队协作时会产生歧义。

---

## 六、可 deferred 项目（用户已授权搁置）

以下配置在 implementDetails.md 中未给出具体值，用户已授权暂时搁置：

| 项目 | 搁置原因 |
|------|----------|
| Nacos 用户名/密码 | 需额外资源申请 |
| 服务 namespace | 需 Nacos 就绪后才能确定 |
| 数据库正式密码 | 需 DBA 配置 |
| RabbitMQ 凭证 | 需运维配置 |

---

## 七、行动建议

### 7.1 必须先完成（阻断实施）

1. **补充缺失模块**：至少需要创建 `gateway` 和 `auth` 模块骨架
2. **引入 Spring Cloud Alibaba BOM**：在 common 或 root pom 中引入 2022.0.0.0 BOM
3. **引入 Nacos、OpenFeign、Sentinel 依赖**：至少在对应模块的 pom.xml 中声明
4. **修正 root pom**：移除直接依赖声明，回归 Plan02 纯 aggregator 设计
5. **统一模块命名**：选择 `dafuweng-*` 或当前命名，保持一致

### 7.2 建议完成（提升质量）

1. **sales 模块**：实现 `MetaObjectHandler` 自动填充、JacksonTypeHandler JSON 处理
2. **finance/system 模块**：补充 Spring Boot 和 MyBatis-Plus 依赖
3. **SalesApplication.java**：修正包路径至 `com.dafuweng.sales`
4. **文档修正**：implementDetails.md 中的 SHA-256 改为 BCrypt（auth 模块）

---

## 八、最终评级

| 维度 | 评分 | 说明 |
|------|------|------|
| sales 模块就绪度 | 6/10 | 基础可用，缺少自动填充和 JSON 处理 |
| finance 模块就绪度 | 2/10 | 仅有目录骨架 |
| system 模块就绪度 | 2/10 | 仅有目录骨架 |
| common 模块就绪度 | 7/10 | 骨架正确，内容待充实 |
| gateway 模块 | 0/10 | 完全缺失 |
| auth 模块 | 0/10 | 完全缺失 |
| 技术栈完整性 | 2/10 | 微服务组件 80% 缺失 |
| 文档代码一致性 | 3/10 | 命名、版本、组件多处不一致 |

**综合结论**：**暂不具实施条件**。需先完成架构对齐，再按 implementDetails.md 推进。
