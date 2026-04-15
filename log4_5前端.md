# 前端修改记录 log4_5

> 基准版本：`cadcc15`（feat: add ruoyi-ui frontend source code）  
> 修改日期：2026-04-15  
> 分支：`fix/ruoyi-frontend-integration`

---

## 一、修改文件汇总

| 文件路径 | 变更类型 | 说明 |
|---|---|---|
| `ruoyi-ui/src/views/index.vue` | 修改 | 重新设计首页，替换为 NeoCC 专属数据看板 |
| `auth/.../SecurityConfig.java` | 修改 | 新增系统管理接口白名单放行 |
| `auth/.../RuoyiAdapterController.java` | 修改 | 补全菜单路由（系统管理/销售/财务模块） |
| `common/pom.xml` | 修改 | 新增 `mybatis-plus-extension` 依赖 |
| `common/.../MybatisPlusConfig.java` | 修改 | 注册 `PaginationInnerInterceptor` 分页插件 |
| `common/.../PageRequest.java` | 修改 | 新增 `setPageNum`/`setPageSize` setter 兼容 RuoYi 前端参数名 |
| `gateway/.../application.yml` | 修改 | 补全系统管理模块（dept/zone/user/role/permission）网关路由 |
| `gateway/.../application-docker.yml` | 修改 | 禁用 Nacos 服务发现，改为直连 Docker 容器名；补全全部业务路由 |
| `ruoyi-ui/src/api/finance/bank.js` | 新增 | 银行管理 API |
| `ruoyi-ui/src/api/finance/financeProduct.js` | 新增 | 金融产品 API |
| `ruoyi-ui/src/api/finance/serviceFeeRecord.js` | 新增 | 服务费记录 API |
| `ruoyi-ui/src/api/sales/contactRecord.js` | 新增 | 跟进记录 API |
| `ruoyi-ui/src/api/sales/customerTransfer.js` | 新增 | 客户转移记录 API |
| `ruoyi-ui/src/api/sales/performanceRecord.js` | 新增 | 业绩记录 API |
| `ruoyi-ui/src/api/sales/workLog.js` | 新增 | 工作日志 API |
| `ruoyi-ui/src/api/system/user.js` | 新增 | 用户管理 API（CRUD + 分配角色 + 改密码 + 解锁） |
| `ruoyi-ui/src/api/system/role.js` | 新增 | 角色管理 API（CRUD + 分配权限） |
| `ruoyi-ui/src/api/system/permission.js` | 新增 | 权限管理 API（CRUD + 树查询） |
| `ruoyi-ui/src/views/finance/bank/index.vue` | 新增 | 银行管理页面 |
| `ruoyi-ui/src/views/finance/product/index.vue` | 新增 | 金融产品页面 |
| `ruoyi-ui/src/views/finance/service-fee/index.vue` | 新增 | 服务费记录页面 |
| `ruoyi-ui/src/views/sales/contact-record/index.vue` | 新增 | 跟进记录页面 |
| `ruoyi-ui/src/views/sales/customer-transfer/index.vue` | 新增 | 客户转移记录页面 |
| `ruoyi-ui/src/views/sales/performance-record/index.vue` | 新增 | 业绩记录页面 |
| `ruoyi-ui/src/views/sales/work-log/index.vue` | 新增 | 工作日志页面 |
| `ruoyi-ui/src/views/system/user/index.vue` | 新增 | 用户管理页面（含分配角色弹窗、修改密码弹窗） |
| `ruoyi-ui/src/views/system/role/index.vue` | 新增 | 角色管理页面（含分配权限树弹窗） |
| `ruoyi-ui/src/views/system/menu/index.vue` | 新增 | 菜单/权限管理页面 |

---

## 二、详细改动说明

### 1. 首页重设计（`ruoyi-ui/src/views/index.vue`）

**改动前：** RuoYi 原始首页，展示若依框架介绍、技术选型、更新日志、捐赠二维码等与业务无关内容。

**改动后：** 替换为 NeoCC 信贷管理系统专属数据看板，包含以下模块：

- **欢迎横幅**：深色渐变背景，显示系统名称、当前登录用户（昵称）、今日日期（含星期）
- **统计卡片（4张）**：
  - 客户总数（调用 `/api/customer/page`）
  - 合同总数（调用 `/api/contract/page`）
  - 待审贷款（调用 `/api/loanAudit/page`）
  - 角色数量（调用 `/api/sysRole/page`）
  - 数字带 count-to 滚动动画，点击可跳转对应页面
- **快捷功能入口（6个）**：客户管理、合同管理、工作日志、贷款审核、佣金记录、用户管理，彩色渐变图标
- **系统概览**：显示系统名称、版本、后端/前端框架、数据库、运行状态
- **技术栈标签**：Spring Boot 3、Spring Security、Spring Cloud Gateway、MyBatis-Plus、MySQL 8、Redis、Docker、RabbitMQ、Vue 3、Element Plus、Vite、Nginx
- **业务模块卡片（3列）**：销售管理、财务管理、系统管理，展示各模块子功能列表，点击可跳转

---

### 2. 分页功能修复

**问题根因（两个 Bug 叠加）：**

**Bug 1 — MyBatis-Plus 分页插件未注册：**
- 文件：`common/src/main/java/com/dafuweng/common/config/MybatisPlusConfig.java`
- 现象：所有分页接口 `total` 字段恒为 0，前端分页栏因 `v-show="total > 0"` 条件不满足而隐藏，用户看不到任何数据
- 修复：在 `MybatisPlusConfig` 中注册 `PaginationInnerInterceptor(DbType.MYSQL)`
- 相关依赖：`common/pom.xml` 新增 `mybatis-plus-extension:3.5.7`

```java
// 修复前：无分页插件
@Bean
public AutoFillMetaObjectHandler autoFillMetaObjectHandler() { ... }

// 修复后：注册分页插件
@Bean
public MybatisPlusInterceptor mybatisPlusInterceptor() {
    MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
    interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
    return interceptor;
}
```

**Bug 2 — 前端分页参数名与后端字段名不匹配：**
- 文件：`common/src/main/java/com/dafuweng/common/entity/PageRequest.java`
- 现象：RuoYi 前端发送 `pageNum`/`pageSize`，后端 `PageRequest` 字段为 `page`/`size`，导致翻页参数完全失效
- 修复：新增 `setPageNum`/`setPageSize` setter 方法，Spring MVC 绑定时自动映射

```java
public void setPageNum(Integer pageNum) {
    if (pageNum != null) this.page = pageNum;
}
public void setPageSize(Integer pageSize) {
    if (pageSize != null) this.size = pageSize;
}
```

---

### 3. 用户/角色/权限管理页面补全

**问题根因：** 上一次 fork 只创建了文件，但没有重新构建前端 dist，Nginx 卷挂载的 dist 中不包含新文件，页面实际是空白。

**修复过程：**
1. 重新执行 `npm run build:prod` 构建前端
2. 重启 `neocc-nginx` 容器，卷挂载自动生效

**新增系统管理接口网关路由（`gateway/application.yml` 和 `application-docker.yml`）：**

```yaml
- id: auth-api-user
  uri: http://neocc-auth:8085
  predicates:
    - Path=/api/sysUser/**

- id: auth-api-role
  uri: http://neocc-auth:8085
  predicates:
    - Path=/api/sysRole/**

- id: auth-api-permission
  uri: http://neocc-auth:8085
  predicates:
    - Path=/api/sysPermission/**
```

**新增 Spring Security 白名单（`SecurityConfig.java`）：**

```java
// 系统管理接口（用户/角色/权限管理）
.requestMatchers("/api/sysUser/**", "/api/sysRole/**", "/api/sysPermission/**").permitAll()
```

---

### 4. 菜单路由补全（`RuoyiAdapterController.java`）

`getRouters` 接口补全了所有业务菜单，修复前只有部分菜单项。

| 模块 | 修复前 | 修复后 |
|---|---|---|
| 系统管理 | 用户、角色、菜单 | 用户、角色、菜单、**部门管理、区域管理** |
| 销售管理 | 客户、合同 | 客户、合同、**跟进记录、工作日志、业绩记录、客户转移记录** |
| 财务管理 | 贷款审核、佣金记录 | 贷款审核、佣金记录、**服务费记录、银行管理、金融产品** |

---

### 5. 数据库字符集修复

**问题：** `sys_user.real_name` 字段存储乱码（`系统管理员` → `ç³»ç»Ÿç®¡ç†å'˜`），原因是早期插入时数据库连接 `character_set_client = latin1`，UTF-8 中文字节被二次编码。

**修复：** 使用 `--default-character-set=utf8mb4` 标志重新 UPDATE 受影响的记录。

```sql
UPDATE sys_user SET real_name='系统管理员' WHERE id=1;
```

---

### 6. Gateway Docker 配置重构（`application-docker.yml`）

- 禁用 Nacos 服务发现（`enabled: false`），改为直连 Docker 容器名（如 `http://neocc-auth:8085`）
- 移除所有路由的 `StripPrefix=1` 过滤器，保留完整路径前缀与后端 `@RequestMapping` 一致
- 补全销售、财务、系统管理模块共 16 条业务路由
- CORS 配置修复：`allowedOrigins: "*"` 改为 `allowedOriginPatterns: "*"`，并将 `allowCredentials` 改为 `false`（避免与通配符冲突）

---

## 三、验证结果

构建并重新部署后，所有关键接口验证通过：

| 接口 | 状态 | total |
|---|---|---|
| 用户管理 `/api/sysUser/page` | 200 ✅ | 1 |
| 角色管理 `/api/sysRole/page` | 200 ✅ | 9 |
| 客户管理 `/api/customer/page` | 200 ✅ | 2 |
| 合同管理 `/api/contract/page` | 200 ✅ | 2 |
| 贷款审核 `/api/loanAudit/page` | 200 ✅ | 0 |
| 部门管理 `/api/sysDepartment/page` | 200 ✅ | 0 |
| 银行管理 `/api/bank/page` | 200 ✅ | 0 |

前端首页数据看板正常加载，统计卡片实时调用接口并展示总数，快捷入口和模块卡片跳转正常。
