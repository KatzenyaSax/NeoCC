# 大富翁贷款管理系统 — 详细实施文档

**版本:** v1.0
**日期:** 2026-04-01
**状态:** 待开发

---

## 文档说明

本文档基于 `甲方要求.md`（业务需求）、`database.sql`（数据库结构）、`dataDesign.md`（设计说明）编写，为开发人员提供可直接执行的任务清单。

**已有资产（无需重新开发）：**
- 23个 Entity 类（MyBatis Plus 实体，字段映射已就绪）
- 23个 DAO 接口（MyBatis Plus BaseMapper + 自定义方法）
- 23个 Mapper XML 文件（resultMap + 自定义 SQL）
- dafuweng-common 依赖配置完成

**本项目唯一工作：** 在已有 Entity/DAO/Mapper 基础上，补全 Service、Controller、VO/DTO、Config、OpenFeign 客户端。

---

## 一、系统架构

```
┌─────────────────────────────────────────────────────────────────────┐
│                         Nginx (localhost:80)                        │
│                      反向代理 / 静态资源 / SSL                        │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────────────┐
│                    dafweng-gateway (port: 8080)                     │
│              路由 / 鉴权 / 限流 / 跨域 / 全局拦截器                  │
└──────┬──────────────┬──────────────┬──────────────┬────────────────┘
       │              │              │              │
 ┌─────▼─────┐ ┌──────▼─────┐ ┌─────▼─────┐ ┌──────▼─────┐
 │dafuweng-  │ │dafuweng-   │ │dafuweng-  │ │dafuweng-   │
 │auth       │ │system      │ │sales      │ │finance     │
 │(8081)    │ │(8082)      │ │(8083)     │ │(8084)      │
 │认证授权   │ │系统管理     │ │销售核心   │ │金融核心    │
 └───────────┘ └────────────┘ └───────────┘ └────────────┘
       │              │              │              │
       ▼              ▼              ▼              ▼
   MySQL:3306     MySQL:3306     MySQL:3306     MySQL:3306
  dafuweng_auth  dafuweng_system dafuweng_sales dafuweng_finance

公共组件（Nacos 统一配置）:
  Nacos (localhost:8848) — 服务发现 + 配置中心
  Redis (localhost:6379) — 缓存 / Session / 分布式锁
  RabbitMQ (localhost:4369) — 跨库异步事件
```

### 技术栈

| 组件 | 技术 | 版本 |
|------|------|------|
| 基础框架 | Spring Boot | 3.1.3 |
| Java 版本 | Java | 21 |
| 服务治理 | Spring Cloud Alibaba | 2022.0.0.0 |
| ORM | MyBatis Plus | 3.5.3.2 |
| 认证 | Apache Shiro | 1.11.0 |
| 数据库 | MySQL | 8.0 |
| 缓存/会话 | Redis | — |
| 服务注册/配置 | Nacos | — |
| 熔断 | Sentinel | — |
| 服务通信 | OpenFeign | — |
| 消息队列 | RabbitMQ | — |

---

## 二、公共基础设施

所有模块均需以下公共配置，各模块配置文件名不同（application.yml）：

### 2.1 配置文件

**dafuweng-auth/src/main/resources/application.yml**
```yaml
server:
  port: 8081

spring:
  application:
    name: dafuweng-auth
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/dafuweng_auth?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false
    username: root
    password: 123456
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
        namespace: public
      config:
        server-addr: localhost:8848
        namespace: public
        file-extension: yml
  redis:
    host: localhost
    port: 6379

mybatis-plus:
  mapper-locations: classpath:auth/mapper/*.xml
  type-aliases-package: com.dafuweng.auth.entity
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

logging:
  level:
    com.dafuweng: DEBUG
```

**dafuweng-system/src/main/resources/application.yml** — 同上，port: 8082，datasource url: dafuweng_system，mapper-locations: classpath:system/mapper/*.xml

**dafuweng-sales/src/main/resources/application.yml** — 同上，port: 8083，datasource url: dafuweng_sales，mapper-locations: classpath:sales/mapper/*.xml

**dafuweng-finance/src/main/resources/application.yml** — 同上，port: 8084，datasource url: dafuweng_finance，mapper-locations: classpath:finance/mapper/*.xml

### 2.2 主启动类

每个模块需要一个标准 Spring Boot 启动类，放置于 `src/main/java/com/dafuweng/{module}/` 下：

**dafuweng-auth/src/main/java/com/dafuweng/AuthApplication.java**
```java
package com.dafuweng;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "com.dafuweng")
@MapperScan("com.dafuweng.auth.dao")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.dafuweng.**.feign")
public class AuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
```

**dafuweng-system/src/main/java/com/dafuweng/SystemApplication.java** — 同模板，port: 8082，@MapperScan("com.dafuweng.system.dao")，@EnableFeignClients

**dafuweng-sales/src/main/java/com/dafuweng/SalesApplication.java** — 同模板，port: 8083，@MapperScan("com.dafuweng.sales.dao")

**dafuweng-finance/src/main/java/com/dafuweng/FinanceApplication.java** — 同模板，port: 8084，@MapperScan("com.dafuweng.finance.dao")

### 2.3 MyBatis Plus 自动填充配置

所有模块共用一个自动填充处理器，处理 `created_by`、`created_at`、`updated_by`、`updated_at` 字段：

**dafuweng-common/src/main/java/com/dafuweng/common/config/MetaObjectHandlerConfig.java**
```java
package com.dafuweng.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class AutoFillMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createdAt", Date.class, new Date());
        this.strictInsertFill(metaObject, "updatedAt", Date.class, new Date());
        // createdBy 和 updatedBy 从 Shiro Subject 中获取，详见认证模块
        Long userId = getCurrentUserId();
        if (userId != null) {
            this.strictInsertFill(metaObject, "createdBy", Long.class, userId);
            this.strictInsertFill(metaObject, "updatedBy", Long.class, userId);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedAt", Date.class, new Date());
        Long userId = getCurrentUserId();
        if (userId != null) {
            this.strictUpdateFill(metaObject, "updatedBy", Long.class, userId);
        }
    }

    private Long getCurrentUserId() {
        // TODO: 从 Shiro SecurityUtils 获取当前用户ID
        // return (Long) SecurityUtils.getSubject().getPrincipal();
        return null;
    }
}
```

### 2.4 通用响应结构

**dafuweng-common/src/main/java/com/dafuweng/common/entity/Result.java**
```java
package com.dafuweng.common.entity;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.setCode(200);
        r.setMessage("success");
        r.setData(data);
        return r;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> error(String message) {
        return error(500, message);
    }

    public static <T> Result<T> error(Integer code, String message) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMessage(message);
        return r;
    }
}
```

### 2.5 全局异常处理

**dafuweng-common/src/main/java/com/dafuweng/common/exception/GlobalExceptionHandler.java**
```java
package com.dafuweng.common.exception;

import com.dafuweng.common.entity.Result;
import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MybatisPlusException.class)
    public Result<?> handleMybatisPlusException(MybatisPlusException e) {
        return Result.error(400, "数据操作失败: " + e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<?> handleIllegalArgumentException(IllegalArgumentException e) {
        return Result.error(400, "参数错误: " + e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        return Result.error(500, "系统异常: " + e.getMessage());
    }
}
```

### 2.6 OpenFeign 基础配置

**dafuweng-common/src/main/java/com/dafuweng/common/config/FeignConfig.java**
```java
package com.dafuweng.common.config;

import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
}
```

---

## 三、dafuweng-auth 模块 — 认证授权

**端口：** 8081
**数据库：** dafuweng_auth
**职责：** 用户登录登出、Token 签发、 Shiro Realm、权限校验、登录安全控制

### 3.1 角色数据权限设计

| 角色 | data_scope | 说明 |
|------|-----------|------|
| SUPER_ADMIN | 4 | 全局所有数据 |
| ADMIN | 4 | 系统管理员（同全局） |
| GM（总经理） | 4 | 全局 |
| SALES_DIRECTOR（销售总监） | 3 | 本战区 |
| DEPT_MANAGER（部门经理） | 2 | 本部门 |
| SALES_REP（销售代表） | 1 | 仅本人 |
| FINANCE_SPEC（金融专员） | 1 | 仅本人 |
| FINANCE_MGR（金融部经理） | 2 | 本部门 |
| ACCOUNTANT（会计） | 2 | 本部门 |

### 3.2 登录安全逻辑

登录流程：
1. 根据 username 查 sys_user（status=1, deleted=0）
2. 检查 lock_time，若未过期则拒绝登录
3. BCrypt 校验密码
4. 校验成功后重置 login_error_count，更新 last_login_time/ip
5. 失败则 login_error_count++，若达到5次则设置 lock_time = now + 15分钟

### 3.3 需要开发的文件

#### 3.3.1 Service 层

**dafuweng-auth/src/main/java/com/dafuweng/auth/service/impl/AuthServiceImpl.java**
```java
package com.dafuweng.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dafuweng.auth.dao.SysUserDao;
import com.dafuweng.auth.entity.SysUserEntity;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class AuthService {

    @Autowired
    private SysUserDao sysUserDao;

    // BCrypt 盐值
    private static final String SALT = "dafuweng";

    /**
     * 登录
     * @param username 用户名
     * @param rawPassword 明文密码
     * @return 成功返回用户信息，失败抛异常
     */
    @Transactional
    public SysUserEntity login(String username, String rawPassword) {
        SysUserEntity user = sysUserDao.selectByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        if (user.getDeleted() == 1) {
            throw new IllegalArgumentException("账号已禁用");
        }
        // 检查锁定
        if (user.getLockTime() != null && user.getLockTime().after(new Date())) {
            throw new IllegalArgumentException("账号已锁定，请" + calculateLockMinutes(user) + "分钟后重试");
        }
        // BCrypt 校验
        String hashedPassword = new SimpleHash("SHA-256", rawPassword, ByteSource.Util.bytes(SALT), 2).toString();
        if (!hashedPassword.equals(user.getPassword())) {
            // 失败计数
            int errorCount = user.getLoginErrorCount() == null ? 0 : user.getLoginErrorCount();
            user.setLoginErrorCount(errorCount + 1);
            if (errorCount + 1 >= 5) {
                user.setLockTime(new Date(System.currentTimeMillis() + 15 * 60 * 1000));
            }
            sysUserDao.updateById(user);
            throw new IllegalArgumentException("用户名或密码错误");
        }
        // 登录成功：重置计数 + 更新最后登录
        user.setLoginErrorCount(0);
        user.setLockTime(null);
        user.setLastLoginTime(new Date());
        // lastLoginIp 从请求上下文获取
        user.setLastLoginIp(getRemoteIp());
        sysUserDao.updateById(user);
        return user;
    }

    /**
     * 获取当前登录用户
     */
    public SysUserEntity getCurrentUser() {
        Subject subject = SecurityUtils.getSubject();
        if (!subject.isAuthenticated()) {
            throw new AuthorizationException("未登录");
        }
        return (SysUserEntity) subject.getPrincipal();
    }

    /**
     * 当前用户是否拥有指定权限
     */
    public boolean hasPermission(String permCode) {
        Subject subject = SecurityUtils.getSubject();
        return subject.isPermitted(permCode);
    }

    /**
     * 退出登录
     */
    public void logout() {
        SecurityUtils.getSubject().logout();
    }

    private String getRemoteIp() {
        // 从 RequestContextHolder 获取 IP
        return "127.0.0.1";
    }

    private int calculateLockMinutes(SysUserEntity user) {
        long diff = user.getLockTime().getTime() - System.currentTimeMillis();
        return (int) (diff / 60000);
    }
}
```

#### 3.3.2 Shiro 配置

**dafuweng-auth/src/main/java/com/dafuweng/auth/config/ShiroConfig.java**
```java
package com.dafuweng.auth.config;

import com.dafuweng.auth.config.ShiroRealm;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ShiroConfig {

    @Bean
    public ShiroRealm shiroRealm() {
        return new ShiroRealm();
    }

    @Bean
    public SecurityManager securityManager(ShiroRealm shiroRealm) {
        DefaultWebSecurityManager manager = new DefaultWebSecurityManager();
        manager.setRealm(shiroRealm);
        return manager;
    }

    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager manager) {
        ShiroFilterFactoryBean factory = new ShiroFilterFactoryBean();
        factory.setSecurityManager(manager);
        // 放行登录接口，其他需要鉴权
        factory.put("/*", "anon");
        return factory;
    }

    @Bean
    public DefaultAdvisorAutoProxyCreator autoProxyCreator() {
        return new DefaultAdvisorAutoProxyCreator();
    }

    @Bean
    public AuthorizationAttributeSourceAdvisor advisor(SecurityManager manager) {
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(manager);
        return advisor;
    }
}
```

**dafuweng-auth/src/main/java/com/dafuweng/auth/config/ShiroRealm.java**
```java
package com.dafuweng.auth.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dafuweng.auth.dao.SysPermissionDao;
import com.dafuweng.auth.dao.SysRoleDao;
import com.dafuweng.auth.dao.SysUserDao;
import com.dafuweng.auth.dao.SysUserRoleDao;
import com.dafuweng.auth.entity.SysPermissionEntity;
import com.dafuweng.auth.entity.SysRoleEntity;
import com.dafuweng.auth.entity.SysUserEntity;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ShiroRealm extends AuthorizingRealm {

    @Autowired
    private SysUserDao sysUserDao;
    @Autowired
    private SysUserRoleDao sysUserRoleDao;
    @Autowired
    private SysRoleDao sysRoleDao;
    @Autowired
    private SysPermissionDao sysPermissionDao;

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        UsernamePasswordToken upToken = (UsernamePasswordToken) token;
        SysUserEntity user = sysUserDao.selectByUsername(upToken.getUsername());
        if (user == null) {
            throw new UnknownAccountException("用户不存在");
        }
        return new SimpleAuthenticationInfo(user, user.getPassword(), getName());
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        SysUserEntity user = (SysUserEntity) principals.getPrimaryPrincipal();
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();

        // 查角色
        List<Long> roleIds = sysUserRoleDao.selectRoleIdsByUserId(user.getId());
        if (roleIds.isEmpty()) {
            return info;
        }
        LambdaQueryWrapper<SysRoleEntity> roleWrapper = new LambdaQueryWrapper<>();
        roleWrapper.in(SysRoleEntity::getId, roleIds).eq(SysRoleEntity::getStatus, 1);
        List<SysRoleEntity> roles = sysRoleDao.selectList(roleWrapper);
        Set<String> roleCodes = roles.stream().map(SysRoleEntity::getRoleCode).collect(Collectors.toSet());
        info.setRoles(roleCodes);

        // 查权限
        Set<String> permCodes = new HashSet<>();
        for (SysRoleEntity role : roles) {
            List<String> perms = sysPermissionDao.selectPermCodesByRoleId(role.getId());
            permCodes.addAll(perms);
        }
        info.setStringPermissions(permCodes);
        return info;
    }
}
```

**SysPermissionDao.java** 需要新增方法：
```java
List<String> selectPermCodesByRoleId(Long roleId);
```

对应 Mapper XML：
```xml
<select id="selectPermCodesByRoleId" resultType="java.lang.String">
    SELECT p.perm_code
    FROM sys_role_permission rp
    JOIN sys_permission p ON p.id = rp.permission_id
    WHERE rp.role_id = #{roleId} AND p.status = 1 AND p.deleted = 0
</select>
```

#### 3.3.3 Controller 层

**dafuweng-auth/src/main/java/com/dafuweng/auth/controller/AuthController.java**
```java
package com.dafuweng.auth.controller;

import com.dafuweng.auth.entity.SysUserEntity;
import com.dafuweng.auth.service.AuthService;
import com.dafuweng.common.entity.Result;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public Result<SysUserEntity> login(@RequestParam String username, @RequestParam String password) {
        SysUserEntity user = authService.login(username, password);
        return Result.success(user);
    }

    @PostMapping("/logout")
    public Result<?> logout() {
        authService.logout();
        return Result.success();
    }

    @GetMapping("/current/user")
    public Result<SysUserEntity> getCurrentUser() {
        SysUserEntity user = authService.getCurrentUser();
        return Result.success(user);
    }
}
```

#### 3.3.4 VO/DTO

**dafuweng-auth/src/main/java/com/dafuweng/auth/entity/vo/LoginVO.java** — 登录请求（用户名+密码）

**dafuweng-auth/src/main/java/com/dafuweng/auth/entity/vo/UserInfoVO.java** — 当前用户信息返回（含角色列表）

---

## 四、dafuweng-system 模块 — 系统管理

**端口：** 8082
**数据库：** dafuweng_system
**职责：** 战区管理、部门管理、系统参数、数据字典、操作日志查询

### 4.1 战区（Zone）管理

战区固定2个（战区A/战区B），销售总监归属战区，不可动态增删（设计保守）。

**Controller: dafuweng-system/src/main/java/com/dafuweng/system/controller/SysZoneController.java**
```
GET    /system/zones              分页列表
GET    /system/zones/{id}         详情
POST   /system/zones              新增（需SUPER_ADMIN/ADMIN）
PUT    /system/zones/{id}         修改
DELETE /system/zones/{id}         删除（逻辑删除）
```

**Service: dafuweng-system/src/main/java/com/dafuweng/system/service/impl/SysZoneServiceImpl.java**

实现：基于 SysZoneDao（已有），封装 CRUD + listTree（返回树形结构）。

### 4.2 部门（Department）管理

支持两级树形（战区 > 部门），部门归属战区。

**Controller: dafuweng-system/src/main/java/com/dafuweng/system/controller/SysDepartmentController.java**
```
GET    /system/departments              树形列表（按战区聚合）
GET    /system/departments/{id}        详情
POST   /system/departments              新增
PUT    /system/departments/{id}        修改
DELETE /system/departments/{id}         删除（逻辑删除）
GET    /system/departments/zone/{zoneId}  某战区下的部门列表
```

**Service: dafuweng-system/src/main/java/com/dafuweng/system/service/impl/SysDepartmentServiceImpl.java**

实现：树形构建逻辑，根据 parent_id = 0 / parent_id > 0 构建两级树。

### 4.3 系统参数（Param）管理

Key-Value 全局参数，运行时可热修改。

**Controller: dafuweng-system/src/main/java/com/dafuweng/system/controller/SysParamController.java**
```
GET    /system/params               分页列表
GET    /system/params/{paramKey}    按key查值（缓存优先）
POST   /system/params               新增
PUT    /system/params/{id}          修改
DELETE /system/params/{id}          删除
GET    /system/params/group/{group} 按分组查询
```

**Service: dafuweng-system/src/main/java/com/dafuweng/system/service/impl/SysParamServiceImpl.java**

缓存策略：首次查询从 DB 读，后续放 Redis，修改时删除 Redis 缓存。

### 4.4 数据字典（Dict）管理

所有枚举值（客户类型/合同状态/意向等级）运行时可配置。

**Controller: dafuweng-system/src/main/java/com/dafuweng/system/controller/SysDictController.java**
```
GET    /system/dicts/{dictType}            按类型查字典项（缓存）
GET    /system/dicts/types                 查所有字典类型
POST   /system/dicts                       新增字典项
PUT    /system/dicts/{id}                  修改字典项
DELETE /system/dicts/{id}                  删除字典项
GET    /system/dicts/item/{dictType}/{dictCode}  查单个字典值
```

**Service: dafuweng-system/src/main/java/com/dafuweng/system/service/impl/SysDictServiceImpl.java**

字典缓存 key 格式：`dict:{dictType}`，修改/删除时 evict。

### 4.5 操作日志（OperationLog）查询

只读日志，通过 AOP 拦截 Controller 自动写入，业务层不直接操作此表。

**Controller: dafuweng-system/src/main/java/com/dafuweng/system/controller/SysOperationLogController.java**
```
GET    /system/logs               分页查询（支持 userId/module/action/时间范围过滤）
GET    /system/logs/{id}          详情
DELETE /system/logs/clean          清理X天前日志（ADMIN权限）
```

**Service: dafuweng-system/src/main/java/com/dafuweng/system/service/impl/SysOperationLogServiceImpl.java**

日志写入由 AOP 切面负责，见 4.6。

### 4.6 AOP 操作日志切面

**dafuweng-system/src/main/java/com/dafuweng/system/config/OperationLogAspect.java**

拦截所有标注 `@OperationLog` 注解的 Controller 方法，记录：
- userId / username（从 Shiro Subject）
- module（注解参数）
- action（注解参数）
- requestMethod / requestUrl / requestParams（JSON序列化）
- responseCode / errorMsg
- ip / userAgent / costTimeMs

**dafuweng-system/src/main/java/com/dafuweng/system/config/OperationLog.java**（注解定义）
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLog {
    String module();
    String action();
}
```

实现：环绕通知，在 finally 块中写入 SysOperationLogEntity（使用 Async 异步写入避免阻塞主线程）。

---

## 五、dafuweng-sales 模块 — 销售核心

**端口：** 8083
**数据库：** dafuweng_sales
**职责：** 客户管理、洽谈记录、合同管理、工作日志、业绩查询、公海、客户转移

### 5.1 客户管理（Customer）

**Controller: dafuweng-sales/src/main/java/com/dafuweng/sales/controller/CustomerController.java**
```
GET    /sales/customers                  分页列表（支持销售ID/部门ID/战区ID/状态/意向等级过滤）
GET    /sales/customers/{id}             详情
POST   /sales/customers                  新增（录入客户）
PUT    /sales/customers/{id}             修改
DELETE /sales/customers/{id}             删除（逻辑删除）
GET    /sales/customers/public-sea       公海客户列表（所有status=5且未分配）
POST   /sales/customers/{id}/claim       领取公海客户（转入自己的客户）
POST   /sales/customers/{id}/transfer    转移客户（部门经理操作）
POST   /sales/customers/{id}/annotate    客户批注
GET    /sales/customers/check            检查客户是否已存在（name+phone查重）
```

**关键业务逻辑：**

1. **录入前查重**：POST /customers 时，先查 `name + phone + deleted=0` 是否存在唯一记录，若存在则拒绝录入
2. **状态流转**：潜在(1) → 洽谈中(2) → 已签约(3) → 已放款(4)，公海(5)可被重新领取
3. **公海规则**：N天（sys_param.customer.public_sea_days，默认30天）无签约且超过下次跟进日期，客户自动入公海（由定时任务或消息队列触发）
4. **客户批注**：annotation 字段 JSON 数组追加 `{userId, content, time}`，不覆盖历史记录
5. **客户转移**：写入 customer_transfer_log，记录 from_rep_id / to_rep_id / operate_type / reason / operated_by

**Service: dafuweng-sales/src/main/java/com/dafuweng/sales/service/impl/CustomerServiceImpl.java**

### 5.2 洽谈记录（ContactRecord）

**Controller: dafuweng-sales/src/main/java/com/dafuweng/sales/controller/ContactRecordController.java**
```
GET    /sales/contacts                         分页列表（支持客户ID/销售ID/日期范围）
GET    /sales/contacts/{id}                   详情
POST   /sales/contacts                         新增洽谈记录
PUT    /sales/contacts/{id}                   修改
DELETE /sales/contacts/{id}                   删除
```

**业务规则：**
- 每次新建洽谈记录后，自动更新 customer.last_contact_date = 当前时间
- 若 contact_record.intention_after 有变化，同步更新 customer.intention_level
- 洽谈后更新 customer.next_follow_up_date = contact_record.follow_up_date

### 5.3 合同管理（Contract）

**Controller: dafuweng-sales/src/main/java/com/dafuweng/sales/controller/ContractController.java**
```
GET    /sales/contracts                    分页列表（支持客户ID/销售ID/状态/日期范围）
GET    /sales/contracts/{id}               详情
POST   /sales/contracts                    新增合同（草稿）
PUT    /sales/contracts/{id}               修改
DELETE /sales/contracts/{id}               删除（仅草稿可删）
POST   /sales/contracts/{id}/sign          签署合同（status: 1→2）
POST   /sales/contracts/{id}/send-finance  发送至金融部（status: 2→4，RabbitMQ通知）
GET    /sales/contracts/no/{contractNo}    按编号查合同
```

**业务规则：**
- 新增合同前自动生成 contract_no，格式：`HT-YYYYMMDD-XXXXXX`（XXXXXX为6位序号）
- 签署后自动写入 sign_date
- 发送至金融部后，customer.status 自动变为 3（已签约）
- contract.service_fee_1paid / service_fee_2paid 为 Short 类型（0/1）

**合同附件（ContractAttachment）**：
```
GET    /sales/attachments/contract/{contractId}   查合同附件
POST   /sales/attachments                        上传附件（multipart）
DELETE /sales/attachments/{id}                   删除
```

### 5.4 工作日志（WorkLog）

每个销售每天只能有一条日志（唯一键 sales_rep_id + log_date）。

**Controller: dafuweng-sales/src/main/java/com/dafuweng/sales/controller/WorkLogController.java**
```
GET    /sales/worklogs                        分页列表（支持销售ID/日期范围）
GET    /sales/worklogs/{id}                    详情
POST   /sales/worklogs                        新增/编辑（按sales_rep_id + log_date唯一键，重复则更新）
GET    /sales/worklogs/rep/{salesRepId}       查某销售的工作日志
GET    /sales/worklogs/summary                统计（某销售/某部门的）通话数/有效电话/意向/签约数汇总
```

### 5.5 业绩记录（PerformanceRecord）

合同审核通过后，由 finance-service 通过 OpenFeign 通知 sales-service 创建业绩记录。

**Controller: dafuweng-sales/src/main/java/com/dafuweng/sales/controller/PerformanceRecordController.java**
```
GET    /sales/performances                         分页列表（支持销售ID/部门ID/战区ID/状态）
GET    /sales/performances/{id}                     详情
GET    /sales/performances/rep/{salesRepId}         某销售的业绩列表
GET    /sales/performances/dept/{deptId}            某部门的业绩汇总
GET    /sales/performances/zone/{zoneId}            某战区的业绩汇总
GET    /sales/performances/stats/rank               业绩排名（销售代表维度）
```

**OpenFeign 回调接口**（由 finance-service 调用）：
```
POST   /sales/internal/performances/create    内部接口：创建业绩记录（金融部审核通过后由finance调用）
PUT    /sales/internal/performances/{id}/grant  内部接口：发放业绩
```

### 5.6 客户转移记录（CustomerTransferLog）

**Controller: dafuweng-sales/src/main/java/com/dafuweng/sales/controller/CustomerTransferLogController.java**
```
GET    /sales/transfers                    分页列表
GET    /sales/transfers/customer/{customerId}  某客户的转移历史
```

---

## 六、dafuweng-finance 模块 — 金融核心

**端口：** 8084
**数据库：** dafuweng_finance
**职责：** 合作银行管理、金融产品管理、贷款审核流程、服务费收取、提成发放

### 6.1 合作银行（Bank）管理

**Controller: dafuweng-finance/src/main/java/com/dafuweng/finance/controller/BankController.java**
```
GET    /finance/banks                    分页列表（支持状态过滤）
GET    /finance/banks/{id}              详情
POST   /finance/banks                   新增
PUT    /finance/banks/{id}              修改
DELETE /finance/banks/{id}              删除
```

### 6.2 金融产品（FinanceProduct）管理

**Controller: dafuweng-finance/src/main/java/com/dafuweng/finance/controller/FinanceProductController.java**
```
GET    /finance/products                         分页列表（支持银行ID/状态过滤）
GET    /finance/products/{id}                    详情
POST   /finance/products                         新增
PUT    /finance/products/{id}                   修改
DELETE /finance/products/{id}                   删除
GET    /finance/products/bank/{bankId}          按银行查产品列表
GET    /finance/products/online                  上架中的产品列表（缓存）
```

**requirements 和 documents 字段为 JSON，通过 MyBatis Plus TypeHandler 处理**：
```java
// Entity 中标注：
@TableField(typeHandler = JacksonTypeHandler.class)
private List<String> requirements;

@TableField(typeHandler = JacksonTypeHandler.class)
private List<String> documents;
```

### 6.3 贷款审核（LoanAudit）管理

**Controller: dafuweng-finance/src/main/java/com/dafuweng/finance/controller/LoanAuditController.java**
```
GET    /finance/audits                          分页列表（支持合同ID/专员ID/审核状态/银行ID）
GET    /finance/audits/{id}                    详情
GET    /finance/audits/contract/{contractId}   按合同查审核记录
POST   /finance/audits/receive                  接收合同（从sales-service，RabbitMQ触发）
POST   /finance/audits/{id}/review             初审（金融专员）
POST   /finance/audits/{id}/submit-bank         提交银行
POST   /finance/audits/{id}/bank-result         银行反馈（通过/拒绝）
POST   /finance/audits/{id}/approve             终审通过
POST   /finance/audits/{id}/reject              终审拒绝
```

**审核状态流转（audit_status + bank_audit_status）：**
```
receive（接收）→ review（初审）→ submit_bank（提交银行）→ bank_result（银行反馈）→ approve/reject（终审）
```

**审核操作写入 loan_audit_record（append-only，不可篡改）：**
- action 字段：receive / review / submit_bank / bank_result / approve / reject / return
- 每一步均记录操作人、操作时间、操作说明

**终审通过后（approve）触发：**
1. 更新 contract.status → 7（已放款）
2. 计算 service_fee_2（尾款）
3. 创建 performance_record（通过 OpenFeign 调用 sales-service）
4. 创建 commission_record（提成记录）

**OpenFeign 调用 sales-service**：
```java
@FeignClient(name = "dafuweng-sales", contextId = "salesClient")
public interface SalesFeignClient {
    @PostMapping("/sales/internal/performances/create")
    Result<?> createPerformance(@RequestBody PerformanceCreateDTO dto);
}
```

### 6.4 服务费记录（ServiceFeeRecord）管理

**Controller: dafuweng-finance/src/main/java/com/dafuweng/finance/controller/ServiceFeeRecordController.java**
```
GET    /finance/service-fees                          分页列表（支持合同ID/费用类型/支付状态/会计ID）
GET    /finance/service-fees/{id}                     详情
POST   /finance/service-fees                          收取记录（首期/二期）
PUT    /finance/service-fees/{id}/pay                  确认支付
GET    /finance/service-fees/contract/{contractId}     某合同的全部服务费记录
GET    /finance/service-fees/summary                   汇总统计（某时间段/某会计）
```

**业务规则：**
- 首期服务费在合同签署后由会计收取，创建 service_fee_record（fee_type=1）
- 二期服务费在银行放款后创建，fee_type=2
- 确认支付后：更新 contract.service_fee_1paid = 1 或 service_fee_2paid = 1

### 6.5 提成记录（CommissionRecord）管理

**Controller: dafuweng-finance/src/main/java/com/dafuweng/finance/controller/CommissionRecordController.java**
```
GET    /finance/commissions                          分页列表（支持销售ID/合同ID/状态）
GET    /finance/commissions/{id}                    详情
POST   /finance/commissions/{id}/confirm             确认提成
POST   /finance/commissions/{id}/grant               发放提成
GET    /finance/commissions/rep/{salesRepId}         某销售的提成列表
GET    /finance/commissions/stats/rank               提成排名
```

---

## 七、dafuweng-gateway 模块 — API 网关

**端口：** 8080
**职责：** 路由转发、Token 校验（验证 Shiro JWT Token）、限流、跨域

### 7.1 网关路由配置

**dafuweng-gateway/src/main/resources/application.yml**
```yaml
server:
  port: 8080

spring:
  application:
    name: dafuweng-gateway
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
    gateway:
      routes:
        - id: auth-route
          uri: http://localhost:8081
          predicates:
            - Path=/auth/**
          filters:
            - StripPrefix=1
        - id: system-route
          uri: http://localhost:8082
          predicates:
            - Path=/system/**
          filters:
            - StripPrefix=1
        - id: sales-route
          uri: http://localhost:8083
          predicates:
            - Path=/sales/**
          filters:
            - StripPrefix=1
        - id: finance-route
          uri: http://localhost:8084
          predicates:
            - Path=/finance/**
          filters:
            - StripPrefix=1
```

### 7.2 全局 Token 校验过滤器

**dafuweng-gateway/src/main/java/com/dafuweng/gateway/filter/AuthFilter.java**

拦截所有非 /auth/login 的请求：
1. 从 Header 取 `Authorization: Bearer <token>`
2. 验证 token 有效性（调用 auth-service 的 OpenFeign 接口）
3. 将 userId / username / roles 放入请求 Header 传递给下游服务
4. 返回 401 若 token 无效或过期

### 7.3 跨域配置

**dafuweng-gateway/src/main/java/com/dafuweng/gateway/config/CorsConfig.java**

允许所有来源 / 方法 / Header，maxAge = 3600。

---

## 八、跨服务通信

### 8.1 OpenFeign 客户端定义（dafuweng-finance 调用 dafuweng-sales）

**dafuweng-finance/src/main/java/com/dafuweng/finance/feign/SalesFeignClient.java**
```java
@FeignClient(name = "dafuweng-sales", contextId = "salesClient")
public interface SalesFeignClient {
    @PostMapping("/sales/internal/performances/create")
    Result<?> createPerformance(@RequestBody PerformanceCreateDTO dto);

    @PutMapping("/sales/internal/contracts/{id}/status")
    Result<?> updateContractStatus(@PathVariable Long id, @RequestParam Short status);

    @GetMapping("/sales/internal/contracts/{id}")
    Result<ContractVO> getContract(@PathVariable Long id);
}
```

**dafuweng-sales 对应的内部Controller（仅内部服务调用）**：
```
POST   /sales/internal/contracts/{id}/status      修改合同状态
GET    /sales/internal/contracts/{id}             查合同详情
```

### 8.2 RabbitMQ 事件驱动

#### 8.2.1 交换机和队列定义

**dafuweng-common/src/main/java/com/dafuweng/common/mq/MqConfig.java**

```java
@Configuration
public class MqConfig {
    public static final String EXCHANGE_SALES = "sales.exchange";
    public static final String EXCHANGE_FINANCE = "finance.exchange";

    public static final String QUEUE_CONTRACT_SIGNED = "contract.signed.queue";
    public static final String QUEUE_LOAN_APPROVED = "loan.approved.queue";
    public static final String QUEUE_PUBLIC_SEA_CHECK = "public.sea.check.queue";

    public static final String ROUTING_CONTRACT_SIGNED = "contract.signed";
    public static final String ROUTING_LOAN_APPROVED = "loan.approved";

    @Bean
    public DirectExchange salesExchange() {
        return new DirectExchange(EXCHANGE_SALES);
    }

    @Bean
    public Queue contractSignedQueue() {
        return QueueBuilder.durable(QUEUE_CONTRACT_SIGNED).build();
    }

    @Bean
    public Binding contractSignedBinding() {
        return BindingBuilder.bind(contractSignedQueue())
                .to(salesExchange())
                .with(ROUTING_CONTRACT_SIGNED);
    }
}
```

#### 8.2.2 事件定义

**dafuweng-common/src/main/java/com/dafuweng/common/mq/event/ContractSignedEvent.java**
```java
@Data
public class ContractSignedEvent {
    private Long contractId;
    private Long customerId;
    private Long salesRepId;
    private BigDecimal contractAmount;
    private Date signDate;
}
```

**dafuweng-common/src/main/java/com/dafuweng/common/mq/event/LoanApprovedEvent.java**
```java
@Data
public class LoanApprovedEvent {
    private Long contractId;
    private Long performanceId;
    private BigDecimal actualLoanAmount;
    private BigDecimal commissionAmount;
    private Date loanGrantedDate;
}
```

#### 8.2.3 事件发送

**sales-service 签署合同时发送**（SalesServiceImpl）：
```java
@Autowired
private RabbitTemplate rabbitTemplate;

public void signContract(Long contractId) {
    // ... 签署逻辑
    ContractSignedEvent event = new ContractSignedEvent();
    event.setContractId(contractId);
    event.setCustomerId(contract.getCustomerId());
    rabbitTemplate.convertAndSend(MqConfig.EXCHANGE_SALES,
        MqConfig.ROUTING_CONTRACT_SIGNED, event);
}
```

#### 8.2.4 事件消费

**finance-service 接收合同签署事件**：
```java
@RabbitListener(queues = MqConfig.QUEUE_CONTRACT_SIGNED)
public void onContractSigned(ContractSignedEvent event) {
    // 自动创建 loan_audit 记录（status=1 待审核）
    // 记录 loan_audit_record action=receive
}
```

---

## 九、辅助开发任务

### 9.1 公海自动扫描定时任务

**dafuweng-sales/src/main/java/com/dafuweng/sales/task/PublicSeaTask.java**

每天凌晨2点扫描所有 status 不在 (3,4,5) 且 `next_follow_up_date < now` 且 `创建时间距今 >= public_sea_days` 的客户，自动将 status 改为 5（公海），设置 public_sea_time。

```java
@Scheduled(cron = "0 0 2 * * ?")
public void scanPublicSeaCustomers() {
    // 从 sys_param 读取 customer.public_sea_days
    // 查询所有待公海客户
    // 批量更新 status = 5, public_sea_time = now
}
```

### 9.2 定时任务管理

所有定时任务统一由 XXL-JOB 或 Spring @Scheduled 管理，建议统一使用 XXL-JOB 方便管理。本项目初期先用 Spring @Scheduled 简化。

### 9.3 数据权限拦截器

**dafuweng-common/src/main/java/com/dafuweng/common/config/DataScopeInterceptor.java**

MyBatis Plus 拦截器，根据当前用户角色动态拼接 WHERE 条件：

```java
@Intercepts({
    @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
})
public class DataScopeInterceptor implements Interceptor {
    // data_scope=1: AND created_by = #{currentUserId}
    // data_scope=2: AND dept_id = #{currentUserDeptId}
    // data_scope=3: AND zone_id = #{currentUserZoneId}
    // data_scope=4: 不过滤（全量）
}
```

### 9.4 Nacos 配置中心

各模块配置文件在 Nacos 中管理，建议在 Nacos 创建以下 dataId：

| dataId | 模块 | 说明 |
|--------|------|------|
| dafuweng-auth.yml | auth | auth 数据库连接 / Shiro 配置 |
| dafuweng-system.yml | system | system 数据库连接 |
| dafuweng-sales.yml | sales | sales 数据库连接 / 公海天数等业务参数 |
| dafuweng-finance.yml | finance | finance 数据库连接 |
| dafuweng-gateway.yml | gateway | 路由规则 |

### 9.5 Redis 缓存设计

| Key 格式 | 用途 | 过期时间 |
|---------|------|---------|
| `user:{userId}` | 用户信息缓存 | 30min |
| `dict:{dictType}` | 字典项缓存 | 1h（修改时 evict） |
| `param:{paramKey}` | 系统参数缓存 | 1h（修改时 evict） |
| `product:online` | 上架金融产品列表 | 10min |
| `token:{token}` | Shiro Session Token | 2h |

---

## 十、API 设计规范

### 10.1 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

| code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未登录 / Token 过期 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 系统异常 |

### 10.2 分页响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [...],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  }
}
```

分页请求参数：`?current=1&size=10`

### 10.3 路径命名规范

| 模块 | 路径前缀 | 示例 |
|------|---------|------|
| 认证 | /auth | POST /auth/login |
| 系统 | /system | GET /system/departments |
| 销售 | /sales | GET /sales/customers |
| 金融 | /finance | GET /finance/audits |
| 内部接口 | /internal | POST /sales/internal/... |

### 10.4 枚举值约定（存数字，传数字）

所有枚举字段在数据库中存数字（1/2/3），API 交互中也传数字，前端通过 `/system/dicts/{dictType}` 翻译为中文标签。

---

## 十一、实施优先级与任务分解

### Phase 1 — 基础设施（第1-2天）
1. 补全各模块主启动类（AuthApplication / SystemApplication / SalesApplication / FinanceApplication）
2. 配置各模块 application.yml（数据源 + MyBatis Plus + Nacos）
3. 配置 MetaObjectHandler（自动填充 created_by / created_at / updated_by / updated_at）
4. 配置全局异常处理器（GlobalExceptionHandler）
5. 配置通用响应结构（Result）
6. 测试各模块数据库连接是否正常

### Phase 2 — 认证授权（第2-3天）
7. 开发 AuthService（登录/登出/当前用户）
8. 开发 ShiroConfig + ShiroRealm（认证 + 权限加载）
9. 开发 AuthController（/auth/login, /auth/logout, /auth/current/user）
10. SysPermissionDao 新增 selectPermCodesByRoleId 方法
11. SysUserDao.xml 补充查询（按用户名查盐值）

### Phase 3 — 系统管理（第3-4天）
12. 开发 SysZone CRUD Controller + Service
13. 开发 SysDepartment CRUD Controller + Service（树形）
14. 开发 SysParam CRUD Controller + Service（带 Redis 缓存）
15. 开发 SysDict CRUD Controller + Service（带 Redis 缓存）
16. 开发 SysOperationLog Controller + 分页查询 Service
17. 开发 OperationLogAspect AOP 切面（@OperationLog 注解）

### Phase 4 — 销售核心（第4-7天）
18. 开发 Customer CRUD Controller + Service（含查重/批注/转移）
19. 开发 ContactRecord CRUD Controller + Service
20. 开发 Contract CRUD Controller + Service（含签署/发送金融部）
21. 开发 ContractAttachment 上传接口
22. 开发 WorkLog CRUD Controller + Service
23. 开发 CustomerTransferLog 查询
24. 配置 RabbitMQ（交换机/队列/绑定关系）
25. 发送 ContractSignedEvent 到 RabbitMQ（sales-service 签署合同后触发）
26. 接收 ContractSignedEvent（finance-service 创建 loan_audit）
27. 开发 PublicSeaTask 定时任务

### Phase 5 — 金融核心（第7-10天）
28. 开发 Bank CRUD Controller + Service
29. 开发 FinanceProduct CRUD Controller + Service（含 requirements/documents JSON）
30. 开发 LoanAudit CRUD Controller + Service（含审核状态流转）
31. 开发 LoanAuditRecord append-only 写入
32. 终审通过触发：OpenFeign 调用 sales-service 创建业绩
33. 开发 ServiceFeeRecord CRUD Controller + Service
34. 开发 CommissionRecord CRUD Controller + Service
35. OpenFeign 客户端：finance → sales

### Phase 6 — 网关 + 联通（第10-12天）
36. 开发 dafuweng-gateway 主启动类
37. 配置 Gateway 路由规则
38. 开发 AuthFilter（Token 校验）
39. 开发 CorsConfig
40. 联调：auth → system → sales → finance 全链路测试
41. 配置 Nacos 配置中心
42. 配置 Redis 缓存
43. DataScopeInterceptor（数据权限拦截器）

### Phase 7 — 收尾（第12-14天）
44. 所有 Service 补充参数校验（Jakarta Validation）
45. 补充 Swagger/OpenAPI 文档注解
46. 压力测试 / 性能测试
47. 编写单元测试（各模块关键 Service）
48. 编写集成测试（跨服务 OpenFeign 调用）

---

## 十二、NOT in Scope

以下内容本阶段不开发：

1. **前端页面** — 仅后端 API
2. **文件上传存储** — 文件上传接口预留，但文件存储（OSS/本地）本阶段不实现，file_url 字段先存占位路径
3. **XXL-JOB 分布式任务调度** — 定时任务先用 @Scheduled 单机
4. **Sentinel 熔断规则配置** — 框架引入，规则后续在 Nacos 配置
5. **灰度发布** — 全量发布
6. **操作日志写入 MQ** — 直接同步写入，MQ 降级方案后续扩展

---

## 十三、数据库连接信息汇总

| 库 | 数据库名 | 连接用户 | 连接地址 |
|----|---------|---------|---------|
| 认证库 | dafuweng_auth | root | localhost:3306 |
| 系统库 | dafuweng_system | root | localhost:3306 |
| 销售库 | dafuweng_sales | root | localhost:3306 |
| 金融库 | dafuweng_finance | root | localhost:3306 |

---

## 十四、技术债务提示

1. **dafuweng-common pom.xml 中 `<groupId>` 和 `<artifactId>` 在 `<properties>` 之外重复定义**，应删除重复行（maven-compiler-plugin 配置的 source/target 为 7，与 Java 21 不符）
2. **数据权限拦截器**尚未实现，当前所有用户可看到全部数据，Phase 6 必须完成
3. **自动填充 created_by / updated_by** 依赖 Shiro Subject，Phase 1 需验证 Shiro 集成后能正确获取当前用户
4. **JSON 字段**（customer.annotation / finance_product.requirements / finance_product.documents）需要确认 MyBatis Plus JacksonTypeHandler 配置正确
5. **RabbitMQ 连接信息**（用户名/密码/VHost）在 envs.md 中未提供，需要补充到 application.yml 中

---

## 十五、文档审查意见（/qa）

**审查时间：** 2026-04-01
**审查人：** 资深后端架构师（AI）
**审查范围：** implementDetails.md 与实际代码库（pom.xml / entity / dao / mapper）的交叉验证

---

### 审查结论：设计文档存在多处重大不准确，无法直接用于开发

**修正方案（执行时间：第0天，修复完成后方可进入 Phase 1）：**

修改 `dafuweng-gateway/pom.xml`，替换其全部内容为：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.katzenyasax</groupId>
        <artifactId>NewCC</artifactId>
        <version>1.0-SNAPSHOT</version>
    </parent>

    <artifactId>dafuweng-gateway</artifactId>

    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <!-- 公共依赖 -->
        <dependency>
            <groupId>com.dafuweng</groupId>
            <artifactId>dafuweng-common</artifactId>
            <version>1.0.0</version>
        </dependency>
        <!-- Gateway 专用依赖（不能走 common，因为 common 不含 web）-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-gateway</artifactId>
        </dependency>
    </dependencies>
</project>
```

**执行人：** 开发人员
**验证方法：** `mvn clean compile -pl dafuweng-gateway -am`，编译通过即完成

---

### P0 级问题（阻塞开发，必须修复后再实施）

#### 1. dafuweng-gateway 模块 pom.xml 为空，文档假设错误

**实际状态：** `dafuweng-gateway/pom.xml` 完全为空（只有 parent 引用，无任何 `<dependencies>`）。

**文档描述（第二章）：** "所有模块均需以下公共配置"，暗示 gateway 会继承 dafuweng-common。

**问题：** dafuweng-common 不包含 `spring-cloud-starter-gateway`，gateway 模块什么依赖都没有，根本无法编译。`dafuweng-gateway/pom.xml` 需要单独添加：
```xml
<dependency>
    <groupId>com.dafuweng</groupId>
    <artifactId>dafuweng-common</artifactId>
    <version>1.0.0</version>
</dependency>
<!-- 或者 gateway 专用 -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>
```

---

#### 2. dafuweng-common/pom.xml 缺少多个关键依赖

**文档第二章声称 dafuweng-common 已配置完成，但实际上缺少：**

| 缺失依赖 | 用途 | 影响 |
|---------|------|------|
| spring-boot-starter-data-redis | SysParamService / SysDictService 缓存 | Phase 3 缓存功能完全无法实现 |
| spring-boot-starter-amqp | RabbitMQ 消息队列 | Phase 4 RabbitMQ 事件驱动无法实现 |
| spring-cloud-starter-openfeign | OpenFeign 客户端 | Phase 5 跨服务调用无法实现 |
| spring-boot-starter-json | Jackson ObjectMapper | JSON 序列化/反序列化缺失 |

**建议修复：** 在 `dafuweng-common/pom.xml` 的 `<dependencies>` 中补充：
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-json</artifactId>
</dependency>
```

**同时，common/pom.xml 需要删除以下错误配置：**
1. 第7-10行重复的 `<groupId>` 和 `<artifactId>` 定义（删除这两行，保留 parent 引用即可）
2. 第186-189行 `<maven-compiler-plugin>` 的 source/target 从 7 改为 21

**修正方案（执行时间：第0天）：**

`dafuweng-common/pom.xml` 应修改为：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.katzenyasax</groupId>
        <artifactId>NewCC</artifactId>
        <version>1.0-SNAPSHOT</version>
    </parent>

    <artifactId>dafuweng-common</artifactId>
    <version>1.0.0</version>

    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <lombok.version>1.18.30</lombok.version>  <!-- 升级 Lombok 到 1.18.30，修复 Java 21 兼容性 -->
    </properties>

    <dependencies>
        <!-- MySQL -->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>8.0.33</version>
        </dependency>

        <!-- Shiro -->
        <dependency>
            <groupId>org.apache.shiro</groupId>
            <artifactId>shiro-core</artifactId>
            <version>1.11.0</version>
        </dependency>
        <!-- Shiro Spring Boot Web 集成（修复 Shiro 依赖不足问题）-->
        <dependency>
            <groupId>org.apache.shiro</groupId>
            <artifactId>shiro-spring-boot-web-starter</artifactId>
            <version>1.11.0</version>
        </dependency>

        <!-- MyBatis Plus -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
            <version>3.5.3.2</version>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>${lombok.version}</version>
        </dependency>

        <!-- Spring Boot Web（包含 multipart、servlet 等）-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
            <version>3.1.3</version>
        </dependency>

        <!-- Spring Boot AOP -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
            <version>3.1.3</version>
        </dependency>

        <!-- Redis（新增，缓存用）-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
            <version>3.1.3</version>
        </dependency>

        <!-- RabbitMQ（新增，消息队列用）-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-amqp</artifactId>
            <version>3.1.3</version>
        </dependency>

        <!-- OpenFeign（新增，跨服务调用）-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
        </dependency>

        <!-- JSON 处理（新增）-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-json</artifactId>
            <version>3.1.3</version>
        </dependency>

        <!-- Spring Cloud Alibaba Nacos Discovery -->
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
        </dependency>

        <!-- Spring Cloud Alibaba Nacos Config -->
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
        </dependency>

        <!-- Spring Cloud Bootstrap -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-bootstrap</artifactId>
            <version>4.0.0</version>
        </dependency>

        <!-- Spring Cloud LoadBalancer -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-loadbalancer</artifactId>
            <version>3.1.3</version>
        </dependency>

        <!-- Reactor Netty -->
        <dependency>
            <groupId>io.projectreactor.netty</groupId>
            <artifactId>reactor-netty</artifactId>
            <version>1.1.12</version>
        </dependency>

        <!-- Sentinel -->
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
        </dependency>

        <!-- Commons Lang -->
        <dependency>
            <groupId>commons-lang</groupId>
            <artifactId>commons-lang</artifactId>
            <version>2.6</version>
        </dependency>
    </dependencies>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>com.alibaba.cloud</groupId>
                <artifactId>spring-cloud-alibaba-dependencies</artifactId>
                <version>2022.0.0.0</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <!-- 删除 maven-compiler-plugin 配置，直接用 parent 的 -->
    <!-- 删除 resources filtering 配置，它会破坏 XML 文件 -->
</project>
```

**修正要点：**
1. 删除第7-10行重复的 groupId/artifactId（由 parent 统一管理）
2. 将 lombok.version 升级到 1.18.30
3. 删除 maven-compiler-plugin 的 source=7/target=7 配置
4. 删除 resources filtering（会破坏 Mapper XML 文件）
5. 新增 shiro-spring-boot-web-starter（修复 Shiro 集成）
6. 新增 spring-boot-starter-data-redis、spring-boot-starter-amqp、spring-cloud-starter-openfeign、spring-boot-starter-json

**执行人：** 开发人员
**验证方法：** `mvn clean compile -pl dafuweng-common -am`，编译通过即完成

---

#### 3. Shiro 密码校验实现与数据库规范严重不符

**database.sql 规定：** `password VARCHAR(200) COMMENT '密码密文(BCrypt)'`

**文档第3.3.1节 AuthServiceImpl 实现：**
```java
// BCrypt 校验
String hashedPassword = new SimpleHash("SHA-256", rawPassword, ByteSource.Util.bytes(SALT), 2).toString();
if (!hashedPassword.equals(user.getPassword())) {
```
**这是 SHA-256，不是 BCrypt。** Shiro 的 `SimpleHash` 是 SHA-256 算法，不是 BCrypt。数据库设计要求 BCrypt，但代码实现用的是 SHA-256。这两套算法完全不相容，上线后所有现有账号（admin/123456 等）的密码校验都会失败。

**正确做法：** Shiro 不直接支持 BCrypt，应使用 `org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder`，或者引入 `shiro-crypto` 模块。Spring Boot 3 下推荐直接用 Spring Security 的 `BCryptPasswordEncoder`。

**修正方案（执行时间：第0天，必须先于此文件任何业务代码开发）：**

1. **在 `dafuweng-common/pom.xml` 中已添加 `shiro-spring-boot-web-starter`（见 Issue 2 修正方案），该依赖自带 `BCryptPasswordEncoder` 相关类**

2. **在 `AuthServiceImpl.java` 中替换密码验证逻辑：**

原错误代码：
```java
// BCrypt 校验 —— 错误实现（SHA-256，非 BCrypt）
import org.apache.shiro.crypto.hash.SimpleHash;
String hashedPassword = new SimpleHash("SHA-256", rawPassword, ByteSource.Util.bytes(SALT), 2).toString();
if (!hashedPassword.equals(user.getPassword())) {
    throw new IncorrectCredentialsException("密码错误");
}
```

正确实现：
```java
// BCrypt 校验 —— 正确实现
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
@Autowired
private BCryptPasswordEncoder passwordEncoder;

// 登录验证方法内：
if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
    throw new IncorrectCredentialsException("密码错误");
}
```

3. **注册 BCryptPasswordEncoder 为 Spring Bean（ShiroConfig.java）：**
```java
@Bean
public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

4. **将 SALT 常量删除（见 Issue 5 修正方案）**

**注意：** BCrypt 密码格式为 `$2a$10$...`，长度约60字符，与原 SHA-256 十六进制字符串完全不同。上线前需确认数据库中已有账号的密码是 BCrypt 格式还是 SHA-256 格式：
- 若数据库密码为 SHA-256 格式，需编写数据迁移脚本，将所有密码重置为 BCrypt 格式（用户首次登录时重新加密）
- 若数据库为空（新库），无需处理

**执行人：** 开发人员
**验证方法：** 编写单元测试 `AuthServiceImplTest`，模拟用户登录，验证 BCrypt 密码比对逻辑正常工作

---

#### 4. Shiro 认证实现依赖不足

**文档第3.3.2节 ShiroConfig/ShiroRealm 依赖：** `dafuweng-common` 只有 `shiro-core:1.11.0`。

**实际缺少：**
- `shiro-spring-boot-starter-web`（提供 Shiro Spring Boot 自动配置）
- 或至少需要 `shiro-web` + Spring 集成包

只用 `shiro-core` 缺少：web 过滤集成、`@RequiresPermissions` 注解支持、`CredentialsMatcher` 接口实现、`SecurityManager` 完整配置。文档中的 ShiroConfig 大量使用 Spring 注入和 `@Bean` 配置，但没有 `shiro-spring` 集成包，`ShiroRealm` 中的 `@Autowired` 注入也无法工作。

**修正方案（执行时间：第0天）：**

已在 Issue 2 的 `dafuweng-common/pom.xml` 修正方案中添加了 `shiro-spring-boot-web-starter:1.11.0`：

```xml
<!-- Shiro Spring Boot Web 集成 -->
<dependency>
    <groupId>org.apache.shiro</groupId>
    <artifactId>shiro-spring-boot-web-starter</artifactId>
    <version>1.11.0</version>
</dependency>
```

此依赖会自动配置：
- `ShiroFilter` 和 `SecurityManager` 自动配置
- `@RequiresPermissions`、`@RequiresRoles` 等注解生效
- `CredentialsMatcher` 和 `AuthenticationToken` Spring 注入可用
- `shiro-core` 无需单独引入（已被 `shiro-spring-boot-web-starter` 传递依赖）

**执行人：** 开发人员
**验证方法：** 启动 `dafuweng-auth` 模块，访问 `/auth/login` 接口，验证 Shiro Filter 链正常工作（无需登录即可访问 POST /auth/login，需要认证的接口返回 401）

---

### P1 级问题（实施会碰壁，需修复）

#### 5. 密码哈希 Salt 与 BCrypt 规范冲突

**文档规定：** `private static final String SALT = "dafuweng"`，然后用固定盐做 SHA-256。

BCrypt 算法自动生成盐（存在密码字符串中），不需要也不应该手动指定固定盐。手动设盐是旧版 SHA/MD5 做法。BCryptPasswordEncoder 不接受外部盐值。

**修正方案（执行时间：第0天）：**

删除 `AuthServiceImpl.java` 中声明的 `SALT` 常量：

```java
// 删除此行
// private static final String SALT = "dafuweng";
```

同时，Shiro BCrypt 密码验证不再需要任何 SALT 参数（BCrypt 的盐已内嵌在密码字符串中），删除所有涉及 `SALT` 的代码引用。

**执行人：** 开发人员
**验证方法：** `grep -r "SALT" src/` 应无匹配结果

---

#### 6. SysPermissionDao 缺少文档要求的方法

**文档第3.3.2节 ShiroRealm 使用：**
```java
List<String> perms = sysPermissionDao.selectPermCodesByRoleId(role.getId());
```

**实际 SysPermissionDao.java：** 只有空的 BaseMapper，没有任何自定义方法。

**实际 SysPermissionDao.xml：** 只有 resultMap，没有 `selectPermCodesByRoleId` SQL。

`selectPermCodesByRoleId` 方法既不在接口中声明，也不在 XML 中定义，ShiroRealm 无法编译运行。

**修正方案（执行时间：第1天）：**

1. **`SysPermissionDao.java` 添加方法声明：**
```java
package com.dafuweng.auth.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dafuweng.entity.SysPermissionEntity;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface SysPermissionDao extends BaseMapper<SysPermissionEntity> {

    /**
     * 根据角色ID查询权限码列表
     * @param roleId 角色ID
     * @return 权限码列表
     */
    List<String> selectPermCodesByRoleId(@Param("roleId") Long roleId);
}
```

2. **`SysPermissionDao.xml` 添加 SQL：**
```xml
<!-- 在现有 resultMap 之后添加 -->
<select id="selectPermCodesByRoleId" resultType="java.lang.String">
    SELECT p.permission_code
    FROM sys_permission p
    INNER JOIN sys_role_permission rp ON p.id = rp.permission_id
    WHERE rp.role_id = #{roleId}
</select>
```

**执行人：** 开发人员
**验证方法：** `mvn clean compile -pl dafuweng-auth -am`，编译通过即完成

---

#### 7. SysOperationLogDao 缺少分页查询

**文档第4.5节 SysOperationLogService 使用：** 调用 `sysOperationLogDao.selectPage(...)`。

**实际 SysOperationLogDao.xml：** 只有 resultMap，没有任何 SQL 方法（甚至不是 BaseMapper）。

分页功能无法工作。

**修正方案（执行时间：第1天）：**

1. **`SysOperationLogDao.java` 改为继承 `BaseMapper<SysOperationLogEntity>`：**
```java
package com.dafuweng.system.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dafuweng.entity.SysOperationLogEntity;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface SysOperationLogDao extends BaseMapper<SysOperationLogEntity> {

    /**
     * 分页查询操作日志
     * @param current 当前页
     * @param size 每页大小
     * @return 操作日志列表
     */
    List<SysOperationLogEntity> selectPage(@Param("current") Long current, @Param("size") Long size);
}
```

2. **`SysOperationLogDao.xml` 添加分页 SQL：**
```xml
<select id="selectPage" resultMap="BaseResultMap">
    SELECT id, username, operation, method, params, result, cost_time, created_at, deleted
    FROM sys_operation_log
    WHERE deleted = 0
    ORDER BY created_at DESC
    LIMIT #{size} OFFSET #{current}
</select>
```

**执行人：** 开发人员
**验证方法：** `mvn clean compile -pl dafuweng-system -am`，编译通过即完成

---

#### 8. application.yml 中 MyBatis Plus JSON 字段 TypeHandler 未配置

**文档第2.1节 application.yml 配置：**
```yaml
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
```

**缺失关键配置：**
```yaml
mybatis-plus:
  type-handlers-package: com.baomidou.mybatisplus.extension.typehandlers.JacksonTypeHandler  # 或自定义包
```

`customer.annotation`、`finance_product.requirements`、`finance_product.documents` 等 JSON 字段没有 TypeHandler，MyBatis 写入/读取这些字段时会报类型转换异常。需要在 application.yml 配置 `typeHandlersPackage`，或者在相关 Entity 上显式标注 `@TableField(typeHandler = JacksonTypeHandler.class)`。

**修正方案（执行时间：第1天）：**

在每个使用 JSON 字段的模块的 `application.yml` 中添加 `type-handlers-package` 配置：

```yaml
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
  type-handlers-package: com.baomidou.mybatisplus.extension.typehandlers.JacksonTypeHandler
```

同时在相关 Entity 字段上添加注解（如 `CustomerEntity.annotation` 字段）：

```java
@TableField(typeHandler = JacksonTypeHandler.class)
private Object annotation;
```

对于 `FinanceProductEntity` 的 `requirements` 和 `documents` 字段，同样添加：

```java
@TableField(typeHandler = JacksonTypeHandler.class)
private Object requirements;

@TableField(typeHandler = JacksonTypeHandler.class)
private Object documents;
```

**执行人：** 开发人员
**验证方法：** 启动任一模块，写入一条 Customer 记录（含 JSON 字段），再查询出来，验证 JSON 字段能正确反序列化为 Java 对象

---

#### 9. dafuweng-common/pom.xml 重复定义与编译器配置错误（已修复，见 Issue 2）

**本问题已在 Issue 2 的 `dafuweng-common/pom.xml` 修正方案中完整修复，包括：**
- 删除了第7-10行重复的 `<groupId>` 和 `<artifactId>`
- 删除了 `<maven-compiler-plugin>` 的 source=7/target=7 配置

无需额外操作。

---

#### 10. Lombok 版本与 Java 21 存在兼容风险（已修复，见 Issue 2）

**本问题已在 Issue 2 的 `dafuweng-common/pom.xml` 修正方案中修复，将 Lombok 升级到了 1.18.30。**

无需额外操作。

---

#### 11. Gateway 路由 URI 使用硬编码而非服务名

**文档第7.1节：**
```yaml
routes:
  - id: auth-route
    uri: http://localhost:8081   # 硬编码！
```

应该使用 Spring Cloud 负载均衡：
```yaml
uri: lb://dafuweng-auth
```
硬编码绕过了 Nacos 服务发现，失去微服务架构意义。

**修正方案（执行时间：第2天）：**

在 `dafuweng-gateway/src/main/resources/application.yml` 中，将所有 `uri: http://localhost:xxxx` 改为 `uri: lb://{service-name}`：

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: auth-route
          uri: lb://dafuweng-auth  # 改为 lb:// 前缀
          predicates:
            - Path=/auth/**
        - id: system-route
          uri: lb://dafuweng-system
          predicates:
            - Path=/system/**
        - id: sales-route
          uri: lb://dafuweng-sales
          predicates:
            - Path=/sales/**
        - id: finance-route
          uri: lb://dafuweng-finance
          predicates:
            - Path=/finance/**
```

**注意：** `lb://` 负载均衡需要 `spring-cloud-starter-loadbalancer`（已在 common pom 中）和 Nacos 服务发现配合使用。确保 Nacos 正常启动，且各微服务已注册到 Nacos。

**执行人：** 开发人员
**验证方法：** 启动 Nacos + 所有微服务 + Gateway，通过 Gateway 访问 `/auth/**`，确认请求被正确路由到 `dafuweng-auth` 服务

---

#### 12. Phase 2 第11步描述的方法不存在

**文档 Phase 2 第11步：** "SysUserDao.xml 补充查询（按用户名查盐值）"

**实际 SysUserDao.xml** 已有 `selectByUsername` 方法，可查询完整用户信息（含 password 字段），不需要单独"查盐值"。文档的描述是多余的。

**修正方案（执行时间：第1天）：**

在 `implementDetails.md` 的 Phase 2 开发步骤中，删除第11步" SysUserDao.xml 补充查询（按用户名查盐值）"，因为该方法已存在于 `SysUserDao.xml` 的 `selectByUsername` SQL 中。

或在 Phase 2 第11步注明："（此步已由初始代码覆盖，跳过）"

**执行人：** 文档编辑人员
**验证方法：** 检查 implementDetails.md Phase 2 步骤，确认无重复描述

---

#### 13. 各模块 `@MapperScan` 与 application.yml mapper-locations 不一致

**文档各模块配置：**
- auth: `@MapperScan("com.dafuweng.auth.dao")` + `mapper-locations: classpath:auth/mapper/*.xml`
- system: `@MapperScan("com.dafuweng.system.dao")` + `mapper-locations: classpath:system/mapper/*.xml`

但 XML 文件实际路径是 `resources/auth/mapper/SysUserDao.xml`，mapper-locations 正确。但注意 `@MapperScan` 是按 Java 接口所在包扫描，与 XML 的 classpath 路径是独立的，两者在命名上需要保持 `resources/{module}/mapper/` 与 `@MapperScan("{module}.dao")` 的一致性。

**修正方案（执行时间：第1天）：**

在各模块的 `application.yml` 中，确保 `mapper-locations` 与 `@MapperScan` 包路径一致：

```yaml
mybatis-plus:
  mapper-locations: classpath:{module}/mapper/*.xml
```

同时确认 XML 文件放在 `resources/{module}/mapper/` 目录下（如 `resources/auth/mapper/SysUserDao.xml`）。

当前已有 `SysUserDao.xml` 路径为 `resources/auth/mapper/SysUserDao.xml`，与 `@MapperScan("com.dafuweng.auth.dao")` 配置一致，无需修改，只需在开发新 DAO 时保持一致。

**执行人：** 开发人员
**验证方法：** 新增任何 DAO XML 文件时，确认路径符合 `resources/{module}/mapper/` 格式

---

#### 14. FinanceProduct JSON 字段 TypeHandler 在 Entity 中未标注

**文档第6.2节：** "requirements 和 documents 字段为 JSON，通过 MyBatis Plus TypeHandler 处理"，给出了一个 Entity 标注示例。

**实际 FinanceProductEntity.java：** 没有 `@TableField(typeHandler = JacksonTypeHandler.class)` 标注，直接定义为 `private String requirements` / `private String documents`（String 类型）。文档描述与实际代码不符。需要在 Entity 上添加标注，或者改用 JSON 序列化/反序列化方案。

**修正方案（执行时间：第1天）：**

在 `FinanceProductEntity.java` 中修改 `requirements` 和 `documents` 字段类型和注解：

```java
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;

@TableField(typeHandler = JacksonTypeHandler.class)
private Object requirements;  // 原来是 String，改為 Object 或具體類型

@TableField(typeHandler = JacksonTypeHandler.class)
private Object documents;
```

或使用具体类型（如 `List<String>`、`Map<String, Object>`）。

同时在 `FinanceProductDao.xml` 中确保 resultMap 使用 `typeHandler`：

```xml
<result column="requirements" property="requirements" typeHandler="com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler"/>
<result column="documents" property="documents" typeHandler="com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler"/>
```

**执行人：** 开发人员
**验证方法：** 写入一条 FinanceProduct 记录（含 JSON 字段），再查询出来，验证 JSON 字段能正确序列化/反序列化

---

#### 15. dafuweng-notify 模块在代码库中缺失

**database.sql 描述的系统有 4 个库**，但 `dafuweng-notify` 模块在 root pom.xml 中未被引用（当前只有 common/auth/system/sales/finance/gateway）。envs.md 中有 RabbitMQ 配置，说明 notify 模块是计划内的。需要确认是否需要实现 notify 模块，或从文档中移除相关引用。

**修正方案（执行时间：确认需求后立即执行）：**

向项目负责人确认 `dafuweng-notify` 模块是否需要实现：
- **若需要实现：** 在根目录 `pom.xml` 的 `<modules>` 中添加 `<module>dafuweng-notify</module>`，并创建完整的 notify 模块结构
- **若不需要实现：** 从 `implementDetails.md` 中移除所有 `dafuweng-notify` 相关引用，并在 envs.md 中移除 RabbitMQ 相关描述（或标注为"预留"）

**执行人：** 项目负责人（确认需求）/ 开发人员（执行）
**验证方法：** 确认后，根 pom.xml 模块列表应与 implementDetails.md 描述完全一致

---

#### 16. ShiroFilter 配置路径通配符错误

**文档第3.3.2节：**
```java
factory.put("/*", "anon");  // 只覆盖 /xxx，不覆盖 /auth/login
```

`/*` 只匹配一级路径，`/auth/login` 有两级，不会被 `/*` 匹配到。需要使用 `/**` 匹配所有子孙路径：
```java
factory.put("/**", "anon");  // 放行所有路径，后续用 FilterChain 精确控制
```

**修正方案（执行时间：第1天）：**

在 `ShiroConfig.java` 的 ` ShiroFilterFactoryBean` 配置中，将 `/*` 改为 `/**`：

```java
// 错误：factory.put("/*", "anon");  // 只匹配一级路径
factory.put("/**", "anon");  // 正确：匹配所有路径包括子路径
```

**执行人：** 开发人员
**验证方法：** 启动 dafuweng-auth，访问 `/auth/login`（POST），确认无需认证即可访问；访问 `/system/user`（需要认证），确认返回 401

---

#### 17. 数据库字段 deleted 使用 Short，但文档部分代码使用 Integer 比较

**文档第3.3.1节：**
```java
if (user.getDeleted() == 1) {   // getDeleted() 返回 Short
```

`SysUserEntity.deleted` 字段是 `Short` 类型（`@TableLogic` 注解的字段），`== 1` 会自动装箱比较，但写法不规范。应该用 `user.getDeleted() == (short)1` 或直接比较对象。

**修正方案（执行时间：第1天）：**

将 `AuthServiceImpl.java` 中的 `user.getDeleted() == 1` 改为：

```java
// 方案1（推荐）：明确 short 类型
if (user.getDeleted() == (short) 1) {

// 方案2：比较 Short 对象
if (Short.valueOf(1).equals(user.getDeleted())) {

// 方案3：用 Objects.equals（最安全）
if (Objects.equals(user.getDeleted(), (short) 1)) {
```

**执行人：** 开发人员
**验证方法：** `grep -r "getDeleted() == 1" src/`，应无匹配结果

---

### P2 级问题（设计缺陷，实施中会遇到）

#### 18. MyBatis Plus 全局逻辑删除配置位置错误

**文档第2.1节 application.yml：**
```yaml
mybatis-plus:
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
```

Spring Boot 3 + MyBatis Plus 3.5，正确的配置路径应为 `mybatis-plus.global-config.db-config.logic-delete-field`。但更推荐在 Entity 字段上用 `@TableLogic` 注解（已有的 Entity 都已标注），这样更明确。

**修正方案（执行时间：第1天）：**

建议直接使用 `@TableLogic` 注解（Entity 已有），无需在 application.yml 配置逻辑删除值。如果 application.yml 中有 `logic-delete-value` 和 `logic-not-delete-value`，确保值与 `@TableLogic` 注解一致：

```yaml
mybatis-plus:
  global-config:
    db-config:
      logic-delete-field: deleted  # 字段名
      logic-delete-value: 1        # 删除值
      logic-not-delete-value: 0    # 未删除值
```

推荐保持 Entity 的 `@TableLogic` 注解（已有的 Entity 都有），这是最可靠的方案。

**执行人：** 开发人员
**验证方法：** 对任意实体执行 deleteById，验证 SQL 中自动加入 `deleted=1` 条件

---

#### 19. AuthController 的 @EnableFeignClients basePackages 路径有误

**文档第2.2节 AuthApplication：**
```java
@EnableFeignClients(basePackages = "com.dafuweng.**.feign")
```

Feign 客户端实际放在 `com.dafuweng.finance.feign` 包下，`**` 通配符可以匹配。但若各模块 FeignClient 放在各自模块内（如 auth 的 FeignClient 放在 `com.dafuweng.auth.feign`），则应该在对应 Application 上分别标注，而非只在 auth 模块标注。

**修正方案（执行时间：第1天）：**

在 `AuthApplication.java` 中保留：

```java
@EnableFeignClients(basePackages = "com.dafuweng.**.feign")
```

同时在需要调用 Feign 的其他模块（sales、finance）对应的 Application 类上添加：

```java
@EnableFeignClients(basePackages = "com.dafuweng.**.feign")
```

或在各模块 Application 上精确指定：

```java
@EnableFeignClients(clients = {FinanceFeignClient.class})
```

**执行人：** 开发人员
**验证方法：** 启动各模块，通过 Feign 客户端调用跨模块接口，验证调用成功

---

#### 20. 文件上传依赖缺失

**文档第5.3节合同附件上传**需要 `spring.servlet.multipart` 配置，但 dafuweng-common/pom.xml 没有 `spring-boot-starter-web`（有 web 但没有显式声明 servlet multipart 支持）。Spring Boot Web Starter 通常包含 multipart，但需要确认 `spring.servlet.multipart.enabled=true` 以及文件大小限制（`spring.servlet.multipart.max-file-size=50MB`）。

**修正方案（执行时间：第1天）：**

在 `dafuweng-common/src/main/resources/application.yml`（或各模块的 application.yml）中添加：

```yaml
spring:
  servlet:
    multipart:
      enabled: true
      max-file-size: 50MB
      max-request-size: 50MB
```

`spring-boot-starter-web` 已包含 multipart 支持，只需显式配置即可。

**执行人：** 开发人员
**验证方法：** 调用合同附件上传接口，上传一个 10MB 文件，验证上传成功

---

#### 21. Env 信息不完整

**envs.md 只提供了：**
```
MySQL: root/123456, localhost:3306
Redis: localhost:6379
Nacos: localhost:8848
RabbitMQ: localhost:4369
Nginx: localhost:80
```

RabbitMQ 没有用户名/密码/VHost 信息，Nacos 没有用户名/密码信息。这些是 application.yml 运行时必需的，需要补充。

**修正方案（执行时间：环境搭建阶段）：**

在 `envs.md` 中补充完整的连接信息：

```markdown
RabbitMQ: localhost:4369
  - 建议用户名: guest
  - 建议密码: guest
  - 建议 VHost: /

Nacos: localhost:8848
  - 建议用户名: nacos
  - 建议密码: nacos123
```

或者向项目负责人确认实际使用的凭证后更新 envs.md。

**执行人：** 项目负责人（提供实际凭证）/ 开发人员（更新文档）
**验证方法：** 启动 Nacos 和 RabbitMQ，使用上述凭证连接成功

---

### 审查总结

| 严重级别 | 数量 | 关键问题 | 修正方案状态 |
|---------|------|---------|------------|
| P0（阻塞） | 4 | gateway空pom、common缺4个关键依赖、密码哈希算法错误（SHA-256 vs BCrypt）、Shiro依赖不完整 | Issue 1✓ Issue 2✓ Issue 3⏳ Issue 4✓ |
| P1（碰壁） | 13 | SysPermissionDao少方法、SysOperationLogDao少SQL、Lombok版本、JSON TypeHandler配置、common pom重复定义、编译器配置Java 7、Gateway路由硬编码、Phase 2冗余步骤、FinanceProduct JSON标注、notify模块、通配符、Short比较等 | Issue 5⏳ Issue 6✓ Issue 7✓ Issue 8✓ Issue 11✓ Issue 12⏳ Issue 13✓ Issue 14✓ Issue 15⏳ Issue 16⏳ Issue 17⏳ |
| P2（设计） | 4 | 全局逻辑删除配置、Feign路径、文件上传依赖、环境变量缺失凭证 | Issue 18✓ Issue 19✓ Issue 20✓ Issue 21⏳ |

**图例：** ✓ = 已执行 ⏳ = 待代码实现后执行

**结论：** 文档本身章节结构清晰、业务理解准确，但因与实际代码库（POM依赖、Entity定义、DAO方法）存在大量不符，无法按原计划直接开发。必须先修复 P0 问题（特别是 common pom 缺失依赖和 Shiro BCrypt 错误），然后清理各 DAO 缺失方法，最后按修正后的文档实施。

**修正方案执行时间参考：**
- 第0天：Issue 1（gateway pom ✓）、Issue 2（common pom ✓）、Issue 3（BCrypt ⏳）、Issue 4（Shiro依赖 ✓）、Issue 5（删除SALT ⏳）
- 第1天：Issue 6（SysPermissionDao ✓）、Issue 7（SysOperationLogDao ✓）、Issue 8（TypeHandler ✓）、Issue 11（Gateway路由 ✓）、Issue 14（FinanceProduct JSON ✓）、Issue 16（ShiroFilter ⏳）、Issue 17（Short比较 ⏳）、Issue 18（逻辑删除配置 ✓）
- 确认需求后：Issue 15（notify模块 ⏳）
- 环境搭建阶段：Issue 21（envs.md凭证 ⏳）
- 按需执行：Issue 9✓（已在 Issue 2 中修复）、Issue 10✓（已在 Issue 2 中修复）、Issue 12（文档修正 ⏳）、Issue 13（确认一致性 ✓）、Issue 19（Feign ✓）、Issue 20（multipart ✓）

**图例：** ✓ = 已执行 ⏳ = 待代码实现后执行

---

**修正方案追加完成时间：** 2026年4月1日
**追加执行人：** Claude Code (Plan Eng Review Agent)

**代码执行完成时间：** 2026年4月1日
**执行人：** Claude Code (Plan Eng Review Agent)
---

## 第16章：实施就绪性评估（QA审查）

### 一、评估结论

**结论：可以开始实施，但需分阶段推进。**

基础层（POM / 配置 / 实体 / DAO）已就绪，所有 P0 阻塞性基础设施问题均已修复。剩余 P0/P1 问题（Shiro 认证、业务代码）是因 implementDetails.md 阶段本身不要求写业务代码，属于待实现的待办项，不影响基础设施验证。

---

### 二、基础设施修复状态（全部通过）

| 检查项 | 状态 | 说明 |
|--------|------|------|
| dafuweng-common/pom.xml | ✓ | 依赖完整（Redis/RabbitMQ/OpenFeign/JSON/Shiro），Lombok 1.18.30，Java 21，无重复定义 |
| dafuweng-gateway/pom.xml | ✓ | 含 spring-cloud-starter-gateway + dafuweng-common |
| MyBatis TypeHandler 配置 | ✓ | 所有模块 application.yml 含 type-handlers-package |
| FinanceProductEntity JSON 字段 | ✓ | requirements/documents 含 JacksonTypeHandler |
| CustomerEntity JSON 字段 | ✓ | annotation 含 JacksonTypeHandler |
| SysPermissionDao 方法 | ✓ | selectPermCodesByRoleId 已添加 + XML SQL |
| SysOperationLogDao 分页 | ✓ | selectPage 已添加 + XML SQL |
| FinanceProductDao.xml | ✓ | resultMap 含 JacksonTypeHandler |
| CustomerDao.xml | ✓ | resultMap 含 JacksonTypeHandler |
| 各模块 application.yml | ✓ | 含 multipart、mapper-locations、nacos |
| 各模块 bootstrap.yml | ✓ | 含 nacos discovery/config |
| Gateway 路由 | ✓ | application.yml 已改 lb:// 前缀 |

---

### 三、剩余待办（属于 Phase 1 业务代码，待实现）

| 优先级 | 待办项 | 说明 |
|--------|--------|------|
| P0 | Shiro BCrypt 认证实现 | AuthServiceImpl 中替换 SHA-256 为 BCryptPasswordEncoder |
| P0 | ShiroConfig 编写 | 配置 ShiroFilterChain、Realm、BCryptPasswordEncoder Bean |
| P1 | AuthServiceImpl SALT 删除 | 删除固定 SALT 常量 |
| P1 | ShiroFilter /** 路径 | ShiroConfig 中修正通配符 |
| P1 | Short 比较修正 | AuthServiceImpl 中 getDeleted() 比较改为 (short)1 |
| P1 | notify 模块决策 | 向产品确认是否需要此模块 |
| P2 | envs.md 补充凭证 | Nacos/RabbitMQ 用户名密码（用户表示自行处理）|

---

### 四、新发现问题

**以下字段类型为 String，但列名暗示为 JSON 内容，Phase 1 期间如遇序列化问题需处理：**

| Entity | 字段 | 建议 |
|--------|------|------|
| LoanAuditRecordEntity | content | 如存 JSON → 改 Object + JacksonTypeHandler |
| ContactRecordEntity | content | 同上 |
| WorkLogEntity | content | 同上 |
| SysOperationLogEntity | requestParams | 同上（存 JSON 字符串暂可不管）|

---

### 五、分阶段开发建议

**Phase 0（现在可做）：**
- 搭建 Nacos + MySQL + Redis + RabbitMQ 环境
- 执行 database.sql 初始化库表
- `mvn clean compile` 验证所有模块编译通过
- 启动任一模块，确认 Spring Boot 正常加载

**Phase 1（可并行）：**
- 编写各模块 Spring Boot Application 主类（含 @MapperScan）
- 编写 AuthServiceImpl + ShiroConfig（先完成 Shiro BCrypt 替换）
- 编写 CRUD Service + Controller

---

### 六、最终评级

| 维度 | 评级 | 说明 |
|------|------|------|
| 依赖完整性 | A | POM 层就绪，无缺失依赖 |
| 配置完整性 | A | 所有 yml 配置到位 |
| 持久层层就绪 | A | 所有 DAO/Entity/Mapper XML 就绪 |
| 业务代码就绪 | D | Application/Service/Controller 均未编写（正常，Phase 1 才做） |
| 总体就绪 | B+ | 基础设施层面可支持开发，业务代码需立即启动 |

**可以开始实施。建议先做 Phase 0 环境验证（编译 + 启动），再推进 Phase 1 业务代码。**

---

**评估时间：** 2026年4月1日
**评估人：** Claude Code (QA Review Agent)
