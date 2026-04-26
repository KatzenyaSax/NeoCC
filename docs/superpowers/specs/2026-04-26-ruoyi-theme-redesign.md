# RuoYi 前端主题焕新设计

**日期**: 2026-04-26
**目标**: 将 RuoYi 后台管理系统的 Element Plus 默认蓝色主题全面升级为现代暖灰棕 + 焦橙点缀的视觉体系。

## 设计决策

| 决策 | 选择 |
|------|------|
| 改动范围 | 配色 + 布局全面重组 |
| 视觉方向 | 活力现代风（大圆角、侧边栏分组、胶囊标签） |
| 主色调 | 焦赭色 #c2410c（暖橙低饱和） |
| 主题数量 | 单套默认主题 |
| 实现方式 | CSS 变量体系重构（方案 A） |

## 配色体系

### 主色系统 — 焦赭色

```
50:  #fff7ed    100: #ffedd5    200: #fed7aa
300: #fdba74    400: #fb923c    500: #f97316
600: #ea580c    700: #c2410c    800: #9a3412    900: #7c2d12
```

- **Primary**: `#c2410c`
- **Primary Dark**: `#9a3412`
- **Primary Light**: `#fed7aa`

### 功能色

| 用途 | 浅色 | 主色 | 深色 |
|------|------|------|------|
| Success | `#f0fdf4` | `#22c55e` | `#16a34a` |
| Warning | `#fffbeb` | `#f59e0b` | `#d97706` |
| Danger | `#fef2f2` | `#ef4444` | `#dc2626` |
| Info | `#eff6ff` | `#3b82f6` | `#1d4ed8` |

### 中性色 — 暖灰阶

基于 Tailwind Warm Gray（Stone）色系：

| 用途 | Token | 值 |
|------|-------|-----|
| 侧边栏背景 | `--sidebar-bg` | `#1c1917` |
| 内容区背景 | `--content-bg` | `#fafaf9` |
| 卡片背景 | `--card-bg` | `#ffffff` |
| 主文字 | `--text-primary` | `#1c1917` |
| 次文字 | `--text-secondary` | `#78716c` |
| 辅助文字 | `--text-tertiary` | `#a8a29e` |
| 边框 | `--border-color` | `#e7e5e4` |
| 浅底 | `--bg-subtle` | `#fafaf9` |

### 配色比例

- 80% 中性色（暖灰黑 / 暖石灰白 / 中性灰边框）
- 15% 暖灰（`#44403c` 等中间色）
- 5% 焦橙 #c2410c 点缀（激活态、主按钮、分页选中）

## 圆角规范

| Token | 值 | 用途 |
|-------|-----|------|
| `--radius-sm` | 6px | 按钮、输入框、标签 |
| `--radius-md` | 10px | 卡片、下拉菜单 |
| `--radius-lg` | 16px | 统计卡片、弹窗 |
| `--radius-xl` | 20px | 大容器、抽屉 |

## 阴影规范

- 卡片悬浮：`0 1px 3px rgba(0,0,0,0.04)`
- 下拉菜单：`0 4px 16px rgba(0,0,0,0.08)`
- 侧边栏：`2px 0 8px rgba(0,0,0,0.06)`

## 间距规范

所有间距基于 `4px` 单位递增：4, 8, 12, 16, 20, 24, 32

## 侧边栏设计

- 背景色：`#1c1917`（暖灰黑，非渐变）
- 菜单文字：`#a8a29e`（非激活）/ `#d6d3d1`（hover）
- 激活项：左边框 `2px solid #c2410c` + `rgba(194,65,12,0.08)` 微底
- Logo 区：渐变色块 Logo + 副标题
- 分组标题：大写字母 `#57534e`，`letter-spacing: 1px`
- 子菜单缩进 `28px`
- 底部固定用户信息栏

## 内容区设计

- 背景：`#fafaf9`（暖石灰白）
- 顶栏：毛玻璃效果 `backdrop-filter: blur(12px)`，底部边框 `#e7e5e4`
- 标签页：非激活白底灰边框，激活态 `#c2410c` 实色胶囊
- 统计卡片：白底 `12px` 圆角，`1px solid #e7e5e4` 边框，弱阴影
- 表格头：`#fafaf9` 浅底，`#78716c` 文字
- 状态标签：圆角胶囊 `border-radius: 20px`，柔色底
- 主按钮：`#c2410c` 实色，`8px` 圆角
- 分页选中：`#c2410c` 实色

## 实现范围

### 需要改动的文件

1. `ruoyi-ui/src/assets/styles/variables.module.scss` — 重新定义所有 CSS 变量和 SCSS 变量
2. `ruoyi-ui/src/assets/styles/sidebar.scss` — 侧边栏新样式（激活态、分组标题、子菜单）
3. `ruoyi-ui/src/assets/styles/ruoyi.scss` — 全局组件样式覆盖（表格、卡片、按钮、标签、分页）
4. `ruoyi-ui/src/assets/styles/element-ui.scss` — Element Plus 组件变量覆写
5. `ruoyi-ui/src/assets/styles/index.scss` — 全局基础样式调整

### 不需要改动的文件

- 所有 `.vue` 组件文件（样式通过 CSS 变量和全局类覆盖，无需修改模板）
- `btn.scss`、`mixin.scss`、`transition.scss` 等辅助样式
- 暗黑模式部分（后续迭代添加）

## 技术要点

- 建立语义化 CSS 自定义属性体系，在 `:root` 中定义
- Element Plus 组件颜色通过 `--el-color-primary` 等官方变量覆写
- 自定义组件通过 `--sidebar-bg`、`--border-color` 等自有变量控制
- 不修改任何 `.vue` 模板，纯 CSS 层面完成焕新
- 保留现有暗黑模式变量结构，后续可新增暗黑版

## 后续扩展

- 暗黑模式适配（基于同一 Token 体系）
- 多主题切换能力（Token 值变体）
