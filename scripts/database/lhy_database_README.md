# 数据库快照说明

## 文件信息
- 文件名：`lhy_database.sql`
- 导出时间：2026-04-22 16:27-16:29
- 文件大小：112KB
- 包含数据库：dafuweng_auth, dafuweng_system, dafuweng_sales, dafuweng_finance
- 表数量：23个INSERT语句

---

## 与原始设计（datas.sql, dataRole.sql）的区别

### 1. auth 数据库变更

#### 1.1 sys_permission 表

| 项目 | 原始设计 | 当前状态 | 说明 |
|------|----------|----------|------|
| 顶级菜单数量 | 5个（工作台/SYSTEM/SALES/FINANCE/PERFORMANCE） | 4个（SYSTEM/SALES/FINANCE/PERFORMANCE） | 工作台(DASHBOARD)被删除或未导入 |
| 菜单ID | 重新分配 | 存在重复ID问题 | 原始设计的ID可能未完全应用 |
| 菜单状态(status) | 1(启用) | 部分为0(禁用) | SYSTEM菜单status=0导致不显示 |
| 删除标记(deleted) | 0 | 部分为1 | SYSTEM及其子菜单deleted=1 |

**问题记录：**
- SYSTEM(id=1) 和其子菜单 status=0, deleted=1 → 已修复
- 顶级菜单排序：原始设计顺序可能被打乱

#### 1.2 sys_role_permission 表

| 项目 | 原始设计 | 当前状态 | 说明 |
|------|----------|----------|------|
| 角色数量 | 7个 | 7个 | 基本一致 |
| 权限关联 | 完整 | 部分缺失 | 部分角色缺少顶级菜单权限 |

**问题记录：**
- SALES_REP(销售代表) 缺少 `SALES` 顶级菜单权限 → 已修复
- FINANCE_SPECIALIST(金融专员) 缺少 `FINANCE` 顶级菜单权限 → 已修复
- 销售代表系统管理权限过重（有 SYSTEM_USER、SYSTEM_ROLE）→ 待讨论

#### 1.3 测试账号

| 原始设计 | 当前状态 |
|----------|----------|
| admin, zhangsan, lisi, wangwu, zhaoliu 等 | 新增 test_admin, test_dept_mgr, test_zone_dir, test_sales, test_finance, test_auditor |

**新增测试账号：**
```sql
-- 密码统一为：123456 (BCrypt哈希)
test_admin / 测试管理员 / 超级管理员
test_dept_mgr / 测试部门经理 / 部门经理
test_zone_dir / 测试战区总监 / 战区总监
test_sales / 测试销售代表 / 销售代表
test_finance / 测试金融专员 / 金融专员
test_auditor / 测试审计员 / 审计员
```

### 2. system 数据库变更

#### 2.1 数据完整性

| 表 | 原始数据 | 当前状态 |
|----|----------|----------|
| sys_zone | 6条 | 完整 |
| sys_department | 8条 | 完整 |
| sys_dict | 15条+ | 完整 |
| sys_param | 12条 | 完整 |
| sys_operation_log | 7条 | 完整 |

### 3. sales 数据库变更

#### 3.1 中文乱码问题

| 表 | 原始设计 | 当前状态 |
|----|----------|----------|
| customer | 中文正常 | **已修复**（之前是乱码） |
| contract | 中文正常 | **已修复** |
| contact_record | 中文正常 | **已修复** |
| work_log | 中文正常 | **已修复** |
| customer_transfer_log | 中文正常 | **已修复** |

**乱码原因：** 数据导入时使用了 latin1 连接字符集，导致 UTF-8 字节被双重编码存储。

**修复方法：** 用 latin1 连接读取正确中文，再用 utf8mb4 写入。

### 4. finance 数据库变更

| 表 | 原始数据 | 当前状态 |
|----|----------|----------|
| bank | 5条 | 完整，contactPerson字段已修复 |
| finance_product | 6条 | 完整 |
| loan_audit | 8条 | 完整 |
| loan_audit_record | 11条 | 完整 |
| service_fee_record | 5条 | 完整 |
| commission_record | 4条 | 完整 |

---

## 待讨论/待实现的功能

### 1. 数据级别权限隔离（未实现）

原始设计中的 `data_scope` 字段目前**未生效**：
- 角色表中定义了 data_scope（1=自己，2=部门，3=战区，4=全部）
- 但后端代码**未实现**根据 data_scope 自动过滤数据的逻辑

**影响：**
- 销售代表理论上只能看到自己的客户，但目前可以看到全部客户
- 部门经理理论上只能看到本部门的数据，但目前可以看到全部
- 战区总监理论上只能看到本战区的数据，但目前可以看到全部

**需要后端配合实现。**

### 2. 菜单命名规范问题

| 问题 | 说明 |
|------|------|
| PERF_ vs PERFORMANCE | 顶级菜单用 PERFORMANCE，子菜单用 PERF_ 开头，命名不统一 |
| 菜单 vs 按钮边界 | 部分操作（如新增合同）被设为菜单类型而非按钮 |

### 3. 权限分配合理性

| 角色 | 当前系统管理权限 | 问题 |
|------|-----------------|------|
| 销售代表 | SYSTEM_USER, SYSTEM_ROLE | 不应该管理系统用户/角色 |
| 金融专员 | SYSTEM_PERMISSION | 不应该管理权限 |
| 部门经理 | SYSTEM_* 全部 | 权限过大 |
| 战区总监 | SYSTEM_* 全部 | 权限过大 |

---

## 修复记录

| 日期 | 修复内容 |
|------|----------|
| 2026-04-22 | 修复 sys_permission 表中 SYSTEM 及子菜单的 status=1, deleted=0 |
| 2026-04-22 | 为销售代表添加 SALES 顶级菜单权限 |
| 2026-04-22 | 为金融专员添加 FINANCE 顶级菜单权限 |
| 2026-04-22 | 修复所有业务表的中文乱码问题（customer, contract, contact_record, work_log, bank 等） |
| 2026-04-22 | 创建测试账号 test_admin, test_dept_mgr, test_zone_dir, test_sales, test_finance, test_auditor |
