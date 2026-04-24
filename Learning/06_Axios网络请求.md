# Axios 网络请求入门指南

> 本文档讲解 Axios 的核心概念和封装方法，帮助你理解如何与后端 API 进行交互。

---

## 目录

1. [什么是 Axios？](#1-什么是-axios)
2. [Axios 基础用法](#2-axios-基础用法)
3. [请求配置详解](#3-请求配置详解)
4. [封装 Axios](#4-封装-axios)
5. [请求拦截器](#5-请求拦截器)
6. [响应拦截器](#6-响应拦截器)
7. [API 接口封装](#7-api-接口封装)
8. [项目实战](#8-项目实战)

---

## 1. 什么是 Axios？

### 1.1 官方定义

Axios 是一个基于 Promise 的 HTTP 客户端，用于浏览器和 Node.js，可以发送 HTTP 请求获取数据。

### 1.2 通俗理解

想象你在网上购物：

```
┌─────────────────────────────────────────────────────────────┐
│                      HTTP 请求就像网购                      │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   你（下单）           卖家（发货）                        │
│      │                    │                               │
│      │  ──→ 订单信息 ──→ │                               │
│      │                    │                               │
│      │  ←── 商品包裹 ←── │                               │
│      │                    │                               │
│                                                             │
│   前端（发起请求）     后端（返回数据）                    │
│      │                    │                               │
│      │  ──→ HTTP请求 ──→ │                               │
│      │                    │                               │
│      │  ←── JSON数据 ←── │                               │
│      │                    │                               │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 1.3 为什么使用 Axios？

| 特性 | 说明 |
|------|------|
| **基于 Promise** | 支持 async/await |
| **自动转换数据** | 自动转换 JSON 数据 |
| **拦截请求/响应** | 可以统一处理请求前后逻辑 |
| **取消请求** | 可以取消正在进行的请求 |
| **防止 XSRF** | 内置防止跨站请求伪造 |
| **支持浏览器/Node** | 浏览器和 Node.js 都能用 |

---

## 2. Axios 基础用法

### 2.1 安装

```bash
npm install axios
```

### 2.2 基础语法

```javascript
import axios from 'axios'

// GET 请求
axios.get('/api/user')
  .then(response => {
    console.log(response.data)
  })
  .catch(error => {
    console.error(error)
  })

// POST 请求
axios.post('/api/user', {
    name: '张三',
    age: 25
  })
  .then(response => {
    console.log(response.data)
  })
  .catch(error => {
    console.error(error)
  })
```

### 2.3 async/await 用法

```javascript
import axios from 'axios'

// 使用 async/await（更优雅）
async function getUser() {
  try {
    const response = await axios.get('/api/user/1')
    console.log(response.data)
  } catch (error) {
    console.error(error)
  }
}

// 创建实例
async function createUser() {
  try {
    const response = await axios.post('/api/user', {
      name: '张三',
      age: 25
    })
    console.log(response.data)
  } catch (error) {
    console.error(error)
  }
}
```

### 2.4 常用请求方法

```javascript
// GET 请求 - 获取数据
axios.get('/api/users', {
  params: { page: 1, size: 10 }  // 查询参数
})

// POST 请求 - 新增数据
axios.post('/api/users', {
  name: '张三',
  age: 25
})

// PUT 请求 - 更新数据（完整更新）
axios.put('/api/users/1', {
  name: '李四',
  age: 30
})

// PATCH 请求 - 部分更新
axios.patch('/api/users/1', {
  name: '王五'  // 只更新 name
})

// DELETE 请求 - 删除数据
axios.delete('/api/users/1')
```

### 2.5 响应结构

```javascript
const response = await axios.get('/api/user')

// response 包含以下属性：
console.log(response.data)      // 后端返回的数据
console.log(response.status)     // HTTP 状态码（200）
console.log(response.statusText) // 状态文本（'OK'）
console.log(response.headers)    // 响应头
console.log(response.config)     // 请求配置
console.log(response.request)    // 请求对象
```

---

## 3. 请求配置详解

### 3.1 全局默认配置

```javascript
import axios from 'axios'

// 设置全局默认配置
axios.defaults.baseURL = 'http://localhost:8080'  // 基础 URL
axios.defaults.timeout = 10000  // 超时时间（毫秒）
axios.defaults.headers.common['Authorization'] = 'Bearer xxx'  // 公共请求头
```

### 3.2 实例配置

```javascript
import axios from 'axios'

// 创建 axios 实例
const instance = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 使用实例发送请求
instance.get('/api/user')
instance.post('/api/user', { name: '张三' })
```

### 3.3 请求级别配置

```javascript
// 在单个请求中覆盖默认配置
axios.get('/api/user', {
  params: { id: 1 },      // 查询参数
  timeout: 5000,           // 超时时间
  headers: {                // 请求头
    'X-Custom-Header': 'custom'
  }
})
```

### 3.4 完整配置示例

```javascript
axios({
  method: 'post',                    // 请求方法
  url: '/api/user',                  // 请求地址
  baseURL: 'http://localhost:8080', // 基础 URL
  headers: {                         // 请求头
    'Authorization': 'Bearer xxx',
    'Content-Type': 'application/json'
  },
  params: {                         // URL 查询参数
    id: 1,
    name: '张三'
  },
  data: {                           // 请求体数据
    name: '张三',
    age: 25
  },
  timeout: 10000,                    // 超时时间
  responseType: 'json',             // 响应类型
  withCredentials: false,            // 是否携带 cookie
  validateStatus: function (status) { // 自定义状态码判断
    return status >= 200 && status < 300
  }
})
```

---

## 4. 封装 Axios

### 4.1 为什么要封装？

```
┌─────────────────────────────────────────────────────────────┐
│                     封装 Axios 的好处                        │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   ❌ 不封装的问题:                                         │
│   - 每个页面都要写重复的 token、baseURL 配置               │
│   - 错误处理逻辑分散，难以统一                            │
│   - 请求分散，难以管理和维护                              │
│                                                             │
│   ✅ 封装后的好处:                                         │
│   - 统一配置（baseURL、timeout、headers）                 │
│   - 统一错误处理（登录过期、网络错误等）                   │
│   - 统一 token 管理（自动添加、自动刷新）                 │
│   - 方便接口管理（模块化、可复用）                        │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 4.2 基础封装

**`src/utils/request.js`：**

```javascript
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken } from '@/utils/auth'

// 创建 axios 实例
const service = axios.create({
  baseURL: '/api',           // 基础 URL（根据环境配置）
  timeout: 10000             // 请求超时时间
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 在请求发送之前做一些处理
    // 1. 添加 Token
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    
    // 2. 其他处理
    console.log('请求配置:', config)
    
    return config
  },
  error => {
    // 请求错误处理
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    // 统一处理响应
    const res = response.data
    
    // 根据后端返回的 code 判断是否成功
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    
    return res
  },
  error => {
    // 错误处理
    console.error('响应错误:', error)
    
    let message = '网络错误'
    if (error.response) {
      // 服务器返回错误状态码
      switch (error.response.status) {
        case 401:
          message = '登录已过期，请重新登录'
          // 可以在这里跳转到登录页
          break
        case 403:
          message = '没有权限'
          break
        case 404:
          message = '请求地址不存在'
          break
        case 500:
          message = '服务器错误'
          break
        default:
          message = error.response.data?.message || '请求失败'
      }
    } else if (error.message.includes('timeout')) {
      message = '请求超时'
    }
    
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default service
```

### 4.3 微服务路由封装

```javascript
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken } from '@/utils/auth'

// 创建 axios 实例
const service = axios.create({
  timeout: 10000
})

// 微服务路径配置
function getBaseURL(url) {
  if (!url || url.startsWith('http')) return ''
  
  // 认证服务
  if (url.startsWith('/auth/')) return ''
  
  // 系统服务
  if (url.startsWith('/sysUser') || url.startsWith('/sysRole')) {
    return '/auth/api'
  }
  
  // 字典/参数服务
  if (url.startsWith('/sysDict') || url.startsWith('/sysParam')) {
    return '/system/api'
  }
  
  // 金融服务
  if (url.startsWith('/bank') || url.startsWith('/loanAudit')) {
    return '/finance/api'
  }
  
  // 销售服务
  if (url.startsWith('/customer') || url.startsWith('/contract')) {
    return '/sales/api'
  }
  
  return '/dev-api'
}

// 请求拦截器
service.interceptors.request.use(config => {
  // 动态设置 baseURL
  config.baseURL = getBaseURL(config.url)
  
  // 添加 Token
  const token = getToken()
  if (token) {
    config.headers['Authorization'] = 'Bearer ' + token
  }
  
  return config
}, error => {
  return Promise.reject(error)
})

// 响应拦截器
service.interceptors.response.use(response => {
  const res = response.data
  
  if (res.code !== 200 && res.code !== undefined) {
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message))
  }
  
  return res
}, error => {
  let message = error.message
  if (message === 'Network Error') {
    message = '网络连接失败'
  } else if (message.includes('timeout')) {
    message = '请求超时'
  }
  ElMessage.error(message)
  return Promise.reject(error)
})

export default service
```

---

## 5. 请求拦截器

### 5.1 请求拦截器的作用

```
┌─────────────────────────────────────────────────────────────┐
│                    请求拦截器工作流程                        │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   发起请求                                                  │
│       │                                                     │
│       ▼                                                     │
│   ┌─────────────────┐                                      │
│   │  请求拦截器      │                                      │
│   │  1. 添加 Token  │                                      │
│   │  2. 添加时间戳  │                                      │
│   │  3. 参数处理    │                                      │
│   └────────┬────────┘                                      │
│            │                                                │
│            ▼                                                │
│       发送请求                                              │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 5.2 完整请求拦截器示例

```javascript
import axios from 'axios'
import { getToken } from '@/utils/auth'

const service = axios.create({
  timeout: 10000
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 1. 添加 Token
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    
    // 2. 添加时间戳（防止缓存）
    if (config.method === 'get') {
      config.params = {
        ...config.params,
        _t: Date.now()
      }
    }
    
    // 3. GET 请求参数序列化
    if (config.method === 'get' && config.params) {
      let url = config.url + '?'
      for (const key in config.params) {
        const value = config.params[key]
        if (value !== undefined && value !== null) {
          url += `${key}=${encodeURIComponent(value)}&`
        }
      }
      config.url = url.slice(0, -1)
      config.params = {}
    }
    
    // 4. 防止重复提交（可选）
    if ((config.method === 'post' || config.method === 'put')) {
      const requestObj = {
        url: config.url,
        data: typeof config.data === 'object' ? JSON.stringify(config.data) : config.data,
        time: new Date().getTime()
      }
      // 可以在这里做防重复提交的检查
      console.log('请求数据:', requestObj)
    }
    
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

export default service
```

---

## 6. 响应拦截器

### 6.1 响应拦截器的作用

```
┌─────────────────────────────────────────────────────────────┐
│                    响应拦截器工作流程                        │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   收到响应                                                  │
│       │                                                     │
│       ▼                                                     │
│   ┌─────────────────┐                                      │
│   │  响应拦截器      │                                      │
│   │  1. 提取数据    │                                      │
│   │  2. 状态码判断  │                                      │
│   │  3. 错误提示    │                                      │
│   └────────┬────────┘                                      │
│            │                                                │
│      ┌─────┴─────┐                                          │
│      │           │                                          │
│   成功                      失败                            │
│      │                       │                              │
│      ▼                       ▼                              │
│   返回数据              统一错误处理                        │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 6.2 完整响应拦截器示例

```javascript
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/modules/user'

let isRelogin = { show: false }

const service = axios.create({
  timeout: 10000
})

// 响应拦截器
service.interceptors.response.use(
  response => {
    // 统一处理响应数据
    const res = response.data
    
    // 如果后端返回的不是 200，说明有错误
    if (res.code && res.code !== 200) {
      // 业务错误提示
      ElMessage({
        message: res.message || res.msg || '请求失败',
        type: 'error',
        duration: 3000
      })
      
      // 特殊错误码处理
      if (res.code === 401) {
        // Token 过期
        if (!isRelogin.show) {
          isRelogin.show = true
          ElMessageBox.confirm(
            '登录状态已过期，请重新登录',
            '系统提示',
            {
              confirmButtonText: '重新登录',
              cancelButtonText: '取消',
              type: 'warning'
            }
          ).then(() => {
            isRelogin.show = false
            useUserStore().logout().then(() => {
              location.href = '/login'
            })
          }).catch(() => {
            isRelogin.show = false
          })
        }
      } else if (res.code === 500) {
        // 服务器错误
        ElMessage.error(res.message || '服务器错误')
      }
      
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    
    // 成功，直接返回数据
    return response.data
  },
  error => {
    // HTTP 错误处理
    console.error('响应错误:', error)
    
    let message = '网络错误'
    
    if (error.response) {
      // 有响应，但状态码不是 2xx
      switch (error.response.status) {
        case 400:
          message = '请求参数错误'
          break
        case 401:
          message = '未授权，请登录'
          // 可以在这里处理 Token 刷新
          break
        case 403:
          message = '拒绝访问'
          break
        case 404:
          message = '请求地址不存在'
          break
        case 408:
          message = '请求超时'
          break
        case 500:
          message = '服务器内部错误'
          break
        case 502:
          message = '网关错误'
          break
        case 503:
          message = '服务不可用'
          break
        case 504:
          message = '网关超时'
          break
        default:
          message = error.response.data?.message || '请求失败'
      }
    } else if (error.message.includes('timeout')) {
      message = '请求超时，请重试'
    } else if (error.message === 'Network Error') {
      message = '网络连接失败，请检查网络'
    }
    
    ElMessage({
      message,
      type: 'error',
      duration: 5000
    })
    
    return Promise.reject(error)
  }
)

export default service
```

---

## 7. API 接口封装

### 7.1 项目结构

```
src/
├── api/
│   ├── index.js          # 导出所有接口
│   ├── user.js           # 用户相关接口
│   ├── customer.js       # 客户相关接口
│   └── contract.js       # 合同相关接口
├── utils/
│   └── request.js        # axios 封装
```

### 7.2 定义接口

**`src/api/user.js`：**

```javascript
import request from '@/utils/request'

// 查询用户列表
export function listUser(query) {
  return request({
    url: '/user/page',
    method: 'get',
    params: query  // GET 请求使用 params 传递参数
  })
}

// 查询用户详情
export function getUser(id) {
  return request({
    url: '/user/' + id,
    method: 'get'
  })
}

// 新增用户
export function addUser(data) {
  return request({
    url: '/user',
    method: 'post',
    data  // POST/PUT/PATCH 请求使用 data 传递参数
  })
}

// 修改用户
export function updateUser(data) {
  return request({
    url: '/user',
    method: 'put',
    data
  })
}

// 删除用户
export function delUser(id) {
  return request({
    url: '/user/' + id,
    method: 'delete'
  })
}

// 重置密码
export function resetPassword(id, password) {
  return request({
    url: '/user/' + id + '/reset-password',
    method: 'put',
    data: { password }
  })
}
```

**`src/api/customer.js`：**

```javascript
import request from '@/utils/request'

// 查询客户列表（分页）
export function listCustomer(query) {
  return request({
    url: '/customer/page',
    method: 'get',
    params: query
  })
}

// 查询客户详情
export function getCustomer(id) {
  return request({
    url: '/customer/' + id,
    method: 'get'
  })
}

// 新增客户
export function addCustomer(data) {
  return request({
    url: '/customer',
    method: 'post',
    data
  })
}

// 修改客户
export function updateCustomer(data) {
  return request({
    url: '/customer',
    method: 'put',
    data
  })
}

// 删除客户
export function delCustomer(id) {
  return request({
    url: '/customer/' + id,
    method: 'delete'
  })
}

// 获取客户视图（聚合所有相关数据）
export function getCustomerView(id) {
  return request({
    url: '/customer/view/' + id,
    method: 'get'
  })
}
```

### 7.3 统一导出

**`src/api/index.js`：**

```javascript
// 用户接口
export * from './user'

// 客户接口
export * from './customer'

// 合同接口
export * from './contract'

// 导入默认的 request
export { default as request } from './request'
```

---

## 8. 项目实战

### 8.1 在组件中使用

**`src/views/user/List.vue`：**

```vue
<template>
  <div class="user-list">
    <!-- 搜索区域 -->
    <el-form :inline="true" :model="queryParams">
      <el-form-item label="用户名">
        <el-input v-model="queryParams.username" placeholder="请输入" clearable />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>
    
    <!-- 操作按钮 -->
    <el-button type="primary" @click="handleAdd">新增</el-button>
    
    <!-- 表格 -->
    <el-table :data="tableData" v-loading="loading">
      <el-table-column prop="id" label="ID" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 分页 -->
    <el-pagination
      v-model:current-page="queryParams.pageNum"
      v-model:page-size="queryParams.pageSize"
      :total="total"
      @pagination="getList"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listUser, addUser, updateUser, delUser } from '@/api/user'

// 数据
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  username: ''
})

// 加载列表
async function getList() {
  loading.value = true
  try {
    const res = await listUser(queryParams)
    tableData.value = res.data?.records || res.records || []
    total.value = res.data?.total || res.total || 0
  } catch (error) {
    console.error('加载列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 查询
function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

// 重置
function handleReset() {
  queryParams.username = ''
  queryParams.pageNum = 1
  getList()
}

// 新增
function handleAdd() {
  // 打开新增对话框
}

// 编辑
function handleEdit(row) {
  // 打开编辑对话框
}

// 删除
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除该用户吗？', '提示', {
      type: 'warning'
    })
    await delUser(row.id)
    ElMessage.success('删除成功')
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// 初始化
onMounted(() => {
  getList()
})
</script>
```

### 8.2 表单提交

```vue
<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { addUser, updateUser } from '@/api/user'

const loading = ref(false)
const formRef = ref()
const isEdit = ref(false)

const form = reactive({
  id: undefined,
  username: '',
  email: '',
  phone: '',
  status: 1
})

// 提交表单
async function submitForm() {
  try {
    await formRef.value.validate()
    loading.value = true
    
    if (isEdit.value) {
      // 编辑
      await updateUser(form)
      ElMessage.success('修改成功')
    } else {
      // 新增
      await addUser(form)
      ElMessage.success('新增成功')
    }
    
    // 关闭对话框
    dialogVisible.value = false
    // 刷新列表
    emit('refresh')
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    loading.value = false
  }
}
</script>
```

---

## 总结

### Axios 核心要点

| 概念 | 说明 |
|------|------|
| `axios.get()` | GET 请求 |
| `axios.post()` | POST 请求 |
| `axios.create()` | 创建实例 |
| `config.params` | URL 查询参数 |
| `config.data` | 请求体数据 |
| `interceptors.request` | 请求拦截器 |
| `interceptors.response` | 响应拦截器 |

### 封装的好处

```
┌─────────────────────────────────────────────────────────────┐
│                    封装 Axios 的核心价值                      │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   1. 统一 baseURL  - 不需要每个接口写完整地址               │
│   2. 统一 Token    - 自动添加认证 Token                     │
│   3. 统一错误处理 - 登录过期、网络错误等统一处理           │
│   4. 统一响应格式  - 只返回需要的数据部分                   │
│   5. 方便接口管理  - 模块化、可复用                         │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

> **文档版本**: 1.0
> **最后更新**: 2026-04-22
> **作者**: AI Assistant
