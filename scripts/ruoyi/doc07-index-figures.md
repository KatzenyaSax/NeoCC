# 首页统计数据接口设计

## 1. 概述

为前端首页 index.vue 的4个统计卡片提供独立的 count 接口，替代当前通过分页接口 `pageSize=1` 变相获取总数的低效方式。

### 1.1 接口列表

| 接口 | 所属模块 | 方法 | URL | 返回值 |
|------|---------|------|-----|--------|
| 客户总数 | sales | GET | `/customer/count` | `Result<Long>` |
| 合同总数 | sales | GET | `/contract/count` | `Result<Long>` |
| 待审合同数（status=4） | sales | GET | `/contract/count-by-status?status=4` | `Result<Long>` |
| 用户总数 | auth | GET | `/sysUser/count` | `Result<Long>` |

---

## 2. 后端实现

### 2.1 Sales 模块

#### 2.1.1 CustomerService 接口

**文件**: `sales/src/main/java/com/dafuweng/sales/service/CustomerService.java`

新增方法：
```java
/**
 * 获取客户总数
 */
Long count();
```

#### 2.1.2 CustomerServiceImpl 实现

**文件**: `sales/src/main/java/com/dafuweng/sales/service/impl/CustomerServiceImpl.java`

新增实现：
```java
@Override
public Long count() {
    return customerDao.selectCount(null);
}
```

#### 2.1.3 CustomerDao

**文件**: `sales/src/main/java/com/dafuweng/sales/dao/CustomerDao.java`

确保继承 `BaseMapper<CustomerEntity>`，无需额外修改。

#### 2.1.4 CustomerController

**文件**: `sales/src/main/java/com/dafuweng/sales/controller/CustomerController.java`

新增端点：
```java
/**
 * GET /api/customer/count
 * 获取客户总数
 */
@GetMapping("/count")
public Result<Long> count() {
    return Result.success(customerService.count());
}
```

---

#### 2.1.5 ContractService 接口

**文件**: `sales/src/main/java/com/dafuweng/sales/service/ContractService.java`

新增方法：
```java
/**
 * 获取合同总数
 */
Long count();

/**
 * 按状态获取合同数量
 */
Long countByStatus(Short status);
```

#### 2.1.6 ContractServiceImpl 实现

**文件**: `sales/src/main/java/com/dafuweng/sales/service/impl/ContractServiceImpl.java`

新增实现：
```java
@Override
public Long count() {
    return contractDao.selectCount(null);
}

@Override
public Long countByStatus(Short status) {
    LambdaQueryWrapper<ContractEntity> wrapper = new LambdaQueryWrapper<>();
    if (status != null) {
        wrapper.eq(ContractEntity::getStatus, status);
    }
    return contractDao.selectCount(wrapper);
}
```

#### 2.1.7 ContractController

**文件**: `sales/src/main/java/com/dafuweng/sales/controller/ContractController.java`

新增端点：
```java
/**
 * GET /api/contract/count
 * 获取合同总数
 */
@GetMapping("/count")
public Result<Long> count() {
    return Result.success(contractService.count());
}

/**
 * GET /api/contract/count-by-status?status=4
 * 按状态获取合同数量
 */
@GetMapping("/count-by-status")
public Result<Long> countByStatus(@RequestParam Short status) {
    return Result.success(contractService.countByStatus(status));
}
```

---

### 2.2 Auth 模块

#### 2.2.1 SysUserService 接口

**文件**: `auth/src/main/java/com/dafuweng/auth/service/SysUserService.java`

新增方法：
```java
/**
 * 获取用户总数
 */
Long count();
```

#### 2.2.2 SysUserServiceImpl 实现

**文件**: `auth/src/main/java/com/dafuweng/auth/service/impl/SysUserServiceImpl.java`

新增实现：
```java
@Override
public Long count() {
    return sysUserDao.selectCount(null);
}
```

#### 2.2.3 SysUserDao

**文件**: `auth/src/main/java/com/dafuweng/auth/dao/SysUserDao.java`

确保继承 `BaseMapper<SysUserEntity>`，无需额外修改。

#### 2.2.4 SysUserController

**文件**: `auth/src/main/java/com/dafuweng/auth/controller/SysUserController.java`

新增端点：
```java
/**
 * GET /api/sysUser/count
 * 获取用户总数
 */
@GetMapping("/count")
public Result<Long> count() {
    return Result.success(sysUserService.count());
}
```

---

## 3. 前端实现

### 3.1 新建 API 文件

#### 3.1.1 销售统计 API

**文件**: `ruoyi-ui/src/api/sales/statistics.js`

```javascript
import request from '@/utils/request'

// 获取客户总数
export function countCustomer() {
  return request({
    url: '/customer/count',
    method: 'get'
  })
}

// 获取合同总数
export function countContract() {
  return request({
    url: '/contract/count',
    method: 'get'
  })
}

// 按状态获取合同数量（用于待审贷款等）
export function countContractByStatus(status) {
  return request({
    url: '/contract/count-by-status',
    method: 'get',
    params: { status }
  })
}
```

#### 3.1.2 系统统计 API

**文件**: `ruoyi-ui/src/api/system/statistics.js`

```javascript
import request from '@/utils/request'

// 获取用户总数
export function countUser() {
  return request({
    url: '/sysUser/count',
    method: 'get'
  })
}
```

### 3.2 修改 index.vue

**文件**: `ruoyi-ui/src/views/index.vue`

#### 3.2.1 修改 import

将：
```javascript
import { listCustomer } from '@/api/sales/customer'
import { listContract } from '@/api/sales/contract'
import { listLoanAudit } from '@/api/finance/loanAudit'
import { listRole } from '@/api/system/role'
```

改为：
```javascript
import { countCustomer } from '@/api/sales/statistics'
import { countContract, countContractByStatus } from '@/api/sales/statistics'
import { countUser } from '@/api/system/statistics'
```

#### 3.2.2 修改 loadStats 函数

将：
```javascript
function loadStats() {
  listCustomer({ pageNum: 1, pageSize: 1 }).then(res => {
    const card = statCards.find(c => c.key === 'customer')
    card.value = res.data?.total || 0
    card.loading = false
  }).catch(() => { statCards.find(c => c.key === 'customer').loading = false })

  listContract({ pageNum: 1, pageSize: 1 }).then(res => {
    const card = statCards.find(c => c.key === 'contract')
    card.value = res.data?.total || 0
    card.loading = false
  }).catch(() => { statCards.find(c => c.key === 'contract').loading = false })

  listLoanAudit({ pageNum: 1, pageSize: 1 }).then(res => {
    const card = statCards.find(c => c.key === 'loan')
    card.value = res.data?.total || 0
    card.loading = false
  }).catch(() => { statCards.find(c => c.key === 'loan').loading = false })

  listRole({ pageNum: 1, pageSize: 1 }).then(res => {
    const card = statCards.find(c => c.key === 'role')
    card.value = res.data?.total || 0
    card.loading = false
  }).catch(() => { statCards.find(c => c.key === 'role').loading = false })
}
```

改为：
```javascript
function loadStats() {
  countCustomer().then(res => {
    const card = statCards.find(c => c.key === 'customer')
    card.value = res.data || 0
    card.loading = false
  }).catch(() => { statCards.find(c => c.key === 'customer').loading = false })

  countContract().then(res => {
    const card = statCards.find(c => c.key === 'contract')
    card.value = res.data || 0
    card.loading = false
  }).catch(() => { statCards.find(c => c.key === 'contract').loading = false })

  countContractByStatus(4).then(res => {
    const card = statCards.find(c => c.key === 'loan')
    card.value = res.data || 0
    card.loading = false
  }).catch(() => { statCards.find(c => c.key === 'loan').loading = false })

  countUser().then(res => {
    const card = statCards.find(c => c.key === 'role')
    card.value = res.data || 0
    card.loading = false
  }).catch(() => { statCards.find(c => c.key === 'role').loading = false })
}
```

---

## 4. 数据流

```
前端 index.vue
  ├── countCustomer() → GET /customer/count → Gateway → sales → CustomerController.count()
  ├── countContract() → GET /contract/count → Gateway → sales → ContractController.count()
  ├── countContractByStatus(4) → GET /contract/count-by-status?status=4 → sales → ContractController.countByStatus()
  └── countUser() → GET /sysUser/count → Gateway → auth → SysUserController.count()
```

---

## 5. 网关路由

无需修改网关配置。现有的路由规则已覆盖新增接口：

| 前缀 | 路由到 |
|------|--------|
| `/customer/**` | sales (8083) |
| `/contract/**` | sales (8083) |
| `/sysUser/**` | auth (8085) |

---

## 6. 测试验证

接口完成后，通过浏览器 F12 或 Postman 验证：

| 接口 | 预期返回 |
|------|---------|
| `GET /customer/count` | `{ code: 200, data: <Long> }` |
| `GET /contract/count` | `{ code: 200, data: <Long> }` |
| `GET /contract/count-by-status?status=4` | `{ code: 200, data: <Long> }` |
| `GET /sysUser/count` | `{ code: 200, data: <Long> }` |

---

## 7. 文件清单

| 操作 | 文件路径 |
|------|---------|
| 新增 | `sales/src/main/java/com/dafuweng/sales/service/CustomerService.java` (方法追加) |
| 新增 | `sales/src/main/java/com/dafuweng/sales/service/impl/CustomerServiceImpl.java` (方法追加) |
| 新增 | `sales/src/main/java/com/dafuweng/sales/controller/CustomerController.java` (端点追加) |
| 新增 | `sales/src/main/java/com/dafuweng/sales/service/ContractService.java` (方法追加) |
| 新增 | `sales/src/main/java/com/dafuweng/sales/service/impl/ContractServiceImpl.java` (方法追加) |
| 新增 | `sales/src/main/java/com/dafuweng/sales/controller/ContractController.java` (端点追加) |
| 新增 | `auth/src/main/java/com/dafuweng/auth/service/SysUserService.java` (方法追加) |
| 新增 | `auth/src/main/java/com/dafuweng/auth/service/impl/SysUserServiceImpl.java` (方法追加) |
| 新增 | `auth/src/main/java/com/dafuweng/auth/controller/SysUserController.java` (端点追加) |
| 新增 | `ruoyi-ui/src/api/sales/statistics.js` |
| 新增 | `ruoyi-ui/src/api/system/statistics.js` |
| 修改 | `ruoyi-ui/src/views/index.vue` |
