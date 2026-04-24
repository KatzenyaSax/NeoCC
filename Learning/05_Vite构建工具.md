# Vite 构建工具入门指南

> 本文档讲解 Vite 的核心概念和配置，帮助你理解前端项目是如何构建和运行的。

---

## 目录

1. [什么是 Vite？](#1-什么是-vite)
2. [Vite vs Webpack](#2-vite-vs-webpack)
3. [Vite 核心概念](#3-vite-核心概念)
4. [项目配置详解](#4-项目配置详解)
5. [开发服务器配置](#5-开发服务器配置)
6. [生产构建配置](#6-生产构建配置)
7. [常用插件](#7-常用插件)
8. [项目实战配置](#8-项目实战配置)

---

## 1. 什么是 Vite？

### 1.1 官方定义

Vite 是一个新一代前端构建工具，利用浏览器原生 ES 模块（ESM）实现极快的开发服务器启动和热更新。

### 1.2 通俗理解

想象你要开一家餐厅：

**传统方式（Webpack）：**
```
1. 你要先把所有食材全部提前准备好（打包整个项目）
2. 然后才能开始营业（启动开发服务器）
3. 每次改一个菜，都要重新准备所有食材（重新打包整个项目）
```

**Vite 方式：**
```
1. 客人点菜时才准备对应的食材（按需编译）
2. 立即开始营业（秒级启动）
3. 改菜只需要准备那道菜（热更新只更新改动的部分）
```

### 1.3 Vite 的特点

| 特性 | 说明 |
|------|------|
| **极速启动** | 使用原生 ESM，无需打包，启动时间接近即时代码执行 |
| **热更新快** | 无论项目多大，热更新延迟都保持在一秒以内 |
| **按需编译** | 只编译当前访问的代码，而不是整个项目 |
| **开箱即用** | 对 TypeScript、JSX、CSS 等开箱即用 |
| **构建优化** | 使用 Rollup 进行高效打包 |

---

## 2. Vite vs Webpack

### 2.1 启动时间对比

```
┌─────────────────────────────────────────────────────────────┐
│                  项目启动时间对比（假设项目有 1000 个文件）   │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   Webpack:  ████████████████████████████████████████ 30秒   │
│                                                             │
│   Vite:     ██ 2秒                                          │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 详细对比

| 方面 | Webpack | Vite |
|------|---------|------|
| **启动方式** | 打包整个项目 | 直接启动服务器，按需编译 |
| **启动时间** | 30秒 ~ 几分钟（项目越大越慢） | 1-2秒 |
| **热更新** | 慢（重新打包） | 快（精准更新） |
| **配置复杂度** | 复杂 | 简单 |
| **生态** | 非常丰富 | 成长中，但够用 |
| **兼容性** | 兼容性好 | 现代浏览器 |
| **适用场景** | 复杂大型项目 | 中小型项目 |

### 2.3 工作原理对比

**Webpack 工作原理：**
```
┌─────────────────────────────────────────────────────────────┐
│                        Webpack                              │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   启动时:                                                   │
│   ┌─────────┐    ┌─────────┐    ┌─────────┐               │
│   │  入口文件  │───>│  递归分析  │───>│  生成依赖图 │          │
│   │ index.js │    │  import   │    │  (整个项目) │          │
│   └─────────┘    └─────────┘    └─────────┘               │
│         │              │                                   │
│         └──────────────┴─── 打包成 bundle ──> bundle.js   │
│                                                             │
│   开发时修改文件:                                           │
│   重新生成整个 bundle.js                                    │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

**Vite 工作原理：**
```
┌─────────────────────────────────────────────────────────────┐
│                         Vite                                │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   启动时:                                                   │
│   ┌─────────┐                                              │
│   │  启动服务器  │ ──> 服务立即就绪，不打包！                │
│   └─────────┘                                              │
│                                                             │
│   浏览器请求:                                              │
│   /src/main.js ──> Vite 按需编译 ──> 返回编译后的代码      │
│                                                             │
│   开发时修改文件:                                           │
│   只重新编译修改的文件，精准热更新                          │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 3. Vite 核心概念

### 3.1 依赖预构建

Vite 会使用 `esbuild` 预构建依赖，提高加载速度。

```javascript
// main.js
import Vue from 'vue'           // 第三方包会被预构建
import lodash from 'lodash'     // 预构建后加载更快
import dayjs from 'dayjs'       // 预构建后加载更快
```

**预构建的好处：**
1. 将 ESM 格式的包转换为 CommonJS
2. 合并多个小文件为一个，减少请求数
3. 将原生 Node 模块转换为浏览器可用格式

### 3.2 模块类型

Vite 原生支持多种模块类型：

```javascript
// JavaScript/TypeScript
import MyComponent from './MyComponent.vue'
import utils from './utils.js'
import * as utils from './utils.ts'

// CSS
import './styles/main.css'
import styles from './styles/main.scss'

// JSON
import packageJson from '../package.json'

// 图片/字体
import logo from './assets/logo.png'

// WASM
import init from './utils.wasm'
```

### 3.3 环境变量

**`.env` 文件：**
```bash
# .env - 所有环境
VITE_APP_TITLE=NeoCC管理系统
VITE_API_BASE=/api

# .env.development - 开发环境
VITE_APP_ENV=development

# .env.production - 生产环境
VITE_APP_ENV=production
```

**使用环境变量：**
```javascript
console.log(import.meta.env.VITE_APP_TITLE)  // 'NeoCC管理系统'
console.log(import.meta.env.VITE_APP_ENV)   // 'development'
```

---

## 4. 项目配置详解

### 4.1 基础配置

**`vite.config.js`：**

```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  // 项目根目录
  root: '.',
  
  // 公共基础路径
  // 开发环境通常为 /
  // 生产环境可以设置为 /xxx/
  base: '/',
  
  // 要使用的插件
  plugins: [
    vue()  // Vue 3 插件
  ],
  
  // 配置别名
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),  // @ 指向 src
      '~': path.resolve(__dirname, './')       // ~ 指向项目根目录
    }
  }
})
```

### 4.2 defineConfig 辅助函数

```javascript
import { defineConfig } from 'vite'

// defineConfig 提供更好的 TypeScript 类型提示
export default defineConfig({
  // 配置内容
})
```

### 4.3 resolve 配置

```javascript
export default defineConfig({
  resolve: {
    // 路径别名
    alias: {
      '@': path.resolve(__dirname, './src'),
      'components': path.resolve(__dirname, './src/components'),
      'utils': path.resolve(__dirname, './src/utils')
    },
    
    // 文件扩展名
    extensions: ['.mjs', '.js', '.ts', '.jsx', '.tsx', '.json', '.vue']
  }
})
```

---

## 5. 开发服务器配置

### 5.1 基础配置

```javascript
export default defineConfig({
  server: {
    // 开发服务器端口
    port: 3001,
    
    // 自动打开浏览器
    open: true,
    
    // 服务器主机名
    host: 'localhost',  // 或 '0.0.0.0' 允许外部访问
    
    // 是否启用 HTTPS
    https: false,
    
    // 代理配置
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

### 5.2 代理配置详解

代理用于解决跨域问题：

```javascript
export default defineConfig({
  server: {
    proxy: {
      // 示例1：代理 API 请求
      '/api': {
        target: 'http://localhost:8080',    // 目标服务器
        changeOrigin: true,                  // 修改请求头中的 Origin
        rewrite: (path) => path.replace(/^\/api/, '')  // 路径重写
      },
      
      // 示例2：代理多个路径
      '/auth': {
        target: 'http://localhost:8085',
        changeOrigin: true
      },
      
      // 示例3：完整配置
      '/ws': {
        target: 'ws://localhost:8080',
        ws: true  // 启用 WebSocket 代理
      }
    }
  }
})
```

### 5.3 代理工作原理

```
┌─────────────────────────────────────────────────────────────┐
│                      代理工作原理                             │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   浏览器                    Vite Dev Server                 │
│   ┌────────┐               ┌────────────┐                  │
│   │        │ ──请求──>     │            │                  │
│   │ /api/  │  /api/user    │  代理转换   │                  │
│   │  user  │               │  /user     │                  │
│   │        │ <──响应──     │            │ ──请求──>        │
│   └────────┘   数据        └────────────┘    http://       │
│                                             localhost:8080   │
│                                             /user           │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 6. 生产构建配置

### 6.1 基础配置

```javascript
export default defineConfig({
  build: {
    // 构建输出目录
    outDir: 'dist',
    
    // 生成 sourcemap（方便调试）
    sourcemap: false,
    
    // 构建后资源目录
    assetsDir: 'assets',
    
    // 单个资源文件大小限制（KB）
    // 超过此限制会提示警告
    assetsInlineLimit: 4096,
    
    // 关闭 css sourcemap
    cssCodeSplit: true,
    
    // 构建目标
    target: 'es2015',
    
    // 压缩配置
    minify: 'terser',
    
    // chunk 大小警告限制（KB）
    chunkSizeWarningLimit: 2000
  }
})
```

### 6.2 rollup 选项配置

```javascript
export default defineConfig({
  build: {
    rollupOptions: {
      // 入口文件
      input: {
        main: path.resolve(__dirname, 'index.html'),
        nested: path.resolve(__dirname, 'nested/index.html')
      },
      
      // 输出配置
      output: {
        // 手动分包（将大的依赖单独打包）
        manualChunks: {
          'vue-vendor': ['vue', 'vue-router', 'pinia'],
          'element-vendor': ['element-plus']
        },
        
        // chunk 文件名
        chunkFileNames: 'static/js/[name]-[hash].js',
        
        // 入口文件 chunk 名
        entryFileNames: 'static/js/[name]-[hash].js',
        
        // 静态资源名
        assetFileNames: 'static/[ext]/[name]-[hash].[ext]'
      }
    }
  }
})
```

### 6.3 分包策略

```javascript
export default defineConfig({
  build: {
    rollupOptions: {
      output: {
        manualChunks(id) {
          // Vue 核心库单独打包
          if (id.includes('node_modules/vue')) {
            return 'vue'
          }
          
          // Element Plus 单独打包
          if (id.includes('node_modules/element-plus')) {
            return 'element-plus'
          }
          
          // 其他第三方库合并打包
          if (id.includes('node_modules')) {
            return 'vendor'
          }
        }
      }
    }
  }
})
```

---

## 7. 常用插件

### 7.1 Vue 插件

```bash
npm install @vitejs/plugin-vue
```

```javascript
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()]
})
```

### 7.2 Vue JSX 插件

```bash
npm install @vitejs/plugin-vue-jsx
```

```javascript
import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'

export default defineConfig({
  plugins: [
    vue(),
    vueJsx()  // 支持 JSX/TSX 语法
  ]
})
```

### 7.3 自动导入插件

```bash
npm install -D unplugin-vue-components unplugin-auto-import
```

```javascript
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

export default defineConfig({
  plugins: [
    // 自动导入 Vue API（ref, computed 等）
    AutoImport({
      imports: ['vue', 'vue-router', 'pinia'],
      dts: 'src/auto-imports.d.ts'
    }),
    
    // 自动导入组件
    Components({
      resolvers: [
        // Element Plus 自动导入
        ElementPlusResolver()
      ],
      dts: 'src/components.d.ts'
    })
  ]
})
```

使用后，组件中无需手动导入：
```vue
<!-- 无需 import { ElButton } from 'element-plus' -->
<template>
  <el-button>按钮</el-button>  <!-- 直接使用 -->
</template>
```

### 7.4 环境变量插件

```bash
npm install -D dotenv
```

**`.env`**
```
VITE_API_BASE_URL=http://localhost:8080
```

**使用**
```javascript
console.log(import.meta.env.VITE_API_BASE_URL)
```

---

## 8. 项目实战配置

### 8.1 完整配置示例

**`vite.config.js`：**

```javascript
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

// https://vitejs.dev/config/
export default defineConfig(({ mode, command }) => {
  // 加载环境变量
  const env = loadEnv(mode, process.cwd())
  
  return {
    // 基础路径
    base: '/',
    
    // 插件
    plugins: [
      vue(),
      
      // 自动导入
      AutoImport({
        imports: ['vue', 'vue-router', 'pinia'],
        dts: 'src/auto-imports.d.ts'
      }),
      
      // 自动导入组件
      Components({
        resolvers: [
          ElementPlusResolver()
        ],
        dts: 'src/components.d.ts'
      })
    ],
    
    // 路径别名
    resolve: {
      alias: {
        '~': path.resolve(__dirname, './'),
        '@': path.resolve(__dirname, './src')
      },
      extensions: ['.mjs', '.js', '.ts', '.jsx', '.tsx', '.json', '.vue']
    },
    
    // 开发服务器
    server: {
      port: 3001,
      host: true,
      open: true,
      proxy: {
        // 开发环境 API 代理
        '/dev-api': {
          target: 'http://localhost:8086',
          changeOrigin: true,
          rewrite: (p) => p.replace(/^\/dev-api/, '')
        },
        
        // 销售服务
        '/sales/api': {
          target: 'http://localhost:8086',
          changeOrigin: true
        },
        
        // 金融服务
        '/finance/api': {
          target: 'http://localhost:8086',
          changeOrigin: true
        },
        
        // 系统服务
        '/system/api': {
          target: 'http://localhost:8086',
          changeOrigin: true
        }
      }
    },
    
    // 构建配置
    build: {
      outDir: 'dist',
      sourcemap: command === 'build' ? false : 'inline',
      assetsDir: 'assets',
      chunkSizeWarningLimit: 2000,
      rollupOptions: {
        output: {
          chunkFileNames: 'static/js/[name]-[hash].js',
          entryFileNames: 'static/js/[name]-[hash].js',
          assetFileNames: 'static/[ext]/[name]-[hash].[ext]',
          manualChunks: {
            'vue-vendor': ['vue', 'vue-router', 'pinia'],
            'element-vendor': ['element-plus']
          }
        }
      }
    },
    
    // CSS 配置
    css: {
      preprocessorOptions: {
        scss: {
          // 全局导入 scss 变量
          additionalData: `@import "@/styles/variables.scss";`
        }
      }
    }
  }
})
```

### 8.2 环境变量文件

**`.env.development`：**
```bash
VITE_APP_ENV=development
VITE_APP_TITLE=NeoCC管理系统（开发）
VITE_API_BASE_URL=http://localhost:8086
```

**`.env.production`：**
```bash
VITE_APP_ENV=production
VITE_APP_TITLE=NeoCC管理系统
VITE_API_BASE_URL=https://api.example.com
```

### 8.3 使用配置中的变量

```javascript
// 在代码中使用
console.log(import.meta.env.VITE_APP_TITLE)
console.log(import.meta.env.VITE_API_BASE_URL)

// 根据环境执行不同逻辑
if (import.meta.env.VITE_APP_ENV === 'development') {
  console.log('开发环境')
} else {
  console.log('生产环境')
}
```

---

## 总结

### Vite 核心配置速查表

| 配置项 | 说明 | 示例 |
|--------|------|------|
| `base` | 公共基础路径 | `'/'` 或 `'/app/'` |
| `plugins` | 插件 | `[vue()]` |
| `resolve.alias` | 路径别名 | `{ '@': './src' }` |
| `server.port` | 开发端口 | `3001` |
| `server.proxy` | 代理配置 | `{ '/api': {...} }` |
| `build.outDir` | 输出目录 | `'dist'` |
| `build.minify` | 压缩方式 | `'terser'` |

### 开发流程

```
1. npm install          安装依赖
2. npm run dev          启动开发服务器
3. 编写代码            Vite 热更新
4. npm run build        构建生产版本
5. npm run preview      预览构建结果
```

---

> **文档版本**: 1.0
> **最后更新**: 2026-04-22
> **作者**: AI Assistant
