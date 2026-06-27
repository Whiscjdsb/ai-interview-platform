# Interview Script

## 1 分钟项目介绍

这个项目是一个 AI 面试训练平台，后端使用 Spring Boot 3，前端使用 Vue 3 + TypeScript。它不只是普通题库系统，还覆盖了用户刷题、自动判题、收藏错题、学习统计、AI 模拟面试、AI 多轮追问、PDF 报告导出、公开分享、成长分析和管理员后台。

我重点做了三类设计：第一是 Spring Security + JWT 的权限体系；第二是 AI Provider 抽象，支持 Mock 和 DeepSeek 切换；第三是 `ScoreEngine` 统一评分引擎，用来解决 AI 评分不稳定和评分逻辑分散的问题。这个项目可以展示完整的全栈工程能力、AI 接入能力和后端架构设计能力。

## 3 分钟项目讲解

项目整体分为用户端、AI 面试模块和管理员端。

用户端支持注册登录、题库列表、题目详情、刷题提交、自动判题、收藏、错题本、答题历史和学习统计。客观题可以自动判题，主观题进入 AI 点评流程。

AI 面试模块是项目的核心亮点。用户可以创建模拟面试，系统生成面试题，用户提交整场答案后，AI 会给出总分、每题评分、优点、不足、建议和参考答案。为了接近真实面试，我还做了多轮追问接口，让系统可以基于用户回答继续追问。面试完成后可以导出 PDF 报告，也可以生成公开分享链接。

后端 AI 能力通过 `AiService` 接口抽象，`AiServiceFactory` 根据配置选择 `MockAiService` 或 `DeepSeekAiService`。这样在没有 API Key 的环境下项目仍然可以运行，在配置 DeepSeek 后可以真实调用大模型。

评分方面，我做了 `ScoreEngine` 统一评分引擎。之前评分逻辑分散在多个 Service 中，容易出现“我不会”也给高分的问题。现在所有最终分数都统一经过 `ScoreEngine`，它会根据原始分、用户回答、题型和 fallback 状态进行质量检测，对空回答、无效回答、泛泛回答做封顶，同时记录 traceId、输入分、最终分和修正原因。

管理员端用于平台管理，包括用户管理、题库管理、答题记录、错题记录、收藏记录、AI 面试记录和统计看板。整体项目体现了从业务建模、权限控制、AI 接入到前后端联调的完整闭环。

## 5 分钟项目讲解

我这个项目的定位是 AI 面试训练平台，目标是帮助 Java 后端求职者完成“刷题、复盘、模拟面试、生成报告、长期成长分析”的完整流程。

架构上，后端采用 Spring Boot 3 分层架构，包含 Controller、Service、Mapper、Entity、DTO、VO。数据层使用 MyBatis-Plus 和 MySQL，缓存使用 Redis，安全层使用 Spring Security + JWT。前端使用 Vue 3 + TypeScript + Element Plus，结合 Pinia、Vue Router、Axios 和 ECharts。

用户端的基础流程是：用户登录后进入题库，筛选题目，查看详情，进入刷题页提交答案。单选、多选、判断题会自动判题，答错后进入错题本，答对后可以把错题状态更新为已解决。所有答题记录会进入历史列表，并且每日学习记录会被更新，统计接口可以生成学习概览、趋势和标签正确率。

AI 模块分为几个层次。第一层是 AI 简答题点评，用户提交主观题答案后，系统根据题目和回答生成结构化点评。第二层是 AI 模拟面试，系统生成面试题，用户提交整场答案后得到总分和逐题点评。第三层是多轮追问，系统基于当前题目、用户回答和历史对话生成下一轮追问。第四层是报告能力，包括 PDF 导出、公开分享链接和成长分析。

AI Provider 设计上，我没有把 DeepSeek 调用直接写死在业务里，而是抽象了 `AiService`。`MockAiService` 用于本地演示和测试，`DeepSeekAiService` 用于真实大模型调用，`AiServiceFactory` 根据 `app.ai.provider`、`APP_AI_PROVIDER`、`AI_PROVIDER` 选择实际实现。这样项目在没有 API Key 的环境下也可以跑起来，在生产或演示环境中只要配置 DeepSeek Key 就能走真实模型。

评分系统是我后期重点重构的地方。最开始评分逻辑分散在 Mock、DeepSeek、单题点评和整场面试里，维护成本高，而且会出现大模型安慰式高分的问题。后来我把最终评分统一收口到 `ScoreEngine.calculateScore(AiScoreContext context)`。AI 服务只负责生成原始分，最终分数必须经过 ScoreEngine。它会判断回答质量，如果是空回答最高 5 分，如果是“我不会”“不知道”“嗯”“对”等无效回答最高 15 分，如果是泛泛回答最高 50 分，只有包含技术关键词和解释内容的回答才允许高分。这个设计让 `structuredResult.score`、`questionResults.score`、`totalScore`、PDF 和分享页里的分数保持一致。

管理员端则提供平台管理能力，包括用户、题库、答题、收藏、错题、AI 面试记录和统计看板。管理员接口统一走 `/api/admin/**`，通过 Spring Security 控制 `ADMIN` 权限。

整体来说，这个项目的难点不在某一个 CRUD，而在如何把 AI 能力接入到一个真实业务系统里，并且保证可运行、可回退、可观测、可维护。

## 面试官可能追问的问题与回答

### 为什么要设计 AiServiceFactory？

因为 AI Provider 不应该和业务逻辑强绑定。业务只依赖 `AiService` 接口，具体使用 Mock、DeepSeek 或未来 OpenAI，由 `AiServiceFactory` 根据配置决定。这样可以降低耦合，也方便本地开发、测试和线上切换。

### 为什么需要 Mock fallback？

AI API 依赖外部网络和 API Key，如果没有 fallback，项目在本地、测试、答辩或 CI 环境里很容易不可用。Mock fallback 可以保证系统核心流程始终可运行，同时 DeepSeek 配好后又能走真实模型。

### 为什么要做 ScoreEngine？

因为评分逻辑原本散落在多个类里，容易出现不同入口评分标准不一致的问题。比如单题点评和整场面试可能使用不同规则。`ScoreEngine` 把最终评分统一收口，让所有 score 都从同一个入口产出，便于维护、测试和观测。

### DeepSeek 如何接入？

通过 `DeepSeekAiService` 使用 `RestTemplate` 调用 DeepSeek Chat Completions API。请求中包含 system prompt、user prompt、model 和 temperature。DeepSeek 返回后，从 `choices[0].message.content` 中解析结构化 JSON，再经过 `ScoreEngine` 得到最终分数。

### 如何保证 AI 评分稳定性？

主要通过三层控制：

1. Prompt 中要求严格评分，只输出结构化 JSON。
2. 后端解析后不直接相信模型分数，而是进入 `ScoreEngine` 二次校验。
3. `ScoreEngine` 对空回答、无效回答、极短回答、泛泛回答设置上限。

### 如何处理低质量回答？

`ScoreEngine` 会先清洗用户回答，判断有效字符长度、是否命中无效短语、是否包含技术关键词、是否有解释性内容。比如“我不会”“不知道”“嗯”“对”最高 15 分，空回答最高 5 分，泛泛而谈最高 50 分。

### 为什么 PDF、分享和成长分析分数能保持一致？

因为这些模块不重新打分，而是读取已经保存的 AI 面试结果。保存前的分数已经经过 `ScoreEngine` 统一处理，所以展示层读取到的是一致的最终分。

### 项目最大的难点是什么？

最大难点是 AI 能力工程化。直接调用大模型不难，难的是让它在业务系统里可配置、可降级、可观测、可测试，并且避免 AI 输出不稳定影响用户体验。

### 项目还有哪些可以优化？

可以继续做流式输出、多租户、企业岗位模型库、代码题沙箱、异步报告生成、消息队列、支付订阅和更完整的监控告警。

