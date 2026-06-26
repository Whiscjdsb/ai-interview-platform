# API 概览

接口统一返回结构：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

## 用户端接口

### 认证相关

| 方法 | 路径 | 用途 | 权限 |
| --- | --- | --- | --- |
| POST | `/api/auth/register` | 用户注册 | 匿名 |
| POST | `/api/auth/login` | 用户登录并返回 JWT | 匿名 |
| GET | `/api/auth/current` | 获取当前登录用户 | 登录 |
| GET | `/api/health` | 健康检查 | 匿名 |

### 题库相关

| 方法 | 路径 | 用途 | 权限 |
| --- | --- | --- | --- |
| GET | `/api/questions` | 题库分页查询，支持关键词、题型、难度、标签筛选 | 登录 |
| GET | `/api/questions/{id}` | 获取题目详情，不返回答案和解析 | 登录 |
| GET | `/api/tags` | 获取标签列表 | 登录 |

### 答题相关

| 方法 | 路径 | 用途 | 权限 |
| --- | --- | --- | --- |
| POST | `/api/answers/submit` | 提交答案并返回判题结果 | 登录 |
| GET | `/api/answers/history` | 分页查询当前用户答题历史 | 登录 |
| GET | `/api/answers/history/{id}` | 查看当前用户答题记录详情 | 登录 |

### 收藏相关

| 方法 | 路径 | 用途 | 权限 |
| --- | --- | --- | --- |
| POST | `/api/favorites/{questionId}` | 收藏题目 | 登录 |
| DELETE | `/api/favorites/{questionId}` | 取消收藏 | 登录 |
| GET | `/api/favorites` | 分页查询我的收藏 | 登录 |

### 错题相关

| 方法 | 路径 | 用途 | 权限 |
| --- | --- | --- | --- |
| GET | `/api/wrong-questions` | 分页查询错题本 | 登录 |
| DELETE | `/api/wrong-questions/{questionId}` | 删除当前用户错题记录 | 登录 |

### 学习统计相关

| 方法 | 路径 | 用途 | 权限 |
| --- | --- | --- | --- |
| GET | `/api/statistics/user/overview` | 用户学习概览 | 登录 |
| GET | `/api/statistics/user/trend` | 最近 N 天学习趋势 | 登录 |
| GET | `/api/statistics/user/tag-accuracy` | 标签维度正确率 | 登录 |
| GET | `/api/statistics/user/wrong-analysis` | 当前活跃错题标签分析 | 登录 |

### AI 面试相关

| 方法 | 路径 | 用途 | 权限 |
| --- | --- | --- | --- |
| POST | `/api/ai/review-answer` | 简答题或编程题 AI 点评 | 登录 |
| POST | `/api/ai/generate-interview` | 创建模拟面试并生成题目 | 登录 |
| GET | `/api/ai/interviews/{id}` | 获取模拟面试会话详情 | 登录且本人 |
| POST | `/api/ai/interviews/{id}/submit` | 提交整场模拟面试答案 | 登录且本人 |
| POST | `/api/ai/weakness-summary` | 生成薄弱知识点总结 | 登录 |
| GET | `/api/ai/history` | 分页查询 AI 历史记录 | 登录 |
| GET | `/api/ai/history/{id}` | 获取 AI 历史详情 | 登录且本人 |
| DELETE | `/api/ai/history/{id}` | 删除 AI 历史记录 | 登录且本人 |

## 管理端接口

所有管理端接口均要求 `ADMIN` 角色。

### 后台统计

| 方法 | 路径 | 用途 | 权限 |
| --- | --- | --- | --- |
| GET | `/api/admin/dashboard` | 管理后台首页总数统计 | ADMIN |
| GET | `/api/admin/statistics/overview` | 平台统计概览 | ADMIN |
| GET | `/api/admin/statistics/trend` | 平台趋势统计 | ADMIN |
| GET | `/api/admin/statistics/popular-questions` | 热门题目排行 | ADMIN |
| GET | `/api/admin/statistics/popular-tags` | 热门标签排行 | ADMIN |
| GET | `/api/admin/statistics/user-ranking` | 用户学习排行 | ADMIN |

### 用户管理

| 方法 | 路径 | 用途 | 权限 |
| --- | --- | --- | --- |
| GET | `/api/admin/users` | 用户分页查询 | ADMIN |
| GET | `/api/admin/users/{id}` | 用户详情 | ADMIN |
| PUT | `/api/admin/users/{id}/status` | 启用或禁用用户 | ADMIN |
| PUT | `/api/admin/users/{id}/roles` | 修改用户角色 | ADMIN |

### 题库管理

| 方法 | 路径 | 用途 | 权限 |
| --- | --- | --- | --- |
| GET | `/api/admin/questions` | 管理员题目分页查询 | ADMIN |
| GET | `/api/admin/questions/{id}` | 获取完整题目详情，包含答案和解析 | ADMIN |
| POST | `/api/admin/questions` | 新增题目 | ADMIN |
| PUT | `/api/admin/questions/{id}` | 编辑题目 | ADMIN |
| DELETE | `/api/admin/questions/{id}` | 删除题目 | ADMIN |
| POST | `/api/admin/tags` | 新增标签 | ADMIN |
| PUT | `/api/admin/tags/{id}` | 编辑标签 | ADMIN |
| DELETE | `/api/admin/tags/{id}` | 删除标签 | ADMIN |

### 记录管理

| 方法 | 路径 | 用途 | 权限 |
| --- | --- | --- | --- |
| GET | `/api/admin/favorites` | 收藏记录列表 | ADMIN |
| GET | `/api/admin/wrong-questions` | 错题记录列表 | ADMIN |
| GET | `/api/admin/answers` | 答题记录列表，包含管理员详情弹窗所需字段 | ADMIN |
| GET | `/api/admin/ai-interviews` | AI 面试记录列表 | ADMIN |
| GET | `/api/admin/ai-interviews/{id}` | AI 面试记录详情 | ADMIN |
