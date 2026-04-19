# customer-view 客户详情模块开发详细规划

> 本文档是 doc07.md 中 `customer-view`（客户详情）功能的完整开发指南，面向零基础开发者，可直接照此开发。
>
> 生成日期：2026-04-19
> 所属模块：客户管理 /sales/customer
> 前端路由：`/sales/customer/view`
> 后端接口：`GET /api/customer/{id}`
> 状态：待开发

---

## 一、功能说明

客户详情页面用于展示指定客户在数据库中的所有相关信息。

业务场景示例：
- 销售人员查看某个客户的完整信息（基本信息、合同记录、联系记录）
- 管理层点击客户名称后弹出详情侧边/页面，查看该客户的全量数据

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
| `ruoyi-ui/src/views/sales/customer/view.vue` | 客户详情页（新建） |
| `ruoyi-ui/src/api/customer.js` | API 文件已存在，无需修改 |

### 2.3 后端文件

无需新建文件，直接复用已有的 `CustomerController.getCustomer(id)` 接口。

---

## 三、页面设计

### 3.1 搜索区域

- **搜索框**：`el-input` 支持输入客户姓名（name）或客户ID（id）
- 搜索方式：按 ID 精确查找，或按 name 模糊查找
- 回车或点击搜索按钮触发查询
- 搜索结果在下方的表格区域展示

### 3.2 表格区域

点击搜索后，下方 `el-table` 展示该客户的所有相关数据，分成以下类别：

| 分类 | 数据来源表 | 说明 |
|------|-----------|------|
| 基本信息 | `customer` | 客户姓名、电话、公司名、意向等级、客户类型等 |
| 联系记录 | `contact_record` | 该客户所有跟进记录（联系类型、联系时间、内容、意向变化等） |
| 合同记录 | `contract` | 该客户所有合同（合同编号、金额、状态、签订日期等） |
| 业绩记录 | `performance_record` | 该客户关联合同的业绩（合同金额、提成金额、状态等） |
| 转移记录 | `customer_transfer_log` | 该客户的公海认领/转移记录（操作人、时间、原因等） |

表格使用 `el-table` 的 `data` 属性绑定，列使用 `prop` 直接映射字段。

### 3.3 Tab 分组展示

由于数据来源多个表，使用 `el-tabs` 按类别分组，每个 Tab 内放一个 `el-table`：

```
[基本信息] [联系记录] [合同记录] [业绩记录] [转移记录]
```

每个 Tab 对应一个 `el-table`，数据分别绑定对应类型的数据数组。

---

## 四、后端接口（已存在）

`GET /api/customer/{id}`

- 路径参数：`id` — 客户ID
- 返回：`CustomerEntity` 客户主表信息

其他数据（联系记录、合同、业绩、转移记录）通过各自已有的 Service/DAO 查询。

建议在后端新建 `CustomerViewService` 和 `CustomerViewController`，新增一个聚合接口：

`GET /api/customer/view/{id}`

一次性返回该客户的所有相关数据（基本信息 + 5类子表数据）。

---

## 五、数据模型

### 5.1 响应结构

```json
{
  "code": 200,
  "data": {
    "customer": { ... },
    "contactRecords": [ ... ],
    "contracts": [ ... ],
    "performanceRecords": [ ... ],
    "transferLogs": [ ... ]
  }
}
```

### 5.2 各表主键

| 表名 | 主键 |
|------|------|
| `customer` | id |
| `contact_record` | id |
| `contract` | id |
| `performance_record` | id |
| `customer_transfer_log` | id |

---

## 六、开发步骤

### Step 1: 后端 — 新建 CustomerViewService

`sales/src/main/java/com/dafuweng/sales/service/CustomerViewService.java`

```java
public interface CustomerViewService {
    Map<String, Object> getCustomerView(Long id);
}
```

### Step 2: 后端 — 实现 CustomerViewServiceImpl

`sales/src/main/java/com/dafuweng/sales/service/impl/CustomerViewServiceImpl.java`

注入以下 DAO：
- `CustomerDao` — 查询 customer 主表
- `ContactRecordDao` — 按 customerId 查询联系记录
- `ContractDao` — 按 customerId 查询合同
- `PerformanceRecordDao` — 按 contractId 或 customerId 查业绩
- `CustomerTransferLogDao` — 按 customerId 查转移记录

### Step 3: 后端 — 新建 CustomerViewController

`sales/src/main/java/com/dafuweng/sales/controller/CustomerViewController.java`

```java
@GetMapping("/view/{id}")
public Result<?> view(@PathVariable Long id)
```

### Step 4: 前端 — 新建 view.vue

`ruoyi-ui/src/views/sales/customer/view.vue`

- 搜索框（客户名称或ID）
- el-tabs：基本信息 / 联系记录 / 合同记录 / 业绩记录 / 转移记录
- 每个 Tab 内一个 el-table

### Step 5: 前端 — 路由注册

`ruoyi-ui/src/router/index.js` 或 `ruoyi-ui/src/router/sales.js`

添加路由：

```js
{
  path: '/sales/customer',
  component: Layout,
  children: [
    { path: 'view', component: () => import('@/views/sales/customer/view.vue') }
  ]
}
```

---

## 七、关键文件清单

| 文件路径 | 操作 |
|---------|------|
| `sales/src/main/java/com/dafuweng/sales/service/CustomerViewService.java` | 新建 |
| `sales/src/main/java/com/dafuweng/sales/service/impl/CustomerViewServiceImpl.java` | 新建 |
| `sales/src/main/java/com/dafuweng/sales/controller/CustomerViewController.java` | 新建 |
| `ruoyi-ui/src/views/sales/customer/view.vue` | 新建 |
| `ruoyi-ui/src/router/index.js` 或 sales.js | 修改（添加路由） |
| `ruoyi-ui/src/utils/request.js` | 可能需要（检查已有 customer 路径是否已配置） |

---

## 八、注意事项

1. `CustomerController` 已有一个 `getCustomer(id)` 接口，返回 customer 主表数据，可直接复用其 DAO 查询。
2. 业绩记录 `performance_record` 通过 `contract_id` 关联 `contract`，再通过 `contract.customer_id` 筛选。
3. 数据库中各表所在库：
   - `customer`、`contact_record`、`contract`、`performance_record`、`customer_transfer_log` → `dafuweng_sales`
   - `sys_user`（用户表） → `dafuweng_auth`
4. 前端 request.js 中 customer 路径已配置在 sales 模块下，无需修改。
5. 路由路径 `/sales/customer/view` 是二级路径，sidebar 需要添加对应菜单。
