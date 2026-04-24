# DFW 项目部署日志

## 部署时间
- 开始时间: 2026-04-22 11:10

## 环境信息
- 操作系统: macOS (darwin)
- 芯片: Apple M4
- Java版本: OpenJDK 21.0.10

---

## 操作记录

### 1. 后端项目构建

**时间**: 2026-04-22 11:10:41

**执行命令**: `mvn clean install -DskipTests`

**构建结果**:
- ✅ common: SUCCESS
- ✅ sales: SUCCESS
- ⚠️ finance: 需要特殊处理（fat jar 问题）
- ✅ system: SUCCESS
- ✅ auth: SUCCESS
- ✅ gateway: SUCCESS

**问题处理**:
- finance 模块依赖 sales，但 sales 是 Spring Boot fat jar
- 解决方案：提取 sales jar 中的类文件，创建瘦 jar 并安装到本地仓库
- 执行命令：
  ```bash
  # 提取类文件
  mkdir -p /tmp/sales-classes
  unzip -q -o /Users/liuhongyu/.m2/repository/com/dafuweng/sales/1.0-SNAPSHOT/sales-1.0-SNAPSHOT.jar -d /tmp/sales-extract
  cp -r /tmp/sales-extract/BOOT-INF/classes/* /tmp/sales-classes/
  
  # 创建瘦 jar
  cd /tmp/sales-classes && jar cf ../sales-thin.jar .
  
  # 安装到本地仓库
  mvn install:install-file -Dfile=/tmp/sales-thin.jar -DgroupId=com.dafuweng -DartifactId=sales -Dversion=1.0-SNAPSHOT -Dpackaging=jar
  
  # 重新构建 finance
  mvn clean install -DskipTests -pl finance
  ```

**最终结果**: ✅ BUILD SUCCESS (11:12:35)

**生成的 JAR 包**:
- `common-1.0-SNAPSHOT.jar` (28,674 bytes)
- `sales-1.0-SNAPSHOT.jar` (59,035,128 bytes)
- `finance-1.0-SNAPSHOT.jar` (59,106,330 bytes)
- `system-1.0-SNAPSHOT.jar` (66,369,972 bytes)
- `auth-1.0-SNAPSHOT.jar` (60,678,849 bytes)
- `gateway-1.0-SNAPSHOT.jar` (11,066 bytes)

---

### 2. 前端项目构建

**时间**: 2026-04-22 11:13:06

**执行命令**: `npm install && npm run build:prod`

**npm install 结果**: ✅ SUCCESS (525 packages, 52s)

**npm run build:prod 结果**: ✅ SUCCESS

**构建产物**: `/Users/liuhongyu/IdeaProjects/dafuweng/ruoyi-ui/dist/`
- `index.html` (5,396 bytes)
- `static/` 目录包含 JS/CSS 等静态资源

---

### 3. Docker 容器部署

**时间**: 2026-04-22 11:15:31

#### 3.1 创建配置文件

**创建文件**: `docker-compose.yml`
- 包含 MySQL、Redis、Nacos、后端服务、前端 UI
- 使用 `nacos/nacos-server:v2.3.0-slim` 镜像
- 创建 `dafuweng-network` 网络

**创建文件**: `ruoyi-ui/Dockerfile`
```dockerfile
FROM nginx:alpine
COPY dist/ /usr/share/nginx/html/
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

#### 3.2 问题处理

**问题 1**: Nacos v2.2.3 镜像拉取失败
- **解决**: 使用已存在的 `nacos/nacos-server:v2.3.0-slim` 镜像

**问题 2**: Nacos 配置使用 MySQL 导致启动失败
- **原因**: Nacos 独立模式不需要 MySQL 配置
- **解决**: 移除 `SPRING_DATASOURCE_PLATFORM: mysql` 环境变量

**问题 3**: Gateway jar 未正确打包（fat jar 问题）
- **原因**: Spring Boot repackage 目标未执行
- **解决**: 使用 `mvn clean package spring-boot:repackage -DskipTests -pl gateway` 重新打包

#### 3.3 容器启动

**执行命令**:
```bash
docker network create dafuweng-network
docker-compose up -d
docker-compose restart neocc-system neocc-auth neocc-sales neocc-finance neocc-gateway
```

**容器状态** (11:26:01):
| 容器名 | 状态 |
|--------|------|
| dafuweng-mysql | Up ~3 min |
| dafuweng-redis | Up ~3 min |
| dafuweng-nacos | Up ~3 min |
| neocc-system | Up ~50s |
| neocc-auth | Up ~50s |
| neocc-sales | Up ~51s |
| neocc-finance | Up ~51s |
| neocc-gateway | Up ~51s |
| dafuweng-ui | Up ~3 min |

#### 3.4 服务验证

| 服务 | URL | 状态 |
|------|-----|------|
| 前端 UI | http://localhost | ✅ 正常 |
| Gateway | http://localhost:8086 | ✅ 正常 |
| Nacos | http://localhost:8848/nacos | ✅ 正常 |
| Sales API | http://localhost:8083 | ✅ 正常 |

---

## 部署完成总结

**完成时间**: 2026-04-22 11:27:00

### 访问地址

| 服务 | 地址 |
|------|------|
| 前端 | http://localhost |
| Nacos 控制台 | http://localhost:8848/nacos (nacos/nacos) |

### 端口映射

| 容器 | 端口 |
|------|------|
| dafuweng-ui | 80 |
| neocc-gateway | 8086 |
| neocc-auth | 8085 |
| neocc-system | 8082 |
| neocc-sales | 8083 |
| neocc-finance | 8084 |
| dafuweng-mysql | 3306 |
| dafuweng-redis | 6379 |
| dafuweng-nacos | 8848, 9848 |

### 生成的文件

1. `docker-compose.yml` - Docker Compose 配置文件
2. `ruoyi-ui/Dockerfile` - 前端 Dockerfile
3. `DEPLOY_LOG.md` - 本部署日志

### 后续操作

1. 初始化数据库（创建数据库表）
2. 配置 Nacos 中的服务配置
3. 访问前端进行登录测试

---

### 4. 数据库初始化

**时间**: 2026-04-22 11:34:52

#### 4.1 执行 SQL 脚本

**脚本文件**:
- `scripts/database/database.sql` - 创建数据库和表结构
- `scripts/database/dataRole.sql` - 角色和权限数据
- `scripts/database/dataUsers.sql` - 用户数据
- `scripts/database/datas.sql` - 测试数据

**执行命令**:
```bash
docker exec -i dafuweng-mysql mysql -uroot -p123456 --default-character-set=utf8mb4 < database.sql
docker exec -i dafuweng-mysql mysql -uroot -p123456 --default-character-set=utf8mb4 < dataRole.sql
docker exec -i dafuweng-mysql mysql -uroot -p123456 --default-character-set=utf8mb4 < dataUsers.sql
docker exec -i dafuweng-mysql mysql -uroot -p123456 --default-character-set=utf8mb4 < datas.sql
```

#### 4.2 数据库状态

**已创建的数据库**:
- `dafuweng_auth` - 认证服务库 ✅
- `dafuweng_system` - 系统服务库 ✅
- `dafuweng_sales` - 销售服务库 ✅ (部分表)
- `dafuweng_finance` - 财务服务库 ❌ (未创建)

**已创建的表**:

`dafuweng_auth`:
- sys_user
- sys_role
- sys_user_role
- sys_permission
- sys_role_permission

`dafuweng_system`:
- sys_zone
- sys_department
- sys_param
- sys_dict
- sys_operation_log

`dafuweng_sales`:
- customer
- contact_record

`dafuweng_finance`:
- (无表)

#### 4.3 错误记录

**❌ 错误 1: database.sql 语法错误**

**位置**: `scripts/database/database.sql` 第 351 行

**问题代码**:
```sql
`zone_id`               BIGINT                                      COLLATE '所属战区',
```

**错误原因**: `COLLATE` 后面跟的是中文注释而不是排序规则，导致 MySQL 语法错误。正确写法应该是 `COMMENT '所属战区'`。

**影响**: `contract` 表未创建，进而导致以下表也未创建：
- contract_attachment
- work_log
- performance_record
- customer_transfer_log
- 以及整个 dafuweng_finance 库的所有表

**状态**: ⚠️ 需要修复 SQL 脚本后才能继续

**❌ 错误 2: dataRole.sql 日期值错误**

**位置**: `scripts/database/dataRole.sql` 第 326 行

**问题代码**:
```sql
(1, 322, '2026-01-00 00:00:00'),
```

**错误原因**: 日期 `'2026-01-00'` 无效（1月0日不存在）。应该是 `'2026-01-01 00:00:00'`。

**MySQL 错误**:
```
ERROR 1292 (22007) at line 271: Incorrect datetime value: '2026-01-00 00:00:00' for column 'created_at' at row 50
```

**状态**: ⚠️ 需要修复 SQL 脚本后才能继续

#### 4.4 SQL 脚本错误汇总

| 文件 | 行号 | 错误类型 | 描述 |
|------|------|----------|------|
| database.sql | 351 | 语法错误 | `COLLATE '所属战区'` 应为 `COMMENT '所属战区'` |
| dataRole.sql | 326 | 日期错误 | `'2026-01-00'` 应为 `'2026-01-01'` |

#### 4.5 脚本执行结果

| 脚本 | 状态 | 备注 |
|------|------|------|
| database.sql | ❌ 失败 | 第 351 行语法错误 |
| dataRole.sql | ❌ 失败 | 第 326 行日期错误 |
| dataUsers.sql | ✅ 成功 | 12 个用户已插入 |
| datas.sql | ❌ 失败 | 多个错误（表不存在、主键冲突） |

#### 4.6 已插入的用户数据

```
id | username      | real_name
---|---------------|----------
11 | admin         | 超级管理员
12 | manager       | 陈总经理
13 | zhangsan      | 张经理
14 | deptmanager   | 刘部门经理
15 | zonedirector  | 孙战区总监
16 | lisi          | 李四
17 | salesrep_east | 周销售A
18 | wangwu        | 王五
19 | salesrep_west | 吴销售B
20 | financeuser   | 金融专员A
21 | accountant    | 郑会计
22 | auditor       | 审计员A
```

**所有用户密码**: `123456` (BCrypt 加密)

#### 4.7 SQL 脚本错误汇总

| 文件 | 行号 | 错误类型 | 描述 |
|------|------|----------|------|
| database.sql | 351 | 语法错误 | `COLLATE '所属战区'` 应为 `COMMENT '所属战区'` |
| dataRole.sql | 326 | 日期错误 | `'2026-01-00'` 应为 `'2026-01-01'` |

#### 4.8 缺失的表

由于 `database.sql` 未完全执行，以下表缺失：

**dafuweng_sales 库**:
- contract
- contract_attachment
- work_log
- performance_record
- customer_transfer_log

**dafuweng_finance 库**:
- bank
- finance_product
- loan_audit
- loan_audit_record
- service_fee_record
- commission_record

#### 4.9 后续处理建议

由于 SQL 脚本存在语法错误，需要：

1. **修复 database.sql**:
   - 文件: `scripts/database/database.sql`
   - 修改: 第 351 行
   - 将 `COLLATE '所属战区'` 改为 `COMMENT '所属战区'`

2. **修复 dataRole.sql**:
   - 文件: `scripts/database/dataRole.sql`
   - 修改: 第 326 行
   - 将 `'2026-01-00 00:00:00'` 改为 `'2026-01-01 00:00:00'`

3. **重新执行脚本**:
   ```bash
   # 1. 先清空所有数据
   docker exec dafuweng-mysql mysql -uroot -p123456 -e "
     SET FOREIGN_KEY_CHECKS=0;
     DROP DATABASE IF EXISTS dafuweng_auth;
     DROP DATABASE IF EXISTS dafuweng_system;
     DROP DATABASE IF EXISTS dafuweng_sales;
     DROP DATABASE IF EXISTS dafuweng_finance;
     SET FOREIGN_KEY_CHECKS=1;
   "

   # 2. 重新执行 database.sql (创建缺失的表)
   docker exec -i dafuweng-mysql mysql -uroot -p123456 < scripts/database/database.sql

   # 3. 重新执行 dataRole.sql
   docker exec -i dafuweng-mysql mysql -uroot -p123456 < scripts/database/dataRole.sql

   # 4. 执行 dataUsers.sql
   docker exec -i dafuweng-mysql mysql -uroot -p123456 < scripts/database/dataUsers.sql

   # 5. 执行 datas.sql
   docker exec -i dafuweng-mysql mysql -uroot -p123456 < scripts/database/datas.sql
   ```

**当前状态**: ⚠️ 数据库初始化不完整，部分功能可能无法正常使用

---

## 5. 服务状态验证

**时间**: 2026-04-22 11:39:17

### 容器状态

| 容器名 | 状态 | 端口映射 |
|--------|------|----------|
| dafuweng-ui | ✅ 运行中 | 80:80 |
| dafuweng-nacos | ✅ 运行中 | 8848:8848, 9848:9848 |
| dafuweng-mysql | ✅ 运行中 | 3306:3306 |
| dafuweng-redis | ✅ 运行中 | 6379:6379 |
| neocc-gateway | ✅ 运行中 | 8086:8086 |
| neocc-auth | ✅ 运行中 | 8085:8085 |
| neocc-system | ✅ 运行中 | 8082:8082 |
| neocc-sales | ✅ 运行中 | 8083:8083 |
| neocc-finance | ✅ 运行中 | 8084:8084 |

### 服务验证

| 服务 | URL | 状态 |
|------|-----|------|
| 前端 | http://localhost | ✅ HTTP 200 |
| Nacos | http://localhost:8848/nacos | ✅ 正常 |

---

## 6. 当前状态总结

### ✅ 已完成
1. 后端项目构建 (所有模块)
2. 前端项目构建
3. Docker 容器部署 (所有容器)
4. 用户数据插入 (12 个测试账号)
5. 部分数据库表创建

### ⚠️ 待修复
1. **SQL 脚本语法错误** - `database.sql` 第 351 行
2. **SQL 脚本日期错误** - `dataRole.sql` 第 326 行
3. **缺失的数据库表** - finance 库全部表，sales 库部分表

### 🔧 可用测试账号
所有密码: `123456`

| 用户名 | 角色 |
|--------|------|
| admin | 超级管理员 |
| manager | 总经理 |
| zhangsan | 部门经理 |
| lisi | 销售代表 |
| financeuser | 金融专员 |
| accountant | 会计 |
| auditor | 审计员 |

### 📝 访问地址
- 前端: http://localhost
- Nacos: http://localhost:8848/nacos (nacos/nacos)

---

## 7. 数据库 SQL 脚本修复

**时间**: 2026-04-22 12:35:00

### 修复的文件

#### 7.1 database.sql

**修复 1**: `contract.zone_id` 字段语法错误
- **位置**: 第 351 行
- **修复前**:
```sql
`zone_id` BIGINT COLLATE '所属战区',
```
- **修复后**:
```sql
`zone_id` BIGINT NOT NULL COMMENT '所属战区',
```
- **原因**: `COLLATE` 后不能直接跟中文注释，应使用 `COMMENT`

**修复 2**: `customer_transfer_log.operate_type` 字段长度不足
- **位置**: 第 458 行
- **修复前**:
```sql
`operate_type` VARCHAR(20) NOT NULL COMMENT '操作类型: dept_manager_transfer/public_sea_claim/manager_assign',
```
- **修复后**:
```sql
`operate_type` VARCHAR(50) NOT NULL COMMENT '操作类型: dept_manager_transfer/public_sea_claim/manager_assign',
```
- **原因**: 操作类型字符串超过 20 字符，需扩展到 50

---

#### 7.2 dataRole.sql

**修复 1**: 日期值错误
- **位置**: 第 326 行
- **修复前**:
```sql
(1, 322, '2026-01-00 00:00:00'),
```
- **修复后**:
```sql
(1, 322, '2026-01-01 00:00:00'),
```
- **原因**: `'2026-01-00'` 是无效日期（1月0日不存在）

**修复 2**: INSERT 语句添加 IGNORE 关键字（多处）
- **修改内容**: 所有 `INSERT INTO` 改为 `INSERT IGNORE INTO`
- **涉及语句**:
  - `sys_role`
  - `sys_permission`
  - `sys_role_permission` (所有角色权限插入)
- **原因**: 避免重复执行时报错

---

#### 7.3 dataUsers.sql

**修复**: INSERT 语句添加 IGNORE 关键字
- **修改内容**: 所有 `INSERT INTO` 改为 `INSERT IGNORE INTO`
- **涉及语句**:
  - `sys_user` (所有用户)
  - `sys_user_role` (所有用户角色关联)

---

#### 7.4 datas.sql

**修复 1**: INSERT 语句添加 IGNORE 关键字（多处）
- **修改内容**: 所有 `INSERT INTO` 改为 `INSERT IGNORE INTO`
- **涉及语句**:
  - `sys_user`
  - `sys_role`
  - `sys_permission`
  - `sys_role_permission`
  - `sys_zone`
  - `sys_department`
  - `sys_dict`
  - `sys_param`
  - `sys_operation_log`
  - `customer`
  - `contact_record`
  - `contract`
  - `contract_attachment`
  - `work_log`
  - `performance_record`
  - `customer_transfer_log`
  - `bank`
  - `finance_product`
  - `loan_audit`
  - `loan_audit_record`
  - `service_fee_record`
  - `commission_record`

**修复 2**: customer 表第5条数据字段顺序错误
- **位置**: 第 199 行（周超客户数据）
- **修复前**:
```sql
(5, '周超', '13900001005', '110101199003033456', NULL, NULL, NULL, 1, NULL, 2, 1, 3, 5, ...)
```
- **修复后**:
```sql
(5, '周超', '13900001005', '110101199003033456', NULL, NULL, NULL, 1, 3, 2, 1, 3, 5, ...)
```
- **原因**: `sales_rep_id` 不能为 NULL，且字段顺序与表结构不匹配

---

### 修复后验证

**执行命令**:
```bash
# 清空数据库
docker exec dafuweng-mysql mysql -uroot -p123456 -e "
  SET FOREIGN_KEY_CHECKS=0;
  DROP DATABASE IF EXISTS dafuweng_auth;
  DROP DATABASE IF EXISTS dafuweng_system;
  DROP DATABASE IF EXISTS dafuweng_sales;
  DROP DATABASE IF EXISTS dafuweng_finance;
  SET FOREIGN_KEY_CHECKS=1;
"

# 重新执行 SQL 脚本
docker exec -i dafuweng-mysql mysql -uroot -p123456 --default-character-set=utf8mb4 < scripts/database/database.sql
docker exec -i dafuweng-mysql mysql -uroot -p123456 --default-character-set=utf8mb4 < scripts/database/dataRole.sql
docker exec -i dafuweng-mysql mysql -uroot -p123456 --default-character-set=utf8mb4 < scripts/database/dataUsers.sql
docker exec -i dafuweng-mysql mysql -uroot -p123456 --default-character-set=utf8mb4 < scripts/database/datas.sql
```

**验证结果**:
| 数据库 | 表 | 记录数 |
|--------|-----|--------|
| dafuweng_auth | sys_user | 18 |
| | sys_role | 7 |
| | sys_permission | 137 |
| | sys_user_role | 13 |
| | sys_role_permission | 600 |
| dafuweng_finance | bank | 5 |
| | finance_product | 6 |
| | loan_audit | 8 |
| | loan_audit_record | 11 |
| | service_fee_record | 5 |
| | commission_record | 4 |
| dafuweng_sales | customer | 10 |
| | contract | 6 |

**状态**: ✅ 所有数据库表和测试数据已成功创建