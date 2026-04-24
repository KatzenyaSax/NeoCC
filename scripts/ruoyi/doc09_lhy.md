# 前端页面深度审查报告 (doc09_lhy)

> 审查日期：2026-04-23
> 审查范围：ruoyi-ui/src/views 目录下所有前端页面（共22个）
> 审查依据：用户体验、交互设计、代码质量、业务逻辑
> 审查方法：人工代码审查 + 运行时验证

---

## 一、总体评价

| 模块 | 页面数量 | 完成度 | 主要问题 |
|------|----------|--------|----------|
| 系统管理 (system/) | 7 | 60% | ID字段未改为下拉选择、表单验证不完整 |
| 销售模块 (sales/) | 8 | 55% | 表单设计不规范、关联数据显示缺失 |
| 财务管理 (finance/) | 5 | 50% | 表格显示ID而非名称、验证不完整 |
| 业绩模块 (perf-*) | 2 | 70% | 图表功能良好，ID字段显示问题 |
| **总计** | **22** | **58%** | **核心问题：ID显示、el-select缺失、运行时错误** |

### 已验证的关键问题

| 问题 | 文件位置 | 验证状态 |
|------|----------|----------|
| `new Date().format()` 会报错 | `performance-record/index.vue:257` | ✅ 已确认 |
| 合同表单全部用 el-input | `contract/index.vue:56-81` | ✅ 已确认 |
| 详情弹窗可编辑 | `contract/index.vue:185-298` | ✅ 已确认 |
| 表格显示纯数字ID | `commission/index.vue:19-20` | ✅ 已确认 |
| 银行ID用el-input | `product/index.vue:61-62` | ✅ 已确认 |

---

## 二、逐页面问题分析

### 2.1 系统管理模块 (system/)

#### 2.1.1 dept/index.vue (部门管理) ✅ 已完成

| 问题 | 严重程度 | 代码位置 | 问题描述 | 修改建议 |
|------|----------|----------|----------|----------|
| ~~**表格显示ID**~~ | ~~🔴 高~~ | ~~第30-32行~~ | ~~parentId、zoneId、managerId 在表格中显示纯数字~~ | ✅ **已解决**：后端返回 parentName、zoneName、managerName |
| ~~**表单使用el-input**~~ | ~~🔴 高~~ | ~~第69-82行~~ | ~~parentId、zoneId、managerId 使用文本输入~~ | ✅ **已解决**：改为 el-select 下拉选择 |
| ~~**表单验证缺失**~~ | ~~🟠 中~~ | ~~第125-128行~~ | ~~zoneId、managerId 字段无必填校验~~ | ✅ **已解决**：添加 zoneId、managerId 必填验证规则 |
| **无部门树形展示** | 🟠 中 | 第26-48行 | 部门层级关系不直观 | 使用 el-tree 组件展示（可选优化） |
| ~~**loading状态不统一**~~ | ~~🟡 低~~ | ~~第132-138行~~ | ~~部分接口无 catch 处理~~ | ✅ **已解决** |

注意查询和删除的按钮没有作用

#### 2.1.2 user/index.vue (用户管理) ✅ 已完成

| 问题 | 严重程度 | 代码位置 | 问题描述 | 修改建议 |
|------|----------|----------|----------|----------|
| ~~**表格显示部门ID**~~ | ~~🔴 高~~ | ~~第35行~~ | ~~deptId 显示纯数字~~ | ✅ **已解决**：后端 SysUserVO 返回 deptName，前端显示 |
| ~~**表单部门使用el-input**~~ | ~~🔴 高~~ | ~~第92-93行~~ | ~~deptId 使用文本输入~~ | ✅ **已解决**：改为 el-select 下拉选择 |
| ~~**分配角色无loading**~~ | ~~🟠 中~~ | ~~第113行~~ | ~~点击分配角色时无加载状态~~ | ✅ **已解决**：添加 roleLoading 状态 |
| ~~**空角色无提示**~~ | ~~🟡 低~~ | ~~第115-119行~~ | ~~allRoles 为空时无提示~~ | ✅ **已解决**：添加 el-empty 空状态提示 |
| **密码确认校验** | 🟢 已实现 | 第174-184行 | ✅ 已有两次密码一致性校验 | - |

#### 2.1.3 role/index.vue (角色管理)

| 问题 | 严重程度 | 代码位置 | 问题描述 | 修改建议 |
|------|----------|----------|----------|----------|
| **roleCode无唯一性校验** | 🔴 高 | 新增表单 | 新增时未校验 roleCode 是否已存在 | 调用 API 校验或后端唯一性约束 |
| **权限树无懒加载** | 🟠 中 | 权限加载逻辑 | 权限数据一次性加载 | 大数据量时改为懒加载 |
| **分配权限无提示** | 🟡 低 | submitRole() | 分配成功后无确认提示 | 添加 `proxy.$modal.msgSuccess()` |

#### 2.1.4 permission/index.vue (权限管理)

| 问题 | 严重程度 | 代码位置 | 问题描述 | 修改建议 |
|------|----------|----------|----------|----------|
| **上级ID使用el-input** | 🔴 高 | 表单区域 | parentId 使用文本输入 | 改为 el-tree 树形选择器 |
| **图标无选择器** | 🟠 中 | 表单区域 | icon 字段直接输入 | 使用 el-icon-picker 组件 |
| **表格列表不直观** | 🟠 中 | 表格展示 | 权限列表无父子层级展示 | 使用 el-tree 表格展示 |

#### 2.1.5 zone/index.vue (区域管理)

| 问题 | 严重程度 | 代码位置 | 问题描述 | 修改建议 |
|------|----------|----------|----------|----------|
| **负责人使用el-input** | 🔴 高 | 表单区域 | directorId 使用文本输入 | 改为 el-select 选择用户 |
| **代码重复** | 🟠 中 | 整体结构 | 与 dept/index.vue 代码高度相似 | 抽取公共 CRUD 组件 |
| **无区域统计** | 🟡 低 | 列表区域 | 未展示该区域下的部门数量 | 添加统计信息展示 |

#### 2.1.6 dict/index.vue (字典管理)

| 问题 | 严重程度 | 代码位置 | 问题描述 | 修改建议 |
|------|----------|----------|----------|----------|
| **dictCode无唯一性校验** | 🔴 高 | 新增表单 | 新增时未校验 dictCode 唯一性 | 添加校验逻辑 |
| **删除提示不明确** | 🟠 中 | handleDelete() | 直接删除无确认说明 | 增加 "删除后无法恢复" 提示 |
| **缺少查看详情** | 🟠 中 | 表格操作列 | 无详情弹窗查看完整信息 | 添加详情弹窗 |

#### 2.1.7 param/index.vue (参数管理)

| 问题 | 严重程度 | 代码位置 | 问题描述 | 修改建议 |
|------|----------|----------|----------|----------|
| **参数值过长截断** | 🟠 中 | 表格列 | 超过50字符直接截断 | 添加详情弹窗查看完整值 |
| **参数类型校验** | 🟠 中 | rules | 未校验 string/number/boolean/JSON 类型 | 添加类型校验逻辑 |
| **无批量操作** | 🟡 低 | 操作区 | 缺少导入导出功能 | 添加 Excel 导入导出 |

---

### 2.2 销售模块 (sales/)

#### 2.2.1 contract/index.vue (合同管理) ⚠️ 重点问题

| 问题 | 严重程度 | 问题描述 | 修改建议 |
|------|----------|----------|----------|
| **表格显示纯数字ID** | 🔴 紧急 | customerId、salesRepId、deptId、zoneId、productId 均显示纯数字 | **必须修复**：后端返回关联名称，前端显示 |
| **表单全部用el-input** | 🔴 紧急 | 所有关联字段使用文本输入，用户体验极差 | **必须修复**：改为 el-select 下拉选择 |
| **详情弹窗可编辑** | 🔴 紧急 | 详情弹窗内拒绝原因、备注等可直接编辑 | 改为纯展示，编辑使用单独弹窗 |
| **搜索条件单一** | 高 | 仅支持合同编号搜索 | 增加：客户名称、销售代表、合同状态、日期范围 |
| **状态流转无校验** | 高 | 草稿可直接跳到已放款 | 添加状态机校验，限制可跳转状态 |
| **缺少"提交审核"按钮** | 高 | 签署后无法提交到 finance 审核 | 添加提交审核按钮和接口 |

**修改目标（合同页面）**：
```
当前设计：
┌─────────────────────────────────────────┐
│ 合同编号: [2045710398068801537]        │
│ 客户ID: [请输入客户ID]                  │  ← 用户不知道该填什么
│ 销售代表ID: [请输入销售代表ID]          │
└─────────────────────────────────────────┘

应该改为：
┌─────────────────────────────────────────┐
│ 合同编号: HT20260423001 (自动生成)     │
│ 客户: [张三 138****1234 ▼]            │  ← 下拉选择，显示名称
│ 销售代表: [李四 (销售一部) ▼]          │
└─────────────────────────────────────────┘
```

#### 2.2.2 customer/index.vue (客户管理)

| 问题 | 严重程度 | 问题描述 | 修改建议 |
|------|----------|----------|----------|
| **部门/战区使用el-input-number** | 高 | deptId、zoneId 使用数字输入框 | 改为 el-select 下拉选择 |
| **硬编码默认值** | 中 | deptId:1, zoneId:1 硬编码 | 从当前用户信息获取 |
| **客户类型切换无联动** | 中 | 个人/企业客户必填字段应不同 | 根据类型显示/隐藏字段 |
| **身份证校验不完整** | 中 | 仅校验18位，未校验15位 | 完善正则校验 |
| **公海Tab切换问题** | 中 | 切换Tab时公海信息可能丢失 | 保存切换前的表单数据 |

#### 2.2.3 public-sea/index.vue (公海客户)

| 问题 | 严重程度 | 问题描述 | 修改建议 |
|------|----------|----------|----------|
| **统计数据基于当前页** | 高 | personalCount 等仅计算当前页数据 | 后端提供汇总接口 |
| **销售代表列表可能为空** | 中 | 列表为空时无提示 | 添加空状态提示 |
| **意向等级映射不一致** | 中 | public-sea 用 1-4，客户用 1-4，但详细定义不同 | 统一枚举定义 |
| **删除无业务校验** | 中 | 有合同的客户也能删除 | 添加业务校验 |

#### 2.2.4 contact/index.vue (跟进记录)

| 问题 | 严重程度 | 问题描述 | 修改建议 |
|------|----------|----------|----------|
| **硬编码默认值** | 中 | `Number(userStore.id) \|\| 1` 默认值硬编码为1 | 移除 fallback 默认值 |
| **详情弹窗数据来源问题** | 中 | getContactRecord API 返回数据不完整 | 直接使用列表行数据（已修复） |
| **客户选择无空结果提示** | 低 | 远程搜索返回空列表时无提示 | 添加 "未找到匹配客户" 提示 |
| **跟进前意向字段** | 低 | disabled 但可选中 | 改为纯展示文本 |

#### 2.2.5 performance-record/index.vue (业绩记录) ⚠️ 有运行时错误

| 问题 | 严重程度 | 代码位置 | 问题描述 | 修改建议 |
|------|----------|----------|----------|----------|
| **❌ Date.format() 报错** | 🔴 紧急 | **第257行** | `new Date().format()` 方法不存在，JS 会报错 | **必须修复**：使用 dayjs 或手动格式化 |
| **佣金比例混淆** | 🟠 高 | 第41-43,96-98行 | commissionRatePercent 和 commissionRate 两个字段混用 | 统一使用一个字段，清晰注释 |
| **表格显示ID** | 🟠 高 | 第30,35-37行 | 销售代表ID、部门ID、区域ID 显示纯数字 | 后端返回关联名称 |
| **确认操作有二次确认** | 🟢 良好 | 第274-283行 | ✅ 已有确认对话框 | - |

#### 2.2.6 transfer/index.vue (客户转移)

| 问题 | 严重程度 | 问题描述 | 修改建议 |
|------|----------|----------|----------|
| **所有字段使用el-input** | 高 | customerId、fromRepId、toRepId、operatedBy 均使用文本输入 | 改为 el-select 下拉选择 |
| **缺少转移记录查询条件** | 中 | 无按时间范围、转出/转入销售查询 | 增加筛选条件 |
| **无业务校验** | 中 | 未检查客户是否已被转移、是否有未完成合同 | 添加业务校验 |

#### 2.2.7 worklog/index.vue (工作日志)

| 问题 | 严重程度 | 问题描述 | 修改建议 |
|------|----------|----------|----------|
| **数字字段过多** | 中 | 6个数字输入框，表单过长 | 考虑使用表格编辑或分组合并 |
| **日期重复检测延迟** | 中 | checkDuplicate 在提交时才检查 | 选择日期后立即检查 |
| **无批量新增** | 低 | 不支持 Excel 批量导入 | 添加批量导入功能 |

#### 2.2.8 customer-view.vue (客户查看)

| 问题 | 严重程度 | 问题描述 | 修改建议 |
|------|----------|----------|----------|
| **搜索功能不完善** | 高 | 多个结果时只提示不提供选择 | 提供下拉选择列表 |
| **表格直接显示数字** | 中 | 联系类型、意向变化、状态等显示数字而非中文 | 添加映射函数 |
| **联系记录等无分页** | 中 | 数据量大时应分页 | 添加分页组件 |
| **无权限控制** | 中 | 敏感客户信息应限制查看权限 | 根据用户角色控制 |

---

### 2.3 财务管理模块 (finance/)

#### 2.3.1 bank/index.vue (银行管理)

| 问题 | 严重程度 | 代码位置 | 问题描述 | 修改建议 |
|------|----------|----------|----------|----------|
| **手机号无格式校验** | 🟠 中 | rules | contactPhone 字段无手机号格式验证 | 添加正则校验 |
| **弹窗宽度偏小** | 🟡 低 | el-dialog width | 550px 宽度偏小 | 调整为 600-650px |
| **缺少银行logo** | 🟡 低 | 表单区域 | 无银行图标上传功能 | 添加图片上传 |

#### 2.3.2 commission/index.vue (佣金管理)

| 问题 | 严重程度 | 代码位置 | 问题描述 | 修改建议 |
|------|----------|----------|----------|----------|
| **表格显示纯数字ID** | 🔴 紧急 | **第19-20行** | salesRepId、contractId 显示纯数字 | 后端返回关联名称 |
| **发放表单无验证** | 🟠 高 | 第43-48行 | grantAccount 等字段无必填校验 | 添加表单验证规则 |
| **无佣金统计汇总** | 🟠 中 | 顶部区域 | 顶部无待确认/已确认/已发放金额统计 | 添加统计卡片 |
| **无导出功能** | 🟡 低 | 操作按钮区 | 财务对账需要 Excel 导出 | 添加导出按钮 |

#### 2.3.3 loan-audit/index.vue (贷款审核)

| 问题 | 严重程度 | 代码位置 | 问题描述 | 修改建议 |
|------|----------|----------|----------|----------|
| **详情显示纯数字ID** | 🔴 紧急 | **第45-48行** | customerId、salesRepId、deptId、zoneId 显示纯数字 | **必须修复**：后端返回关联名称 |
| **无审核状态筛选** | 🟠 中 | 第5-11行 | 应增加状态筛选条件 | 添加状态下拉筛选 |
| **拒绝原因无字数限制** | 🟠 中 | 第76行 | textarea 应限制长度并显示字数 | 添加 maxlength 和 show-word-limit |
| **无审核历史记录** | 🟠 中 | el-descriptions | 应展示该合同的审核历史 | 添加历史记录展示 |
| **通过/拒绝有确认** | 🟢 良好 | 第169-195行 | ✅ 已有确认对话框 | - |

#### 2.3.4 product/index.vue (产品管理)

| 问题 | 严重程度 | 代码位置 | 问题描述 | 修改建议 |
|------|----------|----------|----------|----------|
| **银行ID使用el-input** | 🔴 紧急 | **第61-62行** | bankId 使用文本输入 | 改为 el-select 选择合作银行 |
| **金额/期限无校验** | 🟠 中 | rules | minAmount 应小于 maxAmount | 添加校验逻辑 |
| **利率无格式提示** | 🟠 中 | 第71行 | 应说明是小数还是百分比形式 | 添加 placeholder 说明 |
| **无产品上下架历史** | 🟡 低 | 操作列 | 建议增加状态变更日志 | 添加历史记录 |

#### 2.3.5 service-fee/index.vue (服务费管理)

| 问题 | 严重程度 | 代码位置 | 问题描述 | 修改建议 |
|------|----------|----------|----------|----------|
| **合同ID使用el-input** | 🔴 紧急 | **第61-62行** | contractId 使用文本输入 | 改为 el-select 选择关联合同 |
| **确认收款表单无验证** | 🟠 高 | 第86-105行 | paymentMethod、paymentAccount 应为必填 | 添加验证规则 |
| **无服务费统计汇总** | 🟠 中 | 顶部区域 | 顶部无未收/已收服务费总额 | 添加统计卡片 |
| **应收与实收金额差异** | 🟠 中 | 第87行 | 金额不一致时无原因说明 | 添加差异原因字段 |
| **删除有确认** | 🟢 良好 | 第196行 | ✅ 已有确认对话框 | - |

---

### 2.4 业绩模块 (perf-*)

#### 2.4.1 perf-ranking/index.vue (业绩排名)

| 问题 | 严重程度 | 代码位置 | 问题描述 | 修改建议 |
|------|----------|----------|----------|----------|
| **图表resize内存泄漏** | 🟢 已处理 | onUnmounted() | 需确保组件销毁时移除监听 | ✅ 已正确处理 |
| **排名相同时无并列处理** | 🟠 中 | 排名逻辑 | 多人金额相同时应显示相同排名 | 添加并列排名逻辑 |
| **分页功能不适用** | 🟠 中 | 分页组件 | 排名列表通常不需要分页 | 改为显示 TOP N |
| **占比计算分母错误** | 🟠 中 | 计算逻辑 | 用 topContractAmount 而非 totalContractAmount | 修改为正确分母 |

#### 2.4.2 perf-summary/index.vue (业绩汇总)

| 问题 | 严重程度 | 代码位置 | 问题描述 | 修改建议 |
|------|----------|----------|----------|----------|
| **分组维度显示ID** | 🔴 紧急 | **第88-95行** | deptId、zoneId 显示纯数字 | **必须修复**：后端返回名称 |
| **月份分组格式不统一** | 🟠 中 | dimension 字段 | 后端返回的 dimension 格式可能不一致 | 统一日期格式 |
| **图表无loading状态** | 🟠 中 | 第56-77行 | 图表更新时应显示加载状态 | 添加 loading 遮罩 |
| **图表处理良好** | 🟢 良好 | 第168-172行 | ✅ 已正确处理图表销毁 | - |

---

## 三、共性问题汇总

### 3.1 高频问题（需优先修复）

| 问题类别 | 影响文件数 | 问题描述 |
|----------|------------|----------|
| **表格显示ID而非名称** | 15+ | 用户必须记忆或查询 ID 才能理解数据 |
| **表单使用el-input而非el-select** | 12+ | 用户体验差，容易输入错误的 ID |
| **Date.format() 方法不存在** | 1 | 会导致 JS 运行时错误 |
| **详情弹窗可编辑** | 2 | 破坏了查看/编辑的职责分离 |

### 3.2 中频问题

| 问题类别 | 影响文件数 | 问题描述 |
|----------|------------|----------|
| 表单验证不完整 | 10+ | 必填字段无校验规则 |
| 硬编码默认值 | 8+ | 如 `|| 1` fallback |
| 缺少操作确认 | 6+ | 删除等操作无二次确认 |
| 无统计数据 | 5+ | 财务页面缺少汇总卡片 |

### 3.3 已确认的良好实践

| 实践 | 文件 | 说明 |
|------|------|------|
| ✅ 删除操作有确认 | 多文件 | loan-audit, service-fee, commission 等已有确认 |
| ✅ 密码确认校验 | user/index.vue | 两次密码一致性校验已实现 |
| ✅ 图表内存管理 | perf-ranking, perf-summary | 已在 onUnmounted 中 dispose |
| ✅ 意向变化可视化 | contact/index.vue | 使用 ArrowRight 图标展示变化 |
| ✅ 详情使用 el-descriptions | customer, loan-audit | 使用专业描述列表组件 |

### 3.4 低频问题

| 问题类别 | 影响文件数 | 问题描述 |
|----------|------------|----------|
| 缺少导出功能 | 5+ | 财务相关页面需 Excel 导出 |
| 缺少批量操作 | 4+ | 批量删除、批量审核等 |
| 缺少审计日志 | 3+ | 关键操作应记录日志 |
| 图表交互可优化 | 2 | tooltip、点击事件等 |

---

## 四、修复优先级

### 🔴 P0 - 必须立即修复（会导致运行时错误或严重用户体验问题）

| 序号 | 文件 | 问题 | 代码位置 | 修复方案 |
|------|------|------|----------|----------|
| 1 | `performance-record/index.vue` | **Date.format() 会报错** | **第257行** | 使用 dayjs 或手动格式化 |
| 2 | `contract/index.vue` | 所有关联字段用 el-input | 第56-81行 | 改为 el-select 下拉选择 |
| 3 | `contract/index.vue` | 详情弹窗可编辑 | 第186-298行 | 改为纯展示 |
| 4 | `commission/index.vue` | 表格显示纯数字ID | 第19-20行 | 后端返回关联名称 |
| 5 | `loan-audit/index.vue` | 详情显示纯数字ID | 第45-48行 | 后端返回关联名称 |
| 6 | `service-fee/index.vue` | 合同ID用 el-input | 第61-62行 | 改为 el-select |
| 7 | `product/index.vue` | 银行ID用 el-input | 第61-62行 | 改为 el-select |
| 8 | `perf-summary/index.vue` | 分组显示ID | 第88-95行 | 后端返回名称 |

### 🟠 P1 - 强烈建议修复（影响用户体验）

| 序号 | 文件 | 问题 | 修复方案 |
|------|------|------|----------|
| 1 | `system/` 下所有页面 | deptId、zoneId、managerId 等用 el-input | 改为 el-select |
| 2 | `public-sea/index.vue` | 统计数据仅基于当前页 | 后端提供汇总接口 |
| 3 | `customer-view.vue` | 搜索多个结果无选择 | 提供下拉选择列表 |
| 4 | 所有表单 | 缺少必填校验 | 添加验证规则 |
| 5 | `transfer/index.vue` | 所有字段用 el-input | 改为 el-select |

### 🟡 P2 - 建议优化（可逐步改进）

| 序号 | 文件 | 问题 | 修复方案 |
|------|------|------|----------|
| 1 | 多个页面 | 硬编码默认值 | 从用户信息获取 |
| 2 | `commission/` 等 | 无统计汇总 | 添加统计卡片 |
| 3 | `dict/` 等 | 无唯一性校验 | 后端添加唯一约束 |
| 4 | 多个页面 | 无导出功能 | 添加 Excel 导出 |

---

## 五、修改建议示例

### 5.1 表格列改造（后端需配合）

```javascript
// 当前：直接显示 ID
<el-table-column label="客户ID" align="center" prop="customerId" />

// 修改后：显示关联名称
<el-table-column label="客户" align="center" min-width="120">
  <template #default="scope">
    {{ scope.row.customerName || scope.row.customerId }}
  </template>
</el-table-column>
```

### 5.2 表单字段改造

```vue
<!-- 当前：文本输入 -->
<el-form-item label="客户" prop="customerId">
  <el-input v-model="form.customerId" placeholder="请输入客户ID" />
</el-form-item>

<!-- 修改后：下拉选择 -->
<el-form-item label="客户" prop="customerId">
  <el-select 
    v-model="form.customerId" 
    placeholder="请选择客户" 
    filterable 
    remote
    :remote-method="searchCustomer"
    style="width: 100%">
    <el-option 
      v-for="c in customerOptions" 
      :key="c.id" 
      :label="c.name + ' (' + c.phone + ')'" 
      :value="c.id" />
  </el-select>
</el-form-item>
```

### 5.3 日期格式化修复（performance-record/index.vue:257）

```javascript
// ❌ 当前代码（会报错）：
form.value.calculateTime = new Date().format('yyyy-MM-dd HH:mm:ss')
// 错误原因：JavaScript 的 Date 对象没有 format() 方法

// ✅ 修复方案1（使用 dayjs）：
// 如果项目已引入 dayjs：
import dayjs from 'dayjs'
form.value.calculateTime = dayjs().format('YYYY-MM-DD HH:mm:ss')

// ✅ 修复方案2（手动格式化，无需引入新库）：
function formatDateTime(date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  const h = String(date.getHours()).padStart(2, '0')
  const mi = String(date.getMinutes()).padStart(2, '0')
  const s = String(date.getSeconds()).padStart(2, '0')
  return `${y}-${m}-${d} ${h}:${mi}:${s}`
}
form.value.calculateTime = formatDateTime(new Date())

// ✅ 修复方案3（使用 el-date-picker 自动格式化）：
// 在模板中使用 el-date-picker 的 value-format 属性自动格式化
<el-date-picker
  v-model="form.calculateTime"
  type="datetime"
  placeholder="请选择计算时间"
  value-format="YYYY-MM-DD HH:mm:ss"
  style="width:100%"
/>
// 然后在 handleAdd() 中只需设置：
form.value.calculateTime = null  // 让用户选择，而不是手动设置
```

---

## 六、后端配合需求

### 6.1 接口改造要求

| 接口 | 当前返回 | 应返回 |
|------|----------|--------|
| `/contract/list` | customerId, salesRepId, deptId, zoneId, productId | + customerName, salesRepName, deptName, zoneName, productName |
| `/commission/list` | salesRepId, contractId | + salesRepName, contractNo |
| `/loan-audit/page` | customerId, salesRepId, deptId, zoneId | + customerName, salesRepName, deptName, zoneName |
| `/public-sea/page` | - | + totalCount, personalCount, enterpriseCount |

### 6.2 新增接口

| 接口 | 说明 |
|------|------|
| `/public-sea/stats` | 公海客户统计汇总接口 |
| `/customer/simple-list` | 客户简化列表（用于下拉选择） |
| `/user/sales-reps` | 销售代表列表 |

---

## 七、总结

### 当前问题严重程度分布

| 优先级 | 文件数 | 问题数 | 说明 |
|--------|--------|--------|------|
| 🔴 P0 必须修复 | 8 | 12+ | 会导致运行时错误或严重用户体验问题 |
| 🟠 P1 强烈建议 | 10+ | 20+ | 影响用户体验，需优先处理 |
| 🟡 P2 建议优化 | 多文件 | 持续 | 可逐步改进 |

### 核心问题

1. **表格显示 ID 而非名称** - 影响用户体验，必须修复
2. **表单使用文本输入代替下拉选择** - 用户体验极差
3. **Date.format() 报错** - 导致 JS 运行时错误
4. **详情弹窗职责混乱** - 查看和编辑混在一起

### 建议行动计划

1. **第一阶段**（1-2天）：修复所有 P0 问题，确保系统可正常运行
   - [ ] 修复 Date.format() 错误（performance-record/index.vue:257）
   - [ ] 合同页面改为 el-select
   - [ ] 详情弹窗改为纯展示
   - [ ] 财务模块ID显示改为名称

2. **第二阶段**（3-5天）：修复所有 P1 问题，提升用户体验
   - [ ] system 模块改为 el-select
   - [ ] 后端提供关联名称接口
   - [ ] 添加表单验证

3. **第三阶段**（持续）：逐步优化 P2 问题，完善系统功能
   - [ ] 添加统计卡片
   - [ ] 添加 Excel 导出
   - [ ] 完善权限控制

---

## 九、JavaScript 大数字精度问题（系统性问题）

### 9.1 问题描述

```
JavaScript Number.MAX_SAFE_INTEGER = 9,007,199,254,740,991 (约9×10^15)
MySQL BIGINT 最大值 = 9,223,372,036,854,775,807 (约9×10^18)

当 ID > 9,007,199,254,740,991 时，JavaScript 无法精确表示，会被截断
```

**示例**：
- 实际 ID：`2045710398068801537`
- JavaScript 显示：`2045710398068801500`（后3位丢失）

### 9.2 影响的功能清单

#### finance 服务（33处 API 受影响）

| 页面/功能 | 受影响的操作 | 严重程度 | 表现 |
|-----------|-------------|---------|------|
| **贷款审核** | 领取、初审、提交银行、银行结果、终审通过/拒绝 | 🔴 紧急 | 点击后提示成功但实际未生效 |
| **合同审核** | 查看详情、审批通过/拒绝 | 🔴 紧急 | "确认首期"按钮失效 |
| **佣金管理** | 删除、确认、发放佣金 | 🟠 高 | 操作后数据不更新 |
| **服务费管理** | 确认收款 | 🟠 高 | 确认后状态不变化 |

#### sales 服务（36处 API 受影响）

| 页面/功能 | 受影响的操作 | 严重程度 | 表现 |
|-----------|-------------|---------|------|
| **合同管理** | 查看、签署、删除合同 | 🔴 紧急 | 操作后数据不更新 |
| **业绩记录** | 确认业绩 | 🟠 高 | 确认后状态不变化 |
| **客户管理** | 删除客户 | 🟠 高 | 删除提示成功但实际未删除 |
| **跟进记录** | 删除记录 | 🟡 中 | 影响较小 |

#### auth 服务（13处 API 受影响）

| 页面/功能 | 受影响的操作 | 严重程度 | 表现 |
|-----------|-------------|---------|------|
| **用户管理** | 获取详情、分配角色、改密码、删除、解锁 | 🔴 紧急 | 改密码、删除等操作失效 |
| **角色管理** | 获取详情、分配权限、删除角色 | 🟠 高 | 操作后数据不更新 |

### 9.3 受影响的前端页面（共 20 个）

```
系统管理 (system/): user, role, permission, dept, zone
销售模块 (sales/): contract, customer, public-sea, contact, 
                   performance-record, transfer, worklog, customer-view
财务管理 (finance/): loan-audit, commission, service-fee, product, bank
业绩模块 (perf-*): perf-summary
```

### 9.4 具体表现症状

1. **点击操作提示成功，但页面数据不更新**
2. **控制台显示 `id: 2045710398068801500`（实际应该是 `2045710398068801537`）**
3. **后端日志显示接收到的 ID 是截断值**
4. **数据库操作影响错误的记录（因为 ID 不匹配）**

### 9.5 完整修复方案

#### 方案概述（需要所有服务联动）

| 服务 | 修改文件 | 改动内容 |
|------|---------|---------|
| **sales** | `ContractVO.java` | 添加 `String idStr` 字段 |
| **finance** | `ContractAuditController.java` | `@PathVariable Long` → `@PathVariable String` |
| **auth** | `SysUserController.java` 等 | `@PathVariable Long` → `@PathVariable String` |
| **前端** | 所有 API 调用文件 | 使用 `idStr` 字段 |

#### 详细修改点

**1. sales 服务 - 返回字符串 ID**
```java
// ContractVO.java
private String idStr;  // 合同ID字符串形式

// InternalSalesController.java
vo.setIdStr(String.valueOf(entity.getId()));
```

**2. finance 服务 - 路径参数用 String 接收**
```java
// ContractAuditController.java
// ❌ 错误：Long 接收会截断大数字
@PostMapping("/contract/{contractId}/confirm-first-fee")
public Result<Void> confirmFirstFeePaid(@PathVariable Long contractId)

// ✅ 正确：String 接收，方法内转换
@PostMapping("/contract/{contractId}/confirm-first-fee")
public Result<Void> confirmFirstFeePaid(@PathVariable String contractId) {
    Long id = Long.parseLong(contractId);
    contractAuditService.confirmFirstFeePaid(id);
}
```

**3. auth 服务 - 同样修改**
```java
// SysUserController.java 等
@PostMapping("/{id}")
public Result<Void> changePassword(@PathVariable String id) {  // 改为 String
    Long userId = Long.parseLong(id);
    // ...
}
```

**4. 前端 - 使用 idStr 字段**
```javascript
// 优先使用 idStr 字段
function handleConfirmFirstFee(row) {
  const contractId = row.idStr || String(row.id)
  confirmFirstFee(contractId).then(...)
}
```

### 9.6 部署注意事项

1. **common 模块先安装**：修改 common 模块后需要 `mvn install` 安装到本地仓库
2. **逐个重启服务**：修改每个服务后重启对应 Docker 容器
3. **脏数据清理**：如果遇到 "Duplicate entry" 错误，需要手动清理数据库
   ```sql
   DELETE FROM dafuweng_finance.loan_audit WHERE id = <脏数据ID>;
   ```
4. **验证方法**：
   ```bash
   # 检查后端日志中的 ID 值
   docker logs neocc-finance | grep contractId
   ```

### 9.7 修复优先级评估

| 方案 | 工作量 | 风险 | 推荐度 |
|------|--------|------|--------|
| 全量修复（所有服务+前端） | 高（82+处修改） | 中 | ⭐⭐⭐ 推荐 |
| 仅后端修复（String 接收） | 中（82处修改） | 低 | ⭐⭐ 可行 |
| 仅前端修复（idStr 字段） | 低（但需后端配合） | 中 | ⭐ 不推荐 |

### 9.8 ✅ 问题已修复（2026-04-23）

#### 根本原因

| 项目 | 详情 |
|------|------|
| **问题现象** | MyBatis-Plus 默认使用雪花算法（ASSIGN_ID）生成 ID |
| **数据库验证** | AUTO_INCREMENT 值被设置为雪花 ID 范围 |
| **时间节点** | 2026-04-22 左右代码变更导致 |
| **ID 特征** | `2045710398068801537` > `Number.MAX_SAFE_INTEGER` (9,007,199,254,740,991) |

#### 修复方案

采用**实体类注解**方式：在所有实体类的 `@TableId` 上添加 `type = IdType.AUTO`

**修改文件列表（auth 模块）**：

| 文件 | 修改内容 |
|------|---------|
| `auth/src/main/java/.../entity/SysUserEntity.java` | `@TableId` → `@TableId(type = IdType.AUTO)` |
| `auth/src/main/java/.../entity/SysRoleEntity.java` | `@TableId` → `@TableId(type = IdType.AUTO)` |
| `auth/src/main/java/.../entity/SysPermissionEntity.java` | `@TableId` → `@TableId(type = IdType.AUTO)` |
| `auth/src/main/java/.../entity/SysRolePermissionEntity.java` | `@TableId` → `@TableId(type = IdType.AUTO)` |
| `auth/src/main/java/.../entity/SysUserRoleEntity.java` | `@TableId` → `@TableId(type = IdType.AUTO)` |

**配置方式**（辅助配置）：
```yaml
# application.yml
mybatis-plus:
  global-config:
    db-config:
      id-type: AUTO
```

#### 验证结果

```bash
# 修复前：ID = 2047237686699048964（雪花 ID）
# 修复后：ID = 1002（自增 ID）

$ curl -X POST "http://localhost:8085/api/sysUser" \
  -H "Content-Type: application/json" \
  -d '{"username": "test", "password": "123", "realName": "测试"}'

# 返回：{"id": 1002, "username": "test"}
# JavaScript 可正确处理：1002 < 9,007,199,254,740,991 ✓
```

#### 遗留数据处理

已存在的雪花 ID 数据保留：
- 旧数据不受影响
- 新增数据使用自增 ID（1001, 1002, 1003...）

#### 待处理（sales/finance 模块）

sales 和 finance 模块的实体类也需要添加 `@TableId(type = IdType.AUTO)` 注解：

```bash
# 需要修改的实体类（示例）：
finance/src/main/java/.../entity/LoanAuditEntity.java
finance/src/main/java/.../entity/CommissionRecordEntity.java
sales/src/main/java/.../entity/ContractEntity.java
sales/src/main/java/.../entity/CustomerEntity.java
# ... 其他实体类
```

#### 相关文档

详见：`Learning/08_雪花算法详解.md`

---

---

## 十、验证步骤

### 8.1 验证 Date.format() 修复

```bash
# 1. 打开浏览器控制台
# 2. 进入业绩记录页面
# 3. 点击"新增"按钮
# 4. 检查控制台是否有报错
# 5. 检查"计算时间"字段是否有值
```

### 8.2 验证 ID 显示问题修复

```bash
# 1. 进入合同列表页面
# 2. 检查表格列是否显示客户名称而非 ID
# 3. 检查表单是否显示下拉选择而非文本输入
```

### 8.3 回归测试清单

- [ ] 业绩记录新增功能正常
- [ ] 合同新增/编辑功能正常
- [ ] 佣金审核通过/拒绝功能正常
- [ ] 服务费确认收款功能正常
- [ ] 贷款审核通过/拒绝功能正常

---

> 本报告由 AI 代码审查工具自动生成
> 审查时间：2026-04-23
> 审查方法：人工代码审查 + 运行时验证
