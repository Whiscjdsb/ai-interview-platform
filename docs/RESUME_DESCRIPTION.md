# Resume Description

## 简历项目描述短版

AI 面试训练平台：基于 Spring Boot 3 + Vue 3 开发的全栈项目，支持题库刷题、自动判题、收藏错题、学习统计、AI 模拟面试、DeepSeek 接入、PDF 报告导出、公开分享、成长分析和管理员后台。通过 `AiServiceFactory` 实现 Mock / DeepSeek Provider 切换，通过 `ScoreEngine` 统一 AI 评分入口，提升评分稳定性和系统可维护性。

## 简历项目描述详细版

AI Interview Platform 是一个面向 Java 后端求职者的 AI 面试训练平台。项目包含用户端、AI 面试助手和管理员后台三大模块。用户可以完成题库练习、提交答案、查看错题本、收藏题目、查看学习统计，并使用 AI 模拟面试完成多轮追问、结果评分、PDF 报告导出和公开分享。管理员可以维护用户、题库、答题记录、错题记录、收藏记录和 AI 面试记录，并查看平台统计看板。

后端基于 Spring Boot 3、Spring Security、JWT、MyBatis-Plus、MySQL、Redis 构建，前端基于 Vue 3、TypeScript、Element Plus、Pinia、Vue Router、Axios、ECharts 构建。AI 模块通过 `AiServiceFactory` 抽象 Provider，支持 Mock 与 DeepSeek 切换；评分模块通过 `ScoreEngine` 统一处理 DeepSeek 原始分、fallback 分和低质量回答封顶，保证单题点评、整场面试、PDF、分享和成长分析中的分数一致。

## 技术栈写法

后端：

```text
Java 17, Spring Boot 3, Spring Security, JWT, MyBatis-Plus, MySQL, Redis, Maven, SpringDoc OpenAPI, OpenPDF, DeepSeek API
```

前端：

```text
Vue 3, TypeScript, Vite, Element Plus, Pinia, Vue Router, Axios, ECharts
```

测试与工程：

```text
JUnit 5, Spring Boot Test, H2, Maven Wrapper, Docker Compose, vue-tsc, Vite Build
```

## 项目职责写法

可以按以下方式写入简历：

- 负责后端核心模块设计与实现，包括用户认证、题库、答题记录、收藏、错题本、学习统计和管理员后台。
- 负责 Spring Security + JWT 鉴权体系，完成用户和管理员权限隔离。
- 负责 AI 模块架构设计，抽象 `AiService` 和 `AiServiceFactory`，实现 Mock 与 DeepSeek Provider 切换。
- 负责 `ScoreEngine` 统一评分引擎设计，解决 AI 评分逻辑分散和低质量回答高分问题。
- 负责 AI 面试结果 PDF 导出、公开分享、成长分析等能力。
- 负责 Vue 3 前端页面开发，包括用户端题库、刷题、AI 面试、成长分析和管理员后台。
- 负责接口联调、测试补充、构建回归和项目文档整理。

## 项目亮点写法

可以选 3 到 5 条放在简历中：

- 设计 `AiServiceFactory` 抽象 AI Provider，支持 Mock / DeepSeek 动态切换，并在 API Key 缺失时自动 fallback，保证系统可运行性。
- 设计 `ScoreEngine` 统一评分引擎，对 AI 原始分进行二次校验，统一产出单题点评、整场面试、PDF、分享和成长分析使用的最终分数。
- 针对“我不会”“不知道”“嗯”“对”等低质量回答设计封顶规则，避免大模型安慰式高分，提升评分稳定性。
- 实现 AI 多轮追问，通过当前题目、用户回答和历史对话生成逐步深入的问题，增强模拟面试真实感。
- 实现 AI 面试 PDF 导出和公开分享链接，使面试结果具备交付和传播能力。
- 实现学习统计和成长分析，基于答题与 AI 面试记录生成学习趋势、能力雷达和薄弱方向。
- 实现管理员后台统计看板和记录管理，支持平台运营视角的数据查看。

## 面试中可以重点讲的 5 个亮点

### 1. AI Provider 可插拔

讲点：

- 业务层只依赖 `AiService` 接口。
- `AiServiceFactory` 负责根据配置选择 Mock 或 DeepSeek。
- 没有 API Key 时 fallback 到 Mock，保证项目可演示、可测试。

### 2. ScoreEngine 统一评分

讲点：

- 原来评分逻辑分散，维护成本高。
- 重构后所有最终分数必须经过 `ScoreEngine`。
- 通过上下文对象 `AiScoreContext` 承载原始分、回答、题型、fallback 状态和参考要点。

### 3. AI 评分稳定性

讲点：

- Prompt 要求严格评分。
- 后端不完全信任模型分数。
- 对空回答、无效回答、泛泛回答进行封顶。
- 日志记录 traceId、inputScore、finalScore、correctionReason。

### 4. 完整业务闭环

讲点：

- 从题库刷题到错题本、学习统计、AI 面试、报告导出、分享、成长分析形成闭环。
- 管理端可维护用户、题库和记录。

### 5. 工程化和可展示性

讲点：

- 有 Maven Wrapper、Docker Compose、测试、Swagger、README 和 docs。
- 支持本地 Mock 演示，也支持真实 DeepSeek 调用。
- 前后端构建通过，适合 GitHub 和答辩展示。

## 简历条目示例

```text
AI 面试训练平台 | Spring Boot 3, Vue 3, DeepSeek API, MySQL, Redis

- 设计并实现题库刷题、自动判题、收藏错题、学习统计、AI 模拟面试、PDF 报告和管理员后台等模块。
- 抽象 AiService 与 AiServiceFactory，实现 Mock / DeepSeek Provider 动态切换和 API Key 缺失 fallback。
- 设计 ScoreEngine 统一评分引擎，对 AI 原始分进行质量校验和低质量回答封顶，保证单题点评、整场面试、PDF、分享和成长分析分数一致。
- 使用 Spring Security + JWT 实现用户与管理员权限隔离，使用 Redis 缓存统计结果，使用 ECharts 展示学习趋势和能力雷达。
```

