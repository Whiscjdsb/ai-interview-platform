<template>
  <div v-loading="loading">
    <div class="page-header">
      <div>
        <h1>能力成长</h1>
        <p>基于历史 AI 面试结果生成长期能力画像。</p>
      </div>
      <el-button type="primary" @click="loadGrowth">刷新</el-button>
    </div>

    <el-alert v-if="errorText" :title="errorText" type="error" show-icon :closable="false" />

    <template v-if="growth && hasData">
      <section class="growth-cards">
        <el-card shadow="never">
          <span>平均分</span>
          <strong :style="{ color: scoreColor(growth.averageScore) }">{{ growth.averageScore }}</strong>
          <small>{{ scoreLevel(growth.averageScore) }}</small>
        </el-card>
        <el-card shadow="never">
          <span>面试次数</span>
          <strong>{{ growth.interviewCount }}</strong>
          <small>已提交的模拟面试</small>
        </el-card>
        <el-card shadow="never">
          <span>最新月份</span>
          <strong>{{ latestMonth }}</strong>
          <small>成长趋势按月聚合</small>
        </el-card>
      </section>

      <section class="chart-grid">
        <el-card shadow="never">
          <template #header>
            <strong>成长趋势</strong>
          </template>
          <div ref="trendChartRef" class="chart-box" />
        </el-card>

        <el-card shadow="never">
          <template #header>
            <strong>长期能力雷达</strong>
          </template>
          <div ref="radarChartRef" class="chart-box" />
        </el-card>
      </section>
    </template>

    <el-empty v-else-if="!loading" description="暂无 AI 面试成长数据">
      <el-button type="primary" @click="router.push('/ai-interview')">去完成一次 AI 面试</el-button>
    </el-empty>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'

import { getUserGrowth } from '@/api/ai-interview'
import type { AiUserGrowth } from '@/types/ai-interview'
import { radarIndicators, scoreColor, scoreLevel } from '@/utils/ai-interview'
import { errorMessage } from '@/utils/error'

const router = useRouter()
const loading = ref(false)
const errorText = ref('')
const growth = ref<AiUserGrowth | null>(null)
const trendChartRef = ref<HTMLDivElement>()
const radarChartRef = ref<HTMLDivElement>()
let trendChart: echarts.ECharts | null = null
let radarChart: echarts.ECharts | null = null

const hasData = computed(() => Boolean(growth.value && growth.value.interviewCount > 0))
const latestMonth = computed(() => growth.value?.trend[growth.value.trend.length - 1]?.date || '-')

onMounted(() => {
  loadGrowth()
  window.addEventListener('resize', resizeCharts)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeCharts)
  trendChart?.dispose()
  radarChart?.dispose()
})

async function loadGrowth() {
  loading.value = true
  errorText.value = ''
  try {
    growth.value = await getUserGrowth()
    await nextTick()
    renderCharts()
  } catch (error) {
    errorText.value = errorMessage(error, '加载成长数据失败')
  } finally {
    loading.value = false
  }
}

function renderCharts() {
  renderTrendChart()
  renderRadarChart()
}

function renderTrendChart() {
  if (!trendChartRef.value || !growth.value || !growth.value.trend.length) {
    trendChart?.dispose()
    trendChart = null
    return
  }
  trendChart = trendChart || echarts.init(trendChartRef.value)
  trendChart.setOption({
    color: ['#2563eb'],
    tooltip: { trigger: 'axis' },
    grid: { left: 38, right: 20, top: 28, bottom: 34 },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: growth.value.trend.map((item) => item.date)
    },
    yAxis: { type: 'value', min: 0, max: 100 },
    series: [
      {
        name: '月均分',
        type: 'line',
        smooth: true,
        symbolSize: 8,
        areaStyle: { opacity: 0.12 },
        data: growth.value.trend.map((item) => item.score)
      }
    ]
  })
}

function renderRadarChart() {
  if (!radarChartRef.value || !growth.value) {
    return
  }
  const dimension = growth.value.dimension
  radarChart = radarChart || echarts.init(radarChartRef.value)
  radarChart.setOption({
    color: [scoreColor(growth.value.averageScore)],
    tooltip: { trigger: 'item' },
    radar: {
      indicator: radarIndicators,
      radius: '68%',
      splitNumber: 4,
      axisName: { color: '#486581' },
      splitLine: { lineStyle: { color: '#d9e2ec' } },
      splitArea: { areaStyle: { color: ['#f8fafc', '#ffffff'] } },
      axisLine: { lineStyle: { color: '#d9e2ec' } }
    },
    series: [
      {
        name: '长期能力',
        type: 'radar',
        areaStyle: { opacity: 0.18 },
        lineStyle: { width: 2 },
        data: [
          {
            value: [
              dimension.java,
              dimension.spring,
              dimension.database,
              dimension.systemDesign,
              dimension.project
            ],
            name: '长期能力'
          }
        ]
      }
    ]
  })
}

function resizeCharts() {
  trendChart?.resize()
  radarChart?.resize()
}
</script>

<style scoped>
.el-alert {
  margin-bottom: 18px;
}

.growth-cards {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin-bottom: 16px;
}

.growth-cards :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.growth-cards span {
  color: #62748e;
}

.growth-cards strong {
  font-size: 34px;
  line-height: 1;
  color: #102a43;
}

.growth-cards small {
  color: #829ab1;
}

.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.chart-box {
  height: 320px;
}

@media (max-width: 920px) {
  .growth-cards,
  .chart-grid {
    grid-template-columns: 1fr;
  }
}
</style>
