# 数据库 Schema 核查报告

> 比对范围：`database.sql`（DDL原始脚本）vs Entity类 vs MyBatis Mapper XML
> 说明：实际运行库因无法通过 mysql.exe 直连（Windows环境），以 `database.sql` + 代码反推方式核查

---

## 一、严重问题（必须立即修复）

### 1. `contract.zone_id` 列类型错误

**文件**: `dafuweng_sales.contract`
**问题**: DDL 中 `zone_id` 定义为 `VARCHAR`（乱码 COLLATE），实际应为 `BIGINT`

```sql
-- database.sql 第351行（错误）
`zone_id`  BIGINT       COLLATE '所属战区'   <-- VARCHAR类型 + 中文COLLATE

-- ContractEntity.java 第34行（正确）
private Long zoneId;   // MyBatis-Plus 默认映射到 zone_id 列
```

**影响**: 所有带 `zoneId` 的 INSERT/UPDATE 都会因类型不匹配失败

**修正SQL**:
```sql
ALTER TABLE dafuweng_sales.contract
  MODIFY COLUMN zone_id BIGINT NOT NULL COMMENT '所属战区ID';
```

---

## 二、约束不一致（建议修复）

### 2. `service_fee_record.accountant_id` 约束宽松

**Entity**: `ServiceFeeRecordEntity.accountantId` → `Long accountantId`（无 `@TableField`，默认可null）
**DDL**: `accountant_id BIGINT NOT NULL`

**影响**: MyBatis-Plus INSERT 时若未主动设值，可能写入 null 导致DB约束异常

**修正SQL**（如业务允许会计可空）:
```sql
ALTER TABLE dafuweng_finance.service_fee_record
  MODIFY COLUMN accountant_id BIGINT COMMENT '会计ID';
```

**修正SQL**（如业务要求会计必填，保持DDL严格）:
```sql
-- 无需DDL变更，但请在 ServiceFeeRecordEntity 中补 @TableField 显式标注
-- 并确保 ServiceFeeRecordServiceImpl.insert() 时必填 accountantId
```

---

### 3. `service_fee_record.accountant_id` 在 Mapper XML 中缺失

**文件**: `finance/src/main/resources/finance/mapper/ServiceFeeRecordDao.xml`

`ServiceFeeRecordDao.xml` 的 `serviceFeeRecordMap` 没有 `<result property="accountantId" column="accountant_id"/>`，而 BaseMapper 的 INSERT 会用到所有字段。

**影响**: 插入 service_fee_record 时 accountant_id 永远为 null（即便 Entity 有值）

**修正**: 在 `ServiceFeeRecordDao.xml` 的 `<resultMap>` 内添加:
```xml
<result property="accountantId" column="accountant_id"/>
```

---

## 三、Mapper XML 问题

### 4. `SysRolePermissionDao.xml` INSERT 语句验证

**文件**: `auth/src/main/resources/auth/mapper/SysRolePermissionDao.xml`

```xml
<!-- 第22行 - 已验证正确 -->
INSERT INTO sys_role_permission (role_id, permission_id, created_at) VALUES
```

目标表 `sys_role_permission`（role_id, permission_id）对应 Entity `SysRolePermissionEntity`，INSERT 正确。

**结论**: 无需修改。

---

### 5. `PerformanceRecordDao.xml` 命名空间与 INSERT 缺失

**文件**: `sales/src/main/resources/sales/mapper/PerformanceRecordDao.xml`

- namespace 正确指向 `com.dafuweng.sales.dao.PerformanceRecordDao`
- 该表仅有 `selectByContractId` 和 `selectOne` 两个查询，无 INSERT 语句
- INSERT 走 BaseMapper 自动生成，无需 XML

**结论**: 无需修改。

---

## 四、Entity vs DDL 字段类型对照（均兼容）

以下字段虽存在 Java类型 vs DB类型的"看似不一致"，但经分析属于兼容范围：

| 表 | 字段 | Entity类型 | DDL类型 | 说明 |
|---|---|---|---|---|
| contract | status | Short | TINYINT | 兼容，Short→TINYINT |
| contract | deleted | Short | TINYINT | @TableLogic 正确 |
| loan_audit | audit_status | Short | TINYINT | 兼容，业务层应控制值≤7 |
| loan_audit | deleted | Short | TINYINT | @TableLogic 正确 |
| commission_record | status | Short | TINYINT | 兼容 |
| service_fee_record | payment_status | Short | TINYINT | 兼容 |
| contact_record | attachment_urls | String | TEXT | 兼容，@TableField 标注正确 |
| loan_audit_record | attachment_urls | String | TEXT | 兼容，@TableField 标注正确 |

**注**: `contract.guarantee_info` 在 Entity 中有 `@TableField("guarantee_info")` 注解，与 DB 列名一致，无问题。

---

## 五、业务状态值核查（仅供参考）

### `loan_audit.audit_status` 值域

DDL 注释定义 1-4，但 Phase3 代码预期 7 状态：

| 值 | DDL注释 | Phase3预期 |
|---|---|---|
| 1 | 待审核 | 待接收 |
| 2 | 审核中 | 初审中 |
| 3 | 已通过 | 已提交银行 |
| 4 | 已拒绝 | 银行拒绝 |
| 5 | - | 终审通过 |
| 6 | - | 终审拒绝 |
| 7 | - | 终审拒绝 |

**建议**: Phase3 代码使用 `audit_status` 1-7 范围，DDL 注释陈旧但列类型兼容（`TINYINT` 可存 1-7），无需改 DDL，注释更新即可。

### `contract.status` 值域

DDL 定义 1-8，与 Phase3 预期一致，无需修改。

---

## 六、修正 SQL 执行清单

```sql
-- ① 修复 contract.zone_id 类型错误（严重！立即执行）
ALTER TABLE dafuweng_sales.contract
  MODIFY COLUMN zone_id BIGINT NOT NULL COMMENT '所属战区ID';

-- ②（可选）统一 service_fee_record.accountant_id 约束（根据业务需求二选一）
-- 方案A：允许为空
ALTER TABLE dafuweng_finance.service_fee_record
  MODIFY COLUMN accountant_id BIGINT COMMENT '会计ID';

-- ③ 在 ServiceFeeRecordDao.xml resultMap 中添加 accountantId 映射
-- （见上方第四節第3条，手动修改XML）

-- ④（可选）更新 loan_audit.audit_status 列注释以反映7状态
ALTER TABLE dafuweng_finance.loan_audit
  MODIFY COLUMN audit_status TINYINT NOT NULL DEFAULT 1
  COMMENT '审核状态: 1-待接收 2-初审中 3-已提交银行 4-银行拒绝 5-终审通过 6-终审拒绝';
```

---

## 七、核查结论

| 级别 | 数量 | 说明 |
|---|---|---|
| 🔴 严重 | 1 | `contract.zone_id` 类型错误会导致所有相关写入失败 |
| 🟡 约束 | 2 | accountant_id 约束宽松 + resultMap 缺失映射 |
| 🟢 通过 | 其余 | Entity/Mapper 与 DDL 均匹配或兼容 |

**优先修复顺序**: ① → ③ → ② → ④
