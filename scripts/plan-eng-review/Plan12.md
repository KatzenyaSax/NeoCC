# Plan12: T03/T05/T06/T07/T08 单元测试开发规范

**版本：** v1.0
**日期：** 2026-04-14
**目标：** 为 T03（贷款审核）、T05（合同签署事件）、T06（事件监听）、T07（提成）、T08（服务费）编写规范、可执行的单元测试

---

## 一、技术选型

| 组件 | 选型 | 说明 |
|------|------|------|
| 测试框架 | JUnit 5 (`junit-jupiter`) | Spring Boot 3.x 默认 |
| Mock 框架 | Mockito (`mockito-junit-jupiter`) | Spring Boot 3.x 默认传递引入 |
| 数据库测试 | H2 内存数据库 (`h2`) | 替代 MySQL 做 DAO 层测试 |
| Spring 测试 | `@SpringBootTest` + `@ExtendWith(SpringExtension.class)` | Service 层测试 |
| 持久层 Mock | `org.mockito.InjectMocks` + `@Mock` | 纯 Java 测试，不启动容器 |

**依赖确认（各模块 pom.xml 已就绪）：**

```xml
<!-- finance / sales pom.xml 已通过 spring-boot-starter-test 传递引入 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

<!-- 如需 H2（DAO 层测试） -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

---

## 二、测试文件组织

```
src/
  main/java/.../
    service/impl/LoanAuditServiceImpl.java         ← 被测代码
    service/impl/ContractSignServiceImpl.java
    ...
  test/java/.../
    service/impl/LoanAuditServiceImplTest.java    ← 测试代码
    service/impl/ContractSignServiceImplTest.java
    mq/ContractSignedListenerTest.java
    ...
```

**规则：**
- 测试类与被测类同包（same package），置于 `src/test/java/`
- 文件命名：`{被测类名}Test.java`
- Controller 层不写单元测试（由后续 QA / E2E 覆盖）

---

## 三、Naming Convention

```
方法名_场景_预期结果

receive_Status1_Success           // receive 正常路径
receive_Status2_ThrowsException   // receive 非法状态
review_NotFound_ThrowsException   // review 记录不存在
```

---

## 四、T03 — LoanAuditServiceImpl 测试

**被测文件：** `finance/src/main/java/com/dafuweng/finance/service/impl/LoanAuditServiceImpl.java`

### 4.1 测试数据准备

```java
// 每个测试方法之前创建基础对象 via @BeforeEach
private LoanAuditEntity makeAudit(Short status) {
    LoanAuditEntity entity = new LoanAuditEntity();
    entity.setId(100L);
    entity.setContractId(10L);
    entity.setAuditStatus(status);
    entity.setDeleted((short) 0);
    return entity;
}
```

### 4.2 完整测试用例

```java
@ExtendWith(MockitoExtension.class)
class LoanAuditServiceImplTest {

    @Mock private LoanAuditDao loanAuditDao;
    @Mock private LoanAuditRecordService loanAuditRecordService;
    @Mock private SalesFeignClient salesFeignClient;
    @InjectMocks private LoanAuditServiceImpl loanAuditService;

    // ========== receive() ==========
    @Test
    void receive_Status1_Success() {
        LoanAuditEntity audit = makeAudit((short) 1);
        when(loanAuditDao.selectById(100L)).thenReturn(audit);
        when(loanAuditRecordService.save(any())).thenReturn(null);

        loanAuditService.receive(100L, 1L, "张三", "金融专员", "接收合同");

        assertEquals((short) 2, audit.getAuditStatus());
        verify(loanAuditDao).updateById(audit);
        verify(loanAuditRecordService).save(argThat(r ->
            r.getAction().equals("receive") && r.getOperatorId().equals(1L)
        ));
    }

    @Test
    void receive_Status2_ThrowsIllegalState() {
        LoanAuditEntity audit = makeAudit((short) 2);
        when(loanAuditDao.selectById(100L)).thenReturn(audit);

        assertThrows(IllegalStateException.class, () ->
            loanAuditService.receive(100L, 1L, "张三", "金融专员", ""));
    }

    @Test
    void receive_NotFound_ThrowsIllegalArgument() {
        when(loanAuditDao.selectById(100L)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () ->
            loanAuditService.receive(100L, 1L, "张三", "金融专员", ""));
    }

    // ========== review() ==========
    @Test
    void review_Status2_Success() {
        LoanAuditEntity audit = makeAudit((short) 2);
        when(loanAuditDao.selectById(100L)).thenReturn(audit);

        loanAuditService.review(100L, 1L, "李四", "金融专员", "初审通过");

        assertEquals((short) 3, audit.getAuditStatus());
        assertNotNull(audit.getAuditDate());
        verify(loanAuditDao).updateById(audit);
        verify(loanAuditRecordService).save(argThat(r -> r.getAction().equals("review")));
    }

    @Test
    void review_Status3_ThrowsIllegalState() {
        LoanAuditEntity audit = makeAudit((short) 3);
        when(loanAuditDao.selectById(100L)).thenReturn(audit);
        assertThrows(IllegalStateException.class, () ->
            loanAuditService.review(100L, 1L, "李四", "金融专员", ""));
    }

    // ========== submitBank() ==========
    @Test
    void submitBank_Status3_Success() {
        LoanAuditEntity audit = makeAudit((short) 3);
        when(loanAuditDao.selectById(100L)).thenReturn(audit);

        loanAuditService.submitBank(100L, 5L, 1L, "王五", "金融专员", "提交工行");

        assertEquals((short) 4, audit.getAuditStatus());
        assertEquals(5L, audit.getBankId());
        assertNotNull(audit.getBankApplyTime());
    }

    @Test
    void submitBank_Status2_ThrowsIllegalState() {
        LoanAuditEntity audit = makeAudit((short) 2);
        when(loanAuditDao.selectById(100L)).thenReturn(audit);
        assertThrows(IllegalStateException.class, () ->
            loanAuditService.submitBank(100L, 5L, 1L, "王五", "金融专员", ""));
    }

    // ========== bankResult() ==========
    @Test
    void bankResult_Approved_Status4_Success() {
        LoanAuditEntity audit = makeAudit((short) 4);
        when(loanAuditDao.selectById(100L)).thenReturn(audit);

        loanAuditService.bankResult(100L, true, "审批通过", 1L, "赵六", "银行", "银行通过");

        assertEquals((short) 6, audit.getAuditStatus());
        assertEquals("审批通过", audit.getBankFeedbackContent());
        assertNotNull(audit.getBankFeedbackTime());
    }

    @Test
    void bankResult_Rejected_Status4_Success() {
        LoanAuditEntity audit = makeAudit((short) 4);
        when(loanAuditDao.selectById(100L)).thenReturn(audit);

        loanAuditService.bankResult(100L, false, "条件不符", 1L, "赵六", "银行", "条件不符");

        assertEquals((short) 5, audit.getAuditStatus());
    }

    @Test
    void bankResult_Status3_ThrowsIllegalState() {
        LoanAuditEntity audit = makeAudit((short) 3);
        when(loanAuditDao.selectById(100L)).thenReturn(audit);
        assertThrows(IllegalStateException.class, () ->
            loanAuditService.bankResult(100L, true, "通过", 1L, "赵六", "银行", ""));
    }

    // ========== approve() ==========
    @Test
    void approve_Status6_Success_CreatesPerformanceAndUpdatesContract() {
        LoanAuditEntity audit = makeAudit((short) 6);
        audit.setContractId(20L);
        when(loanAuditDao.selectById(100L)).thenReturn(audit);

        ContractVO contract = new ContractVO();
        contract.setCustomerId(30L);
        contract.setSalesRepId(1L);
        contract.setDeptId(5L);
        contract.setContractAmount(new BigDecimal("100000"));
        when(salesFeignClient.getContract(20L)).thenReturn(Result.success(contract));
        when(salesFeignClient.createPerformance(any())).thenReturn(Result.success(null));
        when(salesFeignClient.updateContractStatus(20L, (short) 7)).thenReturn(Result.success(null));

        loanAuditService.approve(100L, 1L, "钱七", "金融总监", "终审通过",
            new BigDecimal("98000"), new BigDecimal("0.055"), new Date());

        // 验证状态更新
        assertEquals(new BigDecimal("98000"), audit.getActualLoanAmount());
        assertEquals("终审通过", audit.getAuditOpinion());

        // 验证 OpenFeign 调用
        verify(salesFeignClient).getContract(20L);
        verify(salesFeignClient).createPerformance(argThat(dto ->
            dto.getContractId().equals(20L) && dto.getCustomerId().equals(30L)
        ));
        verify(salesFeignClient).updateContractStatus(20L, (short) 7);
    }

    @Test
    void approve_Status4_ThrowsIllegalState() {
        LoanAuditEntity audit = makeAudit((short) 4);
        when(loanAuditDao.selectById(100L)).thenReturn(audit);
        assertThrows(IllegalStateException.class, () ->
            loanAuditService.approve(100L, 1L, "钱七", "金融总监", "",
                new BigDecimal("98000"), new BigDecimal("0.055"), new Date()));
    }

    @Test
    void approve_ContractResultNull_ThrowsRuntimeException() {
        LoanAuditEntity audit = makeAudit((short) 6);
        audit.setContractId(20L);
        when(loanAuditDao.selectById(100L)).thenReturn(audit);
        when(salesFeignClient.getContract(20L)).thenReturn(null);
        assertThrows(RuntimeException.class, () ->
            loanAuditService.approve(100L, 1L, "钱七", "金融总监", "",
                new BigDecimal("98000"), new BigDecimal("0.055"), new Date()));
    }

    // ========== reject() ==========
    @Test
    void reject_Status4_Success() {
        LoanAuditEntity audit = makeAudit((short) 4);
        when(loanAuditDao.selectById(100L)).thenReturn(audit);

        loanAuditService.reject(100L, 1L, "孙八", "金融总监", "不符合准入条件");

        assertEquals((short) 7, audit.getAuditStatus());
        verify(loanAuditRecordService).save(argThat(r -> r.getAction().equals("reject")));
    }

    @Test
    void reject_Status6_ThrowsIllegalState() {
        LoanAuditEntity audit = makeAudit((short) 6);
        when(loanAuditDao.selectById(100L)).thenReturn(audit);
        assertThrows(IllegalStateException.class, () ->
            loanAuditService.reject(100L, 1L, "孙八", "金融总监", ""));
    }
}
```

---

## 五、T05 — ContractSignServiceImpl 测试

**被测文件：** `sales/src/main/java/com/dafuweng/sales/service/impl/ContractSignServiceImpl.java`

```java
@ExtendWith(MockitoExtension.class)
class ContractSignServiceImplTest {

    @Mock private ContractDao contractDao;
    @Mock private RabbitTemplate rabbitTemplate;
    @InjectMocks private ContractSignServiceImpl contractSignService;

    @Test
    void sign_Status1_Success_PublishesEvent() {
        ContractEntity contract = new ContractEntity();
        contract.setId(50L);
        contract.setStatus((short) 1);
        contract.setCustomerId(100L);
        contract.setSalesRepId(10L);
        contract.setDeptId(5L);
        contract.setContractAmount(new BigDecimal("50000"));
        when(contractDao.selectById(50L)).thenReturn(contract);

        contractSignService.sign(50L);

        // 验证状态更新
        assertEquals((short) 2, contract.getStatus());
        assertNotNull(contract.getSignDate());
        verify(contractDao).updateById(contract);

        // 验证 MQ 事件发送
        verify(rabbitTemplate).convertAndSend(
            eq("sales.exchange"),
            eq("contract.signed"),
            argThat((ArgumentCaptor<ContractSignedEvent> captor) -> {
                ContractSignedEvent event = captor.getValue();
                return event.getContractId().equals(50L)
                    && event.getCustomerId().equals(100L)
                    && event.getSalesRepId().equals(10L);
            })
        );
    }

    @Test
    void sign_ContractNotFound_ThrowsIllegalArgument() {
        when(contractDao.selectById(999L)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> contractSignService.sign(999L));
    }

    @Test
    void sign_Status2_ThrowsIllegalState() {
        ContractEntity contract = new ContractEntity();
        contract.setId(50L);
        contract.setStatus((short) 2);  // 已签署状态
        when(contractDao.selectById(50L)).thenReturn(contract);
        assertThrows(IllegalStateException.class, () -> contractSignService.sign(50L));
    }

    @Test
    void sign_Status5_ThrowsIllegalState() {
        ContractEntity contract = new ContractEntity();
        contract.setId(50L);
        contract.setStatus((short) 5);  // 其他非初始状态
        when(contractDao.selectById(50L)).thenReturn(contract);
        assertThrows(IllegalStateException.class, () -> contractSignService.sign(50L));
    }
}
```

---

## 六、T06 — ContractSignedListener 测试

**被测文件：** `finance/src/main/java/com/dafuweng/finance/mq/ContractSignedListener.java`

```java
@ExtendWith(MockitoExtension.class)
class ContractSignedListenerTest {

    @Mock private LoanAuditService loanAuditService;
    @Mock private LoanAuditRecordService loanAuditRecordService;
    @InjectMocks private ContractSignedListener listener;

    @Test
    void onContractSigned_NewContract_CreatesLoanAuditAndRecord() {
        // 准备事件
        ContractSignedEvent event = new ContractSignedEvent();
        event.setContractId(60L);
        event.setCustomerId(100L);
        event.setSalesRepId(10L);
        event.setDeptId(5L);
        event.setContractAmount(new BigDecimal("80000"));

        // 无已有记录
        when(loanAuditService.getByContractId(60L)).thenReturn(null);
        when(loanAuditService.save(any())).thenAnswer(inv -> {
            LoanAuditEntity arg = inv.getArgument(0);
            arg.setId(200L);  // 模拟 DB 自增 ID
            return arg;
        });

        listener.onContractSigned(event);

        // 验证 LoanAudit 创建
        verify(loanAuditService).save(argThat(audit ->
            audit.getContractId().equals(60L) && audit.getAuditStatus().equals((short) 1)
        ));

        // 验证 LoanAuditRecord 创建（幂等检查）
        verify(loanAuditRecordService).save(argThat(record ->
            record.getLoanAuditId().equals(200L)
            && record.getAction().equals("receive")
            && record.getOperatorId().equals(0L)
            && record.getOperatorName().equals("系统")
        ));
    }

    @Test
    void onContractSigned_AlreadyExists_Skips() {
        ContractSignedEvent event = new ContractSignedEvent();
        event.setContractId(60L);

        LoanAuditEntity existing = new LoanAuditEntity();
        existing.setId(300L);
        when(loanAuditService.getByContractId(60L)).thenReturn(existing);

        listener.onContractSigned(event);

        // 不应创建新记录
        verify(loanAuditService, never()).save(any());
        verify(loanAuditRecordService, never()).save(any());
    }
}
```

---

## 七、T07 — CommissionRecordServiceImpl 测试

**被测文件：** `finance/src/main/java/com/dafuweng/finance/service/impl/CommissionRecordServiceImpl.java`

```java
@ExtendWith(MockitoExtension.class)
class CommissionRecordServiceImplTest {

    @Mock private CommissionRecordDao commissionRecordDao;
    @InjectMocks private CommissionRecordServiceImpl commissionRecordService;

    private CommissionRecordEntity makeRecord(Short status) {
        CommissionRecordEntity r = new CommissionRecordEntity();
        r.setId(50L);
        r.setSalesRepId(10L);
        r.setStatus(status);
        return r;
    }

    // ========== confirm() ==========
    @Test
    void confirm_Status0_Success() {
        CommissionRecordEntity record = makeRecord((short) 0);
        when(commissionRecordDao.selectById(50L)).thenReturn(record);

        commissionRecordService.confirm(50L);

        assertEquals((short) 1, record.getStatus());
        assertNotNull(record.getConfirmTime());
        verify(commissionRecordDao).updateById(record);
    }

    @Test
    void confirm_Status1_ThrowsIllegalState() {
        CommissionRecordEntity record = makeRecord((short) 1);
        when(commissionRecordDao.selectById(50L)).thenReturn(record);
        assertThrows(IllegalStateException.class, () -> commissionRecordService.confirm(50L));
    }

    @Test
    void confirm_Status2_ThrowsIllegalState() {
        CommissionRecordEntity record = makeRecord((short) 2);
        when(commissionRecordDao.selectById(50L)).thenReturn(record);
        assertThrows(IllegalStateException.class, () -> commissionRecordService.confirm(50L));
    }

    @Test
    void confirm_NotFound_ThrowsIllegalArgument() {
        when(commissionRecordDao.selectById(999L)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> commissionRecordService.confirm(999L));
    }

    // ========== grant() ==========
    @Test
    void grant_Status1_Success() {
        CommissionRecordEntity record = makeRecord((short) 1);
        when(commissionRecordDao.selectById(50L)).thenReturn(record);

        commissionRecordService.grant(50L, "6222XXXXXXXXXX", "首笔提成发放");

        assertEquals((short) 2, record.getStatus());
        assertNotNull(record.getGrantTime());
        assertEquals("6222XXXXXXXXXX", record.getGrantAccount());
        assertEquals("首笔提成发放", record.getRemark());
        verify(commissionRecordDao).updateById(record);
    }

    @Test
    void grant_Status0_ThrowsIllegalState() {
        CommissionRecordEntity record = makeRecord((short) 0);
        when(commissionRecordDao.selectById(50L)).thenReturn(record);
        assertThrows(IllegalStateException.class, () ->
            commissionRecordService.grant(50L, "账户", ""));
    }

    @Test
    void grant_Status2_ThrowsIllegalState() {
        CommissionRecordEntity record = makeRecord((short) 2);
        when(commissionRecordDao.selectById(50L)).thenReturn(record);
        assertThrows(IllegalStateException.class, () ->
            commissionRecordService.grant(50L, "账户", ""));
    }

    @Test
    void grant_NotFound_ThrowsIllegalArgument() {
        when(commissionRecordDao.selectById(999L)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () ->
            commissionRecordService.grant(999L, "账户", ""));
    }
}
```

---

## 八、T08 — ServiceFeeRecordServiceImpl 测试

**被测文件：** `finance/src/main/java/com/dafuweng/finance/service/impl/ServiceFeeRecordServiceImpl.java`

```java
@ExtendWith(MockitoExtension.class)
class ServiceFeeRecordServiceImplTest {

    @Mock private ServiceFeeRecordDao serviceFeeRecordDao;
    @Mock private SalesFeignClient salesFeignClient;
    @InjectMocks private ServiceFeeRecordServiceImpl serviceFeeRecordService;

    private ServiceFeeRecordEntity makeRecord(Short feeType, Short paymentStatus) {
        ServiceFeeRecordEntity r = new ServiceFeeRecordEntity();
        r.setId(50L);
        r.setContractId(20L);
        r.setFeeType(feeType);
        r.setPaymentStatus(paymentStatus);
        return r;
    }

    @Test
    void confirmPay_Status0_Success_CallsFeign() {
        ServiceFeeRecordEntity record = makeRecord((short) 1, (short) 0);
        when(serviceFeeRecordDao.selectById(50L)).thenReturn(record);
        when(salesFeignClient.updateServiceFeePaid(20L, (short) 1)).thenReturn(Result.success(null));

        serviceFeeRecordService.confirmPay(50L, "银行转账", "6222XXXXXXXX", "RCP2026001", "首期到账");

        assertEquals((short) 1, record.getPaymentStatus());
        assertNotNull(record.getPaymentDate());
        assertEquals("银行转账", record.getPaymentMethod());
        assertEquals("6222XXXXXXXX", record.getPaymentAccount());
        assertEquals("RCP2026001", record.getReceiptNo());
        verify(salesFeignClient).updateServiceFeePaid(20L, (short) 1);
    }

    @Test
    void confirmPay_Status1_ThrowsIllegalState() {
        ServiceFeeRecordEntity record = makeRecord((short) 1, (short) 1);
        when(serviceFeeRecordDao.selectById(50L)).thenReturn(record);
        assertThrows(IllegalStateException.class, () ->
            serviceFeeRecordService.confirmPay(50L, "银行转账", "账户", "单号", ""));
    }

    @Test
    void confirmPay_NotFound_ThrowsIllegalArgument() {
        when(serviceFeeRecordDao.selectById(999L)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () ->
            serviceFeeRecordService.confirmPay(999L, "银行转账", "账户", "单号", ""));
    }

    @Test
    void confirmPay_FeignCallFails_ThrowsRuntimeException() {
        ServiceFeeRecordEntity record = makeRecord((short) 2, (short) 0);
        when(serviceFeeRecordDao.selectById(50L)).thenReturn(record);
        when(salesFeignClient.updateServiceFeePaid(20L, (short) 2)).thenReturn(Result.error("网络异常"));

        assertThrows(RuntimeException.class, () ->
            serviceFeeRecordService.confirmPay(50L, "银行转账", "账户", "单号", ""));
    }

    @Test
    void confirmPay_FeeType2_CallsCorrectFeign() {
        ServiceFeeRecordEntity record = makeRecord((short) 2, (short) 0);
        when(serviceFeeRecordDao.selectById(50L)).thenReturn(record);
        when(salesFeignClient.updateServiceFeePaid(20L, (short) 2)).thenReturn(Result.success(null));

        serviceFeeRecordService.confirmPay(50L, "银行转账", "账户", "单号", "");

        verify(salesFeignClient).updateServiceFeePaid(20L, (short) 2);
    }
}
```

---

## 九、覆盖率目标

| 任务 | 分支覆盖要求 |
|------|------------|
| T03 LoanAuditServiceImpl | 100%（每个状态转换路径 + 异常路径） |
| T05 ContractSignServiceImpl | 100%（正常 + 3 个异常分支） |
| T06 ContractSignedListener | 100%（新建 + 幂等跳过） |
| T07 CommissionRecordServiceImpl | 100%（confirm/grant 各 4 个分支） |
| T08 ServiceFeeRecordServiceImpl | 100%（正常 + Feign 失败 + 状态异常） |

---

## 十、执行命令

```bash
# 单独运行某模块测试
cd finance && mvn test -Dtest=LoanAuditServiceImplTest

# 运行所有 finance 测试
cd finance && mvn test

# 运行所有 sales 测试
cd sales && mvn test

# 从根目录运行特定测试
mvn test -pl finance -Dtest=LoanAuditServiceImplTest,CommissionRecordServiceImplTest,ServiceFeeRecordServiceImplTest
mvn test -pl sales -Dtest=ContractSignServiceImplTest
```

---

## 十一、常见 Mock 模式

```java
// 1. 模拟 DAO 返回 null（记录不存在）
when(loanAuditDao.selectById(999L)).thenReturn(null);

// 2. 模拟实体修改（verify 更新行为）
when(loanAuditDao.selectById(100L)).thenReturn(audit);
loanAuditService.receive(100L, 1L, "张三", "专员", "备注");
verify(loanAuditDao).updateById(audit);

// 3. 捕获 MQ 发送内容
ArgumentCaptor<ContractSignedEvent> captor = ArgumentCaptor.forClass(ContractSignedEvent.class);
verify(rabbitTemplate).convertAndSend(anyString(), anyString(), captor.capture());
assertEquals(50L, captor.getValue().getContractId());

// 4. 模拟 Feign 返回失败
when(salesFeignClient.getContract(20L)).thenReturn(null);
when(salesFeignClient.getContract(20L)).thenReturn(Result.error("合同不存在"));

// 5. argThat 精细断言
verify(loanAuditRecordService).save(argThat(record ->
    "receive".equals(record.getAction()) && record.getOperatorId() == 0
));
```

---

## 十二、NOT in Scope

- Controller 层测试（由 QA / Postman / E2E 覆盖）
- DAO 层 MyBatis XML SQL 测试（H2 可做简单验证，但复杂联查不测）
- RabbitMQ 集成测试（需要本地 RabbitMQ 实例，属于 E2E 范畴）
- T01/T02 已有测试（BCryptPasswordEncoder 和 JwtAuthenticationFilter 测试框架已验证）

---

## 十三、依赖关系图

```
T03: LoanAuditServiceImpl
  └── Mock: LoanAuditDao, LoanAuditRecordService, SalesFeignClient

T05: ContractSignServiceImpl
  └── Mock: ContractDao, RabbitTemplate

T06: ContractSignedListener
  └── Mock: LoanAuditService, LoanAuditRecordService

T07: CommissionRecordServiceImpl
  └── Mock: CommissionRecordDao

T08: ServiceFeeRecordServiceImpl
  └── Mock: ServiceFeeRecordDao, SalesFeignClient
```

无外部数据库依赖，全部使用 Mockito Mock。