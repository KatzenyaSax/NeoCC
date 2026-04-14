# 大富翁金融 CRM — 后端接口文档

**版本：** v1.0.0
**日期：** 2026-04-14
**状态：** 开发联调阶段
**负责人：** 后端架构组

---

## 目录

1. [全局约定](#全局约定)
2. [认证说明](#认证说明)
3. [服务地址](#服务地址)
4. [通用数据结构](#通用数据结构)
5. [认证模块 (auth)](#认证模块-auth)
6. [系统管理模块 (system)](#系统管理模块-system)
7. [销售管理模块 (sales)](#销售管理模块-sales)
8. [金融审核模块 (finance)](#金融审核模块-finance)
9. [状态码枚举速查表](#状态码枚举速查表)

---

## 全局约定

### 请求规范

| 规范项 | 说明 |
|--------|------|
| 编码 | UTF-8 |
| Content-Type | `application/json` |
| 时间格式 | Unix 时间戳（毫秒），或 ISO 8601 字符串（`yyyy-MM-dd HH:mm:ss`） |
| 字段命名 | camelCase |
| 路径参数 | `{id}` 均为 Long 类型 |

### 统一响应格式

所有接口均返回以下结构：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Integer | 业务状态码，200 表示成功 |
| message | String | 提示信息 |
| data | Object/Array/null | 业务数据，无数据时为 null |

### 通用错误码

| code | 含义 | 处理建议 |
|------|------|---------|
| 200 | 成功 | — |
| 400 | 请求参数错误 | 检查请求体字段 |
| 401 | 未认证 | 重新登录，获取新 Token |
| 403 | 无权限 | 检查用户角色/权限配置 |
| 500 | 服务器内部错误 | 联系后端排查 |

---

## 认证说明

本系统使用 **JWT + Bearer Token** 认证（无状态，Token 即用户 ID 字符串）。

### 登录流程

```
1. POST /api/sysUser/login → 获取 token 字段
2. 后续所有请求在 Header 中携带: Authorization: Bearer {token}
```

### Header 要求

```http
Authorization: Bearer 1001
Content-Type: application/json
```

> **注意：** 当前 token 值为用户 ID 字符串（内部约定），格式为纯数字字符串。
> 以下接口**无需** Token：`POST /api/sysUser/login`、`GET /api/sysUser/page`

### 认证失败响应

```json
{
  "code": 403,
  "message": "Access Denied",
  "data": null
}
```

---

## 服务地址

> 开发环境下，前端可直连各服务端口，也可统一走网关（推荐）。

| 服务 | 直连端口 | 说明 |
|------|---------|------|
| **网关 (gateway)** | **8086** | 统一入口，生产环境前端只用此端口 |
| 认证服务 (auth) | 8085 | 用户/角色/权限管理 |
| 系统服务 (system) | 8082 | 部门/字典/参数/操作日志 |
| 销售服务 (sales) | 8083 | 客户/合同/业绩/跟进记录 |
| 金融服务 (finance) | 8084 | 贷款审核/提成/服务费 |

---

## 通用数据结构

### PageRequest — 分页查询参数（GET 请求 Query 参数）

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Integer | 否 | 1 | 当前页码，从 1 开始 |
| size | Integer | 否 | 10 | 每页条数 |
| sortField | String | 否 | — | 排序字段（驼峰，如 `createdAt`）|
| sortOrder | String | 否 | `asc` | 排序方向：`asc` / `desc` |

### PageResponse — 分页返回结构

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 100,
    "records": [],
    "page": 1,
    "size": 10
  }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| total | Long | 总记录数 |
| records | Array | 当前页数据列表 |
| page | Integer | 当前页码 |
| size | Integer | 每页条数 |

---

## 认证模块 (auth)

> 基础路径：`/api` | 端口：8085

---

### 1. 用户管理 `/api/sysUser`

#### 1.1 用户登录

```
POST /api/sysUser/login
```

**请求体：**

```json
{
  "username": "admin",
  "password": "123456",
  "loginIp": "192.168.1.1"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 用户名 |
| password | String | 是 | 密码（明文，HTTPS 传输）|
| loginIp | String | 否 | 客户端 IP，不传默认 "unknown" |

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1001,
    "username": "admin",
    "realName": "管理员",
    "phone": "13800000000",
    "email": "admin@example.com",
    "deptId": 10,
    "zoneId": 1,
    "status": 1,
    "lastLoginTime": "2026-04-14T10:00:00",
    "lastLoginIp": "192.168.1.1"
  }
}
```

> **前端要求：** 响应的 `data.id` 即为 token，后续请求携带 `Authorization: Bearer {id}`

---

#### 1.2 用户退出登录

```
POST /api/sysUser/logout
```

**请求头：** `Authorization: Bearer {token}`

**请求体：**

```json
{
  "userId": 1001
}
```

**响应：** `data: null`

---

#### 1.3 查询用户详情

```
GET /api/sysUser/{id}
```

**路径参数：**

| 参数 | 类型 | 说明 |
|------|------|------|
| id | Long | 用户 ID |

**响应：** [SysUser 对象](#sysuser-对象)

---

#### 1.4 分页查询用户列表

```
GET /api/sysUser/page
```

**Query 参数：** 参见 [PageRequest](#pagerequest--分页查询参数get-请求-query-参数)

**响应：** PageResponse\<SysUser\>

---

#### 1.5 查询用户拥有的角色 ID 列表

```
GET /api/sysUser/{id}/roles
```

**响应：**

```json
{
  "code": 200,
  "data": [1, 2, 3]
}
```

---

#### 1.6 查询用户拥有的权限码列表

```
GET /api/sysUser/{id}/permCodes
```

**响应：**

```json
{
  "code": 200,
  "data": ["customer:view", "customer:edit", "contract:sign"]
}
```

> 前端权限控制：按钮/菜单是否显示以此列表中的权限码为准。

---

#### 1.7 新增用户

```
POST /api/sysUser
```

**请求体：** [SysUser 对象](#sysuser-对象)（`id` 由后端生成，无需传入）

**响应：** 创建后的 SysUser 对象（含 id）

---

#### 1.8 修改用户

```
PUT /api/sysUser
```

**请求体：** [SysUser 对象](#sysuser-对象)（必须包含 `id`）

**响应：** 更新后的 SysUser 对象

---

#### 1.9 为用户分配角色

```
PUT /api/sysUser/{id}/roles
```

**请求体：**

```json
{
  "roleIds": [1, 2, 3]
}
```

**响应：** `data: null`

---

#### 1.10 解锁用户

```
PUT /api/sysUser/{id}/unlock
```

> 用户登录失败次数过多被锁定后，管理员调用此接口解锁。

**响应：** `data: null`

---

#### 1.11 修改密码

```
PUT /api/sysUser/{id}/password
```

**请求体：**

```json
{
  "oldPassword": "oldPass123",
  "newPassword": "newPass456"
}
```

**响应：** `data: null`

---

#### 1.12 删除用户

```
DELETE /api/sysUser/{id}
```

**响应：** `data: null`

---

#### SysUser 对象

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 用户 ID |
| username | String | 用户名（登录账号）|
| password | String | 密码（仅新增/修改时传入，查询返回不含此字段）|
| realName | String | 真实姓名 |
| phone | String | 手机号 |
| email | String | 邮箱 |
| deptId | Long | 所属部门 ID |
| zoneId | Long | 所属战区 ID |
| status | Short | 状态：1=正常，0=禁用 |
| loginErrorCount | Integer | 登录错误次数 |
| lockTime | Date | 锁定时间 |
| lastLoginTime | Date | 最后登录时间 |
| lastLoginIp | String | 最后登录 IP |
| createdBy | Long | 创建人 ID |
| createdAt | Date | 创建时间 |
| updatedBy | Long | 更新人 ID |
| updatedAt | Date | 更新时间 |

---

### 2. 角色管理 `/api/sysRole`

#### 2.1 查询角色详情

```
GET /api/sysRole/{id}
```

**响应：** [SysRole 对象](#sysrole-对象)

---

#### 2.2 分页查询角色列表

```
GET /api/sysRole/page
```

**响应：** PageResponse\<SysRole\>

---

#### 2.3 按状态查询角色列表

```
GET /api/sysRole/listByStatus?status={status}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | Short | 是 | 1=启用，0=禁用 |

**响应：** SysRole 数组

---

#### 2.4 查询角色拥有的权限 ID 列表

```
GET /api/sysRole/{id}/permissions
```

**响应：**

```json
{
  "code": 200,
  "data": [1, 5, 10, 15]
}
```

---

#### 2.5 新增角色

```
POST /api/sysRole
```

**请求体：** [SysRole 对象](#sysrole-对象)

---

#### 2.6 修改角色

```
PUT /api/sysRole
```

---

#### 2.7 为角色分配权限

```
PUT /api/sysRole/{id}/permissions
```

**请求体：**

```json
{
  "permissionIds": [1, 5, 10, 15]
}
```

---

#### 2.8 删除角色

```
DELETE /api/sysRole/{id}
```

---

#### SysRole 对象

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 角色 ID |
| roleCode | String | 角色编码（唯一）|
| roleName | String | 角色名称 |
| dataScope | Short | 数据权限范围：1=全部，2=本战区，3=本部门，4=本人 |
| roleSort | Integer | 排序值 |
| status | Short | 状态：1=启用，0=禁用 |
| createdAt | Date | 创建时间 |
| updatedAt | Date | 更新时间 |

---

### 3. 权限管理 `/api/sysPermission`

#### 3.1 查询权限详情

```
GET /api/sysPermission/{id}
```

---

#### 3.2 分页查询权限列表

```
GET /api/sysPermission/page
```

---

#### 3.3 获取权限树（菜单树）

```
GET /api/sysPermission/tree
```

> 用于前端渲染菜单/权限管理树形结构。

**响应：** SysPermission 数组（含嵌套子节点）

---

#### 3.4 查询子权限列表

```
GET /api/sysPermission/children?parentId={parentId}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| parentId | Long | 是 | 父节点 ID，0 表示顶级 |

---

#### 3.5 按状态查询权限

```
GET /api/sysPermission/listByStatus?status={status}
```

---

#### 3.6 新增权限

```
POST /api/sysPermission
```

---

#### 3.7 修改权限

```
PUT /api/sysPermission
```

---

#### 3.8 删除权限

```
DELETE /api/sysPermission/{id}
```

---

#### SysPermission 对象

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 权限 ID |
| parentId | Long | 父节点 ID（0=顶级）|
| permCode | String | 权限编码（如 `customer:edit`）|
| permName | String | 权限名称 |
| permType | Short | 权限类型：1=目录，2=菜单，3=按钮 |
| permPath | String | 路由路径（前端路由，如 `/customer`）|
| icon | String | 图标名称 |
| sortOrder | Integer | 排序值 |
| status | Short | 状态：1=启用，0=禁用 |
| externalLink | Short | 是否外链：1=是，0=否 |

---

## 系统管理模块 (system)

> 基础路径：`/api` | 端口：8082

---

### 4. 战区管理 `/api/sysZone`

#### 4.1 查询战区详情

```
GET /api/sysZone/{id}
```

**响应：** [SysZone 对象](#syszone-对象)

---

#### 4.2 分页查询

```
GET /api/sysZone/page
```

---

#### 4.3 查询全部战区

```
GET /api/sysZone/listAll
```

**响应：** SysZone 数组

---

#### 4.4 按状态查询

```
GET /api/sysZone/listByStatus?status={status}
```

---

#### 4.5 新增战区

```
POST /api/sysZone
```

---

#### 4.6 修改战区

```
PUT /api/sysZone
```

---

#### 4.7 删除战区

```
DELETE /api/sysZone/{id}
```

---

#### SysZone 对象

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 战区 ID |
| zoneCode | String | 战区编码 |
| zoneName | String | 战区名称 |
| directorId | Long | 战区负责人用户 ID |
| sortOrder | Integer | 排序值 |
| status | Short | 状态：1=启用，0=禁用 |
| createdAt | Date | 创建时间 |
| updatedAt | Date | 更新时间 |

---

### 5. 部门管理 `/api/sysDepartment`

#### 5.1 查询部门详情

```
GET /api/sysDepartment/{id}
```

**响应：** [SysDepartment 对象](#sysdepartment-对象)

---

#### 5.2 分页查询

```
GET /api/sysDepartment/page
```

---

#### 5.3 按父节点查询子部门

```
GET /api/sysDepartment/listByParentId/{parentId}
```

> parentId=0 查询顶级部门

---

#### 5.4 按战区查询部门

```
GET /api/sysDepartment/listByZoneId/{zoneId}
```

---

#### 5.5 新增部门

```
POST /api/sysDepartment
```

---

#### 5.6 修改部门

```
PUT /api/sysDepartment
```

---

#### 5.7 删除部门

```
DELETE /api/sysDepartment/{id}
```

---

#### SysDepartment 对象

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 部门 ID |
| deptCode | String | 部门编码 |
| deptName | String | 部门名称 |
| parentId | Long | 父部门 ID（0=顶级）|
| zoneId | Long | 所属战区 ID |
| managerId | Long | 部门负责人用户 ID |
| sortOrder | Integer | 排序值 |
| status | Short | 状态：1=启用，0=禁用 |

---

### 6. 数据字典 `/api/sysDict`

#### 6.1 按字典类型查询字典项列表

```
GET /api/sysDict/listByDictType?dictType={dictType}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| dictType | String | 是 | 字典类型（如 `customer_status`）|

**响应示例：**

```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "dictType": "customer_status",
      "dictCode": "active",
      "dictLabel": "正常跟进",
      "dictValue": "1",
      "sortOrder": 1,
      "status": 1
    }
  ]
}
```

> 前端下拉框/标签展示推荐通过此接口加载字典项，`dictLabel` 用于显示，`dictValue` 用于传参。

---

#### 6.2 查询字典详情

```
GET /api/sysDict/{id}
```

---

#### 6.3 分页查询

```
GET /api/sysDict/page
```

---

#### 6.4 新增字典

```
POST /api/sysDict
```

---

#### 6.5 修改字典

```
PUT /api/sysDict
```

---

#### 6.6 删除字典

```
DELETE /api/sysDict/{id}
```

---

#### SysDict 对象

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | ID |
| dictType | String | 字典类型分组（如 `customer_status`）|
| dictCode | String | 字典编码（英文 key）|
| dictLabel | String | 字典标签（中文显示名）|
| dictValue | String | 字典值（传参用）|
| sortOrder | Integer | 排序值 |
| status | Short | 状态：1=启用，0=禁用 |
| remark | String | 备注 |

---

### 7. 系统参数 `/api/sysParam`

#### 7.1 查询参数值（直接返回字符串）

```
GET /api/sysParam/value/{paramKey}
```

| 参数 | 类型 | 说明 |
|------|------|------|
| paramKey | String | 参数 Key |

**常用参数 Key：**

| paramKey | 含义 | 示例值 |
|---------|------|--------|
| `customer.public_sea_days` | 客户转公海天数 | `30` |

**响应：** `data: "30"`（String 类型）

---

#### 7.2 按 Key 查询参数对象

```
GET /api/sysParam/getByParamKey?paramKey={paramKey}
```

**响应：** SysParam 对象

---

#### 7.3 按分组查询参数列表

```
GET /api/sysParam/listByParamGroup?paramGroup={paramGroup}
```

---

#### 7.4 查询参数详情

```
GET /api/sysParam/{id}
```

---

#### 7.5 分页查询

```
GET /api/sysParam/page
```

---

#### 7.6 新增参数

```
POST /api/sysParam
```

---

#### 7.7 修改参数

```
PUT /api/sysParam
```

---

#### 7.8 删除参数

```
DELETE /api/sysParam/{id}
```

---

#### SysParam 对象

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | ID |
| paramKey | String | 参数 Key（唯一）|
| paramValue | String | 参数值 |
| paramType | String | 参数类型（string/number/boolean）|
| paramGroup | String | 参数分组 |
| remark | String | 备注 |
| sortOrder | Integer | 排序值 |
| status | Short | 状态：1=启用，0=禁用 |

---

### 8. 操作日志 `/api/sysOperationLog`（只读）

#### 8.1 分页查询操作日志

```
GET /api/sysOperationLog/page
```

---

#### 8.2 按用户查询操作日志

```
GET /api/sysOperationLog/listByUserId/{userId}
```

---

#### 8.3 按模块查询操作日志

```
GET /api/sysOperationLog/listByModule/{module}
```

---

#### SysOperationLog 对象

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | ID |
| userId | Long | 操作用户 ID |
| username | String | 操作用户名 |
| module | String | 模块名称（如 `客户管理`）|
| action | String | 操作名称（如 `新增客户`）|
| requestMethod | String | 方法名（Java 方法名）|
| requestUrl | String | 请求 URL |
| requestParams | String | 请求参数（JSON）|
| responseCode | String | 响应 code |
| errorMsg | String | 异常信息 |
| ip | String | 客户端 IP |
| costTimeMs | Long | 执行耗时（ms）|
| createdAt | Date | 记录时间 |

---

## 销售管理模块 (sales)

> 基础路径：`/api` | 端口：8083

---

### 9. 客户管理 `/api/customer`

#### 9.1 查询客户详情

```
GET /api/customer/{id}
```

**响应：** [Customer 对象](#customer-对象)

---

#### 9.2 分页查询客户列表

```
GET /api/customer/page
```

**响应：** PageResponse\<Customer\>

---

#### 9.3 按销售人员查询客户

```
GET /api/customer/listBySalesRepId/{salesRepId}
```

---

#### 9.4 按状态查询客户

```
GET /api/customer/listByStatus?status={status}
```

| status | 含义 |
|--------|------|
| 1 | 待跟进 |
| 2 | 跟进中 |
| 3 | 已成交 |
| 4 | 已放款 |
| 5 | 公海客户 |
| 6 | 无效客户 |

---

#### 9.5 新增客户

```
POST /api/customer
```

**请求体：** [Customer 对象](#customer-对象)

---

#### 9.6 修改客户

```
PUT /api/customer
```

---

#### 9.7 删除客户（逻辑删除）

```
DELETE /api/customer/{id}
```

---

#### Customer 对象

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 客户 ID |
| name | String | 客户姓名 |
| phone | String | 手机号 |
| idCard | String | 身份证号 |
| companyName | String | 企业名称 |
| companyLegalPerson | String | 法人姓名 |
| companyRegCapital | BigDecimal | 注册资本（万元）|
| customerType | Short | 客户类型：1=个人，2=企业 |
| salesRepId | Long | 跟进销售 ID |
| deptId | Long | 所属部门 ID |
| zoneId | Long | 所属战区 ID |
| intentionLevel | Short | 意向等级：1=A，2=B，3=C，4=D |
| status | Short | 客户状态（见上表）|
| lastContactDate | Date | 最后联系日期 |
| nextFollowUpDate | Date | 下次跟进日期 |
| publicSeaTime | Date | 进入公海时间 |
| publicSeaReason | String | 进入公海原因 |
| annotation | Object | 备注信息（JSON 自由格式）|
| source | String | 客户来源 |
| loanIntentionAmount | BigDecimal | 贷款意向金额（万元）|
| loanIntentionProduct | String | 意向贷款产品 |
| createdBy | Long | 创建人 ID |
| createdAt | Date | 创建时间 |
| updatedAt | Date | 更新时间 |

---

### 10. 跟进记录 `/api/contactRecord`

#### 10.1 查询跟进记录详情

```
GET /api/contactRecord/{id}
```

---

#### 10.2 分页查询

```
GET /api/contactRecord/page
```

---

#### 10.3 按客户查询跟进记录

```
GET /api/contactRecord/listByCustomerId/{customerId}
```

---

#### 10.4 按销售人员查询跟进记录

```
GET /api/contactRecord/listBySalesRepId/{salesRepId}
```

---

#### 10.5 新增跟进记录

```
POST /api/contactRecord
```

---

#### 10.6 修改跟进记录

```
PUT /api/contactRecord
```

---

#### 10.7 删除跟进记录

```
DELETE /api/contactRecord/{id}
```

---

#### ContactRecord 对象

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | ID |
| customerId | Long | 关联客户 ID |
| salesRepId | Long | 销售人员 ID |
| contactType | Short | 联系方式：1=电话，2=微信，3=上门，4=其他 |
| contactDate | Date | 联系日期 |
| content | String | 跟进内容 |
| intentionBefore | Short | 跟进前意向等级（1=A,2=B,3=C,4=D）|
| intentionAfter | Short | 跟进后意向等级（1=A,2=B,3=C,4=D）|
| followUpDate | Date | 下次跟进日期 |
| attachmentUrls | String | 附件 URL 列表（JSON 数组字符串）|
| createdAt | Date | 创建时间 |

---

### 11. 客户转让记录 `/api/customerTransferLog`

#### 11.1 查询转让记录详情

```
GET /api/customerTransferLog/{id}
```

---

#### 11.2 分页查询

```
GET /api/customerTransferLog/page
```

---

#### 11.3 按客户查询转让记录

```
GET /api/customerTransferLog/listByCustomerId/{customerId}
```

---

#### 11.4 新增转让记录

```
POST /api/customerTransferLog
```

---

#### 11.5 修改转让记录

```
PUT /api/customerTransferLog
```

---

#### 11.6 删除转让记录

```
DELETE /api/customerTransferLog/{id}
```

---

#### CustomerTransferLog 对象

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | ID |
| customerId | Long | 客户 ID |
| fromRepId | Long | 转让前销售 ID |
| toRepId | Long | 接收销售 ID |
| operateType | String | 操作类型：`TRANSFER`=主动转让，`PUBLIC_SEA`=转公海，`CLAIM`=领取 |
| reason | String | 转让原因 |
| operatedBy | Long | 操作人 ID |
| operatedAt | Date | 操作时间 |

---

### 12. 合同管理 `/api/contract`

#### 12.1 查询合同详情

```
GET /api/contract/{id}
```

**响应：** [Contract 对象](#contract-对象)

---

#### 12.2 按合同编号查询

```
GET /api/contract/getByContractNo/{contractNo}
```

---

#### 12.3 分页查询合同列表

```
GET /api/contract/page
```

---

#### 12.4 按销售人员查询合同

```
GET /api/contract/listBySalesRepId/{salesRepId}
```

---

#### 12.5 按状态查询合同

```
GET /api/contract/listByStatus?status={status}
```

| status | 含义 |
|--------|------|
| 1 | 待签署 |
| 2 | 已签署 |
| 3 | 审核中 |
| 4 | 已发送金融部 |
| 5 | 银行审核中 |
| 6 | 审核通过 |
| 7 | 已放款 |
| 8 | 已拒绝 |

---

#### 12.6 新增合同

```
POST /api/contract
```

---

#### 12.7 修改合同

```
PUT /api/contract
```

---

#### 12.8 删除合同

```
DELETE /api/contract/{id}
```

---

#### 12.9 合同签署（核心业务接口）

```
POST /api/contract/{id}/sign
```

**说明：** 签署合同，将合同状态从 1（待签署）变为 2（已签署），并通过消息队列通知金融部创建贷款审核任务。

**前提：** 合同当前 status 必须为 1

**副作用：**
1. 合同 status 变为 2
2. 发送 MQ 事件至金融服务
3. 金融服务自动创建 LoanAudit 记录（audit_status=1 待接收）

**响应：** `data: null`（成功表示签署成功，金融任务已触发）

> **注意：** 并发场景下前端需防止重复提交（如按钮 loading 状态）

---

#### Contract 对象

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 合同 ID |
| contractNo | String | 合同编号（唯一）|
| customerId | Long | 关联客户 ID |
| salesRepId | Long | 销售人员 ID |
| deptId | Long | 所属部门 ID |
| productId | Long | 贷款产品 ID |
| zoneId | Long | 所属战区 ID |
| contractAmount | BigDecimal | 合同金额（元）|
| actualLoanAmount | BigDecimal | 实际放款金额（元）|
| serviceFeeRate | BigDecimal | 服务费率（百分比，如 1.5）|
| serviceFee1 | BigDecimal | 首期服务费金额 |
| serviceFee2 | BigDecimal | 二期服务费金额 |
| serviceFee1Paid | Short | 首期服务费支付状态：0=未付，1=已付 |
| serviceFee2Paid | Short | 二期服务费支付状态：0=未付，1=已付 |
| serviceFee1PayDate | Date | 首期服务费支付日期 |
| serviceFee2PayDate | Date | 二期服务费支付日期 |
| status | Short | 合同状态（见上表）|
| signDate | Date | 签署日期 |
| paperContractNo | String | 纸质合同编号 |
| financeSendTime | Date | 发送金融部时间 |
| financeReceiveTime | Date | 金融部接收时间 |
| loanUse | String | 贷款用途 |
| guaranteeInfo | String | 担保信息 |
| rejectReason | String | 拒绝原因 |
| remark | String | 备注 |
| createdAt | Date | 创建时间 |
| updatedAt | Date | 更新时间 |

---

### 13. 合同附件 `/api/contractAttachment`

#### 13.1 按合同查询附件列表

```
GET /api/contractAttachment/listByContractId/{contractId}
```

**响应：** ContractAttachment 数组

---

#### 13.2 查询附件详情

```
GET /api/contractAttachment/{id}
```

---

#### 13.3 分页查询

```
GET /api/contractAttachment/page
```

---

#### 13.4 新增附件记录

```
POST /api/contractAttachment
```

---

#### 13.5 修改附件记录

```
PUT /api/contractAttachment
```

---

#### 13.6 删除附件记录

```
DELETE /api/contractAttachment/{id}
```

---

#### ContractAttachment 对象

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | ID |
| contractId | Long | 关联合同 ID |
| attachmentType | String | 附件类型（如 `ID_CARD`、`BUSINESS_LICENSE`）|
| fileUrl | String | 文件 URL |
| fileName | String | 文件原始名称 |
| fileSize | Long | 文件大小（字节）|
| fileMd5 | String | 文件 MD5（用于去重）|
| uploadBy | Long | 上传人 ID |
| uploadTime | Date | 上传时间 |

---

### 14. 业绩记录 `/api/performanceRecord`

#### 14.1 查询业绩详情

```
GET /api/performanceRecord/{id}
```

---

#### 14.2 分页查询

```
GET /api/performanceRecord/page
```

---

#### 14.3 按销售人员查询

```
GET /api/performanceRecord/listBySalesRepId/{salesRepId}
```

---

#### PerformanceRecord 对象

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | ID |
| contractId | Long | 关联合同 ID |
| salesRepId | Long | 销售人员 ID |
| deptId | Long | 部门 ID |
| zoneId | Long | 战区 ID |
| contractAmount | BigDecimal | 合同金额 |
| commissionRate | BigDecimal | 提成比例 |
| commissionAmount | BigDecimal | 提成金额 |
| status | Short | 状态：0=待计算，1=已计算，2=已确认，3=已发放，4=已取消 |
| calculateTime | Date | 计算时间 |
| confirmTime | Date | 确认时间 |
| grantTime | Date | 发放时间 |
| cancelReason | String | 取消原因 |
| remark | String | 备注 |

> **说明：** 此数据由金融服务在放款审批通过（`approve()`）后自动调用内部接口创建，前端通常只需查询。

---

### 15. 工作日志 `/api/workLog`

#### 15.1 查询工作日志详情

```
GET /api/workLog/{id}
```

---

#### 15.2 分页查询

```
GET /api/workLog/page
```

---

#### 15.3 按销售人员查询

```
GET /api/workLog/listBySalesRepId/{salesRepId}
```

---

#### 15.4 检查是否重复提交

```
GET /api/workLog/checkDuplicate?salesRepId={salesRepId}&logDate={logDate}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| salesRepId | Long | 是 | 销售人员 ID |
| logDate | String | 是 | 日期，格式 `yyyy-MM-dd` |

**响应：** `data: true`（true=已存在，false=未填写）

---

#### 15.5 新增工作日志

```
POST /api/workLog
```

---

#### 15.6 修改工作日志

```
PUT /api/workLog
```

---

#### 15.7 删除工作日志

```
DELETE /api/workLog/{id}
```

---

#### WorkLog 对象

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | ID |
| salesRepId | Long | 销售人员 ID |
| logDate | Date | 日报日期 |
| callsMade | Integer | 拨打电话数 |
| effectiveCalls | Integer | 有效通话数 |
| newIntentions | Integer | 新增意向客户数 |
| intentionClients | Integer | 意向客户拜访数 |
| faceToFaceClients | Integer | 面谈客户数 |
| signedContracts | Integer | 签约数 |
| content | String | 日报内容 |
| createdAt | Date | 创建时间 |

---

## 金融审核模块 (finance)

> 基础路径：`/api` | 端口：8084

---

### 16. 银行管理 `/api/bank`

#### 16.1 查询银行详情

```
GET /api/bank/{id}
```

---

#### 16.2 分页查询

```
GET /api/bank/page
```

---

#### 16.3 按状态查询

```
GET /api/bank/listByStatus?status={status}
```

---

#### 16.4 新增银行

```
POST /api/bank
```

---

#### 16.5 修改银行

```
PUT /api/bank
```

---

#### 16.6 删除银行

```
DELETE /api/bank/{id}
```

---

#### Bank 对象

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | ID |
| bankCode | String | 银行编码 |
| bankName | String | 银行名称 |
| bankBranch | String | 支行名称 |
| contactPerson | String | 银行联系人 |
| contactPhone | String | 联系电话 |
| status | Short | 状态：1=启用，0=禁用 |
| sortOrder | Integer | 排序值 |

---

### 17. 金融产品 `/api/financeProduct`

#### 17.1 查询金融产品详情

```
GET /api/financeProduct/{id}
```

---

#### 17.2 分页查询

```
GET /api/financeProduct/page
```

---

#### 17.3 按银行查询产品

```
GET /api/financeProduct/listByBankId/{bankId}
```

---

#### 17.4 按状态查询产品

```
GET /api/financeProduct/listByStatus?status={status}
```

---

#### 17.5 新增产品

```
POST /api/financeProduct
```

---

#### 17.6 修改产品

```
PUT /api/financeProduct
```

---

#### 17.7 删除产品

```
DELETE /api/financeProduct/{id}
```

---

#### FinanceProduct 对象

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | ID |
| productCode | String | 产品编码 |
| productName | String | 产品名称 |
| bankId | Long | 所属银行 ID |
| minAmount | BigDecimal | 最小贷款金额（元）|
| maxAmount | BigDecimal | 最大贷款金额（元）|
| interestRate | BigDecimal | 贷款利率（年化，如 0.048=4.8%）|
| minTerm | Integer | 最短贷款期限（月）|
| maxTerm | Integer | 最长贷款期限（月）|
| requirements | Object | 申请要求（JSON）|
| documents | Object | 所需材料（JSON）|
| productFeatures | String | 产品特色描述 |
| commissionRate | BigDecimal | 提成比例（如 0.02=2%）|
| status | Short | 状态：1=上线，0=下线 |
| onlineTime | Date | 上线时间 |
| offlineTime | Date | 下线时间 |

---

### 18. 贷款审核 `/api/loanAudit`

#### 18.1 贷款审核状态机说明

```
合同签署（sales 服务）
     ↓ [MQ 事件触发]
 1: 待接收 ──────────────────────────────────────────────────────────────┐
     ↓ [POST /{id}/receive]                                               │
 2: 初审中                                                                │
     ↓ [POST /{id}/review]                                                │ [POST /{id}/reject]
 3: 已提交银行                                                            │ (任意状态)
     ↓ [POST /{id}/submit-bank]                                           │
 4: 银行审核中                                                            │
     ├── approved=true → [POST /{id}/bank-result] → 6: 终审通过待放款    ↓
     └── approved=false → [POST /{id}/bank-result] → 5: 银行拒绝 → 7: 终审拒绝（终态）
                                                            ↓
                                                  6: 终审通过待放款
                                                            ↓ [POST /{id}/approve]
                                                     放款完成（合同 status→7）
```

---

#### 18.2 查询贷款审核详情

```
GET /api/loanAudit/{id}
```

**响应：** [LoanAudit 对象](#loanaudit-对象)

---

#### 18.3 按合同 ID 查询

```
GET /api/loanAudit/getByContractId/{contractId}
```

---

#### 18.4 分页查询

```
GET /api/loanAudit/page
```

---

#### 18.5 按金融专员查询

```
GET /api/loanAudit/listByFinanceSpecialistId/{financeSpecialistId}
```

---

#### 18.6 接收审核（1→2）

```
POST /api/loanAudit/{id}/receive
```

**前提：** auditStatus=1

**请求体：**

```json
{
  "operatorId": 2001,
  "operatorName": "张三",
  "operatorRole": "金融专员",
  "comment": "已确认接收"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| operatorId | Long | 是 | 操作人 ID |
| operatorName | String | 是 | 操作人姓名 |
| operatorRole | String | 是 | 操作人角色名称 |
| comment | String | 否 | 操作备注 |

---

#### 18.7 初审（2→3）

```
POST /api/loanAudit/{id}/review
```

**前提：** auditStatus=2

**请求体：** 同 18.6（可含初审意见 comment）

---

#### 18.8 提交银行（3→4）

```
POST /api/loanAudit/{id}/submit-bank
```

**前提：** auditStatus=3

**请求体：**

```json
{
  "bankId": 101,
  "operatorId": 2001,
  "operatorName": "张三",
  "operatorRole": "金融专员",
  "comment": "已提交工商银行审核"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| bankId | Long | 是 | 提交目标银行 ID |
| operatorId | Long | 是 | 操作人 ID |
| operatorName | String | 是 | 操作人姓名 |
| operatorRole | String | 是 | 操作人角色 |
| comment | String | 否 | 备注 |

---

#### 18.9 录入银行反馈（4→5 或 4→6）

```
POST /api/loanAudit/{id}/bank-result
```

**前提：** auditStatus=4

**请求体：**

```json
{
  "approved": true,
  "bankFeedbackContent": "银行已批准，批准金额50万",
  "operatorId": 2001,
  "operatorName": "张三",
  "operatorRole": "金融主管",
  "comment": ""
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| approved | Boolean | 是 | `true`=银行通过（→6），`false`=银行拒绝（→5）|
| bankFeedbackContent | String | 否 | 银行反馈内容 |
| operatorId | Long | 是 | 操作人 ID |
| operatorName | String | 是 | 操作人姓名 |
| operatorRole | String | 是 | 操作人角色 |
| comment | String | 否 | 备注 |

---

#### 18.10 终审放款（6→放款完成）

```
POST /api/loanAudit/{id}/approve
```

**前提：** auditStatus=6

**请求体：**

```json
{
  "actualLoanAmount": 480000.00,
  "actualInterestRate": 0.048,
  "loanGrantedDate": 1713081600000,
  "operatorId": 2002,
  "operatorName": "李四",
  "operatorRole": "金融总监",
  "comment": "终审通过，已放款"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| actualLoanAmount | BigDecimal | 否 | 实际放款金额（元）|
| actualInterestRate | BigDecimal | 否 | 实际利率 |
| loanGrantedDate | Long | 否 | 放款日期（Unix 时间戳毫秒）|
| operatorId | Long | 是 | 操作人 ID |
| operatorName | String | 是 | 操作人姓名 |
| operatorRole | String | 是 | 操作人角色 |
| comment | String | 否 | 备注 |

**副作用（自动触发，前端无需额外操作）：**
1. 在销售服务创建业绩记录（`/sales/internal/performances/create`）
2. 销售服务合同 status 更新为 7（已放款）
3. 创建操作轨迹记录

---

#### 18.11 拒绝（→7）

```
POST /api/loanAudit/{id}/reject
```

**前提：** auditStatus ∈ {4, 5, 7}（可在银行审核中/银行拒绝/已驳回时再次拒绝）

**请求体：**

```json
{
  "operatorId": 2001,
  "operatorName": "张三",
  "operatorRole": "金融主管",
  "comment": "客户资质不符合要求"
}
```

---

#### 18.12 新增贷款审核记录（手动创建）

```
POST /api/loanAudit
```

> 通常由系统自动创建（合同签署事件触发），手动创建仅用于特殊场景。

---

#### 18.13 修改贷款审核记录

```
PUT /api/loanAudit
```

---

#### 18.14 删除贷款审核记录

```
DELETE /api/loanAudit/{id}
```

---

#### LoanAudit 对象

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | ID |
| contractId | Long | 关联合同 ID |
| financeSpecialistId | Long | 负责金融专员 ID |
| recommendedProductId | Long | 推荐产品 ID |
| approvedAmount | BigDecimal | 批准金额（元）|
| approvedTerm | Integer | 批准期限（月）|
| approvedInterestRate | BigDecimal | 批准利率 |
| auditStatus | Short | 审核状态（见状态机）|
| bankId | Long | 提交银行 ID |
| bankAuditStatus | String | 银行内部审核状态描述 |
| bankApplyTime | Date | 银行申请时间 |
| bankFeedbackTime | Date | 银行反馈时间 |
| bankFeedbackContent | String | 银行反馈内容 |
| rejectReason | String | 拒绝原因 |
| auditOpinion | String | 审核意见 |
| auditDate | Date | 审核日期 |
| loanGrantedDate | Date | 放款日期 |
| actualLoanAmount | BigDecimal | 实际放款金额 |
| actualInterestRate | BigDecimal | 实际利率 |
| createdAt | Date | 创建时间 |
| updatedAt | Date | 更新时间 |

**auditStatus 枚举：**

| 值 | 含义 | 是否终态 |
|----|------|---------|
| 1 | 待接收 | 否 |
| 2 | 初审中 | 否 |
| 3 | 已提交银行 | 否 |
| 4 | 银行审核中 | 否 |
| 5 | 银行拒绝 | 否（可再次提交）|
| 6 | 终审通过（待放款）| 否 |
| 7 | 终审拒绝 | **是** |

> 放款完成无单独的 `auditStatus`，通过合同 `status=7` 体现。

---

### 19. 审核操作轨迹 `/api/loanAuditRecord`（只读为主）

#### 19.1 按审核单查询操作轨迹

```
GET /api/loanAuditRecord/listByLoanAuditId/{loanAuditId}
```

**响应：** LoanAuditRecord 数组（按时间升序）

---

#### 19.2 分页查询

```
GET /api/loanAuditRecord/page
```

---

#### LoanAuditRecord 对象

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | ID |
| loanAuditId | Long | 关联贷款审核 ID |
| operatorId | Long | 操作人 ID（0=系统自动）|
| operatorName | String | 操作人姓名 |
| operatorRole | String | 操作人角色 |
| action | String | 操作类型（如 `receive`/`review`/`approve`/`reject`）|
| content | String | 操作内容/备注 |
| attachmentUrls | String | 附件 URL（JSON 数组字符串）|
| createdAt | Date | 操作时间 |

---

### 20. 提成记录 `/api/commissionRecord`

#### 20.1 查询提成记录详情

```
GET /api/commissionRecord/{id}
```

---

#### 20.2 分页查询

```
GET /api/commissionRecord/page
```

---

#### 20.3 按销售人员查询

```
GET /api/commissionRecord/listBySalesRepId/{salesRepId}
```

---

#### 20.4 确认提成

```
POST /api/commissionRecord/{id}/confirm
```

**说明：** 财务确认提成数据无误，状态由待确认→已确认。

**响应：** `data: null`

---

#### 20.5 发放提成

```
POST /api/commissionRecord/{id}/grant
```

**说明：** ⚠️ 当前版本框架已就绪，具体转账逻辑待完善。接口可调用但实际付款流程不完整。

**请求体：**

```json
{
  "grantAccount": "6222000000000001",
  "remark": "2026年4月提成发放"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| grantAccount | String | 是 | 打款目标账户 |
| remark | String | 否 | 备注 |

---

#### CommissionRecord 对象

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | ID |
| performanceId | Long | 关联业绩记录 ID |
| salesRepId | Long | 销售人员 ID |
| contractId | Long | 关联合同 ID |
| commissionAmount | BigDecimal | 提成金额（元）|
| commissionRate | BigDecimal | 提成比例 |
| status | Short | 状态：0=待确认，1=已确认，2=已发放，3=已取消 |
| confirmTime | Date | 确认时间 |
| grantTime | Date | 发放时间 |
| grantAccount | String | 打款账户 |
| remark | String | 备注 |

---

### 21. 服务费记录 `/api/serviceFeeRecord`

#### 21.1 查询服务费记录详情

```
GET /api/serviceFeeRecord/{id}
```

---

#### 21.2 分页查询

```
GET /api/serviceFeeRecord/page
```

---

#### 21.3 按合同查询服务费记录

```
GET /api/serviceFeeRecord/listByContractId/{contractId}
```

---

#### 21.4 确认收款

```
PUT /api/serviceFeeRecord/{id}/pay
```

**说明：** ⚠️ 当前版本框架已就绪，具体支付核验流程待完善。

**请求体：**

```json
{
  "paymentMethod": "银行转账",
  "paymentAccount": "6222000000000001",
  "receiptNo": "RECEIPT20260414001",
  "remark": "已收到首期服务费"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| paymentMethod | String | 是 | 支付方式（银行转账/现金/微信/支付宝）|
| paymentAccount | String | 是 | 付款账户 |
| receiptNo | String | 是 | 收据编号 |
| remark | String | 否 | 备注 |

---

#### ServiceFeeRecord 对象

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | ID |
| contractId | Long | 关联合同 ID |
| feeType | Short | 服务费类型：1=首期服务费，2=二期服务费 |
| amount | BigDecimal | 实收金额（元）|
| shouldAmount | BigDecimal | 应收金额（元）|
| paymentMethod | String | 支付方式 |
| paymentStatus | Short | 收款状态：0=待收款，1=已收款，2=已退款 |
| paymentDate | Date | 收款日期 |
| paymentAccount | String | 付款账户 |
| receiptNo | String | 收据编号 |
| accountantId | Long | 经办会计 ID |
| remark | String | 备注 |

---

## 状态码枚举速查表

### 用户状态 (SysUser.status)

| 值 | 含义 |
|----|------|
| 0 | 禁用 |
| 1 | 正常 |

### 角色数据范围 (SysRole.dataScope)

| 值 | 含义 |
|----|------|
| 1 | 全部数据 |
| 2 | 本战区数据 |
| 3 | 本部门数据 |
| 4 | 仅本人数据 |

### 权限类型 (SysPermission.permType)

| 值 | 含义 |
|----|------|
| 1 | 目录 |
| 2 | 菜单 |
| 3 | 按钮 |

### 客户状态 (Customer.status)

| 值 | 含义 |
|----|------|
| 1 | 待跟进 |
| 2 | 跟进中 |
| 3 | 已成交 |
| 4 | 已放款 |
| 5 | 公海客户 |
| 6 | 无效客户 |

### 客户意向等级 (Customer.intentionLevel)

| 值 | 含义 |
|----|------|
| 1 | A 级（高意向）|
| 2 | B 级 |
| 3 | C 级 |
| 4 | D 级（低意向）|

### 客户类型 (Customer.customerType)

| 值 | 含义 |
|----|------|
| 1 | 个人客户 |
| 2 | 企业客户 |

### 联系方式类型 (ContactRecord.contactType)

| 值 | 含义 |
|----|------|
| 1 | 电话 |
| 2 | 微信 |
| 3 | 上门拜访 |
| 4 | 其他 |

### 合同状态 (Contract.status)

| 值 | 含义 |
|----|------|
| 1 | 待签署 |
| 2 | 已签署 |
| 3 | 审核中 |
| 4 | 已发送金融部 |
| 5 | 银行审核中 |
| 6 | 审核通过 |
| 7 | 已放款 |
| 8 | 已拒绝 |

### 贷款审核状态 (LoanAudit.auditStatus)

| 值 | 含义 | 操作接口 |
|----|------|---------|
| 1 | 待接收 | `POST /{id}/receive` |
| 2 | 初审中 | `POST /{id}/review` |
| 3 | 已提交银行 | `POST /{id}/submit-bank` |
| 4 | 银行审核中 | `POST /{id}/bank-result` |
| 5 | 银行拒绝 | `POST /{id}/reject` |
| 6 | 终审通过（待放款）| `POST /{id}/approve` |
| 7 | 终审拒绝（终态）| — |

### 提成记录状态 (CommissionRecord.status)

| 值 | 含义 |
|----|------|
| 0 | 待确认 |
| 1 | 已确认 |
| 2 | 已发放 |
| 3 | 已取消 |

### 服务费类型 (ServiceFeeRecord.feeType)

| 值 | 含义 |
|----|------|
| 1 | 首期服务费 |
| 2 | 二期服务费 |

### 服务费收款状态 (ServiceFeeRecord.paymentStatus)

| 值 | 含义 |
|----|------|
| 0 | 待收款 |
| 1 | 已收款 |
| 2 | 已退款 |

### 业绩记录状态 (PerformanceRecord.status)

| 值 | 含义 |
|----|------|
| 0 | 待计算 |
| 1 | 已计算 |
| 2 | 已确认 |
| 3 | 已发放 |
| 4 | 已取消 |

---

## 附录：已知限制与注意事项

| 编号 | 说明 | 影响范围 |
|------|------|---------|
| L-01 | `POST /api/contract/{id}/sign` 无并发幂等保护，前端需防重复点击 | 合同签署页面 |
| L-02 | `POST /api/commissionRecord/{id}/grant` 实际转账逻辑未完成，接口可调用但不产生实际付款 | 提成发放功能 |
| L-03 | `PUT /api/serviceFeeRecord/{id}/pay` 支付核验流程未完成 | 服务费收款功能 |
| L-04 | 数据权限过滤（按战区/部门/本人）基础设施已就绪，但 DAO 层尚未接入，当前所有用户查询全量数据 | 所有列表查询 |
| L-05 | `/api/sysUser/dev/reset-password` 为调试接口，生产上线前需删除 | 安全 |
| L-06 | Redis 缓存无 TTL 配置，字典和参数缓存永不自动过期 | system 模块 |

---

*文档版本：v1.0.0 | 生成时间：2026-04-14*
