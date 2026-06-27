# Interview Q&A Guide

本文档用于面试前准备，重点回答项目设计、技术选型、AI 模块、评分系统、前端、数据库、工程化和扩展方向。回答尽量保持真实、清晰，不夸大项目能力。

## 项目整体类问题

## 这个项目是做什么的？

简洁标准回答：

这是一个 AI 面试训练平台，主要帮助 Java 后端学习者完成刷题、答题复盘、AI 模拟面试、报告生成和成长分析。

深入回答：

项目分为用户端和管理员端。用户端可以注册登录、刷题、收藏题目、查看错题本、查看答题历史和学习统计，还可以使用 AI 模拟面试。管理员端可以管理用户、题库、答题记录、错题记录和 AI 面试记录。AI 部分支持 Mock 和 DeepSeek，可以在没有真实 API Key 的情况下演示，也可以配置 DeepSeek 走真实模型。

面试加分点：

可以强调这不是单点功能，而是从刷题到 AI 面试再到报告和成长分析的闭环。

## 和普通 CRUD 项目有什么区别？

简洁标准回答：

普通 CRUD 主要是增删改查，这个项目在 CRUD 基础上加入了权限、统计、AI 调用、评分引擎、PDF 导出和分享等完整业务流程。

深入回答：

题库和用户管理确实有 CRUD，但项目重点不只是表单和列表。我做了 JWT 鉴权、管理员权限、答题记录、错题状态流转、Redis 缓存、AI Provider 抽象、DeepSeek 接入、ScoreEngine 评分统一、AI 多轮追问、PDF 报告和公开分享。这些更接近一个真实业务系统会遇到的问题。

面试加分点：

可以说 CRUD 是基础，但项目亮点在“AI 能力如何工程化接入业务系统”。

## 项目的核心业务流程是什么？

简洁标准回答：

核心流程是用户登录后刷题，系统记录答题结果和错题，再进入 AI 模拟面试，最后生成评分报告和成长分析。

深入回答：

用户先登录进入题库，选择题目进行练习。客观题提交后自动判题，答错会进入错题本，答题行为会形成历史记录和学习统计。用户也可以创建 AI 模拟面试，系统生成题目，用户提交整场回答后，AI 给出评分、优点、不足、建议和参考答案。结果可以导出 PDF，也可以生成分享链接。

面试加分点：

可以用“练习 -> 复盘 -> 模拟面试 -> 报告 -> 成长分析”这条线来讲，逻辑更清楚。

## 这个项目最大的亮点是什么？

简洁标准回答：

最大的亮点是 AI 面试模块的工程化设计，包括 AiServiceFactory、DeepSeek / Mock 切换和 ScoreEngine 统一评分。

深入回答：

我没有把 DeepSeek 调用直接写死在业务代码里，而是抽象了 AiService，通过 AiServiceFactory 选择 Mock 或 DeepSeek。评分方面，我也没有直接相信大模型给出的分数，而是设计了 ScoreEngine 统一评分入口，对低质量回答做封顶，保证单题点评、整场面试、PDF、分享页和成长分析分数一致。

面试加分点：

可以强调“可运行、可回退、可观测、可维护”，这是 AI 接入业务系统时很重要的点。

## 这个项目的难点在哪里？

简洁标准回答：

难点主要在 AI 调用结果不稳定、评分逻辑统一、权限隔离和前后端完整联调。

深入回答：

AI 返回不一定完全符合预期，所以需要保存 rawResponse，也需要 structuredResult 做页面展示。评分也不能完全交给模型，否则“我不会”这种回答可能拿到不合理高分，所以我做了 ScoreEngine。另一个难点是模块很多，用户端、管理员端、AI 面试、PDF、分享和成长分析都要保证接口字段一致。

面试加分点：

可以主动说明当前是原型系统，不是生产级 SaaS，但已经考虑了可扩展和可维护性。

## 后端技术问题

## 为什么使用 Spring Boot？

简洁标准回答：

Spring Boot 适合快速搭建 Java 后端项目，生态成熟，和 Spring Security、MyBatis-Plus、Redis、Swagger 集成方便。

深入回答：

这个项目需要用户认证、权限控制、REST API、数据库访问、缓存和测试。Spring Boot 可以减少大量基础配置，让我把重点放在业务模块和 AI 能力上。它也适合简历项目，因为企业里 Java 后端常用这一套技术栈。

面试加分点：

可以说选择 Spring Boot 是为了工程效率和生态，而不是为了追新技术。

## Controller、Service、Mapper 分别负责什么？

简洁标准回答：

Controller 负责接收请求和返回结果，Service 负责业务逻辑，Mapper 负责数据库访问。

深入回答：

Controller 不直接操作数据库，只做参数校验、权限入口和调用 Service。Service 处理业务规则，比如注册时判断用户名重复、提交答案时判题、AI 面试提交时计算分数。Mapper 对应 SQL 或 MyBatis-Plus 的数据库操作。

面试加分点：

可以强调分层能降低耦合，也更方便测试和后续维护。

## DTO、VO、Entity 的区别是什么？

简洁标准回答：

DTO 用于接收请求参数，VO 用于返回给前端，Entity 对应数据库表结构。

深入回答：

比如注册请求用 DTO，里面有用户名、密码、确认密码；用户信息返回用 VO，不会返回密码；数据库里的 sys_user 对应 Entity，会有 password、deleted、create_time 等字段。这样可以避免数据库字段直接暴露给前端。

面试加分点：

可以说 DTO/VO/Entity 分离能减少敏感字段泄露，也能让接口和数据库演进相对独立。

## 为什么不能把业务逻辑直接写在 Controller？

简洁标准回答：

Controller 写业务会让代码难维护、难测试，也会破坏分层。

深入回答：

如果 Controller 里直接查数据库、判题、写缓存、调 AI，后面接口变多后会非常混乱。放到 Service 后，Controller 只关心 HTTP，Service 负责业务，Mapper 负责数据，这样同一个业务逻辑也可以被多个接口复用。

面试加分点：

可以举例：AI 点评和保存历史记录都属于业务逻辑，应该放 Service，不应该写在 Controller。

## MyBatis-Plus 在项目中起什么作用？

简洁标准回答：

MyBatis-Plus 简化了基础 CRUD、分页、条件查询和逻辑删除。

深入回答：

项目里很多表都有 `deleted` 字段，MyBatis-Plus 可以统一处理逻辑删除。分页查询题库、答题历史、AI 历史、管理员列表也可以用 MyBatis-Plus 的分页能力。对于复杂统计 SQL，仍然可以写自定义 Mapper 方法。

面试加分点：

可以说明不是所有 SQL 都交给自动 CRUD，复杂统计还是要写聚合查询。

## JWT 鉴权流程是怎样的？

简洁标准回答：

用户登录成功后后端生成 JWT，前端保存 token，后续请求放到 Authorization Header，后端过滤器解析 token 并设置登录态。

深入回答：

登录接口验证用户名和 BCrypt 密码，成功后返回 JWT。前端 Axios 请求拦截器自动加 `Authorization: Bearer token`。后端 JWT Filter 解析 token，加载用户和角色，放入 SecurityContext。没有 token 或 token 无效时返回统一 JSON 错误。

面试加分点：

可以强调项目是无状态鉴权，适合前后端分离。

## 管理员权限如何控制？

简洁标准回答：

管理员接口统一走 `/api/admin/**`，需要登录并拥有 ADMIN 角色。

深入回答：

用户登录后会加载角色列表，Spring Security 根据角色进行权限判断。普通用户访问管理员接口时会返回统一 JSON 403，不会跳转到 HTML 登录页。后续如果要细化权限，可以在方法上继续使用 `@PreAuthorize`。

面试加分点：

可以说明路径权限和方法权限可以结合使用，当前项目先按角色做隔离。

## AI 模块问题

## DeepSeek 是怎么接入的？

简洁标准回答：

通过 DeepSeekAiService 使用 RestTemplate 调用 DeepSeek Chat Completions API。

深入回答：

DeepSeekAiService 会构造 system prompt 和 user prompt，请求 `https://api.deepseek.com/chat/completions`。API Key 从环境变量读取，不写死在代码里。返回后读取 `choices[0].message.content`，尝试解析为结构化 JSON，再交给 ScoreEngine 得到最终分。

面试加分点：

可以说明请求日志会记录是否发出请求和响应状态，但不会打印 API Key。

## AiServiceFactory 为什么要设计？

简洁标准回答：

为了让业务层不用关心具体 AI Provider，实现 Mock 和 DeepSeek 的可切换。

深入回答：

业务层只依赖 AiService 接口，AiServiceFactory 根据配置选择 MockAiService 或 DeepSeekAiService。这样本地没有 API Key 时可以用 Mock，演示真实 AI 时切到 DeepSeek。如果以后接入 OpenAI 或其他模型，只需要新增实现类，不需要大改业务代码。

面试加分点：

可以说这是策略模式或工厂模式在项目里的实际应用。

## MockAiService 有什么作用？

简洁标准回答：

MockAiService 用于本地开发、测试和 DeepSeek 不可用时的 fallback。

深入回答：

AI 接口依赖外部服务和 API Key。如果每次测试都依赖真实模型，成本高也不稳定。MockAiService 可以保证项目没有网络或没有 key 时仍然能跑完整流程，也让自动化测试更稳定。

面试加分点：

可以强调 Mock 不是为了假装 AI，而是为了提高项目可运行性和可测试性。

## DeepSeek 不可用时系统怎么办？

简洁标准回答：

如果 DeepSeek Provider 不可用，系统会 fallback 到 MockAiService，并输出明确日志。

深入回答：

当配置了 deepseek 但缺少 `DEEPSEEK_API_KEY` 时，AiServiceFactory 会判断 DeepSeekAiService 不可用，选择 MockAiService。这样不会因为外部配置缺失导致核心流程直接崩掉。如果 DeepSeek 请求失败，接口会返回友好错误或由业务 fallback 逻辑处理。

面试加分点：

可以说明 fallback 是原型系统稳定演示的关键。

## AI 返回内容不稳定怎么处理？

简洁标准回答：

后端会先尝试解析结构化结果，失败时保留 rawResponse，不让系统崩溃。

深入回答：

Prompt 会要求 DeepSeek 只返回 JSON，但模型输出仍可能有格式偏差。所以系统保存 rawResponse，同时解析 structuredResult。如果解析失败，structuredResult 可以为空，页面仍可展示原始结果或友好提示。

面试加分点：

可以说大模型输出不能完全当成强类型接口，后端必须做容错。

## Prompt 是怎么设计的？

简洁标准回答：

Prompt 会明确角色、题目、用户回答、评分要求和 JSON 输出格式。

深入回答：

比如单题点评会告诉模型它是严格 Java 面试官，输入题目和用户回答，要求输出 score、strengths、weaknesses、suggestions、referenceAnswer。追问 Prompt 会限制每次只问一个问题，不重复历史问题，并逐步深入。

面试加分点：

可以强调 Prompt 只是第一层约束，后端 ScoreEngine 才是最终评分兜底。

## rawResponse 和 structuredResult 为什么都要保存？

简洁标准回答：

structuredResult 方便前端展示，rawResponse 方便排查和兼容模型输出。

深入回答：

structuredResult 是解析后的结构化字段，比如分数、优点、不足、建议。rawResponse 是模型完整原始返回，如果解析失败或以后要调整解析规则，可以回看原始内容。两者一起保存更利于调试和兼容。

面试加分点：

可以说这是处理 AI 不确定性的一种工程化手段。

## ScoreEngine 评分系统问题

## 为什么要重构 ScoreEngine？

简洁标准回答：

因为评分逻辑原来分散在多个类里，容易不一致，也不方便维护和测试。

深入回答：

单题点评、整场面试、Mock、DeepSeek 都可能产生分数。如果每个地方都自己处理封顶和归一化，就容易出现同样回答在不同入口分数不同。ScoreEngine 把最终评分统一到一个入口，所有 score 都从这里产出。

面试加分点：

可以强调这是从“能跑”到“可维护”的重构。

## AiScoreContext 的作用是什么？

简洁标准回答：

AiScoreContext 是评分上下文，用来把原始分、用户回答、题型和 fallback 状态传给 ScoreEngine。

深入回答：

ScoreEngine 不直接依赖具体业务 Service，而是依赖 AiScoreContext。这样它可以服务单题点评，也可以服务整场面试。上下文里包括 deepseekScore、userAnswer、questionType、isFallback 和 referencePoints。

面试加分点：

可以说上下文对象让评分引擎更独立，也方便单元测试。

## DeepSeek 原始分和最终分有什么区别？

简洁标准回答：

DeepSeek 原始分是模型给出的分，最终分是后端 ScoreEngine 校验后的分。

深入回答：

模型可能因为表达礼貌或上下文不足给出偏高分，所以不能完全信任。ScoreEngine 会基于回答质量做二次校验，比如空回答、无效回答、泛泛回答会被封顶。最终保存和展示的都是最终分。

面试加分点：

可以说明 AI 系统里模型输出和业务结果最好分层处理。

## 为什么回答“我不会”不能让 AI 自己评分？

简洁标准回答：

因为大模型可能给出安慰式高分，业务上不合理，所以后端必须兜底。

深入回答：

真实面试里“我不会”应该是很低分。如果完全依赖模型，有时模型会为了鼓励用户给 40 分甚至更高。ScoreEngine 会识别这类无效回答，最高封顶到 15 分，避免用户看到不真实的评分。

面试加分点：

可以强调这是用规则约束 AI 输出，保证业务体验稳定。

## 低质量回答封顶是怎么做的？

简洁标准回答：

通过判断空回答、无效短语、有效字符长度、技术关键词和解释内容来封顶。

深入回答：

空回答最高 5 分；“我不会”“不知道”“嗯”“对”等明显无效回答最高 15 分；很短且没有技术关键词的回答也会封顶；泛泛而谈没有关键技术点的回答最高 50 分。只有包含技术关键词并且有解释、原理或案例时才允许高分。

面试加分点：

可以说规则不是替代 AI，而是给 AI 评分加业务边界。

## totalScore 是怎么计算的？

简洁标准回答：

totalScore 是整场面试每道题最终分的平均值。

深入回答：

每题先通过 ScoreEngine 得到最终分，然后系统对 questionResults.score 做平均，得到 totalScore。这样 totalScore 不会使用封顶前的原始分，能保证整场结果和每题结果一致。

面试加分点：

可以强调 totalScore 来源透明，不是单独再让模型生成一个不可控总分。

## 如何保证 PDF、分享页、成长系统分数一致？

简洁标准回答：

这些模块都读取保存后的最终面试结果，不重新计算分数。

深入回答：

面试提交时，ScoreEngine 已经生成最终分并保存到 ai_review_record 的 result_content 和 score 字段。PDF、分享页和成长系统都是读取这个保存结果，所以不会出现页面一个分、PDF 一个分的情况。

面试加分点：

可以说数据只在一个地方计算，多个展示端复用结果，减少不一致。

## traceId 日志有什么作用？

简洁标准回答：

traceId 用于定位每一次评分过程，方便排查评分为什么被修正。

深入回答：

ScoreEngine 每次计算都会打印 traceId、输入分、最终分和修正原因。比如 DeepSeek 给了 80，但回答是“我不会”，日志会显示 finalScore 被封顶到 15，原因是 clearly invalid answer。

面试加分点：

可以说这是基础可观测性，方便后续接入链路追踪或日志平台。

## 前端问题

## Vue3 项目结构怎么设计？

简洁标准回答：

前端按 api、types、stores、router、layouts、views、utils 拆分。

深入回答：

api 负责接口封装，types 定义 TypeScript 类型，stores 用 Pinia 管理用户状态，router 做路由和权限守卫，layouts 是通用布局，views 是页面，utils 放格式化和工具函数。这样页面不会直接散落 Axios 请求，类型也更清楚。

面试加分点：

可以强调前端也做了分层，而不是把所有逻辑写在 Vue 文件里。

## Pinia 用来做什么？

简洁标准回答：

Pinia 用来保存登录 token、当前用户信息和角色列表。

深入回答：

用户登录成功后，前端把 token 和用户信息写入 Pinia，并同步到 localStorage。页面刷新后可以恢复登录状态。退出登录时清空 Pinia 和本地 token。

面试加分点：

可以说 Pinia 让登录态在多个页面间共享，不需要层层传参。

## Axios 如何封装？

简洁标准回答：

Axios 统一设置 baseURL、Authorization Header 和响应拦截。

深入回答：

请求拦截器会自动加 `Authorization: Bearer token`。响应拦截器统一处理后端 `{ code, message, data }` 结构。如果遇到 401 或 token 失效，会清理本地登录态并跳转登录页。

面试加分点：

可以强调页面不直接写重复请求逻辑，统一封装更方便维护。

## 前端如何处理登录态？

简洁标准回答：

通过 Pinia + localStorage + 路由守卫处理。

深入回答：

登录后保存 token，刷新页面时从 localStorage 恢复。访问需要登录的页面时，路由守卫检查 token；未登录跳转登录页。管理员页面还会检查角色是否包含 ADMIN。

面试加分点：

可以说明前端守卫只是体验层，真正权限仍以后端为准。

## AI 面试页面如何实现多轮对话？

简洁标准回答：

前端维护当前题目、用户回答、历史对话和 AI 追问列表，每轮调用追问接口更新会话。

深入回答：

用户回答当前问题后，前端把 question、answer 和 history 传给 `/api/ai/interview/next-question`。后端返回 nextQuestion 后，前端追加到对话记录中，形成类似聊天的多轮面试体验。

面试加分点：

可以说多轮追问没有重新设计复杂状态机，而是用当前题目和历史对话做轻量会话管理。

## ECharts 用在哪里？

简洁标准回答：

ECharts 用在学习趋势、错题分析、AI 能力雷达和成长分析里。

深入回答：

Dashboard 展示最近学习趋势和高频错题知识点；AI 结果页展示能力雷达图；成长分析页展示历史面试分数趋势和长期能力雷达。

面试加分点：

可以说图表不是装饰，而是帮助用户理解学习和面试表现。

## PDF 下载前端如何处理？

简洁标准回答：

前端以 blob 方式请求 PDF 接口，然后创建下载链接。

深入回答：

点击下载按钮后，前端调用 `GET /api/ai/interview/{id}/export-pdf`，响应类型设置为 blob。拿到数据后创建 Object URL，再触发浏览器下载，文件名类似 `AI面试报告_{id}.pdf`。

面试加分点：

可以说明 PDF 内容由后端生成，前端只负责下载体验。

## 数据库问题

## 核心表有哪些？

简洁标准回答：

核心表包括用户角色表、题库标签表、答题收藏错题表、学习记录表和 AI 记录表。

深入回答：

用户权限相关有 sys_user、sys_role、sys_user_role；题库相关有 question、question_tag、question_tag_relation；答题相关有 user_answer_record、user_favorite、user_wrong_question、daily_learning_record；AI 相关主要是 ai_review_record。

面试加分点：

可以按业务域回答，而不是机械背表名。

## ai_review_record 保存什么？

简洁标准回答：

它保存 AI 点评、模拟面试、弱点总结、分享和企业面试相关记录。

深入回答：

表里有 user_id、question_id、record_type、input_content、result_content、score、model_name 等字段。input_content 保存输入参数，result_content 保存 AI 结果 JSON，score 保存最终分数，model_name 标记 Mock 或 DeepSeek。

面试加分点：

可以说这个表是 AI 模块的核心历史表。

## 为什么 result_content 使用 JSON？

简洁标准回答：

因为 AI 结果结构比较灵活，JSON 更方便保存不同类型记录。

深入回答：

单题点评、整场面试、弱点总结、企业面试结果字段不完全一样。如果每种都拆成很多表，原型阶段复杂度会很高。使用 JSON 可以快速支持不同结构，同时前端展示也比较方便。

面试加分点：

可以补充：如果后续高频查询某些字段，可以再拆字段或建索引。

## 分享链接 token 如何设计？

简洁标准回答：

分享链接使用随机 token 绑定面试记录，并通过 is_public 控制是否公开。

深入回答：

生成分享链接时会生成不可预测的 token，保存到记录中。公开访问时只根据 token 查询已公开记录，不需要登录，也不返回用户敏感信息。

面试加分点：

可以说 token 不能用自增 id，否则容易被枚举。

## 企业面试字段如何扩展？

简洁标准回答：

通过 interviewer_type、enterprise_score 等字段保存企业面试风格和评分维度。

深入回答：

企业面试目前是原型能力，支持不同面试官风格和评分模型。后续如果要做 SaaS，可以把企业、岗位模板、评分权重拆成独立表，再关联用户和面试记录。

面试加分点：

可以说明当前是平滑扩展，不是一开始就做复杂多租户。

## 数据库升级脚本有什么作用？

简洁标准回答：

SQL 脚本用于初始化表结构、索引、角色和测试数据。

深入回答：

`sql/init.sql` 用来创建基础表和索引，`sql/test-data.sql` 用来导入测试账号、标签和题目。测试环境也有 schema-test 和 data-test，保证自动化测试不依赖本地 MySQL。

面试加分点：

可以说脚本化能降低环境搭建成本。

## 工程化问题

## Maven Wrapper 有什么作用？

简洁标准回答：

Maven Wrapper 可以让项目不用依赖本机预装 Maven，也能用固定 Maven 版本构建。

深入回答：

项目里有 `mvnw.cmd` 和 `mvnw`，Windows 可以直接执行 `mvnw.cmd test`。这样别人拉代码后，只要有 JDK，就能用项目指定的 Maven 版本编译和测试。

面试加分点：

可以说 Maven Wrapper 提升了项目可复现性。

## 为什么需要 application-example.yml？

简洁标准回答：

它提供配置模板，告诉使用者需要哪些环境变量，但不保存真实密码。

深入回答：

项目需要 MySQL、Redis、JWT、DeepSeek 等配置。`application-example.yml` 说明字段和默认写法，真实密码和 API Key 通过环境变量传入，避免泄露。

面试加分点：

可以强调配置模板和真实配置分离是基本安全习惯。

## 为什么不能把 DeepSeek API Key 写进代码？

简洁标准回答：

API Key 是敏感信息，写进代码容易泄露。

深入回答：

代码可能上传 GitHub，也可能发给别人。如果把 Key 写死，泄露后别人可以直接调用接口产生费用或风险。正确做法是通过环境变量或配置中心注入。

面试加分点：

可以说 README 中只写变量名，不写真实 Key。

## 本地开发和生产环境如何区分？

简洁标准回答：

通过 profile、环境变量和配置文件区分。

深入回答：

本地可以使用 Mock Provider 和本地 MySQL、Redis；测试环境使用 H2 和 test profile；真实演示或生产可以配置 DeepSeek 和正式数据库。不同环境不应该改代码，只改配置。

面试加分点：

可以说明当前项目是原型，未来可以接入配置中心和容器化部署。

## 前端 build 出现 chunk size warning 是否是错误？

简洁标准回答：

不是错误，只是构建产物较大的提醒。

深入回答：

Vite build 成功才代表构建通过。chunk size warning 是提示某些 JS 包较大，后续可以用动态 import、路由懒加载和 manualChunks 优化，但它不会导致项目不可运行。

面试加分点：

可以说明会区分 warning 和 error，不会把提示当失败。

## 如何验证项目稳定？

简洁标准回答：

通过后端编译、后端测试、前端构建和核心接口手动验证。

深入回答：

后端执行 `mvnw.cmd -DskipTests compile` 和 `mvnw.cmd test`，前端执行 `npm.cmd run build`。另外手动验证登录、题库、刷题、AI 面试、PDF、分享和管理后台。项目还准备了 FINAL_CHECKLIST 做最终检查。

面试加分点：

可以说自动化测试和手动主流程检查结合使用。

## 项目优化与扩展问题

## 后续如何做 SaaS 多租户？

简洁标准回答：

可以增加 tenant_id，把用户、题库、面试记录和配置按租户隔离。

深入回答：

如果做 SaaS，需要新增企业租户表、租户用户关系、租户配置和套餐信息。核心业务表增加 tenant_id，所有查询都带租户条件。管理员也要区分平台管理员和租户管理员。

面试加分点：

可以说明多租户不只是加字段，还包括权限、数据隔离和配置隔离。

## 如何支持更多 AI 模型？

简洁标准回答：

新增 AiService 实现类，并让 AiServiceFactory 根据配置选择即可。

深入回答：

比如接入 OpenAI、通义、智谱，可以各自实现 AiService。业务层不变，只需要配置 provider。不同模型的 prompt 和解析逻辑可以放到各自 Service 中，最终分数仍统一经过 ScoreEngine。

面试加分点：

可以强调 Provider 扩展不应该影响业务流程。

## 如何做企业权限隔离？

简洁标准回答：

需要在用户、角色、题库、面试记录上增加企业或租户维度。

深入回答：

企业用户只能访问自己企业的数据。管理员权限也要分层，比如平台管理员管理所有企业，企业管理员只管理本企业用户和题库。后端需要在查询条件和权限注解中加入租户校验。

面试加分点：

可以说权限隔离要靠后端强制实现，前端隐藏菜单不算安全。

## 如何提升评分准确性？

简洁标准回答：

可以从 Prompt、评分维度、参考答案、样本校准和人工复核几个方向优化。

深入回答：

当前 ScoreEngine 主要做规则兜底。后续可以引入更细的评分 rubric，比如技术准确性、表达结构、项目经验、深度和风险意识。也可以把优秀答案和低分答案作为样例放入 Prompt，或者用人工标注样本做校准。

面试加分点：

可以承认当前评分是原型规则，不夸大为绝对准确。

## 如何做异步 AI 任务？

简洁标准回答：

可以用消息队列把 AI 生成、PDF 生成等耗时任务异步化。

深入回答：

提交面试后先创建任务记录，返回 taskId。后端用 MQ 或线程池异步调用 AI，完成后更新状态。前端轮询或使用 WebSocket 获取结果。这样可以避免接口长时间阻塞。

面试加分点：

可以说当前同步实现适合原型，异步适合并发更高的场景。

## 如何做缓存优化？

简洁标准回答：

对统计类、排行榜类和热门数据做缓存，并在写操作后主动失效。

深入回答：

项目里已经对用户统计和管理员统计做了 Redis 缓存。后续可以细化缓存 key，增加 TTL，避免缓存穿透，也可以对热门题目和标签做预热。写入答题记录、收藏、错题、AI 记录后需要清理相关缓存。

面试加分点：

可以说缓存不是越多越好，重点是读多写少、能接受短暂延迟的数据。

## 如何做日志链路追踪？

简洁标准回答：

可以在请求入口生成 traceId，并贯穿 Controller、Service、AI 调用和 ScoreEngine。

深入回答：

当前 ScoreEngine 已经有评分 traceId。后续可以把 traceId 放入 MDC，让每条日志都带同一个链路 ID。如果接入网关或微服务，也可以接入 OpenTelemetry、Zipkin 或 SkyWalking。

面试加分点：

可以说明当前是局部可观测，后续可以扩展到全链路。

## 如何部署上线？

简洁标准回答：

可以用 Docker 部署 MySQL、Redis、后端和前端，前端用 Nginx 托管，后端通过环境变量注入配置。

深入回答：

后端打成 jar 或镜像，配置 MySQL、Redis、JWT_SECRET、APP_AI_PROVIDER 和 DEEPSEEK_API_KEY。前端执行 build 后把 dist 部署到 Nginx。生产环境需要 HTTPS、日志、备份、监控和敏感配置管理。

面试加分点：

可以说当前项目已具备容器化依赖基础，但还不是完整生产部署方案。

