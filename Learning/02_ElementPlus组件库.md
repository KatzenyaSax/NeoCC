# Element Plus 组件库入门指南

> 本文档讲解 Element Plus 的核心组件，帮助你快速构建漂亮的界面。

---

## 目录

1. [什么是 Element Plus？](#1-什么是-element-plus)
2. [快速开始](#2-快速开始)
3. [Layout 布局](#3-layout-布局)
4. [Form 表单](#4-form-表单)
5. [Table 表格](#5-table-表格)
6. [Dialog 对话框](#6-dialog-对话框)
7. [Message 消息提示](#7-message-消息提示)
8. [分页组件](#8-分页组件)
9. [综合实战：CRUD 列表页面](#9-综合实战crud-列表页面)

---

## 1. 什么是 Element Plus？

### 1.1 官方定义

Element Plus 是一套基于 **Vue 3** 的桌面端组件库，提供了丰富的 UI 组件，帮助开发者快速构建界面。

### 1.2 类比理解

想象你要装修房子：
- **HTML/CSS** = 原始的水泥、砖块
- **Element Plus** = 预制好的家具、门窗（直接使用，不用自己从头做）

### 1.3 组件一览

```
┌─────────────────────────────────────────────────────────────┐
│                    Element Plus 组件分类                     │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  基础组件:     Button 按钮  |  Icon 图标  |  Link 链接      │
│                                                             │
│  布局组件:     Layout 布局  |  Container 容器 |  Row/Col    │
│                                                             │
│  表单组件:     Input 输入框 |  Select 选择器 |  Switch 开关  │
│                Radio 单选   |  Checkbox 多选 |  DatePicker  │
│                                                             │
│  数据展示:     Table 表格   |  Descriptions 描述列表        │
│                Tag 标签     |  Progress 进度条 | Card 卡片   │
│                                                             │
│  反馈组件:     Dialog 对话框 |  Message 消息提示             │
│                MessageBox 消息框 |  Notification 通知      │
│                                                             │
│  导航组件:     Menu 菜单    |  Tabs 标签页  |  Breadcrumb  │
│                                                             │
│  业务组件:     Form 表单（整合） |  Pagination 分页        │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. 快速开始

### 2.1 安装

```bash
npm install element-plus
```

### 2.2 引入方式

**方式一：全局引入（简单，但包体积大）**

```javascript
// main.js
import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'

const app = createApp(App)

app.use(ElementPlus)
app.mount('#app')
```

**方式二：按需引入（推荐，省体积）**

```javascript
// main.js
import { createApp } from 'vue'
import App from './App.vue'

// 按需引入需要的组件
import { 
  ElButton, 
  ElInput, 
  ElForm, 
  ElFormItem,
  ElTable,
  ElDialog
} from 'element-plus'

const app = createApp(App)

// 手动注册组件
app.use(ElButton)
app.use(ElInput)
app.use(ElForm)
app.use(ElFormItem)
// ...

app.mount('#app')
```

### 2.3 项目中使用

```vue
<template>
  <div>
    <el-button type="primary">主要按钮</el-button>
    <el-button type="success">成功按钮</el-button>
  </div>
</template>

<script setup>
import { ElButton } from 'element-plus'
</script>
```

---

## 3. Layout 布局

### 3.1 Row 和 Col 栅格系统

Element Plus 使用 **24 栅格系统**，把屏幕分成 24 列。

```
屏幕宽度 24 等分：

|  1  |  2  |  3  |  4  | ... | 23 | 24 |
|-----|-----|-----|-----|-----|-----|-----|
| <--- 6 列 ---> | <--- 18 列 ---> |
|     lg=6        |      lg=18       |
```

### 3.2 基础用法

```vue
<template>
  <div>
    <h3>基础布局</h3>
    <el-row>
      <el-col :span="24"><div class="grid-content bg-purple-dark">24 列 (整行)</div></el-col>
    </el-row>
    
    <el-row>
      <el-col :span="12"><div class="grid-content bg-purple">12 列</div></el-col>
      <el-col :span="12"><div class="grid-content bg-light">12 列</div></el-col>
    </el-row>
    
    <el-row>
      <el-col :span="8"><div class="grid-content bg-purple">8 列</div></el-col>
      <el-col :span="8"><div class="grid-content bg-light">8 列</div></el-col>
      <el-col :span="8"><div class="grid-content bg-purple">8 列</div></el-col>
    </el-row>
  </div>
</template>

<style scoped>
.grid-content {
  border-radius: 4px;
  min-height: 36px;
  padding: 20px;
  text-align: center;
  color: white;
}
.bg-purple-dark { background: #99a9bf; }
.bg-purple { background: #d3dce6; }
.bg-light { background: #e5e9f2; }
</style>
```

### 3.3 响应式布局

```vue
<template>
  <div>
    <el-row :gutter="20">
      <!-- 
        xs: < 768px  手机
        sm: >= 768px  平板
        md: >= 992px  小桌面
        lg: >= 1200px 大桌面
        xl: >= 1920px 超大桌面
      -->
      <el-col :xs="24" :sm="12" :md="8" :lg="6" :xl="4">
        <div class="card">内容1</div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8" :lg="6" :xl="4">
        <div class="card">内容2</div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8" :lg="6" :xl="4">
        <div class="card">内容3</div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8" :lg="6" :xl="4">
        <div class="card">内容4</div>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.card {
  background: #f5f5f5;
  padding: 20px;
  margin-bottom: 10px;
  text-align: center;
}
</style>
```

### 3.4 Row 的 gutter 属性

```vue
<template>
  <!-- gutter: 列之间的间距（单位 px） -->
  <el-row :gutter="20">
    <el-col :span="12">左侧内容（有20px间距）</el-col>
    <el-col :span="12">右侧内容</el-col>
  </el-row>
</template>
```

---

## 4. Form 表单

### 4.1 基础表单结构

```vue
<template>
  <div>
    <h3>基础表单</h3>
    
    <!-- el-form: 表单容器 -->
    <!-- model: 表单数据对象 -->
    <!-- rules: 表单验证规则 -->
    <el-form 
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
    >
      <!-- el-form-item: 表单项 -->
      <!-- label: 标签文本 -->
      <!-- prop: 对应 model 中的字段名 -->
      <el-form-item label="用户名" prop="username">
        <el-input v-model="form.username" placeholder="请输入用户名" />
      </el-form-item>
      
      <el-form-item label="密码" prop="password">
        <el-input v-model="form.password" type="password" placeholder="请输入密码" />
      </el-form-item>
      
      <el-form-item>
        <el-button type="primary" @click="submitForm">提交</el-button>
        <el-button @click="resetForm">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'

const formRef = ref()
const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 10, message: '长度在 3 到 10 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' }
  ]
}

function submitForm() {
  formRef.value.validate((valid) => {
    if (valid) {
      console.log('表单数据:', form)
      ElMessage.success('提交成功')
    } else {
      console.log('表单验证失败')
      return false
    }
  })
}

function resetForm() {
  formRef.value.resetFields()
}
</script>

<script>
import { ElMessage } from 'element-plus'
export default {
  methods: {
    // 在 Vue 3 的 <script setup> 中不需要这样
    // 但如果你使用 Options API，需要这样导入
  }
}
</script>
```

### 4.2 输入框 Input

```vue
<template>
  <div>
    <h3>输入框示例</h3>
    
    <!-- 基础输入框 -->
    <el-form-item label="基础输入">
      <el-input v-model="form.text" placeholder="请输入" />
    </el-form-item>
    
    <!-- 密码输入 -->
    <el-form-item label="密码">
      <el-input v-model="form.password" type="password" show-password />
    </el-form-item>
    
    <!-- 禁用状态 -->
    <el-form-item label="禁用">
      <el-input v-model="form.disabled" disabled placeholder="不可输入" />
    </el-form-item>
    
    <!-- 带前缀图标 -->
    <el-form-item label="前缀">
      <el-input v-model="form.prefix" placeholder="请输入">
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </el-form-item>
    
    <!-- 带后缀图标 -->
    <el-form-item label="后缀">
      <el-input v-model="form.suffix" placeholder="请输入">
        <template #suffix>
          <el-icon><Calendar /></el-icon>
        </template>
      </el-input>
    </el-form-item>
    
    <!-- 可清空 -->
    <el-form-item label="可清空">
      <el-input v-model="form.clearable" clearable placeholder="可清空" />
    </el-form-item>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { Search, Calendar } from '@element-plus/icons-vue'

const form = reactive({
  text: '',
  password: '',
  disabled: '不可修改的内容',
  prefix: '',
  suffix: '',
  clearable: ''
})
</script>
```

### 4.3 选择器 Select

```vue
<template>
  <div>
    <h3>选择器示例</h3>
    
    <!-- 基础单选 -->
    <el-form-item label="客户类型">
      <el-select v-model="form.customerType" placeholder="请选择">
        <el-option label="个人" :value="1" />
        <el-option label="企业" :value="2" />
      </el-select>
    </el-form-item>
    
    <!-- 多选 -->
    <el-form-item label="多选">
      <el-select v-model="form.hobbies" multiple placeholder="请选择爱好">
        <el-option label="篮球" :value="1" />
        <el-option label="足球" :value="2" />
        <el-option label="游泳" :value="3" />
        <el-option label="跑步" :value="4" />
      </el-select>
    </el-form-item>
    
    <!-- 可搜索 -->
    <el-form-item label="可搜索">
      <el-select v-model="form.city" filterable placeholder="请选择城市">
        <el-option label="北京" :value="1" />
        <el-option label="上海" :value="2" />
        <el-option label="广州" :value="3" />
        <el-option label="深圳" :value="4" />
        <el-option label="杭州" :value="5" />
        <el-option label="成都" :value="6" />
      </el-select>
    </el-form-item>
  </div>
</template>

<script setup>
import { reactive } from 'vue'

const form = reactive({
  customerType: '',
  hobbies: [],
  city: ''
})
</script>
```

### 4.4 日期选择器 DatePicker

```vue
<template>
  <div>
    <h3>日期选择器示例</h3>
    
    <!-- 日期选择 -->
    <el-form-item label="选择日期">
      <el-date-picker
        v-model="form.date"
        type="date"
        placeholder="选择日期"
        value-format="YYYY-MM-DD"
      />
    </el-form-item>
    
    <!-- 日期范围 -->
    <el-form-item label="日期范围">
      <el-date-picker
        v-model="form.dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="YYYY-MM-DD"
      />
    </el-form-item>
    
    <!-- 日期时间选择 -->
    <el-form-item label="日期时间">
      <el-date-picker
        v-model="form.dateTime"
        type="datetime"
        placeholder="选择日期时间"
        value-format="YYYY-MM-DD HH:mm:ss"
      />
    </el-form-item>
  </div>
</template>

<script setup>
import { reactive } from 'vue'

const form = reactive({
  date: '',
  dateRange: [],
  dateTime: ''
})
</script>
```

### 4.5 数字输入框 InputNumber

```vue
<template>
  <div>
    <h3>数字输入框示例</h3>
    
    <!-- 基础 -->
    <el-form-item label="数量">
      <el-input-number v-model="form.count" :min="1" :max="100" />
    </el-form-item>
    
    <!-- 精度 -->
    <el-form-item label="金额">
      <el-input-number 
        v-model="form.amount" 
        :min="0" 
        :precision="2"
        :step="0.1"
        placeholder="请输入金额"
      />
    </el-form-item>
    
    <!-- 禁用 -->
    <el-form-item label="禁用">
      <el-input-number v-model="form.disabled" :min="0" disabled />
    </el-form-item>
  </div>
</template>

<script setup>
import { reactive } from 'vue'

const form = reactive({
  count: 1,
  amount: 0,
  disabled: 10
})
</script>
```

### 4.6 开关 Switch

```vue
<template>
  <div>
    <h3>开关示例</h3>
    
    <el-form-item label="是否启用">
      <el-switch v-model="form.enabled" />
    </el-form-item>
    
    <el-form-item label="是否付费">
      <el-switch 
        v-model="form.isPaid"
        active-text="是"
        inactive-text="否"
      />
    </el-form-item>
  </div>
</template>

<script setup>
import { reactive } from 'vue'

const form = reactive({
  enabled: false,
  isPaid: true
})
</script>
```

### 4.7 单选 Radio 和多选 Checkbox

```vue
<template>
  <div>
    <h3>单选和多选示例</h3>
    
    <!-- 单选 -->
    <el-form-item label="性别">
      <el-radio-group v-model="form.gender">
        <el-radio :label="1">男</el-radio>
        <el-radio :label="0">女</el-radio>
        <el-radio :label="-1">保密</el-radio>
      </el-radio-group>
    </el-form-item>
    
    <!-- 多选 -->
    <el-form-item label="爱好">
      <el-checkbox-group v-model="form.hobbies">
        <el-checkbox :label="1">篮球</el-checkbox>
        <el-checkbox :label="2">足球</el-checkbox>
        <el-checkbox :label="3">游泳</el-checkbox>
      </el-checkbox-group>
    </el-form-item>
  </div>
</template>

<script setup>
import { reactive } from 'vue'

const form = reactive({
  gender: 1,
  hobbies: [1, 2]
})
</script>
```

---

## 5. Table 表格

### 5.1 基础表格

```vue
<template>
  <div>
    <h3>基础表格</h3>
    
    <el-table :data="tableData" stripe border>
      <!-- el-table-column: 列 -->
      <!-- prop: 对应数据字段 -->
      <!-- label: 列标题 -->
      <!-- width: 列宽 -->
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="name" label="姓名" />
      <el-table-column prop="age" label="年龄" width="100" align="center" />
      <el-table-column prop="address" label="地址" />
    </el-table>
  </div>
</template>

<script setup>
const tableData = [
  { id: 1, name: '张三', age: 25, address: '北京市朝阳区' },
  { id: 2, name: '李四', age: 30, address: '上海市浦东新区' },
  { id: 3, name: '王五', age: 28, address: '广州市天河区' }
]
</script>
```

### 5.2 带操作列的表格

```vue
<template>
  <div>
    <h3>带操作按钮的表格</h3>
    
    <el-table :data="tableData" stripe border>
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="name" label="姓名" />
      <el-table-column prop="status" label="状态">
        <!-- 自定义列内容：使用作用域插槽 -->
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
            {{ scope.row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" align="center">
        <template #default="scope">
          <el-button link type="primary" @click="handleView(scope.row)">查看</el-button>
          <el-button link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const tableData = ref([
  { id: 1, name: '张三', status: 1 },
  { id: 2, name: '李四', status: 0 },
  { id: 3, name: '王五', status: 1 }
])

function handleView(row) {
  console.log('查看:', row)
}

function handleEdit(row) {
  console.log('编辑:', row)
}

function handleDelete(row) {
  console.log('删除:', row)
}
</script>
```

### 5.3 带搜索的表格

```vue
<template>
  <div>
    <h3>带搜索的表格</h3>
    
    <!-- 搜索区域 -->
    <el-form :inline="true" :model="queryParams">
      <el-form-item label="姓名">
        <el-input v-model="queryParams.name" placeholder="请输入姓名" clearable />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable>
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>
    
    <!-- 表格 -->
    <el-table :data="tableData" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="姓名" />
      <el-table-column prop="status" label="状态">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'

const loading = ref(false)
const queryParams = reactive({
  name: '',
  status: null
})
const tableData = ref([])

function handleQuery() {
  loading.value = true
  // 模拟请求
  setTimeout(() => {
    tableData.value = [
      { id: 1, name: '张三', status: 1 },
      { id: 2, name: '李四', status: 0 }
    ]
    loading.value = false
  }, 500)
}

function handleReset() {
  queryParams.name = ''
  queryParams.status = null
  handleQuery()
}

onMounted(() => {
  handleQuery()
})
</script>
```

### 5.4 表格属性说明

| 属性 | 说明 |
|------|------|
| `data` | 表格数据数组 |
| `stripe` | 是否为斑马纹表格 |
| `border` | 是否带有边框 |
| `v-loading` | 加载状态 |
| `height` | 固定表头（设置高度） |

---

## 6. Dialog 对话框

### 6.1 基础对话框

```vue
<template>
  <div>
    <el-button type="primary" @click="dialogVisible = true">打开对话框</el-button>
    
    <el-dialog
      v-model="dialogVisible"
      title="提示"
      width="500px"
      @close="handleClose"
    >
      <span>这是一段信息</span>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="dialogVisible = false">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const dialogVisible = ref(false)

function handleClose() {
  console.log('对话框关闭了')
}
</script>
```

### 6.2 表单对话框

```vue
<template>
  <div>
    <el-button type="primary" @click="openDialog">新增</el-button>
    
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      append-to-body
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'

const dialogVisible = ref(false)
const dialogTitle = ref('新增')
const formRef = ref()
const isEdit = ref(false)

const form = reactive({
  id: undefined,
  username: '',
  email: '',
  status: 1
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

function openDialog() {
  resetForm()
  dialogTitle.value = '新增'
  isEdit.value = false
  dialogVisible.value = true
}

function submitForm() {
  formRef.value.validate((valid) => {
    if (valid) {
      console.log('提交数据:', form)
      dialogVisible.value = false
    }
  })
}

function resetForm() {
  formRef.value?.resetFields()
  Object.assign(form, {
    id: undefined,
    username: '',
    email: '',
    status: 1
  })
}
</script>
```

---

## 7. Message 消息提示

### 7.1 全局引入

```javascript
// main.js
import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'

const app = createApp(App)
app.use(ElementPlus)
app.mount('#app')
```

### 7.2 使用方式

```vue
<template>
  <div>
    <el-button @click="showSuccess">成功</el-button>
    <el-button @click="showWarning">警告</el-button>
    <el-button @click="showError">错误</el-button>
    <el-button @click="showInfo">消息</el-button>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'

function showSuccess() {
  ElMessage.success('操作成功！')
}

function showWarning() {
  ElMessage.warning('请注意，这只是一个警告')
}

function showError() {
  ElMessage.error('操作失败！')
}

function showInfo() {
  ElMessage.info('这是一条普通消息')
}
</script>
```

### 7.3 高级用法

```vue
<script setup>
import { ElMessage } from 'element-plus'

// 带持续时间（默认 3 秒）
ElMessage.success({ message: '5秒后消失', duration: 5000 })

// 可关闭
ElMessage.success({ 
  message: '可以手动关闭', 
  showClose: true 
})

// 使用 Promise
ElMessage.success('操作成功')
  .then(() => {
    console.log('消息已关闭')
  })
</script>
```

---

## 8. 分页组件

### 8.1 基础用法

```vue
<template>
  <div>
    <h3>分页示例</h3>
    
    <el-table :data="tableData" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="姓名" />
    </el-table>
    
    <!-- 分页组件 -->
    <!-- v-model:current-page: 当前页 -->
    <!-- v-model:page-size: 每页条数 -->
    <!-- total: 总条数 -->
    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(100)
const tableData = ref([])

function handleSizeChange(val) {
  console.log('每页条数变化:', val)
  pageSize.value = val
  loadData()
}

function handleCurrentChange(val) {
  console.log('当前页变化:', val)
  currentPage.value = val
  loadData()
}

function loadData() {
  // 模拟请求
  console.log('加载数据:', {
    page: currentPage.value,
    size: pageSize.value
  })
}

onMounted(() => {
  loadData()
})
</script>
```

### 8.2 layout 属性说明

| 值 | 说明 |
|---|------|
| `total` | 显示总条数 |
| `sizes` | 显示每页条数选择器 |
| `prev` | 上一页按钮 |
| `pager` | 页码列表 |
| `next` | 下一页按钮 |
| `jumper` | 跳转输入框 |

---

## 9. 综合实战：CRUD 列表页面

### 9.1 完整代码

```vue
<template>
  <div class="user-management">
    <h2>用户管理</h2>
    
    <!-- 搜索区域 -->
    <el-form :inline="true" :model="queryParams" class="search-form">
      <el-form-item label="用户名">
        <el-input 
          v-model="queryParams.name" 
          placeholder="请输入用户名" 
          clearable 
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable>
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
        <el-button icon="Refresh" @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>
    
    <!-- 操作按钮 -->
    <el-row class="mb8">
      <el-button type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
    </el-row>
    
    <!-- 表格 -->
    <el-table 
      v-loading="loading" 
      :data="tableData" 
      stripe 
      border
    >
      <el-table-column label="ID" prop="id" width="80" align="center" />
      <el-table-column label="用户名" prop="username" />
      <el-table-column label="邮箱" prop="email" />
      <el-table-column label="手机" prop="phone" />
      <el-table-column label="状态" prop="status" width="100" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="180" />
      <el-table-column label="操作" width="200" align="center">
        <template #default="scope">
          <el-button link type="primary" @click="handleDetail(scope.row)">详情</el-button>
          <el-button link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 分页 -->
    <el-pagination
      v-model:current-page="queryParams.pageNum"
      v-model:page-size="queryParams.pageSize"
      :total="total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next"
      class="pagination"
      @pagination="getList"
    />
    
    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      append-to-body
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="用户详情"
      width="600px"
      append-to-body
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="ID">{{ detailForm.id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ detailForm.username }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ detailForm.email }}</el-descriptions-item>
        <el-descriptions-item label="手机">{{ detailForm.phone }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailForm.status === 1 ? 'success' : 'danger'">
            {{ detailForm.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailForm.createTime }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// 响应式数据
const loading = ref(false)
const total = ref(0)
const tableData = ref([])
const dialogVisible = ref(false)
const detailVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref()
const detailForm = ref({})

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  name: '',
  status: null
})

// 表单数据
const form = reactive({
  id: undefined,
  username: '',
  email: '',
  phone: '',
  status: 1
})

// 表单验证规则
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

// 模拟数据
const mockData = [
  { id: 1, username: 'admin', email: 'admin@example.com', phone: '13800138000', status: 1, createTime: '2024-01-01 10:00:00' },
  { id: 2, username: 'user1', email: 'user1@example.com', phone: '13800138001', status: 1, createTime: '2024-01-02 10:00:00' },
  { id: 3, username: 'user2', email: 'user2@example.com', phone: '13800138002', status: 0, createTime: '2024-01-03 10:00:00' },
]

// 加载列表
function getList() {
  loading.value = true
  setTimeout(() => {
    tableData.value = mockData
    total.value = mockData.length
    loading.value = false
  }, 500)
}

// 查询
function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

// 重置
function handleReset() {
  queryParams.name = ''
  queryParams.status = null
  handleQuery()
}

// 新增
function handleAdd() {
  reset()
  dialogTitle.value = '新增用户'
  isEdit.value = false
  dialogVisible.value = true
}

// 编辑
function handleEdit(row) {
  Object.assign(form, row)
  dialogTitle.value = '编辑用户'
  isEdit.value = true
  dialogVisible.value = true
}

// 详情
function handleDetail(row) {
  detailForm.value = { ...row }
  detailVisible.value = true
}

// 删除
function handleDelete(row) {
  ElMessageBox.confirm(`是否确认删除用户"${row.username}"？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('删除成功')
    getList()
  }).catch(() => {})
}

// 提交表单
function submitForm() {
  formRef.value.validate((valid) => {
    if (valid) {
      if (isEdit.value) {
        ElMessage.success('修改成功')
      } else {
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      getList()
    }
  })
}

// 重置表单
function reset() {
  formRef.value?.resetFields()
  Object.assign(form, {
    id: undefined,
    username: '',
    email: '',
    phone: '',
    status: 1
  })
}

// 初始加载
getList()
</script>

<style scoped>
.user-management {
  padding: 20px;
}

.search-form {
  margin-bottom: 20px;
}

.mb8 {
  margin-bottom: 16px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
```

### 9.2 页面结构总结

```
┌─────────────────────────────────────────────────────────────┐
│  用户管理                                                     │
├─────────────────────────────────────────────────────────────┤
│  搜索区域 (el-form :inline="true")                          │
│  ┌─────────┐ ┌─────────┐ ┌─────┐ ┌─────┐                   │
│  │用户名输入│ │状态选择 │ │查询 │ │重置 │                   │
│  └─────────┘ └─────────┘ └─────┘ └─────┘                   │
├─────────────────────────────────────────────────────────────┤
│  操作按钮 (el-row)                                          │
│  [新增]                                                     │
├─────────────────────────────────────────────────────────────┤
│  表格 (el-table)                                           │
│  ┌─────┬────────┬────────┬──────┬──────┬─────────┐          │
│  │ ID  │用户名   │邮箱    │手机  │状态  │操作     │          │
│  ├─────┼────────┼────────┼──────┼──────┼─────────┤          │
│  │  1  │ admin  │ a@...  │ 138  │启用  │详情编辑删除│        │
│  └─────┴────────┴────────┴──────┴──────┴─────────┘          │
├─────────────────────────────────────────────────────────────┤
│  分页 (el-pagination)                                       │
│                         < 1 2 3 4 5 >  共 100 条           │
└─────────────────────────────────────────────────────────────┘

对话框：
┌─────────────────────────────────────────────────────────────┐
│  新增/编辑/详情对话框 (el-dialog)                           │
├─────────────────────────────────────────────────────────────┤
│  用户名: [输入框]                                           │
│  邮箱:   [输入框]                                           │
│  手机:   [输入框]                                           │
│  状态:   [选择器]                                           │
├─────────────────────────────────────────────────────────────┤
│                              [取消]  [确定]                 │
└─────────────────────────────────────────────────────────────┘
```

---

## 总结

### 常用组件速查表

| 组件 | 用途 | 关键属性 |
|------|------|----------|
| `el-form` | 表单容器 | `model`, `rules` |
| `el-form-item` | 表单项 | `label`, `prop` |
| `el-input` | 输入框 | `v-model`, `type` |
| `el-select` | 选择器 | `v-model`, `el-option` |
| `el-date-picker` | 日期选择 | `v-model`, `type` |
| `el-table` | 表格 | `data`, `stripe` |
| `el-table-column` | 表格列 | `prop`, `label` |
| `el-dialog` | 对话框 | `v-model`, `title` |
| `el-pagination` | 分页 | `total`, `v-model:current-page` |
| `el-button` | 按钮 | `type`, `icon` |
| `el-tag` | 标签 | `type` |
| `ElMessage` | 消息提示 | `success()`, `error()` |

---

> **文档版本**: 1.0
> **最后更新**: 2026-04-22
> **作者**: AI Assistant
