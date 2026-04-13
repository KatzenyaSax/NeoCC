# Plan08: Gateway 循环依赖问题解决方案

## 问题描述

Gateway 服务启动失败，错误信息：

```
APPLICATION FAILED TO START
***************************
Description:
The dependencies of some of the beans in the application context form a cycle:
┌─────┐
|  authFilter (field private AuthFeignClient)
↑     ↓
|  AuthFeignClient
↑     ↓
|  corsGatewayFilterApplicationListener
↑     ↓
|  routePredicateHandlerMapping
↑     ↓
|  filteringWebHandler
└─────┘
```

## 根因分析

```
AuthFilter (implements GlobalFilter)
    │
    ├── @Autowired AuthFeignClient  ← 直接注入触发 Feign 上下文初始化
    │
    ▼
AuthFeignClient (@FeignClient)
    │
    ├── Feign 上下文与 Spring Gateway 上下文冲突
    │
    ▼
Spring Cloud Gateway 内部组件
    │
    ├── filteringWebHandler: 排序所有 GlobalFilter
    ├── routePredicateHandlerMapping: 路由映射
    └── corsGatewayFilterApplicationListener: CORS 配置
```

**核心问题**: `AuthFilter` 是 `GlobalFilter`，在 Spring Cloud Gateway 初始化时会被 `filteringWebHandler` 排序。此时 Feign Client 的上下文也在初始化，而 Feign Client 依赖 Gateway 的一些配置类，导致循环。

## 约束条件

- **pom 结构不可更改**：历史原因，无法调整依赖
- 只能通过代码层面修改解决

## 解决方案

### 推荐方案：`@Lazy` + `ObjectProvider` 双重保险

**修改文件**: `gateway/src/main/java/com/dafuweng/gateway/filter/AuthFilter.java`

```java
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    // 方案A: 使用 ObjectProvider 延迟获取
    @Autowired
    private ObjectProvider<AuthFeignClient> authFeignClientProvider;

    // 方案B: 配合 @Lazy 注解
    @Autowired
    @Lazy
    private AuthFeignClient authFeignClient;

    private static final String AUTH_HEADER = "Authorization";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ROLES_HEADER = "X-User-Roles";

    // 获取 FeignClient 的安全方法
    private AuthFeignClient getAuthFeignClient() {
        // ObjectProvider 的 getObject() 会在首次调用时才真正获取 bean
        // 此时 Spring 上下文已完全初始化，不会有循环依赖问题
        try {
            return authFeignClientProvider.getObject();
        } catch (Exception e) {
            // 兜底：如果 ObjectProvider 获取失败，使用 @Lazy 的代理
            return authFeignClient;
        }
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // ... 现有逻辑 ...

        // 调用 auth 服务验证用户存在且有效
        try {
            // 使用安全方法获取 FeignClient
            AuthFeignClient client = getAuthFeignClient();
            Result<?> userResult = client.getUserById(userId);
            // ... 其余逻辑 ...
        } catch (Exception e) {
            // ... 错误处理 ...
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
```

### 备选方案：仅使用 `@Lazy`（简单场景）

如果 ObjectProvider 方案过于复杂，可退化为仅使用 `@Lazy`：

```java
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    @Autowired
    @Lazy
    private AuthFeignClient authFeignClient;  // 启动时不注入，调用时才创建代理

    // ... 其余代码不变 ...
}
```

**缺点**: `@Lazy` 只能解决启动时的循环依赖，如果运行时 Feign Client 还未完全初始化，可能有潜在问题。

### 备选方案：使用 `ApplicationContext` 手动获取

```java
@Component
public class AuthFilter implements GlobalFilter, Ordered, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext context) {
        this.applicationContext = context;
    }

    private AuthFeignClient getAuthFeignClient() {
        return applicationContext.getBean(AuthFeignClient.class);
    }

    // ... 其余代码不变 ...
}
```

**缺点**: 违反依赖注入原则，增加耦合度。

## 依赖关系图（修复后）

```
启动阶段:
  Spring Context 初始化
       │
       ├── AuthFilter 注册为 Bean (不立即注入 AuthFeignClient)
       │
       └── @Lazy/ObjectProvider 标记 AuthFeignClient 为延迟加载
              │
              ▼
       filteringWebHandler 排序 GlobalFilter (此时 AuthFeignClient 未加载)
              │
              ▼
       Spring Context 完成初始化
              │
              ▼
运行时 (首次请求):
       AuthFilter.filter() 被调用
              │
              ▼
       getAuthFeignClient() → 真正获取 AuthFeignClient 代理
              │
              ▼
       Feign 调用远程 auth 服务
```

## 实施步骤

| 步骤 | 操作 | 文件 | 风险 |
|------|------|------|------|
| 1 | 备份当前 AuthFilter.java | - | 无 |
| 2 | 添加 `ObjectProvider<AuthFeignClient>` 字段 | AuthFilter.java | 低 |
| 3 | 添加 `@Lazy` + `AuthFeignClient` 字段作为兜底 | AuthFilter.java | 低 |
| 4 | 添加 `getAuthFeignClient()` 安全获取方法 | AuthFilter.java | 低 |
| 5 | 将所有 `authFeignClient.` 调用替换为 `getAuthFeignClient().` | AuthFilter.java | 中 (需改 2 处) |
| 6 | 启动验证 | GatewayApplication | - |

## 验证方案

1. **启动验证**: `mvn spring-boot:run -pl gateway` 应成功启动
2. **功能验证**: 调用需要认证的 API，确认 AuthFilter 正常调用 auth 服务
3. **日志验证**: 观察 Feign Client 的初始化日志

## 风险评估

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| @Lazy 代理在高并发下首次访问慢 | 低 | ObjectProvider 预热机制 |
| Feign Client 网络超时导致请求失败 | 中 | AuthFilter已有超时处理逻辑 |
| pom 依赖冲突未发现 | 高 | 需完整测试 |

## NOT in Scope

- pom 依赖调整（明确不可更改）
- 其他 Gateway Filter 的修改
- Auth 服务本身的改动

## What Already Exists

- `AuthFilter.java` 已实现完整的 Token 验证逻辑
- `AuthFeignClient.java` 已正确定义 Feign 接口
- `GatewayApplication.java` 已正确配置 `@EnableFeignClients`

## 总结

**推荐方案**: `ObjectProvider + @Lazy` 双重保险

- 最小改动：只改 AuthFilter.java 一个文件
- 向后兼容：不影响现有业务逻辑
- 启动安全：`ObjectProvider` 确保上下文完成后才获取 Bean
- 运行安全：`@Lazy` 作为兜底防止 ObjectProvider 异常

