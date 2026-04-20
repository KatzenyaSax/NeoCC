# 合同管理系统开发规范

**创建时间：** 2026-04-20
**作者：** Claude

---

## 一、需求概述

将"合同列表"/"新增合同"/"编辑合同"合并为侧边栏的"合同列表"（改名为"合同管理"），实现一站式解决合同的编辑/删除/查看详情等功能。

### 功能要点

1. **表格显示**：展示所有合同，每行包含"编辑"/"详情"/"删除"按钮
2. **新增改签署**：左上角"新增"按钮改为"签署"，点击弹窗填写销售信息/客户信息/合同细则
3. **合同编号自动生成**：格式 `HT-YYYYMMDD-XXXX`（如 `HT-20260420-0001`）
4. **详情按钮**：显示合同全部详细信息
5. **编辑按钮**：显示合同全部详细信息并可修改

---

## 二、页面重构

### 2.1 路由调整

**侧边栏路由合并：**
- 删除：`/contract-add`、`/contract-edit`
- 保留：`/contract-list`（改名为"合同管理"）

### 2.2 页面模式

`ruoyi-ui/src/views/sales/contract/index.vue` 重构为单页面，支持四种模式：

| 模式 | 触发方式 | 弹窗内容 |
|------|----------|----------|
| 列表模式 | 默认 | 显示表格和搜索 |
| 签署模式 | 点击"签署"按钮 | 完整合同表单（空数据） |
| 详情模式 | 点击"详情"按钮 | 仅展示，不可编辑 |
| 编辑模式 | 点击"编辑"按钮 | 完整合同表单（预填充数据） |

### 2.3 弹窗字段

**销售信息：**
| 字段名 | 字段key | 类型 | 说明 |
|--------|---------|------|------|
| 销售代表 | salesRepId | select | 下拉选择 |
| 部门 | deptId | select | 下拉选择 |
| 战区 | zoneId | select | 下拉选择 |
| 产品 | productId | select | 下拉选择 |

**客户信息：**
| 字段名 | 字段key | 类型 | 说明 |
|--------|---------|------|------|
| 客户 | customerId | select | 下拉选择 |

**合同细则：**
| 字段名 | 字段key | 类型 | 说明 |
|--------|---------|------|------|
| 合同编号 | contractNo | string | 自动生成，不可编辑 |
| 合同金额 | contractAmount | number | |
| 实际贷款金额 | actualLoanAmount | number | |
| 服务费率 | serviceFeeRate | number | |
| 服务费1 | serviceFee1 | number | |
| 服务费2 | serviceFee2 | number | |
| 服务费1是否已付 | serviceFee1Paid | select | 0-否，1-是 |
| 服务费2是否已付 | serviceFee2Paid | select | 0-否，1-是 |
| 签署日期 | signDate | date | |
| 纸质合同编号 | paperContractNo | string | |
| 贷款用途 | loanUse | string | textarea |
| 担保信息 | guaranteeInfo | string | textarea |
| 备注 | remark | string | textarea |

**状态：**
| 字段名 | 字段key | 类型 | 说明 |
|--------|---------|------|------|
| 状态 | status | select | 0-待签署，1-草稿，2-已签署，3-已付首期，4-审核中，5-已通过，6-已拒绝，7-已放款，8-完成 |

---

## 三、后端接口

### 3.1 新增接口

**1. 生成合同编号**
```
GET /api/contract/generateNo
Response: { code: 200, data: "HT-20260420-0001" }
```

**2. 获取合同详情（含关联信息）**
```
GET /api/contract/{id}/detail
Response: { code: 200, data: { contract: {...}, customer: {...}, salesRep: {...}, ... } }
```

### 3.2 修改接口

**签署合同**
- 接口：`POST /api/contract/{id}/sign`
- 触发条件：status=0（待签署）或 status=1（草稿）
- 签署后状态变为 2（已签署），设置 signDate

### 3.3 现有接口清单

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/contract/page | 分页查询 |
| GET | /api/contract/{id} | 获取单个合同 |
| GET | /api/contract/getByContractNo/{contractNo} | 按编号查询 |
| POST | /api/contract | 新增合同 |
| PUT | /api/contract | 修改合同 |
| DELETE | /api/contract/{id} | 删除合同 |
| POST | /api/contract/{id}/sign | 签署合同 |

---

## 四、文件清单

### 4.1 前端文件

| 文件路径 | 操作 |
|----------|------|
| `ruoyi-ui/src/views/sales/contract/index.vue` | 重构 |
| `ruoyi-ui/src/api/sales/contract.js` | 新增 `generateNo` 和 `getContractDetail` |
| `ruoyi-ui/src/router/index.js` | 路由调整（如需） |
| `ruoyi-ui/src/views/index.vue` | 侧边栏名称修改（合同列表→合同管理） |

### 4.2 后端文件

| 文件路径 | 操作 |
|----------|------|
| `sales/src/main/java/com/dafuweng/sales/controller/ContractController.java` | 新增 `generateNo` 和 `detail` 端点 |
| `sales/src/main/java/com/dafuweng/sales/service/ContractService.java` | 新增方法签名 |
| `sales/src/main/java/com/dafuweng/sales/service/impl/ContractServiceImpl.java` | 实现新方法 |

---

## 五、数据校验规则

1. **签署前校验**：
   - 合同金额 > 0
   - 客户已选择
   - 销售代表已选择
   - 状态必须为 0（待签署）或 1（草稿）

2. **删除前校验**：
   - 状态为 0（待签署）或 1（草稿）可删除
   - 其他状态不可删除

3. **编辑权限**：
   - 状态为 0 或 1 可编辑
   - 其他状态仅可查看详情

---

## 六、状态流转

```
0(待签署) ──签署──> 2(已签署)
                   │
1(草稿) ──签署──> 2(已签署) ──付首期──> 3(已付首期) ──审核──> 4(审核中)
                                                       │
                                                       ├──通过──> 5(已通过) ──放款──> 7(已放款) ──完成──> 8(完成)
                                                       │
                                                       └──拒绝──> 6(已拒绝)
```

---

## 七、合同编号生成规则

**格式：** `HT-YYYYMMDD-XXXX`

- `HT`：固定前缀
- `YYYYMMDD`：8位日期
- `XXXX`：当日序号，从0001开始，不足4位补前导零

**示例：** `HT-20260420-0001`、`HT-20260420-0042`

---

## 八、执行步骤

### 步骤1：后端接口
1. 在 `ContractController` 新增 `generateNo` 端点
2. 在 `ContractController` 新增 `detail` 端点（含关联数据）
3. 在 `ContractService` 和 `Impl` 添加并实现对应方法

### 步骤2：前端API
1. 在 `ruoyi-ui/src/api/sales/contract.js` 新增 `generateNo()` 和 `getContractDetail(id)`

### 步骤3：前端页面重构
1. 重构 `ruoyi-ui/src/views/sales/contract/index.vue`：
   - 支持列表/签署/详情/编辑四种模式
   - 实现弹窗表单完整字段
   - 签署按钮自动生成合同编号
2. 修改 `ruoyi-ui/src/views/index.vue` 侧边栏名称

### 步骤4：测试验证
1. 测试合同编号自动生成
2. 测试新增/编辑/删除功能
3. 测试详情展示
4. 测试状态流转