# 项目交付说明

## 项目简介

AI Interview Platform 是一个面向 Java 后端学习与面试准备的刷题平台，包含用户端刷题闭环、AI 模拟面试助手和管理员后台管理能力。项目采用前后端分离架构，后端提供统一 JSON API，前端使用 Vue 3 构建可演示的交互页面。

## 技术栈

- 后端：Java 17、Spring Boot 3、Spring Security、JWT、MyBatis-Plus、MySQL 8、Redis、Lombok、SpringDoc OpenAPI、Maven Wrapper
- 前端：Vue 3、TypeScript、Vite、Element Plus、Vue Router、Pinia、Axios、ECharts、npm
- 基础设施：Docker Compose、MySQL、Redis
- 测试：Spring Boot Test、MockMvc、H2、JUnit 5

## 核心功能

- 用户注册、登录、JWT 鉴权和当前用户信息
- 题库查询、题目详情、刷题提交、客观题自动判题
- 收藏夹、错题本、答题历史
- 用户学习统计与每日学习记录
- AI 面试助手 Mock 能力
- 管理员后台数据看板与多模块记录管理

## 用户端功能

- Dashboard：学习概览、趋势图、薄弱知识点概览
- 题库列表：支持关键词、题型、难度、标签筛选和分页
- 题目详情：展示公开题目信息，避免提前泄露答案和解析
- 刷题页：支持单选、多选、判断、简答、编程题提交
- 收藏夹：收藏、取消收藏、分页查看
- 错题本：按题型、难度、状态筛选，支持删除错题记录
- 答题历史：分页查询和详情查看，详情页展示正确答案与解析

## 管理员端功能

- 后台首页统计：用户、题目、收藏、错题、答题、AI 面试记录总数
- 用户管理：用户列表、搜索、角色和状态展示
- 题库管理：题目列表、新增、编辑、删除
- 收藏记录、错题记录、答题记录、AI 面试记录管理
- 答题记录详情 Dialog：查看用户答案、正确答案、解析、判题结果
- AI 面试记录详情 Dialog：查看总评、逐题点评和改进建议

## AI 面试助手功能

- 创建模拟面试：选择岗位、难度、题目数量和关注方向
- 获取面试会话：展示题目列表和面试状态
- 提交整场面试：返回总分、等级、总结、优点、不足、建议和逐题点评
- AI 历史记录：分页查看模拟面试历史和详情
- 当前 AI 服务为 Mock 规则评分，不调用真实外部大模型 API

## 权限控制说明

- 后端使用 Spring Security + JWT 实现无状态鉴权
- `/api/auth/register`、`/api/auth/login`、`/api/health` 和 Swagger 路径允许匿名访问
- 用户端业务接口需要登录并携带 `Authorization: Bearer <token>`
- `/api/admin/**` 需要登录且拥有 `ADMIN` 角色
- 普通用户访问管理员接口返回统一 JSON 403
- 前端路由守卫会阻止未登录用户进入业务页，并隐藏普通用户无权访问的后台菜单

## 构建与运行方式

后端依赖服务：

```bash
docker compose up -d mysql redis
```

Windows PowerShell 后端编译：

```powershell
mvnw.cmd -DskipTests compile
```

Windows PowerShell 后端测试：

```powershell
mvnw.cmd test
```

后端启动：

```text
运行 com.example.aiinterview.AiInterviewApplication
```

前端安装和启动：

```powershell
cd frontend
npm install
npm.cmd run dev
```

前端构建：

```powershell
cd frontend
npm.cmd run build
```

## 测试账号说明

`sql/test-data.sql` 提供管理员测试账号：

```text
username: admin
password: admin123
role: ADMIN
```

普通用户可通过注册页面或 `POST /api/auth/register` 创建。

## 已知限制

- AI 面试助手当前为 Mock 规则评分，不接入真实 DeepSeek/OpenAI API。
- 编程题暂不接入在线代码执行与沙箱判题。
- 管理后台侧重课程项目核心演示，暂未实现复杂审计日志、批量导入导出和高级运营报表。
- 项目需要本机 JDK 17+；Maven 由 Maven Wrapper 自动下载。

## 后续可扩展方向

- 接入真实大模型服务，增强简答题点评和模拟面试反馈质量
- 增加题目批量导入导出、题目审核流和标签治理
- 增加学习计划、成就体系和更细粒度的统计分析
- 增加代码题在线运行、测试用例和沙箱隔离
- 完善管理员审计日志、操作追踪和权限分级
