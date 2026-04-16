---
name: ruoyi-frontend-integration
description: 在 NeoCC 项目中集成 RuoYi-Vue3 前端框架，使用 Docker 部署基础设施。适用于需要基于 RuoYi 构建企业级前端管理后台的场景。
---

# RuoYi 前端集成指南

## 概述

本 Skill 指导在 NeoCC 多模块 Java 项目中集成 RuoYi-Vue3 前端框架，包含：
- 后端 API 适配（Gateway 统一入口）
- RuoYi-Vue3 前端项目搭建
- Docker 基础设施部署
- 前后端联调配置

## 前置条件

- NeoCC Java 多模块项目（已包含 gateway、auth、system 等模块）
- Docker & Docker Compose 环境
- Node.js >= 18.x

## 实施步骤

### 步骤 1：创建 Docker 基础设施

创建项目根目录下的 `docker-compose.yml`：

```yaml
version: '3.8'

services:
  # MySQL 数据库
  mysql:
    image: mysql:8.0
    container_name: neocc-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root123
      MYSQL_DATABASE: neocc_db
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./scripts/init-db.sql:/docker-entrypoint-initdb.d/init.sql
    networks:
      - neocc-network

  # Redis 缓存
  redis:
    image: redis:7-alpine
    container_name: neocc-redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    networks:
      - neocc-network

  # Nginx 前端服务
  nginx:
    image: nginx:alpine
    container_name: neocc-nginx
    ports:
      - "80:80"
    volumes:
      - ./ruoyi-ui/dist:/usr/share/nginx/html
      - ./nginx.conf:/etc/nginx/nginx.conf
    depends_on:
      - gateway
    networks:
      - neocc-network

  # Gateway 网关服务
  gateway:
    build: ./gateway
    container_name: neocc-gateway
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - REDIS_HOST=redis
      - MYSQL_HOST=mysql
    depends_on:
      - mysql
      - redis
    networks:
      - neocc-network

volumes:
  mysql_data:
  redis_data:

networks:
  neocc-network:
    driver: bridge
```

### 步骤 2：初始化 RuoYi-Vue3 前端项目

在项目根目录执行：

```bash
# 克隆 RuoYi-Vue3 前端代码
git clone https://gitee.com/y_project/RuoYi-Vue3.git ruoyi-ui

# 进入前端目录
cd ruoyi-ui

# 安装依赖
npm install

# 复制环境配置
cp .env.development .env.local
```

### 步骤 3：配置前端 API 代理

修改 `ruoyi-ui/vite.config.js`：

```javascript
server: {
  port: 80,
  proxy: {
    '/dev-api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
      rewrite: (p) => p.replace(/^\/dev-api/, '')
    }
  }
}
```

修改 `ruoyi-ui/.env.local`：

```
# 页面标题
VITE_APP_TITLE = NeoCC管理系统

# 开发环境配置
VITE_APP_ENV = 'development'

# NeoCC 网关地址
VITE_APP_BASE_API = '/dev-api'
```

### 步骤 4：后端 Gateway 适配 RuoYi

修改 `gateway/src/main/resources/application.yml`：

```yaml
spring:
  cloud:
    gateway:
      routes:
        # 认证服务
        - id: auth-service
          uri: lb://neocc-auth
          predicates:
            - Path=/auth/**
          filters:
            - StripPrefix=1
        
        # 系统服务
        - id: system-service
          uri: lb://neocc-system
          predicates:
            - Path=/system/**
          filters:
            - StripPrefix=1
        
        # 销售服务
        - id: sales-service
          uri: lb://neocc-sales
          predicates:
            - Path=/sales/**
          filters:
            - StripPrefix=1
        
        # 财务服务
        - id: finance-service
          uri: lb://neocc-finance
          predicates:
            - Path=/finance/**
          filters:
            - StripPrefix=1

# 白名单配置（RuoYi 登录相关接口）
security:
  whitelist:
    - /auth/login
    - /auth/register
    - /auth/captcha
    - /auth/refresh-token
```

### 步骤 5：Auth 模块适配 RuoYi 登录接口

修改 `auth/src/main/java/com/dafuweng/auth/controller/LoginController.java`：

```java
@RestController
@RequestMapping("/auth")
public class LoginController {
    
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginDTO loginDTO) {
        // 适配 RuoYi 登录格式
        // 返回 token 和用户信息
    }
    
    @GetMapping("/getInfo")
    public Result<UserInfoVO> getInfo() {
        // 返回当前登录用户信息
        // 包含 roles、permissions、user 等字段
    }
    
    @GetMapping("/getRouters")
    public Result<List<RouterVO>> getRouters() {
        // 返回动态路由菜单
        // 适配 RuoYi 路由格式
    }
}
```

### 步骤 6：创建 Nginx 配置

创建项目根目录 `nginx.conf`：

```nginx
events {
    worker_connections 1024;
}

http {
    include /etc/nginx/mime.types;
    default_type application/octet-stream;

    server {
        listen 80;
        server_name localhost;
        
        # 前端静态资源
        location / {
            root /usr/share/nginx/html;
            index index.html;
            try_files $uri $uri/ /index.html;
        }
        
        # API 代理到 Gateway
        location /dev-api/ {
            proxy_pass http://gateway:8080/;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
        }
    }
}
```

### 步骤 7：启动服务

```bash
# 1. 启动基础设施
docker-compose up -d mysql redis

# 2. 初始化数据库
# 等待 MySQL 启动后，执行 database.sql

# 3. 本地启动后端服务（开发模式）
# 依次启动：gateway、auth、system、sales、finance

# 4. 启动前端开发服务器
cd ruoyi-ui
npm run dev

# 5. 生产环境构建
docker-compose up --build
```

## 目录结构

```
NeoCC/
├── docker-compose.yml          # Docker 编排配置
├── nginx.conf                  # Nginx 配置
├── ruoyi-ui/                   # RuoYi-Vue3 前端项目
│   ├── src/
│   ├── vite.config.js
│   └── package.json
├── gateway/                    # 网关服务
├── auth/                       # 认证服务
├── system/                     # 系统服务
├── sales/                      # 销售服务
└── finance/                    # 财务服务
```

## 关键适配点

### 1. 登录接口格式

RuoYi 期望的登录响应格式：
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "token": "xxx",
    "expires_in": 7200
  }
}
```

### 2. 用户信息接口格式

RuoYi 期望的用户信息格式：
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "user": { "userName": "admin", ... },
    "roles": ["admin"],
    "permissions": ["*:*:*"]
  }
}
```

### 3. 路由菜单格式

RuoYi 期望的路由格式：
```json
{
  "name": "Sales",
  "path": "/sales",
  "hidden": false,
  "component": "Layout",
  "children": [{
    "name": "Customer",
    "path": "customer",
    "component": "sales/customer/index"
  }]
}
```

## 常见问题

1. **跨域问题**：Gateway 已配置 CORS，前端代理指向 Gateway
2. **Token 刷新**：RuoYi 自动处理，需后端提供刷新接口
3. **权限控制**：后端返回 permissions，前端动态生成路由
4. **菜单图标**：使用 RuoYi 内置的 Element Plus 图标

## 扩展配置

### 添加自定义页面

1. 在 `ruoyi-ui/src/views/` 创建页面组件
2. 在数据库 `sys_menu` 表添加菜单配置
3. 后端 `getRouters` 接口返回对应路由

### 修改主题样式

编辑 `ruoyi-ui/src/styles/variables.scss`：
```scss
// 主题色
$--color-primary: #409EFF;

// 侧边栏
$menuBg: #304156;
$menuText: #bfcbd9;
```
