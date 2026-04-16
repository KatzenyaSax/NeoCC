# Qoder Skills 使用指南（新手入门）

## 📚 什么是 Skills？

**Skills（技能）** 是 Qoder AI 的扩展功能模块，它们教会 AI 如何执行特定的专业任务。你可以把 Skills 理解为"AI 的专业技能包"。

### 类比理解
- 就像手机安装 App 获得新功能一样
- 每个 Skill 教授 AI 一项专门能力
- 安装后，AI 就能执行该领域的专业任务

---

## 📁 Skills 文件位置

在你的项目中，Skills 文件存放在：

```
/Users/liuhongyu/IdeaProjects/final/NeoCC/.qoder/skills/
```

### 当前已安装的 Skills

你的项目已经有 2 个 Skills：

1. **agent-browser** - 浏览器自动化技能
   - 位置：`.qoder/skills/agent-browser/`
   - 功能：让 AI 能控制浏览器执行自动化操作

2. **ruoyi-frontend-integration** - RuoYi 前端集成技能
   - 位置：`.qoder/skills/ruoyi-frontend-integration/`
   - 功能：指导 AI 如何在 NeoCC 项目中集成 RuoYi-Vue3 前端

---

## 🎯 Skill 文件结构

每个 Skill 通常包含以下文件：

```
skill-name/
├── SKILL.md              # 必需：技能定义文件（核心）
├── references/           # 可选：参考文档
│   ├── doc1.md
│   └── doc2.md
└── templates/            # 可选：模板文件
    └── template1.sh
```

### SKILL.md 文件解析

打开任何一个 `SKILL.md`，你会看到两个主要部分：

#### 1. Front Matter（顶部元数据）

位于文件顶部的 `---` 之间：

```yaml
---
name: skill名称
description: 技能描述（告诉 AI 何时使用这个技能）
allowed-tools: 允许使用的工具列表
---
```

**关键字段说明：**
- `name`: 技能的唯一标识名
- `description`: **最重要！** 描述这个技能的用途和触发条件
- `allowed-tools`: 定义此技能可以调用哪些系统工具

#### 2. 正文内容（Markdown 格式）

这是技能的实际指令内容，包括：
- 使用步骤
- 代码示例
- 最佳实践
- 参考资料

---

## 🚀 如何使用 Skills

### 方式 1：自动触发（推荐新手）

**你不需要手动操作！** Qoder 会自动：

1. 读取你的请求
2. 匹配最合适的 Skill
3. 加载并应用该技能的指导
4. 执行专业任务

**示例场景：**

| 你说的 | 自动触发的 Skill |
|--------|------------------|
| "帮我打开网站并截图" | agent-browser |
| "集成 RuoYi 前端" | ruoyi-frontend-integration |
| "自动化填写表单" | agent-browser |
| "部署 Docker 环境" | ruoyi-frontend-integration |

### 方式 2：手动调用

如果你想明确指定使用某个 Skill，可以这样说：

```
请使用 ruoyi-frontend-integration 技能来帮我配置前端
```

或者使用斜杠命令格式：

```
/ruoyi-frontend-integration
```

---

## 📖 实际使用示例

### 示例 1：使用浏览器自动化

**你的请求：**
```
帮我打开百度并搜索 "Qoder AI"
```

**AI 的工作流程：**
1. 识别到需要浏览器操作 → 匹配 `agent-browser` 技能
2. 读取 `agent-browser/SKILL.md` 了解使用方法
3. 查看 `references/` 中的命令文档
4. 使用正确的命令执行浏览器操作

### 示例 2：集成 RuoYi 前端

**你的请求：**
```
我想在 NeoCC 项目中添加 RuoYi 管理后台
```

**AI 的工作流程：**
1. 识别到 RuoYi 集成需求 → 匹配 `ruoyi-frontend-integration` 技能
2. 按照 SKILL.md 中的 7 个步骤指导你
3. 提供准确的代码示例和配置
4. 确保前后端正确对接

---

## 🔧 如何添加新 Skill

### 方法 1：使用 create-skill 工具（推荐）

直接告诉 AI：

```
帮我创建一个新的 skill，用于 XXX 功能
```

AI 会使用 `create-skill` 工具引导你完成创建。

### 方法 2：手动创建

1. 在 `.qoder/skills/` 下创建新文件夹：

```bash
mkdir .qoder/skills/my-custom-skill
```

2. 创建 SKILL.md 文件：

```markdown
---
name: my-custom-skill
description: 当用户需要执行XXX任务时使用此技能
---

# 我的自定义技能

## 使用步骤

1. 第一步...
2. 第二步...

## 示例代码

\`\`\`bash
# 你的命令
\`\`\`
```

3. （可选）添加参考资料和模板：

```bash
mkdir .qoder/skills/my-custom-skill/references
mkdir .qoder/skills/my-custom-skill/templates
```

---

## 💡 新手最佳实践

### ✅ 推荐做法

1. **自然语言交流**
   - 直接用中文描述你的需求
   - 不需要记住 skill 名称
   - AI 会自动匹配

2. **查看现有 Skills**
   - 浏览 `.qoder/skills/` 目录
   - 阅读 SKILL.md 了解可用功能
   - 这样你知道 AI 能做什么

3. **观察 AI 行为**
   - 注意 AI 何时加载了某个 skill
   - 学习触发条件
   - 积累使用经验

### ❌ 避免的错误

1. **不要直接修改 SKILL.md 的 Front Matter**
   - 除非你清楚每个字段的作用
   - 错误的 description 会导致匹配失败

2. **不要在 Skill 文件夹外创建文件**
   - 所有 skill 相关文件必须在对应文件夹内
   - 保持目录结构清晰

3. **不要忘记 description 的重要性**
   - 这是 AI 判断何时使用技能的唯一依据
   - 要详细、准确地描述使用场景

---

## 🎓 进阶技巧

### 1. 查看 Skill 触发日志

当 AI 使用 skill 时，你会看到类似提示：

```
[Skill 已加载: agent-browser]
```

这表示某个技能已被激活。

### 2. 组合使用多个 Skills

复杂任务可能触发多个 skills：

```
"帮我部署 RuoYi 前端，然后自动化测试登录功能"
```

AI 会：
1. 先用 `ruoyi-frontend-integration` 部署
2. 再用 `agent-browser` 测试登录

### 3. 自定义 Skill 优先级

通过精确的 `description` 控制匹配：

```yaml
description: >
  仅在用户明确要求"高级数据分析"时使用，
  包含复杂统计和可视化。
  不适用于简单的数据查询。
```

---

## 🔍 常见问题 FAQ

### Q1: Skills 会影响 AI 的正常对话吗？
**A:** 不会。Skills 只在匹配到相关需求时才会激活，日常对话不受影响。

### Q2: 我可以删除不需要的 Skill 吗？
**A:** 可以。直接删除对应的文件夹即可：

```bash
rm -rf .qoder/skills/不需要的skill名称
```

### Q3: Skills 是全局的还是项目级别的？
**A:** 当前项目级别的。每个项目可以有自己独立的 skills。

### Q4: 如何查看某个 Skill 的详细内容？
**A:** 直接用文本编辑器打开 SKILL.md，或让我帮你读取：

```
帮我看看 agent-browser 这个 skill 的具体内容
```

### Q5: Skills 会消耗更多资源吗？
**A:** 不会。Skills 只是文本文件，只有在使用时才会被读取。

---

## 📝 快速参考卡片

### 一句话总结

> **Skills = 给 AI 安装专业技能包，让它从"通用助手"变成"领域专家"**

### 常用命令速查

| 需求 | 操作 |
|------|------|
| 查看可用 skills | 浏览 `.qoder/skills/` 目录 |
| 使用某个 skill | 自然语言描述需求即可 |
| 创建新 skill | 说"帮我创建一个 skill" |
| 删除 skill | 删除对应文件夹 |
| 查看 skill 详情 | 阅读 SKILL.md 文件 |

---

## 🎉 开始你的 Skills 之旅

现在你已经了解了基础知识，可以：

1. **试试现有 Skills**
   ```
   帮我用浏览器打开项目的前端页面
   ```

2. **创建你的第一个 Skill**
   ```
   我想创建一个 skill 来处理项目的日常部署任务
   ```

3. **深入探索**
   - 阅读 `.qoder/skills/agent-browser/SKILL.md`
   - 了解 AI 是如何理解和执行专业任务的

---

## 📞 需要帮助？

随时可以问我：
- "这个 skill 是做什么的？"
- "如何改进我的 skill description？"
- "为什么 AI 没有触发我期望的 skill？"

祝你在 Qoder 的世界里玩得开心！🚀
