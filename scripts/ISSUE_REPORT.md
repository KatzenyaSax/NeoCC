# NeoCC 项目问题报告与修复方案

## 一、项目概述

NeoCC（待富翁）是一个基于 Spring Cloud 微服务架构的企业管理系统，包含以下模块：

| 服务 | 端口 | 功能 |
|------|------|------|
| gateway | 8086 | API网关，统一路由 |
| auth | 8085 | 认证、用户、角色、权限管理 |
| system | 8082 | 系统配置（部门、战区、字典） |
| sales | 8083 | 销售管理（客户、合同、业绩） |
| finance | 8084 | 财务管理（银行、产品、贷款） |

---

## 二、问题汇总与修复

### 问题1：登录接口路由冲突

**现象**：`No static resource login` 错误

**根本原因**：
- 网关配置 `StripPrefix=1` 去掉 `/auth` 前缀
- 但 `AuthController` 仍使用 `@RequestMapping("/auth")`
- 导致实际路径变成 `/auth/auth/login`

**修复文件**：`auth/src/main/java/com/dafuweng/auth/controller/AuthController.java`

```java
// 修改前
@RestController
@RequestMapping("/auth")
public class AuthController {

// 修改后
@RestController
@RequestMapping("/")
public class AuthController {
```

---

### 问题2：登录失败时 MyBatis 更新异常

**现象**：密码错误时抛出 MyBatis 异常

**根本原因**：`updateById()` 尝试更新所有字段，包括被 `FieldFill.INSERT_UPDATE` 填充的 `password` 字段，导致乐观锁冲突

**修复文件**：`auth/src/main/java/com/dafuweng/auth/service/impl/SysUserServiceImpl.java`

```java
// 修改前
sysUserDao.updateById(user);

// 修改后 - 使用 LambdaUpdateWrapper 只更新错误计数字段
var wrapper = new LambdaUpdateWrapper<SysUserEntity>()
        .eq(SysUserEntity::getId, user.getId())
        .set(SysUserEntity::getLoginErrorCount, errors);
if (errors >= 5) {
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.MINUTE, 30);
    wrapper.set(SysUserEntity::getLockTime, cal.getTime());
}
sysUserDao.update(null, wrapper);
```

---

### 问题3：数据库密码哈希错误

**现象**：admin 用户无法登录

**修复**：重置 admin 密码的 BCrypt 哈希值

```sql
-- 需要手动执行
UPDATE dafuweng_auth.sys_user
SET password = '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE/TU7UrVBqOC'
WHERE username = 'admin';
```

---

### 问题4：Sales 模块 InternalSalesController 路由冲突

**根本原因**：与 AuthController 相同的路由前缀问题

**修复文件**：`sales/src/main/java/com/dafuweng/sales/controller/InternalSalesController.java`

```java
// 修改前
@RequestMapping("/sales/internal")

// 修改后
@RequestMapping("/internal")
```

---

### 问题5：System 模块路由配置错误

**根本原因**：Gateway 的 `StripPrefix=2` 导致多去掉了一层路径

**修复文件**：`gateway/src/main/resources/application.yml`

```yaml
# 修改前
- id: system-route
  uri: http://neocc-system:8082
  predicates:
    - Path=/system/api/**
  filters:
    - StripPrefix=2  # 错误：多去了一层

# 修改后
- id: system-route
  uri: http://neocc-system:8082
  predicates:
    - Path=/system/api/**
  filters:
    - StripPrefix=1  # 正确
```

---

### 问题6：数据库菜单结构不完整

**根本原因**：缺少 Sales、Finance、Performance 顶级菜单

**修复 SQL**：

```sql
-- 销售管理顶级菜单
INSERT INTO sys_permission (id, parent_id, perm_code, perm_name, perm_type, perm_path, icon, sort_order, status, deleted, created_at)
VALUES (100, 0, 'SALES', '销售管理', 1, '/sales', 'shopping', 10, 1, 0, NOW());

-- 财务管理顶级菜单
INSERT INTO sys_permission (id, parent_id, perm_code, perm_name, perm_type, perm_path, icon, sort_order, status, deleted, created_at)
VALUES (200, 0, 'FINANCE', '财务管理', 1, '/finance', 'money', 20, 1, 0, NOW());

-- 业绩管理顶级菜单
INSERT INTO sys_permission (id, parent_id, perm_code, perm_name, perm_type, perm_path, icon, sort_order, status, deleted, created_at)
VALUES (600, 0, 'PERFORMANCE', '业绩管理', 1, '/performance', 'chart', 30, 1, 0, NOW());
```

---

## 三、当前系统状态

### ✅ 已解决的问题

| 问题 | 状态 | 修复版本 |
|------|------|----------|
| 登录接口路由冲突 | ✅ 已修复 | commit `fix(auth): 登录接口路由和密码更新问题` |
| 登录失败更新异常 | ✅ 已修复 | 同上 |
| Sales 路由冲突 | ✅ 已修复 | commit `fix(sales): InternalSalesController 路由路径` |
| System 路由配置 | ✅ 已修复 | commit `fix(gateway): 完善路由配置，支持所有模块` |
| 菜单结构不完整 | ✅ 已修复 | 同上 |

### ✅ 已解决的问题

| 问题 | 状态 | 修复时间 |
|------|------|----------|
| 登录接口路由冲突 | ✅ 已修复 | 2026-04-22 |
| 登录失败更新异常 | ✅ 已修复 | 2026-04-22 |
| Sales 路由冲突 | ✅ 已修复 | 2026-04-22 |
| System 路由配置 | ✅ 已修复 | 2026-04-22 |
| 菜单结构不完整 | ✅ 已修复 | 2026-04-22 |
| 数据库中文乱码 | ✅ 已修复 | 2026-04-22 |

### ⚠️ 待解决的问题

| 问题 | 优先级 | 说明 |
|------|--------|------|
| 角色数据隔离 | 中 | 后端目前未实现数据级别权限隔离 |

---

## 六、角色权限隔离分析

### 当前实现状态

#### 1. 前端菜单权限 ✅ 已实现

**机制**：后端 `AuthController.getRouters()` 根据用户角色和权限码过滤菜单

```java
// AuthController.java 第124行
.filter(m -> codeSet.contains(m.getPermCode()))
```

前端 `permission.js` 动态加载菜单树。

#### 2. 前端按钮权限 ✅ 部分实现

在部分页面中有角色判断逻辑：

```javascript
// public-sea/index.vue
const isSalesRep = computed(() => userStore.roles?.includes('sales_rep'))
const isManager = computed(() => userStore.roles?.some(r => ['sales_manager', 'admin'].includes(r)))

// 按钮级别权限控制
<el-button v-if="isManager" link type="danger" icon="Delete">删除</el-button>
```

#### 3. 后端数据权限 ⚠️ 框架存在但未启用

系统已有数据权限框架：

| 文件 | 功能 |
|------|------|
| `DataScopeAspect.java` | AOP 切面，提取用户数据权限到 ThreadLocal |
| `DataScopeContext.java` | 数据权限上下文，提供 `toSqlCondition()` |

**当前问题**：`DataScopeAspect.extractUserData()` 默认返回 `dataScope=4`（全部数据），未根据用户角色设置实际权限等级。

### 角色权限等级定义

| 值 | 名称 | 说明 |
|----|------|------|
| 1 | 本人数据 | 只能看自己创建的数据 |
| 2 | 本部门数据 | 只能看本部门的数据 |
| 3 | 本战区数据 | 只能看本战区的数据 |
| 4 | 全部数据 | 管理员/经理权限 |

### 待实现的后端数据隔离

需要在以下场景添加数据范围过滤：

1. **客户列表查询**：`CustomerServiceImpl.pageList()` 添加 `sales_rep_id` 过滤
2. **合同列表查询**：`ContractServiceImpl.pageList()` 添加数据范围条件
3. **公海客户**：`PublicSeaServiceImpl` 经理可看全部，销售代表只能领取

---

## 七、中文乱码问题分析

### 乱码问题发现

**问题描述**：
- 数据库 sales/finance/system 模块的中文数据显示乱码
- auth 模块中文正常

**乱码示例**：
```
API返回: "å·²æ"¾æ¬¾å®¢æˆ·"
应为: "已放款客户"
```

### 乱码原因分析

**根本原因**：数据导入时使用了 `latin1` 连接字符集，导致 UTF-8 字节被错误编码存储。

乱码机制：
1. 正确的中文 "已放款客户" UTF-8 字节：`E5 B7 B2 E6 94 BE E6 AC BE E5 AE A2 E6 88 B7`
2. 用 latin1 连接写入时被双重编码
3. 存储为：`C3 A5 C2 B7 C2 B2...`（错误编码）

### 修复方案 ✅ 已完成

**修复脚本**：`scripts/fix_all_encoding.sql`

修复方法：
1. 用 `latin1` 连接读取正确的中文字符串（因为字节解释为 latin1 时正好是中文）
2. 用 `utf8mb4` 连接将正确的中文写入数据库

```sql
-- 示例修复
UPDATE dafuweng_sales.customer SET name = '已放款客户' WHERE id = 8;
```

### 修复后的验证结果 ✅

```
1. 客户列表：
   边界测试_最小金额 | 测试 | 测试产品
   边界测试_最大金额 | 测试 | 测试产品
   无负责人客户 | 自然到店 | 个人消费贷
   广州某某贸易公司 | 网络推广 | 供应链金融
   陈小明 | 电话营销 | 企业经营贷

2. 银行列表：
   中国工商银行 | 深圳南山支行 | 陈经理
   中国建设银行 | 广州天河支行 | 李经理
   中国农业银行 | 北京朝阳支行 | 王经理

3. 部门列表：
   总部 | 销售部 | 财务部 | 运营部 | 销售一部 | 销售二部

4. 战区列表：
   东部战区 | 西部战区 | 南部战区 | 北部战区
```

### 预防措施

为所有服务添加了 UTF-8 编码配置：

```yaml
spring:
  servlet:
    encoding:
      charset: UTF-8
      enabled: true
      force: true
```

修改的文件：
- auth/src/main/resources/application.yml
- sales/src/main/resources/application.yml
- finance/src/main/resources/application.yml
- system/src/main/resources/application.yml
- gateway/src/main/resources/application.yml

---

## 八、部署注意事项

1. **修改 common 模块后需要重新构建**
   ```bash
   cd common && mvn clean install
   ```

2. **重启 Docker 容器**
   ```bash
   docker-compose down && docker-compose up -d
   ```

3. **验证修复**
   - 检查网关日志：`docker logs neocc-gateway`
   - 检查 auth 日志：`docker logs neocc-auth`

---

## 九、测试验证报告

### 测试时间
2026-04-22 16:05 (最终验证)

### 测试环境
| 服务 | 状态 | 端口 |
|------|------|------|
| neocc-gateway | ✅ 运行中 | 8086 |
| neocc-auth | ✅ 运行中 | 8085 |
| neocc-sales | ✅ 运行中 | 8083 |
| neocc-finance | ✅ 运行中 | 8084 |
| neocc-system | ✅ 运行中 | 8082 |
| dafuweng-ui | ✅ 运行中 | 80 |
| dafuweng-mysql | ✅ 运行中 | 3306 |

### 测试结果汇总

| 测试项 | 状态 | 说明 |
|--------|------|------|
| admin 登录 | ✅ 通过 | 返回 token: "1" |
| 获取用户信息 | ✅ 通过 | 返回 roles: ["ROLE_SUPER_ADMIN"] |
| 获取菜单路由 | ✅ 通过 | 返回完整菜单结构，中文正确 |
| 客户列表查询 | ✅ 通过 | API正常返回，中文正确 |
| 联系记录查询 | ✅ 通过 | API正常返回，中文正确 |
| 银行列表查询 | ✅ 通过 | API正常返回，中文正确 |
| 部门列表查询 | ✅ 通过 | API正常返回，中文正确 |
| 战区列表查询 | ✅ 通过 | API正常返回，中文正确 |
| 中文显示 | ✅ 已修复 | **所有业务数据中文正常显示** |

---

**报告更新时间**：2026-04-22 16:00
**修复内容**：
1. 数据库中文乱码修复完成
2. 所有业务表中文数据显示正常
