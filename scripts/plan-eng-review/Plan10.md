# Plan10: Gateway 跨域与路径问题解决方案

## 问题描述

Plan07.md 识别出 Gateway 模块存在以下未解决缺陷：

| 问题 | 严重度 | 说明 |
|------|--------|------|
| AuthFilter 登录路径检查永远不匹配 | P0 | `path.contains("/api/sysUser/login")` 但实际路径是 `/auth/api/sysUser/login` |
| AuthFeignClient 路径错误 | P0 | `@GetMapping("/auth/api/sysUser/{id}")` 应为 `/api/sysUser/{id}` |
| CorsConfig 未实现 | P0 | 跨域配置缺失，前端请求被浏览器拦截 |

## 约束条件

- **pom 结构不可更改**：历史原因，无法调整依赖
- 只能通过代码层面修改解决

---

## 问题一：AuthFilter 登录路径检查失效

### 根因分析

```
请求流程:
  GET /auth/api/sysUser/login
         │
         ▼
  AuthFilter.filter() 收到 path = "/auth/api/sysUser/login"
         │
         ▼  ← StripPrefix=1 在路由转发时才生效，AuthFilter 在此时看到的是原始路径
  path.contains("/api/sysUser/login") → FALSE (因为包含 /auth/)
         │
         ▼
  AuthFilter 尝试 Token 校验 → 所有登录请求被拦截
```

当前 `application.yml` 中 `auth-route` 配置：
```yaml
- id: auth-route
  uri: lb://auth
  predicates:
    - Path=/auth/**
  filters:
    - StripPrefix=1   # 只在路由转发时生效，不影响 GlobalFilter
```

`StripPrefix=1` 在 `filteringWebHandler` 将请求转发到 `auth-route` 时才生效，**不影响** `AuthFilter`（GlobalFilter）在路由匹配阶段看到的原始 path。

AuthFilter 需要跳过登录路径，但当前检查的是 `/api/sysUser/login`（服务内部路径），而实际 incoming path 是 `/auth/api/sysUser/login`（网关入口路径）。

### 修复方案

**修改文件**: `gateway/src/main/java/com/dafuweng/gateway/filter/AuthFilter.java:50`

```java
// 修复前
if (path.contains("/api/sysUser/login") || path.contains("/api/sysUser/page")) {
    return chain.filter(exchange);
}

// 修复后
if (path.contains("/auth/api/sysUser/login") || path.contains("/auth/api/sysUser/page")) {
    return chain.filter(exchange);
}
```

**说明**: 检查网关入口路径 `/auth/api/sysUser/login`（带 `/auth` 前缀），与实际 incoming path 匹配。

---

## 问题二：AuthFeignClient 路径错误

### 根因分析

```
application.yml 路由配置:
  /auth/** → lb://auth + StripPrefix=1
       │
       ▼ (StripPrefix 去除 /auth)
  实际转发到 auth 服务: /api/sysUser/{id}

AuthFeignClient 当前路径:
  @GetMapping("/auth/api/sysUser/{id}")   ← 错误：带了 /auth 前缀

正确路径应为:
  @GetMapping("/api/sysUser/{id}")        ← 服务内部路径（无 /auth 前缀）
```

`@FeignClient` 定义的是**服务内部调用路径**，不等同于网关入口路径。网关路由 `StripPrefix=1` 会剥除 `/auth` 前缀再转发，所以 Feign 客户端应直接使用服务内部路径。

### 修复方案

**修改文件**: `gateway/src/main/java/com/dafuweng/gateway/feign/AuthFeignClient.java`

```java
// 修复前
@GetMapping("/auth/api/sysUser/{id}")
Result<?> getUserById(@PathVariable Long id);

@GetMapping("/auth/api/sysUser/{id}/roles")
Result<?> getRoleIds(@PathVariable Long id);

@GetMapping("/auth/api/sysUser/{id}/permCodes")
Result<?> getPermCodes(@PathVariable Long id);

// 修复后
@GetMapping("/api/sysUser/{id}")
Result<?> getUserById(@PathVariable Long id);

@GetMapping("/api/sysUser/{id}/roles")
Result<?> getRoleIds(@PathVariable Long id);

@GetMapping("/api/sysUser/{id}/permCodes")
Result<?> getPermCodes(@PathVariable Long id);
```

**说明**: Feign Client 路径 = 服务内部实际路径 = 网关转发后的路径（StripPrefix 剥除 `/auth` 后）。

---

## 问题三：CorsConfig 未实现

### 根因分析

浏览器同源策略会阻止前端 AJAX 请求到不同端口的服务。前端通常运行在 80/443 端口，Gateway 在 8086，auth 在 8081 等。没有 CORS 配置，所有跨域请求被浏览器拦截。

Spring Cloud Gateway 提供两种 CORS 配置方式：
1. `application.yml` 声明式配置（推荐）
2. Java 配置类 `CorsConfigurationSource`

### 修复方案

**修改文件**: `gateway/src/main/resources/application.yml`

在 `spring.cloud.gateway` 下添加 global CORS 配置：

```yaml
spring:
  cloud:
    gateway:
      # 现有路由配置保持不变...
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins: "http://localhost:3000"
            allowedMethods:
              - GET
              - POST
              - PUT
              - DELETE
              - OPTIONS
            allowedHeaders: "*"
            allowCredentials: true
            maxAge: 3600
```

**备选方案**（Java Config）:

如需更细粒度控制，创建 `gateway/src/main/java/com/dafuweng/gateway/config/CorsConfig.java`：

```java
package com.dafuweng.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.util.pattern.PathPatternCollection;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        config.setMaxAge(3600L);
        config.setAllowedMethods(Arrays.asList(HttpMethod.GET.name(), HttpMethod.POST.name(),
                HttpMethod.PUT.name(), HttpMethod.DELETE.name(), HttpMethod.OPTIONS.name()));
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);

        org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource source =
            new org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource();
        source.setCorsConfigurations(PathPatternCollection.parse(""));
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
```

**推荐**: 使用 `application.yml` 方式，配置简单且符合 Spring Cloud Gateway 规范。

---

## 依赖关系图（完整请求流程）

```
修复后请求流程:
─────────────────────────────────────────────────────────────

[前端]  GET /auth/api/sysUser/login  (Origin: http://localhost:3000)
          │
          ▼
[GLOBAL CORS] 检查 Origin → 允许则放行 (预检 OPTIONS 请求也通过)
          │
          ▼
[AuthFilter] 检查 path = "/auth/api/sysUser/login"
          │  contains("/auth/api/sysUser/login") → TRUE
          │  跳过 Token 校验，直接 chain.filter(exchange)
          ▼
[StripPrefix=1] 剥除 /auth 前缀
          │
          ▼
[auth 服务] 收到 /api/sysUser/login
          │
          ▼
[响应返回前端]

─────────────────────────────────────────────────────────────

[前端]  GET /sales/api/customer/page  (带 Authorization: Bearer {userId})
          │
          ▼
[AuthFilter] 检查 path = "/sales/api/customer/page"
          │  不匹配跳过条件
          │  提取 Bearer token → userId
          ▼
[FeignClient] GET /api/sysUser/{userId}
          │  (注意：路径无 /auth 前缀，因为 StripPrefix 已剥除)
          ▼
[auth 服务] 返回用户信息
          │
          ▼
[AuthFilter] 验证通过，添加 X-User-Id / X-User-Roles 头
          │
          ▼
[路由转发] lb://sales + StripPrefix=1
          │
          ▼
[sales 服务] 收到 /api/customer/page (正确路由)
```

---

## 实施步骤

| 步骤 | 操作 | 文件 | 改动点 |
|------|------|------|--------|
| 1 | 修复 AuthFilter 路径检查 | AuthFilter.java:50 | `"/api/sysUser"` → `"/auth/api/sysUser"` |
| 2 | 修复 AuthFeignClient 路径 | AuthFeignClient.java | 3 个 `@GetMapping` 路径去除 `/auth` 前缀 |
| 3 | 添加 CORS 全局配置 | application.yml | 在 `spring.cloud.gateway` 下添加 `globalcors` |
| 4 | 启动验证 | GatewayApplication | 确认无循环依赖错误 + CORS 预检通过 |

---

## 验证方案

| 步骤 | 验证方法 | 预期结果 |
|------|---------|---------|
| 1 | 启动 Gateway，确认无循环依赖 | `Started GatewayApplication in X seconds` |
| 2 | POST /auth/api/sysUser/login（无 Token） | 返回业务响应（非 401），证明 AuthFilter 跳过了登录路径 |
| 3 | GET /sales/api/customer/page（带 Bearer Token） | 返回业务数据，证明 Feign 调用路径正确 |
| 4 | 前端页面 devtools Network，预检 OPTIONS 请求 | HTTP 200，有 `Access-Control-Allow-Origin: http://localhost:3000` |

---

## 风险评估

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| CORS allowedOrigins 硬编码 localhost:3000 | 仅 dev 环境可用 | 生产环境需改为实际前端域名或配置化 |
| FeignClient 路径改错导致 404 | 所有认证请求失败 | 验证阶段确认 auth 服务实际路径 |
| AuthFilter 路径遗漏（如 /auth/api/xxx 其他端点） | 部分登录相关接口被误拦截 | 补充其他需要跳过的公开端点 |

---

## NOT in Scope

- pom 依赖调整（明确不可更改）
- AuthFilter 其他业务逻辑修改
- 其他模块的 CORS 配置（auth/sales/finance 等各自处理）
- Nacos 服务注册完善

## What Already Exists

- `AuthFilter.java` 已实现完整 Token 解析和用户信息传递逻辑（Plan08 已修复循环依赖）
- `AuthFeignClient.java` 已正确定义接口方法，仅路径需修正
- `GatewayApplication.java` 已正确配置 `@EnableFeignClients`
- `application.yml` 路由配置已就绪（仅缺少 CORS 配置）
