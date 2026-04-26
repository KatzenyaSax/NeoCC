# 银行已放款 — 同步创建 commission_record 设计方案

## 概述

修复"银行已放款"功能：在 `performance_record` 创建业绩记录后，同步在 `commission_record` 表中创建提成记录，并将所有时间字段统一为北京时间（UTC+8）。

## 当前问题

`ContractServiceImpl.bankLoan()` 只创建了 `PerformanceRecordEntity`，缺少 `CommissionRecordEntity` 的创建。

## 改动范围

### 后端

**1 个文件：** `sales/src/main/java/com/dafuweng/sales/service/impl/ContractServiceImpl.java`

`bankLoan()` 方法在保存 `PerformanceRecordEntity` 后追加：

1. 通过 `FinanceFeignClient.getMinUnusedCommissionRecordId()` 获取 commission_record 最小未使用 ID
2. 通过 `FinanceFeignClient.createCommissionRecord(Map)` 创建提成记录

#### commission_record 字段填充

| 字段 | 值 |
|------|-----|
| `id` | `getMinUnusedCommissionRecordId()` |
| `performanceId` | 刚保存的 `PerformanceRecordEntity.id` |
| `salesRepId` | `contract.salesRepId` |
| `contractId` | `contract.id` |
| `commissionAmount` | `contractAmount * 0.015` |
| `commissionRate` | `0.015` |
| `status` | `1`（计算中/待确认） |
| `deleted` | `0` |
| `createdAt` | 北京时间当前时间 |

#### 北京时间处理

`new Date()` 替换为北京时间（UTC+8），影响字段：

- `PerformanceRecordEntity.calculateTime`
- `CommissionRecordEntity.createdAt`

### 测试

**1 个文件：** `sales/src/test/java/com/dafuweng/sales/service/impl/ContractServiceImplTest.java`

bankLoan 相关测试追加 mock 和 verify：
- `when(financeFeignClient.getMinUnusedCommissionRecordId()).thenReturn(...)`
- `verify(financeFeignClient).createCommissionRecord(anyMap())`

## 数据流

```
bankLoan(id)
  → contractDao.selectById(id)          // 查合同
  → contractDao.updateById(contract)     // 状态→7, 设置actualLoanAmount
  → financeFeignClient.createServiceFeeRecord()  // 首期服务费记录
  → performanceRecordService.getMinUnusedId()    // 业绩记录ID
  → performanceRecordService.save(entity)        // 业绩记录
  → financeFeignClient.getMinUnusedCommissionRecordId()  // 提成记录ID  ← 新增
  → financeFeignClient.createCommissionRecord(map)        // 提成记录    ← 新增
```

## 验证要点

1. 点击"银行已放款"后 performance_record 和 commission_record 各新增 1 条
2. commission_record.performance_id = performance_record.id
3. commission_record.commission_amount = contract_amount * 0.015
4. calculateTime 和 createdAt 均为北京时间
