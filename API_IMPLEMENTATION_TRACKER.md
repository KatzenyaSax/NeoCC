# NeoCC 系统接口实现进度跟踪

**创建时间**: 2026-04-16  
**最后更新**: 2026-04-16  
**状态**: 📋 规划中  

---

## 📊 总体进度

| 模块 | 计划接口数 | 已实现 | 进度 | 状态 |
|------|-----------|--------|------|------|
| 认证模块 | 8 | 5 | 62.5% | 🟡 部分完成 |
| 系统管理 | 45 | 0 | 0% | ⚪ 未开始 |
| 销售管理 | 35 | 0 | 0% | ⚪ 未开始 |
| 财务管理 | 30 | 0 | 0% | ⚪ 未开始 |
| **总计** | **118** | **5** | **4.2%** | 🔴 初期 |

---

## ✅ 已完成接口

### 认证模块 (auth-service)

| # | 方法 | 路径 | 说明 | 状态 | 完成时间 |
|---|------|------|------|------|----------|
| 1 | POST | `/login` | 用户登录 | ✅ | 2026-04-14 |
| 2 | GET | `/getInfo` | 获取用户信息 | ✅ | 2026-04-14 |
| 3 | POST | `/api/oauth2/qrcode/generate` | 生成二维码 | ✅ | 2026-04-16 |
| 4 | POST | `/api/oauth2/qrcode/scan` | 扫码 | ✅ | 2026-04-16 |
| 5 | POST | `/api/oauth2/qrcode/confirm` | 确认登录 | ✅ | 2026-04-16 |

---

## 📋 待实现接口

### 1. 系统管理模块 (system-service)

**负责人**: 待定  
**计划完成**: 待定  

#### 1.1 战区管理 (Zone) - 0/5

| # | 方法 | 路径 | 说明 | 状态 | 备注 |
|---|------|------|------|------|------|
| 1 | GET | `/api/system/zone/list` | 获取战区列表 | ⚪ | |
| 2 | GET | `/api/system/zone/{id}` | 获取战区详情 | ⚪ | |
| 3 | POST | `/api/system/zone` | 创建战区 | ⚪ | |
| 4 | PUT | `/api/system/zone` | 更新战区 | ⚪ | |
| 5 | DELETE | `/api/system/zone/{ids}` | 删除战区 | ⚪ | |

#### 1.2 部门管理 (Department) - 0/5

| # | 方法 | 路径 | 说明 | 状态 | 备注 |
|---|------|------|------|------|------|
| 1 | GET | `/api/system/dept/list` | 获取部门树 | ⚪ | |
| 2 | GET | `/api/system/dept/{id}` | 获取部门详情 | ⚪ | |
| 3 | POST | `/api/system/dept` | 创建部门 | ⚪ | |
| 4 | PUT | `/api/system/dept` | 更新部门 | ⚪ | |
| 5 | DELETE | `/api/system/dept/{id}` | 删除部门 | ⚪ | |

#### 1.3 用户管理 (User) - 0/9

| # | 方法 | 路径 | 说明 | 状态 | 备注 |
|---|------|------|------|------|------|
| 1 | GET | `/api/system/user/list` | 获取用户列表 | ⚪ | |
| 2 | GET | `/api/system/user/{id}` | 获取用户详情 | ⚪ | |
| 3 | POST | `/api/system/user` | 创建用户 | ⚪ | |
| 4 | PUT | `/api/system/user` | 更新用户 | ⚪ | |
| 5 | DELETE | `/api/system/user/{ids}` | 删除用户 | ⚪ | |
| 6 | PUT | `/api/system/user/resetPwd` | 重置密码 | ⚪ | |
| 7 | PUT | `/api/system/user/changeStatus` | 修改状态 | ⚪ | |
| 8 | GET | `/api/system/user/profile` | 获取个人信息 | ⚪ | |
| 9 | PUT | `/api/system/user/profile` | 更新个人信息 | ⚪ | |

#### 1.4 角色管理 (Role) - 0/7

| # | 方法 | 路径 | 说明 | 状态 | 备注 |
|---|------|------|------|------|------|
| 1 | GET | `/api/system/role/list` | 获取角色列表 | ⚪ | |
| 2 | GET | `/api/system/role/{id}` | 获取角色详情 | ⚪ | |
| 3 | POST | `/api/system/role` | 创建角色 | ⚪ | |
| 4 | PUT | `/api/system/role` | 更新角色 | ⚪ | |
| 5 | DELETE | `/api/system/role/{ids}` | 删除角色 | ⚪ | |
| 6 | PUT | `/api/system/role/dataScope` | 修改数据权限 | ⚪ | |
| 7 | GET | `/api/system/role/authUser/{roleId}` | 获取角色用户 | ⚪ | |

#### 1.5 数据字典 (Dict) - 0/10

| # | 方法 | 路径 | 说明 | 状态 | 备注 |
|---|------|------|------|------|------|
| 1 | GET | `/api/system/dict/type/list` | 字典类型列表 | ⚪ | |
| 2 | GET | `/api/system/dict/type/{dictType}` | 获取字典类型 | ⚪ | |
| 3 | POST | `/api/system/dict/type` | 创建字典类型 | ⚪ | |
| 4 | PUT | `/api/system/dict/type` | 更新字典类型 | ⚪ | |
| 5 | DELETE | `/api/system/dict/type/{ids}` | 删除字典类型 | ⚪ | |
| 6 | GET | `/api/system/dict/data/list` | 字典数据列表 | ⚪ | |
| 7 | GET | `/api/system/dict/data/type/{dictType}` | 根据类型获取 | ⚪ | |
| 8 | POST | `/api/system/dict/data` | 创建字典数据 | ⚪ | |
| 9 | PUT | `/api/system/dict/data` | 更新字典数据 | ⚪ | |
| 10 | DELETE | `/api/system/dict/data/{ids}` | 删除字典数据 | ⚪ | |

#### 1.6 系统参数 (Param) - 0/5

| # | 方法 | 路径 | 说明 | 状态 | 备注 |
|---|------|------|------|------|------|
| 1 | GET | `/api/system/param/list` | 获取参数列表 | ⚪ | |
| 2 | GET | `/api/system/param/{paramKey}` | 获取参数值 | ⚪ | |
| 3 | POST | `/api/system/param` | 创建参数 | ⚪ | |
| 4 | PUT | `/api/system/param` | 更新参数 | ⚪ | |
| 5 | DELETE | `/api/system/param/{ids}` | 删除参数 | ⚪ | |

#### 1.7 操作日志 (Operation Log) - 0/4

| # | 方法 | 路径 | 说明 | 状态 | 备注 |
|---|------|------|------|------|------|
| 1 | GET | `/api/system/operlog/list` | 获取日志列表 | ⚪ | |
| 2 | GET | `/api/system/operlog/{id}` | 获取日志详情 | ⚪ | |
| 3 | DELETE | `/api/system/operlog/{ids}` | 删除日志 | ⚪ | |
| 4 | DELETE | `/api/system/operlog/clean` | 清空日志 | ⚪ | |

---

### 2. 销售管理模块 (sales-service)

**负责人**: 待定  
**计划完成**: 待定  

#### 2.1 客户管理 (Customer) - 0/8

| # | 方法 | 路径 | 说明 | 状态 | 备注 |
|---|------|------|------|------|------|
| 1 | GET | `/api/sales/customer/list` | 获取客户列表 | ⚪ | |
| 2 | GET | `/api/sales/customer/{id}` | 获取客户详情 | ⚪ | |
| 3 | POST | `/api/sales/customer` | 创建客户 | ⚪ | |
| 4 | PUT | `/api/sales/customer` | 更新客户 | ⚪ | |
| 5 | DELETE | `/api/sales/customer/{ids}` | 删除客户 | ⚪ | |
| 6 | PUT | `/api/sales/customer/transfer` | 转移客户 | ⚪ | |
| 7 | PUT | `/api/sales/customer/pickup` | 公海领取 | ⚪ | |
| 8 | GET | `/api/sales/customer/public/list` | 公海客户列表 | ⚪ | |

#### 2.2 跟进记录 (Contact Record) - 0/5

| # | 方法 | 路径 | 说明 | 状态 | 备注 |
|---|------|------|------|------|------|
| 1 | GET | `/api/sales/contact/list` | 获取跟进记录 | ⚪ | |
| 2 | GET | `/api/sales/contact/{id}` | 获取记录详情 | ⚪ | |
| 3 | POST | `/api/sales/contact` | 创建跟进记录 | ⚪ | |
| 4 | PUT | `/api/sales/contact` | 更新跟进记录 | ⚪ | |
| 5 | DELETE | `/api/sales/contact/{ids}` | 删除记录 | ⚪ | |

#### 2.3 合同管理 (Contract) - 0/8

| # | 方法 | 路径 | 说明 | 状态 | 备注 |
|---|------|------|------|------|------|
| 1 | GET | `/api/sales/contract/list` | 获取合同列表 | ⚪ | |
| 2 | GET | `/api/sales/contract/{id}` | 获取合同详情 | ⚪ | |
| 3 | POST | `/api/sales/contract` | 创建合同 | ⚪ | |
| 4 | PUT | `/api/sales/contract` | 更新合同 | ⚪ | |
| 5 | DELETE | `/api/sales/contract/{ids}` | 删除合同 | ⚪ | |
| 6 | PUT | `/api/sales/contract/sign` | 签署合同 | ⚪ | |
| 7 | GET | `/api/sales/contract/{id}/attachments` | 获取附件 | ⚪ | |
| 8 | POST | `/api/sales/contract/{id}/attachment` | 上传附件 | ⚪ | |

#### 2.4 工作日志 (Work Log) - 0/5

| # | 方法 | 路径 | 说明 | 状态 | 备注 |
|---|------|------|------|------|------|
| 1 | GET | `/api/sales/worklog/list` | 获取工作日志 | ⚪ | |
| 2 | GET | `/api/sales/worklog/{id}` | 获取日志详情 | ⚪ | |
| 3 | POST | `/api/sales/worklog` | 创建工作日志 | ⚪ | |
| 4 | PUT | `/api/sales/worklog` | 更新工作日志 | ⚪ | |
| 5 | DELETE | `/api/sales/worklog/{ids}` | 删除日志 | ⚪ | |

#### 2.5 业绩记录 (Performance) - 0/5

| # | 方法 | 路径 | 说明 | 状态 | 备注 |
|---|------|------|------|------|------|
| 1 | GET | `/api/sales/performance/list` | 获取业绩列表 | ⚪ | |
| 2 | GET | `/api/sales/performance/{id}` | 获取业绩详情 | ⚪ | |
| 3 | POST | `/api/sales/performance` | 创建业绩记录 | ⚪ | |
| 4 | PUT | `/api/sales/performance` | 更新业绩记录 | ⚪ | |
| 5 | GET | `/api/sales/performance/statistics` | 业绩统计 | ⚪ | |

#### 2.6 客户转移日志 (Transfer Log) - 0/2

| # | 方法 | 路径 | 说明 | 状态 | 备注 |
|---|------|------|------|------|------|
| 1 | GET | `/api/sales/transfer/list` | 获取转移日志 | ⚪ | |
| 2 | GET | `/api/sales/transfer/{id}` | 获取转移详情 | ⚪ | |

---

### 3. 财务管理模块 (finance-service)

**负责人**: 待定  
**计划完成**: 待定  

#### 3.1 银行管理 (Bank) - 0/5

| # | 方法 | 路径 | 说明 | 状态 | 备注 |
|---|------|------|------|------|------|
| 1 | GET | `/api/finance/bank/list` | 获取银行列表 | ⚪ | |
| 2 | GET | `/api/finance/bank/{id}` | 获取银行详情 | ⚪ | |
| 3 | POST | `/api/finance/bank` | 创建银行 | ⚪ | |
| 4 | PUT | `/api/finance/bank` | 更新银行 | ⚪ | |
| 5 | DELETE | `/api/finance/bank/{ids}` | 删除银行 | ⚪ | |

#### 3.2 金融产品 (Product) - 0/5

| # | 方法 | 路径 | 说明 | 状态 | 备注 |
|---|------|------|------|------|------|
| 1 | GET | `/api/finance/product/list` | 获取产品列表 | ⚪ | |
| 2 | GET | `/api/finance/product/{id}` | 获取产品详情 | ⚪ | |
| 3 | POST | `/api/finance/product` | 创建产品 | ⚪ | |
| 4 | PUT | `/api/finance/product` | 更新产品 | ⚪ | |
| 5 | DELETE | `/api/finance/product/{ids}` | 删除产品 | ⚪ | |

#### 3.3 贷款审核 (Loan Audit) - 0/11

| # | 方法 | 路径 | 说明 | 状态 | 备注 |
|---|------|------|------|------|------|
| 1 | GET | `/api/finance/audit/list` | 获取审核列表 | ⚪ | |
| 2 | GET | `/api/finance/audit/{id}` | 获取审核详情 | ⚪ | |
| 3 | POST | `/api/finance/audit` | 创建审核 | ⚪ | |
| 4 | PUT | `/api/finance/audit` | 更新审核 | ⚪ | |
| 5 | PUT | `/api/finance/audit/receive` | 接收审核 | ⚪ | |
| 6 | PUT | `/api/finance/audit/review` | 初审 | ⚪ | |
| 7 | PUT | `/api/finance/audit/submitBank` | 提交银行 | ⚪ | |
| 8 | PUT | `/api/finance/audit/bankResult` | 银行反馈 | ⚪ | |
| 9 | PUT | `/api/finance/audit/approve` | 终审通过 | ⚪ | |
| 10 | PUT | `/api/finance/audit/reject` | 终审拒绝 | ⚪ | |
| 11 | GET | `/api/finance/audit/{id}/records` | 获取审核轨迹 | ⚪ | |

#### 3.4 服务费记录 (Service Fee) - 0/4

| # | 方法 | 路径 | 说明 | 状态 | 备注 |
|---|------|------|------|------|------|
| 1 | GET | `/api/finance/fee/list` | 获取服务费列表 | ⚪ | |
| 2 | GET | `/api/finance/fee/{id}` | 获取服务费详情 | ⚪ | |
| 3 | POST | `/api/finance/fee` | 创建服务费记录 | ⚪ | |
| 4 | PUT | `/api/finance/fee/payment` | 确认收款 | ⚪ | |

#### 3.5 提成记录 (Commission) - 0/4

| # | 方法 | 路径 | 说明 | 状态 | 备注 |
|---|------|------|------|------|------|
| 1 | GET | `/api/finance/commission/list` | 获取提成列表 | ⚪ | |
| 2 | GET | `/api/finance/commission/{id}` | 获取提成详情 | ⚪ | |
| 3 | POST | `/api/finance/commission` | 创建提成记录 | ⚪ | |
| 4 | PUT | `/api/finance/commission/grant` | 发放提成 | ⚪ | |

---

### 4. 认证模块增强 (auth-service)

**负责人**: 待定  
**计划完成**: 待定  

#### 4.1 验证码 - 0/2

| # | 方法 | 路径 | 说明 | 状态 | 备注 |
|---|------|------|------|------|------|
| 1 | GET | `/api/auth/captcha` | 获取图形验证码 | ⚪ | |
| 2 | POST | `/api/auth/captcha/verify` | 验证验证码 | ⚪ | |

#### 4.2 密码管理 - 0/3

| # | 方法 | 路径 | 说明 | 状态 | 备注 |
|---|------|------|------|------|------|
| 1 | PUT | `/api/auth/password` | 修改密码 | ⚪ | |
| 2 | POST | `/api/auth/password/reset` | 忘记密码重置 | ⚪ | |
| 3 | POST | `/api/auth/password/sendCode` | 发送验证码 | ⚪ | |

---

## 📅 实施计划

### 第一阶段：核心功能（1-2周）

**目标**: 实现基础 CRUD 功能

**任务清单**:
- [ ] 系统管理 - 用户管理（9个接口）
- [ ] 系统管理 - 角色管理（7个接口）
- [ ] 销售管理 - 客户管理（8个接口）
- [ ] 销售管理 - 合同管理（8个接口）

**预计完成**: 待定

---

### 第二阶段：业务流程（2-3周）

**目标**: 实现核心业务流程

**任务清单**:
- [ ] 销售管理 - 跟进记录（5个接口）
- [ ] 销售管理 - 工作日志（5个接口）
- [ ] 财务管理 - 贷款审核（11个接口）
- [ ] 财务管理 - 银行和产品（10个接口）

**预计完成**: 待定

---

### 第三阶段：完善功能（1-2周）

**目标**: 完善辅助功能

**任务清单**:
- [ ] 系统管理 - 数据字典（10个接口）
- [ ] 系统管理 - 操作日志（4个接口）
- [ ] 财务管理 - 服务费（4个接口）
- [ ] 财务管理 - 提成（4个接口）
- [ ] 认证模块 - 验证码和密码（5个接口）

**预计完成**: 待定

---

## 🎯 关键里程碑

| 里程碑 | 目标 | 计划日期 | 实际日期 | 状态 |
|--------|------|----------|----------|------|
| M1 | 核心 CRUD 完成 | 待定 | - | ⚪ |
| M2 | 销售流程完成 | 待定 | - | ⚪ |
| M3 | 财务流程完成 | 待定 | - | ⚪ |
| M4 | 全部功能完成 | 待定 | - | ⚪ |
| M5 | 系统测试完成 | 待定 | - | ⚪ |
| M6 | 正式上线 | 待定 | - | ⚪ |

---

## 📊 统计信息

### 按模块统计

| 模块 | 接口数 | 百分比 |
|------|--------|--------|
| 认证模块 | 13 | 11.0% |
| 系统管理 | 45 | 38.1% |
| 销售管理 | 33 | 28.0% |
| 财务管理 | 27 | 22.9% |
| **总计** | **118** | **100%** |

### 按状态统计

| 状态 | 数量 | 百分比 |
|------|------|--------|
| ✅ 已完成 | 5 | 4.2% |
| 🟡 进行中 | 0 | 0% |
| ⚪ 未开始 | 113 | 95.8% |
| **总计** | **118** | **100%** |

---

## 📝 备注

1. 接口数量可能会根据实际情况调整
2. 优先级可能会根据业务需求变化
3. 每完成一个阶段更新一次进度
4. 遇到问题及时记录在备注栏

---

**文档维护**: AI Assistant  
**更新频率**: 每次实现后更新  
**下次审核**: 待定
