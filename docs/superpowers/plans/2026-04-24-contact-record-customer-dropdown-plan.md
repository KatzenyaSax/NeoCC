# 跟进记录页面客户下拉及权限控制实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在跟进记录页面实现：客户ID字段改为搜索式下拉菜单，新增按钮对销售代表和超级管理员都可点击，但提交时仅销售代表可提交

**Architecture:** 修改 `ruoyi-ui/src/views/sales/contact/index.vue`，参考 worklog 页面的下拉实现方式

**Tech Stack:** Vue 3 + Element Plus + Pinia

---

### Task 1: 添加客户下拉相关数据和方法

**Files:**
- Modify: `ruoyi-ui/src/views/sales/contact/index.vue`

- [ ] **Step 1: 添加 customerOptions 和 customerLoading 状态**

在 `script setup` 中 `customerLoading = ref(false)` 后添加：
```javascript
const customerOptions = ref([])
const customerLoading = ref(false)
```

- [ ] **Step 2: 添加 loadCustomerOptions 函数**

在 `handleUpdate` 函数后添加：
```javascript
function loadCustomerOptions(searchValue) {
  customerLoading.value = true
  listCustomer({ pageNum: 1, pageSize: 100, name: searchValue || '' }).then(response => {
    const records = response.data?.records || response.records || []
    customerOptions.value = records.map(c => ({
      id: c.id,
      realName: c.name
    }))
    customerLoading.value = false
  }).catch(() => {
    customerLoading.value = false
  })
}
```

- [ ] **Step 3: 在 handleAdd 中调用 loadCustomerOptions**

修改 `handleAdd` 函数：
```javascript
function handleAdd() {
  reset()
  loadCustomerOptions('')
  open.value = true
  title.value = "新增跟进记录"
}
```

- [ ] **Step 4: 在 handleUpdate 中调用 loadCustomerOptions**

修改 `handleUpdate` 函数：
```javascript
function handleUpdate(row) {
  reset()
  getContactRecord(row.id).then(response => {
    form.value = response.data || response
    loadCustomerOptions('')
    open.value = true
    title.value = "修改跟进记录"
  })
}
```

---

### Task 2: 将表单中的客户ID输入框改为下拉选择

**Files:**
- Modify: `ruoyi-ui/src/views/sales/contact/index.vue:57-59`

- [ ] **Step 1: 替换客户ID表单项为下拉选择**

将：
```html
<el-form-item label="客户ID" prop="customerId">
  <el-input v-model="form.customerId" placeholder="请输入客户ID" />
</el-form-item>
```

替换为：
```html
<el-form-item label="客户" prop="customerId">
  <el-select
    v-model="form.customerId"
    placeholder="请选择客户"
    filterable
    :loading="customerLoading"
    @Focus="loadCustomerOptions('')"
    :remote="true"
    :remote-method="loadCustomerOptions"
    style="width: 100%">
    <el-option
      v-for="item in customerOptions"
      :key="item.id"
      :label="item.realName"
      :value="item.id"
    />
  </el-select>
</el-form-item>
```

---

### Task 3: 添加角色判断和提交权限控制

**Files:**
- Modify: `ruoyi-ui/src/views/sales/contact/index.vue`

- [ ] **Step 1: 导入 useUserStore**

确认 import 已有：
```javascript
import useUserStore from '@/store/modules/user'
```

- [ ] **Step 2: 添加 isSalesRepRole 辅助函数**

在 `intentionLabel` 函数后添加：
```javascript
function isSalesRepRole() {
  const roles = userStore.roles || []
  return roles.some(r => r === 'ROLE_sales_rep')
}
```

- [ ] **Step 3: 修改 submitForm 添加权限检查**

将 `submitForm` 函数修改为：
```javascript
function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      // 如果是销售代表角色，自动填充当前用户的ID
      if (isSalesRepRole()) {
        form.value.salesRepId = userStore.id
        const fn = form.value.id ? updateContactRecord : addContactRecord
        fn(form.value).then(() => {
          proxy.$modal.msgSuccess(form.value.id ? "修改成功" : "新增成功")
          open.value = false
          getList()
        })
      } else {
        // 非销售代表角色不能提交
        proxy.$modal.msgError("您不是销售代表！")
      }
    }
  })
}
```

---

### Task 4: 验证和测试

**Files:**
- None (manual testing)

- [ ] **Step 1: 打开浏览器访问跟进记录页面**

URL: http://localhost/dev-api/sales/contact (或实际部署地址)

- [ ] **Step 2: 测试新增按钮**

以超级管理员身份登录：点击新增按钮，应该能打开弹窗且客户下拉可以搜索
以销售代表身份登录：点击新增按钮，应该能打开弹窗

- [ ] **Step 3: 测试修改功能**

点击修改按钮，弹窗中客户应该显示为下拉菜单且可搜索

- [ ] **Step 4: 测试提交权限**

以销售代表身份登录：提交表单应该成功，salesRepId 自动填充为当前用户ID
以超级管理员身份登录：点击提交应该弹窗提示"您不是销售代表！"

---

## 验证清单

- [ ] 客户ID字段已改为搜索式下拉菜单
- [ ] 新增弹窗打开时加载全部客户列表
- [ ] 修改弹窗打开时加载全部客户列表
- [ ] 下拉菜单支持输入文本搜索客户
- [ ] 销售代表可以正常提交表单（salesRepId自动填充）
- [ ] 超级管理员提交时提示"您不是销售代表！"并阻止提交
