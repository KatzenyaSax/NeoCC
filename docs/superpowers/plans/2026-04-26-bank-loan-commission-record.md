# bank-loan commission_record 同步创建 + 北京时间 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 银行已放款时同步创建 commission_record，且所有时间字段使用北京时间

**Architecture:** 在 ContractServiceImpl.bankLoan() 中 performanceRecordService.save() 后追加 Feign 调用 finance 模块创建 commission_record；用 ZonedDateTime(Asia/Shanghai) 替代 new Date()

**Tech Stack:** Java, Spring Boot, MyBatis-Plus, OpenFeign, JUnit 5 + Mockito

---

### Task 1: 修改 bankLoan() — 北京时间 + commission_record 创建

**Files:**
- Modify: `sales/src/main/java/com/dafuweng/sales/service/impl/ContractServiceImpl.java:421-470`

- [ ] **Step 1: 添加 import**

在文件顶部 import 区域追加（按字母序放入已有 java.time 导入附近）：

```java
import java.time.ZoneId;
import java.time.ZonedDateTime;
```

- [ ] **Step 2: 提取北京时间工具方法并替换 new Date()**

将 `bankLoan()` 方法中第 450 行和第 467 行的 `new Date()` 替换为北京时间获取，并在方法内定义局部 helper：

修改第 421-470 行为：

```java
    @Transactional
    @Override
    public void bankLoan(Long id) {
        ContractEntity contract = contractDao.selectById(id);
        if (contract == null) {
            throw new IllegalArgumentException("合同不存在");
        }
        if (contract.getStatus() != 5) {
            throw new IllegalStateException("当前状态不允许操作，状态：" + contract.getStatus());
        }
        // 计算实际放款金额：合同金额 - 服务费1 - 服务费2
        BigDecimal actualLoanAmount = contract.getContractAmount()
                .subtract(contract.getServiceFee1() != null ? contract.getServiceFee1() : BigDecimal.ZERO)
                .subtract(contract.getServiceFee2() != null ? contract.getServiceFee2() : BigDecimal.ZERO);

        // 更新合同状态为已放款，并标记服务费2已支付，设置实际放款金额
        contract.setStatus((short) 7);
        contract.setServiceFee2Paid((short) 1);
        contract.setActualLoanAmount(actualLoanAmount);
        contractDao.updateById(contract);

        Date now = Date.from(ZonedDateTime.now(ZoneId.of("Asia/Shanghai")).toInstant());

        // 创建首期服务费记录
        Map<String, Object> serviceFeeRecord = new HashMap<>();
        serviceFeeRecord.put("id", financeFeignClient.getMinUnusedServiceFeeRecordId().getData());
        serviceFeeRecord.put("contractId", contract.getId());
        serviceFeeRecord.put("feeType", (short) 1);
        serviceFeeRecord.put("amount", contract.getServiceFee1());
        serviceFeeRecord.put("shouldAmount", contract.getServiceFee1());
        serviceFeeRecord.put("paymentStatus", (short) 1);
        serviceFeeRecord.put("paymentDate", now);
        serviceFeeRecord.put("accountantId", 1);
        serviceFeeRecord.put("deleted", (short) 0);
        financeFeignClient.createServiceFeeRecord(serviceFeeRecord);

        // 创建业绩记录（提成记录）：提成金额为合同金额的 1.5%
        BigDecimal commissionAmount = contract.getContractAmount().multiply(new BigDecimal("0.015"));
        PerformanceRecordEntity performanceRecord = new PerformanceRecordEntity();
        performanceRecord.setId(performanceRecordService.getMinUnusedId());
        performanceRecord.setContractId(contract.getId());
        performanceRecord.setSalesRepId(contract.getSalesRepId());
        performanceRecord.setDeptId(contract.getDeptId());
        performanceRecord.setZoneId(contract.getZoneId());
        performanceRecord.setContractAmount(contract.getContractAmount());
        performanceRecord.setCommissionRate(new BigDecimal("0.015"));
        performanceRecord.setCommissionAmount(commissionAmount);
        performanceRecord.setStatus((short) 1);
        performanceRecord.setCalculateTime(now);
        performanceRecord.setDeleted((short) 0);
        performanceRecordService.save(performanceRecord);

        // 创建提成发放记录：ID 从 commission_record 表查询，performance_id 绑定业绩记录 ID
        Map<String, Object> commissionRecord = new HashMap<>();
        commissionRecord.put("id", financeFeignClient.getMinUnusedCommissionRecordId().getData());
        commissionRecord.put("performanceId", performanceRecord.getId());
        commissionRecord.put("salesRepId", contract.getSalesRepId());
        commissionRecord.put("contractId", contract.getId());
        commissionRecord.put("commissionAmount", commissionAmount);
        commissionRecord.put("commissionRate", new BigDecimal("0.015"));
        commissionRecord.put("status", (short) 1);
        commissionRecord.put("createdAt", now);
        commissionRecord.put("deleted", (short) 0);
        financeFeignClient.createCommissionRecord(commissionRecord);
    }
```

修改要点：
- `Date now = Date.from(ZonedDateTime.now(ZoneId.of("Asia/Shanghai")).toInstant())` 统一北京时间
- 第 450 行 `new Date()` → `now`
- 第 467 行 `new Date()` → `now`
- 第 469 行后新增 11 行：Feign 调用创建 commission_record

- [ ] **Step 3: 编译验证**

```bash
mvn compile -pl sales -am -DskipTests -q
```

Expected: BUILD SUCCESS

---

### Task 2: 更新测试 — 追加 commission_record 的 mock 和 verify

**Files:**
- Modify: `sales/src/test/java/com/dafuweng/sales/service/impl/ContractServiceImplTest.java:78-153`

- [ ] **Step 1: 更新 bankLoan_Status5_Success_CalculatesActualLoanAndCreatesRecords**

替换第 78-108 行为：

```java
    @Test
    void bankLoan_Status5_Success_CalculatesActualLoanAndCreatesRecords() {
        // 准备测试数据
        ContractEntity contract = new ContractEntity();
        contract.setId(1L);
        contract.setStatus((short) 5);
        contract.setSalesRepId(10L);
        contract.setContractAmount(new BigDecimal("100000"));
        contract.setServiceFee1(new BigDecimal("5000"));
        contract.setServiceFee2(new BigDecimal("3000"));
        contract.setDeptId(2L);
        contract.setZoneId(3L);

        when(contractDao.selectById(1L)).thenReturn(contract);
        when(financeFeignClient.getMinUnusedServiceFeeRecordId()).thenReturn(Result.success(200L));
        when(financeFeignClient.createServiceFeeRecord(anyMap())).thenReturn(Result.success(null));
        when(performanceRecordService.getMinUnusedId()).thenReturn(300L);
        when(financeFeignClient.getMinUnusedCommissionRecordId()).thenReturn(Result.success(400L));
        when(financeFeignClient.createCommissionRecord(anyMap())).thenReturn(Result.success(null));

        // 执行测试
        contractService.bankLoan(1L);

        // 验证结果
        assertEquals((short) 7, contract.getStatus());
        assertEquals((short) 1, contract.getServiceFee2Paid());
        assertEquals(new BigDecimal("92000"), contract.getActualLoanAmount()); // 100000 - 5000 - 3000 = 92000
        verify(contractDao).updateById(contract);
        verify(financeFeignClient).getMinUnusedServiceFeeRecordId();
        verify(financeFeignClient).createServiceFeeRecord(anyMap());
        verify(performanceRecordService).getMinUnusedId();
        verify(performanceRecordService).save(any(PerformanceRecordEntity.class));
        verify(financeFeignClient).getMinUnusedCommissionRecordId();
        verify(financeFeignClient).createCommissionRecord(anyMap());
    }
```

- [ ] **Step 2: 更新 bankLoan_CalculatesCommissionCorrectly**

替换第 110-136 行为：

```java
    @Test
    void bankLoan_CalculatesCommissionCorrectly() {
        // 准备测试数据
        ContractEntity contract = new ContractEntity();
        contract.setId(1L);
        contract.setStatus((short) 5);
        contract.setSalesRepId(10L);
        contract.setContractAmount(new BigDecimal("100000"));
        contract.setServiceFee1(new BigDecimal("5000"));
        contract.setServiceFee2(new BigDecimal("3000"));
        contract.setDeptId(2L);
        contract.setZoneId(3L);

        when(contractDao.selectById(1L)).thenReturn(contract);
        when(financeFeignClient.getMinUnusedServiceFeeRecordId()).thenReturn(Result.success(200L));
        when(financeFeignClient.createServiceFeeRecord(anyMap())).thenReturn(Result.success(null));
        when(performanceRecordService.getMinUnusedId()).thenReturn(300L);
        when(financeFeignClient.getMinUnusedCommissionRecordId()).thenReturn(Result.success(400L));
        when(financeFeignClient.createCommissionRecord(anyMap())).thenReturn(Result.success(null));

        // 执行测试
        contractService.bankLoan(1L);

        // 验证提成金额计算是否正确：100000 * 0.015 = 1500
        BigDecimal expectedCommission = new BigDecimal("1500.00");
        ArgumentCaptor<PerformanceRecordEntity> captor = ArgumentCaptor.forClass(PerformanceRecordEntity.class);
        verify(performanceRecordService).save(captor.capture());
        assertEquals(0, expectedCommission.compareTo(captor.getValue().getCommissionAmount()));
    }
```

- [ ] **Step 3: 运行测试验证**

```bash
mvn test -pl sales -Dtest=ContractServiceImplTest -Dmaven.test.failure.ignore=false
```

Expected: Tests run: 7, Failures: 0

- [ ] **Step 4: 提交**

```bash
git add sales/src/main/java/com/dafuweng/sales/service/impl/ContractServiceImpl.java \
        sales/src/test/java/com/dafuweng/sales/service/impl/ContractServiceImplTest.java
git commit -m "feat: bankLoan 同步创建 commission_record 并使用北京时间"
```
