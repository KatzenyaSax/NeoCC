# Check06: Plan10 落地验收评审

**评审时间：** 2026-04-13
**评审人：** 资深后端架构审查员
**评审对象：** scripts/plan-eng-review/Plan10.md

---

## 一、总体评分

| 维度 | 得分 | 满分 | 说明 |
|------|------|------|------|
| 代码准确性 | 9/10 | 10 | 三项修复均正确落地，AuthFeignClient 路径与 SysUserController 实际路径完全吻合 |
| 改动完整性 | 9/10 | 10 | 三项 P0 缺陷全部修复，globalcors 配置完整 |
| 风险控制 | 7/10 | 10 | CORS allowedOrigins 仅支持 localhost:3000，生产环境需扩展 |
| 文档一致性 | 10/10 | 10 | 实际代码与 Plan10.md 方案完全一致 |
| 向后兼容性 | 9/10 | 10 | 不破坏现有路由、认证逻辑 |

**综合得分：8.8/10**

---

## 二、逐项验收

### 2.1 问题一：AuthFilter 登录路径检查失效

**Plan10.md 要求：**
```java
if (path.contains("/auth/api/sysUser/login") || path.contains("/auth/api/sysUser/page")) {
    return chain.filter(exchange);
}
```

**实际代码：** `AuthFilter.java:50`
```java
if (path.contains("/auth/api/sysUser/login") || path.contains("/auth/api/sysUser/page")) {
    return chain.filter(exchange);
}
```

**验收结果：** ✅ **完全一致**

---

### 2.2 问题二：AuthFeignClient 路径错误

**Plan10.md 要求：** 3 个 `@GetMapping` 路径去除 `/auth` 前缀

**实际代码：** `AuthFeignClient.java`
```java
@GetMapping("/api/sysUser/{id}")           // line 16
@GetMapping("/api/sysUser/{id}/roles")     // line 22
@GetMapping("/api/sysUser/{id}/permCodes") // line 28
```

**交叉验证：** 对照 `auth/.../controller/SysUserController.java`:
```java
@RestController
@RequestMapping("/api/sysUser")  // 类级路径
public class SysUserController {
    @GetMapping("/{id}")              // → /api/sysUser/{id}
    @GetMapping("/{id}/roles")        // → /api/sysUser/{id}/roles
    @GetMapping("/{id}/permCodes")    // → /api/sysUser/{id}/permCodes
}
```

**验收结果：** ✅ **完全匹配，路径正确**

---

### 2.3 问题三：CorsConfig 未实现

**Plan10.md 要求：** 在 `spring.cloud.gateway` 下添加 `globalcors` 声明式配置

**实际代码：** `application.yml:52-64`
```yaml
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

**验收结果：** ✅ **完全一致**

---

## 三、发现的问题

### 3.1 需关注的风险（不阻断，但需注意）

| 风险 | 严重度 | 说明 |
|------|--------|------|
| CORS allowedOrigins 硬编码 | 中 | 仅 `http://localhost:3000` 有效，生产环境需改为实际前端域名 |
| AuthFilter 跳过路径可能不全 | 低 | 仅覆盖 `/auth/api/sysUser/login` 和 `/auth/api/sysUser/page`，其他公开端点（如 logout）未覆盖但逻辑上可接受 |

### 3.2 验证未覆盖

本次仅完成**代码层面静态验收**，未执行以下动态验证：
- 启动 GatewayApplication，确认无循环依赖（需 Plan08 的 @Lazy 修复配合）
- 实际 HTTP 请求验证 CORS 预检 OPTIONS 返回 200
- 实际登录请求验证 AuthFilter 跳过逻辑

---

## 四、修复优先级建议

| 优先级 | 任务 | 说明 |
|--------|------|------|
| P0 | 启动验证 | 执行 `mvn spring-boot:run -pl gateway`，确认无报错 |
| P1 | CORS 生产扩展 | allowedOrigins 支持多环境配置（dev/staging/prod） |
| P2 | AuthFilter 补充 | 检查是否还有其他公开端点需要加入跳过列表 |

---

## 五、综合评级

| 评级项 | 结果 |
|--------|------|
| Plan10.md 方案落地率 | **100%**（3/3 项完全实施） |
| 代码准确性 | **通过**（交叉验证路径匹配） |
| 主要缺陷 | **已修复** |
| 遗留风险 | CORS 配置仅支持 dev 环境 |

**结论：** 代码层面三处 P0 缺陷均已正确修复，跨域配置已实现。需启动验证确认运行时行为。
