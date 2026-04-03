# 数据库设计说明书 — 大富翁金融服务公司贷款管理系统
**版本:** v1.0
**日期:** 2026-03-31

---

## 1. 设计原则

### 1.1 库隔离策略
按业务领域垂直拆分4个独立MySQL库，取消传统单库+多Schema模式。理由：
- 销售服务与金融服务的表毫无重叠，隔离可独立演进
- 跨库查询通过OpenFeign在应用层实现，不在数据库层制造耦合
- 故障隔离：一个库炸了不影响其他服务

| 库名 | 服务 | 主要表数 |
|------|------|---------|
| dafuweng_auth | auth-service | 4 |
| dafuweng_system | system-service | 5 |
| dafuweng_sales | sales-service | 6 |
| dafuweng_finance | finance-service | 6 |

### 1.2 通用字段规范
所有业务表统一包含以下字段，由MyBatisPlus的`FillMetaObjectHandler`自动填充：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 自增主键 |
| created_by | BIGINT | 创建人ID |
| created_at | DATETIME | 创建时间，默认CURRENT_TIMESTAMP |
| updated_by | BIGINT | 修改人ID |
| updated_at | DATETIME | 自动更新为当前时间戳 |
| deleted | TINYINT | 逻辑删除标记（0=未删, 1=已删） |
| version | INT | 乐观锁版本号 |

### 1.3 索引设计原则
- 主键索引：自动聚集索引
- 外键字段：必须建普通索引（如`sales_rep_id`）
- 唯一约束：逻辑删除字段参与唯一索引（`name, phone, deleted`），避免软删后数据无法再录入
- 联合索引：按查询最常用组合建索引（如`sales_rep_id, log_date`）
- 禁止：`SELECT *`，所有查询必须命中覆盖索引

---

## 2. dafuweng_auth 库 — 认证授权

### 设计理念
存储所有用户的认证信息，与业务完全解耦。任何服务通过OpenFeign调用`auth-service`验证Token，不直接持有用户表。

### ER图

```
┌─────────────────┐       ┌─────────────────────┐       ┌──────────────────┐
│    sys_user     │       │    sys_user_role    │       │     sys_role     │
├─────────────────┤       ├─────────────────────┤       ├──────────────────┤
│ id (PK)         │───1:N─│ user_id (FK)        │N:1────│ id (PK)          │
│ username (UK)   │       │ role_id (FK)        │       │ role_code (UK)   │
│ password        │       └─────────────────────┘       │ role_name         │
│ real_name       │                                      │ data_scope        │
│ phone           │                                      └──────────────────┘
│ email           │                                            │
│ dept_id (FK)   │                                            │ N:M
│ zone_id (FK)   │                                            │
│ status          │              ┌──────────────────────┐     │
│ login_error_cnt │              │  sys_role_permission  │◄────┘
│ lock_time      │              ├──────────────────────┤
│ last_login_time│              │ role_id (FK)         │
│ last_login_ip  │              │ permission_id (FK)   │N:1──┌──────────────────┐
└─────────────────┘              └──────────────────────┘     │  sys_permission  │
                                                               ├──────────────────┤
                                                               │ id (PK)           │
                                                               │ parent_id (FK)    │
                                                               │ perm_code (UK)    │
                                                               │ perm_name         │
                                                               │ perm_type         │
                                                               │ perm_path         │
                                                               └──────────────────┘
```

### 表结构

| 表名 | 主键 | 外键 | 唯一索引 | 说明 |
|------|------|------|---------|------|
| sys_user | id | dept_id, zone_id | username | 用户账号 |
| sys_role | id | — | role_code | 角色定义 |
| sys_user_role | id | user_id, role_id | (user_id, role_id) | 用户角色关联 |
| sys_permission | id | parent_id | perm_code | 权限树 |
| sys_role_permission | id | role_id, permission_id | (role_id, permission_id) | 角色权限关联 |

### 关键设计决策

**data_scope字段** — 1=本人，2=本部门，3=本战区，4=全部。在MyBatisPlus拦截器中根据当前用户角色动态拼接WHERE条件，销售代表只能看到自己录入的数据。

**login_error_count + lock_time** — 在用户表内自包含登录安全字段，无需独立表。连续5次失败后锁定15分钟，应用层检查而非数据库锁。

---

## 3. dafuweng_system 库 — 系统管理

### 设计理念
存储组织架构（战区、部门）和系统全局参数。系统管理员操作此库，业务数据（客户/合同）不在此处。

### ER图

```
┌──────────────────┐       ┌───────────────────┐
│     sys_zone      │       │   sys_department   │
├──────────────────┤       ├───────────────────┤
│ id (PK)          │───1:N─│ zone_id (FK)       │
│ zone_code (UK)   │       │ id (PK)            │
│ zone_name        │       │ dept_code (UK)     │
│ director_id      │       │ dept_name          │
│ sort_order       │       │ parent_id (FK,自引用)│
│ status           │       │ manager_id         │
└──────────────────┘       │ sort_order         │
                            │ status             │
                            └───────────────────┘

┌──────────────────┐       ┌───────────────────┐       ┌─────────────────┐
│    sys_param     │       │   sys_operation_log│       │    sys_dict     │
├──────────────────┤       ├───────────────────┤       ├─────────────────┤
│ id (PK)          │       │ id (PK)            │       │ id (PK)         │
│ param_key (UK)   │       │ user_id (FK)        │       │ dict_type (UK)  │
│ param_value      │       │ module             │       │ dict_code (UK)  │
│ param_type       │       │ action             │       │ dict_label      │
│ param_group      │       │ request_url        │       │ dict_value      │
│ remark           │       │ request_params     │       │ sort_order      │
│ status           │       │ response_code      │       │ status          │
│ sort_order       │       │ error_msg          │       │ remark          │
└──────────────────┘       │ ip                 │       └─────────────────┘
                             │ cost_time_ms       │
                             │ created_at         │
                             └───────────────────┘
```

### 表结构

| 表名 | 主键 | 外键 | 唯一索引 | 说明 |
|------|------|------|---------|------|
| sys_zone | id | — | zone_code | 战区（2个，战区A/B） |
| sys_department | id | zone_id, parent_id(自引用) | dept_code | 部门，支持树形 |
| sys_param | id | — | param_key | 系统参数KV |
| sys_operation_log | id | user_id | — | 操作审计日志 |
| sys_dict | id | — | (dict_type, dict_code) | 数据字典（枚举值） |

### 关键设计决策

**sys_department.parent_id自引用** — 部门表用邻接表模型（parent_id）存储树形，支持最多两级（战区>部门）。设计保守，够用且简单，不上closure_table。

**sys_dict统一枚举** — 所有枚举值（客户类型/合同状态/意向等级）存储在字典表而非Java枚举类，支持运行时热修改。application层做缓存。

**sys_operation_log非业务表** — 记录所有写操作的审计日志，通过AOP拦截Controller方法自动写入，不影响业务表结构。

---

## 4. dafuweng_sales 库 — 销售核心

### 设计理念
这是整个系统的业务核心。围绕"客户"这个主实体展开：客户 -> 洽谈 -> 合同 -> 业绩。设计核心是防止数据孤岛，保证从客户到合同到业绩的完整链路可追溯。

### ER图

```
┌──────────────────────────────────────────────────────────────────────────┐
│                           customer                                        │
├──────────────────────────────────────────────────────────────────────────┤
│ id (PK)           │ sales_rep_id (FK)  │ dept_id (FK)   │ zone_id (FK) │
│ name              │ intention_level    │ status         │ public_sea_time│
│ phone             │ annotation (JSON)  │ last_contact_..│ next_follow_up│
└───────┬───────────┴─────────┬──────────┴───────┬────────┴───────────────┘
        │ 1:N                 │ 1:N             │ 1:1 (合同签约后)
        ▼                     ▼                 ▼
┌──────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│ contact_record   │  │ work_log         │  │    contract     │
├──────────────────┤  ├─────────────────┤  ├─────────────────┤
│ id (PK)          │  │ id (PK)         │  │ id (PK)         │
│ customer_id (FK) │  │ sales_rep_id(FK)│  │ contract_no(UK) │
│ sales_rep_id(FK) │  │ log_date (UK)   │  │ customer_id(FK) │
│ contact_type     │  │ calls_made      │  │ product_id(FK)  │
│ contact_date     │  │ effective_calls │  │ contract_amount │
│ intention_after  │  │ new_intentions  │  │ service_fee_1   │
│ content          │  │ signed_contracts│  │ service_fee_2   │
└──────────────────┘  └─────────────────┘  │ status          │
                                            └───────┬─────────┘
                                                    │ 1:N
                                                    ▼
                         ┌─────────────────┐  ┌──────────────────────┐
                         │performance_record│  │ contract_attachment  │
                         ├─────────────────┤  ├──────────────────────┤
                         │ id (PK)         │  │ id (PK)              │
                         │ contract_id (UK) │  │ contract_id (FK)     │
                         │ sales_rep_id(FK) │  │ attachment_type       │
                         │ dept_id (FK)    │  │ file_url             │
                         │ commission_amt  │  │ file_name            │
                         │ status          │  │ file_md5             │
                         └─────────────────┘  └──────────────────────┘

                         ┌──────────────────────┐
                         │customer_transfer_log │
                         ├──────────────────────┤
                         │ id (PK)             │
                         │ customer_id (FK)    │
                         │ from_rep_id (FK)    │
                         │ to_rep_id (FK)      │
                         │ operate_type        │
                         │ operated_by (FK)    │
                         │ operated_at         │
                         └──────────────────────┘
```

### 表结构

| 表名 | 主键 | 外键 | 唯一索引 | 说明 |
|------|------|------|---------|------|
| customer | id | sales_rep_id, dept_id, zone_id | (name, phone, deleted) | 客户主表 |
| contact_record | id | customer_id, sales_rep_id | — | 洽谈历史 |
| contract | id | customer_id, sales_rep_id, product_id | contract_no | 贷款合同 |
| contract_attachment | id | contract_id | — | 合同附件 |
| work_log | id | sales_rep_id | (sales_rep_id, log_date) | 工作日志 |
| performance_record | id | contract_id, sales_rep_id, dept_id | contract_id | 业绩记录 |
| customer_transfer_log | id | customer_id, from_rep_id, to_rep_id | — | 客户转移流水 |

### 关键设计决策

**customer表(name, phone, deleted)联合唯一索引** — 防止同一销售重复录入相同姓名+手机号客户。deleted参与索引使得软删除后可以重新录入同名同手机客户（如客户重新合作）。

**contract_id作为performance_record的唯一索引** — 一个合同只对应一条业绩记录，从数据库层面保证不重复计算业绩。

**customer_transfer_log记录所有转移** — 包括部门经理迁移、公海领取，不仅记录结果还记录操作人和原因，审计无死角。

**annotation字段存JSON** — 客户批注结构简单（userId + content + time），不上独立表，直接JSON存，MyBatisPlus的JsonTypeHandler处理序列化。

**work_log用(sales_rep_id, log_date)唯一键** — 每个销售每天只能有一条日志，防止重复提交。

---

## 5. dafuweng_finance 库 — 金融核心

### 设计理念
金融业务的核心是"审核流程"。围绕LoanAudit（贷款审核）这个实体展开：接收合同 -> 初审 -> 提交银行 -> 银行反馈 -> 放款。审核记录（loan_audit_record）作为审计轨迹，是日后争议仲裁的关键证据。

### ER图

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              bank                                       │
├─────────────────────────────────────────────────────────────────────────┤
│ id (PK)              │ bank_code (UK)  │ bank_name     │ status         │
└─────────────────────────────────────────────────────────────────────────┘
         │ 1:N
         ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                        finance_product                                  │
├─────────────────────────────────────────────────────────────────────────┤
│ id (PK)              │ product_code (UK)│ bank_id (FK)│ min_amount    │
│ max_amount           │ interest_rate    │ min_term    │ max_term      │
│ requirements (JSON) │ commission_rate  │ status      │ online_time   │
└──────────┬────────────────────────────┬────────────────────────────────┘
           │ 1:N                          │ 1:1 (签约后关联)
           ▼                              ▼
┌──────────────────────┐       ┌────────────────────────────────────────┐
│    loan_audit        │       │            contract                      │
├──────────────────────┤       │ (来自sales库, 通过contract_id关联)         │
│ id (PK)              │       ├────────────────────────────────────────┤
│ contract_id (UK,FK) │───────►│ id                                     │
│ finance_specialist_id│       │ contract_no                            │
│ recommended_product_id│      │ customer_id                            │
│ approved_amount      │       │ sales_rep_id                           │
│ approved_term       │       │ status (审核后流转)                       │
│ audit_status        │       └─────────────────────────────────────────┘
│ bank_id (FK)        │──────────────────────────────────────────►
│ bank_audit_status   │                              ┌──────────────────┐
│ bank_feedback_time  │                              │service_fee_record │
│ bank_feedback_content│                              ├──────────────────┤
│ reject_reason       │                              │ id (PK)          │
│ loan_granted_date   │                              │ contract_id (FK) │
│ actual_loan_amount │                              │ fee_type         │
└───────┬────────────┘                              │ amount           │
        │ 1:N                                        │ payment_method   │
        ▼                                            │ accountant_id(FK)│
┌────────────────────────┐                           │ payment_status   │
│   loan_audit_record    │                           │ payment_date    │
├────────────────────────┤                           └──────────────────┘
│ id (PK)               │
│ loan_audit_id (FK)   │         ┌────────────────────────┐
│ operator_id (FK)     │         │  commission_record       │
│ operator_name        │         ├────────────────────────┤
│ operator_role        │         │ id (PK)                │
│ action               │         │ performance_id (FK)    │
│ content              │         │ sales_rep_id (FK)      │
│ attachment_urls      │         │ contract_id (FK)      │
│ created_at           │         │ commission_amount      │
└───────────────────────┘         │ status                │
                                   │ grant_account         │
                                   └────────────────────────┘
```

### 表结构

| 表名 | 主键 | 外键 | 唯一索引 | 说明 |
|------|------|------|---------|------|
| bank | id | — | bank_code | 合作银行 |
| finance_product | id | bank_id | product_code | 金融产品 |
| loan_audit | id | contract_id, finance_specialist_id, bank_id | contract_id | 贷款审核 |
| loan_audit_record | id | loan_audit_id, operator_id | — | 审核轨迹（审计） |
| service_fee_record | id | contract_id, accountant_id | — | 服务费收取记录 |
| commission_record | id | performance_id, sales_rep_id, contract_id | — | 提成发放记录 |

### 关键设计决策

**loan_audit.contract_id唯一键** — 一个合同只有一条审核记录，从DB层保证幂等。重复提交银行会报错。

**loan_audit_record action字段** — 存储审核轨迹：receive（接收）→ review（初审）→ submit_bank（提交银行）→ bank_result（银行反馈）→ approve/reject（终审）。任何一步出问题都有据可查。

**service_fee_record分离于contract表** — contract表存服务费金额，service_fee_record存实际收取流水。会计收取服务费时写入自己的记录，不动合同表。

**actual_loan_amount和actual_interest_rate字段在loan_audit** — 银行最终放款金额和利率可能与审批时不同（如银行微调），这些字段在银行反馈后填充，是真实执行数据。

---

## 6. 跨库关联设计

```
sales-service                                  finance-service
┌─────────────────────┐                        ┌─────────────────────┐
│ customer            │                        │ loan_audit          │
│ id=1                │◄── OpenFeign 调用 ─────│ contract_id=1       │
│ name="张三"         │   auth: 验证token       │ audit_status=3      │
└─────────────────────┘   传递: Authorization   └─────────────────────┘
        │                       Header
        │ 签约后
        ▼
┌─────────────────────┐     contract_id
│ contract            │────────────────────────►finance_service更新状态
│ id=1                │     RabbitMQ事件
│ contract_amount     │
└─────────────────────┘
        │
        │ 计算业绩
        ▼
┌─────────────────────┐
│ performance_record  │
│ contract_id=1       │
│ commission_amt      │
└─────────────────────┘
```

跨库关联通过：
1. **OpenFeign** — 实时查询（如金融部查询合同详情）
2. **RabbitMQ** — 异步事件（如合同签署通知金融部）
3. **Nacos配置** — 金融产品ID等字典数据通过消息同步或定时刷新

---

## 7. 关键设计模式

### 7.1 逻辑删除 + 唯一索引冲突解决

MySQL唯一索引忽略NULL但包含NULL值。设计中所有唯一索引都包含`deleted`字段：

```sql
UNIQUE KEY `uk_name_phone` (`name`, `phone`, `deleted`)
-- deleted=0 时约束生效
-- deleted=1 时该行变为幽灵行，不再参与唯一约束
```

### 7.2 乐观锁

所有核心业务表有`version`字段，更新时：
```sql
UPDATE customer SET ... , version = version + 1
WHERE id = ? AND version = ?
-- 影响行数=0时抛出VersionNotMatchException
```

### 7.3 审核轨迹设计

loan_audit_record是append-only日志表，只INSERT不UPDATE，历史不可篡改。这是金融业务审计合规要求。

### 7.4 JSON字段使用场景

以下字段用JSON存储而非独立关联表：

| 字段 | 表 | JSON内容 |
|------|-----|---------|
| annotation | customer | [{userId, content, time}] |
| requirements | finance_product | ["条件1", "条件2"] |
| documents | finance_product | ["材料1", "材料2"] |
| request_params | sys_operation_log | {param1: xxx, param2: yyy} |

判定标准：内聚性强（永不在JOIN中单独查询JSON内字段）、结构稳定。

---

## 8. 索引清单（按库汇总）

### dafuweng_auth
| 表 | 索引类型 | 索引字段 |
|----|---------|---------|
| sys_user | UK | username |
| sys_user | IDX | dept_id, zone_id, status |
| sys_role | UK | role_code |
| sys_user_role | UK | (user_id, role_id) |
| sys_permission | UK | perm_code |
| sys_role_permission | UK | (role_id, permission_id) |

### dafuweng_system
| 表 | 索引类型 | 索引字段 |
|----|---------|---------|
| sys_zone | UK | zone_code |
| sys_department | UK | dept_code |
| sys_department | IDX | parent_id, zone_id |
| sys_param | UK | param_key |
| sys_param | IDX | param_group |
| sys_operation_log | IDX | user_id, created_at |
| sys_dict | UK | (dict_type, dict_code) |

### dafuweng_sales
| 表 | 索引类型 | 索引字段 |
|----|---------|---------|
| customer | UK | (name, phone, deleted) |
| customer | IDX | sales_rep_id, dept_id, zone_id, status, intention_level, public_sea_time, last_contact_date |
| contact_record | IDX | customer_id, sales_rep_id, contact_date |
| contract | UK | contract_no |
| contract | IDX | customer_id, sales_rep_id, dept_id, product_id, status, sign_date |
| contract_attachment | IDX | contract_id |
| work_log | UK | (sales_rep_id, log_date) |
| performance_record | UK | contract_id |
| performance_record | IDX | sales_rep_id, dept_id, zone_id, status |
| customer_transfer_log | IDX | customer_id, operated_at |

### dafuweng_finance
| 表 | 索引类型 | 索引字段 |
|----|---------|---------|
| bank | UK | bank_code |
| finance_product | UK | product_code |
| finance_product | IDX | bank_id, status, min_amount |
| loan_audit | UK | contract_id |
| loan_audit | IDX | finance_specialist_id, audit_status, bank_id |
| loan_audit_record | IDX | loan_audit_id, created_at |
| service_fee_record | IDX | contract_id, fee_type, payment_status, accountant_id |
| commission_record | IDX | performance_id, sales_rep_id, contract_id, status |

---

## 9. 命名规范

| 规范 | 示例 |
|------|------|
| 表名 | 单词用单数，全小写，下划线分隔 `customer` |
| 字段名 | 小写下划线 `created_at`, `sales_rep_id` |
| 枚举值存JSON | `customer_status` 存1/2/3，不存字符串 |
| 外键命名 | `{子表}_{父表}_id`：`customer_sales_rep_id` |
| 时间字段 | `_at`结尾（DATETIME），`_date`结尾（DATE） |
| 布尔字段 | `is_`/`has_`/`paid`前缀：`service_fee_1_paid` |
| 索引命名 | `idx_{表}_{字段}`：`idx_customer_sales_rep_id` |
| 唯一约束 | `uk_{表}_{字段}`：`uk_contract_no` |
