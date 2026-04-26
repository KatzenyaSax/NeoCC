# 金融审核/提成记录功能扩展 — 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 对贷款审核页面、合同管理页面、提成记录页面进行功能扩展，包括名称显示、银行放款按钮、自动创建提成记录。

**Architecture:** 模块A/C通过后端JOIN返回名称字段，前端直接显示；模块B通过Feign跨服务调用在状态变更时自动创建提成记录。

**Tech Stack:** MyBatis-Plus, Spring Feign, Vue Element Plus

---

## 文件结构

### 模块A: 贷款审核页面
- `sales/src/main/java/com/dafuweng/sales/entity/ContractEntity.java` — 新增 customerName, salesRepName
- `sales/src/main/java/com/dafuweng/sales/dao/ContractDao.java` — 新增 selectPageAuditWithNames
- `sales/src/main/java/com/dafuweng/sales/service/impl/ContractServiceImpl.java` — pageListAudit 改用新方法
- `ruoyi-ui/src/views/finance/loan-audit/index.vue` — 表格列显示名称

### 模块B: 合同管理 + 银行放款
- `sales/src/main/java/com/dafuweng/sales/service/impl/ContractServiceImpl.java` — 新增 bankLoan 方法
- `sales/src/main/java/com/dafuweng/sales/controller/ContractController.java` — 新增 bank-loan 接口
- `finance/src/main/java/com/dafuweng/finance/feign/FinanceFeignClient.java` — 新增 createCommissionRecord
- `ruoyi-ui/src/views/sales/contract/index.vue` — 新增"银行已放款"按钮

### 模块C: 提成记录页面
- `finance/src/main/java/com/dafuweng/finance/entity/CommissionRecordEntity.java` — 新增 salesRepName, contractNo
- `finance/src/main/java/com/dafuweng/finance/dao/CommissionRecordDao.java` — 新增 selectPageWithNames
- `finance/src/main/java/com/dafuweng/finance/service/impl/CommissionRecordServiceImpl.java` — pageList 改用新方法
- `ruoyi-ui/src/views/finance/commission/index.vue` — 表格列显示名称 + 侧边栏文字

---

## 任务清单

### 任务 A1: ContractEntity 新增 customerName, salesRepName

**Files:**
- Modify: `sales/src/main/java/com/dafuweng/sales/entity/ContractEntity.java`

- [ ] **Step 1: 添加非数据库字段**

在 `ContractEntity.java` 的 `salesRepId` 字段后添加：

```java
@TableField(exist = false)
private String customerName;

@TableField(exist = false)
private String salesRepName;
```

- [ ] **Step 2: 提交**

```bash
git add sales/src/main/java/com/dafuweng/sales/entity/ContractEntity.java
git commit -m "feat(sales): ContractEntity新增customerName和salesRepName非数据库字段"
```

---

### 任务 A2: ContractDao 新增 selectPageAuditWithNames

**Files:**
- Modify: `sales/src/main/java/com/dafuweng/sales/dao/ContractDao.java`

- [ ] **Step 1: 添加 JOIN 查询方法**

在 `ContractDao.java` 中新增方法：

```java
@Select("SELECT c.*, cu.name AS customerName, u.real_name AS salesRepName " +
        "FROM contract c " +
        "LEFT JOIN customer cu ON c.customer_id = cu.id " +
        "LEFT JOIN sys_user u ON c.sales_rep_id = u.id " +
        "WHERE c.deleted = 0 " +
        "ORDER BY c.created_at DESC")
IPage<ContractEntity> selectPageAuditWithNames(@Param("page") IPage<ContractEntity> page);
```

确保导入：
```java
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
```

- [ ] **Step 2: 提交**

```bash
git add sales/src/main/java/com/dafuweng/sales/dao/ContractDao.java
git commit -m "feat(sales): ContractDao新增selectPageAuditWithNames联查方法"
```

---

### 任务 A3: ContractServiceImpl.pageListAudit 改用新查询

**Files:**
- Modify: `sales/src/main/java/com/dafuweng/sales/service/impl/ContractServiceImpl.java`

- [ ] **Step 1: 确认调用方式**

找到 `pageListAudit` 方法（金融审核页面调用的分页方法），将其调用 `selectPage` 改为 `selectPageWithBank` 类似的模式：

```java
IPage<ContractEntity> result = contractDao.selectPageAuditWithNames(page);
```

注意：如果 `pageListAudit` 存在 filterRole 等过滤条件，需要调整 WHERE SQL。

- [ ] **Step 2: 提交**

```bash
git add sales/src/main/java/com/dafuweng/sales/service/impl/ContractServiceImpl.java
git commit -m "feat(sales): ContractServiceImpl.pageListAudit改用selectPageAuditWithNames"
```

---

### 任务 A4: 贷款审核页面表格列显示名称

**Files:**
- Modify: `ruoyi-ui/src/views/finance/loan-audit/index.vue`

- [ ] **Step 1: 修改表格列**

将：
```html
<el-table-column label="客户ID" align="center" prop="customerId" />
<el-table-column label="销售代表ID" align="center" prop="salesRepId" />
```

改为：
```html
<el-table-column label="客户" align="center" prop="customerName" />
<el-table-column label="销售代表" align="center" prop="salesRepName" />
```

- [ ] **Step 2: 提交**

```bash
git add ruoyi-ui/src/views/finance/loan-audit/index.vue
git commit -m "feat(finance): 贷款审核页面表格列显示客户和销售代表名称"
```

---

### 任务 B1: FinanceFeignClient 新增 createCommissionRecord 方法

**Files:**
- Modify: `finance/src/main/java/com/dafuweng/finance/feign/FinanceFeignClient.java`

- [ ] **Step 1: 添加Feign方法**

找到 `FinanceFeignClient`，新增方法：

```java
@PostMapping("/api/commissionRecord")
Result<CommissionRecordEntity> createCommissionRecord(@RequestBody CommissionRecordEntity entity);
```

- [ ] **Step 2: 提交**

```bash
git add finance/src/main/java/com/dafuweng/finance/feign/FinanceFeignClient.java
git commit -m "feat(finance): FinanceFeignClient新增createCommissionRecord方法"
```

---

### 任务 B2: ContractServiceImpl 新增 bankLoan 方法

**Files:**
- Modify: `sales/src/main/java/com/dafuweng/sales/service/impl/ContractServiceImpl.java`

- [ ] **Step 1: 新增 bankLoan 方法**

在文件末尾（`submitToFinance` 方法之后）添加：

```java
@Override
@Transactional
public void bankLoan(Long id) {
    ContractEntity contract = contractDao.selectById(id);
    if (contract == null) {
        throw new IllegalArgumentException("合同不存在");
    }
    if (contract.getStatus() != 5) {
        throw new IllegalStateException("当前状态不允许操作，状态：" + contract.getStatus());
    }
    // 更新合同状态为已放款
    contract.setStatus((short) 7);
    contractDao.updateById(contract);

    // 创建提成记录（serviceFee2）
    CommissionRecordEntity record = new CommissionRecordEntity();
    record.setId(financeFeignClient.getMinUnusedCommissionRecordId().getData());
    record.setSalesRepId(contract.getSalesRepId());
    record.setContractId(contract.getId());
    record.setCommissionAmount(contract.getServiceFee2());
    record.setStatus((short) 0);
    record.setDeleted((short) 0);
    financeFeignClient.createCommissionRecord(record);
}
```

注意需要注入 `FinanceFeignClient`（已有）。

确保导入：
```java
import com.dafuweng.finance.entity.CommissionRecordEntity;
```

- [ ] **Step 2: 提交**

```bash
git add sales/src/main/java/com/dafuweng/sales/service/impl/ContractServiceImpl.java
git commit -m "feat(sales): ContractServiceImpl新增bankLoan方法"
```

---

### 任务 B3: ContractController 新增 bank-loan 接口

**Files:**
- Modify: `sales/src/main/java/com/dafuweng/sales/controller/ContractController.java`

- [ ] **Step 1: 添加接口**

在 `submitToFinance` 方法后添加：

```java
/**
 * POST /api/contract/{id}/bank-loan
 * 银行已放款：更新状态为已放款，并创建提成记录
 */
@PostMapping("/{id}/bank-loan")
public Result<Void> bankLoan(@PathVariable Long id) {
    contractService.bankLoan(id);
    return Result.success();
}
```

- [ ] **Step 2: 提交**

```bash
git add sales/src/main/java/com/dafuweng/sales/controller/ContractController.java
git commit -m "feat(sales): ContractController新增bank-loan接口"
```

---

### 任务 B4: 合同页面新增"银行已放款"按钮

**Files:**
- Modify: `ruoyi-ui/src/views/sales/contract/index.vue`

- [ ] **Step 1: 在操作列添加按钮**

在 `handleSubmitToFinance` 按钮之后添加：

```html
<el-button link type="primary" icon="Money" @click="handleBankLoan(scope.row)" v-if="scope.row.status === 5 && (isSuperAdmin || isZoneDirector)">银行已放款</el-button>
```

- [ ] **Step 2: 添加 handleBankLoan 方法**

在 `handleSubmitToFinance` 方法后添加：

```javascript
/** 银行已放款按钮 */
function handleBankLoan(row) {
  proxy.$modal.confirm('是否确认合同"' + row.contractNo + '"银行已放款？').then(function () {
    return bankLoanContract(row.id)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("操作成功")
  }).catch(() => {})
}
```

- [ ] **Step 3: 导入API**

在 import 语句中添加 `bankLoanContract`（从 `@/api/sales/contract` 导入）。

- [ ] **Step 4: 提交**

```bash
git add ruoyi-ui/src/views/sales/contract/index.vue
git commit -m "feat(sales): 合同页面新增银行已放款按钮"
```

---

### 任务 C1: CommissionRecordEntity 新增 salesRepName, contractNo

**Files:**
- Modify: `finance/src/main/java/com/dafuweng/finance/entity/CommissionRecordEntity.java`

- [ ] **Step 1: 添加非数据库字段**

在 `contractId` 字段后添加：

```java
@TableField(exist = false)
private String salesRepName;

@TableField(exist = false)
private String contractNo;
```

确保导入：
```java
import com.baomidou.mybatisplus.annotation.TableField;
```

- [ ] **Step 2: 提交**

```bash
git add finance/src/main/java/com/dafuweng/finance/entity/CommissionRecordEntity.java
git commit -m "feat(finance): CommissionRecordEntity新增salesRepName和contractNo非数据库字段"
```

---

### 任务 C2: CommissionRecordDao 新增 selectPageWithNames

**Files:**
- Modify: `finance/src/main/java/com/dafuweng/finance/dao/CommissionRecordDao.java`

- [ ] **Step 1: 添加 JOIN 查询方法**

新增方法：

```java
@Select("SELECT cr.*, u.real_name AS salesRepName, c.contract_no AS contractNo " +
        "FROM commission_record cr " +
        "LEFT JOIN sys_user u ON cr.sales_rep_id = u.id " +
        "LEFT JOIN contract c ON cr.contract_id = c.id " +
        "WHERE cr.deleted = 0 " +
        "ORDER BY cr.created_at DESC")
IPage<CommissionRecordEntity> selectPageWithNames(@Param("page") IPage<CommissionRecordEntity> page);
```

确保导入：
```java
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
```

- [ ] **Step 2: 提交**

```bash
git add finance/src/main/java/com/dafuweng/finance/dao/CommissionRecordDao.java
git commit -m "feat(finance): CommissionRecordDao新增selectPageWithNames联查方法"
```

---

### 任务 C3: CommissionRecordServiceImpl.pageList 改用新查询

**Files:**
- Modify: `finance/src/main/java/com/dafuweng/finance/service/impl/CommissionRecordServiceImpl.java`

- [ ] **Step 1: 修改 pageList 方法**

将：
```java
IPage<CommissionRecordEntity> result = commissionRecordDao.selectPage(page, wrapper);
```

改为：
```java
IPage<CommissionRecordEntity> result = commissionRecordDao.selectPageWithNames(page);
```

注意：原有 wrapper 条件不再适用，新方法独立实现分页。

- [ ] **Step 2: 提交**

```bash
git add finance/src/main/java/com/dafuweng/finance/service/impl/CommissionRecordServiceImpl.java
git commit -m "feat(finance): CommissionRecordServiceImpl.pageList改用selectPageWithNames"
```

---

### 任务 C4: 提成记录页面表格列显示名称

**Files:**
- Modify: `ruoyi-ui/src/views/finance/commission/index.vue`

- [ ] **Step 1: 修改表格列**

将：
```html
<el-table-column label="销售代表ID" align="center" prop="salesRepId" />
<el-table-column label="合同ID" align="center" prop="contractId" />
```

改为：
```html
<el-table-column label="销售代表" align="center" prop="salesRepName" />
<el-table-column label="合同" align="center" prop="contractNo" />
```

- [ ] **Step 2: 提交**

```bash
git add ruoyi-ui/src/views/finance/commission/index.vue
git commit -m "feat(finance): 提成记录页面表格列显示销售代表和合同编号"
```

---

## 验证

### 模块A验证
1. 启动 sales 模块
2. 访问贷款审核页面 `GET /finance/loan-audit`
3. 表格"客户"列显示客户名称（非ID）
4. 表格"销售代表"列显示销售代表名称（非ID）

### 模块B验证
1. 启动 sales + finance 模块
2. 访问合同管理页面，找到状态为"已审核"的合同
3. 操作列出现"银行已放款"按钮
4. 点击按钮后，合同状态变为"已放款"
5. 访问提成记录页面，新增多条记录，佣金金额为 serviceFee2

### 模块C验证
1. 访问提成记录页面
2. "销售代表"列显示名称（非ID）
3. "合同"列显示合同编号（非ID）
4. 侧边栏菜单显示"服务费记录"
