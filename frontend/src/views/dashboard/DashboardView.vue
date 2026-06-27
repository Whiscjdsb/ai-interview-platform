<template>
  <div v-loading="loading">
    <div class="page-header">
      <div>
        <h1>欢迎回来，{{ userStore.nickname }}</h1>
        <p>这里汇总你的学习节奏、答题质量和当前薄弱点。</p>
      </div>
      <el-button type="primary" @click="reload">刷新数据</el-button>
    </div>

    <section class="dashboard-hero">
      <div class="hero-copy">
        <span class="hero-eyebrow">AI Interview Platform</span>
        <h2>把刷题、追问、评分和复盘放进同一条学习路径</h2>
        <p>从题库练习进入 AI 多轮面试，再用评分报告、PDF、分享和成长分析沉淀可展示的学习成果。</p>
        <div class="hero-actions">
          <el-button type="primary" size="large" @click="router.push('/ai-interview')">开始 AI 面试</el-button>
          <el-button size="large" @click="router.push('/questions')">进入题库</el-button>
        </div>
      </div>
      <div class="hero-feature-grid">
        <div v-for="feature in heroFeatures" :key="feature.title" class="hero-feature-card">
          <span>{{ feature.icon }}</span>
          <strong>{{ feature.title }}</strong>
          <p>{{ feature.desc }}</p>
        </div>
      </div>
    </section>

    <el-alert v-if="errorText" :title="errorText" type="error" show-icon :closable="false" />

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
import { useUserStore } from '@/stores/user'
import { errorMessage } from '@/utils/error'

const userStore = useUserStore()
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
  { icon: 'AI', title: 'AI 面试', desc: '岗位、难度、题数可配置' },
  { icon: 'DS', title: 'DeepSeek 评分', desc: '支持 Mock / DeepSeek 切换' },
  { icon: 'Q+', title: '多轮追问', desc: '围绕回答逐步深入' },
  { icon: 'PDF', title: '报告导出', desc: '生成正式面试报告' },
  { icon: 'UP', title: '成长分析', desc: '沉淀长期能力画像' }
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
.dashboard-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.05fr) minmax(360px, 0.95fr);
  gap: 20px;
  margin-bottom: 20px;
  border: 1px solid rgba(191, 219, 254, 0.75);
  border-radius: 18px;
  background:
    radial-gradient(circle at 84% 16%, rgba(124, 58, 237, 0.2), transparent 30%),
    linear-gradient(135deg, #0f172a 0%, #1e3a8a 48%, #2563eb 100%);
  padding: 26px;
  color: #ffffff;
  box-shadow: 0 18px 44px rgba(37, 99, 235, 0.22);
  overflow: hidden;
}

.hero-copy {
  align-self: center;
}

.hero-eyebrow {
  display: inline-flex;
  border: 1px solid rgba(255, 255, 255, 0.24);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  padding: 5px 11px;
  font-size: 12px;
  font-weight: 700;
}

.hero-copy h2 {
  margin: 14px 0 10px;
  max-width: 660px;
  font-size: 30px;
  line-height: 1.25;
}

.hero-copy p {
  margin: 0;
  max-width: 620px;
  color: rgba(255, 255, 255, 0.78);
  line-height: 1.7;
}

.hero-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 20px;
}

.hero-feature-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.hero-feature-card {
  border: 1px solid rgba(255, 255, 255, 0.16);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.1);
  padding: 14px;
  backdrop-filter: blur(12px);
  transition:
    transform 0.18s ease,
    background 0.18s ease;
}

.hero-feature-card:hover {
  transform: translateY(-2px);
  background: rgba(255, 255, 255, 0.16);
}

.hero-feature-card span {
  display: inline-grid;
  place-items: center;
  width: 34px;
  height: 34px;
  margin-bottom: 10px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.18);
  font-size: 12px;
  font-weight: 800;
}

.hero-feature-card strong,
.hero-feature-card p {
  display: block;
}

.hero-feature-card p {
  margin: 5px 0 0;
  color: rgba(255, 255, 255, 0.72);
  font-size: 13px;
}

.el-alert {
  margin-bottom: 18px;
}

@media (max-width: 980px) {
  .dashboard-hero {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .dashboard-hero {
    padding: 18px;
  }

  .hero-copy h2 {
    font-size: 24px;
  }

  .hero-feature-grid {
    grid-template-columns: 1fr;
  }
}
</style>
