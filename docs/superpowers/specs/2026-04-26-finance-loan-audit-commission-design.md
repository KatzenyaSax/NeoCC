# 金融审核/提成记录功能扩展 — 设计方案

## 概述

对前端金融审核/贷款审核、提成记录及销售管理/合同管理进行功能扩展。

---

## 模块A: 金融审核/贷款审核 — 表格显示客户名和销售代表名

### 改动范围

**前端：** `ruoyi-ui/src/views/finance/loan-audit/index.vue`
- 表格列 `客户ID` → 改为 `客户`，显示客户名称
- 表格列 `销售代表ID` → 改为 `销售代表`，显示销售代表名称

**后端：** sales 模块
- `ContractEntity` 新增 `@TableField(exist = false) private String customerName` 和 `salesRepName`
- `ContractDao` 新增 `selectPageAuditWithNames` LEFT JOIN customer + user
- `ContractServiceImpl.pageListAudit`（或对应分页方法）改用新查询

### 数据流

```
ContractDao.selectPageAuditWithNames
  → LEFT JOIN customer ON contract.customer_id = customer.id
  → LEFT JOIN user ON contract.sales_rep_id = user.id
  → 返回 customerName, salesRepName
```

---

## 模块B: 销售管理/合同管理 — 银行已放款按钮 + 自动创建提成记录

### 改动范围

**前端：** `ruoyi-ui/src/views/sales/contract/index.vue`
- 表格操作列：状态=5(已审核)时显示"银行已放款"按钮
- 点击按钮 → 调用 `POST /api/contract/{id}/bank-loan` → 后端处理状态变更+创建提成记录

**后端：** sales 模块
- `ContractServiceImpl` 新增 `bankLoan(Long id)` 方法：
  1. 检查合同状态必须为5(已审核)
  2. 更新合同状态为7(已放款)
  3. 通过 Feign 调用金融部 `CommissionRecordService` 新增提成记录，佣金金额 = `serviceFee2`
  4. 提成记录 `salesRepId` = 合同的 `salesRepId`，`contractId` = 合同ID
- `ContractController` 新增 `POST /api/contract/{id}/bank-loan`

### 业务规则

| 触发时机 | 状态变更 | 提成记录佣金金额 |
|---------|---------|----------------|
| 点击"银行已放款" | 5(已审核) → 7(已放款) | `serviceFee2` |
| 点击"已支付首期"（已有） | 2(已签署) → 3(已付首期) | `serviceFee1` |

### 提成记录字段填充

| 字段 | 值 |
|------|-----|
| `id` | 后端查询 commission_record 最小未使用ID |
| `salesRepId` | `contract.salesRepId` |
| `contractId` | `contract.id` |
| `commissionAmount` | `contract.serviceFee2`（或`serviceFee1`） |
| `status` | 0（待确认） |
| `deleted` | 0 |

---

## 模块C: 金融审核/提成记录 — 表格显示销售代表名和合同编号 + 侧边栏改名

### 改动范围

**前端：** `ruoyi-ui/src/views/finance/commission/index.vue`
- 表格列 `销售代表ID` → 改为 `销售代表`，显示销售代表名称
- 表格列 `合同ID` → 改为 `合同`，显示合同编号
- 侧边栏菜单：将"提成记录"菜单项文字改为"服务费记录"（前端菜单配置，非代码）

**后端：** finance 模块
- `CommissionRecordEntity` 新增 `@TableField(exist = false) private String salesRepName` 和 `contractNo`
- `CommissionRecordDao` 新增 `selectPageWithNames` — LEFT JOIN user + contract
- `CommissionRecordServiceImpl.pageList` 改用新查询

### 数据流

```
CommissionRecordDao.selectPageWithNames
  → LEFT JOIN user ON commission_record.sales_rep_id = user.id
  → LEFT JOIN contract ON commission_record.contract_id = contract.id
  → 返回 salesRepName, contractNo
```

---

## 影响文件清单

### 前端
- `ruoyi-ui/src/views/finance/loan-audit/index.vue`
- `ruoyi-ui/src/views/sales/contract/index.vue`
- `ruoyi-ui/src/views/finance/commission/index.vue`

### 后端 sales 模块
- `sales/src/main/java/com/dafuweng/sales/entity/ContractEntity.java`
- `sales/src/main/java/com/dafuweng/sales/dao/ContractDao.java`
- `sales/src/main/java/com/dafuweng/sales/service/impl/ContractServiceImpl.java`
- `sales/src/main/java/com/dafuweng/sales/controller/ContractController.java`

### 后端 finance 模块
- `finance/src/main/java/com/dafuweng/finance/entity/CommissionRecordEntity.java`
- `finance/src/main/java/com/dafuweng/finance/dao/CommissionRecordDao.java`
- `finance/src/main/java/com/dafuweng/finance/service/impl/CommissionRecordServiceImpl.java`

---

## 验证要点

1. 贷款审核页面表格"客户"列显示客户名称（非ID）
2. 贷款审核页面表格"销售代表"列显示销售代表名称（非ID）
3. 合同状态为"已审核"时，操作列出现"银行已放款"按钮
4. 点击"银行已放款"后，合同状态变为"已放款"，提成记录新增一条
5. 提成记录页面表格"销售代表"列显示名称，"合同"列显示合同编号
6. 侧边栏菜单显示"服务费记录"
