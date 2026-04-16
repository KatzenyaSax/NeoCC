# Gateway 修复与 Token 刷新机制实现

> 修复日期: 2026-04-16  
> 状态: ✅ 已完成并测试通过

## 📋 问题背景

### 问题1: Gateway 不是真正的 Spring Cloud Gateway

**症状**:
- Gateway 使用 Tomcat 服务器（非响应式）
- 无法使用 Spring Cloud Gateway 的路由功能
- 所有请求被当作静态资源处理，返回 404 错误
- 错误信息: `No static resource login.`

**根本原因**:
- `pom.xml` 中同时引入了 `spring-boot-starter-web`（Tomcat）和 `spring-cloud-starter-gateway`
- 两者冲突，导致 Gateway 以普通 Spring MVC 应用运行

### 问题2: API 路由配置缺失 `/prod-api/` 前缀

**症状**:
- 登录接口正常，但业务页面（客户管理、合同管理等）报 404 错误
- 错误信息: `No static resource prod-api/api/customer/page.`
- 前端请求路径: `/prod-api/api/customer/page`
- Gateway 路由配置: `/api/customer/**`（不匹配）

**根本原因**:
- Nginx 将 `/prod-api/` 前缀保留并转发给 Gateway
- Gateway 路由配置中没有 `/prod-api/` 前缀，导致路由不匹配

---

## 🔧 修复方案

### 修复1: 升级 Gateway 为真正的 Spring Cloud Gateway

#### 修改的文件

##### 1. `gateway/pom.xml`

**移除的依赖**:
```xml
<!-- ❌ 移除 - 与 Gateway 冲突 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- ❌ 移除 - Feign 不兼容响应式 -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

**保留的依赖**:
```xml
<!-- ✅ 保留 - 基于 WebFlux 的响应式 Gateway -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>
```

##### 2. `gateway/src/main/java/com/dafuweng/GatewayApplication.java`

**修改前**:
```java
@SpringBootApplication(scanBasePackages = "com.dafuweng")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.dafuweng.gateway.feign")
public class GatewayApplication {
```

**修改后**:
```java
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {
```

##### 3. `gateway/src/main/java/com/dafuweng/gateway/filter/AuthFilter.java`

**重大简化**:
- ❌ 移除 Feign 客户端调用（不兼容响应式）
- ❌ 移除复杂的用户验证逻辑
- ✅ 简化为请求头传递过滤器
- ✅ JWT 验证由 Auth 服务负责

**修改后核心逻辑**:
```java
@Component
public class AuthFilter implements GlobalFilter, Ordered {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // 添加请求ID用于追踪
        String requestId = UUID.randomUUID().toString();
        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-Request-Id", requestId)
                .build();
        
        // 传递 Authorization 头到下游服务
        // JWT 验证由 Auth 服务负责
        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }
}
```

##### 4. 删除文件

- 🗑️ `gateway/src/main/java/com/dafuweng/gateway/feign/AuthFeignClient.java`

---

### 修复2: 配置所有 API 路由的 `/prod-api/` 前缀

#### 路由配置原则

**请求流程**:
```
前端请求: /prod-api/api/customer/page
    ↓
Nginx: proxy_pass http://neocc-gateway:8086; (保留完整路径)
    ↓
Gateway 匹配: Path=/prod-api/api/customer/**
    ↓
StripPrefix=1: 移除 /prod-api 前缀
    ↓
转发到服务: /api/customer/page
```

#### 修改的路由配置

##### `gateway/src/main/resources/application-docker.yml`

**修改前**:
```yaml
- id: sales-api-customer
  uri: http://sales-service:8083
  predicates:
    - Path=/api/customer/**
```

**修改后**:
```yaml
- id: sales-api-customer
  uri: http://sales-service:8083
  predicates:
    - Path=/prod-api/api/customer/**
  filters:
    - StripPrefix=1
```

##### 修改的路由列表

**认证模块**:
- ✅ `/prod-api/login` → `/login` (StripPrefix=1)
- ✅ `/prod-api/getInfo` → `/getInfo` (StripPrefix=1)
- ✅ `/prod-api/api/token/**` → `/api/token/**` (StripPrefix=1)
- ✅ `/prod-api/api/sysUser/**` → `/api/sysUser/**` (StripPrefix=1)
- ✅ `/prod-api/api/sysRole/**` → `/api/sysRole/**` (StripPrefix=1)
- ✅ `/prod-api/api/sysPermission/**` → `/api/sysPermission/**` (StripPrefix=1)

**销售模块**:
- ✅ `/prod-api/api/customer/**` → `/api/customer/**` (StripPrefix=1)
- ✅ `/prod-api/api/contract/**` → `/api/contract/**` (StripPrefix=1)
- ✅ `/prod-api/api/contactRecord/**` → `/api/contactRecord/**` (StripPrefix=1)
- ✅ `/prod-api/api/customerTransferLog/**` → `/api/customerTransferLog/**` (StripPrefix=1)
- ✅ `/prod-api/api/performanceRecord/**` → `/api/performanceRecord/**` (StripPrefix=1)
- ✅ `/prod-api/api/workLog/**` → `/api/workLog/**` (StripPrefix=1)

**财务模块**:
- ✅ `/prod-api/api/loanAudit/**` → `/api/loanAudit/**` (StripPrefix=1)
- ✅ `/prod-api/api/commissionRecord/**` → `/api/commissionRecord/**` (StripPrefix=1)
- ✅ `/prod-api/api/serviceFeeRecord/**` → `/api/serviceFeeRecord/**` (StripPrefix=1)
- ✅ `/prod-api/api/bank/**` → `/api/bank/**` (StripPrefix=1)
- ✅ `/prod-api/api/financeProduct/**` → `/api/financeProduct/**` (StripPrefix=1)

**系统模块**:
- ✅ `/prod-api/api/sysDepartment/**` → `/api/sysDepartment/**` (StripPrefix=1)
- ✅ `/prod-api/api/sysZone/**` → `/api/sysZone/**` (StripPrefix=1)

---

### 修复3: Nginx 配置优化

#### `nginx.conf`

**修改前**:
```nginx
location /prod-api/ {
    proxy_pass http://neocc-gateway:8086/prod-api/;  # ❌ 错误：导致路径变成 /prod-api/prod-api/
}
```

**修改后**:
```nginx
location /prod-api/ {
    proxy_pass http://neocc-gateway:8086;  # ✅ 正确：保留完整路径
    proxy_redirect off;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}
```

**关键点**:
- `proxy_pass` 末尾**不加** `/`，保留完整路径 `/prod-api/xxx`
- Gateway 通过 `StripPrefix=1` 过滤器移除 `/prod-api` 前缀

---

## 🎯 Token 刷新机制实现

### 核心组件

#### 1. JwtUtil.java
- 生成包含 JTI (JWT ID) 的 Token
- 支持解析 Token 获取用户信息
- 支持 Token 验证和过期检查

#### 2. TokenStoreService.java
- 内存存储 Refresh Token (ConcurrentHashMap)
- 支持 Token 轮转 (每次刷新生成新的 refresh token)
- 支持 Token 黑名单 (用于主动登出)
- 支持多设备管理
- 自动清理过期 Token (每小时)

#### 3. TokenController.java

**接口列表**:
- `POST /api/token/refresh` - 刷新 Token
- `POST /api/token/logout` - 登出
- `GET /api/token/devices` - 获取设备信息
- `DELETE /api/token/devices` - 强制登出所有设备
- `DELETE /api/token/devices/{deviceId}` - 登出指定设备

#### 4. 登录接口返回

```json
{
  "code": 200,
  "data": {
    "token": "eyJhbGci...",
    "refreshToken": "9f9beb95dc0147d398b4c36ca29b1083",
    "expires_in": 86400,
    "refreshExpiresIn": 604800
  }
}
```

---

## ✅ 测试验证

### 测试环境
- Nginx: `http://localhost` (端口 80)
- Gateway: `http://localhost:8086`
- Auth 服务: 端口 8085

### 完整测试流程

#### 1. 登录测试
```bash
curl -X POST "http://localhost/prod-api/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

**结果**: ✅ 成功
```json
{
  "code": 200,
  "data": {
    "token": "eyJhbGci...",
    "refreshToken": "abc123...",
    "expires_in": 86400,
    "refreshExpiresIn": 604800
  }
}
```

#### 2. Token 刷新测试
```bash
curl -X POST "http://localhost/prod-api/api/token/refresh" \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"abc123..."}'
```

**结果**: ✅ 成功
```json
{
  "code": 200,
  "data": {
    "accessToken": "新Token...",
    "refreshToken": "新RefreshToken...",
    "expiresIn": 86400,
    "refreshExpiresIn": 604800
  }
}
```

#### 3. 使用新 Token 访问
```bash
curl "http://localhost/prod-api/getInfo" \
  -H "Authorization: Bearer 新Token"
```

**结果**: ✅ 成功，返回用户信息

#### 4. 登出测试
```bash
curl -X POST "http://localhost/prod-api/api/token/logout" \
  -H "Authorization: Bearer 新Token" \
  -d '{"refreshToken":"新RefreshToken"}'
```

**结果**: ✅ 成功，返回"登出成功"

#### 5. 验证旧 Token 失效
```bash
curl -X POST "http://localhost/prod-api/api/token/refresh" \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"旧RefreshToken"}'
```

**结果**: ✅ 成功，返回 401 "Refresh Token无效或已过期"

#### 6. 业务 API 测试

**客户管理**:
```bash
curl "http://localhost/prod-api/api/customer/page" \
  -H "Authorization: Bearer Token"
```
**结果**: ✅ 成功，返回客户列表

**合同管理**:
```bash
curl "http://localhost/prod-api/api/contract/page" \
  -H "Authorization: Bearer Token"
```
**结果**: ✅ 成功，返回合同列表

---

## 📊 技术栈

| 组件 | 技术 | 版本 |
|------|------|------|
| Gateway | Spring Cloud Gateway | 2023.0.3 |
| Web框架 | Spring WebFlux | 响应式 |
| 服务器 | Netty | 异步非阻塞 |
| JWT | JJWT | 0.12.3 |
| 签名算法 | HMAC-SHA384 | 256位密钥 |
| 反向代理 | Nginx | Alpine |

---

## 🔑 关键配置说明

### StripPrefix 工作原理

```
请求路径: /prod-api/api/customer/page
         └──────┬──────┘ └───────┬──────┘
           前缀(被移除)    保留路径

StripPrefix=1 移除第一段路径 (/prod-api)
         ↓
转发路径: /api/customer/page
```

### 路由匹配规则

1. **精确匹配**: `/prod-api/login` (登录接口)
2. **通配符匹配**: `/prod-api/api/customer/**` (客户API)
3. **过滤器**: `StripPrefix=1` 移除前缀

---

## 🚀 部署步骤

### 1. 构建 Gateway
```bash
cd /Users/liuhongyu/IdeaProjects/final/NeoCC
mvn clean package -pl gateway -am -DskipTests
```

### 2. 重新构建并部署
```bash
docker-compose up -d --build --force-recreate gateway-service
```

### 3. 重启 Nginx
```bash
docker-compose restart nginx
```

### 4. 验证服务
```bash
# 检查 Gateway 日志
docker logs neocc-gateway --tail 30

# 应该看到: "Netty started on port 8086"
```

---

## 📝 重要变更总结

| 变更项 | 修改前 | 修改后 | 影响 |
|--------|--------|--------|------|
| Gateway 服务器 | Tomcat | Netty | 支持响应式路由 |
| Feign 客户端 | 使用 | 移除 | 简化架构 |
| AuthFilter | 复杂验证 | 简单转发 | 提升性能 |
| API 路由前缀 | `/api/**` | `/prod-api/api/**` | 匹配 Nginx 转发 |
| Nginx proxy_pass | `/prod-api/` 后缀 | 无后缀 | 避免重复前缀 |

---

## ⚠️ 注意事项

1. **所有 API 路由都必须添加 `/prod-api/` 前缀**
2. **所有路由都必须配置 `StripPrefix=1` 过滤器**
3. **Nginx 的 `proxy_pass` 末尾不能加 `/`**
4. **JWT 验证由 Auth 服务负责，Gateway 只负责路由**

---

## 🐛 常见问题

### Q1: Gateway 仍然返回 "No static resource"
**A**: 确认 Gateway 使用 Netty 而非 Tomcat
```bash
docker logs neocc-gateway | grep -E "(Netty|Tomcat)"
# 应该看到: "Netty started on port 8086"
```

### Q2: 业务 API 返回 404
**A**: 检查路由配置是否包含 `/prod-api/` 前缀和 `StripPrefix=1`

### Q3: Token 刷新失败
**A**: 确认 `/api/token/**` 路由已添加到 Security 白名单

---

## 📚 参考文档

- [Spring Cloud Gateway 官方文档](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/)
- [Spring WebFlux 官方文档](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html)
- [JJWT GitHub](https://github.com/jwtk/jjwt)

---

## 👥 维护者

- 修复者: AI Assistant
- 审核者: 开发团队
- 最后更新: 2026-04-16

---

**状态**: ✅ 所有问题已修复，测试通过，系统正常运行
