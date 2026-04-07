const pptxgen = require("pptxgenjs");

// ============ Global Design Spec ============
const COLORS = {
  bgLight: "F5F5F7",
  bgWhite: "FFFFFF",
  textPrimary: "1D1D1F",
  textSecondary: "868689",
  accent: "0071E3",
  cardShadow: "000000",
};

const makeShadow = () => ({
  type: "outer",
  color: COLORS.cardShadow,
  blur: 8,
  offset: 3,
  angle: 135,
  opacity: 0.12,
});

// ============ Layout Helpers ============

// Card A: Hovering Cover
function addCoverSlide(pres, title, subtitle) {
  let slide = pres.addSlide();
  slide.background = { color: COLORS.bgLight };

  slide.addShape(pres.shapes.ROUNDED_RECTANGLE, {
    x: 1.5, y: 1.5, w: 7, h: 2.8,
    fill: { color: COLORS.bgWhite },
    rectRadius: 0.15,
    shadow: makeShadow(),
  });

  slide.addText(title, {
    x: 1.5, y: 1.8, w: 7, h: 1.2,
    fontSize: 36, fontFace: "Microsoft YaHei",
    color: COLORS.textPrimary, bold: true, align: "center", valign: "middle",
  });

  slide.addText(subtitle, {
    x: 1.5, y: 3.1, w: 7, h: 0.8,
    fontSize: 18, fontFace: "Microsoft YaHei",
    color: COLORS.textSecondary, align: "center", valign: "middle",
  });
  return slide;
}

// Card B: Dashboard (2-3 data cards)
function addDashboardSlide(pres, title, cards) {
  let slide = pres.addSlide();
  slide.background = { color: COLORS.bgLight };

  slide.addText(title, {
    x: 0.5, y: 0.4, w: 9, h: 0.6,
    fontSize: 28, fontFace: "Microsoft YaHei",
    color: COLORS.textPrimary, bold: true,
  });

  const cardW = 2.8;
  const gap = 0.35;
  const startX = (10 - (cardW * cards.length + gap * (cards.length - 1))) / 2;
  const cardY = 1.3;
  const cardH = 3.5;

  cards.forEach((card, i) => {
    const x = startX + i * (cardW + gap);
    slide.addShape(pres.shapes.ROUNDED_RECTANGLE, {
      x, y: cardY, w: cardW, h: cardH,
      fill: { color: COLORS.bgWhite },
      rectRadius: 0.12,
      shadow: makeShadow(),
    });

    slide.addText(card.stat, {
      x, y: cardY + 0.5, w: cardW, h: 0.9,
      fontSize: 42, fontFace: "Microsoft YaHei",
      color: COLORS.accent, bold: true, align: "center",
    });

    slide.addText(card.label, {
      x: x + 0.2, y: cardY + 1.6, w: cardW - 0.4, h: 1.5,
      fontSize: 14, fontFace: "Microsoft YaHei",
      color: COLORS.textSecondary, align: "center", valign: "top",
    });
  });
  return slide;
}

// Card C: Focus Card (left text, right big card)
function addFocusSlide(pres, title, mainText, bulletPoints) {
  let slide = pres.addSlide();
  slide.background = { color: COLORS.bgLight };

  slide.addText(title, {
    x: 0.5, y: 0.4, w: 9, h: 0.6,
    fontSize: 28, fontFace: "Microsoft YaHei",
    color: COLORS.textPrimary, bold: true,
  });

  slide.addShape(pres.shapes.ROUNDED_RECTANGLE, {
    x: 4.5, y: 1.2, w: 5, h: 3.9,
    fill: { color: COLORS.bgWhite },
    rectRadius: 0.12,
    shadow: makeShadow(),
  });

  slide.addText(mainText, {
    x: 4.8, y: 1.5, w: 4.4, h: 1.2,
    fontSize: 16, fontFace: "Microsoft YaHei",
    color: COLORS.textPrimary, bold: true,
  });

  if (bulletPoints && bulletPoints.length > 0) {
    const bulletText = bulletPoints.map((p, i) => ({
      text: p,
      options: { bullet: true, breakLine: i < bulletPoints.length - 1 },
    }));
    slide.addText(bulletText, {
      x: 4.8, y: 2.7, w: 4.4, h: 2.2,
      fontSize: 12, fontFace: "Microsoft YaHei",
      color: COLORS.textSecondary,
    });
  }
  return slide;
}

// Card D: Vision List (horizontal banner cards)
function addVisionListSlide(pres, title, items) {
  let slide = pres.addSlide();
  slide.background = { color: COLORS.bgLight };

  slide.addText(title, {
    x: 0.5, y: 0.4, w: 9, h: 0.6,
    fontSize: 28, fontFace: "Microsoft YaHei",
    color: COLORS.textPrimary, bold: true,
  });

  const itemH = 0.85;
  const gap = 0.2;
  const startY = 1.2;

  items.forEach((item, i) => {
    const y = startY + i * (itemH + gap);
    slide.addShape(pres.shapes.ROUNDED_RECTANGLE, {
      x: 0.5, y, w: 9, h: itemH,
      fill: { color: COLORS.bgWhite },
      rectRadius: 0.1,
      shadow: makeShadow(),
    });

    slide.addText(item, {
      x: 0.8, y, w: 8.4, h: itemH,
      fontSize: 14, fontFace: "Microsoft YaHei",
      color: COLORS.textPrimary, valign: "middle",
    });
  });
  return slide;
}

// Chapter Cover (dark style)
function addChapterCover(pres, chapterNum, chapterTitle) {
  let slide = pres.addSlide();
  slide.background = { color: "1D1D1F" };

  slide.addText(chapterNum, {
    x: 0.5, y: 1.8, w: 9, h: 0.8,
    fontSize: 18, fontFace: "Microsoft YaHei",
    color: COLORS.accent,
  });

  slide.addText(chapterTitle, {
    x: 0.5, y: 2.5, w: 9, h: 1.2,
    fontSize: 40, fontFace: "Microsoft YaHei",
    color: COLORS.bgWhite, bold: true,
  });
  return slide;
}

// ============ Create Presentation ============
let pres = new pptxgen();
pres.layout = "LAYOUT_16x9";
pres.title = "Harness Engineering 工程实践";
pres.author = "Agent Developer";

// ===== Page 1: Cover =====
addCoverSlide(pres,
  "Harness Engineering",
  "打不过 Agent 那就加入"
);

// ===== Page 2: TOC =====
let tocSlide = pres.addSlide();
tocSlide.background = { color: COLORS.bgLight };
tocSlide.addText("目录", {
  x: 0.5, y: 0.4, w: 9, h: 0.6,
  fontSize: 28, fontFace: "Microsoft YaHei",
  color: COLORS.textPrimary, bold: true,
});

const chapters = [
  "第一章：在开发中引入 Agent 的便利",
  "第二章：Skill —— Agent 的「数字器官」",
  "第三章：从 Vibe Coding 到 Harness Engineering",
  "第四章：Agent 冲击下我们何去何从？",
];
chapters.forEach((ch, i) => {
  const y = 1.3 + i * 1.0;
  tocSlide.addShape(pres.shapes.ROUNDED_RECTANGLE, {
    x: 0.5, y, w: 9, h: 0.8,
    fill: { color: COLORS.bgWhite },
    rectRadius: 0.1,
    shadow: makeShadow(),
  });
  tocSlide.addText(ch, {
    x: 0.8, y, w: 8.4, h: 0.8,
    fontSize: 16, fontFace: "Microsoft YaHei",
    color: COLORS.textPrimary, valign: "middle",
  });
});

// ===== Chapter 1 Cover =====
addChapterCover(pres, "第一章", "在开发中引入 Agent 的便利");

// ===== Page 4: Pre-Agent Era =====
addFocusSlide(pres,
  "前 Agent 时代的 AI Coding",
  "传统对话框式网页大语言模型的局限：",
  [
    "模型的输出需要你自行搬运到项目中，coding实际上还是人进行的",
    "模型看不到你的本地项目，无法处理抽象宏大的任务",
    "不论用多么精确的提示词，永远无法让 AI 发挥出应有的实力",
  ]
);

// ===== Page 5: Claude Code Part 1 =====
addVisionListSlide(pres, "Claude Code 开发模式（一）", [
  "Agent 帮助 brainstorm，将模糊需求转化为：",
  "  · 需求分析 & 技术选型",
  "  · 开发流程 & 验收标准",
  "  · 风险控制",
]);

// ===== Page 6: Claude Code Part 2 =====
addVisionListSlide(pres, "Claude Code 开发模式（二）", [
  "三步走战略：规划 → 执行 → 审查 → 再规划",
  "Agent 自己思考 → 自己执行 → 自己反思",
  "直到符合规范，或风险降低到可控范围",
  "先 plan 出具体规划，再 execute，最后 qa 查找错误并修复",
]);

// ===== Page 7: Claude Code Part 3 =====
addVisionListSlide(pres, "Claude Code 开发模式（三）", [
  "前面是 Agent 干的活，那我呢？",
  "写规范的提示词（Specification）文档",
  "调用 skill 让 Agent 以不同身份干不同的事",
  "方案的决定 & 否决 & 交由 Agent 自行裁决，全部由我管辖",
  "身份转变：由「打工人」变成「老板」",
  "时间花销：100分钟手动coding → 10分钟写提示词 + 10分钟等待 + 10分钟纠错",
]);

// ===== Page 8: Agent VS Dialog Box =====
addDashboardSlide(pres, "Agent VS 对话框", [
  { stat: "MCP", label: "通过标准化接口协议，Agent获得直接读写本地文件的能力（感知能力）" },
  { stat: "👁", label: "在获取了眼之后，Agent得以窥见项目全貌" },
  { stat: "✋", label: "获得手之后，Agent能够真正意义上自己 Coding" },
]);

// ===== Chapter 2 Cover =====
addChapterCover(pres, "第二章", "Skill —— Agent 的「数字器官」");

// ===== Page 10: What is Skill =====
addFocusSlide(pres,
  "Skill 究竟是什么？",
  "Skill 本质：",
  [
    "可以是一个文档，也可以是包含文档&工具的文件夹",
    "只要是能让 Agent 读取以达成某些效果的都属于 skill",
    "类比函数/方法：你可以只知道它的作用而不知道实现",
    "Skill 是规范和约束，约束 Agent 应该做什么、怎么做、绝对不能做什么",
  ]
);

// ===== Page 11: What can Skill do =====
addVisionListSlide(pres, "Skill 能做什么？", [
  "一句话：什么都能干，只要一件事能用代码实现，就能封装成 skill",
  "写 LaTeX、画 draw.io 框图、做 PPT……",
  "我这个 PPT 就是用 /example-skills:pptx 做的",
]);

// ===== Page 12: Harness Engineering Intro =====
addFocusSlide(pres,
  "Harness Engineering：基于 Skill 的 Agent 开发新范式",
  "核心概念：",
  [
    "Skill 是规范，Harness（鞍具）是由规范组成的框架",
    "Harness Engineering（驾驭工程）：为 Agent 构建无法犯错、必须按正确步骤、可溯源可控制的开发环境",
    "在 Harness Engineering 中：Agent = Harness + Model",
    "Claude Code 是 harness，skill 是 harness，开发者也是 harness",
    "核心思路：不是提升 model 本身性能，而是更精确高效地发挥 model 性能",
  ]
);

// ===== Page 13: Latest Practice =====
addVisionListSlide(pres, "Harness Engineering：当前最新实践方案", [
  "superpowers & gstack：经过实际检验的两个 skill 包",
  "superpowers：硬件级 TDD，规定 Agent 必须执行单元测试，结果必须严格验收",
  "gstack：定义若干不同身份的 skill，在规划→质疑循环中审视项目",
  "标准实践流程：/plan-ceo-review → /brainstorm → /plan-eng-review → /review → /execute-plan → /qa",
  "两个 skill 包恰好互补，构建了当前最主流的 Harness Engineering 工程开发流程",
]);

// ===== Page 14: Subagent Advantage =====
addVisionListSlide(pres, "Harness Engineering 最大优势：Subagent & Agent Teams", [
  "Subagent：子 Agent，由主 Agent 主动调用，只给必要且干净的上下文",
  "Subagent 能以极高专注度、低上下文干扰完成任务",
  "Agent Teams：若干 Agent 围绕同一任务链，自动分配角色与任务，并发运行",
  "两种模式可交叉融合，构筑反馈回环",
  "开发者能够定制化属于自己的 Harness",
]);

// ===== Chapter 3 Cover =====
addChapterCover(pres, "第三章", "从 Vibe Coding 到 Harness Engineering");

// ===== Page 16: Vibe Coding Era =====
addFocusSlide(pres,
  "Vibe Coding：想法驱动开发（2022-2023）",
  "特点：",
  [
    "直接告诉模型自己想干什么，不用文档也不用规范",
    "感觉对了就继续，感觉错了就改正",
    "缺点：没有任何书面规范，错误无法溯源，进度无法预测，风险无法管控",
  ]
);

// ===== Page 17: Context Rot & Drift =====
addFocusSlide(pres,
  "Context Rot & Context Drift",
  "隐藏的问题：",
  [
    "一次会话内上下文太长，导致前面的上下文信息被压缩",
    "模型错误记忆和幻觉",
    "第一轮说的话，模型第五轮记错，第十轮早已忘记",
    "如果在第七轮写了有偏差的方法，第十二轮才发现早已为时已晚",
  ]
);

// ===== Page 18: Spec Coding =====
addFocusSlide(pres,
  "Spec Coding：规范驱动开发（2023-2025）",
  "改进方案：",
  [
    "将严谨定义的提示词本地持久化",
    "让 AI Coding 有了足以支持工程开发的规范标准",
    "由想法驱动向设计驱动转变",
    "重要信息本地化，极大避免信息被压缩",
  ]
);

// ===== Page 19: Harness Engineering =====
addFocusSlide(pres,
  "Harness Engineering：AI Coding 从随性到可信（2025——）",
  "Harness Engineering 的突破：",
  [
    "在 Spec Coding 基础上嵌套框架，牵制 Agent 必须按规划执行",
    "通过自检反馈循环保障生成的代码一定可用",
    "整个过程均在开发者控制范围内",
    "Agent 的失误本质上就是开发者的失误",
  ]
);

// ===== Chapter 4 Cover =====
addChapterCover(pres, "第四章", "Agent 冲击下我们何去何从？");

// ===== Page 21: Agent Impact =====
addVisionListSlide(pres, "Agent 正在瓦解传统组织架构", [
  "熟练的 Agent 开发者调用 Agent 开发效率远大于传统开发者",
  "当前互联网正在快速取缔传统开发者",
  "证据：各家大厂都在加紧布局 Agent 基础设施",
  "面试内容更加偏重 Agent，倾向于选择熟练使用 Agent 的求职者",
  "案例：今年年初多家大厂优化前端部门，新型 Agent 初创崛起",
]);

// ===== Page 22: What should developers do =====
addVisionListSlide(pres, "普通开发者怎么办？", [
  "认清形势，放弃幻想。Agent 可能会崩盘，但起码不是近几年",
  "趁着技术仍在探索的窗口期顺势跟上",
  "转变观念，尝试以管理者视角思考",
  "在 Agent 时代，企业会更加期望招募能够管理一组 Agent 的高级架构师",
]);

// ===== Page 23: Ending =====
let endSlide = pres.addSlide();
endSlide.background = { color: "1D1D1F" };

endSlide.addShape(pres.shapes.ROUNDED_RECTANGLE, {
  x: 1.5, y: 1.5, w: 7, h: 2.8,
  fill: { color: COLORS.bgWhite },
  rectRadius: 0.15,
  shadow: makeShadow(),
});

endSlide.addText("打不过 Agent？那就加入。", {
  x: 1.5, y: 1.8, w: 7, h: 1.2,
  fontSize: 32, fontFace: "Microsoft YaHei",
  color: COLORS.textPrimary, bold: true, align: "center", valign: "middle",
});

endSlide.addText("从 Vibe Coding 到 Spec Coding，再到 Harness Engineering\nAI Coding 正在从随性走向可信，从个人走向组织\n拥抱 Harness Engineering，成为驾驭 Agent 的「驾驭者」", {
  x: 1.5, y: 3.1, w: 7, h: 1.0,
  fontSize: 14, fontFace: "Microsoft YaHei",
  color: COLORS.textSecondary, align: "center", valign: "middle",
});

// ===== Save =====
pres.writeFile({ fileName: "scripts/pptx/ppt01/PPT01.pptx" })
  .then(() => console.log("PPT created: scripts/pptx/ppt01/PPT01.pptx"))
  .catch(err => console.error("Error:", err));