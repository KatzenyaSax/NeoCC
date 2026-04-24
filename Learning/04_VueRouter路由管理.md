# Vue Router 路由管理入门指南

> 本文档讲解 Vue Router 的核心概念和使用方法，帮助你理解页面导航和路由守卫。

---

## 目录

1. [什么是路由？](#1-什么是路由)
2. [路由基础](#2-路由基础)
3. [动态路由](#3-动态路由)
4. [嵌套路由](#4-嵌套路由)
5. [路由守卫](#5-路由守卫)
6. [路由传参](#6-路由传参)
7. [项目实战](#7-项目实战)

---

## 1. 什么是路由？

### 1.1 生活中的类比

路由器（Router）在生活中的作用：
- 你家的 WiFi 路由器，把网络信号分配到各个房间
- 快递分拣中心，根据地址把快递送到不同地方

### 1.2 网页中的路由

```
┌─────────────────────────────────────────────────────────────┐
│                      URL 地址                               │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   /user/list        ──→  用户列表页面                      │
│   /user/detail/1    ──→  ID为1的用户详情页面              │
│   /product/search   ──→  商品搜索页面                      │
│   /order/123        ──→  订单123的详情页面                │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 1.3 单页面应用（SPA）vs 多页面应用

**多页面应用：**
```
请求1 → 返回 index.html + index.js + index.css
请求2 → 返回 detail.html + detail.js + detail.css  （整个页面刷新）
请求3 → 返回 list.html + list.js + list.css       （整个页面刷新）
```

**单页面应用（SPA）：**
```
首次请求 → 返回 index.html + app.js + router.js
          之后的所有操作都在 JS 控制下完成，不刷新页面
          
          点击链接 → JS 切换组件（很快！）
```

### 1.4 Vue Router 的作用

```
┌─────────────────────────────────────────────────────────────┐
│                    Vue Router 的作用                          │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   URL 变化 ──→ Vue Router 监听 ──→ 加载对应组件            │
│                                                             │
│   /login    ──→  Login.vue                                 │
│   /home     ──→  Home.vue                                  │
│   /user/1   ──→  UserDetail.vue                           │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. 路由基础

### 2.1 安装

```bash
npm install vue-router
```

### 2.2 基本配置

**项目结构：**
```
src/
├── router/
│   └── index.js       # 路由配置
├── views/
│   ├── Home.vue
│   ├── About.vue
│   └── Login.vue
└── main.js
```

**创建路由文件 `src/router/index.js`：**

```javascript
import { createRouter, createWebHistory } from 'vue-router'

// 1. 定义路由组件（懒加载）
const Home = () => import('@/views/Home.vue')
const About = () => import('@/views/About.vue')
const Login = () => import('@/views/Login.vue')

// 2. 定义路由规则
const routes = [
  {
    path: '/',              // URL 路径
    name: 'Home',           // 路由名称（可选，用于编程式导航）
    component: Home         // 对应的组件
  },
  {
    path: '/about',
    name: 'About',
    component: About
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  }
]

// 3. 创建路由实例
const router = createRouter({
  // history 模式：使用 HTML5 History API，URL 看起来更美观
  // hash 模式：URL 会带 #，如 http://example.com/#/home
  history: createWebHistory(),
  
  routes  // 路由规则
})

// 4. 导出
export default router
```

**在 main.js 中安装：**

```javascript
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

const app = createApp(App)

// 使用 router 插件
app.use(router)

app.mount('#app')
```

### 2.3 组件中使用路由

**App.vue：**

```vue
<template>
  <div id="app">
    <!-- 路由出口：显示匹配到的组件 -->
    <router-view />
  </div>
</template>

<script setup>
// 根组件不需要引入 router
</script>
```

### 2.4 路由导航

**1. 声明式导航（使用 router-link）**

```vue
<template>
  <div>
    <!-- router-link 会渲染为 <a> 标签 -->
    <!-- to 指定目标路由 -->
    <router-link to="/">首页</router-link>
    <router-link to="/about">关于</router-link>
    <router-link to="/login">登录</router-link>
    
    <!-- 实际渲染为： -->
    <!-- <a href="/">首页</a> -->
    <!-- <a href="/about">关于</a> -->
    <!-- <a href="/login">登录</a> -->
  </div>
</template>
```

**2. 编程式导航（使用 router.push）**

```vue
<template>
  <div>
    <button @click="goHome">去首页</button>
    <button @click="goLogin">去登录</button>
    <button @click="goBack">返回</button>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

// 跳转到指定路径
function goHome() {
  router.push('/')
}

// 跳转到指定路径
function goLogin() {
  router.push('/login')
}

// 返回上一页
function goBack() {
  router.back()
}

// 前进一页
function goForward() {
  router.forward()
}

// 替换当前页（不会产生历史记录）
function replaceTo() {
  router.replace('/about')
}

// 前进/后退 n 步
function go(n) {
  router.go(n)  // router.go(-1) = router.back()
}
</script>
```

**router.push vs router.replace：**

| 方法 | 作用 | 历史记录 |
|------|------|----------|
| `router.push()` | 导航到新页面 | ✅ 有记录 |
| `router.replace()` | 替换当前页面 | ❌ 无记录 |

---

## 3. 动态路由

### 3.1 什么是动态路由？

动态路由允许 URL 中包含参数，实现通用页面组件的复用。

```
/user/1     ──→  显示用户 ID 为 1 的详情
/user/2     ──→  显示用户 ID 为 2 的详情
/user/100   ──→  显示用户 ID 为 100 的详情

上述三个 URL 可以共用一个 UserDetail 组件
```

### 3.2 定义动态路由

```javascript
// src/router/index.js
const routes = [
  // 方式1：使用 :id 动态参数
  {
    path: '/user/:id',           // :id 是动态参数
    name: 'UserDetail',
    component: () => import('@/views/UserDetail.vue')
  },
  
  // 方式2：多个动态参数
  {
    path: '/order/:userId/:orderId',
    name: 'OrderDetail',
    component: () => import('@/views/OrderDetail.vue')
  },
  
  // 方式3：可选参数
  {
    path: '/product/:id?',
    name: 'ProductDetail',
    component: () => import('@/views/ProductDetail.vue')
  }
]
```

### 3.3 获取路由参数

**在组件中获取：**

```vue
<template>
  <div>
    <h2>用户详情</h2>
    
    <!-- 方式1：模板中使用 $route -->
    <p>用户ID：{{ $route.params.id }}</p>
    
    <!-- 方式2：使用 useRoute 组合式 API（推荐） -->
    <p>用户ID：{{ route.params.id }}</p>
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router'

// 获取当前路由信息
const route = useRoute()

// 打印所有参数
console.log(route.params)      // { id: '1' }
console.log(route.params.id)  // '1'
console.log(route.path)       // '/user/1'
console.log(route.query)      // 查询参数 { name: '张三' }
</script>
```

### 3.4 带查询参数的导航

```vue
<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

// 导航到 /user/1?name=张三&age=25
router.push({
  path: '/user/1',
  query: {
    name: '张三',
    age: 25
  }
})
</script>
```

在目标页面获取：
```javascript
// URL: /user/1?name=张三&age=25
console.log(route.query)      // { name: '张三', age: '25' }
console.log(route.query.name) // '张三'
```

---

## 4. 嵌套路由

### 4.1 什么是嵌套路由？

嵌套路由用于实现多级菜单结构：

```
┌─────────────────────────────────────────────────────────────┐
│                    嵌套路由示例                              │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   /dashboard                                               │
│       ├── /dashboard/analysis    ──→ 分析面板              │
│       └── /dashboard/workplace   ──→ 工作台                │
│                                                             │
│   /user                                                    │
│       ├── /user/list             ──→ 用户列表              │
│       ├── /user/create           ──→ 创建用户              │
│       └── /user/:id              ──→ 用户详情              │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 4.2 配置嵌套路由

```javascript
// src/router/index.js
const routes = [
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/Dashboard.vue'),
    children: [
      {
        // 访问 /dashboard/analysis
        path: 'analysis',
        name: 'Analysis',
        component: () => import('@/views/dashboard/Analysis.vue')
      },
      {
        // 访问 /dashboard/workplace
        path: 'workplace',
        name: 'Workplace',
        component: () => import('@/views/dashboard/Workplace.vue')
      }
    ]
  },
  {
    path: '/user',
    component: () => import('@/views/User.vue'),
    children: [
      // 列表页
      {
        path: '',
        name: 'UserList',
        component: () => import('@/views/user/List.vue')
      },
      // 创建页
      {
        path: 'create',
        name: 'UserCreate',
        component: () => import('@/views/user/Create.vue')
      },
      // 详情页（带动态参数）
      {
        path: ':id',
        name: 'UserDetail',
        component: () => import('@/views/user/Detail.vue')
      }
    ]
  }
]
```

### 4.3 Dashboard 组件

**`src/views/Dashboard.vue`**

```vue
<template>
  <div class="dashboard">
    <h1>仪表盘</h1>
    
    <!-- 菜单 -->
    <div class="menu">
      <router-link to="/dashboard/analysis">分析</router-link>
      <router-link to="/dashboard/workplace">工作台</router-link>
    </div>
    
    <!-- 子路由出口 -->
    <div class="content">
      <router-view />
    </div>
  </div>
</template>
```

### 4.4 User 布局组件

**`src/views/User.vue`**

```vue
<template>
  <div class="user-container">
    <div class="sidebar">
      <router-link to="/user">用户列表</router-link>
      <router-link to="/user/create">创建用户</router-link>
    </div>
    
    <div class="main">
      <!-- 子路由出口 -->
      <router-view />
    </div>
  </div>
</template>
```

---

## 5. 路由守卫

### 5.1 什么是路由守卫？

路由守卫用于控制页面的访问权限，类似于门口的保安。

```
┌─────────────────────────────────────────────────────────────┐
│                      路由守卫流程                           │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   用户访问页面 ──→ 路由守卫检查 ──→ 有权限？               │
│                              │                              │
│                              ├──→ 是 ──→ 放行 ──→ 显示页面 │
│                              │                              │
│                              └──→ 否 ──→ 重定向 ──→ 登录页 │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 5.2 守卫类型

| 守卫 | 触发时机 | 用途 |
|------|----------|------|
| `beforeEach` | 每次路由切换前 | 权限检查、登录验证 |
| `beforeResolve` | 路由解析后，组件加载前 | 最后的检查 |
| `afterEach` | 路由切换后 | 页面埋点、滚动位置 |

### 5.3 全局守卫示例

```javascript
// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { requiresAuth: false }  // 不需要登录
  },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/home/index.vue'),
        meta: { requiresAuth: true }  // 需要登录
      },
      {
        path: 'user/:id',
        name: 'UserDetail',
        component: () => import('@/views/user/Detail.vue'),
        meta: { requiresAuth: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局前置守卫
router.beforeEach((to, from, next) => {
  // to: 目标路由
  // from: 来源路由
  // next: 放行函数
  
  // 获取 Token
  const hasToken = getToken()
  
  // 判断是否需要登录
  if (to.meta.requiresAuth) {
    if (hasToken) {
      // 已登录，放行
      next()
    } else {
      // 未登录，跳转到登录页
      next('/login')
    }
  } else {
    // 不需要登录的页面，直接放行
    next()
  }
})

// 全局后置守卫
router.afterEach((to, from) => {
  // 设置页面标题
  document.title = to.meta.title || 'NeoCC'
  
  // 页面埋点
  console.log(`访问页面: ${to.path}`)
})

export default router
```

### 5.4 完整示例：权限控制

```javascript
// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/auth'

const routes = [
  // 公开路由
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  
  // 404
  {
    path: '/404',
    name: '404',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404', requiresAuth: false }
  },
  
  // 捕获所有路由（放在最后）
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  },
  
  // 受保护的路由
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/home/index.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('@/layout/components/ParentView.vue'),
        meta: { title: '用户管理' },
        children: [
          {
            path: 'list',
            name: 'UserList',
            component: () => import('@/views/user/list.vue'),
            meta: { title: '用户列表' }
          },
          {
            path: 'create',
            name: 'UserCreate',
            component: () => import('@/views/user/create.vue'),
            meta: { title: '创建用户' }
          }
        ]
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  // 滚动行为
  scrollBehavior(to, from, savedPosition) {
    // 如果有保存的位置（如浏览器后退），恢复到那个位置
    if (savedPosition) {
      return savedPosition
    }
    // 否则滚动到顶部
    return { top: 0 }
  }
})

// 白名单（不需要登录就能访问的页面）
const whiteList = ['/login', '/404']

// 全局前置守卫
router.beforeEach(async (to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - NeoCC` : 'NeoCC'
  
  // 判断是否已登录
  const hasToken = getToken()
  
  if (hasToken) {
    // 已登录
    if (to.path === '/login') {
      // 已登录，访问登录页 → 重定向到首页
      next({ path: '/' })
    } else {
      // 放行
      next()
    }
  } else {
    // 未登录
    if (whiteList.includes(to.path)) {
      // 在白名单中，直接放行
      next()
    } else {
      // 不在白名单，跳转到登录页
      next(`/login?redirect=${to.path}`)
    }
  }
})

export default router
```

### 5.5 路由独享守卫

```javascript
const routes = [
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('@/views/admin/index.vue'),
    // 路由独享守卫
    beforeEnter: (to, from, next) => {
      // 检查是否是管理员
      const isAdmin = localStorage.getItem('role') === 'admin'
      
      if (isAdmin) {
        next()
      } else {
        next('/403')
      }
    }
  }
]
```

---

## 6. 路由传参

### 6.1 路由参数 vs 查询参数

| 类型 | 格式 | 获取方式 | 特点 |
|------|------|----------|------|
| 路由参数 | `/user/123` | `$route.params.id` | 必选，SEO 友好 |
| 查询参数 | `/user?id=123` | `$route.query.id` | 可选，显示在 URL |

### 6.2 路由参数

```vue
<script setup>
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

// 导航时传递参数
router.push({
  path: '/user/123'
})

// 获取参数
console.log(route.params.id)  // '123'
</script>
```

### 6.3 查询参数

```vue
<script setup>
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

// 导航时传递查询参数
router.push({
  path: '/user/list',
  query: {
    page: 1,
    pageSize: 10,
    name: '张三'
  }
})

// 获取参数
// URL: /user/list?page=1&pageSize=10&name=张三
console.log(route.query.page)      // '1'
console.log(route.query.pageSize)  // '10'
console.log(route.query.name)      // '张三'
</script>
```

### 6.4 使用 props 解耦

```javascript
// 路由配置
const routes = [
  {
    path: '/user/:id',
    name: 'UserDetail',
    component: () => import('@/views/UserDetail.vue'),
    // 开启 props
    props: true
  }
]
```

```vue
<!-- UserDetail.vue -->
<template>
  <div>
    <p>用户ID：{{ id }}</p>
  </div>
</template>

<script setup>
// 使用 props 接收路由参数（更清晰）
const props = defineProps({
  id: {
    type: String,
    required: true
  }
})

console.log(props.id)  // '123'
</script>
```

---

## 7. 项目实战

### 7.1 项目路由结构

```
src/
├── router/
│   └── index.js
├── layout/
│   ├── index.vue              # 主布局
│   └── components/
│       ├── Sidebar.vue        # 侧边栏
│       └── Navbar.vue         # 导航栏
├── views/
│   ├── home/
│   │   └── index.vue
│   ├── login/
│   │   └── index.vue
│   ├── sales/
│   │   ├── customer/
│   │   │   └── index.vue     # 客户管理
│   │   └── contract/
│   │       └── index.vue     # 合同管理
│   └── finance/
│       └── loan-audit/
│           └── index.vue      # 贷款审核
```

### 7.2 完整路由配置

**`src/router/index.js`**

```javascript
import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/layout'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken } from '@/utils/auth'

NProgress.configure({ showSpinner: false })

// 路由懒加载
const home = () => import('@/views/home/index.vue')
const login = () => import('@/views/login/index.vue')
const customer = () => import('@/views/sales/customer/index.vue')
const contract = () => import('@/views/sales/contract/index.vue')
const loanAudit = () => import('@/views/finance/loan-audit/index.vue')
const notFound = () => import('@/views/error/404.vue')

const routes = [
  // 登录页
  {
    path: '/login',
    name: 'Login',
    component: login,
    meta: { title: '登录' }
  },
  
  // 404
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: notFound,
    meta: { title: '页面不存在' }
  },
  
  // 主布局
  {
    path: '/',
    component: Layout,
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: home,
        meta: { title: '首页', icon: 'dashboard' }
      },
      {
        path: 'sales/customer',
        name: 'Customer',
        component: customer,
        meta: { title: '客户管理', icon: 'user' }
      },
      {
        path: 'sales/contract',
        name: 'Contract',
        component: contract,
        meta: { title: '合同管理', icon: 'document' }
      },
      {
        path: 'finance/loan-audit',
        name: 'LoanAudit',
        component: loanAudit,
        meta: { title: '贷款审核', icon: 'check' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

// 白名单
const whiteList = ['/login']

// 前置守卫
router.beforeEach((to, from, next) => {
  // 进度条开始
  NProgress.start()
  
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - NeoCC` : 'NeoCC'
  
  // 获取 Token
  const hasToken = getToken()
  
  if (hasToken) {
    // 已登录
    if (to.path === '/login') {
      next({ path: '/' })
    } else {
      next()
    }
  } else {
    // 未登录
    if (whiteList.includes(to.path)) {
      next()
    } else {
      next(`/login?redirect=${to.fullPath}`)
    }
  }
})

// 后置守卫
router.afterEach(() => {
  NProgress.done()
})

export default router
```

### 7.3 主布局组件

**`src/layout/index.vue`**

```vue
<template>
  <div class="app-wrapper">
    <!-- 侧边栏 -->
    <Sidebar class="sidebar-container" />
    
    <!-- 主内容区 -->
    <div class="main-container">
      <!-- 顶部导航 -->
      <Navbar />
      
      <!-- 页面内容 -->
      <div class="app-main">
        <!-- 路由出口 -->
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import Sidebar from './components/Sidebar.vue'
import Navbar from './components/Navbar.vue'
</script>

<style scoped>
.app-wrapper {
  display: flex;
  height: 100vh;
}

.sidebar-container {
  width: 200px;
  background: #304156;
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.app-main {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}
</style>
```

### 7.4 编程式导航实战

**`src/views/sales/customer/index.vue`**

```vue
<template>
  <div class="customer-list">
    <h2>客户列表</h2>
    
    <!-- 点击行跳转详情 -->
    <el-table :data="customerList" @row-click="handleRowClick">
      <el-table-column prop="id" label="ID" />
      <el-table-column prop="name" label="姓名" />
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button @click.stop="goDetail(row.id)">查看详情</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const customerList = ref([
  { id: 1, name: '张三' },
  { id: 2, name: '李四' }
])

// 点击行跳转
function handleRowClick(row) {
  router.push(`/sales/customer/${row.id}`)
}

// 按钮跳转
function goDetail(id) {
  router.push({
    name: 'CustomerDetail',
    params: { id }
  })
}
</script>
```

---

## 总结

### 路由核心概念

| 概念 | 说明 |
|------|------|
| `router-view` | 路由出口，显示匹配到的组件 |
| `router-link` | 声明式导航，渲染为 `<a>` |
| `router.push()` | 编程式导航，追加历史记录 |
| `router.replace()` | 编程式导航，不追加历史记录 |
| `route.params` | 动态路由参数 |
| `route.query` | URL 查询参数 |
| `beforeEach` | 全局前置守卫 |

### 路由守卫执行顺序

```
用户点击导航
    │
    ▼
beforeEach (全局前置)
    │
    ▼
路由解析
    │
    ▼
beforeEnter (路由独享)
    │
    ▼
组件渲染
    │
    ▼
afterEach (全局后置)
```

---

> **文档版本**: 1.0
> **最后更新**: 2026-04-22
> **作者**: AI Assistant
