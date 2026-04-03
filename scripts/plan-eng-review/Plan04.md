# Plan04: auth 模块 Service/Controller 层实施策略

**版本：** v1.0
**日期：** 2026-04-02
**状态：** 待实施
**目标：** 为 auth 模块编写完整的 Service 接口层和 Controller 层，向外提供 REST API

---

## 一、开发现状

### 1.1 auth 模块现有资产

| 类型 | 数量 | 说明 |
|------|------|------|
| Entity | 5 个 | SysUserEntity, SysRoleEntity, SysPermissionEntity, SysUserRoleEntity, SysRolePermissionEntity |
| DAO | 5 个 | 均继承 `BaseMapper<T>`，含少量自定义方法 |
| XML Mapper | 5 个 | 位于 `auth/mapper/` 目录 |
| Service | 0 个 | 待实现 |
| Controller | 0 个 | 待实现 |

### 1.2 实体清单

| Entity | 表名 | 关键字段 | 特点 |
|--------|------|---------|------|
| `SysUserEntity` | `sys_user` ⚠️ | username, password, loginErrorCount, lockTime, lastLoginTime, lastLoginIp, @Version | 含登录锁定机制，密码现为明文 |
| `SysRoleEntity` | `sys_role` | roleCode, roleName, dataScope, roleSort, status | 含顺序字段 |
| `SysPermissionEntity` | `sys_permission` | parentId, permCode, permName, permType, permPath, icon, sortOrder, status, externalLink | 树形结构，含类型区分 |
| `SysUserRoleEntity` | `sys_user_role` | userId, roleId | 纯关联表 |
| `SysRolePermissionEntity` | `sys_role_permission` | roleId, permissionId | 纯关联表 |

### 1.3 DAO 自定义方法

| DAO | 自定义方法 |
|-----|-----------|
| SysUserDao | `selectByUsername(String username)` |
| SysRoleDao | （无，仅 BaseMapper） |
| SysPermissionDao | `selectPermCodesByRoleId(Long roleId)` |
| SysUserRoleDao | `selectRoleIdsByUserId(Long)`, `deleteByUserId(Long)`, `insertBatch(List)` |
| SysRolePermissionDao | `selectPermissionIdsByRoleId(Long)`, `deleteByRoleId(Long)`, `insertBatch(List)` |

---

## 二、关键问题（实施前必须处理）

### 2.1 Entity 注解 Bug — SysUserEntity 表名错误

**问题：** `SysUserEntity.java:13` 的 `@TableName("sys_role")` 标注了错误的表名，用户表实际是 `sys_user`。

```java
// 当前（错误）
@TableName("sys_role")
public class SysUserEntity implements Serializable { ... }

// 正确
@TableName("sys_user")
public class SysUserEntity implements Serializable { ... }
```

**影响：** 所有基于 BaseMapper 的 CRUD（selectById, updateById, deleteById 等）会操作错误的表。
**优先级：** P0，实施第一步必须修复。

### 2.2 application.yml 的 mapper-locations 路径错误

**问题：** `auth/src/main/resources/application.yml` 中 `mapper-locations: classpath:finance/mapper/*.xml`，但 auth 的 XML 实际位于 `auth/mapper/`。

```yaml
# 当前（错误）
mybatis-plus:
  mapper-locations: classpath:finance/mapper/*.xml
```
# 正确
mybatis-plus:
  mapper-locations: classpath:auth/mapper/*.xml
```

**优先级：** P0，必须修正，否则 XML Mapper 中的自定义 SQL 不生效。

### 2.3 密码明文存储 — Check02 P0 问题

**现状：** 数据库中 password 字段为明文，Check02 明确要求 Phase 2 必须改为 BCrypt 加密。

**方案：** 实施时使用 Spring Security BCrypt：
1. 用户注册 / 更新密码时：`BCryptPasswordEncoder.encode(rawPassword)`
2. 登录验证时：`BCryptPasswordEncoder.matches(rawPassword, storedPassword)`
3. 修改密码时：须校验原密码

**注意：** 这是 Check02 明确指出的 P0 问题，Phase 1 实施时就应解决，不应再拖延到 Phase 2。

---

## 三、服务设计

### 3.1 总体服务分层

```
Controller 层（REST API）
    ↓
Service 接口层（定义业务契约）
    ↓
ServiceImpl（实现业务逻辑，调用 DAO）
```

**不设关联表独立 Service：** SysUserRoleEntity 和 SysRolePermissionEntity 是纯关联表，其操作内嵌到 UserService 和 RoleService 中（通过 assignRoles / assignPermissions 方法）。

### 3.2 服务清单

| Service | 所属实体 | 说明 |
|---------|---------|------|
| SysUserService | SysUserEntity | 用户 CRUD + 登录 + 锁定 + 权限加载 + 角色分配 |
| SysRoleService | SysRoleEntity | 角色 CRUD + 权限分配 |
| SysPermissionService | SysPermissionEntity | 权限 CRUD + 树形结构 |

---

## 四、详细实施步骤

### Step 1：修复 Entity 注解 Bug

**文件：** `auth/src/main/java/com/dafuweng/auth/entity/SysUserEntity.java`

```java
@TableName("sys_user")  // 原来是 "sys_role"
```

### Step 2：修正 application.yml

**文件：** `auth/src/main/resources/application.yml`

```yaml
mybatis-plus:
  mapper-locations: classpath:auth/mapper/*.xml  # 原来是 classpath:finance/mapper/*.xml
  type-aliases-package: com.dafuweng.auth.entity  # 原来是 com.dafuweng.finance.entity
```

### Step 3：引入 BCrypt 依赖（pom.xml）

**文件：** `auth/pom.xml`

auth 已引入 `spring-cloud-starter-alibaba-nacos-discovery`，需确认是否有 `spring-boot-starter-security`。如果没有，需要添加：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

### Step 4：编写 SysUserService + SysUserServiceImpl

**文件：** `auth/src/main/java/com/dafuweng/auth/service/SysUserService.java`

```java
package com.dafuweng.auth.service;

import com.dafuweng.auth.entity.SysUserEntity;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import java.util.List;

public interface SysUserService {

    // --- 查询 ---
    SysUserEntity getById(Long id);
    PageResponse<SysUserEntity> pageList(PageRequest request);
    SysUserEntity getByUsername(String username);

    // --- 认证相关 ---
    SysUserEntity login(String username, String password, String loginIp);
    void logout(Long userId);
    void unlock(Long userId);

    // --- 权限 ---
    List<String> getPermCodesByUserId(Long userId);

    // --- 角色分配 ---
    List<Long> getRoleIdsByUserId(Long userId);
    void assignRoles(Long userId, List<Long> roleIds);

    // --- 写操作 ---
    @Transactional
    SysUserEntity save(SysUserEntity entity);            // 密码需 BCrypt 加密

    @Transactional
    SysUserEntity update(SysUserEntity entity);

    @Transactional
    void delete(Long id);

    @Transactional
    boolean changePassword(Long userId, String oldPassword, String newPassword);
}
```

**文件：** `auth/src/main/java/com/dafuweng/auth/service/impl/SysUserServiceImpl.java`

关键实现说明：

**login 逻辑（重点）：**
```
1. 根据 username 查 SysUserEntity
2. 若 lockTime 不为空且未过期 → 返回账号已锁定
3. BCryptPasswordEncoder.matches(rawPassword, entity.password) 验证密码
4. 密码错误：
   - loginErrorCount + 1
   - 若 >= 5 次 → 设置 lockTime = now + 30分钟
   - updateById(entity)
5. 密码正确：
   - loginErrorCount 重置为 0
   - lastLoginTime = now, lastLoginIp = loginIp
   - updateById(entity)
6. 返回 entity（注意：返回前需将 password 置为 null）
```

**changePassword 逻辑：**
```
1. 验证 oldPassword 与存储密码匹配
2. newPassword 经 BCryptPasswordEncoder.encode() 加密
3. entity.password = encodedPassword
4. updateById(entity)
```

**assignRoles 逻辑：**
```
1. 删除 sys_user_role 中该用户的旧记录（deleteByUserId）
2. 批量插入新记录（insertBatch）
```

### Step 5：编写 SysRoleService + SysRoleServiceImpl

**文件：** `auth/src/main/java/com/dafuweng/auth/service/SysRoleService.java`

```java
package com.dafuweng.auth.service;

import com.dafuweng.auth.entity.SysRoleEntity;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import java.util.List;

public interface SysRoleService {

    SysRoleEntity getById(Long id);
    PageResponse<SysRoleEntity> pageList(PageRequest request);
    List<SysRoleEntity> listByStatus(Short status);

    // 权限分配
    List<Long> getPermissionIdsByRoleId(Long roleId);
    void assignPermissions(Long roleId, List<Long> permissionIds);

    @Transactional
    SysRoleEntity save(SysRoleEntity entity);

    @Transactional
    SysRoleEntity update(SysRoleEntity entity);

    @Transactional
    void delete(Long id);
}
```

**assignPermissions 逻辑：**
```
1. 删除 sys_role_permission 中该角色的旧记录
2. 批量插入新记录
```

### Step 6：编写 SysPermissionService + SysPermissionServiceImpl

**文件：** `auth/src/main/java/com/dafuweng/auth/service/SysPermissionService.java`

```java
package com.dafuweng.auth.service;

import com.dafuweng.auth.entity.SysPermissionEntity;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import java.util.List;

public interface SysPermissionService {

    SysPermissionEntity getById(Long id);
    PageResponse<SysPermissionEntity> pageList(PageRequest request);
    List<SysPermissionEntity> listByStatus(Short status);

    // 树形结构
    List<SysPermissionEntity> treeList();                              // 完整树
    List<SysPermissionEntity> listByParentId(Long parentId);            // 子节点

    @Transactional
    SysPermissionEntity save(SysPermissionEntity entity);

    @Transactional
    SysPermissionEntity update(SysPermissionEntity entity);

    @Transactional
    void delete(Long id);                                               // 含子节点递归删除（建议）
}
```

**treeList 逻辑：**
```
1. 查询所有未删除的权限记录（按 sortOrder 排序）
2. 按 parentId 构建树形结构返回
```

### Step 7：编写 Controller 层

#### 7.1 SysUserController

**路径：** `auth/src/main/java/com/dafuweng/auth/controller/SysUserController.java`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/sysUser/{id}` | 获取用户详情 |
| GET | `/api/sysUser/page` | 分页查询 |
| GET | `/api/sysUser/{id}/roles` | 获取用户的角色 ID 列表 |
| GET | `/api/sysUser/{id}/permCodes` | 获取用户的权限码列表 |
| POST | `/api/sysUser/login` | 登录（body: {username, password}） |
| POST | `/api/sysUser/logout` | 登出（body: {userId}） |
| POST | `/api/sysUser` | 新增用户（密码需加密） |
| PUT | `/api/sysUser` | 更新用户 |
| PUT | `/api/sysUser/{id}/roles` | 分配角色（body: {roleIds: List<Long>}） |
| PUT | `/api/sysUser/{id}/unlock` | 解锁用户 |
| PUT | `/api/sysUser/{id}/password` | 修改密码（body: {oldPassword, newPassword}） |
| DELETE | `/api/sysUser/{id}` | 删除用户 |

#### 7.2 SysRoleController

**路径：** `auth/src/main/java/com/dafuweng/auth/controller/SysRoleController.java`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/sysRole/{id}` | 获取角色详情 |
| GET | `/api/sysRole/page` | 分页查询 |
| GET | `/api/sysRole/listByStatus` | 按状态查列表 |
| GET | `/api/sysRole/{id}/permissions` | 获取角色的权限 ID 列表 |
| POST | `/api/sysRole` | 新增角色 |
| PUT | `/api/sysRole` | 更新角色 |
| PUT | `/api/sysRole/{id}/permissions` | 分配权限（body: {permissionIds: List<Long>}） |
| DELETE | `/api/sysRole/{id}` | 删除角色 |

#### 7.3 SysPermissionController

**路径：** `auth/src/main/java/com/dafuweng/auth/controller/SysPermissionController.java`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/sysPermission/{id}` | 获取权限详情 |
| GET | `/api/sysPermission/page` | 分页查询 |
| GET | `/api/sysPermission/tree` | 获取权限树 |
| GET | `/api/sysPermission/children` | 获取子节点（?parentId=xxx） |
| GET | `/api/sysPermission/listByStatus` | 按状态查列表 |
| POST | `/api/sysPermission` | 新增权限 |
| PUT | `/api/sysPermission` | 更新权限 |
| DELETE | `/api/sysPermission/{id}` | 删除权限（含子节点） |

---

## 五、API 路径前缀规范

参考 sales/finance/system 的现有规范：

| 模块 | 路径前缀 |
|------|---------|
| sales | `/api/customer`, `/api/contract`, ... |
| finance | `/api/bank`, `/api/financeProduct`, ... |
| system | `/api/sysZone`, `/api/sysDepartment`, ... |
| **auth** | `/api/sysUser`, `/api/sysRole`, `/api/sysPermission` |

---

## 六、事务策略

参考现有模块的 `@Transactional` 标注规范：

| 操作 | 事务 |
|------|------|
| save() | @Transactional |
| update() | @Transactional |
| delete() | @Transactional |
| assignRoles() | @Transactional |
| assignPermissions() | @Transactional |
| changePassword() | @Transactional |
| login() | @Transactional（登录状态更新） |
| logout() | @Transactional |

查询方法（getById, pageList, list*, treeList）**不**加 `@Transactional`。

---

## 七、密码加密策略（BCrypt）

### 7.1 为什么要用 BCrypt

BCrypt 是自适应哈希函数（adaptive cost），自带盐（salt），可防彩虹表和 GPU 暴力破解。Spring Security 提供 `BCryptPasswordEncoder`。

**错误方式（绝对禁止）：**
- MD5(password) — 可被秒破
- SHA-256(password) — 无 salt，高速 GPU 可秒破
- 任何自定义加密 — 均不安全

### 7.2 BCryptPasswordEncoder 使用方式

```java
@Autowired
private PasswordEncoder passwordEncoder;

// 密码加密（注册 / 修改密码时）
String encoded = passwordEncoder.encode(rawPassword);

// 密码验证（登录时）
boolean matches = passwordEncoder.matches(rawPassword, storedPassword);
```

### 7.3 登录时密码验证实现

```java
@Override
public SysUserEntity login(String username, String password, String loginIp) {
    SysUserEntity user = sysUserDao.selectByUsername(username);
    if (user == null) {
        throw new IllegalArgumentException("用户名或密码错误");
    }
    // 检查锁定
    if (user.getLockTime() != null && user.getLockTime().after(new Date())) {
        throw new IllegalArgumentException("账号已锁定，请稍后再试");
    }
    // 验证密码（BCrypt）
    if (!passwordEncoder.matches(password, user.getPassword())) {
        // 密码错误处理...
        int errors = user.getLoginErrorCount() == null ? 1 : user.getLoginErrorCount() + 1;
        user.setLoginErrorCount(errors);
        if (errors >= 5) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MINUTE, 30);
            user.setLockTime(cal.getTime());
        }
        sysUserDao.updateById(user);
        throw new IllegalArgumentException("用户名或密码错误");
    }
    // 登录成功，重置错误计数
    user.setLoginErrorCount(0);
    user.setLastLoginTime(new Date());
    user.setLastLoginIp(loginIp);
    sysUserDao.updateById(user);
    // 不返回明文密码
    user.setPassword(null);
    return user;
}
```

---

## 八、文件变更清单

| 操作 | 文件路径 |
|------|---------|
| 修改 | `auth/src/main/java/com/dafuweng/auth/entity/SysUserEntity.java` — @TableName 修正 |
| 修改 | `auth/src/main/resources/application.yml` — mapper-locations 和 type-aliases-package 修正 |
| 修改 | `auth/pom.xml` — 添加 spring-boot-starter-security 依赖 |
| 新建 | `auth/src/main/java/com/dafuweng/auth/service/SysUserService.java` |
| 新建 | `auth/src/main/java/com/dafuweng/auth/service/SysRoleService.java` |
| 新建 | `auth/src/main/java/com/dafuweng/auth/service/SysPermissionService.java` |
| 新建 | `auth/src/main/java/com/dafuweng/auth/service/impl/SysUserServiceImpl.java` |
| 新建 | `auth/src/main/java/com/dafuweng/auth/service/impl/SysRoleServiceImpl.java` |
| 新建 | `auth/src/main/java/com/dafuweng/auth/service/impl/SysPermissionServiceImpl.java` |
| 新建 | `auth/src/main/java/com/dafuweng/auth/controller/SysUserController.java` |
| 新建 | `auth/src/main/java/com/dafuweng/auth/controller/SysRoleController.java` |
| 新建 | `auth/src/main/java/com/dafuweng/auth/controller/SysPermissionController.java` |

**预计新建文件：** 9 个（3 Service 接口 + 3 ServiceImpl + 3 Controller）
**预计修改文件：** 3 个（Entity + application.yml + pom.xml）

---

## 九、验收标准

1. `mvn clean compile -DskipTests` auth 模块通过
2. SysUserEntity 的 `@TableName` 注解值为 `"sys_user"`
3. application.yml 的 `mapper-locations` 指向 `classpath:auth/mapper/*.xml`
4. `BCryptPasswordEncoder` 用于所有密码相关操作
5. SysUserService.login() 实现了登录错误计数和锁定机制
6. SysUserService.getPermCodesByUserId() 能正确返回用户权限码列表
7. SysRoleService.assignPermissions() 和 SysUserService.assignRoles() 均内嵌关联表操作
8. SysPermissionService.treeList() 返回正确的树形结构
9. 所有写操作方法标注 `@Transactional`
10. 所有 Controller 方法返回 `Result<T>` 统一响应结构
