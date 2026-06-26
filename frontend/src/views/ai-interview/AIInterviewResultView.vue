<template>
  <div>
    <section v-if="result" class="result-panel panel">
      <div class="result-header">
        <div>
          <h1>{{ result.position }} 面试结果</h1>
          <p>{{ difficultyText(result.difficulty) }} · {{ result.modelName }} · {{ formatInterviewTime(result.createTime) }}</p>
        </div>
        <div class="score-box" :style="{ color: scoreColor(displayScore) }">
          <strong>{{ displayScore }}</strong>
          <span>{{ displayLevel }}</span>
        </div>
      </div>

      <div class="result-layout">
        <el-card shadow="never" class="score-card">
          <template #header>
            <div class="card-title">总分概览</div>
          </template>
          <el-progress
            type="dashboard"
            :percentage="displayScore"
            :color="scoreColor(displayScore)"
            :width="150"
          />
          <p class="score-summary">{{ result.summary || fallbackSummary }}</p>
        </el-card>

        <el-card shadow="never" class="radar-card">
          <template #header>
            <div class="card-title">能力雷达图</div>
          </template>
          <div ref="radarChartRef" class="radar-chart" />
        </el-card>
      </div>

      <el-card shadow="never" class="analysis-card">
        <template #header>
          <div class="card-title">AI 分析</div>
        </template>

        <div v-if="hasStructuredOrLegacyContent" class="analysis-grid">
          <section class="analysis-section section-green">
            <h3>优点</h3>
            <ul v-if="strengths.length">
              <li v-for="item in strengths" :key="item">{{ item }}</li>
            </ul>
            <el-empty v-else description="暂无优点分析" :image-size="72" />
          </section>

          <section class="analysis-section section-red">
            <h3>不足</h3>
            <ul v-if="weaknesses.length">
              <li v-for="item in weaknesses" :key="item">{{ item }}</li>
            </ul>
            <el-empty v-else description="暂无不足分析" :image-size="72" />
          </section>

          <section class="analysis-section section-blue">
            <h3>改进建议</h3>
            <ul v-if="suggestions.length">
              <li v-for="item in suggestions" :key="item">{{ item }}</li>
            </ul>
            <el-empty v-else description="暂无改进建议" :image-size="72" />
          </section>
        </div>

        <div v-if="referenceAnswer" class="reference-section">
          <h3>参考答案</h3>
          <pre>{{ referenceAnswer }}</pre>
        </div>

        <div v-if="showRawResponse" class="reference-section raw-section">
          <h3>原始返回</h3>
          <pre>{{ rawResponse }}</pre>
        </div>
      </el-card>

      <div v-if="result.questionResults.length" class="question-results">
        <h2>各题评分</h2>
        <div v-for="item in result.questionResults" :key="item.questionNo" class="question-result">
          <div class="question-result__header">
            <h3>第 {{ item.questionNo }} 题：{{ item.question }}</h3>
            <el-tag :color="scoreColor(item.score || 0)" effect="dark">{{ item.score ?? 0 }} 分</el-tag>
          </div>
          <p>{{ item.review }}</p>
          <el-collapse>
            <el-collapse-item title="我的回答与建议" :name="String(item.questionNo)">
              <el-descriptions border :column="1">
                <el-descriptions-item label="我的回答">
                  <pre class="plain-text">{{ item.answer || '未作答' }}</pre>
                </el-descriptions-item>
                <el-descriptions-item label="AI 回答建议">
                  <pre class="plain-text">{{ item.suggestedAnswer }}</pre>
                </el-descriptions-item>
                <el-descriptions-item label="优点">{{ item.advantages.join('；') }}</el-descriptions-item>
                <el-descriptions-item label="不足">{{ item.improvements.join('；') }}</el-descriptions-item>
              </el-descriptions>
            </el-collapse-item>
          </el-collapse>
        </div>
      </div>

      <div class="result-actions">
        <el-button :loading="shareLoading" type="warning" @click="createShareLink">生成分享链接</el-button>
        <el-button :loading="pdfLoading" type="success" @click="downloadPdf">下载PDF报告</el-button>
        <el-button @click="router.push('/ai-interview')">重新开始</el-button>
        <el-button type="primary" @click="router.push('/dashboard')">返回首页</el-button>
      </div>

      <el-dialog v-model="shareDialogVisible" title="分享面试报告" width="520px">
        <p class="share-hint">复制以下链接后，任何拥有链接的人都可以只读查看这份面试报告。</p>
        <el-input v-model="shareUrl" readonly>
          <template #append>
            <el-button @click="copyShareUrl">复制</el-button>
          </template>
        </el-input>
      </el-dialog>
    </section>

    <el-empty v-else description="未找到面试结果">
      <el-button type="primary" @click="router.push('/ai-interview')">重新开始</el-button>
    </el-empty>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'

import { createInterviewShareLink, downloadInterviewPdf } from '@/api/ai-interview'
import type { InterviewResult } from '@/types/ai-interview'
import {
  buildRadarScores,
  difficultyText,
  formatInterviewTime,
  getInterviewResult,
  radarIndicators,
  scoreColor,
  scoreLevel
} from '@/utils/ai-interview'

const route = useRoute()
const router = useRouter()
const result = ref<InterviewResult | null>(null)
const radarChartRef = ref<HTMLDivElement>()
const pdfLoading = ref(false)
const shareLoading = ref(false)
const shareDialogVisible = ref(false)
const shareUrl = ref('')
let radarChart: echarts.ECharts | null = null

const structuredResult = computed(() => result.value?.structuredResult || null)
const displayScore = computed(() => normalizeScore(structuredResult.value?.score ?? result.value?.totalScore ?? 0))
const displayLevel = computed(() => result.value?.level || scoreLevel(displayScore.value))
const fallbackSummary = computed(() => `本次面试得分为 ${displayScore.value} 分，建议结合逐题点评继续复盘。`)
const rawResponse = computed(() => result.value?.rawResponse || '')
const strengths = computed(() => safeList(structuredResult.value?.strengths, result.value?.advantages))
const weaknesses = computed(() =>
  safeList(structuredResult.value?.weaknesses, result.value?.disadvantages || result.value?.improvements)
)
const suggestions = computed(() => safeList(structuredResult.value?.suggestions, result.value?.suggestions))
const referenceAnswer = computed(() => {
  if (structuredResult.value?.referenceAnswer) {
    return structuredResult.value.referenceAnswer
  }
  return result.value?.questionResults.find((item) => item.suggestedAnswer)?.suggestedAnswer || ''
})
const hasStructuredOrLegacyContent = computed(
  () => strengths.value.length > 0 || weaknesses.value.length > 0 || suggestions.value.length > 0
)
const showRawResponse = computed(() => !structuredResult.value && Boolean(rawResponse.value))

onMounted(async () => {
  result.value = getInterviewResult(route.params.id as string)
  await nextTick()
  renderRadarChart()
  window.addEventListener('resize', resizeRadarChart)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeRadarChart)
  radarChart?.dispose()
})

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
        data: [
          {
            value: buildRadarScores(displayScore.value),
            name: '当前表现'
          }
        ]
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

async function downloadPdf() {
  if (!result.value) {
    return
  }
  pdfLoading.value = true
  try {
    const blob = await downloadInterviewPdf(result.value.id)
    const url = window.URL.createObjectURL(new Blob([blob], { type: 'application/pdf' }))
    const link = document.createElement('a')
    link.href = url
    link.download = `AI面试报告_${result.value.id}.pdf`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  } catch {
    ElMessage.error('PDF报告下载失败，请确认面试已提交并稍后重试')
  } finally {
    pdfLoading.value = false
  }
}

async function createShareLink() {
  if (!result.value) {
    return
  }
  shareLoading.value = true
  try {
    const response = await createInterviewShareLink(result.value.id)
    shareUrl.value = toFrontendShareUrl(response.shareUrl)
    shareDialogVisible.value = true
  } catch {
    ElMessage.error('生成分享链接失败，请确认面试已提交并稍后重试')
  } finally {
    shareLoading.value = false
  }
}

async function copyShareUrl() {
  if (!shareUrl.value) {
    return
  }
  try {
    await navigator.clipboard.writeText(shareUrl.value)
    ElMessage.success('分享链接已复制')
  } catch {
    ElMessage.warning('复制失败，请手动复制链接')
  }
}

function toFrontendShareUrl(url: string) {
  const token = url.split('/').filter(Boolean).pop()
  if (!token) {
    return url
  }
  return `${window.location.origin}/share/${token}`
}
</script>

<style scoped>
.result-panel {
  padding: 22px;
}

.result-header {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 18px;
}

.result-header h1 {
  margin: 0 0 8px;
  font-size: 24px;
}

.result-header p {
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
  display: block;
  margin-top: 6px;
  color: #62748e;
}

.result-layout {
  display: grid;
  grid-template-columns: minmax(220px, 320px) minmax(0, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.score-card,
.radar-card,
.analysis-card {
  border-radius: 8px;
}

.score-card :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
}

.card-title {
  font-size: 16px;
  font-weight: 700;
  color: #102a43;
}

.score-summary {
  margin: 0;
  color: #486581;
  line-height: 1.7;
  text-align: center;
}

.radar-chart {
  height: 280px;
}

.analysis-card {
  margin-bottom: 18px;
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
  background: #ffffff;
}

.analysis-section h3,
.reference-section h3,
.question-results h2,
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

.reference-section pre {
  margin: 10px 0 0;
  border-radius: 8px;
  background: #f4f6f8;
  padding: 14px;
  color: #334e68;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.7;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', monospace;
}

.raw-section pre {
  max-height: 280px;
  overflow: auto;
}

.question-results h2 {
  margin-bottom: 12px;
  font-size: 20px;
}

.question-result {
  border: 1px solid #d9e2ec;
  border-radius: 8px;
  background: #ffffff;
  padding: 16px;
}

.question-result + .question-result {
  margin-top: 12px;
}

.question-result__header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.question-result p {
  color: #486581;
  line-height: 1.7;
}

.plain-text {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: inherit;
}

.result-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 18px;
}

.share-hint {
  margin: 0 0 12px;
  color: #62748e;
  line-height: 1.6;
}

@media (max-width: 920px) {
  .result-layout,
  .analysis-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .result-header,
  .question-result__header,
  .result-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .score-box {
    text-align: left;
  }
}
</style>
