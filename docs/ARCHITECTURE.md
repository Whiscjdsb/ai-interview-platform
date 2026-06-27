# Architecture

本文档用文字和 Mermaid 图梳理 AI Interview Platform 的整体架构、AI 调用链路、评分链路、数据库关系和前后端交互流程。

## 1. 整体架构

项目采用前后端分离架构：

- 前端：Vue 3 + TypeScript + Element Plus
- 后端：Spring Boot 3 + Spring Security + MyBatis-Plus
- 数据库：MySQL 8
- 缓存：Redis 7
- AI Provider：Mock / DeepSeek
- 文档与调试：SpringDoc OpenAPI / Swagger

```mermaid
flowchart LR
    User["User Browser"] --> Frontend["Vue 3 Frontend"]
    Admin["Admin Browser"] --> Frontend

    Frontend --> Axios["Axios Request Layer"]
    Axios --> Backend["Spring Boot Backend"]

    Backend --> Security["Spring Security + JWT"]
    Security --> Controllers["Controllers"]
    Controllers --> Services["Service Layer"]
    Services --> Mappers["MyBatis-Plus Mappers"]
    Mappers --> MySQL[("MySQL 8")]

    Services --> Redis[("Redis 7 Cache")]
    Services --> AI["AI Service Layer"]
    AI --> Factory["AiServiceFactory"]
    Factory --> Mock["MockAiService"]
    Factory --> DeepSeek["DeepSeekAiService"]
    DeepSeek --> DeepSeekAPI["DeepSeek API"]

    Services --> ScoreEngine["ScoreEngine"]
    Services --> PDF["PDF Export Service"]
```

## 2. 后端分层

后端主要按业务模块拆分：

```text
common       统一返回、异常、分页
security     JWT、安全过滤器、用户详情
module/user  用户、角色、认证
module/question  题库、标签、管理员题目管理
module/answer    答题、收藏、错题本
module/statistics 学习统计
module/ai        AI 点评、模拟面试、追问、PDF、分享、成长分析
module/admin     管理员后台、平台统计
```

```mermaid
flowchart TB
    Controller["Controller"]
    Service["Service / ServiceImpl"]
    Mapper["Mapper"]
    Entity["Entity"]
    DTO["DTO"]
    VO["VO"]
    DB[("Database")]

    DTO --> Controller
    Controller --> Service
    Service --> Mapper
    Mapper --> Entity
    Mapper --> DB
    Service --> VO
    VO --> Controller
```

## 3. 前端架构

前端采用 Vue 3 + TypeScript，按接口、类型、页面、布局和状态管理拆分。

```mermaid
flowchart TB
    App["App.vue"]
    Router["Vue Router"]
    Layout["AppLayout"]
    Views["Views"]
    Store["Pinia User Store"]
    API["API Modules"]
    Request["Axios Request"]
    Backend["Backend API"]

    App --> Router
    Router --> Layout
    Layout --> Views
    Views --> Store
    Views --> API
    API --> Request
    Request --> Backend
```

主要目录：

```text
frontend/src/api        接口封装
frontend/src/types      类型定义
frontend/src/stores     Pinia 状态
frontend/src/router     路由与权限守卫
frontend/src/layouts    通用布局
frontend/src/views      页面
frontend/src/utils      工具函数
```

## 4. AI Provider 调用链路

AI 调用通过 `AiService` 抽象，不直接把 DeepSeek 逻辑写死到业务 Service。

Provider 解析优先级：

```text
app.ai.provider
  ↓
APP_AI_PROVIDER
  ↓
AI_PROVIDER
  ↓
mock
```

```mermaid
sequenceDiagram
    participant Client as Frontend
    participant Controller as AiController
    participant Service as AiAssistantServiceImpl
    participant Factory as AiServiceFactory
    participant Mock as MockAiService
    participant DeepSeek as DeepSeekAiService
    participant API as DeepSeek API

    Client->>Controller: POST /api/ai/review-answer
    Controller->>Service: reviewAnswer(userId, request)
    Service->>Factory: current()
    Factory-->>Service: AiService implementation

    alt provider=deepseek and api key exists
        Service->>DeepSeek: reviewAnswer(question, tags, answer)
        DeepSeek->>API: POST /chat/completions
        API-->>DeepSeek: choices[0].message.content
        DeepSeek-->>Service: raw score + structured result
    else provider missing or unavailable
        Service->>Mock: reviewAnswer(question, tags, answer)
        Mock-->>Service: mock raw score + review text
    end

    Service->>Service: ScoreEngine.calculateScore(...)
    Service-->>Controller: final review result
    Controller-->>Client: unified JSON response
```

## 5. ScoreEngine 评分链路

`ScoreEngine` 是最终评分唯一入口。AI Provider 只负责生成原始分，最终分数由 `ScoreEngine` 统一计算。

```mermaid
flowchart TB
    Raw["Raw Score<br/>DeepSeek / Mock / Fallback"]
    Context["AiScoreContext<br/>deepseekScore<br/>userAnswer<br/>questionType<br/>isFallback<br/>referencePoints"]
    Engine["ScoreEngine.calculateScore"]
    Quality["Quality Rules<br/>empty <= 5<br/>invalid <= 15<br/>generic <= 50"]
    Final["Final Score 0-100"]
    Persist["Persist Result"]
    Display["Frontend / PDF / Share / Growth"]

    Raw --> Context
    Context --> Engine
    Engine --> Quality
    Quality --> Final
    Final --> Persist
    Persist --> Display
```

评分结果覆盖：

- 单题 AI 点评 `score`
- `structuredResult.score`
- 整场面试 `questionResults.score`
- 整场面试 `totalScore`
- PDF 报告
- 分享页
- 成长分析

可观测日志：

```text
traceId
questionType
isFallback
inputScore
finalScore
correctionReason
```

## 6. AI 模拟面试流程

```mermaid
sequenceDiagram
    participant User as User
    participant Frontend as Frontend
    participant Backend as Backend
    participant AI as AI Service
    participant Score as ScoreEngine
    participant DB as MySQL

    User->>Frontend: Create interview
    Frontend->>Backend: POST /api/ai/generate-interview
    Backend->>AI: generateInterview(...)
    AI-->>Backend: questions
    Backend->>DB: save MOCK_INTERVIEW record
    Backend-->>Frontend: interviewId + questions

    User->>Frontend: Submit answers
    Frontend->>Backend: POST /api/ai/interviews/{id}/submit
    Backend->>Score: calculate each question score
    Score-->>Backend: final question scores
    Backend->>Backend: calculate totalScore
    Backend->>DB: update AI review record
    Backend-->>Frontend: final result
```

## 7. 多轮追问流程

```mermaid
sequenceDiagram
    participant User as User
    participant Frontend as Session Page
    participant Backend as Backend
    participant Factory as AiServiceFactory
    participant DeepSeek as DeepSeekAiService

    User->>Frontend: Answer current question
    Frontend->>Backend: POST /api/ai/interview/next-question
    Backend->>Factory: current()
    Factory-->>Backend: DeepSeek or Mock

    alt DeepSeek available
        Backend->>DeepSeek: chat(prompt with question, answer, history)
        DeepSeek-->>Backend: nextQuestion + reason
    else Mock fallback
        Backend-->>Backend: build local follow-up question
    end

    Backend-->>Frontend: nextQuestion
    Frontend-->>User: show follow-up question
```

## 8. PDF 导出与分享流程

```mermaid
flowchart LR
    Result["AI Interview Result"]
    Export["GET /api/ai/interview/{id}/export-pdf"]
    PDF["AiInterviewPdfService"]
    File["application/pdf download"]

    Share["POST /api/ai/interview/{id}/share"]
    Token["share_token + is_public"]
    Public["GET /api/share/{token}"]
    Page["Public Share Page"]

    Result --> Export --> PDF --> File
    Result --> Share --> Token --> Public --> Page
```

## 9. 数据库关系

主要数据表：

- `sys_user`
- `sys_role`
- `sys_user_role`
- `question`
- `question_tag`
- `question_tag_relation`
- `user_answer_record`
- `user_favorite`
- `user_wrong_question`
- `daily_learning_record`
- `ai_review_record`

```mermaid
erDiagram
    SYS_USER ||--o{ SYS_USER_ROLE : has
    SYS_ROLE ||--o{ SYS_USER_ROLE : grants

    SYS_USER ||--o{ USER_ANSWER_RECORD : answers
    SYS_USER ||--o{ USER_FAVORITE : favorites
    SYS_USER ||--o{ USER_WRONG_QUESTION : owns
    SYS_USER ||--o{ DAILY_LEARNING_RECORD : learns
    SYS_USER ||--o{ AI_REVIEW_RECORD : creates

    QUESTION ||--o{ USER_ANSWER_RECORD : answered_by
    QUESTION ||--o{ USER_FAVORITE : favorited_by
    QUESTION ||--o{ USER_WRONG_QUESTION : wrong_by
    QUESTION ||--o{ AI_REVIEW_RECORD : reviewed_by

    QUESTION ||--o{ QUESTION_TAG_RELATION : has
    QUESTION_TAG ||--o{ QUESTION_TAG_RELATION : maps

    SYS_USER {
        bigint id
        varchar username
        varchar password
        varchar nickname
        int status
        datetime create_time
    }

    QUESTION {
        bigint id
        varchar title
        text content
        varchar question_type
        varchar difficulty
        text correct_answer
        text analysis
    }

    AI_REVIEW_RECORD {
        bigint id
        bigint user_id
        bigint question_id
        varchar record_type
        text input_content
        text result_content
        decimal score
        varchar model_name
        varchar share_token
        boolean is_public
    }
```

## 10. 前后端交互流程

以登录和刷题为例：

```mermaid
sequenceDiagram
    participant User as User
    participant Vue as Vue Frontend
    participant Axios as Axios
    participant Security as Spring Security
    participant API as Backend Controller
    participant Service as Service
    participant DB as MySQL

    User->>Vue: Submit login form
    Vue->>Axios: POST /api/auth/login
    Axios->>API: username/password
    API->>Service: authenticate
    Service->>DB: load user and roles
    Service-->>API: JWT + user info
    API-->>Axios: Result data
    Axios-->>Vue: save token

    User->>Vue: Open question list
    Vue->>Axios: GET /api/questions
    Axios->>Security: Authorization Bearer token
    Security-->>API: authenticated user
    API->>Service: query page
    Service->>DB: select questions and tags
    DB-->>Service: records
    Service-->>API: page result
    API-->>Vue: question list
```

## 11. 管理端权限流程

```mermaid
flowchart TB
    Request["/api/admin/** Request"]
    JWT["JWT Filter"]
    UserDetails["CustomUserDetailsService"]
    Roles["Roles: USER / ADMIN"]
    PreAuth["@PreAuthorize / Security Rules"]
    Allow["Allow"]
    Deny["JSON 403"]

    Request --> JWT
    JWT --> UserDetails
    UserDetails --> Roles
    Roles --> PreAuth
    PreAuth -->|has ADMIN| Allow
    PreAuth -->|no ADMIN| Deny
```

## 12. 缓存与一致性

Redis 主要用于统计结果缓存：

- 用户统计概览
- 管理员统计概览
- 平台趋势
- 热门题目
- 热门标签

缓存失效场景：

- 用户提交答案
- 用户收藏或取消收藏
- 删除错题
- 新增、修改、删除题目
- 新增 AI 记录
- 用户状态或角色变化

```mermaid
flowchart LR
    Write["Business Write"]
    DB["Update MySQL"]
    Evict["Evict Redis Cache"]
    Query["Next Query"]
    Rebuild["Rebuild Cache"]

    Write --> DB
    DB --> Evict
    Evict --> Query
    Query --> Rebuild
```

