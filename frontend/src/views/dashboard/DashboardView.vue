<template>
  <div v-loading="loading" class="dashboard-page">
    <section class="dashboard-hero">
      <div class="hero-copy">
        <span class="hero-eyebrow">AI Interview Platform</span>
        <h1>AI 模拟面试平台工作台</h1>
        <p>
          集成 DeepSeek 智能评分、多轮追问、ScoreEngine 统一评分、成长分析、PDF 报告和分享能力，
          适合 Java 后端面试练习、项目演示和答辩讲解。
        </p>
        <div class="hero-actions">
          <el-button type="primary" size="large" @click="router.push('/ai-interview')">开始 AI 面试</el-button>
          <el-button size="large" @click="router.push('/questions')">进入题库</el-button>
          <el-button text @click="reload">刷新数据</el-button>
        </div>
      </div>

      <div class="hero-snapshot">
        <div class="snapshot-card snapshot-card--main">
          <span>今日答题</span>
          <strong>{{ overview.todayAnswerCount }}</strong>
          <p>学习时长 {{ overview.todayStudyDuration }} 秒</p>
        </div>
        <div class="snapshot-card">
          <span>正确率</span>
          <strong>{{ overview.accuracyRate }}%</strong>
        </div>
        <div class="snapshot-card">
          <span>连续学习</span>
          <strong>{{ overview.continuousLearningDays }} 天</strong>
        </div>
      </div>
    </section>

    <el-alert v-if="errorText" :title="errorText" type="error" show-icon :closable="false" />

    <section class="capability-section">
      <div class="section-heading">
        <div>
          <span>Core Capabilities</span>
          <h2>核心能力</h2>
        </div>
        <p>围绕“练习 - 面试 - 评分 - 复盘 - 展示”组织体验。</p>
      </div>

      <div class="capability-grid">
        <article v-for="feature in heroFeatures" :key="feature.title" class="capability-card ai-hover-lift">
          <span class="capability-icon">{{ feature.icon }}</span>
          <strong>{{ feature.title }}</strong>
          <p>{{ feature.desc }}</p>
        </article>
      </div>
    </section>

    <section class="project-proof-section">
      <div v-for="item in projectStats" :key="item.label" class="proof-card">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <p>{{ item.desc }}</p>
      </div>
    </section>

    <section class="demo-path-section panel">
      <div class="section-heading">
        <div>
          <span>Demo Flow</span>
          <h2>推荐体验路径</h2>
        </div>
        <p>面试或答辩时可以按这条路径快速展示完整闭环。</p>
      </div>

      <div class="demo-path">
        <button
          v-for="(step, index) in demoSteps"
          :key="step.title"
          type="button"
          class="demo-step"
          @click="router.push(step.path)"
        >
          <span>{{ index + 1 }}</span>
          <strong>{{ step.title }}</strong>
          <p>{{ step.desc }}</p>
        </button>
      </div>
    </section>

    <section class="stats-grid">
      <StatCard label="总答题数" :value="overview.totalAnswerCount" hint="累计提交记录" />
      <StatCard label="正确率" :value="`${overview.accuracyRate}%`" hint="客观题统计" />
      <StatCard label="连续学习" :value="`${overview.continuousLearningDays} 天`" hint="从今天向前计算" />
      <StatCard label="收藏题数" :value="overview.favoriteCount" />
      <StatCard label="活跃错题" :value="overview.activeWrongQuestionCount" />
    </section>

    <section class="dashboard-grid">
      <div class="panel">
        <div class="panel-header">
          <h2 class="panel-title">最近 7 天学习趋势</h2>
        </div>
        <div ref="trendChartRef" class="chart-box" />
      </div>

      <div class="panel">
        <div class="panel-header">
          <h2 class="panel-title">高频错题知识点</h2>
        </div>
        <div v-if="wrongAnalysis.length" ref="wrongChartRef" class="chart-box" />
        <div v-else class="empty-panel">
          <el-empty description="暂无活跃错题数据" />
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, onUnmounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'

import { userOverviewApi, userTrendApi, userWrongAnalysisApi } from '@/api/statistics'
import type { LearningTrendItem, UserStatisticsOverview, WrongAnalysisItem } from '@/api/types'
import StatCard from '@/components/StatCard.vue'
import { errorMessage } from '@/utils/error'

const router = useRouter()
const loading = ref(false)
const errorText = ref('')
const trend = ref<LearningTrendItem[]>([])
const wrongAnalysis = ref<WrongAnalysisItem[]>([])
const trendChartRef = ref<HTMLDivElement>()
const wrongChartRef = ref<HTMLDivElement>()
let trendChart: echarts.ECharts | null = null
let wrongChart: echarts.ECharts | null = null

const heroFeatures = [
  { icon: 'AI', title: 'AI 模拟面试', desc: '按岗位、难度和方向生成结构化面试题' },
  { icon: 'DS', title: 'DeepSeek 智能评分', desc: '支持 DeepSeek 与 Mock Provider 平滑切换' },
  { icon: 'Q+', title: '多轮追问', desc: '基于用户回答继续追问，模拟真实面试节奏' },
  { icon: 'SE', title: 'ScoreEngine 统一评分', desc: '统一低质量回答封顶和最终分数口径' },
  { icon: 'PDF', title: 'PDF 报告导出', desc: '将面试结果沉淀为可下载正式报告' },
  { icon: 'UP', title: '成长分析', desc: '基于历史面试结果生成长期能力画像' },
  { icon: 'B2B', title: '企业面试原型', desc: '预留企业面试官风格和岗位模型扩展' }
]

const projectStats = [
  { label: '演示题库', value: '60+', desc: '覆盖 Java、Spring、MySQL、Redis 与实战排障' },
  { label: 'AI Provider', value: '2', desc: 'DeepSeek / Mock 双模式，便于本地演示' },
  { label: '测试回归', value: '50', desc: '后端测试通过，覆盖用户端与管理端核心链路' },
  { label: '报告闭环', value: 'PDF + Share', desc: '支持导出报告、生成分享链接和成长分析' }
]

const demoSteps = [
  { title: '进入题库', desc: '浏览 Java 后端方向题目并开始练习', path: '/questions' },
  { title: '创建 AI 面试', desc: '选择岗位、难度和题目数量', path: '/ai-interview' },
  { title: '完成多轮追问', desc: '在会话页回答问题并生成 AI 追问', path: '/ai-interview' },
  { title: '查看结果报告', desc: '查看总分、雷达图、优点、不足和建议', path: '/ai-interview/history' },
  { title: '导出与分享', desc: '下载 PDF 报告或生成公开分享链接', path: '/ai-interview/history' }
]

const overview = reactive<UserStatisticsOverview>({
  totalAnswerCount: 0,
  correctAnswerCount: 0,
  accuracyRate: 0,
  totalStudyDuration: 0,
  favoriteCount: 0,
  wrongQuestionCount: 0,
  activeWrongQuestionCount: 0,
  resolvedWrongQuestionCount: 0,
  continuousLearningDays: 0,
  todayAnswerCount: 0,
  todayStudyDuration: 0
})

onMounted(() => {
  reload()
  window.addEventListener('resize', resizeCharts)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeCharts)
  trendChart?.dispose()
  wrongChart?.dispose()
})

async function reload() {
  loading.value = true
  errorText.value = ''
  try {
    const [overviewData, trendData, wrongData] = await Promise.all([
      userOverviewApi(),
      userTrendApi(7),
      userWrongAnalysisApi()
    ])
    Object.assign(overview, overviewData)
    trend.value = trendData
    wrongAnalysis.value = wrongData
    await nextTick()
    renderCharts()
  } catch (error) {
    errorText.value = errorMessage(error, '加载首页数据失败')
  } finally {
    loading.value = false
  }
}

function renderCharts() {
  renderTrendChart()
  renderWrongChart()
}

function renderTrendChart() {
  if (!trendChartRef.value) {
    return
  }
  trendChart = trendChart || echarts.init(trendChartRef.value)
  trendChart.setOption({
    color: ['#2563eb', '#16a34a'],
    tooltip: { trigger: 'axis' },
    legend: { top: 0, data: ['答题数', '正确数'] },
    grid: { left: 36, right: 18, top: 48, bottom: 32 },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: trend.value.map((item) => item.date.slice(5))
    },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      {
        name: '答题数',
        type: 'line',
        smooth: true,
        areaStyle: { opacity: 0.12 },
        data: trend.value.map((item) => item.answerCount)
      },
      {
        name: '正确数',
        type: 'line',
        smooth: true,
        data: trend.value.map((item) => item.correctCount)
      }
    ]
  })
}

function renderWrongChart() {
  if (!wrongAnalysis.value.length || !wrongChartRef.value) {
    wrongChart?.dispose()
    wrongChart = null
    return
  }
  wrongChart = wrongChart || echarts.init(wrongChartRef.value)
  wrongChart.setOption({
    color: ['#f59e0b'],
    tooltip: { trigger: 'axis' },
    grid: { left: 38, right: 18, top: 26, bottom: 54 },
    xAxis: {
      type: 'category',
      axisLabel: { interval: 0, rotate: 24 },
      data: wrongAnalysis.value.map((item) => item.tagName)
    },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      {
        name: '错题数',
        type: 'bar',
        barMaxWidth: 36,
        data: wrongAnalysis.value.map((item) => item.wrongQuestionCount)
      }
    ]
  })
}

function resizeCharts() {
  trendChart?.resize()
  wrongChart?.resize()
}
</script>

<style scoped>
.dashboard-page {
  display: grid;
  gap: var(--ai-space-3);
}

.dashboard-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(320px, 0.85fr);
  gap: var(--ai-space-3);
  border: 1px solid rgba(191, 219, 254, 0.75);
  border-radius: var(--ai-radius-lg);
  background:
    radial-gradient(circle at 84% 16%, rgba(124, 58, 237, 0.26), transparent 30%),
    linear-gradient(135deg, #0f172a 0%, #1e3a8a 48%, var(--ai-color-primary) 100%);
  padding: 32px;
  color: #ffffff;
  box-shadow: 0 18px 44px rgba(37, 99, 235, 0.22);
  overflow: hidden;
}

.hero-copy {
  align-self: center;
}

.hero-eyebrow,
.section-heading span {
  display: inline-flex;
  border-radius: var(--ai-radius-pill);
  font-size: var(--ai-font-size-xs);
  font-weight: 800;
}

.hero-eyebrow {
  border: 1px solid rgba(255, 255, 255, 0.24);
  background: rgba(255, 255, 255, 0.12);
  padding: 5px 11px;
}

.hero-copy h1 {
  margin: 16px 0 12px;
  max-width: 760px;
  color: #ffffff;
  font-size: 38px;
  line-height: 1.16;
}

.hero-copy p {
  margin: 0;
  max-width: 720px;
  color: rgba(255, 255, 255, 0.78);
  font-size: 16px;
  line-height: 1.8;
}

.hero-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 24px;
}

.hero-actions :deep(.el-button.is-text) {
  color: rgba(255, 255, 255, 0.86);
}

.hero-snapshot {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  align-self: stretch;
}

.snapshot-card {
  border: 1px solid rgba(255, 255, 255, 0.16);
  border-radius: var(--ai-radius-md);
  background: rgba(255, 255, 255, 0.1);
  padding: 18px;
  backdrop-filter: blur(12px);
}

.snapshot-card--main {
  grid-column: 1 / -1;
}

.snapshot-card span,
.snapshot-card p {
  color: rgba(255, 255, 255, 0.72);
}

.snapshot-card span {
  font-size: var(--ai-font-size-sm);
}

.snapshot-card strong {
  display: block;
  margin-top: 8px;
  color: #ffffff;
  font-size: 34px;
  line-height: 1;
}

.snapshot-card p {
  margin: 10px 0 0;
}

.section-heading {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: var(--ai-space-2);
  margin-bottom: var(--ai-space-2);
}

.section-heading span {
  background: var(--ai-color-primary-soft);
  color: var(--ai-color-primary);
  padding: 4px 10px;
}

.section-heading h2 {
  margin: 8px 0 0;
}

.section-heading p {
  margin: 0;
  max-width: 460px;
  color: var(--ai-text-muted);
}

.capability-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--ai-space-2);
}

.capability-card,
.proof-card {
  border: 1px solid var(--ai-border-soft);
  border-radius: var(--ai-radius-md);
  background: var(--ai-bg-card);
  box-shadow: var(--ai-shadow-soft);
}

.capability-card {
  padding: 18px;
}

.capability-icon {
  display: inline-grid;
  place-items: center;
  width: 38px;
  height: 38px;
  margin-bottom: 14px;
  border-radius: 14px;
  background: linear-gradient(135deg, var(--ai-color-primary-soft), var(--ai-color-accent-soft));
  color: var(--ai-color-primary);
  font-size: var(--ai-font-size-xs);
  font-weight: 900;
}

.capability-card strong,
.capability-card p,
.proof-card span,
.proof-card strong,
.proof-card p {
  display: block;
}

.capability-card p,
.proof-card p {
  margin: 8px 0 0;
  color: var(--ai-text-muted);
  font-size: var(--ai-font-size-sm);
  line-height: 1.7;
}

.project-proof-section {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--ai-space-2);
}

.proof-card {
  padding: 18px;
}

.proof-card span {
  color: var(--ai-text-muted);
  font-size: var(--ai-font-size-sm);
}

.proof-card strong {
  margin-top: 8px;
  color: var(--ai-color-primary);
  font-size: 26px;
}

.demo-path-section {
  padding: 20px;
}

.demo-path {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
}

.demo-step {
  min-height: 150px;
  border: 1px solid var(--ai-border-soft);
  border-radius: var(--ai-radius-md);
  background: linear-gradient(180deg, #ffffff 0%, var(--ai-bg-card-muted) 100%);
  padding: 16px;
  text-align: left;
  cursor: pointer;
  transition:
    transform var(--ai-transition-fast),
    box-shadow var(--ai-transition-fast),
    border-color var(--ai-transition-fast);
}

.demo-step:hover {
  border-color: var(--ai-color-primary-subtle);
  box-shadow: var(--ai-shadow-hover);
  transform: translateY(-2px);
}

.demo-step span {
  display: inline-grid;
  place-items: center;
  width: 30px;
  height: 30px;
  margin-bottom: 14px;
  border-radius: var(--ai-radius-pill);
  background: var(--ai-color-primary);
  color: #ffffff;
  font-weight: 800;
}

.demo-step strong,
.demo-step p {
  display: block;
}

.demo-step p {
  margin: 8px 0 0;
  color: var(--ai-text-muted);
  line-height: 1.65;
}

.el-alert {
  margin-bottom: 0;
}

@media (max-width: 1360px) {
  .capability-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .demo-path {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 980px) {
  .dashboard-hero,
  .section-heading {
    grid-template-columns: 1fr;
  }

  .dashboard-hero,
  .section-heading {
    align-items: stretch;
  }

  .capability-grid,
  .project-proof-section,
  .demo-path {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .dashboard-hero {
    padding: 20px;
  }

  .hero-copy h1 {
    font-size: 28px;
  }

  .hero-snapshot,
  .capability-grid,
  .project-proof-section,
  .demo-path {
    grid-template-columns: 1fr;
  }
}
</style>
