# public-sea 公海客户模块开发详细规划

> 本文档是 doc07.md 中 `public-sea`（公海客户）功能的完整开发指南，面向零基础开发者，可直接照此开发。
>
> 生成日期：2026-04-19
> 所属模块：客户管理 /sales/public-sea
> 前端路由：`/sales/public-sea`
> 后端接口：`GET /api/customer/public-sea/page`、`PUT /api/customer/{id}/transfer`、`DELETE /api/customer/{id}`
> 状态：待开发

---

## 一、功能说明

公海客户页面用于展示所有公海客户（`status = 5`），支持销售经理/总经理将公海客户分配给指定销售代表，支持销售代表认领公海客户到自己名下，支持删除和修改公海客户。

业务场景示例：
- 销售经理点击公海客户列表，将某个客户分配给指定销售代表
- 销售代表进入公海，认领一个客户到自己名下
- 销售经理删除长期无价值公海客户
- 销售经理修改公海客户的基本信息

权限说明：
- **销售代表**：只能将公海客户转移到自己名下（修改 `salesRepId` 为当前用户ID）
- **销售经理/总经理**：可以将任意公海客户转移到任意销售代表名下

---

## 二、技术架构

### 2.1 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + Element Plus + Pinia + axios |
| 后端 | Spring Boot + MyBatis-Plus + Feign |
| 数据库 | MySQL |

### 2.2 前端文件

| 文件 | 说明 |
|------|------|
| `ruoyi-ui/src/views/sales/public-sea/index.vue` | 公海客户列表页（新建） |
| `ruoyi-ui/src/api/sales/publicSea.js` | 公海客户 API（新建） |

### 2.3 后端文件

| 文件 | 说明 |
|------|------|
| `sales/src/main/java/com/dafuweng/sales/service/PublicSeaService.java` | 公海服务接口（新建） |
| `sales/src/main/java/com/dafuweng/sales/service/impl/PublicSeaServiceImpl.java` | 公海服务实现（新建） |
| `sales/src/main/java/com/dafuweng/sales/controller/PublicSeaController.java` | 公海控制器（新建） |
| `sales/src/main/java/com/dafuweng/sales/entity/CustomerTransferLogEntity.java` | 转移日志（已存在，无需修改） |
| `sales/src/main/java/com/dafuweng/sales/entity/CustomerEntity.java` | 客户实体（已存在，无需修改） |

---

## 三、数据模型

### 3.1 客户状态枚举

| status 值 | 含义 |
|-----------|------|
| 0 | 无效 |
| 1 | 有效 |
| 5 | 公海客户 |

公海客户的判断标准：`status = 5`

### 3.2 客户主表（customer）关键字段

| 字段 | 说明 |
|------|------|
| id | 主键 |
| name | 客户名称 |
| phone | 联系电话 |
| idCard | 身份证号 |
| companyName | 公司名称 |
| customerType | 客户类型 |
| salesRepId | 当前销售代表ID（公海客户此字段为 null 或 0） |
| deptId | 部门ID |
| zoneId | 战区ID |
| intentionLevel | 意向等级 |
| status | 客户状态（5 = 公海） |
| publicSeaTime | 进入公海时间 |
| publicSeaReason | 进入公海原因 |
| source | 来源 |
| loanIntentionAmount | 贷款意向金额 |
| loanIntentionProduct | 贷款意向产品 |

### 3.3 客户转移日志表（customer_transfer_log）字段

| 字段 | 说明 |
|------|------|
| id | 主键 |
| customerId | 客户ID |
| fromRepId | 转出销售代表ID（公海转入时为 null） |
| toRepId | 转入销售代表ID |
| operateType | 操作类型：`claim`（认领）、`assign`（分配） |
| reason | 转移原因 |
| operatedBy | 操作人ID |
| operatedAt | 操作时间 |

---

## 四、后端接口设计

### 4.1 公海客户分页列表

`GET /api/customer/public-sea/page`

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认 1 |
| size | int | 否 | 每页条数，默认 20 |
| name | string | 否 | 客户名称（模糊搜索） |
| sortField | string | 否 | 排序字段 |
| sortOrder | string | 否 | 排序方向：asc / desc |

**响应结构：**

```json
{
  "code": 200,
  "data": {
    "total": 100,
    "records": [
      {
        "id": 1,
        "name": "张三",
        "phone": "13800138000",
        "companyName": "XX公司",
        "customerType": 1,
        "intentionLevel": 3,
        "status": 5,
        "publicSeaTime": "2026-04-01",
        "publicSeaReason": "长期未跟进",
        "source": "渠道A",
        "loanIntentionAmount": 500000,
        "loanIntentionProduct": "抵押贷",
        "createdAt": "2026-01-01"
      }
    ]
  }
}
```

**实现说明：**
- `LambdaQueryWrapper` 添加 `eq(CustomerEntity::getStatus, 5)` 条件筛选公海客户
- 按 `createdAt` 倒序排列

### 4.2 转移公海客户

`PUT /api/customer/public-sea/transfer`

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| customerId | Long | 是 | 客户ID |
| toRepId | Long | 是 | 转入销售代表ID |
| reason | string | 否 | 转移原因 |

**请求体示例：**

```json
{
  "customerId": 1,
  "toRepId": 5,
  "reason": "分配给销售代表张三"
}
```

**响应结构：**

```json
{
  "code": 200,
  "message": "转移成功"
}
```

**实现步骤：**
1. 根据 `customerId` 查询客户，确认 `status = 5`
2. 获取当前登录用户信息（通过 SecurityContext 或 Token）
3. 判断权限：
   - 若当前用户角色为 `sales_rep`，则只能转移到自己名下（`toRepId` 必须等于当前用户ID）
   - 若当前用户角色为 `sales_manager` 或 `admin`，可转移给任意销售代表
4. 更新客户 `salesRepId = toRepId`，`status = 1`（从公海转出）
5. 写入 `customer_transfer_log` 日志记录

### 4.3 删除公海客户

`DELETE /api/customer/{id}`

物理删除客户记录（与现有 delete 接口共用）

### 4.4 修改公海客户

`PUT /api/customer`

更新客户基本信息（与现有 update 接口共用）

### 4.5 获取销售代表列表（下拉选择）

`GET /api/sysUser/sales-reps`

**响应结构：**

```json
{
  "code": 200,
  "data": [
    { "id": 1, "realName": "张三" },
    { "id": 2, "realName": "李四" }
  ]
}
```

用于转移弹窗中选择目标销售代表。

---

## 五、前端页面设计

### 5.1 页面布局

```
+--------------------------------------------------------------+
|  搜索区域（客户名称搜索）+ 操作按钮（新增暂不需要）                |
+--------------------------------------------------------------+
|  公海客户列表（el-table）                                       |
|  - ID | 客户名称 | 电话 | 公司 | 意向等级 | 公海时间 | 操作     |
|  - 操作列：[转移] [修改] [删除]                                  |
+--------------------------------------------------------------+
|  分页组件                                                     |
+--------------------------------------------------------------+
```

### 5.2 搜索区域

- `el-input` 输入客户名称，支持模糊搜索
- 点击搜索按钮触发 `handleQuery`

### 5.3 表格列

| 列名 | 字段 | 说明 |
|------|------|------|
| ID | id | 客户ID |
| 客户名称 | name | |
| 联系电话 | phone | |
| 公司名称 | companyName | |
| 意向等级 | intentionLevel | 1-5 显示对应文字 |
| 公海时间 | publicSeaTime | 格式：YYYY-MM-DD |
| 公海原因 | publicSeaReason | |
| 操作 | — | 转移/修改/删除 按钮 |

### 5.4 转移弹窗（el-dialog）

点击「转移」按钮弹出：

```
+------------------------------------------+
|  转移客户给销售代表                        |
+------------------------------------------+
|  目标销售代表：                            |
|  [el-select 下拉销售代表列表]              |
|                                          |
|  转移原因：                               |
|  [el-input 填写原因]                      |
+------------------------------------------+
|  [取消]                    [确认转移]      |
+------------------------------------------+
```

- 销售代表角色看到：只能选择自己（下拉只能选当前用户）
- 销售经理/总经理看到：可以选择任意销售代表

### 5.5 修改弹窗

点击「修改」按钮，弹出 `el-dialog` 编辑客户基本信息字段（与现有客户管理共用表单）

---

## 六、权限控制

### 6.1 前端权限控制

| 角色 | 转移按钮 | 修改按钮 | 删除按钮 |
|------|---------|---------|---------|
| sales_rep | 显示（只能转给自己） | 显示 | 不显示 |
| sales_manager | 显示 | 显示 | 显示 |
| admin | 显示 | 显示 | 显示 |

前端通过 `getters` 或 `auth` 插件判断角色：`hasPermiOr(['sales_manager', 'admin'])` 或 `hasRole('sales_rep')`

### 6.2 后端权限控制

| 接口 | 权限 |
|------|------|
| GET /api/customer/public-sea/page | 任意登录用户 |
| PUT /api/customer/public-sea/transfer | sales_rep（仅转自己）/ sales_manager/admin（任意转） |
| PUT /api/customer | sales_manager/admin |
| DELETE /api/customer/{id} | sales_manager/admin |

---

## 七、开发步骤

### Step 1: 后端 — 新建 PublicSeaService

`sales/src/main/java/com/dafuweng/sales/service/PublicSeaService.java`

```java
public interface PublicSeaService {
    // 公海客户分页列表
    PageResponse<CustomerEntity> pageList(PageRequest request);

    // 转移公海客户
    void transfer(Long customerId, Long toRepId, String reason, Long operatorId);

    // 获取销售代表列表（下拉用）
    List<Map<String, Object>> listSalesReps();
}
```

### Step 2: 后端 — 实现 PublicSeaServiceImpl

`sales/src/main/java/com/dafuweng/sales/service/impl/PublicSeaServiceImpl.java`

注入：
- `CustomerDao`
- `CustomerTransferLogDao`
- `AuthFeignClient`（调用 auth 服务查询用户姓名）

关键逻辑：
- `pageList`：添加 `status = 5` 条件
- `transfer`：
  - 权限校验（销售代表只能 toRepId = 自己）
  - 更新 customer 的 `salesRepId`、`status = 1`
  - 写入 `customer_transfer_log`

### Step 3: 后端 — 新建 PublicSeaController

`sales/src/main/java/com/dafuweng/sales/controller/PublicSeaController.java`

```java
@GetMapping("/public-sea/page")
public Result<?> publicSeaPage(PageRequest request)

@PutMapping("/public-sea/transfer")
public Result<?> transfer(@RequestBody Map<String, Object> req)

@GetMapping("/sales-reps")
public Result<?> listSalesReps()
```

### Step 4: 前端 — 新建 api/sales/publicSea.js

```javascript
import request from '@/utils/request'

export function getPublicSeaPage(query) {
  return request({
    url: '/customer/public-sea/page',
    method: 'get',
    params: query
  })
}

export function transferCustomer(data) {
  return request({
    url: '/customer/public-sea/transfer',
    method: 'put',
    data
  })
}

export function listSalesReps() {
  return request({
    url: '/customer/sales-reps',
    method: 'get'
  })
}
```

### Step 5: 前端 — 新建 views/sales/public-sea/index.vue

页面结构：
- 搜索框（客户名称）
- `el-table` 显示公海客户列表
- 操作列：转移 / 修改 / 删除按钮
- 转移弹窗：`el-select` 销售代表下拉 + 原因输入框
- 修改弹窗：复用客户表单

### Step 6: 前端 — 路由注册

`ruoyi-ui/src/router/index.js`

```js
{
  path: '/sales/public-sea',
  component: Layout,
  children: [
    {
      path: '',
      component: () => import('@/views/sales/public-sea/index.vue'),
      name: 'PublicSea',
      meta: { title: '公海客户', icon: 'user' }
    }
  ]
}
```

### Step 7: Sidebar 菜单配置

在 `ruoyi-ui/src/layout/components/Sidebar/sidebar.vue` 或后端菜单表中添加公海客户菜单入口（路由：`/sales/public-sea`）

---

## 八、关键文件清单

| 文件路径 | 操作 |
|---------|------|
| `sales/src/main/java/com/dafuweng/sales/service/PublicSeaService.java` | 新建 |
| `sales/src/main/java/com/dafuweng/sales/service/impl/PublicSeaServiceImpl.java` | 新建 |
| `sales/src/main/java/com/dafuweng/sales/controller/PublicSeaController.java` | 新建 |
| `ruoyi-ui/src/api/sales/publicSea.js` | 新建 |
| `ruoyi-ui/src/views/sales/public-sea/index.vue` | 新建 |
| `ruoyi-ui/src/router/index.js` | 修改（添加路由） |

---

## 九、注意事项

1. `status = 5` 是公海客户唯一判断标准，与 `salesRepId` 是否为空无关。
2. 客户从公海转出后，`status` 应改为 `1`（有效），同时设置 `salesRepId`。
3. 转移日志 `customer_transfer_log` 的 `fromRepId`：从公海转出时为 `null`，操作类型为 `claim`（认领）或 `assign`（分配）。
4. 销售代表列表通过 `AuthFeignClient` 调用 auth 服务的用户接口查询（过滤 role = sales_rep 的用户）。
5. 前端 `request.js` 中 `/customer/*` 已配置走 `/sales/api`，无需修改。
6. 公海客户列表需要分页，不能一次性加载所有公海数据。
7. 意向等级 `intentionLevel` 前端显示时转换为文字：1=低，2=中，3=高，4=很有意向，5=已签约。
