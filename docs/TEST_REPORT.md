# 测试与构建报告

## 本地环境说明

- 操作系统：Windows 11
- 后端运行环境：JDK 26 执行 Maven Wrapper，项目编译目标为 Java 17
- Maven：通过 Maven Wrapper 自动下载 Maven 3.9.9
- 前端：Node.js + npm
- 测试数据库：H2，MySQL 兼容模式
- 外部服务：后端单元/接口测试不依赖真实 MySQL、Redis 或外部 AI API

## 后端编译

命令：

```powershell
mvnw.cmd -DskipTests compile
```

结果：

```text
BUILD SUCCESS
```

## 后端测试

命令：

```powershell
mvnw.cmd test
```

结果：

```text
BUILD SUCCESS
Tests run: 39, Failures: 0, Errors: 0, Skipped: 0
```

测试覆盖模块：

- 用户注册、登录、重复用户名、未登录访问
- 题库分页查询、管理员新增题目、按标签筛选、标签删除保护
- 答题提交、自动判题、多选顺序兼容、错题状态流转、收藏重复保护、越权访问保护
- 用户学习统计、趋势补齐、缓存清理、参数校验
- AI 点评、模拟面试生成、薄弱知识点总结、AI 历史访问控制
- 管理员权限、用户管理、统计概览、趋势补齐、缓存清理

## 前端构建

命令：

```powershell
cd frontend
npm.cmd run build
```

结果：

```text
成功
```

说明：

- `vue-tsc --noEmit` 通过
- `vite build` 通过
- 构建过程中存在 Vite chunk size 提示，不影响产物生成

## 已验证功能

- 普通用户登录、题库、题目详情、收藏、刷题提交、错题本、答题历史
- AI 面试创建、会话详情、整场提交、结果展示、历史记录
- 管理员后台首页、用户管理、题库管理、收藏记录、错题记录、答题记录详情、AI 面试记录详情
- JWT 鉴权、`ADMIN` 权限控制、统一 JSON 错误返回
- Maven Wrapper 编译与测试链路
- 前端生产构建链路

## 未覆盖风险

- 未接入真实 MySQL/Redis 的端到端自动化测试，当前以 H2 和 Mock 测试覆盖核心逻辑
- AI 能力为 Mock 规则，真实大模型接入后的超时、限流、错误重试需要单独测试
- 前端尚未加入自动化 E2E 测试
- 生产部署、HTTPS、日志采集和监控告警未纳入当前交付范围
