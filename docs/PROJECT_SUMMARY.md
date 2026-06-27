# Project Summary

## 一句话介绍

AI Interview Platform 是一个基于 Spring Boot 3 + Vue 3 的 AI 面试训练平台，支持题库刷题、自动判题、学习统计、AI 模拟面试、PDF 报告、公开分享、成长分析和管理员后台。

## 完整功能清单

用户端功能：

- 注册、登录、JWT 鉴权
- 当前用户信息
- 题库列表、题目详情、刷题提交
- 单选、多选、判断题自动判题
- 简答题和编程题提交记录
- 答题历史和答题详情
- 收藏题目、取消收藏、收藏列表
- 错题本、错题状态、错题删除
- 学习统计概览
- 最近学习趋势
- 标签正确率分析
- 错题知识点分析
- AI 简答题点评
- AI 模拟面试创建、会话、提交、结果展示
- AI 多轮追问
- AI 面试历史和详情
- AI 面试 PDF 报告导出
- AI 面试报告公开分享
- AI 面试成长分析

管理员端功能：

- 后台首页统计
- 用户列表、用户详情
- 用户启用和禁用
- 用户角色维护
- 题库列表、题目新增、编辑、删除
- 收藏记录管理
- 错题记录管理
- 答题记录管理
- AI 面试记录管理
- 热门题目、热门标签、用户排行

企业面试原型：

- 企业面试官风格
- 企业面试模板
- 企业评分维度
- 岗位匹配分析
- 企业模拟面试入口

## 核心技术亮点

- 使用 Spring Security + JWT 实现无状态鉴权。
- 使用 MyBatis-Plus 实现数据访问和逻辑删除。
- 使用 Redis 缓存用户统计和管理员统计结果。
- 使用 `AiServiceFactory` 抽象 AI Provider，支持 Mock 和 DeepSeek。
- 使用 `ScoreEngine` 统一评分入口，降低评分逻辑散落风险。
- 使用 SpringDoc OpenAPI 提供接口调试入口。
- 使用 Vue 3 + TypeScript + Element Plus 构建用户端和管理端。
- 使用 ECharts 展示学习趋势、错题分析和能力雷达图。
- 使用 OpenPDF 导出 AI 面试报告。

## 后端架构说明

后端采用典型分层结构：

```text
Controller
  ↓
Service / ServiceImpl
  ↓
Mapper
  ↓
MySQL / Redis
```

模块划分：

- `module/user`：认证、用户、角色
- `module/question`：题库、标签、题目管理
- `module/answer`：答题、收藏、错题本
- `module/statistics`：学习统计
- `module/ai`：AI 点评、模拟面试、追问、PDF、分享、成长分析
- `module/admin`：管理员用户管理、统计看板、后台记录管理
- `common`：统一返回、异常、分页
- `security`：JWT、安全过滤器、权限控制

## 前端架构说明

前端使用 Vue 3 + TypeScript + Vite：

```text
src/api        接口封装
src/types      类型定义
src/stores     Pinia 状态管理
src/router     路由与权限守卫
src/layouts    通用布局
src/views      页面
src/utils      工具函数
```

前端特点：

- Axios 统一处理后端返回结构。
- Pinia 保存 Token、用户信息和角色。
- Router 实现登录态和管理员权限守卫。
- Element Plus 保持统一 UI 风格。
- ECharts 展示趋势图、柱状图和雷达图。

## AI 模块设计说明

AI 模块核心目标是“可运行、可扩展、可回退”。

核心组件：

- `AiService`：AI 服务统一接口
- `MockAiService`：本地规则生成，用于无 API Key 环境
- `DeepSeekAiService`：真实 DeepSeek 调用
- `OpenAiService`：预留扩展
- `AiServiceFactory`：根据配置选择 Provider
- `ScoreEngine`：统一评分入口

Provider 选择逻辑：

```text
app.ai.provider
  ↓
APP_AI_PROVIDER
  ↓
AI_PROVIDER
  ↓
mock
```

当 `deepseek` 被选中但 `DEEPSEEK_API_KEY` 缺失时，系统会 fallback 到 Mock，并打印明确日志。

## ScoreEngine 评分流转说明

评分入口：

```text
ScoreEngine.calculateScore(AiScoreContext context)
```

上下文字段：

- `deepseekScore`
- `userAnswer`
- `questionType`
- `isFallback`
- `referencePoints`

统一流转：

```text
AI 原始分
  ↓
AiScoreContext
  ↓
ScoreEngine
  ↓
质量检测和封顶
  ↓
最终 score
```

统一覆盖：

- 单题 AI 点评 `score`
- 结构化结果 `structuredResult.score`
- 整场面试 `questionResults.score`
- 整场面试 `totalScore`
- PDF、分享、成长分析读取最终保存分数

可观测日志：

```text
traceId
inputScore
finalScore
correctionReason
```

## 数据库主要表说明

用户与权限：

- `sys_user`：用户
- `sys_role`：角色
- `sys_user_role`：用户角色关系

题库：

- `question`：题目
- `question_tag`：标签
- `question_tag_relation`：题目标签关系

答题与学习：

- `user_answer_record`：答题记录
- `user_favorite`：收藏
- `user_wrong_question`：错题本
- `daily_learning_record`：每日学习记录

AI：

- `ai_review_record`：AI 点评、模拟面试、弱点总结、分享、企业面试原型记录

## 当前项目完成度

已完成：

- 后端核心业务闭环
- 前端用户端主要页面
- 前端管理员后台主要页面
- AI 面试主流程
- DeepSeek 接入
- ScoreEngine 统一评分
- PDF 导出
- 分享页面
- 成长分析
- 测试与构建回归
- 文档交付材料

当前定位：

- 可作为完整课程设计或毕业设计原型
- 可作为 Java 后端简历项目
- 可作为面试讲解项目
- 可继续扩展为 SaaS 化 AI 面试平台

## 后续可扩展方向

- 接入更多大模型 Provider
- 增加流式输出
- 增加真实代码题判题沙箱
- 增加企业多租户
- 增加订阅和计费
- 增加岗位能力模型库
- 增加更细粒度的评分解释
- 增加消息队列异步生成报告
- 增加在线通知和任务状态

