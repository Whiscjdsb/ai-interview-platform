# Demo Ready Guide

本文档用于面试、答辩或 GitHub 展示前的快速准备。目标是在 5 到 10 分钟内稳定跑通项目，并且在 DeepSeek 不可用时有 Mock 兜底方案。

## 1. 演示前准备

- 确认 JDK 17+ 可用
- 确认 Node.js 18+ 可用
- 确认 Docker Desktop 已启动
- 确认项目根目录存在 Maven Wrapper
- 确认前端依赖已安装
- 确认 MySQL 和 Redis 端口未被占用
- 准备两个浏览器窗口：
  - 普通用户页面
  - 管理员后台页面
- 打开以下文档备用：
  - `README.md`
  - `docs/DEMO_SCRIPT.md`
  - `docs/INTERVIEW_QA.md`
  - `docs/ARCHITECTURE.md`
  - `docs/FINAL_CHECKLIST.md`

## 2. MySQL 检查

启动依赖：

```powershell
docker compose up -d mysql redis
```

确认容器运行：

```powershell
docker ps
```

确认数据库脚本：

- `sql/init.sql`：建表、索引、基础角色
- `sql/test-data.sql`：演示账号、标签、题库测试数据

如果需要重新导入演示数据，可在 MySQL 客户端执行：

```sql
SOURCE sql/test-data.sql;
```

重点检查表：

```text
sys_user
sys_role
sys_user_role
question
question_tag
question_tag_relation
ai_review_record
```

## 3. 后端启动检查

配置环境变量示例：

```powershell
$env:SERVER_PORT="8080"
$env:MYSQL_URL="jdbc:mysql://localhost:3306/ai_interview_platform?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true"
$env:MYSQL_USERNAME="root"
$env:MYSQL_PASSWORD="root"
$env:REDIS_HOST="localhost"
$env:REDIS_PORT="6379"
$env:JWT_SECRET="replace-with-a-long-random-secret"
```

启动后端：

```powershell
.\mvnw.cmd spring-boot:run
```

健康检查：

```text
GET http://localhost:8080/api/health
```

期望返回：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "status": "UP"
  }
}
```

Swagger：

```text
http://localhost:8080/swagger-ui.html
```

## 4. 前端启动检查

```powershell
cd frontend
npm install
npm run dev
```

常见访问地址：

```text
http://localhost:5173
http://localhost:5174
```

前端 API 地址配置：

```text
frontend/.env.development
VITE_API_BASE_URL=http://localhost:8080
```

## 5. DeepSeek 模式演示方案

适合网络稳定、API Key 可用时演示。

配置：

```powershell
$env:APP_AI_PROVIDER="deepseek"
$env:DEEPSEEK_API_KEY="your_deepseek_api_key"
```

启动日志应出现：

```text
System final AI Provider = deepseek
selectedService=DeepSeekAiService
DeepSeek request sending
DeepSeek response received: status=200
```

演示重点：

- 单题 AI 点评返回 `modelName=deepseek-chat`
- `rawResponse` 包含 DeepSeek 原始 JSON
- `structuredResult` 用于前端结构化展示
- ScoreEngine 会对最终分数做统一校验

## 6. Mock 模式演示方案

适合无网络、无 API Key、答辩现场环境不稳定时演示。

配置：

```powershell
$env:APP_AI_PROVIDER="mock"
```

演示重点：

- 不依赖外部 AI API
- 项目完整流程仍可跑通
- AI 点评、模拟面试、追问和历史记录仍可演示
- 可以解释 Mock 是为了本地开发、测试和兜底

## 7. 普通用户演示账号

`sql/test-data.sql` 提供普通演示用户：

```text
username: demo_user
password: admin123
role: USER
```

说明：

- 密码在数据库中使用 BCrypt 密文保存
- 该账号用于演示用户端题库、刷题、收藏、错题、AI 面试和成长分析

## 8. 管理员演示账号

`sql/test-data.sql` 提供管理员演示用户：

```text
username: admin
password: admin123
role: ADMIN
```

说明：

- 密码在数据库中使用 BCrypt 密文保存
- 该账号用于演示后台首页、用户管理、题库管理、答题记录、AI 面试记录等能力

## 9. 推荐演示流程

5 分钟版本：

1. 登录普通用户
2. 展示题库列表和题目详情
3. 进入刷题页并提交答案
4. 展示错题本或答题历史
5. 创建 AI 模拟面试并提交
6. 展示 AI 结果页和 ScoreEngine 低质量回答封顶
7. 切换管理员账号展示后台统计

10 分钟版本：

1. 登录普通用户
2. 展示 Dashboard 学习概览
3. 展示题库筛选和题目详情
4. 收藏题目
5. 提交一次正确答案和一次错误答案
6. 展示错题本、收藏夹、答题历史
7. 创建 AI 模拟面试
8. 演示 AI 多轮追问
9. 提交面试并查看结构化结果
10. 下载 PDF 报告
11. 生成分享链接
12. 查看成长分析
13. 切换管理员后台
14. 展示用户、题库、答题记录和 AI 面试记录

## 10. 演示时推荐回答示例

客观题示例：

```text
StringBuilder 正确选 A，因为它是可变对象，适合单线程下频繁字符串拼接。
```

Spring Boot 简答题示例：

```text
Spring Boot 自动配置主要依赖 AutoConfiguration 类、条件注解和 classpath 判断。它会根据当前依赖、配置项和已有 Bean 自动装配默认 Bean。如果用户自己定义了 Bean，通常用户配置会优先生效，也可以通过 properties 或 exclude 排除某些自动配置。
```

Redis 简答题示例：

```text
Redis 缓存穿透是请求查询不存在的数据，导致缓存和数据库都无法命中。常见解决方式包括缓存空值、使用布隆过滤器、参数校验和接口限流。实际项目中需要结合过期时间和业务场景，避免空值缓存带来一致性问题。
```

低质量回答演示：

```text
我不会
```

说明话术：

```text
这里可以看到系统不会完全相信 AI 原始分，而是通过 ScoreEngine 对低质量回答做封顶，避免出现“我不会”也拿高分的情况。
```

## 11. 演示失败时的兜底方案

DeepSeek 不可用：

- 切换到 `APP_AI_PROVIDER=mock`
- 说明项目支持 Provider fallback
- 展示 Mock 下的完整业务流程

MySQL 启动失败：

- 检查 Docker 是否启动
- 检查 3306 端口是否占用
- 重新执行 `docker compose up -d mysql redis`
- 如果现场时间有限，切换到文档讲解架构和测试报告

前端无法访问后端：

- 检查 `VITE_API_BASE_URL`
- 检查后端 `/api/health`
- 检查浏览器 Network 中是否是 CORS 或 401
- 说明项目已有 CORS 配置和统一鉴权处理

登录失败：

- 确认已导入 `sql/test-data.sql`
- 使用 `admin/admin123` 或 `demo_user/admin123`
- 检查用户是否被禁用

PDF 下载失败：

- 先展示 AI 结果页
- 说明 PDF 由后端生成，前端以 blob 下载
- 切换到 `docs/ARCHITECTURE.md` 讲 PDF 流程图

## 12. 面试官提问时如何切换到文档讲解

如果现场网络或环境不稳定，可以切换到以下文档讲解：

- 问系统整体架构：打开 `docs/ARCHITECTURE.md`
- 问项目功能和完成度：打开 `docs/PROJECT_SUMMARY.md`
- 问面试回答：打开 `docs/INTERVIEW_QA.md`
- 问演示路线：打开 `docs/DEMO_SCRIPT.md`
- 问最终检查：打开 `docs/FINAL_CHECKLIST.md`
- 问测试结果：打开 `docs/TEST_REPORT.md`

推荐话术：

```text
如果现场环境不稳定，我可以先用文档把核心链路讲清楚。项目里有架构图、AI 调用链路、ScoreEngine 评分链路、数据库关系和测试报告，后续环境恢复后可以继续跑实际页面。
```

