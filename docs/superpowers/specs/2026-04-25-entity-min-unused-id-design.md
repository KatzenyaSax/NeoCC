# 前端实体新建/删除改造设计

## 1. 需求概述

1. **过滤已删除数据**：前端所有列表不显示 `deleted=1` 的记录
2. **新建时使用最小未使用ID**：所有实体新建时都先调用 `GET /{entity}/min-unused-id` 获取ID
3. **删除改为软删除**：前端所有删除操作改为将实体的 `deleted` 改为1，而非硬删除

## 2. 后端改造

### 2.1 需要实现min-unused-id的实体（共20个）

| 模块 | 实体 | 表名 |
|------|------|------|
| auth | SysPermissionEntity | sys_permission |
| auth | SysRoleEntity | sys_role |
| auth | SysUserEntity | sys_user |
| finance | BankEntity | bank |
| finance | CommissionRecordEntity | commission_record |
| finance | FinanceProductEntity | finance_product |
| finance | LoanAuditEntity | loan_audit |
| finance | ServiceFeeRecordEntity | service_fee_record |
| sales | ContactRecordEntity | contact_record |
| sales | ContractAttachmentEntity | contract_attachment |
| sales | ContractEntity | contract |
| sales | CustomerEntity | customer |
| sales | CustomerTransferLogEntity | customer_transfer_log |
| sales | PerformanceRecordEntity | performance_record |
| sales | WorkLogEntity | work_log |
| system | SysDepartmentEntity | sys_department |
| system | SysDictEntity | sys_dict |
| system | SysParamEntity | sys_param |
| system | SysZoneEntity | sys_zone |

### 2.2 每个实体需要添加

**Dao层**：
```java
@Select("SELECT COALESCE(MIN(t.id + 1), 1) FROM (SELECT 1 as id UNION SELECT MAX(id) + 1 FROM {table_name}) t WHERE NOT EXISTS (SELECT 1 FROM {table_name} c WHERE c.id = t.id)")
Long selectMinUnusedId();
```

**Service层**：
```java
Long getMinUnusedId();
```

**Controller层**：
```java
@GetMapping("/min-unused-id")
public Result<Long> getMinUnusedId() {
    return Result.success(entityService.getMinUnusedId());
}
```

## 3. 前端改造

### 3.1 删除操作改造（17个Vue页面）

将硬删除改为软删除：
- 原：`delEntity(row.id)` 调用DELETE接口
- 改：`updateEntity({ id: row.id, deleted: 1 })` 调用PUT接口

### 3.2 新建操作改造（15个Vue页面）

在handleAdd中先调用min-unused-id接口：
```javascript
function handleAdd() {
  reset()
  getMinUnusedEntityId().then(res => {
    form.value.id = res.data
    open.value = true
    title.value = "新增..."
  })
}
```

### 3.3 涉及的Vue页面

| 模块 | 页面 | 新建改造 | 删除改造 |
|------|------|---------|---------|
| system | user/index.vue | 是 | 是 |
| system | role/index.vue | 是 | 是 |
| system | dept/index.vue | 是 | 是 |
| system | dict/index.vue | 是 | 是 |
| system | param/index.vue | 是 | 是 |
| system | permission/index.vue | 是 | 是 |
| system | zone/index.vue | 是 | 是 |
| sales | customer/index.vue | 已完成 | 是 |
| sales | transfer/index.vue | 已完成 | 是 |
| sales | worklog/index.vue | 是 | 是 |
| sales | contact/index.vue | 是 | 是 |
| sales | performance-record/index.vue | 是 | 是 |
| finance | bank/index.vue | 是 | 是 |
| finance | product/index.vue | 是 | 是 |
| finance | service-fee/index.vue | 是 | 是 |

**注**：contract/index.vue 和 public-sea/index.vue 只有删除没有新增按钮，无需改造新建功能。

## 4. TDD检查清单

实现完成后验证：
1. 新建实体时ID是连续紧凑的
2. 删除操作后数据仍在数据库但前端不显示
3. 刷新页面后已删除数据不出现
