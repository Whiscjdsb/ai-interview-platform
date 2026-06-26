<template>
  <div v-loading="loading">
    <div class="page-header">
      <div>
        <h1>欢迎回来，{{ userStore.nickname }}</h1>
        <p>这里汇总你的学习节奏、答题质量和当前薄弱点。</p>
      </div>
      <el-button type="primary" @click="reload">刷新数据</el-button>
    </div>

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
import * as echarts from 'echarts'

import { userOverviewApi, userTrendApi, userWrongAnalysisApi } from '@/api/statistics'
import type { LearningTrendItem, UserStatisticsOverview, WrongAnalysisItem } from '@/api/types'
import StatCard from '@/components/StatCard.vue'
import { useUserStore } from '@/stores/user'
import { errorMessage } from '@/utils/error'

const userStore = useUserStore()
const loading = ref(false)
const errorText = ref('')
const trend = ref<LearningTrendItem[]>([])
const wrongAnalysis = ref<WrongAnalysisItem[]>([])
const trendChartRef = ref<HTMLDivElement>()
const wrongChartRef = ref<HTMLDivElement>()
let trendChart: echarts.ECharts | null = null
let wrongChart: echarts.ECharts | null = null

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
.el-alert {
  margin-bottom: 18px;
}
</style>
