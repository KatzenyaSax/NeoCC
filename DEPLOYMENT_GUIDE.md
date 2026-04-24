# DFW (大福网) 项目部署指南

## 目录
- [项目架构](#项目架构)
- [环境要求](#环境要求)
- [一、构建项目](#一构建项目)
- [二、启动依赖服务](#二启动依赖服务)
- [三、构建并启动后端服务](#三构建并启动后端服务)
- [四、构建并启动前端](#四构建并启动前端)
- [五、验证部署](#五验证部署)
- [常见问题](#常见问题)

---

## 项目架构

本项目是一个基于 Spring Cloud 的微服务系统，包含以下模块：

| 模块 | 端口 | 功能描述 |
|------|------|----------|
| `gateway` | 8086 | API 网关，统一入口 |
| `auth` | 8085 | 认证服务，负责用户登录认证 |
| `system` | 8082 | 系统服务，组织架构、权限管理 |
| `sales` | 8083 | 销售服务，客户管理、合同管理 |
| `finance` | 8084 | 财务服务，贷款审核、佣金管理 |
| `common` | - | 公共模块，供其他服务依赖 |
| `ruoyi-ui` | 80/3000 | 前端 Vue 项目 |

### 服务依赖关系

```
用户浏览器
    ↓
前端 (ruoyi-ui:80)
    ↓ /dev-api/ /prod-api/
网关 (gateway:8086) ←→ Nacos (8848) ←→ Redis (6379)
    ↓                           ↑
认证/系统/销售/财务服务 ←→ MySQL (3306)
```

---

## 环境要求

### 软件版本要求

| 软件 | 版本要求 | 说明 |
|------|----------|------|
| **JDK** | **21** | Spring Boot 3.2.5 需要 Java 21，M4 芯片建议使用 ARM64 版本 |
| Maven | 3.8+ | 构建后端项目 |
| Node.js | 18+ | 构建前端项目 |
| npm | 9+ | 前端包管理 |
| Docker | 最新版 | 容器化部署 |
| MySQL | 8.0+ | 数据库 |
| Redis | 7.0+ | 缓存/Session 存储 |
| Nacos | 2.2+ | 服务注册与配置中心 |

### 验证 Java 版本

```bash
java -version
# 应显示 OpenJDK 21.x.x 或其他 Java 21 版本
```

> **M4 芯片注意**: 确保使用支持 ARM64 (Apple Silicon) 的 JDK 版本。Docker 镜像推荐使用 `linux/arm64/v8` 变体或 `amd64` (Docker Desktop 会自动转换)。

---

## 一、构建项目

### 1.1 构建后端

在项目根目录下执行：

```bash
cd /Users/liuhongyu/IdeaProjects/dafuweng

# 清理并构建所有模块（跳过测试）
mvn clean package -DskipTests

# 或者分别构建
mvn clean install -DskipTests
```

构建完成后，各服务的 JAR 包位置：
- `auth/target/auth-1.0-SNAPSHOT.jar`
- `system/target/system-1.0-SNAPSHOT.jar`
- `sales/target/sales-1.0-SNAPSHOT.jar`
- `finance/target/finance-1.0-SNAPSHOT.jar`
- `gateway/target/gateway-1.0-SNAPSHOT.jar`

### 1.2 构建前端

```bash
cd /Users/liuhongyu/IdeaProjects/dafuweng/ruoyi-ui

# 安装依赖
npm install

# 构建生产版本
npm run build:prod
```

构建产物在 `dist/` 目录下。

---

## 二、启动依赖服务

### 2.1 方式一：使用 Docker 启动依赖服务（推荐）

创建 `docker-compose.yml` 文件：

```yaml
version: '3.8'

services:
  # MySQL 数据库
  mysql:
    image: mysql:8.0
    container_name: dafuweng-mysql
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: 123456
      TZ: Asia/Shanghai
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./scripts/init.sql:/docker-entrypoint-initdb.d/init.sql
    command: --default-authentication-plugin=mysql_native_password --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci
    platform: linux/arm64

  # Redis 缓存
  redis:
    image: redis:7-alpine
    container_name: dafuweng-redis
    restart: unless-stopped
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    platform: linux/arm64

  # Nacos 服务注册与配置中心
  nacos:
    image: nacos/nacos-server:v2.2.3
    container_name: dafuweng-nacos
    restart: unless-stopped
    environment:
      MODE: standalone
      SPRING_DATASOURCE_PLATFORM: mysql
      PREFER_HOST_MODE: hostname
      JVM_XMS: 256m
      JVM_XMX: 512m
    ports:
      - "8848:8848"
      - "9848:9848"
    volumes:
      - nacos_data:/home/nacos/data
      - nacos_logs:/home/nacos/logs
    platform: linux/arm64

volumes:
  mysql_data:
  redis_data:
  nacos_data:
  nacos_logs:
```

启动依赖服务：

```bash
docker-compose up -d

# 查看运行状态
docker-compose ps
```

### 2.2 初始化数据库

创建初始化脚本 `scripts/init.sql`：

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS dafuweng_auth DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS dafuweng_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS dafuweng_sales DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS dafuweng_finance DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 设置字符集
SET NAMES utf8mb4;
```

执行初始化：

```bash
docker exec -i dafuweng-mysql mysql -uroot -p123456 < scripts/init.sql
```

### 2.3 验证依赖服务

```bash
# 验证 MySQL
docker exec dafuweng-mysql mysql -uroot -p123456 -e "SHOW DATABASES;"

# 验证 Redis
docker exec dafuweng-redis redis-cli ping

# 验证 Nacos (等待约 30 秒启动完成)
curl http://localhost:8848/nacos/v1/console/health/readiness
```

---

## 三、构建并启动后端服务

### 3.1 创建后端服务 Docker 网络

```bash
docker network create dafuweng-network
```

### 3.2 方式一：使用 Docker 容器运行（推荐）

为每个后端服务创建 Dockerfile。例如 `sales/Dockerfile`：

```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/sales-1.0-SNAPSHOT.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "-Xms256m", "-Xmx512m", "app.jar"]
```

> **注意**: 项目中未提供 Dockerfile，你需要创建上述文件，或参考下方"方式二"直接运行 JAR 包。

### 3.3 方式二：直接运行 JAR 包（本地开发环境）

在启动依赖服务后，使用以下命令启动各个后端服务：

```bash
# 启动 system 服务
java -jar system/target/system-1.0-SNAPSHOT.jar \
  --server.port=8082 \
  --spring.datasource.url=jdbc:mysql://localhost:3306/dafuweng_system \
  --spring.redis.host=localhost \
  --spring.cloud.nacos.discovery.server-addr=localhost:8848

# 启动 auth 服务
java -jar auth/target/auth-1.0-SNAPSHOT.jar \
  --server.port=8085 \
  --spring.datasource.url=jdbc:mysql://localhost:3306/dafuweng_auth \
  --spring.cloud.nacos.discovery.server-addr=localhost:8848

# 启动 sales 服务
java -jar sales/target/sales-1.0-SNAPSHOT.jar \
  --server.port=8083 \
  --spring.datasource.url=jdbc:mysql://localhost:3306/dafuweng_sales \
  --spring.cloud.nacos.discovery.server-addr=localhost:8848

# 启动 finance 服务
java -jar finance/target/finance-1.0-SNAPSHOT.jar \
  --server.port=8084 \
  --spring.datasource.url=jdbc:mysql://localhost:3306/dafuweng_finance \
  --spring.cloud.nacos.discovery.server-addr=localhost:8848

# 启动 gateway 服务
java -jar gateway/target/gateway-1.0-SNAPSHOT.jar \
  --server.port=8086 \
  --spring.cloud.nacos.discovery.server-addr=localhost:8848
```

> **注意**: 每次修改后端代码后需要重新构建并重启服务。

### 3.4 方式三：使用 Docker Compose 完整部署

创建 `docker-compose.services.yml`：

```yaml
version: '3.8'

services:
  # System 服务
  neocc-system:
    image: eclipse-temurin:21-jre-alpine
    container_name: neocc-system
    restart: unless-stopped
    ports:
      - "8082:8082"
    volumes:
      - ./system/target/system-1.0-SNAPSHOT.jar:/app/app.jar
    command: java -jar -Xms256m -Xmx512m /app/app.jar
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://dafuweng-mysql:3306/dafuweng_system
      - SPRING_REDIS_HOST=dafuweng-redis
      - SPRING_CLOUD_NACOS_DISCOVERY_SERVER-ADDR=dafuweng-nacos:8848
    networks:
      - dafuweng-network
    depends_on:
      - mysql
      - redis
      - nacos

  # Auth 服务
  neocc-auth:
    image: eclipse-temurin:21-jre-alpine
    container_name: neocc-auth
    restart: unless-stopped
    ports:
      - "8085:8085"
    volumes:
      - ./auth/target/auth-1.0-SNAPSHOT.jar:/app/app.jar
    command: java -jar -Xms256m -Xmx512m /app/app.jar
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://dafuweng-mysql:3306/dafuweng_auth
      - SPRING_CLOUD_NACOS_DISCOVERY_SERVER-ADDR=dafuweng-nacos:8848
    networks:
      - dafuweng-network
    depends_on:
      - mysql
      - nacos

  # Sales 服务
  neocc-sales:
    image: eclipse-temurin:21-jre-alpine
    container_name: neocc-sales
    restart: unless-stopped
    ports:
      - "8083:8083"
    volumes:
      - ./sales/target/sales-1.0-SNAPSHOT.jar:/app/app.jar
    command: java -jar -Xms256m -Xmx512m /app/app.jar
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://dafuweng-mysql:3306/dafuweng_sales
      - SPRING_CLOUD_NACOS_DISCOVERY_SERVER-ADDR=dafuweng-nacos:8848
    networks:
      - dafuweng-network
    depends_on:
      - mysql
      - nacos

  # Finance 服务
  neocc-finance:
    image: eclipse-temurin:21-jre-alpine
    container_name: neocc-finance
    restart: unless-stopped
    ports:
      - "8084:8084"
    volumes:
      - ./finance/target/finance-1.0-SNAPSHOT.jar:/app/app.jar
    command: java -jar -Xms256m -Xmx512m /app/app.jar
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://dafuweng-mysql:3306/dafuweng_finance
      - SPRING_CLOUD_NACOS_DISCOVERY_SERVER-ADDR=dafuweng-nacos:8848
    networks:
      - dafuweng-network
    depends_on:
      - mysql
      - nacos

  # Gateway 服务
  neocc-gateway:
    image: eclipse-temurin:21-jre-alpine
    container_name: neocc-gateway
    restart: unless-stopped
    ports:
      - "8086:8086"
    volumes:
      - ./gateway/target/gateway-1.0-SNAPSHOT.jar:/app/app.jar
    command: java -jar -Xms256m -Xmx512m /app/app.jar
    environment:
      - SPRING_CLOUD_NACOS_DISCOVERY_SERVER-ADDR=dafuweng-nacos:8848
    networks:
      - dafuweng-network
    depends_on:
      - nacos

networks:
  dafuweng-network:
    external: true
```

启动所有后端服务：

```bash
docker network create dafuweng-network
docker-compose -f docker-compose.services.yml up -d
```

---

## 四、构建并启动前端

### 4.1 使用 Docker 运行前端

```bash
cd /Users/liuhongyu/IdeaProjects/dafuweng/ruoyi-ui

# 构建前端
npm install
npm run build:prod
```

创建前端 Dockerfile：

```dockerfile
FROM nginx:alpine
COPY dist/ /usr/share/nginx/html/
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

构建并运行前端：

```bash
docker build -t dafuweng-ui:latest .
docker run -d \
  --name dafuweng-ui \
  --network dafuweng-network \
  -p 80:80 \
  dafuweng-ui:latest
```

### 4.2 或者本地直接运行前端（开发模式）

```bash
cd /Users/liuhongyu/IdeaProjects/dafuweng/ruoyi-ui

# 开发模式运行
npm run dev

# 或指定端口
npm run dev -- --port 3000
```

---

## 五、验证部署

### 5.1 检查容器状态

```bash
docker ps -a
```

应看到以下容器运行：
- `dafuweng-mysql`
- `dafuweng-redis`
- `dafuweng-nacos`
- `neocc-system`
- `neocc-auth`
- `neocc-sales`
- `neocc-finance`
- `neocc-gateway`
- `dafuweng-ui`

### 5.2 验证 Nacos 服务注册

访问 http://localhost:8848/nacos
- 用户名: `nacos`
- 密码: `nacos`

在"服务管理"中应能看到：
- `auth`
- `finance`
- `gateway`
- `sales`
- `system`

### 5.3 验证 API 网关

```bash
curl http://localhost:8086/actuator/health
```

### 5.4 验证前端

访问 http://localhost

### 5.5 停止所有服务

```bash
# 停止并删除容器
docker-compose -f docker-compose.services.yml down
docker-compose down

# 删除数据卷（慎用，会清除数据）
docker volume prune
```

---

## 常见问题

### Q1: Java 版本不匹配

**问题**: Spring Boot 3.x 需要 Java 17+，本项目需要 Java 21。

**解决**:
```bash
# 检查当前 Java 版本
java -version

# 如果需要安装 Java 21 (macOS)
brew install openjdk@21
brew install --cask temurin21

# 配置 JAVA_HOME
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
```

### Q2: M4 芯片兼容性问题

**问题**: 部分 Docker 镜像可能不支持 ARM64 架构。

**解决**: 
- 使用 `amd64` 镜像时，Docker Desktop 会自动进行转换
- 或使用明确支持 ARM64 的镜像（如 `linux/arm64/v8`）

### Q3: Nacos 启动慢或失败

**解决**:
```bash
# 查看 Nacos 日志
docker logs dafuweng-nacos

# 等待 30-60 秒后再尝试访问
sleep 60
curl http://localhost:8848/nacos
```

### Q4: 数据库连接失败

**解决**:
1. 确认 MySQL 已启动: `docker ps | grep mysql`
2. 检查数据库是否创建: `docker exec dafuweng-mysql mysql -uroot -p123456 -e "SHOW DATABASES;"`
3. 检查连接配置是否正确

### Q5: Maven 构建失败

**解决**:
```bash
# 清理 Maven 缓存后重新构建
mvn clean
rm -rf ~/.m2/repository/com/dafuweng
mvn clean install -DskipTests
```

---

## 快速启动命令汇总

```bash
# 1. 进入项目目录
cd /Users/liuhongyu/IdeaProjects/dafuweng

# 2. 构建后端
mvn clean package -DskipTests

# 3. 构建前端
cd ruoyi-ui && npm install && npm run build:prod && cd ..

# 4. 创建网络
docker network create dafuweng-network

# 5. 启动依赖服务
docker-compose up -d

# 6. 初始化数据库
docker exec -i dafuweng-mysql mysql -uroot -p123456 < scripts/init.sql

# 7. 启动后端服务
docker-compose -f docker-compose.services.yml up -d

# 8. 构建并启动前端
docker build -t dafuweng-ui:latest ./ruoyi-ui/
docker run -d --name dafuweng-ui --network dafuweng-network -p 80:80 dafuweng-ui:latest

# 9. 验证
curl http://localhost:8848/nacos/v1/console/health/readiness
curl http://localhost:8086/actuator/health
```

---

## 服务端口一览

| 服务 | 端口 | URL |
|------|------|-----|
| 前端 | 80 | http://localhost |
| Gateway | 8086 | http://localhost:8086 |
| Auth | 8085 | http://localhost:8085 |
| System | 8082 | http://localhost:8082 |
| Sales | 8083 | http://localhost:8083 |
| Finance | 8084 | http://localhost:8084 |
| MySQL | 3306 | localhost:3306 |
| Redis | 6379 | localhost:6379 |
| Nacos | 8848 | http://localhost:8848/nacos |
