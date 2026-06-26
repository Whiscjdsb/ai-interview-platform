# AI Interview Platform Frontend

Vue 3 + TypeScript + Vite 前端项目。当前包含基础架构、登录注册、用户首页 Dashboard、题库、刷题、收藏夹、错题本、答题历史和 AI 面试助手页面。

## 相关文档

- [项目交付说明](../docs/DELIVERY.md)
- [演示脚本](../docs/DEMO_SCRIPT.md)
- [API 概览](../docs/API_OVERVIEW.md)
- [测试与构建报告](../docs/TEST_REPORT.md)

## 环境要求

- Node.js 18.18+，推荐 Node.js 20 LTS
- npm 9+
- 后端服务运行在 `http://localhost:8080`

## 安装依赖

```bash
npm install
```

## 启动开发服务

```bash
npm run dev
```

默认访问地址：

```text
http://localhost:5173
```

## 构建

```bash
npm run build
```

## API 地址配置

开发环境配置文件为 `.env.development`：

```text
VITE_API_BASE_URL=http://localhost:8080
```

如需连接其他后端地址，可复制 `.env.example` 并调整 `VITE_API_BASE_URL`。

## 后端启动要求

1. 在项目根目录启动 MySQL 和 Redis：

```bash
docker compose up -d mysql redis
```

2. 使用 IDEA 运行后端主类：

```text
com.example.aiinterview.AiInterviewApplication
```

3. 确认后端健康检查：

```text
GET http://localhost:8080/api/health
```

## 当前页面

- `/login`：登录
- `/register`：注册
- `/dashboard`：用户学习首页
- `/questions`：题库列表，支持关键词、题型、难度、标签和分页筛选
- `/questions/:id`：题目详情，只展示公开题目信息，支持收藏和取消收藏
- `/practice/:id`：刷题页，支持单选、多选、判断、简答和编程题提交
- `/favorites`：我的收藏，支持查看详情、开始练习和取消收藏
- `/wrong-questions`：错题本，支持按题型、难度、状态筛选和删除错题记录
- `/answer-history`：答题历史，支持按题型、判题结果和答题时间筛选
- `/answer-history/:id`：答题记录详情，展示本次答题的正确答案和解析
- `/ai-interview`：AI 面试首页，支持创建模拟面试
- `/ai-interview/session/:id`：AI 面试进行页，支持逐题作答、上一题、下一题和提交
- `/ai-interview/result/:id`：AI 面试结果页，展示总评分、逐题评分和改进建议
- `/ai-interview/history`：AI 面试历史，支持分页、搜索和时间筛选
- `/ai-interview/history/:id`：AI 面试历史详情
- `/admin`：管理员后台首页，仅 ADMIN 可访问
- `/admin/users`：用户管理
- `/admin/questions`：题库管理
- `/admin/questions/create`：新增题目
- `/admin/questions/:id/edit`：编辑题目
- `/admin/favorites`：收藏记录管理
- `/admin/wrong-questions`：错题记录管理
- `/admin/answers`：答题记录管理
- `/admin/ai-interviews`：AI 面试记录管理
- 个人中心当前为“开发中”占位页

## 接口对应关系

```text
POST /api/auth/login                  登录
POST /api/auth/register               注册
GET  /api/auth/current                当前用户
GET  /api/statistics/user/overview    Dashboard 概览
GET  /api/statistics/user/trend       Dashboard 趋势
GET  /api/statistics/user/wrong-analysis 高频错题知识点

GET  /api/questions                   题库分页列表
GET  /api/questions/{id}              题目详情
GET  /api/tags                        标签筛选
POST /api/answers/submit              提交答案
GET  /api/answers/history             答题历史分页
GET  /api/answers/history/{id}        答题记录详情

POST /api/favorites/{questionId}      收藏题目
DELETE /api/favorites/{questionId}    取消收藏
GET  /api/favorites                   我的收藏分页

GET  /api/wrong-questions             错题本分页
DELETE /api/wrong-questions/{questionId} 删除错题记录

POST /api/ai/generate-interview       生成模拟面试题
GET  /api/ai/history                  AI 历史记录分页
GET  /api/ai/history/{id}             AI 历史记录详情
GET  /api/ai/interviews/{id}          获取模拟面试会话详情
POST /api/ai/interviews/{id}/submit   提交整场模拟面试

GET  /api/admin/dashboard             管理后台首页统计
GET  /api/admin/users                 用户管理列表
GET  /api/admin/questions             管理员题库列表
POST /api/admin/questions             新增题目
PUT  /api/admin/questions/{id}        编辑题目
DELETE /api/admin/questions/{id}      删除题目
GET  /api/admin/favorites             收藏记录列表
GET  /api/admin/wrong-questions       错题记录列表
GET  /api/admin/answers               答题记录列表
GET  /api/admin/ai-interviews         AI 面试记录列表
GET  /api/admin/ai-interviews/{id}    AI 面试记录详情
```

## AI 面试模块

页面截图位置预留：

```text
frontend/docs/screenshots/ai-interview-home.png
frontend/docs/screenshots/ai-interview-session.png
frontend/docs/screenshots/ai-interview-result.png
frontend/docs/screenshots/ai-interview-history.png
```

测试流程：

1. 登录后进入 `/ai-interview`。
2. 输入岗位，选择难度、题目数量和关注方向。
3. 点击“开始面试”，前端调用 `POST /api/ai/generate-interview` 生成题目。
4. 在 `/ai-interview/session/:id` 逐题输入回答，可使用上一题和下一题切换。
5. 点击“提交整场面试”，前端调用 `POST /api/ai/interviews/{id}/submit` 获取评分结果并进入 `/ai-interview/result/:id`。
6. 进入 `/ai-interview/history`，查看后端保存的 `MOCK_INTERVIEW` 历史记录。
7. 点击历史记录详情进入 `/ai-interview/history/:id`，查看后端返回的 AI 历史结果。

## 管理员后台

管理员后台菜单仅在当前用户角色包含 `ADMIN` 时显示。普通用户直接访问 `/admin` 或 `/admin/**` 时，前端路由守卫会跳转回 `/dashboard`；后端 `/api/admin/**` 接口也会通过 Spring Security 校验 `ADMIN` 角色。

测试流程：

1. 使用管理员账号登录。
2. 进入 `/admin` 查看用户、题目、收藏、错题、答题记录和 AI 面试记录总数。
3. 进入 `/admin/users` 搜索用户并检查角色、状态和注册时间。
4. 进入 `/admin/questions` 按题型、难度和关键词筛选题目。
5. 进入 `/admin/questions/create` 新增题目，或进入 `/admin/questions/:id/edit` 编辑题目。
6. 在题库管理列表执行删除题目，确认弹窗后再提交。
7. 分别进入 `/admin/favorites`、`/admin/wrong-questions`、`/admin/answers`、`/admin/ai-interviews` 验证记录分页和筛选。
8. 使用普通用户账号访问 `/admin`，应被跳转回首页，且后台菜单不可见。

## 刷题功能测试

1. 登录后进入 `/questions`。
2. 使用关键词、题型、难度或标签筛选题目。
3. 点击“查看详情”，确认详情页只展示标题、题型、难度、标签和题目内容。
4. 返回列表后点击“开始练习”。
5. 根据题型填写答案：
   - 单选题选择一个选项，例如 `A`
   - 多选题可选择多个选项，提交时会自动规范为 `A,B,C`
   - 判断题选择“正确”或“错误”
   - 简答题和编程题使用文本框输入
6. 点击“提交答案”，确认弹窗后完成提交。
7. 提交成功后才会展示正确答案和题目解析；提交前页面不会渲染这些字段。

## 收藏、错题本和答题历史测试

1. 登录后进入 `/questions`，打开任意题目详情。
2. 点击“收藏题目”，确认按钮切换为“已收藏”，再进入 `/favorites` 查看收藏列表。
3. 在 `/favorites` 中点击“取消收藏”，确认后列表会刷新；无收藏时会显示空状态。
4. 在 `/practice/:id` 提交客观题错误答案后，进入 `/wrong-questions` 查看错题记录。
5. 在错题本中按题型、难度、状态筛选，点击“再次练习”可返回刷题页。
6. 在 `/answer-history` 按题型、判题结果或时间范围筛选答题记录。
7. 点击“查看详情”进入 `/answer-history/:id`，该页面会展示用户答案、正确答案、解析、得分和耗时。

正确答案和题目解析只会在两种场景展示：刷题提交成功后的结果区域，或答题历史详情页。题库列表、题目详情、收藏列表、错题本列表和答题历史列表都不会提前展示正确答案和解析。

## 联调顺序

1. 启动后端服务。
2. 启动前端服务。
3. 访问 `/register` 注册普通用户，或使用已有账号登录。
4. 登录成功后进入 `/dashboard`。
5. Dashboard 会请求当前用户、学习概览、7 天趋势和错题知识点数据。
6. 进入 `/questions` 查询题库并开始练习。
7. 提交答案后进入 `/answer-history` 查看历史记录，进入 `/favorites` 和 `/wrong-questions` 验证收藏和错题本流程。

## 质量验收命令

环境要求：

- JDK 17+
- Node.js 18+

项目根目录已包含 Maven Wrapper，首次运行会自动下载 Maven 3.9.9。

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
npm run build
```

Windows PowerShell 推荐：

```powershell
npm.cmd run build
```

## 测试覆盖说明

用户端建议按顺序验证：登录、题库列表、题目详情、收藏与取消收藏、刷题提交、错题本、答题历史、AI 面试创建、AI 面试详情、AI 面试提交、AI 面试历史。

管理端建议按顺序验证：管理员登录、后台统计、用户列表、题库列表、新增题目、编辑题目、删除题目、收藏记录、错题记录、答题记录、AI 面试记录、普通用户访问后台接口被拒绝。

已知限制：

- AI 面试助手当前使用 Mock 规则评分，不调用真实大模型。
- 管理后台为课程项目级实现，侧重核心 CRUD、分页筛选和详情查看。
- 后端已补齐 Maven Wrapper，本机仍需要 JDK 17+。
- 在 JDK 26 下运行测试时，后端 Surefire 已配置 `-Dnet.bytebuddy.experimental=true` 兼容 Mockito/Byte Buddy。
