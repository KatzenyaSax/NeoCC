# 客户转移记录及销售代表调整功能设计

## 1. 需求概述

对客户转移记录页面进行优化显示，并在客户管理和公海客户模块中根据登录人角色自动设置转移类型和原因。

## 2. 后端接口新增

### 批量查询客户名称
- **接口**: `GET /api/customer/names/by-ids`
- **请求体**: `List<Long> ids`
- **返回**: `Result<Map<Long, String>>` (id → 客户名称)

## 3. 前端页面修改

### 3.1 客户转移记录页面 (transfer/index.vue)

| 原列名 | 新列名 | 显示内容 |
|--------|--------|----------|
| 客户ID | 客户 | 客户姓名 |
| 转出销售ID | 转出销售 | 销售姓名 |
| 转入销售ID | 转入销售 | 销售姓名 |
| 操作人ID | 操作人 | 姓名 |

**实现方式**:
- 加载列表后，批量调用 `getUserNamesByIds` 查销售姓名和操作人姓名
- 批量调用 `getCustomerNamesByIds` 查客户姓名

### 3.2 客户管理页面 (customer/index.vue)

**修改客户时**，当销售代表发生变动：
- 根据 `userStore.roles` 判断登录人角色
- 设置 `operateType` 和 `reason`:
  - 战区总监 (ROLE_ZONE_DIRECTOR) → `总监调整`
  - 部门经理 (ROLE_DEPT_MANAGER) → `部门经理调整`
  - 总经理 (ROLE_GENERAL_MANAGER) → `总经理调整`
- 同时更新客户的 `deptId` 和 `zoneId` 为新销售代表的部门/战区

### 3.3 公海客户转移 (public-sea/index.vue)

**转移客户时**：
- 根据 `userStore.roles` 判断登录人角色
- 设置 `operateType` 和 `reason`:
  - 战区总监 → `总监调整`
  - 部门经理 → `部门经理调整`
  - 总经理 → `总经理调整`
  - 销售代表 → `销售代表领取`
- 同时更新客户的 `deptId` 和 `zoneId`

## 4. 文件清单

### 后端
- `CustomerController.java` - 新增 `getCustomerNamesByIds` 接口

### 前端
- `ruoyi-ui/src/api/sales/customer.js` - 新增 `getCustomerNamesByIds` API
- `ruoyi-ui/src/views/sales/transfer/index.vue` - 修改列显示
- `ruoyi-ui/src/views/sales/customer/index.vue` - 修改客户时根据角色设置转移类型
- `ruoyi-ui/src/views/sales/public-sea/index.vue` - 转移客户时根据角色设置转移类型
