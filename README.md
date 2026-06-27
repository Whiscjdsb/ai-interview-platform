# AI Interview Platform

AI Interview Platform 是一个面向 Java 后端学习与面试训练的全栈项目，包含题库刷题、自动判题、收藏错题、学习统计、AI 面试助手、PDF 报告、公开分享、成长分析和管理员后台等能力。

项目适合作为 GitHub 展示、简历项目和面试讲解材料。后端以 Spring Boot 3 为核心，前端使用 Vue 3 + TypeScript，AI 能力支持 Mock 与 DeepSeek 双 Provider，并通过统一 `ScoreEngine` 保证评分入口一致。

## 项目定位

本项目不是简单 CRUD，而是一个“AI 面试训练平台”原型：

- 面向学生和求职者：提供题库练习、错题复盘、AI 模拟面试和能力成长分析。
- 面向管理员：提供用户、题库、答题记录、收藏记录、错题记录、AI 面试记录和统计看板。
- 面向面试展示：体现后端分层、权限控制、AI 服务抽象、评分工程化、前后端联调和可观测性。

## 核心功能

用户端：

- 用户注册、登录、JWT 鉴权、当前用户信息
- 题库列表、题目详情、刷题提交
- 单选、多选、判断题自动判题
- 答题历史、收藏夹、错题本
- 用户学习概览、趋势、标签正确率、错题分析
- AI 简答题点评
- AI 模拟面试创建、会话、提交、结果展示
- AI 多轮追问
- AI 面试 PDF 报告导出
- AI 面试报告公开分享
- 用户 AI 面试成长分析

管理员端：

- 管理员后台首页统计
- 用户管理、禁用用户、角色管理
- 题库管理、题目新增、编辑、删除
- 收藏记录、错题记录、答题记录管理
- AI 面试记录管理与详情查看
- 平台数据概览、趋势、热门题目、热门标签、用户排行

AI 与企业面试原型：

- Mock / DeepSeek Provider 切换
- 企业面试官风格原型
- 企业面试模板原型
- 企业评分维度原型
- 岗位匹配分析原型

## 技术栈

后端：

- Java 17
- Spring Boot 3
- Spring Security
- JWT
- Maven
- MyBatis-Plus
- MySQL 8
- Redis 7
- Lombok
- SpringDoc OpenAPI
- Docker Compose
- DeepSeek API
- OpenPDF

前端：

- Vue 3
- TypeScript
- Vite
- Element Plus
- Vue Router
- Pinia
- Axios
- ECharts
- npm

测试与工程：

- Maven Wrapper
- JUnit 5
- Spring Boot Test
- H2 test profile
- vue-tsc
- Vite production build

## 系统亮点

- 前后端完整闭环：认证、题库、答题、统计、AI 面试、管理后台均已打通。
- AI Provider 抽象：通过 `AiServiceFactory` 支持 Mock、DeepSeek，并预留 OpenAI 扩展。
- 统一评分引擎：通过 `ScoreEngine` 统一输出最终分数，避免评分逻辑散落。
- 低质量回答防护：对“我不会”“不知道”“嗯”“对”等无效回答进行严格封顶。
- 多轮 AI 追问：支持“用户回答 -> AI 追问 -> 用户继续回答”的模拟面试流程。
- 报告能力：支持 AI 面试结果 PDF 导出和公开分享链接。
- 成长分析：基于历史面试记录形成用户能力成长画像。
- 管理后台：提供平台运营视角的数据和记录管理。
- 可观测性：AI Provider 和评分链路均有关键日志，方便排查是否真实调用 DeepSeek。

## AI Provider 架构说明

AI 服务通过统一接口 `AiService` 抽象：

```text
AiService
├── MockAiService
├── DeepSeekAiService
└── OpenAiService 预留
```

`AiServiceFactory` 负责选择当前 Provider。解析优先级：

1. JVM 或配置属性 `app.ai.provider`
2. 环境变量 `APP_AI_PROVIDER`
3. 环境变量 `AI_PROVIDER`
4. 全部为空时 fallback 为 `mock`

启动时会打印：

```text
System final AI Provider = deepseek, source=...
```

调用时会打印：

```text
AI provider configured=deepseek, selectedService=DeepSeekAiService, modelName=deepseek-chat
DeepSeek request sending: url=https://api.deepseek.com/chat/completions
DeepSeek response received: status=200
```

## DeepSeek / Mock 切换说明

使用 Mock：

```powershell
$env:APP_AI_PROVIDER="mock"
```

使用 DeepSeek：

```powershell
$env:APP_AI_PROVIDER="deepseek"
$env:DEEPSEEK_API_KEY="your_deepseek_api_key"
```

IDEA 推荐配置：

- `Run/Debug Configurations -> Environment variables`
- 填入：

```text
APP_AI_PROVIDER=deepseek;DEEPSEEK_API_KEY=your_deepseek_api_key
```

也可以使用 VM options：

```text
-Dapp.ai.provider=deepseek
```

常见问题：

- 如果日志显示 `System final AI Provider = mock`，说明 IDEA 没有读取到 `APP_AI_PROVIDER` 或 `app.ai.provider`。
- 如果选择了 `deepseek` 但没有配置 `DEEPSEEK_API_KEY`，系统会记录原因并 fallback 到 `MockAiService`。
- 如果在系统环境变量中新增变量后 IDEA 仍不生效，请重启 IDEA，或者直接写入 Run Configuration。

## ScoreEngine 统一评分引擎

评分逻辑统一收口到 `ScoreEngine.calculateScore(AiScoreContext context)`。

输入上下文：

- `deepseekScore`
- `userAnswer`
- `questionType`
- `isFallback`
- `referencePoints`

统一输出：

- 单题 AI 点评 `score`
- `structuredResult.score`
- 整场面试 `questionResults.score`
- 整场面试 `totalScore`

评分流转：

```text
DeepSeek / Mock / 本地面试评分
        ↓ 生成原始分
AiScoreContext
        ↓
ScoreEngine.calculateScore(...)
        ↓
最终 score
        ↓
PDF / 分享 / 成长分析继续读取最终分数
```

每次评分会输出：

```text
traceId, inputScore, finalScore, correctionReason
```

## 多轮 AI 追问

多轮追问接口用于模拟真实面试官连续追问：

```text
POST /api/ai/interview/next-question
```

流程：

```text
当前题目 + 用户回答 + 历史对话
        ↓
DeepSeek 或 Mock 追问生成
        ↓
返回 nextQuestion 和 reason
```

Prompt 限制：

- 每次只问一个问题
- 不重复历史问题
- 基于候选人的回答逐步深入
- 保持严格 Java 面试官角色

## PDF 导出 / 分享 / 成长分析

PDF 导出：

```text
GET /api/ai/interview/{id}/export-pdf
```

包含总分、优点、不足、建议、参考答案、每题结果和追问记录。

分享功能：

```text
POST /api/ai/interview/{id}/share
GET /api/share/{token}
```

生成公开只读报告链接，不需要登录，不暴露敏感用户信息。

成长分析：

```text
GET /api/ai/user/growth
```

基于历史 AI 面试记录输出平均分、趋势和能力维度。

## 企业面试原型

项目已预留企业级 AI 面试 SaaS 原型能力：

- 多企业面试官类型：阿里、腾讯、字节、创业公司风格
- 企业面试模板
- 多维评分模型
- 用户与岗位模型匹配分析
- 前端企业面试入口

该部分用于展示系统扩展方向，不影响普通 AI 面试主流程。

## 项目运行方式

启动 MySQL 和 Redis：

```powershell
docker compose up -d mysql redis
```

后端环境变量示例：

```powershell
$env:SERVER_PORT="8080"
$env:MYSQL_URL="jdbc:mysql://localhost:3306/ai_interview_platform?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true"
$env:MYSQL_USERNAME="root"
$env:MYSQL_PASSWORD="root"
$env:REDIS_HOST="localhost"
$env:REDIS_PORT="6379"
$env:JWT_SECRET="replace-with-a-long-random-secret"
$env:APP_AI_PROVIDER="deepseek"
$env:DEEPSEEK_API_KEY="your_deepseek_api_key"
```

启动后端：

```powershell
.\mvnw.cmd spring-boot:run
```

启动前端：

```powershell
cd frontend
npm install
npm run dev
```

默认访问：

- 后端 API: `http://localhost:8080`
- Swagger: `http://localhost:8080/swagger-ui.html`
- 前端: `http://localhost:5173` 或 `http://localhost:5174`

默认演示账号由 `sql/test-data.sql` 提供，密码在数据库中保存为 BCrypt 密文：

- 普通用户：`demo_user / admin123`
- 管理员：`admin / admin123`

## 编译、测试与构建

后端编译：

```powershell
.\mvnw.cmd -DskipTests compile
```

后端测试：

```powershell
.\mvnw.cmd test
```

前端构建：

```powershell
cd frontend
npm.cmd run build
```

当前已知验证结果见：

- [测试报告](docs/TEST_REPORT.md)
- [交付说明](docs/DELIVERY.md)

## 常见问题

### 为什么显示 MOCK？

通常是因为没有配置 `APP_AI_PROVIDER=deepseek`，或者 IDEA 没有读取到环境变量。请检查启动日志：

```text
System final AI Provider = ...
```

### 为什么需要 APP_AI_PROVIDER？

`APP_AI_PROVIDER` 是为了让 IDEA、PowerShell、Docker 等环境都能清晰配置 AI Provider。系统也兼容 `AI_PROVIDER` 和 `-Dapp.ai.provider=deepseek`。

### DeepSeek API Key 配了但没有调用？

请同时确认：

- `APP_AI_PROVIDER=deepseek`
- `DEEPSEEK_API_KEY` 不为空
- 日志出现 `selectedService=DeepSeekAiService`
- 日志出现 `DeepSeek request sending`

### 为什么低质量回答分数很低？

项目通过 `ScoreEngine` 对空回答、明显无效回答、极短回答、泛泛回答做封顶，防止 AI 模型给出安慰式高分。

### PDF / 分享 / 成长分析的分数来自哪里？

这些模块读取的是最终保存的 AI 面试结果。最终分数统一由 `ScoreEngine` 产出。

## 文档入口

- [项目交付说明](docs/DELIVERY.md)
- [演示脚本](docs/DEMO_SCRIPT.md)
- [演示准备清单](docs/DEMO_READY.md)
- [部署说明](docs/DEPLOYMENT_GUIDE.md)
- [API 概览](docs/API_OVERVIEW.md)
- [测试报告](docs/TEST_REPORT.md)
- [项目总结](docs/PROJECT_SUMMARY.md)
- [面试讲解稿](docs/INTERVIEW_SCRIPT.md)
- [面试高频问答](docs/INTERVIEW_QA.md)
- [简历描述模板](docs/RESUME_DESCRIPTION.md)
