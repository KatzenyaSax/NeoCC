# 客户管理 - 对接销售功能设计

## 概述

在客户管理页面（销售管理/客户管理）实现：
1. 表格中"身份证号"列替换为"对接销售"列，显示销售代表的真实姓名
2. 新增/修改客户对话框中添加"对接销售"下拉框，可选择 roleId=4（销售代表）的用户

## 架构

```
前端 customer/index.vue
    │
    ├── GET /api/customer/page  (已有)
    │       └── 拿到 customerList（含 salesRepId）
    │
    ├── 批量调用 /api/sysUser/names/by-ids  (已有 Feign)
    │       └── 返回 Map<Long, String> (id → realName)
    │
    └── 在前端合并：customerList[i].salesRepName = nameMap[salesRepId]
```

## 后端改动

### 1. 前端 API - ruoyi-ui/src/api/sales/customer.js

新增：

```js
// 根据用户ID列表批量查询姓名
export function getUserNamesByIds(ids) {
  return request({
    url: '/user/names/by-ids',
    method: 'post',
    data: ids
  })
}
```

### 2. 前端页面 - ruoyi-ui/src/views/sales/customer/index.vue

**表格列替换：**
- 把 `<el-table-column label="身份证号" align="center" prop="idCard" />` 替换为：
```html
<el-table-column label="对接销售" align="center">
  <template #default="scope">
    {{ scope.row.salesRepName || '-' }}
  </template>
</el-table-column>
```

**新增/修改对话框 - 对接销售下拉框：**
在表单中添加（放在"状态"之前）：
```html
<el-form-item label="对接销售" prop="salesRepId">
  <el-select v-model="form.salesRepId" placeholder="请选择对接销售" clearable>
    <el-option
      v-for="item in salesRepOptions"
      :key="item.id"
      :label="item.realName"
      :value="item.id"
    />
  </el-select>
</el-form-item>
```

**Script 部分新增：**
```js
import { listSalesReps } from "@/api/system/user"  // 已有接口

// 对接销售下拉选项
const salesRepOptions = ref([])

// 加载销售代表下拉选项
function loadSalesRepOptions() {
  listSalesReps().then(res => {
    salesRepOptions.value = res.data || []
  })
}

// 在 getList() 成功后，批量查询姓名
function getList() {
  loading.value = true
  listCustomer(queryParams.value).then(response => {
    // ... 现有过滤逻辑 ...
    customerList.value = records
    total.value = records.length
    loading.value = false

    // 批量查询销售代表姓名
    const repIds = records
      .map(r => r.salesRepId)
      .filter(id => id != null)
      .filter((v, i, a) => a.indexOf(v) === i) // 去重
    if (repIds.length > 0) {
      getUserNamesByIds(repIds).then(res => {
        const nameMap = res.data || {}
        customerList.value.forEach(c => {
          c.salesRepName = nameMap[c.salesRepId] || null
        })
      })
    }
  })
}
```

**在 onMounted 或 getList 之前调用：**
```js
loadSalesRepOptions()
```

### 3. form 重置中也要清理 salesRepId

```js
function reset() {
  form.value = {
    // ... 现有字段 ...
    salesRepId: undefined,  // 新增
  }
}
```

## 文件清单

| 文件 | 改动 |
|------|------|
| `ruoyi-ui/src/api/sales/customer.js` | 新增 `getUserNamesByIds` |
| `ruoyi-ui/src/views/sales/customer/index.vue` | 表格列替换 + 下拉框 + 批量查姓名逻辑 |

## 注意

- 销售代表下拉数据直接复用 `listSalesReps` 接口（已通过 role_code='sales_rep' 过滤）
- 姓名批量查询用已有的 `POST /api/sysUser/names/by-ids` 接口
- 表格中的姓名是前端合并的，不影响现有接口
