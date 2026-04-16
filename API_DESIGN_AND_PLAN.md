# NeoCC 系统业务模块接口设计方案

**文档版本**: v1.0  
**生成时间**: 2026-04-16  
**状态**: 📝 设计方案  

---

## 📋 目录

1. [系统管理模块](#1-系统管理模块)
2. [销售管理模块](#2-销售管理模块)
3. [财务管理模块](#3-财务管理模块)
4. [认证模块增强](#4-认证模块增强)
5. [跨服务调用设计](#5-跨服务调用设计)

---

## 1. 系统管理模块

**服务**: system-service  
**数据库**: dafuweng_system  
**基础路径**: `/api/system`

### 1.1 战区管理 (Zone)

#### 接口列表

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/system/zone/list` | 获取战区列表 | system:zone:list |
| GET | `/api/system/zone/{id}` | 获取战区详情 | system:zone:query |
| POST | `/api/system/zone` | 创建战区 | system:zone:add |
| PUT | `/api/system/zone` | 更新战区 | system:zone:edit |
| DELETE | `/api/system/zone/{ids}` | 删除战区 | system:zone:remove |

#### 接口详情

**1. 获取战区列表**
```http
GET /api/system/zone/list?pageNum=1&pageSize=10&zoneName=战区A&status=1

Response:
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 2,
    "rows": [
      {
        "id": 1,
        "zoneCode": "ZONE_A",
        "zoneName": "战区A",
        "directorId": 1,
        "directorName": "张三",
        "sortOrder": 1,
        "status": 1,
        "createTime": "2026-04-16 10:00:00"
      }
    ]
  }
}
```

**2. 创建战区**
```http
POST /api/system/zone
Content-Type: application/json

{
  "zoneCode": "ZONE_C",
  "zoneName": "战区C",
  "directorId": 3,
  "sortOrder": 3,
  "status": 1
}

Response:
{
  "code": 200,
  "message": "创建成功"
}
```

---

### 1.2 部门管理 (Department)

#### 接口列表

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/system/dept/list` | 获取部门树 | system:dept:list |
| GET | `/api/system/dept/{id}` | 获取部门详情 | system:dept:query |
| POST | `/api/system/dept` | 创建部门 | system:dept:add |
| PUT | `/api/system/dept` | 更新部门 | system:dept:edit |
| DELETE | `/api/system/dept/{id}` | 删除部门 | system:dept:remove |

#### 接口详情

**1. 获取部门树**
```http
GET /api/system/dept/list?zoneId=1&deptName=销售部

Response:
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "deptCode": "DEPT_SALES_01",
      "deptName": "销售一部",
      "zoneId": 1,
      "zoneName": "战区A",
      "parentId": 0,
      "managerId": 5,
      "managerName": "销售代表1",
      "sortOrder": 1,
      "status": 1,
      "children": []
    }
  ]
}
```

---

### 1.3 用户管理 (User)

#### 接口列表

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/system/user/list` | 获取用户列表 | system:user:list |
| GET | `/api/system/user/{id}` | 获取用户详情 | system:user:query |
| POST | `/api/system/user` | 创建用户 | system:user:add |
| PUT | `/api/system/user` | 更新用户 | system:user:edit |
| DELETE | `/api/system/user/{ids}` | 删除用户 | system:user:remove |
| PUT | `/api/system/user/resetPwd` | 重置密码 | system:user:resetPwd |
| PUT | `/api/system/user/changeStatus` | 修改状态 | system:user:edit |
| GET | `/api/system/user/profile` | 获取个人信息 | - |
| PUT | `/api/system/user/profile` | 更新个人信息 | - |

#### 接口详情

**1. 获取用户列表**
```http
GET /api/system/user/list?pageNum=1&pageSize=10&username=admin&status=1&deptId=1

Response:
{
  "code": 200,
  "data": {
    "total": 10,
    "rows": [
      {
        "id": 1,
        "username": "admin",
        "realName": "系统管理员",
        "phone": "13800138000",
        "email": "admin@test.com",
        "deptId": 1,
        "deptName": "销售一部",
        "zoneId": 1,
        "zoneName": "战区A",
        "status": 1,
        "createTime": "2026-04-16 10:00:00"
      }
    ]
  }
}
```

**2. 创建用户**
```http
POST /api/system/user
Content-Type: application/json

{
  "username": "test_user",
  "password": "test123",
  "realName": "测试用户",
  "phone": "13800138001",
  "email": "test@test.com",
  "deptId": 1,
  "zoneId": 1,
  "roleIds": [1, 2],
  "status": 1
}

Response:
{
  "code": 200,
  "message": "创建成功"
}
```

---

### 1.4 角色管理 (Role)

#### 接口列表

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/system/role/list` | 获取角色列表 | system:role:list |
| GET | `/api/system/role/{id}` | 获取角色详情 | system:role:query |
| POST | `/api/system/role` | 创建角色 | system:role:add |
| PUT | `/api/system/role` | 更新角色 | system:role:edit |
| DELETE | `/api/system/role/{ids}` | 删除角色 | system:role:remove |
| PUT | `/api/system/role/dataScope` | 修改数据权限 | system:role:edit |
| GET | `/api/system/role/authUser/{roleId}` | 获取角色用户列表 | system:role:list |
| PUT | `/api/system/role/authUser` | 给用户分配角色 | system:role:edit |

---

### 1.5 数据字典 (Dict)

#### 接口列表

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/system/dict/type/list` | 获取字典类型列表 | system:dict:list |
| GET | `/api/system/dict/type/{dictType}` | 获取字典类型 | system:dict:query |
| POST | `/api/system/dict/type` | 创建字典类型 | system:dict:add |
| PUT | `/api/system/dict/type` | 更新字典类型 | system:dict:edit |
| DELETE | `/api/system/dict/type/{ids}` | 删除字典类型 | system:dict:remove |
| GET | `/api/system/dict/data/list` | 获取字典数据列表 | system:dict:list |
| GET | `/api/system/dict/data/type/{dictType}` | 根据类型获取数据 | - |
| POST | `/api/system/dict/data` | 创建字典数据 | system:dict:add |
| PUT | `/api/system/dict/data` | 更新字典数据 | system:dict:edit |
| DELETE | `/api/system/dict/data/{ids}` | 删除字典数据 | system:dict:remove |

---

### 1.6 系统参数 (Param)

#### 接口列表

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/system/param/list` | 获取参数列表 | system:param:list |
| GET | `/api/system/param/{paramKey}` | 获取参数值 | system:param:query |
| POST | `/api/system/param` | 创建参数 | system:param:add |
| PUT | `/api/system/param` | 更新参数 | system:param:edit |
| DELETE | `/api/system/param/{ids}` | 删除参数 | system:param:remove |

---

### 1.7 操作日志 (Operation Log)

#### 接口列表

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/system/operlog/list` | 获取日志列表 | system:operlog:list |
| GET | `/api/system/operlog/{id}` | 获取日志详情 | system:operlog:query |
| DELETE | `/api/system/operlog/{ids}` | 删除日志 | system:operlog:remove |
| DELETE | `/api/system/operlog/clean` | 清空日志 | system:operlog:remove |

---

## 2. 销售管理模块

**服务**: sales-service  
**数据库**: dafuweng_sales  
**基础路径**: `/api/sales`

### 2.1 客户管理 (Customer)

#### 接口列表

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/sales/customer/list` | 获取客户列表 | sales:customer:list |
| GET | `/api/sales/customer/{id}` | 获取客户详情 | sales:customer:query |
| POST | `/api/sales/customer` | 创建客户 | sales:customer:add |
| PUT | `/api/sales/customer` | 更新客户 | sales:customer:edit |
| DELETE | `/api/sales/customer/{ids}` | 删除客户 | sales:customer:remove |
| PUT | `/api/sales/customer/transfer` | 转移客户 | sales:customer:transfer |
| PUT | `/api/sales/customer/pickup` | 公海领取 | sales:customer:pickup |
| GET | `/api/sales/customer/public/list` | 公海客户列表 | sales:customer:list |

#### 接口详情

**1. 获取客户列表**
```http
GET /api/sales/customer/list?pageNum=1&pageSize=10&name=张三&phone=138&salesRepId=5&status=1

Response:
{
  "code": 200,
  "data": {
    "total": 5,
    "rows": [
      {
        "id": 1,
        "name": "测试客户A公司",
        "phone": "13900001001",
        "companyName": "A科技有限公司",
        "customerType": 1,
        "salesRepId": 5,
        "salesRepName": "销售代表1",
        "deptId": 3,
        "zoneId": 1,
        "intentionLevel": 2,
        "status": 1,
        "lastContactDate": "2026-04-15",
        "nextFollowUp": "2026-04-18",
        "createTime": "2026-04-16 10:00:00"
      }
    ]
  }
}
```

**2. 创建客户**
```http
POST /api/sales/customer
Content-Type: application/json

{
  "name": "新客户公司",
  "phone": "13900001010",
  "companyName": "新科技有限公司",
  "customerType": 1,
  "intentionLevel": 3,
  "source": "网络推广"
}

Response:
{
  "code": 200,
  "message": "创建成功"
}
```

**3. 转移客户**
```http
PUT /api/sales/customer/transfer
Content-Type: application/json

{
  "customerId": 1,
  "toRepId": 6,
  "reason": "客户重新分配"
}

Response:
{
  "code": 200,
  "message": "转移成功"
}
```

---

### 2.2 跟进记录 (Contact Record)

#### 接口列表

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/sales/contact/list` | 获取跟进记录 | sales:contact:list |
| GET | `/api/sales/contact/{id}` | 获取记录详情 | sales:contact:query |
| POST | `/api/sales/contact` | 创建跟进记录 | sales:contact:add |
| PUT | `/api/sales/contact` | 更新跟进记录 | sales:contact:edit |
| DELETE | `/api/sales/contact/{ids}` | 删除记录 | sales:contact:remove |

#### 接口详情

**1. 创建跟进记录**
```http
POST /api/sales/contact
Content-Type: application/json

{
  "customerId": 1,
  "contactType": 1,
  "contactDate": "2026-04-16 14:00:00",
  "content": "电话沟通，客户确认贷款需求",
  "intentionAfter": 2,
  "nextFollowUp": "2026-04-19"
}

Response:
{
  "code": 200,
  "message": "记录成功"
}
```

---

### 2.3 合同管理 (Contract)

#### 接口列表

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/sales/contract/list` | 获取合同列表 | sales:contract:list |
| GET | `/api/sales/contract/{id}` | 获取合同详情 | sales:contract:query |
| POST | `/api/sales/contract` | 创建合同 | sales:contract:add |
| PUT | `/api/sales/contract` | 更新合同 | sales:contract:edit |
| DELETE | `/api/sales/contract/{ids}` | 删除合同 | sales:contract:remove |
| PUT | `/api/sales/contract/sign` | 签署合同 | sales:contract:sign |
| GET | `/api/sales/contract/{id}/attachments` | 获取合同附件 | sales:contract:query |
| POST | `/api/sales/contract/{id}/attachment` | 上传附件 | sales:contract:upload |

#### 接口详情

**1. 创建合同**
```http
POST /api/sales/contract
Content-Type: application/json

{
  "customerId": 1,
  "productName": "流动资金贷款",
  "contractAmount": 500000.00,
  "serviceFee1": 15000.00,
  "serviceFee2": 5000.00,
  "signDate": "2026-04-16",
  "startDate": "2026-04-16",
  "endDate": "2027-04-16"
}

Response:
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "contractNo": "HT2026040003"
  }
}
```

---

### 2.4 工作日志 (Work Log)

#### 接口列表

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/sales/worklog/list` | 获取工作日志 | sales:worklog:list |
| GET | `/api/sales/worklog/{id}` | 获取日志详情 | sales:worklog:query |
| POST | `/api/sales/worklog` | 创建工作日志 | sales:worklog:add |
| PUT | `/api/sales/worklog` | 更新工作日志 | sales:worklog:edit |
| DELETE | `/api/sales/worklog/{ids}` | 删除日志 | sales:worklog:remove |

#### 接口详情

**1. 创建工作日志**
```http
POST /api/sales/worklog
Content-Type: application/json

{
  "logDate": "2026-04-16",
  "callsMade": 20,
  "effectiveCalls": 15,
  "newIntentions": 3,
  "signedContracts": 1,
  "summary": "今日完成20通电话，新增3个意向客户，签约1个合同"
}

Response:
{
  "code": 200,
  "message": "提交成功"
}
```

---

### 2.5 业绩记录 (Performance)

#### 接口列表

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/sales/performance/list` | 获取业绩列表 | sales:performance:list |
| GET | `/api/sales/performance/{id}` | 获取业绩详情 | sales:performance:query |
| POST | `/api/sales/performance` | 创建业绩记录 | sales:performance:add |
| PUT | `/api/sales/performance` | 更新业绩记录 | sales:performance:edit |
| GET | `/api/sales/performance/statistics` | 业绩统计 | sales:performance:list |

---

## 3. 财务管理模块

**服务**: finance-service  
**数据库**: dafuweng_finance  
**基础路径**: `/api/finance`

### 3.1 银行管理 (Bank)

#### 接口列表

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/finance/bank/list` | 获取银行列表 | finance:bank:list |
| GET | `/api/finance/bank/{id}` | 获取银行详情 | finance:bank:query |
| POST | `/api/finance/bank` | 创建银行 | finance:bank:add |
| PUT | `/api/finance/bank` | 更新银行 | finance:bank:edit |
| DELETE | `/api/finance/bank/{ids}` | 删除银行 | finance:bank:remove |

---

### 3.2 金融产品 (Finance Product)

#### 接口列表

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/finance/product/list` | 获取产品列表 | finance:product:list |
| GET | `/api/finance/product/{id}` | 获取产品详情 | finance:product:query |
| POST | `/api/finance/product` | 创建产品 | finance:product:add |
| PUT | `/api/finance/product` | 更新产品 | finance:product:edit |
| DELETE | `/api/finance/product/{ids}` | 删除产品 | finance:product:remove |

#### 接口详情

**1. 创建金融产品**
```http
POST /api/finance/product
Content-Type: application/json

{
  "productCode": "PROD_001",
  "productName": "流动资金贷款A",
  "bankId": 1,
  "minAmount": 100000,
  "maxAmount": 1000000,
  "interestRate": 6.5,
  "minTerm": 3,
  "maxTerm": 24,
  "commissionRate": 3.0,
  "requirements": {
    "minAge": 25,
    "maxAge": 60,
    "minIncome": 5000
  },
  "status": 1
}

Response:
{
  "code": 200,
  "message": "创建成功"
}
```

---

### 3.3 贷款审核 (Loan Audit)

#### 接口列表

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/finance/audit/list` | 获取审核列表 | finance:audit:list |
| GET | `/api/finance/audit/{id}` | 获取审核详情 | finance:audit:query |
| POST | `/api/finance/audit` | 创建审核 | finance:audit:add |
| PUT | `/api/finance/audit` | 更新审核 | finance:audit:edit |
| PUT | `/api/finance/audit/receive` | 接收审核 | finance:audit:receive |
| PUT | `/api/finance/audit/review` | 初审 | finance:audit:review |
| PUT | `/api/finance/audit/submitBank` | 提交银行 | finance:audit:submit |
| PUT | `/api/finance/audit/bankResult` | 银行反馈 | finance:audit:bankResult |
| PUT | `/api/finance/audit/approve` | 终审通过 | finance:audit:approve |
| PUT | `/api/finance/audit/reject` | 终审拒绝 | finance:audit:reject |
| GET | `/api/finance/audit/{id}/records` | 获取审核轨迹 | finance:audit:query |

#### 接口详情

**1. 接收审核**
```http
PUT /api/finance/audit/receive
Content-Type: application/json

{
  "auditId": 1,
  "financeSpecialistId": 7,
  "recommendedProductId": 1
}

Response:
{
  "code": 200,
  "message": "接收成功"
}
```

**2. 初审**
```http
PUT /api/finance/audit/review
Content-Type: application/json

{
  "auditId": 1,
  "approvedAmount": 500000,
  "approvedTerm": 12,
  "reviewComment": "客户资质良好，建议通过"
}

Response:
{
  "code": 200,
  "message": "初审通过"
}
```

**3. 提交银行**
```http
PUT /api/finance/audit/submitBank
Content-Type: application/json

{
  "auditId": 1,
  "bankId": 1,
  "submitComment": "已初审通过，提交银行审批"
}

Response:
{
  "code": 200,
  "message": "提交成功"
}
```

**4. 银行反馈**
```http
PUT /api/finance/audit/bankResult
Content-Type: application/json

{
  "auditId": 1,
  "bankAuditStatus": 1,
  "actualLoanAmount": 500000,
  "actualInterestRate": 6.5,
  "bankFeedbackContent": "银行审批通过",
  "loanGrantedDate": "2026-04-20"
}

Response:
{
  "code": 200,
  "message": "银行反馈已记录"
}
```

---

### 3.4 服务费记录 (Service Fee)

#### 接口列表

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/finance/fee/list` | 获取服务费列表 | finance:fee:list |
| GET | `/api/finance/fee/{id}` | 获取服务费详情 | finance:fee:query |
| POST | `/api/finance/fee` | 创建服务费记录 | finance:fee:add |
| PUT | `/api/finance/fee/payment` | 确认收款 | finance:fee:payment |

---

### 3.5 提成记录 (Commission)

#### 接口列表

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/finance/commission/list` | 获取提成列表 | finance:commission:list |
| GET | `/api/finance/commission/{id}` | 获取提成详情 | finance:commission:query |
| POST | `/api/finance/commission` | 创建提成记录 | finance:commission:add |
| PUT | `/api/finance/commission/grant` | 发放提成 | finance:commission:grant |

---

## 4. 认证模块增强

**服务**: auth-service  
**基础路径**: `/api/auth`

### 4.1 验证码

#### 接口列表

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/auth/captcha` | 获取图形验证码 |
| POST | `/api/auth/captcha/verify` | 验证验证码 |

---

### 4.2 密码管理

#### 接口列表

| 方法 | 路径 | 说明 |
|------|------|------|
| PUT | `/api/auth/password` | 修改密码 |
| POST | `/api/auth/password/reset` | 忘记密码重置 |
| POST | `/api/auth/password/sendCode` | 发送验证码 |

---

## 5. 跨服务调用设计

### 5.1 Sales → Auth

**场景**: 验证用户权限

```java
@FeignClient(name = "auth-service", path = "/api/auth")
public interface AuthFeignClient {
    
    @GetMapping("/user/{id}")
    Result<UserInfo> getUserInfo(@PathVariable("id") Long userId);
    
    @PostMapping("/token/verify")
    Result<Boolean> verifyToken(@RequestHeader("Authorization") String token);
}
```

---

### 5.2 Finance → Sales

**场景**: 获取合同信息

```java
@FeignClient(name = "sales-service", path = "/api/sales")
public interface SalesFeignClient {
    
    @GetMapping("/contract/{id}")
    Result<Contract> getContract(@PathVariable("id") Long contractId);
    
    @PutMapping("/contract/status")
    Result<Void> updateContractStatus(@RequestBody ContractStatusRequest request);
}
```

---

### 5.3 事件驱动（RabbitMQ）

**场景**: 合同签署后通知财务

```java
// Sales 服务发送事件
rabbitTemplate.convertAndSend(
    "exchange.contract",
    "contract.signed",
    new ContractSignedEvent(contractId, contractAmount)
);

// Finance 服务监听
@RabbitListener(queues = "queue.contract.signed")
public void handleContractSigned(ContractSignedEvent event) {
    // 创建贷款审核记录
    loanAuditService.createAudit(event.getContractId());
}
```

---

## 📝 实现优先级

### 第一阶段（核心功能）

- [x] 认证模块（已完成）
- [ ] 系统管理 - 用户管理
- [ ] 系统管理 - 角色管理
- [ ] 销售管理 - 客户管理
- [ ] 销售管理 - 合同管理

### 第二阶段（业务流程）

- [ ] 销售管理 - 跟进记录
- [ ] 销售管理 - 工作日志
- [ ] 财务管理 - 贷款审核
- [ ] 财务管理 - 银行和产品

### 第三阶段（完善功能）

- [ ] 系统管理 - 数据字典
- [ ] 系统管理 - 操作日志
- [ ] 财务管理 - 服务费
- [ ] 财务管理 - 提成

---

## 🎯 技术栈

| 技术 | 用途 |
|------|------|
| Spring Boot 3.x | 应用框架 |
| MyBatis Plus | ORM |
| Spring Security | 安全认证 |
| JWT | Token 管理 |
| RabbitMQ | 消息队列 |
| OpenFeign | 服务调用 |
| Redis | 缓存 |
| MySQL 8.x | 数据库 |

---

**文档生成时间**: 2026-04-16  
**下次更新**: 实现后更新实际接口文档
