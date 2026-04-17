# doc05: 登录 403 错误调查报告

## 当前环境端口映射

| 端口 | 服务 | 备注 |
|------|------|------|
| 80 | WSL nginx (docker) | 默认配置，未配置 /auth/* 代理 |
| 3001 | Vite dev server | ruoyi-ui 开发服务器 |
| 8086 | Spring Cloud Gateway | 路由配置：Path=/auth/** → uri:8085, StripPrefix=0 |
| 8085 | auth 微服务 | AuthController: @RequestMapping("/auth") |

## 后端链路测试 (curl)

```
# 1. 直接到 Gateway (8086) — ✅ 200
curl -X POST http://localhost:8086/auth/login
→ 200: {"code":200,"data":{"token":"11","userId":11}}

# 2. 直接到 Auth 服务 (8085) — ✅ 200
curl -X POST http://localhost:8085/auth/login
→ 200: {"code":200,"data":{"token":"11","userId":11}}

# 3. 通过 Vite (3001) — ✅ 200
curl -X POST http://localhost:3001/auth/login
→ 200: {"code":200,"data":{"token":"11","userId":11}}

# 4. 通过 WSL nginx (80) — ❌ 404
curl -X POST http://localhost/auth/login
→ 404: nginx 默认页，未配置 /auth/* 代理
```

## 后端配置审查

### Gateway (application.yml)
```yaml
- id: auth-route
  uri: http://localhost:8085
  predicates:
    - Path=/auth/**
  filters:
    - StripPrefix=0   # /auth/login → 完整路径转发到 auth:8085/auth/login
```
✅ 配置正确。

### Gateway AuthFilter (AuthFilter.java)
```java
if (path.equals("/auth/login") || path.startsWith("/auth/login") ||
    path.equals("/auth/getInfo") || path.startsWith("/auth/getInfo") ||
    path.equals("/auth/getRouters") || path.startsWith("/auth/getRouters") ||
    path.equals("/auth/logout") || path.startsWith("/auth/logout") ||
    path.contains("/auth/api/sysUser/login") || path.contains("/auth/api/sysUser/page")) {
    return chain.filter(exchange);  // 跳过验证，直接路由
}
```
✅ 跳过路径已包含所有 `/auth/*` 公开接口。

### auth SecurityConfig (SecurityConfig.java)
```java
.requestMatchers("/auth/**").permitAll()       // /auth/* 全部放行 ✅
.requestMatchers("/login", "/getInfo", "/getRouters", "/logout").permitAll()  // 备用
.anyRequest().authenticated()
```
✅ permitAll 配置正确。

### auth JwtAuthenticationFilter (JwtAuthenticationFilter.java)
```java
if (path.equals("/auth/login") || path.startsWith("/auth/login") ||
    path.equals("/auth/getInfo") || path.startsWith("/auth/getInfo") ||
    path.equals("/auth/getRouters") || path.startsWith("/auth/getRouters") ||
    path.equals("/auth/logout") || path.startsWith("/auth/logout")) {
    filterChain.doFilter(request, response);  // 跳过 JWT 验证
    return;
}
```
✅ JWT 过滤器跳过路径正确。

### auth AuthController (AuthController.java)
```java
@RestController
@RequestMapping("/auth")          // 类级：/auth
public class AuthController {
    @PostMapping("/login")         // 方法级：/login
    public Result<Map<String, Object>> login(...) { ... }  // 完整路径：/auth/login ✅
}
```
✅ 路径映射正确。

## 前端配置审查

### ruoyi-ui/src/api/login.js
```javascript
export function login(username, password) {
  return request({
    url: '/auth/login',           // 发送 /auth/login ✅
    method: 'post',
    data: { username, password }
  })
}
```

### ruoyi-ui/src/utils/request.js getBaseURL()
```javascript
function getBaseURL(url) {
  if (url.startsWith('/auth/')) return ''  // 空字符串 → 相对路径 /auth/login ✅
  ...
}
```

### ruoyi-ui/vite.config.js proxy
```javascript
server: {
  port: 3001,
  proxy: {
    '/dev-api': { target: baseUrl, rewrite: (p) => p.replace(/^\/dev-api/, '') },
    '/auth': { target: 'http://localhost:8086', changeOrigin: true },  // ✅
  }
}
```

## 结论

### 后端：全部正常
- Gateway → auth 链路：✅ 200
- AuthController 路径：✅ `/auth/login` 匹配
- SecurityConfig permitAll：✅ 包含 `/auth/**`
- JwtAuthenticationFilter 跳过：✅ `/auth/login` 等路径已配置
- Gateway AuthFilter 跳过：✅ `/auth/login` 等路径已配置

### 前端 (Vite dev server, localhost:3001)：全部正常
- Vite proxy `/auth` → Gateway：✅ 200
- getBaseURL('/auth/login') → ''：✅ 正确
- axios url：`/auth/login`：✅ 正确

### 关键问题：WSL nginx (端口 80) 未配置代理

WSL nginx 作为前端入口（端口 80），但**没有配置 `/auth/*` 的 proxy_pass**，也没有配置静态文件目录。

```
# 当前 WSL nginx 配置（默认）
server {
    listen 80;
    root /usr/share/nginx/html;   # 默认静态页
    index index.html;
    # 缺少：location /auth/ { proxy_pass http://host.docker.internal:8086; }
}
```

因此：
- 访问 `http://localhost/` → nginx 返回默认欢迎页（不是 Vue app）
- 访问 `http://localhost/auth/login` → nginx 返回 404（找不到静态文件）

## 建议修复方案

### 方案 A（推荐）：修复 WSL nginx 配置
在 WSL 中修改 nginx 配置，添加 proxy_pass：

```nginx
server {
    listen 80;
    server_name localhost;

    # Vue 前端静态资源
    location / {
        root /path/to/ruoyi-ui/dist;
        try_files $uri $uri/ /index.html;
        index index.html;
    }

    # API 代理到 Gateway
    location /auth/ {
        proxy_pass http://host.docker.internal:8086;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }

    location /dev-api/ {
        proxy_pass http://host.docker.internal:8086;
        proxy_set_header Host $host;
    }

    location /finance/ {
        proxy_pass http://host.docker.internal:8086;
        proxy_set_header Host $host;
    }

    location /sales/ {
        proxy_pass http://host.docker.internal:8086;
        proxy_set_header Host $host;
    }

    location /system/ {
        proxy_pass http://host.docker.internal:8086;
        proxy_set_header Host $host;
    }
}
```

然后：
```bash
# 在 WSL 中
sudo nginx -t && sudo nginx -s reload
```

### 方案 B：绕过 nginx，直接使用 Vite dev server

浏览器访问 `http://localhost:3001` 而不是 `http://localhost`。**此时：
- Vite 接收请求（已配置 proxy `/auth` → Gateway:8086）**
- API 链路完整可用

### 方案 C：配置 Vite proxy 将请求转发到 nginx，再由 nginx 转发到 Gateway

不推荐，增加不必要的代理层级。

## 自测验证命令

```bash
# 1. 验证 Gateway 直接访问（应返回 200）
curl -X POST http://localhost:8086/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 2. 验证 Vite dev server 代理（应返回 200）
curl -X POST http://localhost:3001/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 3. 验证 WSL nginx（预期 404，因为未配置代理）
curl -X POST http://localhost/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

## 关于 "403" vs "404" 的说明

curl 测试 `http://localhost/auth/login` 返回 **404**（nginx 未配置）。

如果浏览器端出现 **403**，可能原因：
1. 浏览器通过不同路径访问（如 localhost:3000 或其他端口）
2. 浏览器缓存了旧的 API 基础路径配置
3. 某个中间代理的权限配置问题

建议使用浏览器开发者工具 Network 面板，确认：
- 实际发送的请求 URL
- 实际的响应状态码和响应头
- `X-Frame-Options` 或其他 Spring Security 响应头是否存在（存在则说明请求到达了 auth 服务）
