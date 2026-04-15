# RuoYi 前端清理文档

本文档记录了为适配 NeoCC 后端而删除的 RuoYi 前端接口和组件。

## 1. 删除的 API 接口

### 1.1 login.js - 移除的接口
- `register()` - 注册功能（NeoCC 后端无此接口）
- `unlockScreen()` - 解锁屏幕功能（NeoCC 后端无此接口）
- `getCodeImg()` - 获取验证码功能（NeoCC 后端已禁用验证码）

### 1.2 删除的整个 API 目录

#### api/system/ 目录（系统管理模块）
| 文件 | 功能 | 删除原因 |
|------|------|----------|
| user.js | 用户管理 | NeoCC 使用独立的用户管理接口 |
| role.js | 角色管理 | NeoCC 后端无此功能 |
| menu.js | 菜单管理 | NeoCC 使用独立的菜单接口 |
| dept.js | 部门管理 | NeoCC 后端无此功能 |
| post.js | 岗位管理 | NeoCC 后端无此功能 |
| config.js | 参数配置 | NeoCC 后端无此功能 |
| notice.js | 通知公告 | NeoCC 后端无此功能 |
| dict/data.js | 字典数据 | NeoCC 后端无此功能 |
| dict/type.js | 字典类型 | NeoCC 后端无此功能 |

#### api/monitor/ 目录（系统监控模块）
| 文件 | 功能 | 删除原因 |
|------|------|----------|
| cache.js | 缓存监控 | NeoCC 后端无此功能 |
| job.js | 定时任务 | NeoCC 后端无此功能 |
| jobLog.js | 任务日志 | NeoCC 后端无此功能 |
| logininfor.js | 登录日志 | NeoCC 后端无此功能 |
| online.js | 在线用户 | NeoCC 后端无此功能 |
| operlog.js | 操作日志 | NeoCC 后端无此功能 |
| server.js | 服务监控 | NeoCC 后端无此功能 |

#### api/tool/ 目录（系统工具模块）
| 文件 | 功能 | 删除原因 |
|------|------|----------|
| gen.js | 代码生成 | NeoCC 后端无此功能 |

## 2. 删除的视图页面

### 2.1 删除的视图目录
- `views/system/` - 系统管理页面（用户、角色、菜单、部门、岗位、字典、参数、通知等）
- `views/monitor/` - 系统监控页面（在线用户、定时任务、日志、缓存、服务器等）
- `views/tool/` - 系统工具页面（代码生成等）

### 2.2 删除的单个视图文件
- `views/register.vue` - 注册页面
- `views/lock.vue` - 锁定屏幕页面

## 3. 删除的组件

### 3.1 布局组件
- `layout/components/HeaderNotice/` - 消息通知组件

## 4. 删除的 Store 模块

| 文件 | 功能 | 删除原因 |
|------|------|----------|
| store/modules/dict.js | 字典缓存 | 字典功能已删除 |

## 5. 修改的文件

### 5.1 src/main.js
- 移除 `getConfigKey` 导入和全局挂载

### 5.2 src/utils/dict.js
- 简化为返回空数组，不再调用后端字典接口

### 5.3 src/layout/components/Navbar.vue
- 移除消息通知组件
- 移除锁定屏幕功能
- 移除 `useLockStore` 和 `HeaderNotice` 导入

### 5.4 src/router/index.js
- 移除注册、锁定屏幕、个人中心等路由
- 移除所有动态路由（system、monitor、tool 相关）
- 简化为仅保留登录、首页、404、401 路由

## 6. 保留的接口

以下是 NeoCC 后端支持并保留的接口：

| 接口路径 | 功能 | 文件 |
|----------|------|------|
| `/login` | 用户登录 | api/login.js |
| `/logout` | 用户登出 | api/login.js |
| `/getInfo` | 获取用户信息 | api/login.js |
| `/getRouters` | 获取路由菜单 | api/menu.js |

## 7. 保留的视图页面

| 页面 | 路径 | 功能 |
|------|------|------|
| 登录页 | views/login.vue | 用户登录 |
| 首页 | views/index.vue | 系统首页 |
| 401页面 | views/error/401.vue | 无权限提示 |
| 404页面 | views/error/404.vue | 页面不存在 |
| 重定向 | views/redirect/index.vue | 路由重定向 |

## 8. 后续工作

如需添加 NeoCC 业务相关的功能，请在以下位置创建新文件：

1. **API 接口**: `src/api/` 目录下创建新文件
2. **视图页面**: `src/views/` 目录下创建新文件
3. **路由配置**: `src/router/index.js` 中添加新路由

---

**清理日期**: 2026-04-15
**清理原因**: 适配 NeoCC 后端接口，移除冗余功能
