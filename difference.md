# NeoCC 后端与 RuoYi 前端接口差异文档

## 一、接口适配总览

本文档记录 NeoCC 后端与 RuoYi-Vue3 前端接口的差异及适配方案。

---

## 二、登录认证模块

### 2.1 前端需求接口

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取验证码 | GET | `/captchaImage` | 获取验证码图片 |
| 登录 | POST | `/login` | 用户登录 |
| 获取用户信息 | GET | `/getInfo` | 获取当前登录用户信息 |
| 获取路由菜单 | GET | `/getRouters` | 获取动态路由菜单 |
| 退出登录 | POST | `/logout` | 用户退出 |
| 注册 | POST | `/register` | 用户注册 |
| 解锁屏幕 | POST | `/unlockscreen` | 解锁屏幕 |

### 2.2 后端已有接口

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 登录 | POST | `/api/sysUser/login` | 原有登录接口 |
| 登出 | POST | `/api/sysUser/logout` | 原有登出接口 |

### 2.3 差异处理

| 接口 | 处理方式 | 说明 |
|------|----------|------|
| `/captchaImage` | 放行直接通过 | 返回模拟数据，关闭验证码功能 |
| `/login` | 适配改造 | 调用原有登录逻辑，返回 RuoYi 格式 |
| `/getInfo` | 新增实现 | 整合原有用户信息接口，返回 RuoYi 格式 |
| `/getRouters` | 新增实现 | 根据 NeoCC 功能树生成路由菜单 |
| `/logout` | 新增实现 | 调用原有登出逻辑 |
| `/register` | 放行直接通过 | 返回成功，暂不开放注册 |
| `/unlockscreen` | 放行直接通过 | 返回成功 |

---

## 三、系统管理模块

### 3.1 用户管理

#### 前端需求接口

| 接口 | 方法 | 路径 |
|------|------|------|
| 查询用户列表 | GET | `/system/user/list` |
| 查询用户详细 | GET | `/system/user/{userId}` |
| 新增用户 | POST | `/system/user` |
| 修改用户 | PUT | `/system/user` |
| 删除用户 | DELETE | `/system/user/{userId}` |
| 重置密码 | PUT | `/system/user/resetPwd` |
| 修改状态 | PUT | `/system/user/changeStatus` |
| 查询个人信息 | GET | `/system/user/profile` |
| 修改个人信息 | PUT | `/system/user/profile` |
| 修改个人密码 | PUT | `/system/user/profile/updatePwd` |
| 上传头像 | POST | `/system/user/profile/avatar` |
| 查询授权角色 | GET | `/system/user/authRole/{userId}` |
| 保存授权角色 | PUT | `/system/user/authRole` |
| 部门下拉树 | GET | `/system/user/deptTree` |

#### 后端已有接口

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 查询用户 | GET | `/api/sysUser/{id}` | 基本查询 |
| 分页查询 | GET | `/api/sysUser/page` | 分页列表 |
| 新增用户 | POST | `/api/sysUser` | 创建用户 |
| 修改用户 | PUT | `/api/sysUser` | 更新用户 |
| 删除用户 | DELETE | `/api/sysUser/{id}` | 删除用户 |
| 分配角色 | PUT | `/api/sysUser/{id}/roles` | 角色分配 |
| 解锁用户 | PUT | `/api/sysUser/{id}/unlock` | 解锁账号 |
| 修改密码 | PUT | `/api/sysUser/{id}/password` | 修改密码 |

#### 差异处理

- **已有功能**：用户增删改查、角色分配、密码修改、解锁
- **缺失功能**：个人信息管理、头像上传、授权角色查询
- **处理方式**：创建占位接口，返回空数据或模拟数据

### 3.2 角色管理

#### 前端需求接口

| 接口 | 方法 | 路径 |
|------|------|------|
| 查询角色列表 | GET | `/system/role/list` |
| 查询角色详细 | GET | `/system/role/{roleId}` |
| 新增角色 | POST | `/system/role` |
| 修改角色 | PUT | `/system/role` |
| 数据权限 | PUT | `/system/role/dataScope` |
| 修改状态 | PUT | `/system/role/changeStatus` |
| 删除角色 | DELETE | `/system/role/{roleId}` |
| 已授权用户 | GET | `/system/role/authUser/allocatedList` |
| 未授权用户 | GET | `/system/role/authUser/unallocatedList` |
| 取消授权 | PUT | `/system/role/authUser/cancel` |
| 批量取消 | PUT | `/system/role/authUser/cancelAll` |
| 批量授权 | PUT | `/system/role/authUser/selectAll` |
| 部门树 | GET | `/system/role/deptTree/{roleId}` |

#### 后端已有接口

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 查询角色 | GET | `/api/sysRole/{id}` | 基本查询 |
| 分页查询 | GET | `/api/sysRole/page` | 分页列表 |
| 新增角色 | POST | `/api/sysRole` | 创建角色 |
| 修改角色 | PUT | `/api/sysRole` | 更新角色 |
| 删除角色 | DELETE | `/api/sysRole/{id}` | 删除角色 |
| 查询权限 | GET | `/api/sysRole/{id}/permissions` | 权限查询 |
| 分配权限 | PUT | `/api/sysRole/{id}/permissions` | 权限分配 |

#### 差异处理

- **已有功能**：角色增删改查、权限分配
- **缺失功能**：数据权限、用户授权管理、部门树
- **处理方式**：创建占位接口

### 3.3 菜单管理

#### 前端需求接口

| 接口 | 方法 | 路径 |
|------|------|------|
| 查询菜单列表 | GET | `/system/menu/list` |
| 查询菜单详细 | GET | `/system/menu/{menuId}` |
| 菜单下拉树 | GET | `/system/menu/treeselect` |
| 角色菜单树 | GET | `/system/menu/roleMenuTreeselect/{roleId}` |
| 新增菜单 | POST | `/system/menu` |
| 修改菜单 | PUT | `/system/menu` |
| 保存排序 | PUT | `/system/menu/updateSort` |
| 删除菜单 | DELETE | `/system/menu/{menuId}` |

#### 后端已有接口

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 查询权限树 | GET | `/api/sysPermission/tree` | 权限树形结构 |
| 子权限查询 | GET | `/api/sysPermission/children` | 子权限列表 |

#### 差异处理

- **已有功能**：权限树查询
- **缺失功能**：菜单管理完整 CRUD
- **处理方式**：创建占位接口，后续可整合权限管理

### 3.4 部门管理

#### 前端需求接口

| 接口 | 方法 | 路径 |
|------|------|------|
| 查询部门列表 | GET | `/system/dept/list` |
| 排除节点 | GET | `/system/dept/list/exclude/{deptId}` |
| 查询部门详细 | GET | `/system/dept/{deptId}` |
| 新增部门 | POST | `/system/dept` |
| 修改部门 | PUT | `/system/dept` |
| 保存排序 | PUT | `/system/dept/updateSort` |
| 删除部门 | DELETE | `/system/dept/{deptId}` |

#### 后端已有接口

无（NeoCC 使用组织架构概念，接口不同）

#### 差异处理

- **处理方式**：创建占位接口，后续可整合组织架构

### 3.5 岗位管理

#### 前端需求接口

| 接口 | 方法 | 路径 |
|------|------|------|
| 查询岗位列表 | GET | `/system/post/list` |
| 查询岗位详细 | GET | `/system/post/{postId}` |
| 新增岗位 | POST | `/system/post` |
| 修改岗位 | PUT | `/system/post` |
| 删除岗位 | DELETE | `/system/post/{postId}` |

#### 后端已有接口

无

#### 差异处理

- **处理方式**：创建占位接口

### 3.6 参数管理

#### 前端需求接口

| 接口 | 方法 | 路径 |
|------|------|------|
| 查询参数列表 | GET | `/system/config/list` |
| 查询参数详细 | GET | `/system/config/{configId}` |
| 根据键名查询 | GET | `/system/config/configKey/{configKey}` |
| 新增参数 | POST | `/system/config` |
| 修改参数 | PUT | `/system/config` |
| 删除参数 | DELETE | `/system/config/{configId}` |

#### 后端已有接口

无

#### 差异处理

- **处理方式**：创建占位接口

### 3.7 通知公告

#### 前端需求接口

| 接口 | 方法 | 路径 |
|------|------|------|
| 查询公告列表 | GET | `/system/notice/list` |
| 查询公告详细 | GET | `/system/notice/{noticeId}` |
| 新增公告 | POST | `/system/notice` |
| 修改公告 | PUT | `/system/notice` |
| 删除公告 | DELETE | `/system/notice/{noticeId}` |

#### 后端已有接口

无

#### 差异处理

- **处理方式**：创建占位接口

---

## 四、监控管理模块

### 4.1 在线用户

| 接口 | 方法 | 路径 | 处理方式 |
|------|------|------|----------|
| 查询在线用户 | GET | `/monitor/online/list` | 占位接口 |
| 强退用户 | DELETE | `/monitor/online/{tokenId}` | 占位接口 |

### 4.2 定时任务

| 接口 | 方法 | 路径 | 处理方式 |
|------|------|------|----------|
| 查询任务列表 | GET | `/monitor/job/list` | 占位接口 |
| 查询任务详细 | GET | `/monitor/job/{jobId}` | 占位接口 |
| 新增任务 | POST | `/monitor/job` | 占位接口 |
| 修改任务 | PUT | `/monitor/job` | 占位接口 |
| 修改状态 | PUT | `/monitor/job/changeStatus` | 占位接口 |
| 删除任务 | DELETE | `/monitor/job/{jobId}` | 占位接口 |
| 立即执行 | POST | `/monitor/job/run` | 占位接口 |
| 查询调度日志 | GET | `/monitor/jobLog/list` | 占位接口 |
| 删除调度日志 | DELETE | `/monitor/jobLog/{jobLogId}` | 占位接口 |
| 清空调度日志 | DELETE | `/monitor/jobLog/clean` | 占位接口 |

### 4.3 登录日志

| 接口 | 方法 | 路径 | 处理方式 |
|------|------|------|----------|
| 查询日志列表 | GET | `/monitor/logininfor/list` | 占位接口 |
| 删除日志 | DELETE | `/monitor/logininfor/{infoId}` | 占位接口 |
| 清空日志 | DELETE | `/monitor/logininfor/clean` | 占位接口 |
| 解锁用户 | GET | `/monitor/logininfor/unlock/{userName}` | 占位接口 |

### 4.4 操作日志

| 接口 | 方法 | 路径 | 处理方式 |
|------|------|------|----------|
| 查询日志列表 | GET | `/monitor/operlog/list` | 占位接口 |
| 删除日志 | DELETE | `/monitor/operlog/{operId}` | 占位接口 |
| 清空日志 | DELETE | `/monitor/operlog/clean` | 占位接口 |

### 4.5 数据监控

| 接口 | 方法 | 路径 | 处理方式 |
|------|------|------|----------|
| 查询缓存信息 | GET | `/monitor/cache` | 返回模拟数据 |
| 查询缓存名称 | GET | `/monitor/cache/getNames` | 占位接口 |
| 查询缓存键 | GET | `/monitor/cache/getKeys/{cacheName}` | 占位接口 |
| 查询缓存值 | GET | `/monitor/cache/getValue/{cacheName}/{cacheKey}` | 占位接口 |
| 清空缓存名 | DELETE | `/monitor/cache/clearCacheName/{cacheName}` | 占位接口 |
| 清空缓存键 | DELETE | `/monitor/cache/clearCacheKey/{cacheKey}` | 占位接口 |
| 清空所有缓存 | DELETE | `/monitor/cache/clearCacheAll` | 占位接口 |

### 4.6 服务监控

| 接口 | 方法 | 路径 | 处理方式 |
|------|------|------|----------|
| 查询服务器信息 | GET | `/monitor/server` | 返回模拟数据 |

---

## 五、销售管理模块

### 5.1 客户管理

| 接口 | 方法 | 路径 | 后端已有 | 处理方式 |
|------|------|------|----------|----------|
| 查询客户列表 | GET | `/sales/customer/list` | 有 | 整合原有接口 |
| 查询客户详细 | GET | `/sales/customer/{customerId}` | 有 | 整合原有接口 |
| 新增客户 | POST | `/sales/customer` | 有 | 整合原有接口 |
| 修改客户 | PUT | `/sales/customer` | 有 | 整合原有接口 |
| 删除客户 | DELETE | `/sales/customer/{customerId}` | 有 | 整合原有接口 |

### 5.2 合同管理

| 接口 | 方法 | 路径 | 后端已有 | 处理方式 |
|------|------|------|----------|----------|
| 查询合同列表 | GET | `/sales/contract/list` | 有 | 整合原有接口 |
| 查询合同详细 | GET | `/sales/contract/{contractId}` | 有 | 整合原有接口 |
| 新增合同 | POST | `/sales/contract` | 有 | 整合原有接口 |
| 修改合同 | PUT | `/sales/contract` | 有 | 整合原有接口 |
| 删除合同 | DELETE | `/sales/contract/{contractId}` | 有 | 整合原有接口 |
| 签署合同 | PUT | `/sales/contract/{contractId}/sign` | 有 | 整合原有接口 |

### 5.3 联系记录

| 接口 | 方法 | 路径 | 后端已有 | 处理方式 |
|------|------|------|----------|----------|
| 查询联系记录 | GET | `/sales/contact/list` | 有 | 整合原有接口 |
| 新增联系记录 | POST | `/sales/contact` | 有 | 整合原有接口 |
| 修改联系记录 | PUT | `/sales/contact` | 有 | 整合原有接口 |
| 删除联系记录 | DELETE | `/sales/contact/{contactId}` | 有 | 整合原有接口 |

### 5.4 业绩管理

| 接口 | 方法 | 路径 | 后端已有 | 处理方式 |
|------|------|------|----------|----------|
| 查询业绩列表 | GET | `/sales/performance/list` | 有 | 整合原有接口 |
| 查询业绩统计 | GET | `/sales/performance/statistics` | 无 | 占位接口 |

### 5.5 工作日志

| 接口 | 方法 | 路径 | 后端已有 | 处理方式 |
|------|------|------|----------|----------|
| 查询工作日志 | GET | `/sales/worklog/list` | 有 | 整合原有接口 |
| 新增工作日志 | POST | `/sales/worklog` | 有 | 整合原有接口 |
| 修改工作日志 | PUT | `/sales/worklog` | 有 | 整合原有接口 |
| 删除工作日志 | DELETE | `/sales/worklog/{logId}` | 有 | 整合原有接口 |

---

## 六、财务管理模块

### 6.1 贷款审核

| 接口 | 方法 | 路径 | 后端已有 | 处理方式 |
|------|------|------|----------|----------|
| 查询审核列表 | GET | `/finance/loan-audit/list` | 有 | 整合原有接口 |
| 查询审核详细 | GET | `/finance/loan-audit/{auditId}` | 有 | 整合原有接口 |
| 新增审核 | POST | `/finance/loan-audit` | 有 | 整合原有接口 |
| 修改审核 | PUT | `/finance/loan-audit` | 有 | 整合原有接口 |
| 提交审核 | PUT | `/finance/loan-audit/{auditId}/submit` | 有 | 整合原有接口 |
| 通过审核 | PUT | `/finance/loan-audit/{auditId}/approve` | 有 | 整合原有接口 |
| 拒绝审核 | PUT | `/finance/loan-audit/{auditId}/reject` | 有 | 整合原有接口 |

### 6.2 银行管理

| 接口 | 方法 | 路径 | 后端已有 | 处理方式 |
|------|------|------|----------|----------|
| 查询银行列表 | GET | `/finance/bank/list` | 有 | 整合原有接口 |
| 查询银行详细 | GET | `/finance/bank/{bankId}` | 有 | 整合原有接口 |
| 新增银行 | POST | `/finance/bank` | 有 | 整合原有接口 |
| 修改银行 | PUT | `/finance/bank` | 有 | 整合原有接口 |
| 删除银行 | DELETE | `/finance/bank/{bankId}` | 有 | 整合原有接口 |

### 6.3 金融产品

| 接口 | 方法 | 路径 | 后端已有 | 处理方式 |
|------|------|------|----------|----------|
| 查询产品列表 | GET | `/finance/product/list` | 有 | 整合原有接口 |
| 查询产品详细 | GET | `/finance/product/{productId}` | 有 | 整合原有接口 |
| 新增产品 | POST | `/finance/product` | 有 | 整合原有接口 |
| 修改产品 | PUT | `/finance/product` | 有 | 整合原有接口 |
| 删除产品 | DELETE | `/finance/product/{productId}` | 有 | 整合原有接口 |

### 6.4 佣金记录

| 接口 | 方法 | 路径 | 后端已有 | 处理方式 |
|------|------|------|----------|----------|
| 查询佣金列表 | GET | `/finance/commission/list` | 有 | 整合原有接口 |
| 查询佣金详细 | GET | `/finance/commission/{commissionId}` | 有 | 整合原有接口 |
| 新增佣金 | POST | `/finance/commission` | 有 | 整合原有接口 |
| 修改佣金 | PUT | `/finance/commission` | 有 | 整合原有接口 |
| 支付佣金 | PUT | `/finance/commission/{commissionId}/pay` | 有 | 整合原有接口 |

### 6.5 服务费记录

| 接口 | 方法 | 路径 | 后端已有 | 处理方式 |
|------|------|------|----------|----------|
| 查询服务费列表 | GET | `/finance/service-fee/list` | 有 | 整合原有接口 |
| 查询服务费详细 | GET | `/finance/service-fee/{feeId}` | 有 | 整合原有接口 |
| 新增服务费 | POST | `/finance/service-fee` | 有 | 整合原有接口 |
| 修改服务费 | PUT | `/finance/service-fee` | 有 | 整合原有接口 |
| 收取服务费 | PUT | `/finance/service-fee/{feeId}/charge` | 有 | 整合原有接口 |

---

## 七、适配控制器清单

已创建的适配控制器：

| 控制器 | 路径 | 说明 |
|--------|------|------|
| `RuoyiAdapterController` | `/auth/` | 登录认证适配 |
| `RuoyiSystemController` | `/system/` | 系统管理适配 |
| `RuoyiMonitorController` | `/monitor/` | 监控管理适配 |
| `RuoyiSalesController` | `/sales/` | 销售管理适配 |
| `RuoyiFinanceController` | `/finance/` | 财务管理适配 |

---

## 八、后续优化建议

### 8.1 高优先级

1. **完善登录认证**：实现 JWT Token 机制，替换简单的 userId 作为 token
2. **整合用户管理**：将 `/api/sysUser/*` 接口与 `/system/user/*` 接口统一
3. **实现验证码**：接入验证码生成库，启用验证码功能

### 8.2 中优先级

1. **权限菜单动态化**：从数据库读取权限配置生成路由菜单
2. **实现监控功能**：接入 Spring Boot Actuator，实现真实的服务监控
3. **日志功能**：实现操作日志和登录日志的记录

### 8.3 低优先级

1. **定时任务**：接入 Quartz 或 Spring Scheduler
2. **数据字典**：实现参数管理功能
3. **通知公告**：实现系统公告功能
