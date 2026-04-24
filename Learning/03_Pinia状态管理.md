# Pinia 状态管理入门指南

> 本文档讲解 Pinia 状态管理的核心概念和使用方法，帮助你理解如何管理全局状态。

---

## 目录

1. [什么是状态管理？](#1-什么是状态管理)
2. [为什么需要 Pinia？](#2-为什么需要-pinia)
3. [Pinia 核心概念](#3-pinia-核心概念)
4. [定义 Store](#4-定义-store)
5. [在组件中使用 Store](#5-在组件中使用-store)
6. [实战：用户状态管理](#6-实战用户状态管理)
7. [多个 Store](#7-多个-store)
8. [进阶用法](#8-进阶用法)

---

## 1. 什么是状态管理？

### 1.1 生活中的类比

想象你在玩一个游戏：

```
┌─────────────────────────────────────────────────────────────┐
│                        游戏状态                              │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   玩家信息 ─────┐                                           │
│   (金币、等级)   │                                           │
│                 ├───→ 共享状态 ──→ 背包界面                 │
│   背包信息 ─────┤              │                           │
│   (装备、道具)   │              ├───→ 商店界面               │
│                 │              │                           │
│   任务信息 ─────┘              ├───→ 战斗界面               │
│   (进度、奖励)                  │                           │
│                                └───→ 设置界面                 │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 1.2 代码中的状态管理

在网页应用中，状态管理解决的问题：

```
❌ 不用状态管理的问题：
   组件A ──→ 组件B ──→ 组件C ──→ 组件D
   props  props  props  props
   层层传递，很麻烦！

✅ 用状态管理：
   Store（仓库）
       │
       ├───→ 组件A
       ├───→ 组件B
       ├───→ 组件C
       └───→ 组件D
   所有组件直接从 Store 获取数据
```

### 1.3 什么情况下需要状态管理？

| 场景 | 需要的组件层级 | 推荐方案 |
|------|--------------|----------|
| 简单的父子组件通信 | 1-2层 | Props + Emit |
| 简单的跨级组件通信 | 2-3层 | Provide + Inject |
| 复杂的全局状态 | 多层/多个页面 | Pinia |
| 多个相关组件共享数据 | 多组件 | Pinia |

---

## 2. 为什么需要 Pinia？

### 2.1 Pinia 是什么？

Pinia 是 Vue 官方推荐的新一代状态管理库，是 Vuex 的替代者。

### 2.2 Pinia vs Vuex

| 特性 | Vuex | Pinia |
|------|------|------|
| API 复杂度 | 复杂（Mutation/Action/State） | 简单（State/Getter/Action） |
| TypeScript 支持 | 一般 | 更好 |
| 体积 | 较大 | 更小 |
| 学习曲线 | 陡峭 | 平缓 |
| 官方推荐 | 以前是 | 现在是 |

### 2.3 Pinia 的优势

1. **更简单的 API** - 不需要 Mutation，直接修改 state
2. **更小的体积** - 只有约 1KB
3. **更好的 TypeScript 支持** - 类型推导更智能
4. **模块化更灵活** - 不需要嵌套模块
5. **支持组合式风格** - 可以用函数式定义 Store

---

## 3. Pinia 核心概念

### 3.1 概念图解

```
┌─────────────────────────────────────────────────────────────┐
│                      Pinia 核心概念                          │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   ┌───────────────────────────────────────────────────┐     │
│   │                  Store（仓库）                      │     │
│   │                                                   │     │
│   │   state ────→ Getters ────→ Actions              │     │
│   │     │              │              │                │     │
│   │     ▼              ▼              ▼                │     │
│   │   数据(状态)      计算属性        方法              │     │
│   │                                                   │     │
│   └───────────────────────────────────────────────────┘     │
│                                                             │
│   ┌─────────┐   ┌─────────┐   ┌─────────┐                 │
│   │组件 A   │   │组件 B   │   │组件 C   │                 │
│   └────┬────┘   └────┬────┘   └────┬────┘                 │
│        │             │             │                        │
│        └─────────────┴─────────────┘                        │
│                      │                                      │
│                      ▼                                      │
│              ┌───────────────┐                              │
│              │  useXxxStore() │                            │
│              │    (调用 Store) │                            │
│              └───────────────┘                              │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 3.2 三个核心概念

| 概念 | 类比 | 说明 |
|------|------|------|
| **state** | 冰箱里的食物 | 存储应用的数据（响应式的） |
| **getters** | 计算器 | 基于 state 计算出的派生值 |
| **actions** | 厨师的操作 | 修改 state 的方法（可以是异步的） |

---

## 4. 定义 Store

### 4.1 项目结构

```
src/
├── store/
│   ├── index.js          # 统一导出
│   ├── modules/
│   │   ├── user.js       # 用户相关状态
│   │   └── app.js        # 应用相关状态
│   └── permission.js     # 权限相关状态
```

### 4.2 基本语法

**方式一：选项式（类似 Vuex）**

```javascript
// store/modules/user.js
import { defineStore } from 'pinia'
import { getToken, setToken, removeToken } from '@/utils/auth'
import { login as loginApi, getUserInfo } from '@/api/login'

export const useUserStore = defineStore('user', {
  // 1. State - 存储数据
  state: () => ({
    token: getToken() || '',
    id: '',
    username: '',
    roles: [],
    permissions: []
  }),
  
  // 2. Getters - 计算属性
  getters: {
    // 是否已登录
    isLoggedIn: (state) => !!state.token,
    
    // 是否是管理员
    isAdmin: (state) => state.roles.includes('ROLE_admin'),
    
    // 是否是销售经理
    isSalesManager: (state) => state.roles.includes('ROLE_sales_manager'),
    
    // 是否有指定权限
    hasPermission: (state) => (permission) => {
      return state.permissions.includes(permission)
    }
  },
  
  // 3. Actions - 方法（可以修改 state）
  actions: {
    // 登录
    async login(loginForm) {
      try {
        const res = await loginApi(loginForm)
        const token = res.data?.token || res.token
        setToken(token)
        this.token = token
        return res
      } catch (error) {
        throw error
      }
    },
    
    // 获取用户信息
    async getInfo() {
      try {
        const res = await getUserInfo()
        const data = res.data || res
        this.id = data.userId || data.id
        this.username = data.userName || data.username
        this.roles = data.roles || []
        this.permissions = data.permissions || []
        return res
      } catch (error) {
        throw error
      }
    },
    
    // 退出登录
    async logout() {
      this.token = ''
      this.roles = []
      this.permissions = []
      removeToken()
    }
  }
})
```

**方式二：组合式（推荐，更现代）**

```javascript
// store/modules/user.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getToken, setToken, removeToken } from '@/utils/auth'
import { login as loginApi, getUserInfo } from '@/api/login'

export const useUserStore = defineStore('user', () => {
  // State
  const token = ref(getToken() || '')
  const id = ref('')
  const username = ref('')
  const roles = ref([])
  const permissions = ref([])
  
  // Getters（计算属性）
  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => roles.value.includes('ROLE_admin'))
  const isSalesManager = computed(() => roles.value.includes('ROLE_sales_manager'))
  
  // Actions（方法）
  async function login(loginForm) {
    try {
      const res = await loginApi(loginForm)
      const tokenValue = res.data?.token || res.token
      setToken(tokenValue)
      token.value = tokenValue
      return res
    } catch (error) {
      throw error
    }
  }
  
  async function getInfo() {
    try {
      const res = await getUserInfo()
      const data = res.data || res
      id.value = data.userId || data.id
      username.value = data.userName || data.username
      roles.value = data.roles || []
      permissions.value = data.permissions || []
      return res
    } catch (error) {
      throw error
    }
  }
  
  async function logout() {
    token.value = ''
    roles.value = []
    permissions.value = []
    removeToken()
  }
  
  // 返回所有内容
  return {
    // State
    token,
    id,
    username,
    roles,
    permissions,
    // Getters
    isLoggedIn,
    isAdmin,
    isSalesManager,
    // Actions
    login,
    getInfo,
    logout
  }
})
```

### 4.3 创建 Store 实例

```javascript
// main.js
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'

const app = createApp(App)

// 创建 Pinia 实例
const pinia = createPinia()

// 安装到应用
app.use(pinia)

app.mount('#app')
```

---

## 5. 在组件中使用 Store

### 5.1 基础用法

```vue
<!-- src/views/UserProfile.vue -->
<template>
  <div class="user-profile">
    <h2>用户信息</h2>
    
    <!-- 显示用户信息 -->
    <p>用户名：{{ userStore.username }}</p>
    <p>ID：{{ userStore.id }}</p>
    <p>角色：{{ userStore.roles.join(', ') }}</p>
    
    <!-- 使用 getters -->
    <p>登录状态：{{ userStore.isLoggedIn ? '已登录' : '未登录' }}</p>
    <p>是否管理员：{{ userStore.isAdmin ? '是' : '否' }}</p>
    
    <!-- 操作按钮 -->
    <button @click="handleLogin">登录</button>
    <button @click="handleLogout">退出</button>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useUserStore } from '@/store/modules/user'

// 1. 获取 Store 实例
const userStore = useUserStore()

// 2. 访问 state
console.log(userStore.username)  // '张三'

// 3. 访问 getters
console.log(userStore.isAdmin)  // false

// 4. 调用 actions
async function handleLogin() {
  try {
    await userStore.login({ username: 'admin', password: '123456' })
    console.log('登录成功')
  } catch (error) {
    console.error('登录失败:', error)
  }
}

function handleLogout() {
  userStore.logout()
  console.log('已退出')
}

// 5. 页面加载时获取用户信息
onMounted(async () => {
  if (userStore.token) {
    await userStore.getInfo()
  }
})
</script>
```

### 5.2 在模板中使用

```vue
<template>
  <div>
    <!-- 方式1：直接使用 -->
    <p>{{ userStore.username }}</p>
    
    <!-- 方式2：解构（保持响应式） -->
    <p>{{ username }}</p>
    
    <!-- 方式3：在计算属性中使用 -->
    <div v-if="isAdmin">
      <p>管理员面板</p>
    </div>
  </div>
</template>

<script setup>
import { storeToRefs } from 'pinia'
import { useUserStore } from '@/store/modules/user'

const userStore = useUserStore()

// 使用 storeToRefs 保持响应式
const { username, roles, isAdmin } = storeToRefs(userStore)

// actions 不需要 storeToRefs
const { login, logout } = userStore
</script>
```

### 5.3 修改 State

```vue
<script setup>
import { useUserStore } from '@/store/modules/user'

const userStore = useUserStore()

// ❌ 不推荐：直接修改 state（虽然 Pinia 允许）
// userStore.username = '李四'

// ✅ 推荐：通过 actions 修改
function updateUsername() {
  // 在 Store 中定义 action
  userStore.setUsername('李四')
}
</script>
```

---

## 6. 实战：用户状态管理

### 6.1 Store 定义

**文件：`src/store/modules/user.js`**

```javascript
import { defineStore } from 'pinia'
import { getToken, setToken, removeToken } from '@/utils/auth'
import { login as loginApi, logout as logoutApi, getInfo as getInfoApi } from '@/api/login'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken() || '',
    id: '',
    username: '',
    nickName: '',
    avatar: '',
    roles: [],
    permissions: []
  }),
  
  getters: {
    // 是否已登录
    isLoggedIn: (state) => !!state.token,
    
    // 是否是管理员
    isAdmin: (state) => state.roles.includes('ROLE_admin'),
    
    // 是否有某个权限
    hasPermission: (state) => (permission) => {
      return state.permissions.includes(permission)
    },
    
    // 是否有某些权限之一
    hasAnyPermission: (state) => (permissions) => {
      return permissions.some(p => state.permissions.includes(p))
    }
  },
  
  actions: {
    // 登录
    async login(loginForm) {
      const username = loginForm.username.trim()
      const password = loginForm.password
      
      return new Promise((resolve, reject) => {
        loginApi(username, password).then(res => {
          const token = res.data?.token || res.token
          if (token) {
            setToken(token)
            this.token = token
            resolve()
          } else {
            reject(new Error('登录失败：未获取到 token'))
          }
        }).catch(error => {
          reject(error)
        })
      })
    },
    
    // 获取用户信息
    async getInfo() {
      return new Promise((resolve, reject) => {
        getInfoApi().then(res => {
          const data = res.data || res
          
          // 处理用户信息
          this.id = data.userId || data.id || ''
          this.username = data.userName || data.username || ''
          this.nickName = data.nickName || data.realName || this.username
          this.avatar = data.avatar || ''
          
          // 处理角色和权限
          if (data.roles && data.roles.length > 0) {
            this.roles = data.roles
            this.permissions = data.permissions || []
          } else {
            this.roles = ['ROLE_DEFAULT']
            this.permissions = ['*:*:*']
          }
          
          resolve(res)
        }).catch(error => {
          reject(error)
        })
      })
    },
    
    // 退出登录
    async logOut() {
      return new Promise((resolve) => {
        // 清除本地状态
        this.token = ''
        this.roles = []
        this.permissions = []
        removeToken()
        resolve()
      })
    }
  }
})
```

### 6.2 在登录页面使用

**文件：`src/views/login/index.vue`**

```vue
<template>
  <div class="login-container">
    <el-form :model="loginForm" @submit.prevent="handleLogin">
      <el-form-item>
        <el-input 
          v-model="loginForm.username" 
          placeholder="用户名"
        />
      </el-form-item>
      <el-form-item>
        <el-input 
          v-model="loginForm.password" 
          type="password" 
          placeholder="密码"
        />
      </el-form-item>
      <el-form-item>
        <el-button 
          type="primary" 
          :loading="loading"
          native-type="submit"
        >
          登录
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/modules/user'

const router = useRouter()
const userStore = useUserStore()

const loginForm = reactive({
  username: '',
  password: ''
})

const loading = ref(false)

async function handleLogin() {
  if (!loginForm.username || !loginForm.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  
  loading.value = true
  
  try {
    // 1. 执行登录
    await userStore.login(loginForm)
    
    // 2. 获取用户信息
    await userStore.getInfo()
    
    // 3. 跳转首页
    ElMessage.success('登录成功')
    router.push('/')
  } catch (error) {
    ElMessage.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>
```

### 6.3 在需要权限的页面使用

```vue
<template>
  <div>
    <!-- 所有登录用户可见 -->
    <h1>欢迎，{{ userStore.username }}</h1>
    
    <!-- 仅管理员可见 -->
    <div v-if="userStore.isAdmin">
      <el-button type="danger">删除用户</el-button>
    </div>
    
    <!-- 有权限的用户可见 -->
    <div v-if="userStore.hasPermission('user:add')">
      <el-button type="primary">新增用户</el-button>
    </div>
  </div>
</template>

<script setup>
import { useUserStore } from '@/store/modules/user'

const userStore = useUserStore()
</script>
```

---

## 7. 多个 Store

### 7.1 创建多个 Store

**用户 Store - `src/store/modules/user.js`**

```javascript
import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    username: '张三',
    role: 'admin'
  }),
  actions: {
    setUsername(name) {
      this.username = name
    }
  }
})
```

**应用 Store - `src/store/modules/app.js`**

```javascript
import { defineStore } from 'pinia'

export const useAppStore = defineStore('app', {
  state: () => ({
    sidebarOpened: true,
    device: 'desktop',
    language: 'zh-CN',
    size: 'default'
  }),
  actions: {
    toggleSidebar() {
      this.sidebarOpened = !this.sidebarOpened
    },
    setDevice(device) {
      this.device = device
    }
  }
})
```

**权限 Store - `src/store/modules/permission.js`**

```javascript
import { defineStore } from 'pinia'

export const usePermissionStore = defineStore('permission', {
  state: () => ({
    routes: [],
    addRoutes: [],
    sidebarRouters: []
  }),
  actions: {
    setRoutes(routes) {
      this.addRoutes = routes
      this.routes = [...routes]
    },
    setSidebarRouters(routes) {
      this.sidebarRouters = routes
    }
  }
})
```

### 7.2 在组件中使用多个 Store

```vue
<template>
  <div>
    <!-- 用户信息 -->
    <p>用户：{{ userStore.username }}</p>
    
    <!-- 应用状态 -->
    <p>设备：{{ appStore.device }}</p>
    
    <!-- 权限路由 -->
    <p>路由数：{{ permissionStore.routes.length }}</p>
  </div>
</template>

<script setup>
import { useUserStore } from '@/store/modules/user'
import { useAppStore } from '@/store/modules/app'
import { usePermissionStore } from '@/store/modules/permission'

// 导入多个 Store
const userStore = useUserStore()
const appStore = useAppStore()
const permissionStore = usePermissionStore()
</script>
```

### 7.3 Store 之间相互调用

```javascript
// store/modules/permission.js
import { defineStore } from 'pinia'
import { useUserStore } from './user'

export const usePermissionStore = defineStore('permission', {
  actions: {
    async generateRoutes() {
      const userStore = useUserStore()
      
      // 根据用户角色生成路由
      const routes = []
      if (userStore.isAdmin) {
        routes.push({ path: '/admin', name: 'Admin' })
      } else {
        routes.push({ path: '/user', name: 'User' })
      }
      
      this.setRoutes(routes)
      return routes
    }
  }
})
```

---

## 8. 进阶用法

### 8.1 订阅 Store 变化

```javascript
// main.js
import { createApp } from 'vue'
import { createPinia } from 'pinia'

const pinia = createPinia()

// 订阅所有 Store 的变化
pinia.use(({ store }) => {
  store.$subscribe((mutation, state) => {
    console.log('Store 变化了:', mutation)
    console.log('新状态:', state)
  })
})

const app = createApp(App)
app.use(pinia)
app.mount('#app')
```

### 8.2 订阅特定 Store

```javascript
const userStore = useUserStore()

// 订阅 token 变化
userStore.$subscribe((mutation, state) => {
  console.log('token 变化了:', state.token)
}, { detached: true })  // detached: true 表示组件卸载后依然监听
```

### 8.3 持久化 Store

安装插件：
```bash
npm install pinia-plugin-persistedstate
```

使用：
```javascript
// main.js
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'

const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)

app.use(pinia)
```

定义 Store：
```javascript
export const useUserStore = defineStore('user', {
  state: () => ({
    token: '',
    username: ''
  }),
  persist: true  // 开启持久化，刷新页面后数据不丢失
})
```

### 8.4 完整的项目 Store 结构

```
src/
├── store/
│   ├── index.js           # 导出所有 Store
│   └── modules/
│       ├── user.js        # 用户状态（token、用户信息）
│       ├── app.js         # 应用状态（侧边栏、设备）
│       ├── permission.js  # 权限状态（路由、菜单）
│       └── tagsView.js    # 标签页状态
```

**`src/store/index.js`**
```javascript
import { createPinia } from 'pinia'

const pinia = createPinia()

export default pinia

// 导出所有 Store
export { useUserStore } from './modules/user'
export { useAppStore } from './modules/app'
export { usePermissionStore } from './modules/permission'
export { useTagsViewStore } from './modules/tagsView'
```

**在组件中使用**
```vue
<script setup>
import { useUserStore, useAppStore } from '@/store'

const userStore = useUserStore()
const appStore = useAppStore()
</script>
```

---

## 总结

### Pinia 使用流程

```
1. 安装 Pinia
   npm install pinia

2. 在 main.js 中创建和安装
   const pinia = createPinia()
   app.use(pinia)

3. 定义 Store
   export const useXxxStore = defineStore('xxx', {
     state: () => ({}),
     getters: {},
     actions: {}
   })

4. 在组件中使用
   const store = useXxxStore()
   store.xxx

5. 修改状态
   store.$patch({})
   store.xxxAction()
```

### 对比记忆

| Pinia | Vuex | 类比 |
|-------|------|------|
| `state` | `state` | 冰箱里的食物 |
| `getters` | `getters` | 计算器 |
| `actions` | `actions` + `mutations` | 操作方法 |
| `$patch()` | `commit()` | 修改状态 |
| `storeToRefs()` | `mapState()` | 获取响应式数据 |

---

> **文档版本**: 1.0
> **最后更新**: 2026-04-22
> **作者**: AI Assistant
