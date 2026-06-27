# ai-interview-platform-backend

AI 面试刷题平台后端项目。当前已实现基础工程、用户认证、JWT 鉴权、题库核心模块、管理员题目管理、用户答题、自动判题、学习统计、AI 面试助手 Mock 模块，以及管理员用户管理和数据统计看板接口。

## 当前已完成内容

- Spring Boot 3 + Java 17 + Maven 项目结构
- MySQL 8、Redis 7、MyBatis-Plus、Lombok、SpringDoc OpenAPI 配置
- 用户注册、登录、当前用户信息接口
- 无状态 JWT 鉴权与统一 JSON 错误返回
- 题目、标签、题目标签关系管理
- 匿名题库分页查询、题目详情、标签列表
- 管理员题目和标签增删改
- 用户提交答案、客观题自动判题
- 答题历史、收藏夹、错题本
- 每日学习记录、用户学习概览、趋势、标签正确率和错题分析
- AI 简答题点评、模拟面试题生成、薄弱知识点总结和 AI 历史记录
- 管理员用户分页、用户详情、账号启停、角色维护
- 管理员平台概览、趋势、热门题目、热门标签和用户排行

## 交付文档

- [项目交付说明](docs/DELIVERY.md)
- [演示脚本](docs/DEMO_SCRIPT.md)
- [API 概览](docs/API_OVERVIEW.md)
- [测试与构建报告](docs/TEST_REPORT.md)

## 启动依赖

```bash
docker compose up -d mysql redis
```

首次启动 MySQL 容器时会自动执行 `sql/init.sql`。如需导入测试数据，可手动执行 `sql/test-data.sql`。

## 环境变量

```bash
SERVER_PORT=8080
MYSQL_URL=jdbc:mysql://localhost:3306/ai_interview_platform?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
MYSQL_USERNAME=root
MYSQL_PASSWORD=root
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=
REDIS_DATABASE=0
JWT_SECRET=replace-with-a-long-random-secret
JWT_EXPIRATION_SECONDS=86400
APP_AI_PROVIDER=deepseek
# Optional compatibility alias. Use either APP_AI_PROVIDER or AI_PROVIDER.
AI_PROVIDER=
AI_API_KEY=
DEEPSEEK_API_KEY=replace-with-your-deepseek-key
OPENAI_API_KEY=
```

AI provider resolution:

1. `app.ai.provider` from JVM/system/application properties has highest priority.
2. `APP_AI_PROVIDER` is supported for IDEA Run Configuration environment variables.
3. `AI_PROVIDER` is also supported for compatibility.
4. If all provider configs are blank, the backend falls back to `mock`.
5. When `deepseek` is selected but `DEEPSEEK_API_KEY` is blank, the backend logs the reason and falls back to `MockAiService`.

IDEA startup note:

- In `Run/Debug Configurations -> Environment variables`, set `APP_AI_PROVIDER=deepseek;DEEPSEEK_API_KEY=your_key`.
- Or set VM options: `-Dapp.ai.provider=deepseek`.
- Do not rely on shell-only variables if IDEA was opened before the variables were created; restart IDEA or set them directly in the Run Configuration.
- Startup logs should contain `System final AI Provider = deepseek` and request logs should show `selectedService=DeepSeekAiService`.

## IDEA 启动步骤

1. 使用 IDEA 打开项目根目录。
2. Project SDK 选择 Java 17。
3. 等待 Maven 依赖导入完成。
4. 启动 MySQL 和 Redis。
5. 运行 `com.example.aiinterview.AiInterviewApplication`。

## 认证接口

```text
POST /api/auth/register
POST /api/auth/login
GET /api/auth/current
```

## 题库接口

匿名可访问：

```text
GET /api/questions
GET /api/questions/{id}
GET /api/tags
```

管理员接口：

```text
GET /api/admin/questions/{id}
POST /api/admin/questions
PUT /api/admin/questions/{id}
DELETE /api/admin/questions/{id}
POST /api/admin/tags
PUT /api/admin/tags/{id}
DELETE /api/admin/tags/{id}
```

测试管理员账号由 `sql/test-data.sql` 提供：

```text
username: admin
password: admin123
role: ADMIN
```

## 答题、收藏、错题本接口

以下接口均需要登录后携带 `Authorization: Bearer <token>`。

```text
POST /api/answers/submit
GET /api/answers/history
GET /api/answers/history/{id}

POST /api/favorites/{questionId}
DELETE /api/favorites/{questionId}
GET /api/favorites

GET /api/wrong-questions
DELETE /api/wrong-questions/{questionId}
```

提交答案示例：

```bash
curl -X POST http://localhost:8080/api/answers/submit \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d "{\"questionId\":1,\"userAnswer\":\"A\",\"answerDuration\":45}"
```

多选题答案会忽略顺序和空格，例如 `A,B,C` 与 `C, A, B` 会判定一致。`SHORT_ANSWER` 和 `CODING` 暂不自动判题，会返回等待 AI 点评或人工评估的提示。

收藏题目示例：

```bash
curl -X POST http://localhost:8080/api/favorites/1 \
  -H "Authorization: Bearer <token>"
```

查看错题本示例：

```bash
curl "http://localhost:8080/api/wrong-questions?page=1&size=10&status=ACTIVE" \
  -H "Authorization: Bearer <token>"
```

## 学习统计接口

以下接口均需要登录后携带 `Authorization: Bearer <token>`。

```text
GET /api/statistics/user/overview
GET /api/statistics/user/trend
GET /api/statistics/user/tag-accuracy
GET /api/statistics/user/wrong-analysis
```

学习趋势示例：

```bash
curl "http://localhost:8080/api/statistics/user/trend?days=7" \
  -H "Authorization: Bearer <token>"
```

`/api/statistics/user/overview` 使用 Redis 缓存 10 分钟，缓存 key 格式为 `ai-interview:statistics:user:{userId}:overview`。用户提交答案、收藏题目、取消收藏、删除错题时会自动删除对应用户的概览缓存。

## AI 面试助手接口

以下接口均需要登录后携带 `Authorization: Bearer <token>`。当前默认使用 `MockAiService`，不会调用真实外部大模型接口。

```text
POST /api/ai/review-answer
POST /api/ai/generate-interview
POST /api/ai/weakness-summary
GET /api/ai/history
GET /api/ai/history/{id}
DELETE /api/ai/history/{id}
```

简答题点评示例：

```bash
curl -X POST http://localhost:8080/api/ai/review-answer \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d "{\"questionId\":2,\"answer\":\"Spring Boot 自动配置依赖条件注解、Bean 定义和 classpath 匹配来减少样板配置。\"}"
```

模拟面试题生成示例：

```bash
curl -X POST http://localhost:8080/api/ai/generate-interview \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d "{\"position\":\"Java Backend Engineer\",\"difficulty\":\"MEDIUM\",\"focusTags\":[\"Java\",\"Spring Boot\",\"MySQL\",\"Redis\"],\"questionCount\":5}"
```

Mock 点评规则会综合答案长度、关键术语命中、题目标签相关性生成 0-100 分、优点、改进建议和参考组织方式。后续如需接入 DeepSeek 或 OpenAI，可通过 `AI_PROVIDER`、`DEEPSEEK_API_KEY`、`OPENAI_API_KEY` 配置扩展，当前不会发起真实外部调用。

## 管理员用户管理接口

以下接口均需要管理员登录后携带 `Authorization: Bearer <admin-token>`。

```text
GET /api/admin/users
GET /api/admin/users/{id}
PUT /api/admin/users/{id}/status
PUT /api/admin/users/{id}/roles
```

禁用用户示例：

```bash
curl -X PUT http://localhost:8080/api/admin/users/100/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin-token>" \
  -d "{\"status\":0}"
```

修改角色示例：

```bash
curl -X PUT http://localhost:8080/api/admin/users/100/roles \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin-token>" \
  -d "{\"roleCodes\":[\"USER\"]}"
```

注意事项：管理员不能禁用自己，不能禁用最后一个 `ADMIN`，也不能移除自己或系统最后一个 `ADMIN` 角色。禁用后用户登录会失败，角色调整会在后续请求中按最新数据库角色生效。

## 管理员统计接口

以下接口均需要管理员登录。

```text
GET /api/admin/statistics/overview
GET /api/admin/statistics/trend
GET /api/admin/statistics/popular-questions
GET /api/admin/statistics/popular-tags
GET /api/admin/statistics/user-ranking
```

管理员统计缓存 5 分钟，缓存 key 包括：

```text
ai-interview:statistics:admin:overview
ai-interview:statistics:admin:trend:{days}
ai-interview:statistics:admin:popular-questions:{limit}
ai-interview:statistics:admin:popular-tags:{limit}
```

用户注册、提交答案、题目或标签变更、AI 点评记录新增、用户状态或角色变更时会清理管理员统计缓存。

## 管理员后台接口

以下接口均需要管理员登录。

```text
GET /api/admin/dashboard
GET /api/admin/users
GET /api/admin/questions
POST /api/admin/questions
PUT /api/admin/questions/{id}
DELETE /api/admin/questions/{id}
GET /api/admin/favorites
GET /api/admin/wrong-questions
GET /api/admin/answers
GET /api/admin/ai-interviews
GET /api/admin/ai-interviews/{id}
```

## 质量验收命令

环境要求：

```text
JDK 17+
Node.js 18+
```

项目已补齐 Maven Wrapper，首次运行会自动下载 Maven 3.9.9。

后端编译：

```bash
./mvnw -DskipTests compile
```

Windows：

```powershell
mvnw.cmd -DskipTests compile
```

后端测试：

```bash
./mvnw test
```

Windows：

```powershell
mvnw.cmd test
```

前端构建：

```bash
cd frontend
npm run build
```

Windows PowerShell：

```powershell
cd frontend
npm.cmd run build
```

## 用户端测试流程

1. 登录或注册普通用户。
2. 查询题库列表并打开题目详情，确认未提前展示正确答案和解析。
3. 收藏题目，再取消收藏。
4. 进入刷题页提交答案，确认提交后才展示正确答案和解析。
5. 查看错题本和答题历史详情。
6. 创建 AI 模拟面试，获取面试详情，提交整场面试，查看 AI 历史。

## 管理员测试流程

1. 使用 `admin/admin123` 登录。
2. 访问管理后台统计、用户列表和题库列表。
3. 新增、编辑、删除题目。
4. 查看收藏记录、错题记录、答题记录，并通过弹窗查看答题详情。
5. 查看 AI 面试记录，并通过弹窗查看逐题点评详情。
6. 使用普通用户访问 `/api/admin/**`，应返回统一 JSON 403。

## 已知限制

- 项目使用 Maven Wrapper 3.3.2 + Maven 3.9.9；本机仍需要 JDK 17+。
- 在 JDK 26 下运行测试时，已通过 Surefire 配置 `-Dnet.bytebuddy.experimental=true` 兼容 Mockito/Byte Buddy。
- AI 能力当前为 Mock 规则生成和评分，不调用真实 DeepSeek 或 OpenAI API。
- 管理端聚焦课程项目核心管理闭环，暂无复杂审计日志和批量导入导出。

## Swagger 测试步骤

1. 启动项目后访问 `http://localhost:8080/swagger-ui.html`。
2. 调用 `POST /api/auth/login` 登录，测试管理员账号为 `admin/admin123`。
3. 普通用户可先调用 `POST /api/auth/register` 注册，再登录获取 token。
4. 点击 Swagger 右上角 Authorize，填写 `Bearer <token>`。
5. 调用 `POST /api/answers/submit` 提交答案。
6. 调用 `POST /api/favorites/{questionId}` 收藏题目。
7. 调用 `GET /api/wrong-questions` 查看错题本。
8. 调用 `GET /api/statistics/user/overview` 查看学习概览。
9. 调用 `POST /api/ai/review-answer` 或 `POST /api/ai/generate-interview` 测试 AI Mock 能力。
10. 使用管理员账号登录，重新 Authorize 为管理员 token。
11. 调用 `GET /api/admin/users` 查询用户列表。
12. 调用 `PUT /api/admin/users/{id}/status` 测试启用或禁用普通用户。
13. 调用 `GET /api/admin/statistics/overview` 查看管理看板概览。

## 后续开发计划

下一步建议围绕真实大模型接入、代码题沙箱判题、前端 E2E 测试和生产部署配置继续完善。
