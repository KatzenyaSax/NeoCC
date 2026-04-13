# ppt风格要求

## 1. 全局设计规范 (Global Design Spec)

* **色彩系统 (Color Palette):**
    * **全局背景色 (Slide Background):** 极浅的苹果灰 `RGB(245, 245, 247)`。如果有条件，请使用从左上到右下的极微弱浅灰到纯白的渐变。
    * **主标题/正文颜色 (Primary Text):** 苹果深灰（近乎黑）`RGB(29, 29, 31)`。
    * **辅助说明颜色 (Secondary Text):** 中灰色 `RGB(134, 134, 139)`。
* **核心构图元素 (Core Composition Elements):**
    * **卡片载体 (The Cards):** 所有的核心内容都必须放置在“卡片”内。不要让文字直接漂浮在背景上。
    * **圆角矩形 (Rounded Rectangles):** 卡片形状强制使用圆角矩形。圆角半径（Border Radius）应适中，体现出现代且圆润的切割感。
    * **模拟高斯模糊 (Simulated Glass Effect):** 卡片的填充色设置为纯白 `RGB(255, 255, 255)`，但**必须附带极其柔和、弥散的浅灰色外部阴影 (Drop Shadow)**，以模拟卡片悬浮在背景上的毛玻璃层级感。
* **排版系统 (Typography):**
    * **字体:** 无衬线字体（Sans-serif），优先选用 `Helvetica`、`Arial` 或 `Microsoft YaHei`。
    * 标题字重极粗（Heavy/Bold），正文字重极细（Light/Regular），形成强烈的粗细对比。

## 2. 幻灯片版式组件库 (Slide Archetypes)

请根据下文中提供的文案内容，选择以下 4 种卡片版式进行生成：

**版式 A：The Hovering Cover (悬浮封面页)**
* 布局：页面正中央放置一个巨大的纯白圆角矩形卡片，带有柔和阴影。卡片占据画面约 60% 的面积。
* 内容：主标题（极大字号，粗体，居中），副标题（常规字号，中灰色，居中）。

**版式 B：The Dashboard (仪表盘数据页)**
* 布局：页面背景为极浅灰。画面中排列 2-3 个独立的纯白圆角矩形小卡片（水平排列或网格排列）。
* 内容：每个卡片内部是一个独立的数据模块。卡片顶部是数据（极大字号），底部是一句简短的解释。

**版式 C：The Focus Card (焦点阐述页)**
* 布局：左侧留白或放置简单的图形，右侧放置一个纵向的白色圆角矩形大卡片。
* 内容：长句或核心观点写在卡片内。利用卡片边界收束读者的视线。

**版式 D：The Vision List (现代卡片列表)**
* 布局：摒弃传统的圆点列表（Bullet points）。将每一条列表内容，分别封装成一个横向的、带阴影的白色圆角矩形条带（Banner Card），从上到下等距排列。

## 3. 执行要求
请确认你完全理解了这套“浅色卡片+柔和阴影模拟悬浮”的 UI 规范。请避免使用任何生硬的直角边框或高饱和度色块。


# 正文

# 标题：Harness Engineering 工程实践：打不过 Agent 那就加入

### page1：封面

### page2：目录



## 第一章：在开发中引入 Agent 的便利

### page3：第一章封面

### page4：先让我们回想前 Agent 时代的 AI Coding

载体：传统对话框式网页大语言模型

双重局限：
    1.模型的输出需要你自行搬运到项目中，所以coding实际上还是人进行的；
    2.模型看不到你的本地项目，模型只能正确处理较为原子的任务，对于更抽象更宏大的任务的处理能力甚至不如学生

不论用多么精确多么规范的提示词，永远无法让 AI 发挥出它应有的实力，用户用这样的方式进行开发，体验只会糟糕透顶。

### page5：再来看看我是如何用 Claude Code 开发的 part1

agent 帮助我 brainstorm，将模糊的需求转化为 需求分析 & 技术选型 & 开发流程 & 验收标准 & 风险控制 

### page6：再来看看我是如何用 Claude Code 开发的 part2

三步走战略：规划-执行-审查-再规划，agent 自己思考-自己执行-自己反思，直到符合规范，或者风险降低到可控范围

先 plan 出一个具体规划，再 execute 这个规划，最后 qa 执行效果，查找错误并执行修复

### page7：再来看看我是如何用 Claude Code 开发的 part3

前面是 agent 干的活，那我呢？

我的任务：
    1.写规范的提示词（Specification）文档；
    2.调用 skill 让 Agent 以不同的身份干不同的事；
    2.方案的决定 & 否决 & 交由 agent 自行裁决，全部由我管辖。

我的身份转变：由 “打工人” 变成 “老板”

时间花销的变化：100分钟的手动coding ——> 10分钟的提示词书写 + 10分钟的等待 + 10分钟的纠错 

### page8：Agent VS 对话框

通过接入标准化的接口协议（MCP），大语言模型最终得以获得了直接读写本地文件的能力（也就是感知能力），Agent 就此诞生

Agent 几乎克服了基于对话框式大语言模型的 AI Coding 的全部缺点：在获取了 “眼” 后，Agent 得以窥见项目的全貌；获得了 “手” 后，Agent 能够真正意义上自己 Coding

结论：Agent 在 Coding 方面是对话框式的上位迭代



## 第二章：Skill：Agent 的 “数字器官”

### page9：第二章封面

### page10：互联网常言的 skill 究竟是什么？

其本质：可以是一个文档，也可以是一个包含文档&工具等资源的文件夹，只要是能让 agent 读取以达成某些效果的都属于skill。 Agent 可以随时随地地调用 skill 从而提升任务效果。

和提示词有什么区别？可以认为是封装的提示词，类比函数或方法，你可以只知道它的作用而不知道实现

对于工程实践而言，skill 是规范和约束，它约束着 Agent 应该做什么，应该怎么做，绝对不能做什么，或绝对不能忘记什么

### page11：skill 能做什么？

一句话：什么都能干，只要一件事能用代码实现，都能封装成一个 skill 。

比方写 latex，画 draw.io 格式的框图，或者做ppt。我这个 ppt 就是用 /example-skills:pptx 做的。

### page12：Harness Engineering：基于 Skill 的 Agent 开发新范式

Skill 是规范，Harness （鞍具）就是由规范组成的框架，Harness Engineering（驾驭工程）就是为 Agent 构建一个无法犯错，必须按照正确步骤，可溯源可控制的开发环境

在Harness Engineering 中，Agent = Harness + Model，claude code 是 harness，skill 是harness，开发者也是 harness

所以我们不是在 model 本身的性能上下工夫，而是将角度放在如何更精确更高效地发挥出 model 性能，所以叫做 “驾驭工程”

### page13：Harness Engineering：当前最新实践方案

经过实际检验的是 superpowers & gstack ：这是两个 skill 包，前者为执行带来强制性与标准化，后者以多元的团队视角审视项目

superpowers 配备了硬件级别的 TDD（Test Driven Development） ，规定 agent 必须在执行时进行单元测试，结果必须经过严格验收，保证了代码层级的可信

gstack 则将重心放在项目质量的保证。gstack 定义了若干不同身份的 skill，在 规划-质疑 的循环中不断审视项目，项目进展 & 实施状况 & 需求合理性 等

画图：
```
标题：gstack规划 + superpowers执行 + gstack审查
流程：/plan-ceo-review CEO质疑方向 -> /brainstorm 需求澄清 -> /plan-eng-review 拆解任务  -> /review 偏执型审查 -> /execute-plan 执行计划 -> 
/qa 健康评分
```

两个 skill 包恰好互补，构建了当前最主流 Harness Engineering 工程开发的标准实践流程。

### page13：Harness Engineering 最大优势：subagent & agent teams

依靠 Skill 提供的强大功能，Agent 能主动构建 subagent 与 agent teams

subagent：子 agent，由主 agent 主动调用，只给必要且干净的上下文，subagent 能够以极高的专注度与低级的上下文干扰完成任务，也不会干扰到主 agent

agent teams：最热门的方向之一，若干 agent 围绕同一个任务链，自动分配角色与任务，并发运行规划&执行&审查&纠错等任务

两种 agent 构建模式可以交叉融合，构筑反馈回环，开发者能够定制化属于自己的 harness



## 第三章：从 Vibe Coding 到 Harness Engineering

### page14：第三章封面

### page15：Vibe Coding：想法驱动开发（2022-2023）

Vibe Coding（Vibration，氛围感编程）：直接告诉模型自己想干什么，不用文档也不用规范，感觉对了就继续——感觉错了就改正，直到最后做出一个可用的产品。很多人在使用网页大模型时候就在不知不觉地 Vibe 了。

缺点很明显，没有任何书面规范，错误无法溯源，进度无法预测，风险无法管控。

### page16：Context Rot & Context Drift

更为隐藏的问题：context rot（上下文腐烂）和 Context Drift （上下文漂移）

产生的原因：在一次会话内上下文太长，会导致前面的上下文信息被压缩，表现出来就是模型错误记忆和幻觉。

你在第一轮对模型说的话，模型第五轮就记错了，第十轮早就忘了。而模型如果在第七轮写了一个有偏差甚至错误的方法，当你第十二轮才发现问题的时候，早已为时已晚

### page17：Spec Coding：规范驱动开发（2023-2025）

Vibe Coding 没有取得市场的主流认可，而 Agent 对于本地文件可读写的能力给了一种可行的改进方案：将严谨定义的提示词本地持久化。

这种思路一是让 AI Coding 有了足以支持工程开发的规范标准，由想法驱动向设计驱动转变；二是重要信息的本地化，极大地避免了重要信息被压缩的情况。

### page18: Harness Engineering：AI Coding 从随性到可信（2025——）

然而，Spec Coding 仍然没能保障最关键的代码可信度的问题： 1.虽然给了规范化提示词，但是无法强制保证 Agent 一定会按照流程执行；2.只管输入和输出，内部代码仍然是黑箱，问题仍然无法溯源。

而 Harness Engineering 的设计哲学让 AI Coding 彻底有了可信度：在 Spec Coding 的基础上嵌套一层框架牵制 Agent 必须按照规划执行；通过自检反馈循环保障生成的代码一定可用

最重要的是，整个过程均在开发者的控制范围内，Agent 的失误本质上就是开发者的失误.



## 第四章：Agent 冲击下我们何去何从？

### page19：第四章封面

### page20：Agent 正在瓦解传统组织架构

一个不可回避的问题是：熟练的 Agent 开发者调用 Agent 开发效率远大于传统开发者

一个更加不容忽视的问题是：当前互联网正在快速取缔传统开发者

证据：各家大厂都在加紧布局 Agent 基础设施，面试内容更加偏重 Agent，更倾向于选择熟练使用 Agent 的求职者

案例：就在今年年初：某某大厂干掉某部门前端部门，某某大厂薪资倒挂，某某 Agent 新型初创被老牌企业干掉等

### page21：普通开发者怎么办？

· 认清形势，放弃幻想。Agent 可能会崩盘，但是起码不是近几年，趁着技术仍在探索的窗口期顺势跟上。

· 转变观念，尝试以管理者视角思考。在 Agent 时代开发者被异化为 harness 的一环，企业会更加期望招募 能够管理一组 Agent 的高级架构师。


### page22：结尾

【agent自行总结，以一句话概况ppt主要内容】















