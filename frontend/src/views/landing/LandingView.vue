<template>
  <div class="landing-page">
    <header class="landing-nav">
      <button class="brand-link" type="button" @click="go('/landing')">
        <span class="brand-mark">AI</span>
        <span>AI Interview Platform</span>
      </button>

      <nav class="nav-links" aria-label="Landing navigation">
        <a href="#features">功能</a>
        <a href="#highlights">亮点</a>
        <a href="#workflow">工作流</a>
        <a href="#architecture">技术架构</a>
        <a href="#demo">演示</a>
      </nav>

      <div class="nav-actions">
        <el-button @click="go('/dashboard')">进入系统</el-button>
        <el-button type="primary" @click="go('/ai-interview')">开始 AI 面试</el-button>
      </div>
    </header>

    <main>
      <section class="hero-section">
        <div class="hero-copy fade-up">
          <span class="landing-eyebrow">AI Interview SaaS Prototype</span>
          <h1>用 AI 进行真实感技术面试训练</h1>
          <p>
            基于 DeepSeek、多轮追问、ScoreEngine 评分引擎和成长分析，构建完整 AI 模拟面试闭环。
          </p>

          <div class="hero-actions">
            <el-button type="primary" size="large" @click="go('/ai-interview')">
              开始 AI 面试
            </el-button>
            <el-button size="large" @click="go('/questions')">查看题库</el-button>
          </div>

          <div class="hero-metrics">
            <div v-for="metric in metrics" :key="metric.label">
              <strong>{{ metric.value }}</strong>
              <span>{{ metric.label }}</span>
            </div>
          </div>
        </div>

        <aside class="hero-demo-card fade-up">
          <div class="demo-window">
            <div class="demo-window__top">
              <span />
              <span />
              <span />
              <strong>AI 面试进行中</strong>
            </div>

            <div class="chat-preview">
              <div class="message message-ai">
                <span>AI 面试官</span>
                <p>请解释 Spring Boot 自动配置的核心原理。</p>
              </div>
              <div class="message message-user">
                <span>候选人</span>
                <p>它通过 starter、条件注解和自动配置类减少手动配置。</p>
              </div>
              <div class="message message-ai">
                <span>AI 追问</span>
                <p>如果某个 Bean 没有生效，你会如何排查条件注解和配置加载链路？</p>
              </div>
            </div>

            <div class="score-preview">
              <div>
                <span>ScoreEngine</span>
                <strong>82</strong>
              </div>
              <p>良好：回答方向正确，建议补充源码流程和项目案例。</p>
            </div>
          </div>
        </aside>
      </section>

      <section id="features" class="landing-section fade-up">
        <div class="section-heading">
          <span class="landing-eyebrow">Product Capabilities</span>
          <h2>完整 AI 面试闭环</h2>
          <p>从题库练习、模拟面试到报告导出，覆盖一次技术面试复盘所需的关键环节。</p>
        </div>

        <div class="feature-grid">
          <article v-for="feature in features" :key="feature.title" class="feature-card">
            <div class="feature-icon">
              <el-icon><component :is="feature.icon" /></el-icon>
            </div>
            <h3>{{ feature.title }}</h3>
            <p>{{ feature.description }}</p>
          </article>
        </div>
      </section>

      <section id="workflow" class="landing-section workflow-section fade-up">
        <div class="section-heading">
          <span class="landing-eyebrow">Workflow</span>
          <h2>从练习到报告，一条路径跑通</h2>
          <p>适合面试演示，也适合日常把一次技术面试拆成可复盘的学习过程。</p>
        </div>

        <div class="workflow-track">
          <article v-for="(step, index) in workflow" :key="step.title" class="workflow-card">
            <span>{{ String(index + 1).padStart(2, '0') }}</span>
            <h3>{{ step.title }}</h3>
            <p>{{ step.description }}</p>
          </article>
        </div>
      </section>

      <section id="architecture" class="landing-section architecture-section fade-up">
        <div class="section-heading">
          <span class="landing-eyebrow">Engineering Highlights</span>
          <h2>适合讲解的工程亮点</h2>
          <p>不是单纯 CRUD，而是围绕 AI Provider、评分稳定性、报告输出和演示回归做了工程化设计。</p>
        </div>

        <div class="highlight-grid">
          <article v-for="item in highlights" :key="item" class="highlight-pill">{{ item }}</article>
        </div>
      </section>

      <section id="highlights" class="landing-section product-showcase fade-up">
        <div class="section-heading">
          <span class="landing-eyebrow">Product Preview</span>
          <h2>用 CSS 模拟核心产品界面</h2>
          <p>不依赖截图资源，直接展示题库、AI 对话和分析报告三类核心体验。</p>
        </div>

        <div class="showcase-grid">
          <article class="mock-panel question-mock">
            <div class="mock-header">
              <strong>题库卡片</strong>
              <span>Java / Spring / DB</span>
            </div>
            <div class="mock-question">
              <h3>Redis 缓存穿透如何解决？</h3>
              <p>请从空值缓存、布隆过滤器、参数校验三个方向说明。</p>
              <div>
                <span>Redis</span>
                <span>中等</span>
                <span>系统设计</span>
              </div>
            </div>
            <div class="mock-question muted">
              <h3>JWT 登录失效如何排查？</h3>
              <p>检查 Token 过期、签名密钥、请求头和服务端鉴权链路。</p>
            </div>
          </article>

          <article class="mock-panel chat-mock">
            <div class="mock-header">
              <strong>AI 对话窗口</strong>
              <span>多轮追问</span>
            </div>
            <div class="mini-chat ai">请说明 MyBatis 一级缓存的作用域。</div>
            <div class="mini-chat user">一级缓存默认是 SqlSession 级别。</div>
            <div class="mini-chat ai">如果同一个查询返回旧数据，你会如何定位？</div>
          </article>

          <article class="mock-panel report-mock">
            <div class="mock-header">
              <strong>结果报告</strong>
              <span>PDF / 分享</span>
            </div>
            <div class="mock-score">
              <strong>86</strong>
              <span>良好</span>
            </div>
            <div class="mock-bars">
              <span style="--bar-width: 88%" />
              <span style="--bar-width: 76%" />
              <span style="--bar-width: 82%" />
            </div>
          </article>
        </div>
      </section>

      <section id="demo" class="demo-cta fade-up">
        <div>
          <span class="landing-eyebrow">Live Demo</span>
          <h2>准备开始一次 AI 模拟面试？</h2>
          <p>建议先从演示题库进入，再创建一场 Java 后端方向的 AI 模拟面试。</p>
        </div>
        <div class="demo-cta__actions">
          <el-button type="primary" size="large" @click="go('/ai-interview')">立即开始</el-button>
          <el-button size="large" @click="go('/questions')">查看演示题库</el-button>
          <el-button size="large" @click="go('/user/growth')">查看成长分析</el-button>
        </div>
      </section>

      <section class="landing-section faq-section fade-up">
        <div class="section-heading">
          <span class="landing-eyebrow">FAQ</span>
          <h2>演示时常见问题</h2>
          <p>把技术方案和兜底逻辑讲清楚，项目展示会更稳。</p>
        </div>

        <el-collapse class="faq-collapse">
          <el-collapse-item
            v-for="item in faqs"
            :key="item.question"
            :title="item.question"
            :name="item.question"
          >
            <p>{{ item.answer }}</p>
          </el-collapse-item>
        </el-collapse>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import {
  ChatDotRound,
  Connection,
  DataAnalysis,
  Document,
  MagicStick,
  Share,
  TrendCharts
} from '@element-plus/icons-vue'

const router = useRouter()

const metrics = [
  { value: '60+', label: '演示题库' },
  { value: '2', label: 'AI Provider' },
  { value: '50', label: '后端测试' }
]

const features = [
  {
    icon: MagicStick,
    title: 'DeepSeek 智能评分',
    description: '接入真实 AI Provider，同时保留 Mock 模式，方便本地演示与稳定回归。'
  },
  {
    icon: ChatDotRound,
    title: '多轮 AI 追问',
    description: '用户回答后继续生成追问题，让模拟面试更接近真实技术面。'
  },
  {
    icon: DataAnalysis,
    title: 'ScoreEngine 统一评分',
    description: '统一收敛原始 AI 分数、低质量回答封顶和最终展示分数。'
  },
  {
    icon: Document,
    title: 'PDF 面试报告',
    description: '面试结束后生成正式报告，包含总分、建议、参考答案和追问记录。'
  },
  {
    icon: Share,
    title: '分享链接',
    description: '将面试报告生成公开只读链接，适合展示学习成果。'
  },
  {
    icon: TrendCharts,
    title: '成长趋势分析',
    description: '基于历史面试分数形成长期能力趋势和雷达画像。'
  }
]

const workflow = [
  { title: '题库练习', description: '通过 Java 后端方向题库建立基础知识储备。' },
  { title: '创建面试', description: '选择岗位、难度、题量和企业面试官风格。' },
  { title: 'AI 追问', description: '围绕当前回答继续深入，形成多轮对话。' },
  { title: '智能评分', description: 'DeepSeek 与 ScoreEngine 共同输出稳定评分。' },
  { title: '生成报告', description: '沉淀总评、每题反馈、优点、不足和建议。' },
  { title: '导出分享', description: '下载 PDF 或生成公开分享链接用于复盘展示。' }
]

const highlights = [
  'Spring Boot + Vue3 前后端分离',
  'DeepSeek / Mock Provider 可切换',
  'ScoreEngine 评分引擎',
  'JWT 鉴权与管理员权限',
  'MySQL + Redis 数据支撑',
  'OpenPDF 报告导出',
  '分享链接公开只读访问',
  '健康检查脚本一键回归'
]

const faqs = [
  {
    question: 'DeepSeek 不可用怎么办？',
    answer: '系统保留 MockAiService 作为兜底 Provider，适合本地演示、断网场景和自动化测试。'
  },
  {
    question: 'Mock 模式有什么用？',
    answer: 'Mock 模式不依赖外部 API Key，可以保证项目在演示和测试时稳定跑通。'
  },
  {
    question: 'ScoreEngine 为什么需要？',
    answer: 'AI 原始评分可能不稳定，ScoreEngine 用统一入口做低质量回答封顶和最终分数收敛。'
  },
  {
    question: '面试报告能否导出？',
    answer: '可以。结果页支持 PDF 下载，也支持生成公开只读分享链接。'
  },
  {
    question: '是否支持企业面试原型？',
    answer: '支持。当前已有企业面试官类型、企业模板和岗位匹配分析的原型能力。'
  }
]

function go(path: string) {
  router.push(path)
}
</script>

<style scoped>
.landing-page {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  background:
    linear-gradient(rgba(37, 99, 235, 0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(37, 99, 235, 0.04) 1px, transparent 1px),
    radial-gradient(circle at 18% 12%, rgba(14, 165, 233, 0.2), transparent 34%),
    radial-gradient(circle at 82% 8%, rgba(124, 58, 237, 0.18), transparent 32%),
    linear-gradient(135deg, #f8fbff 0%, #ffffff 42%, #f5f3ff 100%);
  background-size:
    28px 28px,
    28px 28px,
    auto,
    auto,
    auto;
  color: var(--ai-text-primary);
}

.landing-page::before {
  content: '';
  position: fixed;
  inset: 0;
  pointer-events: none;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.22), rgba(255, 255, 255, 0.82));
}

.landing-nav {
  position: sticky;
  top: 0;
  z-index: 20;
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: var(--ai-space-2);
  max-width: 1180px;
  margin: 0 auto;
  padding: 14px var(--ai-space-3);
  border-bottom: 1px solid rgba(217, 226, 236, 0.76);
  background: rgba(255, 255, 255, 0.76);
  backdrop-filter: blur(18px);
}

.brand-link {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  border: 0;
  background: transparent;
  color: var(--ai-text-primary);
  font: inherit;
  font-weight: 850;
  cursor: pointer;
}

.brand-mark {
  display: grid;
  width: 36px;
  height: 36px;
  place-items: center;
  border-radius: 12px;
  background: linear-gradient(135deg, var(--ai-color-primary), var(--ai-color-accent));
  color: #ffffff;
  box-shadow: 0 12px 26px rgba(37, 99, 235, 0.24);
}

.nav-links {
  display: flex;
  justify-content: center;
  gap: 22px;
}

.nav-links a {
  color: var(--ai-text-secondary);
  font-size: var(--ai-font-size-sm);
  font-weight: 700;
  text-decoration: none;
  transition: color var(--ai-transition-fast);
}

.nav-links a:hover {
  color: var(--ai-color-primary);
}

.nav-actions,
.hero-actions,
.demo-cta__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

main {
  position: relative;
  z-index: 1;
}

.hero-section,
.landing-section,
.demo-cta {
  max-width: 1180px;
  margin: 0 auto;
  padding: 92px var(--ai-space-3);
}

.hero-section {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(420px, 0.82fr);
  gap: 56px;
  align-items: center;
  min-height: calc(100vh - 68px);
}

.landing-eyebrow {
  display: inline-flex;
  align-items: center;
  border: 1px solid rgba(37, 99, 235, 0.14);
  border-radius: var(--ai-radius-pill);
  background: rgba(37, 99, 235, 0.08);
  padding: 6px 11px;
  color: var(--ai-color-primary);
  font-size: var(--ai-font-size-xs);
  font-weight: 850;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.hero-copy h1 {
  max-width: 760px;
  margin: 18px 0;
  font-size: clamp(40px, 6vw, 72px);
  line-height: 1.02;
  letter-spacing: 0;
}

.hero-copy p,
.section-heading p,
.demo-cta p {
  color: var(--ai-text-secondary);
  line-height: 1.8;
}

.hero-copy > p {
  max-width: 640px;
  margin: 0 0 var(--ai-space-3);
  font-size: 18px;
}

.hero-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  max-width: 520px;
  margin-top: var(--ai-space-3);
}

.hero-metrics div,
.feature-card,
.workflow-card,
.highlight-pill,
.mock-panel {
  border: 1px solid rgba(217, 226, 236, 0.84);
  background: rgba(255, 255, 255, 0.78);
  box-shadow: var(--ai-shadow-soft);
  backdrop-filter: blur(14px);
}

.hero-metrics div {
  border-radius: var(--ai-radius-md);
  padding: 14px;
}

.hero-metrics strong,
.hero-metrics span {
  display: block;
}

.hero-metrics strong {
  font-size: var(--ai-font-size-xl);
}

.hero-metrics span {
  color: var(--ai-text-muted);
  font-size: var(--ai-font-size-xs);
}

.hero-demo-card {
  animation: floatPanel 5s ease-in-out infinite;
}

.demo-window {
  border: 1px solid rgba(217, 226, 236, 0.78);
  border-radius: 28px;
  background: rgba(15, 23, 42, 0.78);
  padding: var(--ai-space-2);
  box-shadow:
    0 34px 80px rgba(37, 99, 235, 0.22),
    inset 0 1px 0 rgba(255, 255, 255, 0.16);
  color: #ffffff;
}

.demo-window__top {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px 18px;
}

.demo-window__top span {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.38);
}

.demo-window__top strong {
  margin-left: auto;
  color: rgba(255, 255, 255, 0.72);
  font-size: var(--ai-font-size-xs);
}

.chat-preview {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message {
  max-width: 86%;
  border-radius: 18px;
  padding: 13px 14px;
  line-height: 1.65;
}

.message span {
  display: block;
  margin-bottom: 6px;
  font-size: var(--ai-font-size-xs);
  font-weight: 800;
}

.message p {
  margin: 0;
}

.message-ai {
  background: rgba(255, 255, 255, 0.1);
}

.message-user {
  align-self: flex-end;
  background: linear-gradient(135deg, #2563eb, #7c3aed);
}

.score-preview {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  gap: var(--ai-space-2);
  align-items: center;
  margin-top: var(--ai-space-2);
  border: 1px solid rgba(255, 255, 255, 0.16);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.08);
  padding: var(--ai-space-2);
}

.score-preview span,
.score-preview strong {
  display: block;
}

.score-preview strong {
  font-size: 42px;
  line-height: 1;
}

.score-preview p {
  margin: 0;
  color: rgba(255, 255, 255, 0.74);
  line-height: 1.7;
}

.section-heading {
  max-width: 760px;
  margin-bottom: var(--ai-space-3);
}

.section-heading h2,
.demo-cta h2 {
  margin: 14px 0 10px;
  font-size: clamp(28px, 4vw, 44px);
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--ai-space-2);
}

.feature-card,
.workflow-card,
.mock-panel {
  border-radius: var(--ai-radius-lg);
  padding: var(--ai-space-3);
  transition:
    transform var(--ai-transition-fast),
    box-shadow var(--ai-transition-fast),
    border-color var(--ai-transition-fast);
}

.feature-card:hover,
.workflow-card:hover,
.mock-panel:hover {
  transform: translateY(-4px);
  border-color: var(--ai-color-primary-subtle);
  box-shadow: var(--ai-shadow-hover);
}

.feature-icon {
  display: grid;
  width: 46px;
  height: 46px;
  place-items: center;
  border-radius: 16px;
  background: linear-gradient(135deg, var(--ai-color-primary-soft), var(--ai-color-accent-soft));
  color: var(--ai-color-primary);
  font-size: 22px;
}

.feature-card h3,
.workflow-card h3 {
  margin: var(--ai-space-2) 0 8px;
}

.feature-card p,
.workflow-card p {
  margin: 0;
  color: var(--ai-text-secondary);
  line-height: 1.75;
}

.workflow-track {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 12px;
}

.workflow-card {
  padding: var(--ai-space-2);
}

.workflow-card span {
  color: var(--ai-color-primary);
  font-size: var(--ai-font-size-xs);
  font-weight: 900;
}

.highlight-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.highlight-pill {
  border-radius: var(--ai-radius-pill);
  padding: 11px 15px;
  color: var(--ai-text-secondary);
  font-weight: 750;
}

.showcase-grid {
  display: grid;
  grid-template-columns: 1.12fr 0.88fr 0.88fr;
  gap: var(--ai-space-2);
}

.mock-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--ai-space-2);
  margin-bottom: var(--ai-space-2);
}

.mock-header span {
  color: var(--ai-text-muted);
  font-size: var(--ai-font-size-xs);
  font-weight: 750;
}

.mock-question {
  border: 1px solid var(--ai-border-soft);
  border-radius: var(--ai-radius-md);
  background: #ffffff;
  padding: var(--ai-space-2);
}

.mock-question + .mock-question {
  margin-top: 12px;
}

.mock-question h3,
.mock-question p {
  margin: 0;
}

.mock-question p {
  margin-top: 8px;
  color: var(--ai-text-secondary);
}

.mock-question div {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.mock-question div span {
  border-radius: var(--ai-radius-pill);
  background: var(--ai-color-primary-soft);
  padding: 5px 9px;
  color: var(--ai-color-primary);
  font-size: var(--ai-font-size-xs);
  font-weight: 800;
}

.mock-question.muted {
  opacity: 0.72;
}

.mini-chat {
  max-width: 88%;
  border-radius: 16px;
  padding: 12px 14px;
  line-height: 1.65;
}

.mini-chat + .mini-chat {
  margin-top: 12px;
}

.mini-chat.ai {
  background: var(--ai-bg-card-muted);
  color: var(--ai-text-secondary);
}

.mini-chat.user {
  margin-left: auto;
  background: linear-gradient(135deg, var(--ai-color-primary), var(--ai-color-accent));
  color: #ffffff;
}

.mock-score {
  display: grid;
  width: 132px;
  height: 132px;
  place-items: center;
  margin: var(--ai-space-2) auto;
  border-radius: 999px;
  background: conic-gradient(var(--ai-color-primary) 310deg, #e5edf5 0deg);
}

.mock-score strong,
.mock-score span {
  display: block;
  text-align: center;
}

.mock-score strong {
  font-size: 40px;
}

.mock-score span {
  color: var(--ai-text-muted);
  font-size: var(--ai-font-size-sm);
  font-weight: 800;
}

.mock-bars {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.mock-bars span {
  height: 10px;
  width: var(--bar-width);
  border-radius: var(--ai-radius-pill);
  background: linear-gradient(90deg, var(--ai-color-primary), var(--ai-color-accent));
}

.demo-cta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--ai-space-3);
  border: 1px solid rgba(217, 226, 236, 0.9);
  border-radius: 32px;
  background:
    radial-gradient(circle at 88% 12%, rgba(124, 58, 237, 0.18), transparent 26%),
    linear-gradient(135deg, #ffffff, #eff6ff);
  box-shadow: var(--ai-shadow-card);
}

.faq-collapse {
  border: 1px solid rgba(217, 226, 236, 0.84);
  border-radius: var(--ai-radius-lg);
  background: rgba(255, 255, 255, 0.8);
  padding: 6px var(--ai-space-2);
  box-shadow: var(--ai-shadow-soft);
}

.faq-collapse :deep(.el-collapse-item__header) {
  background: transparent;
  font-weight: 800;
}

.faq-collapse p {
  margin: 0;
  color: var(--ai-text-secondary);
  line-height: 1.75;
}

.fade-up {
  animation: fadeUp 0.7s ease both;
}

@keyframes fadeUp {
  from {
    opacity: 0;
    transform: translateY(18px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes floatPanel {
  0%,
  100% {
    transform: translateY(0);
  }

  50% {
    transform: translateY(-10px);
  }
}

@media (max-width: 1080px) {
  .landing-nav {
    grid-template-columns: 1fr auto;
  }

  .nav-links {
    display: none;
  }

  .hero-section,
  .showcase-grid,
  .demo-cta {
    grid-template-columns: 1fr;
  }

  .hero-section {
    min-height: auto;
    padding-top: 64px;
  }

  .feature-grid,
  .workflow-track {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .landing-nav,
  .hero-section,
  .landing-section,
  .demo-cta {
    padding-right: var(--ai-space-2);
    padding-left: var(--ai-space-2);
  }

  .landing-nav {
    grid-template-columns: 1fr;
  }

  .nav-actions,
  .hero-actions,
  .demo-cta__actions {
    width: 100%;
  }

  .nav-actions .el-button,
  .hero-actions .el-button,
  .demo-cta__actions .el-button {
    flex: 1;
    margin-left: 0;
  }

  .hero-section,
  .landing-section,
  .demo-cta {
    padding-top: 56px;
    padding-bottom: 56px;
  }

  .hero-metrics,
  .feature-grid,
  .workflow-track,
  .showcase-grid {
    grid-template-columns: 1fr;
  }

  .demo-window,
  .demo-cta {
    border-radius: var(--ai-radius-lg);
  }
}
</style>
