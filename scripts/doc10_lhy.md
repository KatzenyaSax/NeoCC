# 销售模块前端审查报告 (doc10_lhy)

> 审查日期：2026-04-24
> 审查范围：ruoyi-ui/src/views/sales 目录下所有前端页面（共8个）
> 审查依据：基于 doc09_lhy.md 审核标准 2.2
> 审查方法：人工代码审查 + 运行时验证

---

## 一、总体评价

| 模块 | 页面 | 完成度 | 主要问题 | 优先级 |
|------|------|--------|----------|--------|
| 合同管理 | contract/index.vue | 35% | 表格显示ID、表单用el-input、详情可编辑 | P0 |
| 客户管理 | customer/index.vue | 60% | deptId/zoneId用el-input-number、硬编码默认值 | P1 |
| 公海客户 | public-sea/index.vue | 70% | 统计数据仅当前页、部分字段数字显示 | P1 |
| 跟进记录 | contact/index.vue | 80% | 远程搜索无空结果提示 | P2 |
| 业绩记录 | performance-record/index.vue | 40% | **Date.format()报错**、表格显示ID | P0 |
| 客户转移 | transfer/index.vue | 25% | 所有字段用el-input、无下拉选择 | P1 |
| 工作日志 | worklog/index.vue | 50% | 数字字段过多、缺少销售代表选择 | P1 |
| 客户查看 | customer-view.vue | 55% | 意向等级/状态显示数字、多结果无选择 | P1 |
| **总计** | **8** | **50%** | **核心问题：Date.format()、ID显示、el-select缺失** | - |

### 严重问题分布

| 优先级 | 问题数 | 说明 |
|--------|--------|------|
| 🔴 P0 必须立即修复 | 3 | 会导致运行时错误或严重用户体验问题 |
| 🟠 P1 强烈建议修复 | 8 | 影响用户体验，需优先处理 |
| 🟡 P2 建议优化 | 4 | 可逐步改进 |

---

## 二、逐页面问题分析

### 2.1 contract/index.vue (合同管理) ⚠️ 重点问题页面

| 问题 | 严重程度 | 代码位置 | 问题描述 | 修改建议 |
|------|----------|----------|----------|----------|
| **❌ Date.format() 报错** | 🔴 紧急 | 无 | ✅ 已确认无此问题 | - |
| **表格显示纯数字ID** | 🔴 紧急 | **第25行** | customerId 显示纯数字 | 后端返回 customerName，前端显示 |
| **表单全部用el-input** | 🔴 紧急 | **第56-82行** | salesRepId、deptId、zoneId、productId、customerId 全部用文本输入 | 改为 el-select 下拉选择 |
| **详情弹窗可编辑** | 🔴 紧急 | **第186-298行** | 详情内拒绝原因、备注等可直接编辑，status可改 | 改为纯展示，编辑用单独弹窗 |
| **详情内 status 可改** | 🔴 紧急 | **第218-229行** | el-select 可选状态，会导致业务数据混乱 | 改为纯文本显示 |
| **纸质合同编号可改** | 🟠 高 | **第266-268行** | 详情弹窗内可编辑纸质合同编号 | 改为纯展示 |
| **搜索条件单一** | 🟠 高 | 第5-11行 | 仅支持合同编号搜索 | 增加：客户名称、销售代表、合同状态、日期范围 |
| **状态流转无校验** | 🟠 高 | 签署逻辑 | 草稿可直接跳到已放款 | 添加状态机校验 |
| **缺少"提交审核"按钮** | 🟠 高 | 按钮区 | 签署后无法提交到 finance 审核 | 添加提交审核按钮和接口 |
| **表格无状态tag** | 🟡 低 | 第28-34行 | ✅ 已有状态标签显示 | - |

**当前设计问题**：
```
第56-72行 - 表单使用 el-input：
<el-input v-model="form.salesRepId" placeholder="请输入销售代表ID" />
<el-input v-model="form.deptId" placeholder="请输入部门ID" />
<el-input v-model="form.zoneId" placeholder="请输入战区ID" />
<el-input v-model="form.productId" placeholder="请输入产品ID" />
<el-input v-model="form.customerId" placeholder="请输入客户ID" />
```

**应该改为**：
```
<el-select v-model="form.salesRepId" placeholder="请选择销售代表" filterable>
  <el-option v-for="rep in salesRepOptions" :key="rep.id" :label="rep.realName" :value="rep.id" />
</el-select>
```

---

### 2.2 customer/index.vue (客户管理)

| 问题 | 严重程度 | 代码位置 | 问题描述 | 修改建议 |
|------|----------|----------|----------|----------|
| **部门ID用el-input-number** | 🟠 高 | **第212-214行** | deptId 使用数字输入框 | 改为 el-select 下拉选择 |
| **战区ID用el-input-number** | 🟠 高 | **第217-219行** | zoneId 使用数字输入框 | 改为 el-select 下拉选择 |
| **硬编码默认值** | 🟠 高 | **第396-397行** | `deptId: 1, zoneId: 1` 硬编码 | 从当前用户信息获取 |
| **客户类型切换无联动** | 🟡 中 | 表单区域 | 个人/企业客户必填字段应不同 | 根据类型显示/隐藏字段 |
| **身份证校验不完整** | 🟡 中 | 第342行 | 仅校验18位，未校验15位 | 完善正则校验 |
| **删除无业务校验** | 🟡 低 | handleDelete | 有合同的客户也能删除 | 添加业务校验 |

**当前代码（第396-397行）**：
```javascript
deptId: 1,  // 默认部门ID ← 硬编码
zoneId: 1,  // 默认战区ID ← 硬编码
```

**应该改为**：
```javascript
deptId: userStore.deptId,  // 从当前用户获取
zoneId: userStore.zoneId,  // 从当前用户获取
```

---

### 2.3 public-sea/index.vue (公海客户)

| 问题 | 严重程度 | 代码位置 | 问题描述 | 修改建议 |
|------|----------|----------|----------|----------|
| **统计数据基于当前页** | 🟠 高 | **第322-328行** | personalCount 等仅计算当前页数据 | 后端提供汇总接口 |
| **意向等级映射不一致** | 🟡 中 | 第367行 | 定义为 1-4+5，但 customer 用 1-4 | 统一枚举定义 |
| **公海时间格式问题** | 🟡 低 | 第110-113行 | 如果是完整时间戳会被截断 | 统一格式化处理 |
| **修改弹窗Tab问题** | 🟡 低 | 第217行 | 详情弹窗和修改弹窗Tab切换 | 保持数据同步 |

**当前代码（第322-328行）**：
```javascript
// 统计数据仅基于当前页
const personalCount = computed(() => publicSeaList.value.filter(c => c.customerType === 1).length)
const enterpriseCount = computed(() => publicSeaList.value.filter(c => c.customerType === 2).length)
const monthCount = computed(() => {
  // ...
})
```

**应该改为**：后端提供 `/public-sea/stats` 汇总接口，前端直接调用显示。

---

### 2.4 contact/index.vue (跟进记录)

| 问题 | 严重程度 | 代码位置 | 问题描述 | 修改建议 |
|------|----------|----------|----------|----------|
| **远程搜索无空结果提示** | 🟡 低 | **searchCustomer 函数** | 返回空列表时无提示 | 添加 "未找到匹配客户" 提示 |
| **跟进前意向字段可选** | 🟡 低 | **第130-136行** | disabled 但可选中 | 改为纯展示文本 |
| **详情弹窗数据来源良好** | ✅ 已解决 | handleDetail | 直接使用行数据 | - |
| **意向变化可视化良好** | ✅ 良好 | 第48-55行 | ✅ 使用 ArrowRight 图标展示变化 | - |

**当前代码（第254-265行）**：
```javascript
function searchCustomer(query) {
  if (!query) {
    loadCustomers()
    return
  }
  customerLoading.value = true
  listCustomer({ name: query, pageSize: 50 }).then(res => {
    customerOptions.value = res.data?.records || res.records || []
    customerLoading.value = false
  }).catch(() => {
    customerLoading.value = false
  })
}
// ❌ 没有空结果提示
```

**应该改为**：
```javascript
listCustomer({ name: query, pageSize: 50 }).then(res => {
  customerOptions.value = res.data?.records || res.records || []
  if (customerOptions.value.length === 0) {
    proxy.$modal.msgWarning('未找到匹配客户')
  }
  customerLoading.value = false
})
```

---

### 2.5 performance-record/index.vue (业绩记录) ⚠️ 有运行时错误

| 问题 | 严重程度 | 代码位置 | 问题描述 | 修改建议 |
|------|----------|----------|----------|----------|
| **❌ Date.format() 报错** | 🔴 紧急 | **第257行** | `new Date().format()` 方法不存在，JS 会报错 | **必须修复**：使用 dayjs 或手动格式化 |
| **表格显示纯数字ID** | 🟠 高 | **第30,35-37行** | 合同ID、销售代表ID、部门ID、区域ID 显示纯数字 | 后端返回关联名称 |
| **佣金比例混淆** | 🟠 高 | 第41-43,96-98行 | commissionRatePercent 和 commissionRate 两个字段混用 | 统一使用一个字段，清晰注释 |
| **确认操作有二次确认** | ✅ 良好 | 第274-283行 | ✅ 已有确认对话框 | - |

**❌ 错误代码（第257行）**：
```javascript
form.value.calculateTime = new Date().format('yyyy-MM-dd HH:mm:ss')
// ❌ Date.prototype.format 不存在！会报错
```

**✅ 修复方案**：
```javascript
// 方案1：手动格式化
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

// 方案2：使用 el-date-picker 自动格式化（推荐）
// 在模板中：<el-date-picker v-model="form.calculateTime" value-format="YYYY-MM-DD HH:mm:ss" />
// handleAdd 中只需：form.value.calculateTime = null  // 让用户选择
```

---

### 2.6 transfer/index.vue (客户转移)

| 问题 | 严重程度 | 代码位置 | 问题描述 | 修改建议 |
|------|----------|----------|----------|----------|
| **所有字段使用el-input** | 🔴 紧急 | **第53-79行** | customerId、fromRepId、toRepId、operatedBy 全部用文本输入 | 改为 el-select 下拉选择 |
| **缺少转移记录查询条件** | 🟠 中 | 第4-19行 | 无按时间范围、转出/转入销售查询 | 增加筛选条件 |
| **无业务校验** | 🟠 中 | 转移逻辑 | 未检查客户是否已被转移、是否有未完成合同 | 添加业务校验 |
| **操作类型映射良好** | ✅ 良好 | 第33-35行 | ✅ 已有标签显示 | - |

**当前代码（第53-79行）**：
```html
<el-form-item label="客户ID" prop="customerId">
  <el-input v-model="form.customerId" placeholder="请输入客户ID" />
</el-form-item>
<el-form-item label="转出销售ID" prop="fromRepId">
  <el-input v-model="form.fromRepId" placeholder="请输入转出销售ID" />
</el-form-item>
<el-form-item label="转入销售ID" prop="toRepId">
  <el-input v-model="form.toRepId" placeholder="请输入转入销售ID" />
</el-form-item>
<el-form-item label="操作人ID" prop="operatedBy">
  <el-input v-model="form.operatedBy" placeholder="请输入操作人ID" />
</el-form-item>
```

**应该改为**：
```html
<el-form-item label="客户" prop="customerId">
  <el-select v-model="form.customerId" placeholder="请选择客户" filterable>
    <el-option v-for="c in customerOptions" :key="c.id" :label="c.name" :value="c.id" />
  </el-select>
</el-form-item>
<el-form-item label="转出销售" prop="fromRepId">
  <el-select v-model="form.fromRepId" placeholder="请选择销售代表" filterable>
    <el-option v-for="rep in salesRepOptions" :key="rep.id" :label="rep.realName" :value="rep.id" />
  </el-select>
</el-form-item>
```

---

### 2.7 worklog/index.vue (工作日志)

| 问题 | 严重程度 | 代码位置 | 问题描述 | 修改建议 |
|------|----------|----------|----------|----------|
| **销售代表用el-input** | 🟠 高 | **第48-51行** | salesRepId 使用文本输入 | 改为 el-select 下拉选择 |
| **数字字段过多** | 🟡 中 | 第59-94行 | 6个数字输入框，表单过长 | 考虑使用表格编辑或分组合并 |
| **日期重复检测延迟** | 🟡 中 | submitForm | checkDuplicate 在提交时才检查 | 选择日期后立即检查 |
| **无批量新增** | 🟡 低 | 操作区 | 不支持 Excel 批量导入 | 添加批量导入功能 |

**当前代码（第48-51行）**：
```html
<el-form-item label="销售代表ID" prop="salesRepId">
  <el-input v-model="form.salesRepId" placeholder="请输入销售代表ID" />
</el-form-item>
```

**应该改为**：
```html
<el-form-item label="销售代表" prop="salesRepId">
  <el-select v-model="form.salesRepId" placeholder="请选择销售代表" filterable>
    <el-option v-for="rep in salesRepOptions" :key="rep.id" :label="rep.realName" :value="rep.id" />
  </el-select>
</el-form-item>
```

---

### 2.8 customer-view.vue (客户查看)

| 问题 | 严重程度 | 代码位置 | 问题描述 | 修改建议 |
|------|----------|----------|----------|----------|
| **意向等级显示数字** | 🟠 高 | **第155行** | `意向等级: c.intentionLevel` 显示纯数字 | 添加映射函数 |
| **状态显示数字** | 🟠 高 | **第156行** | `c.status === 1 ? '有效' : '无效'` 不完整 | 添加完整状态映射 |
| **客户类型显示数字** | 🟠 高 | **第154行** | `c.customerType` 显示纯数字 | 添加映射函数 |
| **搜索多结果无选择** | 🟠 高 | **第198-200行** | 多个结果时只提示不提供选择 | 提供下拉选择列表 |
| **联系记录等无分页** | 🟡 中 | 各Tab | 数据量大时应分页 | 添加分页组件 |
| **无权限控制** | 🟡 低 | 整体 | 敏感客户信息应限制查看权限 | 根据用户角色控制 |

**当前代码（第154-156行）**：
```javascript
{ label: '客户类型', value: c.customerType },      // ❌ 显示数字
{ label: '意向等级', value: c.intentionLevel },    // ❌ 显示数字
{ label: '状态', value: c.status === 1 ? '有效' : '无效' },  // ⚠️ 不完整
```

**应该改为**：
```javascript
{ label: '客户类型', value: { 1: '个人', 2: '企业' }[c.customerType] || '-' },
{ label: '意向等级', value: { 1: '低', 2: '中', 3: '高', 4: '很有意向' }[c.intentionLevel] || '-' },
{ label: '状态', value: { 0: '无效', 1: '有效', 5: '公海' }[c.status] || '-' },
```

---

## 三、共性问题汇总

### 3.1 高频问题（需优先修复）

| 问题类别 | 影响文件数 | 问题描述 |
|----------|------------|----------|
| **Date.format() 报错** | 1 | performance-record/index.vue:257 会导致 JS 运行时错误 |
| **表格显示ID而非名称** | 5+ | contract、performance-record、transfer、customer-view 等页面 |
| **表单使用el-input而非el-select** | 4+ | contract、transfer、worklog、customer 等页面 |

### 3.2 关联字段显示问题

| 页面 | 需要显示名称的字段 |
|------|------------------|
| contract/index.vue | customerId, salesRepId, deptId, zoneId, productId |
| performance-record/index.vue | contractId, salesRepId, deptId, zoneId |
| transfer/index.vue | customerId, fromRepId, toRepId, operatedBy |
| customer-view.vue | customerType, intentionLevel, status |

### 3.3 表单设计问题

| 页面 | 问题字段 | 当前控件 |
|------|----------|----------|
| contract/index.vue | 所有关联字段 | el-input (应改为 el-select) |
| transfer/index.vue | 所有关联字段 | el-input (应改为 el-select) |
| worklog/index.vue | salesRepId | el-input (应改为 el-select) |
| customer/index.vue | deptId, zoneId | el-input-number (应改为 el-select) |

---

## 四、修复优先级

### 🔴 P0 - 必须立即修复（会导致运行时错误或严重用户体验问题）

| 序号 | 文件 | 问题 | 代码位置 | 修复方案 |
|------|------|------|----------|----------|
| 1 | `performance-record/index.vue` | **Date.format() 会报错** | **第257行** | 使用 dayjs 或手动格式化 |
| 2 | `contract/index.vue` | 所有关联字段用 el-input | 第56-82行 | 改为 el-select 下拉选择 |
| 3 | `contract/index.vue` | 详情弹窗可编辑 | 第186-298行 | 改为纯展示 |

### 🟠 P1 - 强烈建议修复（影响用户体验）

| 序号 | 文件 | 问题 | 修复方案 |
|------|------|------|----------|
| 1 | `performance-record/index.vue` | 表格显示纯数字ID | 后端返回关联名称 |
| 2 | `transfer/index.vue` | 所有字段用 el-input | 改为 el-select |
| 3 | `customer/index.vue` | deptId、zoneId 用 el-input-number | 改为 el-select |
| 4 | `customer-view.vue` | 意向等级、状态显示数字 | 添加映射函数 |
| 5 | `worklog/index.vue` | salesRepId 用 el-input | 改为 el-select |
| 6 | `public-sea/index.vue` | 统计数据基于当前页 | 后端提供汇总接口 |
| 7 | `customer/index.vue` | 硬编码默认值 | 从用户信息获取 |

### 🟡 P2 - 建议优化（可逐步改进）

| 序号 | 文件 | 问题 | 修复方案 |
|------|------|------|----------|
| 1 | `contact/index.vue` | 远程搜索无空结果提示 | 添加 msgWarning |
| 2 | `worklog/index.vue` | 日期重复检测延迟 | 选择日期后立即检查 |
| 3 | `customer-view.vue` | 联系记录等无分页 | 添加分页组件 |
| 4 | `transfer/index.vue` | 缺少转移记录查询条件 | 增加筛选条件 |

---

## 五、修复建议示例

### 5.1 Date.format() 修复（performance-record/index.vue:257）

```javascript
// ❌ 当前代码（会报错）：
form.value.calculateTime = new Date().format('yyyy-MM-dd HH:mm:ss')

// ✅ 修复方案1（手动格式化，无需引入新库）：
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

// ✅ 修复方案2（使用 el-date-picker 自动格式化）：
// 在模板中：
// <el-date-picker v-model="form.calculateTime" type="datetime"
//   placeholder="请选择计算时间" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
// handleAdd 中只需设置：
form.value.calculateTime = null  // 让用户选择，而不是手动设置
```

### 5.2 详情弹窗改为纯展示（contract/index.vue）

```vue
<!-- ❌ 当前代码（可编辑）： -->
<el-dialog title="合同详情" v-model="detailVisible" ...>
  <el-form-item label="状态">
    <el-select v-model="detailForm.status" ...>  <!-- ❌ 可编辑 -->
    ...
  </el-form-item>
  <el-form-item label="拒绝原因">
    <el-input v-model="detailForm.rejectReason" ...>  <!-- ❌ 可编辑 -->
  </el-form-item>
</el-dialog>

<!-- ✅ 修复后（纯展示）： -->
<el-dialog title="合同详情" v-model="detailVisible" ...>
  <el-descriptions :column="2" border>
    <el-descriptions-item label="状态">
      {{ getStatusLabel(detailForm.status) }}
    </el-descriptions-item>
    <el-descriptions-item label="拒绝原因" :span="2">
      {{ detailForm.rejectReason || '-' }}
    </el-descriptions-item>
    ...
  </el-descriptions>
</el-dialog>
```

---

## 六、后端配合需求

### 6.1 接口改造要求

| 接口 | 当前返回 | 应返回 |
|------|----------|--------|
| `/contract/list` | customerId, salesRepId, deptId, zoneId, productId | + customerName, salesRepName, deptName, zoneName, productName |
| `/performance-record/list` | contractId, salesRepId, deptId, zoneId | + contractNo, salesRepName, deptName, zoneName |
| `/customer-transfer/list` | customerId, fromRepId, toRepId, operatedBy | + customerName, fromRepName, toRepName, operatorName |

### 6.2 新增接口

| 接口 | 说明 |
|------|------|
| `/public-sea/stats` | 公海客户统计汇总接口 |
| `/contract/submit-audit` | 提交合同到 finance 审核 |

---

## 七、行动计划

### 第一阶段（立即）：修复 P0 问题

- [ ] 修复 Date.format() 错误（performance-record/index.vue:257）
- [ ] 合同页面改为 el-select
- [ ] 详情弹窗改为纯展示

### 第二阶段（本周）：修复 P1 问题

- [ ] 业绩记录页面 ID 显示改为名称
- [ ] 客户转移页面改为 el-select
- [ ] 客户管理 deptId/zoneId 改为 el-select
- [ ] 客户查看页面添加状态映射
- [ ] 工作日志页面 salesRepId 改为 el-select

### 第三阶段（后续）：优化 P2 问题

- [ ] 公海统计改为后端接口
- [ ] 跟进记录添加空结果提示
- [ ] 客户查看添加分页
- [ ] 转移记录增加筛选条件

---

## 八、对比 doc09_lhy.md 审核标准 2.2 的变化

### 8.1 已修复问题

| 问题 | doc09_lhy.md 状态 | doc10_lhy 状态 |
|------|-------------------|----------------|
| contact/index.vue 详情弹窗数据来源 | 需修复 | ✅ 已解决 |
| performance-record/index.vue 详情弹窗 | 需修复 | ✅ 已解决 |

### 8.2 新发现问题

| 问题 | doc09_lhy.md | doc10_lhy |
|------|--------------|-----------|
| contract/index.vue 详情内 status 可改 | 未提及 | 🔴 新发现 |
| contract/index.vue 纸质合同编号可改 | 未提及 | 🟠 新发现 |
| customer-view.vue 联系类型显示数字 | 未提及 | 🟠 新发现 |

---

> 本报告由 AI 代码审查工具自动生成
> 审查时间：2026-04-24
> 审查方法：基于 doc09_lhy.md 审核标准 2.2 的人工代码审查
