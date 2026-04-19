# NeoCC 前端缺失页面开发前期调用报告

> 基于 getRouters.md 路由清单，逐一对照前后端现状，输出完整开发规划。
>
> 生成日期：2026-04-19
> 负责人：待分配
> 状态：待开发

---

## 一、项目技术架构总览

### 1.1 技术栈

| 层级 | 技术 |
|------|------|
| 前端框架 | Vue 3 + Vite + Element Plus + Pinia |
| 路由 | Vue Router 4（动态路由，后端驱动） |
| UI 组件库 | Element Plus |
| HTTP 客户端 | axios（`@/utils/request.js`） |
| 后端 | Spring Cloud 微服务（auth / system / sales / finance） |
| 网关 | Spring Cloud Gateway（8086） |

### 1.2 请求路由链路（前端→后端）

```
前端 API URL
  → Vite proxy（vite.config.js）
    → Gateway（application.yml, port 8086）
      → 微服务 Controller
```

`request.js` 中的 `getBaseURL()` 根据 URL 前缀决定走哪个 proxy：

| URL 前缀 | Vite proxy 目标 | Gateway 路由 | 到达服务 |
|---------|-----------------|-------------|---------|
| `/auth/*` | `/auth` → Gateway | `Path=/auth/**` | auth:8085 |
| `/sysUser/*`, `/sysRole/*`, `/sysPermission/*` | `/auth/api` | `Path=/api/sysUser/**` 等 | auth:8085 |
| `/sysDict/*`, `/sysParam/*` | `/system/api` | `Path=/system/**` | system:8082 |
| `/sysZone/*`, `/sysDepartment/*` | `/system/api` | `Path=/sysZone/**` 等 | system:8082 |
| `/sales/api/*` | `/sales/api` | `Path=/sales/api/**` | sales:8083 |
| `/finance/api/*` | `/finance/api` | `Path=/finance/api/**` | finance:8084 |
| 其他 | `/dev-api` | — | 直接请求 |

### 1.3 路由注册机制

- `AuthController.getRouters()` 返回 Vue Router JSON 树
- `permission.js` 的 `loadView()` 通过 `import.meta.glob` 动态匹配 `views/` 下的 `.vue` 文件
- 后端返回 `component: "sales/customer/index"` → 前端加载 `views/sales/customer/index.vue`
- 所有路由均以**扁平顶级方式**注册到 Vue Router（父路由 path 不作为子路由前缀）

---

## 二、路由清单与页面对照表

### 2.1 系统常量路由（已完成）

| 路由路径 | 前端文件 | 状态 |
|---------|---------|------|
| `/login` | `views/login.vue` | ✅ 已完成 |
| `/404` | `views/error/404.vue` | ✅ 已完成 |
| `/401` | `views/error/401.vue` | ✅ 已完成 |
| `/index` | `views/index.vue` | ✅ 已完成 |
| `/redirect` | `views/redirect/index.vue` | ✅ 已完成 |

---

### 2.2 系统管理模块 `/system`（Layout 分组容器）

| 路由路径 | 后端组件路径 | 前端文件 | 状态 |
|---------|------------|---------|------|
| `/system` | Layout | — | ✅ 菜单容器 |
| `/user` | `system/user/index` | `views/system/user/index.vue` | ✅ 已完成 |
| `/role` | `system/role/index` | `views/system/role/index.vue` | ✅ 已完成 |
| `/permission` | `system/permission/index` | `views/system/permission/index.vue` | ✅ 已完成 |
| `/dept` | `system/dept/index` | `views/system/dept/index.vue` | ✅ 已完成 |
| `/zone` | `system/zone/index` | `views/system/zone/index.vue` | ✅ 已完成 |
| `/dict` | `system/dict/index` | `views/system/dict/index.vue` | ✅ 已完成 |
| `/param` | `system/param/index` | `views/system/param/index.vue` | ✅ 已完成 |
| `/log` | `system/log/index` | — | ❌ **缺失视图** |

#### `/system/log` — 操作日志

- **前端**：需新建 `views/system/log/index.vue`
- **后端**：Controller、Service、Dao 已完整实现
  - Controller: `SysOperationLogController` (`/api/sysOperationLog`)
  - Entity: `SysOperationLogEntity`
  - 主要接口: `GET /api/sysOperationLog/page`
- **前端 API**：需新建 `api/system/operationLog.js`

---

### 2.3 销售管理模块 `/sales`（Layout 分组容器）

| 路由路径 | 后端组件路径 | 前端文件 | 状态 |
|---------|------------|---------|------|
| `/customer-list` | `sales/customer/index` | `views/sales/customer/index.vue` | ✅ 已完成 |
| `/customer-add` | `sales/customer/index` | `views/sales/customer/index.vue` | ✅ 复用 customer |
| `/customer-edit` | `sales/customer/index` | `views/sales/customer/index.vue` | ✅ 复用 customer |
| `/customer-view` | `sales/customer-view/index` | — | ❌ **缺失视图** |
| `/public-sea` | `sales/public-sea/index` | — | ❌ **缺失视图** |
| `/contact` | `sales/contact/index` | `views/sales/contact/index.vue` | ✅ 已完成 |
| `/contract-list` | `sales/contract/index` | `views/sales/contract/index.vue` | ✅ 已完成 |
| `/contract-add` | `sales/contract/index` | `views/sales/contract/index.vue` | ✅ 复用 contract |
| `/contract-edit` | `sales/contract/index` | `views/sales/contract/index.vue` | ✅ 复用 contract |
| `/contract-view` | `sales/contract-view/index` | — | ❌ **缺失视图** |
| `/contract-sign` | `sales/contract-sign/index` | — | ❌ **缺失视图** |
| `/worklog` | `sales/worklog/index` | `views/sales/worklog/index.vue` | ✅ 已完成 |
| `/transfer` | `sales/transfer/index` | `views/sales/customer-transfer/index.vue` | ⚠️ 路径不匹配 |

#### `/sales/customer-view` — 客户详情页

- **前端**：需新建 `views/sales/customer-view/index.vue`
- **需对接后端接口**：

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/customer/{id}` | GET | 获取客户基本信息 |
| `/api/contactRecord/listByCustomerId/{customerId}` | GET | 获取跟进记录列表 |
| `/api/contract/listByCustomerId/{customerId}` | GET | 获取关联合同列表（需确认此接口是否存在） |
| `/api/customerTransferLog/listByCustomerId/{customerId}` | GET | 获取转移记录 |

- **前端 API**：需新建 `api/sales/customerView.js`（或复用 `customer.js`）
- **后端**：无需新建 Controller，直接复用现有接口即可

#### `/sales/public-sea` — 公海客户

- **前端**：需新建 `views/sales/public-sea/index.vue`
- **需对接后端接口**：

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/customer/listByStatus` | GET | 按状态获取客户列表（需确认公海状态值） |

- **前置确认**：需与后端确认公海客户的 `status` 字段值（如 `status=3` 表示公海），或后端是否提供独立接口 `GET /api/customer/publicSeaPage`
- **前端 API**：可在 `customer.js` 中补充 `listByStatus()` 方法

#### `/sales/contract-view` — 合同详情页

- **前端**：需新建 `views/sales/contract-view/index.vue`
- **需对接后端接口**：

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/contract/{id}` | GET | 获取合同基本信息 |
| `/api/contractAttachment/listByContractId/{contractId}` | GET | 获取合同附件列表 |
| `/api/contract/getByContractNo/{contractNo}` | GET | 按合同编号获取（备选） |

- **前端 API**：可在 `contract.js` 中补充 `getContract(id)` 方法

#### `/sales/contract-sign` — 合同签署页

- **前端**：需新建 `views/sales/contract-sign/index.vue`
- **需对接后端接口**：

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/contract/{id}/sign` | POST | 触发合同签署 |

- **注意**：此接口为写操作（触发签署流程），需处理成功/失败两种返回
- **前置确认**：签署成功后前端应跳转到哪个页面？合同详情页还是合同列表页？

#### `/sales/transfer` — 客户转移记录（路径不匹配）

- **现有视图**：`views/sales/customer-transfer/index.vue`（已存在）
- **getRouters 组件路径**：`sales/transfer/index`
- **问题**：`loadView("sales/transfer/index")` 查找 `views/sales/transfer/index.vue`，找不到（实际文件在 `customer-transfer/` 目录）
- **解决方案（二选一）**：
  - **方案 A**：将 `views/sales/customer-transfer/` 目录改名为 `views/sales/transfer/`
  - **方案 B**：后端 AuthController 的 `permCodeToComponent()` 返回值保持 `sales/customer-transfer/index`（建议采用）

---

### 2.4 金融审核模块 `/finance`（Layout 分组容器）

| 路由路径 | 后端组件路径 | 前端文件 | 状态 |
|---------|------------|---------|------|
| `/bank` | `finance/bank/index` | `views/finance/bank/index.vue` | ✅ 已完成 |
| `/product` | `finance/product/index` | `views/finance/product/index.vue` | ✅ 已完成 |
| `/loan-audit` | `finance/loan-audit/index` | `views/finance/loan-audit/index.vue` | ✅ 已完成 |
| `/commission` | `finance/commission/index` | `views/finance/commission/index.vue` | ✅ 已完成 |
| `/service-fee` | `finance/service-fee/index` | `views/finance/service-fee/index.vue` | ✅ 已完成 |

金融审核模块所有页面均已完成，无需新建。

---

### 2.5 业绩统计模块 `/performance`（Layout 分组容器）

| 路由路径 | 后端组件路径 | 前端文件 | 状态 |
|---------|------------|---------|------|
| `/perf-summary` | `perf-summary/index` | — | ❌ **缺失视图+后端** |
| `/perf-ranking` | `perf-ranking/index` | — | ❌ **缺失视图+后端** |

#### `/performance/perf-summary` — 业绩汇总页

**前后端均缺失，需完整新建。**

#### `/performance/perf-ranking` — 业绩排名页

**前后端均缺失，需完整新建。**

---

## 三、缺失清单汇总

### 3.1 必须新建的前端视图

| 优先级 | 路由路径 | 目标文件 | 需配套后端工作 |
|--------|---------|---------|--------------|
| P1 | `/system/log` | `views/system/log/index.vue` | 后端已完整，仅需新建视图 |
| P1 | `/sales/customer-view` | `views/sales/customer-view/index.vue` | 复用现有接口 |
| P1 | `/sales/public-sea` | `views/sales/public-sea/index.vue` | 复用现有接口（需确认状态值） |
| P1 | `/sales/contract-view` | `views/sales/contract-view/index.vue` | 复用现有接口 |
| P2 | `/sales/contract-sign` | `views/sales/contract-sign/index.vue` | 复用现有接口 |
| P2 | `/transfer` 路径修复 | `views/sales/customer-transfer/` 改名 | 或后端组件路径对齐 |

### 3.2 前后端均缺失（需完整新建）

| 优先级 | 路由路径 | 前端文件 | 后端新建内容 |
|--------|---------|---------|------------|
| P1 | `/perf-summary` | `views/perf-summary/index.vue` + `api/perf-summary.js` | 新 Controller + Service + DAO |
| P1 | `/perf-ranking` | `views/perf-ranking/index.vue` + `api/perf-ranking.js` | 新 Controller + Service + DAO |

---

## 四、业绩统计模块详细规划（perf-summary / perf-ranking）

> 这是最复杂的缺失模块，前后端均无对应实现。以下给出完整的开发规格。

### 4.1 数据库表（已存在）

**performance_record**（`dafuweng_sales`）— 业绩记录表：

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT | 主键 |
| `contract_id` | BIGINT | 合同 ID |
| `sales_rep_id` | BIGINT | 销售人员 ID |
| `dept_id` | BIGINT | 部门 ID |
| `zone_id` | BIGINT | 战区 ID |
| `contract_amount` | DECIMAL(15,2) | 合同金额 |
| `commission_rate` | DECIMAL(6,4) | 提成比例 |
| `commission_amount` | DECIMAL(15,2) | 提成金额 |
| `status` | TINYINT | 1-计算中 2-已确认 3-已发放 4-已取消 |
| `calculate_time` | DATETIME | 计算时间 |
| `confirm_time` | DATETIME | 确认时间 |
| `grant_time` | DATETIME | 发放时间 |

**commission_record**（`dafuweng_finance`）— 提成记录表：

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT | 主键 |
| `performance_id` | BIGINT | 业绩记录 ID |
| `sales_rep_id` | BIGINT | 销售人员 ID |
| `contract_id` | BIGINT | 合同 ID |
| `commission_amount` | DECIMAL(15,2) | 提成金额 |
| `commission_rate` | DECIMAL(6,4) | 提成比例 |
| `status` | TINYINT | 1-待确认 2-已确认 3-已发放 |
| `confirm_time` | DATETIME | 确认时间 |
| `grant_time` | DATETIME | 发放时间 |
| `grant_account` | VARCHAR(100) | 发放账户 |
| `remark` | VARCHAR(500) | 备注 |

### 4.2 perf-summary — 业绩汇总

#### 后端新建（system 或 sales 模块新建 Controller）

建议在 `system` 模块新建 `PerfSummaryController`：

**推荐方案：独立 Controller（推荐）**

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/perfSummary/summary` | GET | 按维度聚合业绩数据 |
| `/api/perfSummary/trend` | GET | 业绩趋势（按月/季度） |

**查询参数**（`/api/perfSummary/summary`）：

| 参数 | 类型 | 说明 |
|------|------|------|
| `beginTime` | String | 统计开始日期 |
| `endTime` | String | 统计结束日期 |
| `deptId` | Long | 部门维度（可选） |
| `zoneId` | Long | 战区维度（可选） |
| `groupBy` | String | 聚合维度：`sales_rep` / `dept` / `zone` / `month` |

**响应示例**：

```json
{
  "code": 200,
  "data": {
    "totalContractAmount": 5000000.00,
    "totalCommissionAmount": 500000.00,
    "totalCount": 120,
    "groupedList": [
      { "dimension": "张三", "contractAmount": 500000.00, "commissionAmount": 50000.00, "count": 12 },
      { "dimension": "李四", "contractAmount": 300000.00, "commissionAmount": 30000.00, "count": 8 }
    ]
  }
}
```

#### 前端新建

- **API 文件**：`api/perf-summary.js`
  - `getSummary(params)` — `GET /api/perfSummary/summary`
  - `getTrend(params)` — `GET /api/perfSummary/trend`
- **视图文件**：`views/perf-summary/index.vue`
  - 顶部统计卡片（总合同金额、总提成、合同数量）
  - 搜索条件（日期范围、部门、战区）
  - 分组表格（按销售/部门/战区展示）
  - 趋势图表（折线图，可选引入 ECharts）

---

### 4.3 perf-ranking — 业绩排名

#### 后端新建

在 `system` 模块新建 `PerfRankingController`：

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/perfRanking/list` | GET | 业绩排名列表 |
| `/api/perfRanking/export` | GET | 导出排名 Excel（可选） |

**查询参数**（`/api/perfRanking/list`）：

| 参数 | 类型 | 说明 |
|------|------|------|
| `beginTime` | String | 统计开始日期 |
| `endTime` | String | 统计结束日期 |
| `deptId` | Long | 部门筛选（可选） |
| `rankingType` | String | 排名类型：`commission`（按提成）/ `contract_amount`（按合同额）/ `count`（按合同数） |
| `limit` | Integer | 返回前 N 名，默认 20 |

**响应示例**：

```json
{
  "code": 200,
  "data": {
    "rankings": [
      { "rank": 1, "salesRepName": "张三", "deptName": "销售一部", "contractAmount": 5000000.00, "commissionAmount": 500000.00, "count": 45 },
      { "rank": 2, "salesRepName": "李四", "deptName": "销售二部", "contractAmount": 4500000.00, "commissionAmount": 450000.00, "count": 40 }
    ],
    "totalSalesReps": 25
  }
}
```

#### 前端新建

- **API 文件**：`api/perf-ranking.js`
  - `getRankingList(params)` — `GET /api/perfRanking/list`
  - `exportRanking(params)` — `GET /api/perfRanking/export`
- **视图文件**：`views/perf-ranking/index.vue`
  - 搜索条件（日期范围、部门、排名类型）
  - 排名表格（显示名次、销售人员姓名、部门、合同金额、提成金额、合同数）
  - 前三名高亮展示

---

## 五、Gateway 路由配置现状

### 5.1 当前已知问题

以下路由存在配置问题，需确认是否已修复：

| 问题 | 描述 | 影响 |
|------|------|------|
| `/sysZone/*` 被错误路由到 `auth:8085` | Gateway `auth-api-route` 包含 `Path=/sysZone/**`，但 Controller 在 `system:8082` | 战区管理接口 404 |
| `/sysDepartment/*` 被错误路由到 `auth:8085` | 同上，Controller 在 `system:8082` | 部门管理接口 404 |
| 缺少 `/api/perfSummary/**` 和 `/api/perfRanking/**` 的 Gateway 路由 | 新 Controller 部署后无路由指向 | 新接口 404 |

**建议修复方案（仅供参考，不在本文档修改范围内）：**

```yaml
# gateway/application.yml 新增
- id: system-api-route
  uri: http://localhost:8082
  predicates:
    - Path=/sysZone/**
    - Path=/sysDepartment/**
  filters:
    - StripPrefix=0
    - PrefixPath=/api
```

### 5.2 Vite Proxy 配置确认

`vite.config.js` 中的 proxy 需要与 Gateway 路由对应。确认以下代理已配置：

| 代理路径 | 目标 | 状态 |
|---------|------|------|
| `/sysZone` | → Gateway | 需确认 |
| `/sysDepartment` | → Gateway | 需确认 |
| `/system/api` | → Gateway | ✅ 已有 |
| `/sales/api` | → Gateway | ✅ 已有 |
| `/finance/api` | → Gateway | ✅ 已有 |

---

## 六、现有页面开发模板

以 `views/sales/customer/index.vue` 为标准模板，所有列表页应遵循以下模式。

### 6.1 目录结构规范

```
views/[module]/[page]/index.vue    ← 主视图文件
api/[module]/[page].js              ← API 文件（可选）
```

### 6.2 标准视图模板

```vue
<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮区 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
      </el-col>
    </el-row>

    <!-- 表格区域 -->
    <el-table v-loading="loading" :data="dataList">
      <el-table-column label="ID" align="center" prop="id" width="80" />
      <el-table-column label="名称" align="center" prop="name" />
      <el-table-column label="状态" align="center" prop="status">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
            {{ scope.row.status === 1 ? '有效' : '无效' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total"
                v-model:page="queryParams.pageNum"
                v-model:limit="queryParams.pageSize"
                @pagination="getList" />

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { listXxx, getXxx, delXxx, addXxx, updateXxx } from "@/api/[module]/xxx"

const { proxy } = getCurrentInstance()
const dataList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const title = ref("")
const queryParams = reactive({ pageNum: 1, pageSize: 10 })
const form = ref({})
const rules = ref({})

onMounted(() => { getList() })

function getList() {
  loading.value = true
  listXxx(queryParams).then(res => {
    dataList.value = res.data?.list || res.data || []
    total.value = res.data?.total || 0
    loading.value = false
  })
}

function handleQuery() { queryParams.pageNum = 1; getList() }
function resetQuery() { /* 重置 queryParams */ }
function handleAdd() { title.value = "新增"; open.value = true }
function handleUpdate(row) { title.value = "编辑"; open.value = true; form.value = { ...row } }
function submitForm() {
  const fn = form.value.id ? updateXxx : addXxx
  fn(form.value).then(() => {
    ElMessage.success('操作成功')
    open.value = false
    getList()
  })
}
function cancel() { open.value = false }
function handleDelete(row) {
  proxy.$confirm('是否确认删除？').then(() => delXxx(row.id)).then(() => getList())
}
</script>
```

---

## 七、开发任务分解与责任人建议

### 7.1 任务分解

| 任务 ID | 描述 | 类型 | 依赖 | 建议工时 |
|---------|------|------|------|---------|
| T1 | 新建 `views/system/log/index.vue` | 前端 | 操作日志 API 已存在 | 0.5d |
| T2 | 确认公海客户 `status` 值（或新增专用接口） | 后端确认 | T3 阻塞 | 0.25d |
| T3 | 新建 `views/sales/public-sea/index.vue` | 前端 | T2 解除后 | 0.5d |
| T4 | 新建 `views/sales/customer-view/index.vue` | 前端 | 接口复用 | 1d |
| T5 | 新建 `views/sales/contract-view/index.vue` | 前端 | 接口复用 | 1d |
| T6 | 新建 `views/sales/contract-sign/index.vue` | 前端 | 接口复用 | 0.5d |
| T7 | 修复 `transfer` 路由不匹配（改目录名或对齐后端组件路径） | 前端+后端 | 无 | 0.25d |
| T8 | 新建 `perf-summary` 后端 Controller + Service + DAO | 后端 | 新表/视图（可选） | 2d |
| T9 | 新建 `perf-ranking` 后端 Controller + Service + DAO | 后端 | T8 完成后可并行 | 2d |
| T10 | 新建 `views/perf-summary/index.vue` + `api/perf-summary.js` | 前端 | T8 完成后 | 1d |
| T11 | 新建 `views/perf-ranking/index.vue` + `api/perf-ranking.js` | 前端 | T9 完成后 | 1d |
| T12 | 修复 Gateway `/sysZone` / `/sysDepartment` 路由错误 | 后端基建 | 影响所有相关功能 | 0.5d |

### 7.2 推荐开发顺序

```
Phase 1（1-2天，可并行）
  T1 → T4 → T5 → T6
  后端基建: T12（优先修复，影响所有功能）

Phase 2（需先确认公海状态）
  T2 → T3

Phase 3（业绩模块，2-3天）
  T8（后端）→ T10（前端）
  T9（后端）→ T11（前端）
  T8 和 T9 可并行开发

Phase 4（扫尾）
  T7（transfer 路径修复）
```

---

## 八、待确认事项（开发前必须澄清）

| # | 问题 | 涉及功能 | 答复人 |
|---|------|---------|-------|
| 1 | 公海客户的 `status` 字段值是多少？还是有独立的公海客户接口？ | public-sea | 后端 |
| 2 | 合同签署成功后，跳转目标页面是哪里？ | contract-sign | 产品/业务 |
| 3 | `contract/listByCustomerId/{customerId}` 接口是否存在？ | customer-view | 后端 |
| 4 | 业绩汇总/排名的"时间范围"默认展示什么区间（本月/本季度/本年）？ | perf-summary, perf-ranking | 产品/业务 |
| 5 | 业绩排名是否需要导出 Excel 功能？ | perf-ranking | 产品/业务 |
| 6 | `perf-summary` 和 `perf-ranking` 是否放在 `system` 模块下（当前建议），还是放在 `sales` 模块？ | 后端架构 | 后端 |

---

## 九、NOT in Scope（本文档不包含）

1. **Gateway 路由配置的修改**（属于基础设施，不属于页面开发）
2. **Vite proxy 配置的调整**（属于开发环境配置）
3. **数据库新表创建**（perf-summary 和 perf-ranking 均基于现有 `performance_record` 表做聚合查询，无需新建表）
4. **移动端适配**（H5/小程序等）
5. **数据导出 Excel 功能**（除非 product/业务明确要求，否则暂不规划）

---

*本报告由 Claude Code 分析生成，基于 getRouters.md 路由清单、ruoyi-ui/src/views 目录扫描、全部 Controller 接口定义及数据库表结构。*
