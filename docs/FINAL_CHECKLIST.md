# Final Checklist

本文档用于项目提交、GitHub 展示、面试演示或答辩前的最终检查。本轮清单不涉及功能开发，只用于确认项目处于稳定可演示状态。

## 1. 后端启动检查

- [ ] 已安装 JDK 17+
- [ ] 项目根目录存在 `mvnw.cmd` 和 `mvnw`
- [ ] 后端编译通过

```powershell
.\mvnw.cmd -DskipTests compile
```

- [ ] 后端测试通过

```powershell
.\mvnw.cmd test
```

- [ ] 后端可正常启动

```powershell
.\mvnw.cmd spring-boot:run
```

- [ ] 健康检查正常

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

## 2. 前端启动检查

- [ ] 已安装 Node.js 18+
- [ ] 已进入前端目录

```powershell
cd frontend
```

- [ ] 已安装依赖

```powershell
npm install
```

- [ ] 前端可正常启动

```powershell
npm run dev
```

- [ ] 前端构建通过

```powershell
npm.cmd run build
```

- [ ] 浏览器可访问前端地址

```text
http://localhost:5173
http://localhost:5174
```

## 3. MySQL 数据库检查

- [ ] Docker 已启动
- [ ] MySQL 容器已启动

```powershell
docker compose up -d mysql
```

- [ ] 数据库连接配置正确

```text
MYSQL_URL
MYSQL_USERNAME
MYSQL_PASSWORD
```

- [ ] `sql/init.sql` 已正确执行
- [ ] 核心表存在

```text
sys_user
sys_role
sys_user_role
question
question_tag
question_tag_relation
user_answer_record
user_favorite
user_wrong_question
daily_learning_record
ai_review_record
```

- [ ] 如需演示数据，已导入 `sql/test-data.sql`
- [ ] 管理员测试账号可用

```text
username: admin
password: admin123
```

## 4. DeepSeek 环境变量检查

- [ ] 已配置 DeepSeek API Key

```powershell
$env:DEEPSEEK_API_KEY="your_deepseek_api_key"
```

- [ ] 已配置 AI Provider

```powershell
$env:APP_AI_PROVIDER="deepseek"
```

- [ ] IDEA Run Configuration 已配置环境变量

```text
APP_AI_PROVIDER=deepseek;DEEPSEEK_API_KEY=your_deepseek_api_key
```

- [ ] 或已配置 VM options

```text
-Dapp.ai.provider=deepseek
```

- [ ] 启动日志出现最终 Provider

```text
System final AI Provider = deepseek
```

## 5. Mock / DeepSeek 切换检查

Mock 模式：

- [ ] 设置 Provider 为 Mock

```powershell
$env:APP_AI_PROVIDER="mock"
```

- [ ] 单题 AI 点评返回 `modelName=MOCK`
- [ ] 无需配置 `DEEPSEEK_API_KEY`

DeepSeek 模式：

- [ ] 设置 Provider 为 DeepSeek

```powershell
$env:APP_AI_PROVIDER="deepseek"
$env:DEEPSEEK_API_KEY="your_deepseek_api_key"
```

- [ ] 单题 AI 点评返回 `modelName=deepseek-chat`
- [ ] `rawResponse` 包含 DeepSeek `chat.completion`
- [ ] 日志出现：

```text
selectedService=DeepSeekAiService
DeepSeek request sending
DeepSeek response received: status=200
```

Fallback 检查：

- [ ] 当 `APP_AI_PROVIDER=deepseek` 但 `DEEPSEEK_API_KEY` 为空时，系统 fallback 到 Mock
- [ ] 日志说明 fallback 原因

## 6. AI 面试完整流程检查

- [ ] 用户可以登录
- [ ] 可以进入 AI 面试助手页面
- [ ] 可以创建模拟面试
- [ ] 可以进入面试会话页
- [ ] 可以查看题目列表
- [ ] 可以输入每题回答
- [ ] 可以调用 AI 追问
- [ ] 可以提交整场面试
- [ ] 可以查看总分
- [ ] 可以查看每题评分
- [ ] 可以查看优点、不足、改进建议和参考答案
- [ ] 低质量回答不会得到异常高分
- [ ] AI 面试历史列表可查看
- [ ] AI 面试详情页可查看

重点验证低质量回答：

```text
我不会
不知道
嗯
对
空回答
```

期望：

- 空回答不超过 5 分
- 明显无效回答不超过 15 分
- 泛泛回答不超过 50 分

## 7. PDF 导出检查

- [ ] 完成一场 AI 面试
- [ ] 进入结果页
- [ ] 点击下载 PDF 报告
- [ ] 浏览器开始下载
- [ ] PDF 可以正常打开
- [ ] PDF 中文不乱码
- [ ] PDF 包含以下内容：

```text
面试岗位
面试时间
总分
优点
不足
改进建议
参考答案
每题评分
追问记录
```

## 8. 分享链接检查

- [ ] 完成一场 AI 面试
- [ ] 点击生成分享链接
- [ ] 分享链接成功生成
- [ ] 复制链接到无登录浏览器窗口
- [ ] 可以公开访问报告
- [ ] 分享页只读
- [ ] 不暴露用户敏感信息
- [ ] token 随机不可预测

## 9. 成长分析检查

- [ ] 用户至少完成一场 AI 面试
- [ ] 进入成长分析页面
- [ ] 平均分显示正常
- [ ] 面试次数显示正常
- [ ] 成长趋势图显示正常
- [ ] 能力雷达图显示正常
- [ ] 无历史数据时显示空状态，不报错

## 10. 管理后台检查

- [ ] 管理员可以登录
- [ ] 普通用户不能进入管理后台
- [ ] 后台首页统计正常
- [ ] 用户管理列表正常
- [ ] 题库管理列表正常
- [ ] 可以新增题目
- [ ] 可以编辑题目
- [ ] 可以删除题目
- [ ] 收藏记录列表正常
- [ ] 错题记录列表正常
- [ ] 答题记录列表正常
- [ ] 答题记录详情弹窗正常
- [ ] AI 面试记录列表正常
- [ ] AI 面试记录详情弹窗正常
- [ ] 普通用户访问 `/api/admin/**` 返回统一 JSON 403

## 11. GitHub 提交检查

- [ ] `README.md` 内容清晰，没有乱码
- [ ] `docs` 文档完整
- [ ] 没有提交真实 API Key
- [ ] 没有提交 `.env` 中的真实密码
- [ ] 没有无关临时文件
- [ ] 没有多余日志文件
- [ ] 没有构建产物误提交
- [ ] 执行查看变更

```powershell
git status --short
git diff --stat
```

- [ ] commit message 清晰

推荐：

```text
docs: add final project checklist
```

## 12. 面试演示前检查

- [ ] 后端已启动
- [ ] 前端已启动
- [ ] MySQL 和 Redis 已启动
- [ ] 管理员账号可登录
- [ ] 普通用户账号可登录
- [ ] DeepSeek 或 Mock 模式已确认
- [ ] Swagger 可打开
- [ ] 浏览器已准备用户端页面
- [ ] 浏览器已准备管理员后台页面
- [ ] 已准备一条可演示的题目
- [ ] 已准备一场 AI 面试记录
- [ ] PDF 下载可用
- [ ] 分享链接可用
- [ ] 成长分析页面可用
- [ ] 演示脚本已打开

建议演示顺序：

1. 登录普通用户
2. 查看题库并提交答案
3. 查看错题本和答题历史
4. 创建 AI 模拟面试
5. 展示 AI 评分和追问
6. 导出 PDF
7. 生成分享链接
8. 查看成长分析
9. 切换管理员后台
10. 展示统计和记录管理

