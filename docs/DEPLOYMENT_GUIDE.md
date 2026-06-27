# 部署说明文档

本文档用于说明 AI Interview Platform 从本地开发环境扩展到服务器部署时的推荐做法。当前项目是学习与展示型原型，部署方案以“简单、可复现、便于面试讲解”为目标。

## 部署架构说明

推荐部署架构：

```text
Browser
  |
  | HTTPS / HTTP
  v
Nginx
  |-- 托管 Vue build 后的静态资源 dist/
  |-- /api 反向代理到 Spring Boot 8080
  v
Spring Boot Backend
  |-- 以 jar 包方式运行
  |-- 读取 MySQL / Redis / JWT / AI Provider 环境变量
  v
MySQL 8

Redis 7 可选，用于统计缓存等能力。
DeepSeek API Key 通过环境变量配置，不写入代码仓库。
```

组件职责：

- 前端：Vue 3 项目执行 `npm run build` 后生成静态文件，由 Nginx 托管。
- 后端：Spring Boot 3 项目打成 jar 包后运行，默认端口 `8080`。
- MySQL：存储用户、题库、答题、AI 面试、分享记录等业务数据。
- Redis：用于缓存用户统计、管理员统计等数据；演示环境可以按需启用。
- DeepSeek：通过环境变量开启真实 AI 调用；不配置时可使用 Mock 模式兜底。

## 本地打包流程

后端打包：

```powershell
.\mvnw.cmd clean package -DskipTests
```

生成位置：

```text
target/*.jar
```

如果需要先跑测试：

```powershell
.\mvnw.cmd test
```

前端打包：

```powershell
cd frontend
npm install
npm.cmd run build
```

生成位置：

```text
frontend/dist
```

部署时通常上传：

- 后端 jar：`target/*.jar`
- 前端静态文件：`frontend/dist/*`
- SQL 脚本：`sql/init.sql`、`sql/test-data.sql`

## 服务器环境要求

基础要求：

- JDK 17+
- MySQL 8+
- Nginx
- Maven Wrapper，无需服务器全局安装 Maven
- Node.js 18+，仅在服务器上构建前端时需要
- Redis 7，可选

Maven Wrapper 使用说明：

```powershell
.\mvnw.cmd -DskipTests compile
.\mvnw.cmd clean package -DskipTests
```

Linux / macOS：

```bash
./mvnw -DskipTests compile
./mvnw clean package -DskipTests
```

如果前端已经在本地构建完成，服务器可以不安装 Node.js，只上传 `frontend/dist`。

## 后端部署步骤

### 1. 上传 jar

示例目录：

```text
/opt/ai-interview/backend/ai-interview-platform-backend.jar
```

### 2. 配置环境变量

最小配置示例：

```bash
export SERVER_PORT=8080
export MYSQL_URL="jdbc:mysql://127.0.0.1:3306/ai_interview_platform?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true"
export MYSQL_USERNAME="root"
export MYSQL_PASSWORD="your_mysql_password"
export REDIS_HOST="127.0.0.1"
export REDIS_PORT="6379"
export JWT_SECRET="replace-with-a-long-random-secret"
```

Mock 模式：

```bash
export APP_AI_PROVIDER="mock"
java -jar ai-interview-platform-backend.jar
```

DeepSeek 模式：

```bash
export APP_AI_PROVIDER="deepseek"
export DEEPSEEK_API_KEY="your_deepseek_api_key"
java -jar ai-interview-platform-backend.jar
```

### 3. 后台运行示例

简单方式：

```bash
nohup java -jar ai-interview-platform-backend.jar > app.log 2>&1 &
```

查看日志：

```bash
tail -f app.log
```

建议检查日志中的关键信息：

```text
Started AiInterviewApplication
System final AI Provider = ...
selectedService=DeepSeekAiService 或 selectedService=MockAiService
```

生产环境可以进一步使用 systemd、Docker 或进程管理工具托管。

## 前端部署步骤

### 1. 构建前端

```powershell
cd frontend
npm.cmd run build
```

构建产物：

```text
frontend/dist
```

将 `dist` 目录内容上传到服务器，例如：

```text
/usr/share/nginx/html/ai-interview
```

### 2. Nginx 配置示例

```nginx
server {
    listen 80;
    server_name example.com;

    root /usr/share/nginx/html/ai-interview;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header Authorization $http_authorization;
    }

    location /swagger-ui/ {
        proxy_pass http://127.0.0.1:8080/swagger-ui/;
    }

    location /v3/api-docs {
        proxy_pass http://127.0.0.1:8080/v3/api-docs;
    }
}
```

重新加载 Nginx：

```bash
nginx -t
sudo systemctl reload nginx
```

如果前端 `.env.production` 使用同域代理，推荐配置：

```text
VITE_API_BASE_URL=
```

或让前端请求 `/api`，由 Nginx 代理到后端。

## 数据库初始化

### 1. 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS ai_interview_platform
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

### 2. 导入表结构

```bash
mysql -u root -p ai_interview_platform < sql/init.sql
```

### 3. 导入演示数据

```bash
mysql -u root -p ai_interview_platform < sql/test-data.sql
```

演示账号：

```text
普通用户：demo_user / admin123
管理员：admin / admin123
```

密码在数据库中为 BCrypt 密文。

### 4. 旧库升级说明

如果已有旧数据库，不建议直接覆盖数据。推荐做法：

1. 先备份旧库。
2. 对比 `sql/init.sql` 中新增字段和索引。
3. 编写单独的 migration 脚本执行 `ALTER TABLE`。
4. 在测试库验证后再应用到正式库。

当前仓库中的 `init.sql` 更适合全新环境初始化。

## 常见问题

### 后端启动失败

检查：

- JDK 是否为 17+
- jar 文件是否完整
- `SERVER_PORT` 是否被占用
- `MYSQL_URL`、`MYSQL_USERNAME`、`MYSQL_PASSWORD` 是否正确
- 日志中是否有表不存在或字段不存在

### 数据库连接失败

检查：

- MySQL 服务是否启动
- 数据库名是否为 `ai_interview_platform`
- 用户是否有访问权限
- MySQL 是否允许当前服务器连接
- JDBC URL 是否包含 `allowPublicKeyRetrieval=true`

### 前端请求 404

常见原因：

- Nginx 没有配置 `try_files $uri $uri/ /index.html`
- Vue Router history 模式刷新时没有回退到 `index.html`
- `/api` 没有正确代理到后端

### 跨域问题

如果前端和后端不同域名或端口，需要检查：

- 后端 CORS 配置是否允许当前前端域名
- Spring Security 是否启用了 CORS
- OPTIONS 预检请求是否允许匿名访问
- Nginx 是否把 `Authorization` 请求头转发给后端

同域 Nginx 反向代理部署可以减少跨域问题。

### DeepSeek 仍显示 MOCK

检查：

- 是否设置 `APP_AI_PROVIDER=deepseek`
- 是否设置 `DEEPSEEK_API_KEY`
- 启动后是否重启了 Java 进程
- IDEA 或服务器进程是否真的读取到了环境变量
- 日志中最终 Provider 是否为 `deepseek`

如果 API Key 缺失，系统会 fallback 到 Mock，保证演示流程不崩。

### API Key 不要写进代码

不要把真实 `DEEPSEEK_API_KEY` 写入：

- `application.yml`
- `application-example.yml`
- README
- 前端代码
- Git commit

推荐只通过环境变量、服务器密钥管理或 CI/CD secret 注入。

### Nginx 代理路径配置错误

如果前端请求 `/api/auth/login`，Nginx 应转发到：

```text
http://127.0.0.1:8080/api/auth/login
```

注意 `proxy_pass` 末尾斜杠会影响路径拼接。修改后使用：

```bash
nginx -t
sudo systemctl reload nginx
```

## 安全提醒

- 不要提交真实 DeepSeek API Key。
- 演示账号仅用于本地或演示环境。
- 生产环境必须修改默认密码。
- `application-example.yml` 只放模板和占位配置。
- `JWT_SECRET` 应使用足够长的随机字符串。
- Nginx 正式部署建议启用 HTTPS。
- 数据库不要直接暴露到公网。
- 管理员接口必须保留后端角色校验，不要只依赖前端菜单隐藏。

## 面试讲解建议

可以这样解释部署设计：

```text
前端构建后由 Nginx 托管，后端以 Spring Boot jar 运行，Nginx 负责静态资源和 /api 反向代理。MySQL 保存业务数据，Redis 用于缓存。AI Provider 通过环境变量切换，DeepSeek 不可用时可以 fallback 到 Mock，保证演示和测试流程稳定。
```
