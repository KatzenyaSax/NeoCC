# Plan05: OpenFeign 远程调用 DTVO 实体类设计

**版本：** v1.0
**日期：** 2026-04-04
**目标：** 为 OpenFeign 远程调用设计并实现所有缺失的 DTVO 实体类

---

## 一、现状分析

### 1.1 已有资产

**SalesFeignClient（finance 模块）已定义但引用了不存在的类型：**

```java
// finance/src/main/java/com/dafuweng/finance/feign/SalesFeignClient.java
@FeignClient(name = "dafuweng-sales", contextId = "salesClient")
public interface SalesFeignClient {
    @PostMapping("/sales/internal/performances/create")
    Result<?> createPerformance(@RequestBody PerformanceCreateDTO dto);  // ❌ 不存在

    @PutMapping("/sales/internal/contracts/{id}/status")
    Result<?> updateContractStatus(@PathVariable Long id, @RequestParam Short status);  // ✅ 仅路径参数

    @GetMapping("/sales/internal/contracts/{id}")
    Result<ContractVO> getContract(@PathVariable Long id);  // ❌ 不存在
}
```

**缺失的 DTVO：**
1. `PerformanceCreateDTO` — finance → sales 创建业绩记录
2. `ContractVO` — sales → finance 返回合同详情

### 1.2 跨模块调用链路分析

根据 `implementDetails.md` 和 `dataDesign.md`，跨模块调用场景如下：

| 调用方向 | 场景 | 触发时机 |
|---------|------|---------|
| **finance → sales** | `createPerformance` 创建业绩记录 | 贷款终审通过后 |
| **finance → sales** | `updateContractStatus` 更新合同状态 | 审核流转时（发送金融部/银行反馈等） |
| **finance → sales** | `getContract` 查询合同详情 | 金融部接收合同后查看合同信息 |
| **sales → system** | 查询部门/战区信息 | 业绩归属需要部门/战区元数据 |
| **sales → auth** | 查询用户信息/权限码 | 业务操作需要用户身份 |

**当前 Phase 的核心：** finance → sales 的三个调用（其他跨模块调用待后续 Phase）

---

## 二、DTVO 设计原则

根据 implementDetails.md 规范：

1. **远程调用只传递必要字段** — 不传递整个 Entity，避免暴露不必要的内部字段
2. **使用 Lombok @Data** — 与项目现有 Entity 风格一致
3. **实现 Serializable** — OpenFeign 跨 JVM 传输需要序列化
4. **放在 common 模块** — 避免循环依赖，finance 和 sales 均可引用
5. **VO 用于返回，DTO 用于传入**

---

## 三、DTVO 详细设计

### 3.1 PerformanceCreateDTO（finance → sales）

**用途：** finance 终审通过后，调用 sales 创建业绩记录

**设计依据：** `PerformanceRecordEntity` 字段 + `implementDetails.md`"终审通过后触发"逻辑

```java
package com.dafuweng.common.entity.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class PerformanceCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 合同ID（来源：loan_audit.contract_id） */
    private Long contractId;

    /** 客户ID（来源：contract.customer_id） */
    private Long customerId;

    /** 销售代表ID（来源：contract.sales_rep_id） */
    private Long salesRepId;

    /** 部门ID（来源：contract.dept_id，金融部需要能查到这个） */
    private Long deptId;

    /** 战区ID（需要从 system 查到 sales_rep 对应的 zone_id，或由 finance 传递） */
    private Long zoneId;

    /** 合同金额（来源：contract.contract_amount） */
    private BigDecimal contractAmount;

    /** 提成比例（来源：loan_audit.approved_amount 上的金融产品 commission_rate） */
    private BigDecimal commissionRate;

    /** 提成金额（金融部计算得出：contract_amount × commission_rate） */
    private BigDecimal commissionAmount;

    /** 业绩状态（默认待计算：0 = pending） */
    private Short status;

    /** 业绩计算时间 */
    private Date calculateTime;

    /** 备注（如拒绝原因） */
    private String remark;
}
```

**字段来源对应关系：**

| PerformanceCreateDTO 字段 | 来源 | 说明 |
|--------------------------|------|------|
| contractId | loan_audit.contract_id | 必填，唯一索引 |
| customerId | contract.customer_id | 需从 contract 查询 |
| salesRepId | contract.sales_rep_id | 需从 contract 查询 |
| deptId | contract.dept_id | 需从 contract 查询 |
| zoneId | contract.zone_id | 需从 contract 查询，或由 finance 根据 dept 查到 zone |
| contractAmount | contract.contract_amount | 合同上签署的金额 |
| commissionRate | finance_product.commission_rate | 金融产品上配置的提成比例 |
| commissionAmount | finance 计算 | contract_amount × commission_rate |
| status | 固定 0 | pending，待确认 |
| calculateTime | 当前时间 | finance 计算时的时间戳 |
| remark | 可选 | 金融部可填写备注 |

### 3.2 ContractVO（sales → finance）

**用途：** sales 返回合同详情给 finance

**设计依据：** `ContractEntity` 字段，finance 只需要查看关键合同信息

```java
package com.dafuweng.common.entity.vo;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class ContractVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /** 合同编号 */
    private String contractNo;

    /** 客户ID */
    private Long customerId;

    /** 销售代表ID */
    private Long salesRepId;

    /** 部门ID */
    private Long deptId;

    /** 金融产品ID */
    private Long productId;

    /** 合同金额 */
    private BigDecimal contractAmount;

    /** 实际放款金额（银行放款后填充） */
    private BigDecimal actualLoanAmount;

    /** 服务费比例 */
    private BigDecimal serviceFeeRate;

    /** 首期服务费 */
    private BigDecimal serviceFee1;

    /** 二期服务费 */
    private BigDecimal serviceFee2;

    /** 首期服务费是否已付（0=未付, 1=已付） */
    private Short serviceFee1Paid;

    /** 二期服务费是否已付 */
    private Short serviceFee2Paid;

    /** 合同状态（1=草稿, 2=已签署, 3=已发送金融部...） */
    private Short status;

    /** 签署日期 */
    private Date signDate;

    /** 纸质合同编号 */
    private String paperContractNo;

    /** 贷款用途 */
    private String loanUse;

    /** 拒单原因 */
    private String rejectReason;

    /** 备注 */
    private String remark;
}
```

**字段选择说明：**
- 排除了 `guarantee_info`（担保证书，仅内部使用）
- 排除了 `financeSendTime/financeReceiveTime`（finance 自己写入，不需要回传）
- 排除了审计字段 `createdBy/createdAt/updatedBy/updatedAt/deleted/version`（VO 不暴露内部审计信息）
- 保留了 finance 审核流程中需要查看的所有字段

### 3.3 ContractStatusUpdateDTO（finance → sales）

**用途：** 更新合同状态（可选，OpenFeign GET 用 @RequestParam 也能传 Short）

**说明：** 当前 `SalesFeignClient.updateContractStatus` 使用 `@RequestParam Short status`，无需 DTO。但如果后续需要传递更多信息（如操作人、操作原因），应新增此 DTO。

```java
// 当前签名（无需 DTO）：
@PutMapping("/sales/internal/contracts/{id}/status")
Result<?> updateContractStatus(@PathVariable Long id, @RequestParam Short status);

// 如需扩展（建议后续）：
@Data
public class ContractStatusUpdateDTO implements Serializable {
    private Short status;
    private String reason;
    private Long operatorId;
}
```

**建议：** 当前先用 `@RequestParam`，后续 Phase 再扩展为此 DTO。

---

## 四、文件清单

### 4.1 新建 DTVO 文件

| 文件路径 | 类型 | 说明 |
|---------|------|------|
| `common/src/main/java/com/dafuweng/common/entity/dto/PerformanceCreateDTO.java` | DTO | finance→sales 创建业绩 |
| `common/src/main/java/com/dafuweng/common/entity/vo/ContractVO.java` | VO | sales→finance 返回合同详情 |

### 4.2 需同步更新的文件

| 文件路径 | 变更 |
|---------|------|
| `finance/src/main/java/com/dafuweng/finance/feign/SalesFeignClient.java` | 补充缺失的 import（待 DTO 创建后） |
| `sales/src/main/java/com/dafuweng/sales/controller/PerformanceRecordController.java` | 新增内部接口 `/internal/performances/create` |
| `sales/src/main/java/com/dafuweng/sales/controller/ContractController.java` | 新增内部接口 `/internal/contracts/{id}` 和 `/internal/contracts/{id}/status` |

---

## 五、内部接口设计（sales 模块）

### 5.1 创建业绩记录

**POST /sales/internal/performances/create**

```java
@RestController
@RequestMapping("/sales/internal")
public class InternalSalesController {

    @Autowired
    private PerformanceRecordService performanceRecordService;

    @PostMapping("/performances/create")
    public Result<?> createPerformance(@RequestBody PerformanceCreateDTO dto) {
        PerformanceRecordEntity entity = new PerformanceRecordEntity();
        entity.setContractId(dto.getContractId());
        entity.setSalesRepId(dto.getSalesRepId());
        entity.setDeptId(dto.getDeptId());
        entity.setZoneId(dto.getZoneId());
        entity.setContractAmount(dto.getContractAmount());
        entity.setCommissionRate(dto.getCommissionRate());
        entity.setCommissionAmount(dto.getCommissionAmount());
        entity.setStatus(dto.getStatus());
        entity.setCalculateTime(dto.getCalculateTime());
        entity.setRemark(dto.getRemark());
        // createdBy 等字段由 MetaObjectHandler 自动填充
        performanceRecordService.save(entity);
        return Result.success(entity);
    }
}
```

**幂等性保证：** `PerformanceRecordEntity.contract_id` 有唯一索引，重复创建会报 `DuplicateKeyException`，finance 侧需要捕获处理。

### 5.2 查询合同详情

**GET /sales/internal/contracts/{id}**

```java
@GetMapping("/contracts/{id}")
public Result<ContractVO> getContract(@PathVariable Long id) {
    ContractEntity entity = contractService.getById(id);
    if (entity == null) {
        return Result.error("合同不存在");
    }
    ContractVO vo = new ContractVO();
    vo.setId(entity.getId());
    vo.setContractNo(entity.getContractNo());
    vo.setCustomerId(entity.getCustomerId());
    vo.setSalesRepId(entity.getSalesRepId());
    vo.setDeptId(entity.getDeptId());
    vo.setProductId(entity.getProductId());
    vo.setContractAmount(entity.getContractAmount());
    vo.setActualLoanAmount(entity.getActualLoanAmount());
    vo.setServiceFeeRate(entity.getServiceFeeRate());
    vo.setServiceFee1(entity.getServiceFee1());
    vo.setServiceFee2(entity.getServiceFee2());
    vo.setServiceFee1Paid(entity.getServiceFee1Paid());
    vo.setServiceFee2Paid(entity.getServiceFee2Paid());
    vo.setStatus(entity.getStatus());
    vo.setSignDate(entity.getSignDate());
    vo.setPaperContractNo(entity.getPaperContractNo());
    vo.setLoanUse(entity.getLoanUse());
    vo.setRejectReason(entity.getRejectReason());
    vo.setRemark(entity.getRemark());
    return Result.success(vo);
}
```

### 5.3 更新合同状态

**PUT /sales/internal/contracts/{id}/status**

```java
@PutMapping("/contracts/{id}/status")
public Result<?> updateContractStatus(@PathVariable Long id, @RequestParam Short status) {
    ContractEntity entity = contractService.getById(id);
    if (entity == null) {
        return Result.error("合同不存在");
    }
    entity.setStatus(status);
    contractService.update(entity);
    return Result.success();
}
```

---

## 六、依赖关系

### 6.1 common 模块需要新增目录结构

```
common/src/main/java/com/dafuweng/common/entity/
├── dto/           ← 新建目录
│   └── PerformanceCreateDTO.java
└── vo/            ← 新建目录
    └── ContractVO.java
```

### 6.2 编译验证

```bash
mvn clean compile -pl common,sales,finance -DskipTests
```

---

## 七、实施步骤

### Step 1：创建目录结构

在 `common/src/main/java/com/dafuweng/common/entity/` 下新建 `dto/` 和 `vo/` 目录。

### Step 2：实现 PerformanceCreateDTO

创建 `dto/PerformanceCreateDTO.java`，字段设计见 3.1 节。

### Step 3：实现 ContractVO

创建 `vo/ContractVO.java`，字段设计见 3.2 节。

### Step 4：验证 common 模块编译

```bash
mvn clean compile -pl common -DskipTests
```

### Step 5：实现 sales 内部接口

在 `sales` 模块新建 `InternalSalesController`（或追加到现有 Controller），实现三个内部接口（见 5.1~5.3 节）。

### Step 6：验证 sales 模块编译

```bash
mvn clean compile -pl sales -am -DskipTests
```

### Step 7：验证 finance 模块编译

`salesFeignClient` 中的类型引用问题应已解决，验证：

```bash
mvn clean compile -pl finance -am -DskipTests
```

---

## 八、验收标准

1. `common` 模块新增 `PerformanceCreateDTO.java` 和 `ContractVO.java` 文件
2. `mvn clean compile -pl common,sales,finance -DskipTests` 全模块编译通过
3. `SalesFeignClient.java` 中的 `PerformanceCreateDTO` 和 `ContractVO` import 不再报错
4. sales 模块内部接口 `/sales/internal/performances/create`、`/sales/internal/contracts/{id}`、`/sales/internal/contracts/{id}/status` 已实现

---

## 九、后续扩展

### 9.1 待后续 Phase 实现的跨模块调用

| 调用方向 | 场景 | 需要的 DTVO |
|---------|------|------------|
| sales → system | 查询部门树 | `DeptQueryDTO` / `DeptTreeVO` |
| sales → system | 查询战区信息 | `ZoneQueryDTO` / `ZoneVO` |
| sales → auth | 查询用户信息 | `UserQueryDTO` / `UserVO` |
| sales → auth | 查询权限码 | `PermCodeQueryDTO` / `PermCodeVO` |
| finance → sales | 查询客户信息 | `CustomerQueryDTO` / `CustomerVO` |

### 9.2 ContractStatusUpdateDTO 扩展

当前 `updateContractStatus` 仅传 `status`，如果后续需要传递操作原因或操作人，建议扩展为：

```java
@Data
public class ContractStatusUpdateDTO implements Serializable {
    private Short status;
    private String reason;
    private Long operatorId;
}
```