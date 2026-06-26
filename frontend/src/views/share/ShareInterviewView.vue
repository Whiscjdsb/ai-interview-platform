<template>
  <main class="share-page">
    <section class="share-header">
      <div>
        <p class="eyebrow">AI 面试报告</p>
        <h1>{{ result?.position || '公开面试报告' }}</h1>
        <p v-if="result">{{ difficultyText(result.difficulty) }} · {{ result.modelName }} · {{ formatInterviewTime(result.createTime) }}</p>
      </div>
      <div v-if="result" class="score-box" :style="{ color: scoreColor(displayScore) }">
        <strong>{{ displayScore }}</strong>
        <span>{{ displayLevel }}</span>
      </div>
    </section>

    <section v-loading="loading" class="share-content">
      <el-alert v-if="errorText" :title="errorText" type="error" show-icon :closable="false" />

      <template v-if="result">
        <div class="result-layout">
          <el-card shadow="never">
            <template #header>
              <strong>总分概览</strong>
            </template>
            <el-progress type="dashboard" :percentage="displayScore" :color="scoreColor(displayScore)" :width="150" />
            <p class="summary">{{ result.summary || fallbackSummary }}</p>
          </el-card>

          <el-card shadow="never">
            <template #header>
              <strong>能力雷达图</strong>
            </template>
            <div ref="radarChartRef" class="radar-chart" />
          </el-card>
        </div>

        <el-card shadow="never" class="analysis-card">
          <template #header>
            <strong>AI 分析</strong>
          </template>
          <div class="analysis-grid">
            <section class="analysis-section section-green">
              <h3>优点</h3>
              <ul>
                <li v-for="item in strengths" :key="item">{{ item }}</li>
              </ul>
              <el-empty v-if="!strengths.length" description="暂无优点分析" :image-size="72" />
            </section>
            <section class="analysis-section section-red">
              <h3>不足</h3>
              <ul>
                <li v-for="item in weaknesses" :key="item">{{ item }}</li>
              </ul>
              <el-empty v-if="!weaknesses.length" description="暂无不足分析" :image-size="72" />
            </section>
            <section class="analysis-section section-blue">
              <h3>改进建议</h3>
              <ul>
                <li v-for="item in suggestions" :key="item">{{ item }}</li>
              </ul>
              <el-empty v-if="!suggestions.length" description="暂无改进建议" :image-size="72" />
            </section>
          </div>

          <div v-if="referenceAnswer" class="reference-section">
            <h3>参考答案</h3>
            <pre>{{ referenceAnswer }}</pre>
          </div>
        </el-card>

        <el-card v-if="result.questionResults.length" shadow="never" class="question-card">
          <template #header>
            <strong>逐题点评</strong>
          </template>
          <div v-for="item in result.questionResults" :key="item.questionNo" class="question-result">
            <div class="question-title">
              <h3>第 {{ item.questionNo }} 题：{{ item.question }}</h3>
              <el-tag :color="scoreColor(item.score || 0)" effect="dark">{{ item.score ?? 0 }} 分</el-tag>
            </div>
            <p>{{ item.review }}</p>
            <el-descriptions border :column="1">
              <el-descriptions-item label="回答记录">
                <pre class="plain-text">{{ item.answer || item.userAnswer || '未作答' }}</pre>
              </el-descriptions-item>
              <el-descriptions-item label="参考答案">
                <pre class="plain-text">{{ item.suggestedAnswer || item.referenceAnswer }}</pre>
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
      </template>

      <el-empty v-else-if="!loading && !errorText" description="分享报告不存在或已关闭" />
    </section>
  </main>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import * as echarts from 'echarts'

import { getSharedInterview } from '@/api/ai-interview'
import type { InterviewResult } from '@/types/ai-interview'
import {
  buildRadarScores,
  difficultyText,
  formatInterviewTime,
  radarIndicators,
  scoreColor,
  scoreLevel
} from '@/utils/ai-interview'
import { errorMessage } from '@/utils/error'

const route = useRoute()
const loading = ref(false)
const errorText = ref('')
const result = ref<InterviewResult | null>(null)
const radarChartRef = ref<HTMLDivElement>()
let radarChart: echarts.ECharts | null = null

const structuredResult = computed(() => result.value?.structuredResult || null)
const displayScore = computed(() => normalizeScore(structuredResult.value?.score ?? result.value?.totalScore ?? 0))
const displayLevel = computed(() => result.value?.level || scoreLevel(displayScore.value))
const fallbackSummary = computed(() => `本次面试得分为 ${displayScore.value} 分。`)
const strengths = computed(() => safeList(structuredResult.value?.strengths, result.value?.advantages))
const weaknesses = computed(() =>
  safeList(structuredResult.value?.weaknesses, result.value?.disadvantages || result.value?.improvements)
)
const suggestions = computed(() => safeList(structuredResult.value?.suggestions, result.value?.suggestions))
const referenceAnswer = computed(() => {
  if (structuredResult.value?.referenceAnswer) {
    return structuredResult.value.referenceAnswer
  }
  const item = result.value?.questionResults.find((question) => question.suggestedAnswer || question.referenceAnswer)
  return item?.suggestedAnswer || item?.referenceAnswer || ''
})

onMounted(async () => {
  await loadSharedInterview()
  window.addEventListener('resize', resizeRadarChart)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeRadarChart)
  radarChart?.dispose()
})

async function loadSharedInterview() {
  loading.value = true
  errorText.value = ''
  try {
    result.value = await getSharedInterview(route.params.token as string)
    await nextTick()
    renderRadarChart()
  } catch (error) {
    errorText.value = errorMessage(error, '分享报告加载失败')
  } finally {
    loading.value = false
  }
}

function renderRadarChart() {
  if (!radarChartRef.value || !result.value) {
    return
  }
  radarChart = radarChart || echarts.init(radarChartRef.value)
  radarChart.setOption({
    color: [scoreColor(displayScore.value)],
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
        name: '能力模型',
        type: 'radar',
        areaStyle: { opacity: 0.18 },
        lineStyle: { width: 2 },
        data: [{ value: buildRadarScores(displayScore.value), name: '当前表现' }]
      }
    ]
  })
}

function resizeRadarChart() {
  radarChart?.resize()
}

function safeList(primary?: string[], fallback?: string[]) {
  const value = primary?.length ? primary : fallback
  return (value || []).filter(Boolean)
}

function normalizeScore(value: number) {
  return Math.max(0, Math.min(100, Math.round(value || 0)))
}
</script>

<style scoped>
.share-page {
  min-height: 100vh;
  background: #f6f8fb;
  padding: 28px;
}

.share-header,
.share-content {
  max-width: 1120px;
  margin: 0 auto;
}

.share-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 18px;
}

.eyebrow {
  margin: 0 0 6px;
  color: #2563eb;
  font-weight: 700;
}

.share-header h1 {
  margin: 0 0 8px;
  font-size: 28px;
}

.share-header p {
  margin: 0;
  color: #62748e;
}

.score-box {
  min-width: 110px;
  text-align: right;
}

.score-box strong {
  display: block;
  font-size: 48px;
  line-height: 1;
}

.score-box span {
  color: #62748e;
}

.result-layout {
  display: grid;
  grid-template-columns: minmax(220px, 320px) minmax(0, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.summary {
  margin: 14px 0 0;
  color: #486581;
  line-height: 1.7;
}

.radar-chart {
  height: 280px;
}

.analysis-card,
.question-card {
  margin-bottom: 16px;
}

.analysis-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.analysis-section {
  border: 1px solid #d9e2ec;
  border-radius: 8px;
  padding: 16px;
}

.analysis-section h3,
.reference-section h3,
.question-result h3 {
  margin: 0;
}

.analysis-section ul {
  margin: 12px 0 0;
  padding-left: 18px;
  color: #486581;
  line-height: 1.8;
}

.section-green {
  border-color: #bbf7d0;
  background: #f0fdf4;
}

.section-red {
  border-color: #fecaca;
  background: #fef2f2;
}

.section-blue {
  border-color: #bfdbfe;
  background: #eff6ff;
}

.reference-section {
  margin-top: 14px;
}

.reference-section pre,
.plain-text {
  margin: 10px 0 0;
  border-radius: 8px;
  background: #f4f6f8;
  padding: 14px;
  color: #334e68;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.7;
  font-family: inherit;
}

.question-result + .question-result {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #e5edf5;
}

.question-title {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.question-result p {
  color: #486581;
  line-height: 1.7;
}

@media (max-width: 920px) {
  .result-layout,
  .analysis-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .share-page {
    padding: 18px;
  }

  .share-header,
  .question-title {
    flex-direction: column;
    align-items: stretch;
  }

  .score-box {
    text-align: left;
  }
}
</style>
