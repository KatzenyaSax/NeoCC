# Plan06: OpenFeign 跨服务调用完整实施

**版本：** v1.0
**日期：** 2026-04-04
**目标：** 实现所有 OpenFeign 跨服务调用，包括 Feign 客户端、对应内部接口、服务调用触发逻辑

---

## 一、现状分析

### 1.1 已有资产

| 资产 | 状态 | 说明 |
|------|------|------|
| `SalesFeignClient` | ✅ 已定义 | finance 模块，3个方法，引用了 DTO/VO |
| `PerformanceCreateDTO` | ✅ 已创建 | Plan05 产出，位于 `common/entity/dto/` |
| `ContractVO` | ✅ 已创建 | Plan05 产出，位于 `common/entity/vo/` |
| `FinanceApplication` | ✅ 有 @EnableFeignClients | `@EnableFeignClients(basePackages = "com.dafuweng.finance.feign")` |
| finance/pom.xml | ✅ 有 openfeign 依赖 | `spring-cloud-starter-openfeign` + `spring-cloud-starter-loadbalancer` |

### 1.2 缺失资产

| 缺失项 | 说明 |
|--------|------|
| sales 内部接口 | `/sales/internal/...` 三个接口全部不存在 |
| InternalSalesController | 承载 sales 内部接口的 Controller |
| LoanAuditServiceImpl 调用 | 审核终审通过后未触发 Feign 调用 |
| Feign 编译验证 | DTO/VO 存在但 sales 内部接口不存在，Feign 调用必定失败 |
| gateway → auth Feign | gateway 需要调用 auth 验证 Token，当前无 Feign 客户端 |
| sales → auth Feign | 业务操作需要用户身份/权限，当前无 Feign 客户端 |
| sales → system Feign | 业绩归属需要部门/战区元数据，当前无 Feign 客户端 |

### 1.3 implementDetails.md 中明确描述的 Feign 接口

根据 `implementDetails.md` 第八章：

```
finance → sales（唯一明确描述的 Feign 客户端）:
  SalesFeignClient:
    POST /sales/internal/performances/create   创建业绩记录
    PUT  /sales/internal/contracts/{id}/status  修改合同状态
    GET  /sales/internal/contracts/{id}        查询合同详情
```

gateway → auth（第7.2节描述）：
```
验证 token 有效性（调用 auth-service 的 OpenFeign 接口）
```

### 1.4 本次实施范围

1. **核心：** finance → sales 的 `SalesFeignClient` 完整闭环（内部接口 + Feign 调用触发）
2. **扩展 A：** gateway → auth Feign 客户端（Token 验证）
3. **扩展 B：** sales → auth Feign 客户端（用户/权限查询）
4. **扩展 C：** sales → system Feign 客户端（部门/战区查询）

---

## 二、finance → sales（核心，implementDetails.md 明确要求）

### 2.1 需要创建的 sales 内部接口

#### 接口清单

| 方法 | 路径 | 功能 | 对应 Feign 方法 |
|------|------|------|----------------|
| POST | `/sales/internal/performances/create` | 创建业绩记录 | `createPerformance(dto)` |
| GET | `/sales/internal/contracts/{id}` | 查询合同详情 | `getContract(id)` |
| PUT | `/sales/internal/contracts/{id}/status` | 更新合同状态 | `updateContractStatus(id, status)` |

#### InternalSalesController

**路径：** `sales/src/main/java/com/dafuweng/sales/controller/InternalSalesController.java`

```java
package com.dafuweng.sales.controller;

import com.dafuweng.common.entity.Result;
import com.dafuweng.common.entity.dto.PerformanceCreateDTO;
import com.dafuweng.common.entity.vo.ContractVO;
import com.dafuweng.sales.entity.ContractEntity;
import com.dafuweng.sales.entity.PerformanceRecordEntity;
import com.dafuweng.sales.service.ContractService;
import com.dafuweng.sales.service.PerformanceRecordService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sales/internal")
public class InternalSalesController {

    @Autowired
    private PerformanceRecordService performanceRecordService;

    @Autowired
    private ContractService contractService;

    /**
     * POST /sales/internal/performances/create
     * 创建业绩记录（供 finance 服务调用）
     *
     * 幂等性：contract_id 有唯一索引，重复插入会抛 DuplicateKeyException
     */
    @PostMapping("/performances/create")
    public Result<?> createPerformance(@RequestBody PerformanceCreateDTO dto) {
        // 检查是否已存在（幂等保护）
        PerformanceRecordEntity existing = performanceRecordService.getByContractId(dto.getContractId());
        if (existing != null) {
            return Result.error(400, "该合同已创建业绩记录，请勿重复提交");
        }

        PerformanceRecordEntity entity = new PerformanceRecordEntity();
        entity.setContractId(dto.getContractId());
        entity.setSalesRepId(dto.getSalesRepId());
        entity.setDeptId(dto.getDeptId());
        entity.setZoneId(dto.getZoneId());
        entity.setContractAmount(dto.getContractAmount());
        entity.setCommissionRate(dto.getCommissionRate());
        entity.setCommissionAmount(dto.getCommissionAmount());
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : 0);  // 默认待计算
        entity.setCalculateTime(dto.getCalculateTime() != null ? dto.getCalculateTime() : new java.util.Date());
        entity.setRemark(dto.getRemark());

        try {
            performanceRecordService.save(entity);
            return Result.success(entity);
        } catch (DuplicateKeyException e) {
            return Result.error(400, "该合同已创建业绩记录，幂等冲突");
        }
    }

    /**
     * GET /sales/internal/contracts/{id}
     * 查询合同详情（供 finance 服务调用）
     */
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

    /**
     * PUT /sales/internal/contracts/{id}/status
     * 更新合同状态（供 finance 服务调用）
     *
     * finance 审核流程中：
     * - 合同签署后 status=2（已签署）→ 发送金融部后 status=4（已发送金融部）
     * - 银行放款后 status=7（已放款）
     */
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
}
```

#### PerformanceRecordService 新增方法

在 `sales/src/main/java/com/dafuweng/sales/service/PerformanceRecordService.java` 接口中新增：

```java
/** 根据合同ID查询业绩记录（幂等保护用） */
PerformanceRecordEntity getByContractId(Long contractId);
```

在 `sales/src/main/java/com/dafuweng/sales/service/impl/PerformanceRecordServiceImpl.java` 中实现：

```java
@Override
public PerformanceRecordEntity getByContractId(Long contractId) {
    LambdaQueryWrapper<PerformanceRecordEntity> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(PerformanceRecordEntity::getContractId, contractId);
    wrapper.eq(PerformanceRecordEntity::getDeleted, 0);
    return performanceRecordDao.selectOne(wrapper);
}
```

在 `sales/src/main/java/com/dafuweng/sales/dao/PerformanceRecordDao.java` 中新增：

```java
PerformanceRecordEntity selectOne(Wrapper<PerformanceRecordEntity> wrapper);
```

对应 XML（`sales/src/main/resources/sales/mapper/PerformanceRecordDao.xml`）新增：

```xml
<select id="selectOne" resultMap="BaseResultMap">
    SELECT * FROM performance_record
    WHERE contract_id = #{contractId} AND deleted = 0
    LIMIT 1
</select>
```

### 2.2 LoanAuditServiceImpl 调用 Feign

#### 2.2.1 注入 SalesFeignClient

修改 `finance/src/main/java/com/dafuweng/finance/service/impl/LoanAuditServiceImpl.java`：

```java
@Service
public class LoanAuditServiceImpl implements LoanAuditService {

    @Autowired
    private LoanAuditDao loanAuditDao;

    @Autowired
    private SalesFeignClient salesFeignClient;  // 新增

    // ... 现有方法不变 ...
}
```

#### 2.2.2 终审通过触发（approve 方法）

在 `LoanAuditServiceImpl` 中新增或修改 `approve` 方法：

```java
/**
 * 终审通过
 * 触发：1. 更新 loan_audit 状态  2. 更新 contract 状态  3. 创建业绩记录  4. 创建提成记录
 */
@Transactional
public void approve(Long loanAuditId, BigDecimal actualLoanAmount,
        BigDecimal actualInterestRate, Date loanGrantedDate) {
    LoanAuditEntity loanAudit = loanAuditDao.selectById(loanAuditId);
    if (loanAudit == null) {
        throw new IllegalArgumentException("审核记录不存在");
    }

    // 1. 更新 loan_audit 状态
    loanAudit.setAuditStatus(5);  // 终审通过
    loanAudit.setActualLoanAmount(actualLoanAmount);
    loanAudit.setActualInterestRate(actualInterestRate);
    loanAudit.setLoanGrantedDate(loanGrantedDate);
    loanAuditDao.updateById(loanAudit);

    // 2. 更新合同状态 → 已放款（status=7）
    salesFeignClient.updateContractStatus(loanAudit.getContractId(), (short) 7);

    // 3. 计算提成金额（需要查金融产品的 commission_rate）
    //    此处需要先查 finance_product 获知 commission_rate
    //    为简化，假设 finance 层已计算好 commission_amount 传入
    //    实际应在 LoanAuditEntity 中增加 commission_amount 字段，或通过 ProductFeignClient 查询

    // 4. 创建业绩记录（异步建议，实际同步调用）
    //    注意：这里需要 deptId/zoneId，需要先调用 getContract 查到合同完整信息
    Result<ContractVO> contractResult = salesFeignClient.getContract(loanAudit.getContractId());
    if (contractResult == null || contractResult.getData() == null) {
        throw new RuntimeException("无法获取合同信息，无法创建业绩");
    }
    ContractVO contract = contractResult.getData();

    PerformanceCreateDTO perfDto = new PerformanceCreateDTO();
    perfDto.setContractId(loanAudit.getContractId());
    perfDto.setCustomerId(contract.getCustomerId());
    perfDto.setSalesRepId(contract.getSalesRepId());
    perfDto.setDeptId(contract.getDeptId());
    // zoneId 需要从 system 查到或合同表有 zone_id 字段，此处传 null 或从 contract 扩展
    perfDto.setZoneId(null);
    perfDto.setContractAmount(contract.getContractAmount());
    // commissionRate 和 commissionAmount 需要从产品查到，此处传 0 或补充 ProductFeignClient
    perfDto.setCommissionRate(BigDecimal.ZERO);
    perfDto.setCommissionAmount(BigDecimal.ZERO);
    perfDto.setStatus((short) 0);  // 待计算
    perfDto.setCalculateTime(new Date());

    Result<?> perfResult = salesFeignClient.createPerformance(perfDto);
    if (perfResult == null || perfResult.getCode() != 200) {
        throw new RuntimeException("创建业绩记录失败: " + (perfResult != null ? perfResult.getMessage() : "未知错误"));
    }
}
```

**注：** 上述 approve 方法的完整实现需要 `zoneId` 和 `commissionRate`，这些字段需要额外的 Feign 调用（查 system 或查 finance_product）。本 Plan 先实现核心调用链路，业绩创建时 zoneId 和 commissionRate 传 0，后续 Phase 补充完善。

#### 2.2.3 审核状态流转触发

| 审核动作 | 触发 Feign 调用 | 参数 |
|---------|---------------|------|
| 发送至金融部 | `updateContractStatus(id, 4)` | contract_id, status=4 |
| 银行反馈通过 | `updateContractStatus(id, 5)` | contract_id, status=5 |
| 终审通过 | `updateContractStatus(id, 7)` + `createPerformance` | contract_id, status=7 + perfDto |

---

## 三、gateway → auth（Token 验证，implementDetails.md 第7.2节要求）

### 3.1 背景分析

`implementDetails.md` 第7.2节描述：

> "验证 token 有效性（调用 auth-service 的 OpenFeign 接口）"

当前 auth 模块登录机制是**明文密码比对**（plain text comparison），没有实现 JWT Token。gateway 无法通过 Feign 调用验证 JWT Token。

**两种可行方案：**

| 方案 | 描述 | 前提 |
|------|------|------|
| 方案 A：走 Gateway 直连 auth HTTP | gateway 通过 RestTemplate/WebClient 调用 auth 接口验证 | auth 接口支持通过 token 查用户 |
| 方案 B：待 auth 实现 JWT 后走 Feign | auth 实现 JWT 签发/验证后，gateway 通过 Feign 调用 auth | auth 完成 JWT 改造 |

**推荐方案 A**（本次实施），因为：
- auth 已有 `/api/sysUser/{id}` 接口可查用户信息
- 不依赖 auth 实现 JWT
- gateway 和 auth 在同一网络，可直连 HTTP

### 3.2 AuthFeignClient（gateway 模块）

**路径：** `gateway/src/main/java/com/dafuweng/gateway/feign/AuthFeignClient.java`

```java
package com.dafuweng.gateway.feign;

import com.dafuweng.common.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "dafuweng-auth", contextId = "authClient")
public interface AuthFeignClient {

    /**
     * 根据用户ID查询用户信息（验证 Token 对应的用户是否存在且有效）
     * gateway 在 Token 校验时调用
     */
    @GetMapping("/auth/api/sysUser/{id}")
    Result<?> getUserById(@PathVariable Long id);

    /**
     * 根据用户名查询用户信息（用于 Token 解析后二次验证）
     */
    @GetMapping("/auth/api/sysUser/username/{username}")
    Result<?> getUserByUsername(@PathVariable String username);

    /**
     * 查询用户的角色ID列表
     */
    @GetMapping("/auth/api/sysUser/{id}/roles")
    Result<?> getRoleIds(@PathVariable Long id);

    /**
     * 查询用户的权限码列表
     */
    @GetMapping("/auth/api/sysUser/{id}/permCodes")
    Result<?> getPermCodes(@PathVariable Long id);
}
```

**注：** auth 的 `SysUserController` 路径是 `/api/sysUser/...`，但 gateway 需要通过 `/auth/...` 前缀路由。因此需要在 gateway 的 application.yml 中添加 `/auth/**` 路由（已有，StripPrefix=1 会去掉 `/auth` 前缀）。

### 3.3 AuthFilter 中的 Token 校验逻辑

在 `gateway/src/main/java/com/dafuweng/gateway/filter/AuthFilter.java` 中：

```java
@Autowired
private AuthFeignClient authFeignClient;

// 在 filter 中：
// 1. 从 Header 提取 Authorization: Bearer <token>
// 2. 解析 token 获取 userId（当前阶段 token = userId 的字符串形式）
// 3. 调用 authFeignClient.getUserById(userId) 验证用户存在且有效
// 4. 验证通过后，将 userId/username/roles 放入 downstream header
```

**注：** 当前 auth 没有实现 JWT，token 的格式暂时为明文 userId。后续 auth 实现 JWT 后，此处解析逻辑需要对应修改。

---

## 四、sales → auth（用户/权限查询）

### 4.1 AuthFeignClient（sales 模块）

**路径：** `sales/src/main/java/com/dafuweng/sales/feign/AuthFeignClient.java`

```java
package com.dafuweng.sales.feign;

import com.dafuweng.common.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "dafuweng-auth", contextId = "authClientForSales")
public interface AuthFeignClient {

    /**
     * 根据用户ID查询用户信息
     */
    @GetMapping("/auth/api/sysUser/{id}")
    Result<?> getUserById(@PathVariable Long id);

    /**
     * 查询用户的权限码列表（业务操作前鉴权）
     */
    @GetMapping("/auth/api/sysUser/{id}/permCodes")
    Result<List<String>> getPermCodes(@PathVariable Long id);
}
```

**注：** sales 的 controller 路径前缀是 `/api/sysUser`，gateway 路由到 auth 时 StripPrefix=1 变为 `/auth/api/sysUser`，与 FeignClient 中的路径一致。

### 4.2 使用场景

| 场景 | 调用 Feign | 用途 |
|------|-----------|------|
| 业绩归属验证 | `getUserById` | 验证 salesRepId 对应的用户是否存在 |
| 操作权限校验 | `getPermCodes` | 业务操作前确认用户有对应权限码 |

### 4.3 sales Application 需添加 @EnableFeignClients

修改 `sales/src/main/java/com/dafuweng/sales/SalesApplication.java`：

```java
@SpringBootApplication(scanBasePackages = "com.dafuweng")
@MapperScan("com.dafuweng.sales.dao")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.dafuweng.sales.feign")  // 新增
public class SalesApplication {
    public static void main(String[] args) {
        SpringApplication.run(SalesApplication.class, args);
    }
}
```

---

## 五、sales → system（部门/战区查询）

### 5.1 SystemFeignClient（sales 模块）

**路径：** `sales/src/main/java/com/dafuweng/sales/feign/SystemFeignClient.java`

```java
package com.dafuweng.sales.feign;

import com.dafuweng.common.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "dafuweng-system", contextId = "systemClientForSales")
public interface SystemFeignClient {

    /**
     * 根据部门ID查询部门详情
     * sales 在创建业绩记录时需要 deptId 对应的 zoneId
     */
    @GetMapping("/system/api/sysDepartment/{id}")
    Result<?> getDepartmentById(@PathVariable Long id);

    /**
     * 根据战区ID查询战区详情
     */
    @GetMapping("/system/api/sysZone/{id}")
    Result<?> getZoneById(@PathVariable Long id);
}
```

### 5.2 使用场景

| 场景 | 调用 Feign | 用途 |
|------|-----------|------|
| 业绩归属 zoneId | `getDepartmentById` | 从 deptId 查到对应的 zoneId，完善业绩记录 |

### 5.3 sales Application 需更新 @EnableFeignClients

`sales` 的 `@EnableFeignClients` 已添加（见 4.3 节），只需在 `basePackages` 中加入 `com.dafuweng.sales.feign` 即可（多包用逗号分隔）。

---

## 六、文件变更总清单

### 6.1 新建文件

| 文件路径 | 类型 | 说明 |
|---------|------|------|
| `sales/src/main/java/com/dafuweng/sales/controller/InternalSalesController.java` | Controller | 承载3个内部接口 |
| `sales/src/main/java/com/dafuweng/sales/feign/AuthFeignClient.java` | FeignClient | sales→auth |
| `sales/src/main/java/com/dafuweng/sales/feign/SystemFeignClient.java` | FeignClient | sales→system |
| `gateway/src/main/java/com/dafuweng/gateway/feign/AuthFeignClient.java` | FeignClient | gateway→auth |
| `sales/src/main/resources/sales/mapper/PerformanceRecordDao.xml` | Mapper XML | selectOne 查询 |

### 6.2 修改文件

| 文件路径 | 变更 |
|---------|------|
| `sales/src/main/java/com/dafuweng/sales/service/PerformanceRecordService.java` | 新增 `getByContractId()` |
| `sales/src/main/java/com/dafuweng/sales/service/impl/PerformanceRecordServiceImpl.java` | 实现 `getByContractId()` |
| `sales/src/main/java/com/dafuweng/sales/dao/PerformanceRecordDao.java` | 新增 `selectOne` 方法 |
| `sales/src/main/java/com/dafuweng/sales/SalesApplication.java` | 添加 `@EnableFeignClients` |
| `finance/src/main/java/com/dafuweng/finance/service/impl/LoanAuditServiceImpl.java` | 注入 `SalesFeignClient`，添加 `approve()` 方法 |
| `gateway/src/main/java/com/dafuweng/gateway/filter/AuthFilter.java` | 添加 Feign 调用 auth 验证 Token 逻辑 |

### 6.3 依赖验证

| 模块 | 需要的依赖 | 状态 |
|------|---------|------|
| sales | `spring-cloud-starter-openfeign` | ⚠️ 需添加到 sales/pom.xml |
| sales | `spring-cloud-starter-loadbalancer` | ⚠️ 需添加到 sales/pom.xml |
| gateway | `spring-cloud-starter-openfeign` | ⚠️ 需添加到 gateway/pom.xml |
| gateway | `spring-cloud-starter-loadbalancer` | ⚠️ 需添加到 gateway/pom.xml |

---

## 七、POM 依赖补充

### 7.1 sales/pom.xml 新增

```xml
<!-- OpenFeign -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-loadbalancer</artifactId>
</dependency>
```

### 7.2 gateway/pom.xml 新增

```xml
<!-- OpenFeign -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

**注：** gateway 已有 `spring-cloud-starter-loadbalancer`（在 common 依赖中已有或通过 nacos-discovery 传递引入）。

---

## 八、实施步骤

### Step 1：补充 POM 依赖

在 `sales/pom.xml` 和 `gateway/pom.xml` 中添加 OpenFeign 依赖。

### Step 2：创建 sales 内部接口

1. 新建 `InternalSalesController`（3个接口）
2. `PerformanceRecordDao` 新增 `selectOne` 方法及 XML
3. `PerformanceRecordService` 新增 `getByContractId()`
4. `PerformanceRecordServiceImpl` 实现 `getByContractId()`

### Step 3：验证 sales 模块编译

```bash
mvn clean compile -pl sales -am -DskipTests
```

### Step 4：finance LoanAuditServiceImpl 调用 Feign

1. 在 `LoanAuditServiceImpl` 中注入 `SalesFeignClient`
2. 新增 `approve()` 方法，触发 `updateContractStatus` + `createPerformance` 调用
3. 在其他审核状态流转处（发送金融部、银行反馈）触发 `updateContractStatus`

### Step 5：验证 finance 模块编译

```bash
mvn clean compile -pl finance -am -DskipTests
```

### Step 6：创建 sales Feign 客户端

1. 新建 `AuthFeignClient`（sales → auth）
2. 新建 `SystemFeignClient`（sales → system）
3. `SalesApplication` 添加 `@EnableFeignClients`

### Step 7：创建 gateway Feign 客户端

1. 新建 `AuthFeignClient`（gateway → auth）
2. `GatewayApplication` 添加 `@EnableFeignClients`

### Step 8：实现 gateway AuthFilter Token 校验逻辑

在 AuthFilter 中，通过 `AuthFeignClient` 调用 auth 验证 Token。

### Step 9：全项目编译验证

```bash
mvn clean compile -pl common,sales,finance,system,auth,gateway -DskipTests
```

---

## 九、验收标准

1. sales 内部接口 `/sales/internal/performances/create`、`/sales/internal/contracts/{id}`、`/sales/internal/contracts/{id}/status` 可正常响应
2. finance `LoanAuditServiceImpl.approve()` 方法能成功调用 `SalesFeignClient`，触发 sales 创建业绩记录
3. sales → auth、sales → system Feign 客户端可正常调用
4. gateway → auth Feign 客户端可正常调用（待 auth 接口就绪后）
5. `mvn clean compile -pl common,sales,finance,system,auth,gateway -DskipTests` 全模块通过

---

## 十、后续扩展

### 10.1 待 Phase 后续处理的 Feign 调用

| 扩展项 | 说明 |
|--------|------|
| ProductFeignClient（sales → finance） | 查询金融产品获取 commission_rate，完善业绩创建逻辑 |
| AuthFeignClient 增强 | auth 实现 JWT 后，增强 Token 解析和验证逻辑 |
| 业绩确认/发放回调 | finance 调用 sales `/internal/performances/{id}/grant` 发放业绩 |

### 10.2 ContractVO 补充 zoneId

当前 `ContractVO` 没有 `zoneId` 字段，但业绩创建需要。建议在 `ContractVO` 中增加：

```java
/** 战区ID */
private Long zoneId;
```

对应的 `InternalSalesController.getContract()` 中需要从 `ContractEntity` 映射此字段（需确认 `ContractEntity` 是否有 `zoneId`）。

### 10.3 commissionRate 查询

业绩创建时 `commissionRate` 和 `commissionAmount` 当前传 0，需后续通过 `FinanceProductFeignClient` 查询产品费率后完善。
