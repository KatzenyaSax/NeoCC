# 贷款审核功能改造规范

**创建时间：** 2026-04-20
**作者：** Claude

---

## 一、需求概述

修改贷款审核功能，要求：
1. 只显示 `contract.status = 4`（审核中/已发送金融部）的合同
2. 表格显示所有审核中的合同，操作列有"查看"按钮
3. 点击"查看"弹出详情弹窗，显示合同完整信息（含客户、销售代表信息）
4. 详情弹窗下方有"通过"和"拒绝"按钮
5. 点击"通过"：合同 status 变为 5（已通过）
6. 点击"拒绝"：弹出拒绝理由输入框，合同 status 变为 6（已拒绝）

---

## 二、后端接口

### 2.1 新增接口

**1. 分页查询审核中合同**
```
GET /api/finance/contract-audit/page
Query: pageNum, pageSize, contractNo(optional)
Response: { code: 200, data: { records: [...], total: N } }
```

**2. 获取合同详情（含关联信息）**
```
GET /api/finance/contract-audit/{contractId}/detail
Response: {
  code: 200,
  data: {
    contract: { id, contractNo, contractAmount, actualLoanAmount, ... },
    customer: { id, name, phone, ... },
    salesRep: { id, realName, ... },
    dept: { id, deptName, ... },
    zone: { id, zoneName, ... }
  }
}
```

**3. 通过审核**
```
POST /api/finance/contract-audit/{contractId}/approve
Request: { auditOpinion: "审核意见" }
Response: { code: 200 }
```
操作后 contract.status = 5

**4. 拒绝审核**
```
POST /api/finance/contract-audit/{contractId}/reject
Request: { rejectReason: "拒绝原因" }
Response: { code: 200 }
```
操作后 contract.status = 6

### 2.2 文件清单

| 文件 | 操作 |
|------|------|
| `finance/src/main/java/com/dafuweng/finance/controller/ContractAuditController.java` | 新建 |
| `finance/src/main/java/com/dafuweng/finance/service/ContractAuditService.java` | 新建 |
| `finance/src/main/java/com/dafuweng/finance/service/impl/ContractAuditServiceImpl.java` | 新建 |
| `common/src/main/java/com/dafuweng/common/entity/vo/ContractDetailVO.java` | 新建 |

---

## 三、前端页面

### 3.1 页面改造

**文件：** `ruoyi-ui/src/views/finance/loan-audit/index.vue`

**改动点：**
1. 查询表单：移除 status 选择，只查 status=4 的合同
2. 表格列：显示合同编号、客户ID、销售代表ID、合同金额、状态、创建时间
3. 操作列：只保留"查看"按钮
4. 详情弹窗：显示合同完整信息 + 客户信息 + 销售代表信息
5. 详情弹窗底部：增加"通过"和"拒绝"按钮

### 3.2 API 文件

**文件：** `ruoyi-ui/src/api/finance/contractAudit.js`（新建）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/contractAudit/page` | 分页查询审核中合同 |
| GET | `/contractAudit/{id}/detail` | 获取合同详情 |
| POST | `/contractAudit/{id}/approve` | 通过审核 |
| POST | `/contractAudit/{id}/reject` | 拒绝审核 |

### 3.3 文件清单

| 文件 | 操作 |
|------|------|
| `ruoyi-ui/src/api/finance/contractAudit.js` | 新建 |
| `ruoyi-ui/src/views/finance/loan-audit/index.vue` | 修改 |

---

## 四、数据模型

### 4.1 ContractDetailVO

```java
public class ContractDetailVO {
    private ContractEntity contract;      // 合同信息
    private Map<String, Object> customer; // 客户信息（id, name, phone）
    private Map<String, Object> salesRep; // 销售代表信息（id, realName）
    private Map<String, Object> dept;    // 部门信息（id, deptName）
    private Map<String, Object> zone;     // 战区信息（id, zoneName）
}
```

### 4.2 合同状态

| status | 含义 |
|--------|------|
| 4 | 审核中（查询条件） |
| 5 | 已通过 |
| 6 | 已拒绝 |

---

## 五、执行步骤

### 步骤1：后端
1. 创建 `ContractAuditController`
2. 创建 `ContractAuditService` 接口
3. 创建 `ContractAuditServiceImpl` 实现
4. 创建 `ContractDetailVO`

### 步骤2：前端
1. 创建 `contractAudit.js` API
2. 修改 `loan-audit/index.vue` 页面

### 步骤3：测试验证
