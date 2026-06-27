<template>
  <div class="result-page">
    <section v-if="!pageReady" class="report-shell panel">
      <el-skeleton animated>
        <template #template>
          <div class="report-skeleton-hero">
            <div>
              <el-skeleton-item variant="text" style="width: 140px" />
              <el-skeleton-item variant="h1" style="width: 360px; margin-top: 14px" />
              <el-skeleton-item variant="text" style="width: 520px; margin-top: 14px" />
            </div>
            <el-skeleton-item variant="circle" style="width: 140px; height: 140px" />
          </div>
          <div class="report-skeleton-grid">
            <el-skeleton-item v-for="item in 4" :key="item" variant="rect" style="height: 156px" />
          </div>
        </template>
      </el-skeleton>
    </section>

    <section v-else-if="result" class="report-shell">
      <header class="report-hero panel">
        <div class="report-hero__content">
          <span class="report-eyebrow">AI Interview Report</span>
          <h1>{{ result.position }} 面试分析报告</h1>
          <p>
            基于本场 AI 模拟面试的回答质量、追问表现和结构化反馈生成，适合复盘知识短板与下一轮练习重点。
          </p>

          <div class="report-meta">
            <span>{{ difficultyText(result.difficulty) }}</span>
            <span>{{ providerName }}</span>
            <span>{{ completionTime }}</span>
          </div>
        </div>

        <div class="report-hero__score" :class="scoreBandClass(displayScore)">
          <div class="score-ring" :style="scoreRingStyle">
            <div class="score-ring__inner">
              <strong>{{ displayScore }}</strong>
              <span>/ 100</span>
            </div>
          </div>
          <div>
            <span class="score-level">{{ displayLevel }}</span>
            <p>{{ scoreDescription }}</p>
          </div>
        </div>
      </header>

      <div class="report-actions">
        <div>
          <strong>报告操作</strong>
          <span>导出 PDF、生成分享链接，或重新开始一场面试</span>
        </div>
        <div class="report-actions__buttons">
          <el-button :loading="shareLoading" type="warning" @click="createShareLink">
            生成分享链接
          </el-button>
          <el-button :loading="pdfLoading" type="success" @click="downloadPdf">
            下载 PDF 报告
          </el-button>
          <el-button @click="router.push('/ai-interview')">重新开始</el-button>
          <el-button type="primary" @click="router.push('/dashboard')">返回首页</el-button>
        </div>
      </div>

      <div class="kpi-grid">
        <section v-for="card in kpiCards" :key="card.label" class="kpi-card ai-hover-lift">
          <span>{{ card.label }}</span>
          <strong>{{ card.value }}</strong>
          <p>{{ card.description }}</p>
        </section>
      </div>

      <div class="report-grid">
        <el-card shadow="never" class="score-card report-card">
          <template #header>
            <div class="card-title">
              <span>总分概览</span>
              <el-tag :color="scoreBandColor(displayScore)" effect="dark">{{ displayLevel }}</el-tag>
            </div>
          </template>

          <div class="score-overview">
            <el-progress
              type="dashboard"
              :percentage="displayScore"
              :color="scoreBandColor(displayScore)"
              :width="168"
            />
            <p>{{ result.summary || fallbackSummary }}</p>
          </div>
        </el-card>

        <el-card shadow="never" class="radar-card report-card">
          <template #header>
            <div class="card-title">
              <span>能力雷达图</span>
              <small>前端基于总分映射的演示模型</small>
            </div>
          </template>
          <div v-if="displayScore > 0" ref="radarChartRef" class="radar-chart" />
          <el-empty v-else description="暂无可用于生成雷达图的评分数据" :image-size="92" />
          <p class="radar-note">
            雷达图将总分映射到 Java 基础、Spring Boot、数据库、系统设计和项目经验五个维度，用于快速判断复盘优先级。
          </p>
        </el-card>
      </div>

      <section class="analysis-card panel">
        <div class="section-heading">
          <div>
            <span class="report-eyebrow">AI Analysis</span>
            <h2>分析摘要</h2>
          </div>
          <p>优先使用结构化结果，缺失时回退到原有字段或友好兜底文案。</p>
        </div>

        <div class="analysis-grid">
          <article
            v-for="group in analysisGroups"
            :key="group.title"
            class="analysis-section"
            :class="group.className"
          >
            <div class="analysis-section__header">
              <span>{{ group.icon }}</span>
              <h3>{{ group.title }}</h3>
            </div>
            <ul v-if="group.items.length">
              <li v-for="item in group.items" :key="item">{{ item }}</li>
            </ul>
            <p v-else class="empty-copy">{{ group.emptyText }}</p>
          </article>
        </div>

        <div class="reference-section">
          <div class="reference-section__header">
            <h3>参考答案 / 推荐复习方向</h3>
            <span>{{ referenceAnswer ? '已生成' : '暂无结构化参考答案' }}</span>
          </div>
          <pre>{{ referenceAnswer || fallbackReferenceAnswer }}</pre>
        </div>

        <div v-if="showRawResponse" class="reference-section raw-section">
          <div class="reference-section__header">
            <h3>原始 AI 返回</h3>
            <span>结构化解析失败时保留</span>
          </div>
          <pre>{{ rawResponse }}</pre>
        </div>
      </section>

      <section v-if="questionResults.length" class="question-results panel">
        <div class="section-heading">
          <div>
            <span class="report-eyebrow">Question Review</span>
            <h2>每题评分反馈</h2>
          </div>
          <p>按面试过程整理每道题的回答、评分和 AI 建议。</p>
        </div>

        <div class="question-timeline">
          <article v-for="item in questionResults" :key="item.questionNo" class="question-result">
            <div class="question-result__marker">{{ item.questionNo }}</div>
            <div class="question-result__body">
              <div class="question-result__header">
                <div>
                  <span class="question-label">第 {{ item.questionNo }} 题</span>
                  <h3>{{ item.question }}</h3>
                </div>
                <div class="question-score" :style="{ '--question-score-color': scoreBandColor(item.score || 0) }">
                  <strong>{{ formatScore(item.score) }}</strong>
                  <span>分</span>
                </div>
              </div>

              <p class="question-review">{{ item.review || '暂无单题点评，建议结合参考答案继续复盘。' }}</p>

              <el-collapse class="question-collapse">
                <el-collapse-item title="查看回答与 AI 建议" :name="String(item.questionNo)">
                  <div class="answer-grid">
                    <section>
                      <h4>我的回答</h4>
                      <pre>{{ item.userAnswer || item.answer || '未作答' }}</pre>
                    </section>
                    <section>
                      <h4>AI 建议 / 参考思路</h4>
                      <pre>{{ item.suggestedAnswer || item.referenceAnswer || '暂无建议' }}</pre>
                    </section>
                  </div>

                  <div class="mini-analysis-grid">
                    <section>
                      <h4>优点</h4>
                      <ul v-if="item.advantages?.length">
                        <li v-for="advantage in item.advantages" :key="advantage">{{ advantage }}</li>
                      </ul>
                      <p v-else>暂无优点分析</p>
                    </section>
                    <section>
                      <h4>不足</h4>
                      <ul v-if="item.improvements?.length">
                        <li v-for="improvement in item.improvements" :key="improvement">{{ improvement }}</li>
                      </ul>
                      <p v-else>暂无不足分析</p>
                    </section>
                  </div>
                </el-collapse-item>
              </el-collapse>
            </div>
          </article>
        </div>
      </section>

      <section v-else class="empty-report panel">
        <el-empty description="暂无单题评分数据">
          <el-button type="primary" @click="router.push('/ai-interview')">重新开始一场面试</el-button>
        </el-empty>
      </section>

      <el-dialog v-model="shareDialogVisible" title="分享面试报告" width="520px">
        <p class="share-hint">
          复制以下链接后，拥有链接的人可以只读查看这份面试报告。链接不会暴露登录 Token 或后台管理数据。
        </p>
        <el-input v-model="shareUrl" readonly>
          <template #append>
            <el-button @click="copyShareUrl">复制</el-button>
          </template>
        </el-input>
      </el-dialog>
    </section>

    <section v-else class="empty-report panel">
      <el-empty description="未找到面试结果">
        <el-button type="primary" @click="router.push('/ai-interview')">重新开始</el-button>
      </el-empty>
    </section>
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
const pageReady = ref(false)
let radarChart: echarts.ECharts | null = null

const structuredResult = computed(() => result.value?.structuredResult || null)
const questionResults = computed(() => result.value?.questionResults || [])
const displayScore = computed(() => normalizeScore(structuredResult.value?.score ?? result.value?.totalScore ?? 0))
const displayLevel = computed(() => scoreBandText(displayScore.value) || result.value?.level || scoreLevel(displayScore.value))
const providerName = computed(() => result.value?.modelName || 'AI Provider')
const completionTime = computed(() => formatInterviewTime(result.value?.createTime) || '刚刚完成')
const fallbackSummary = computed(
  () => `本次面试总分为 ${displayScore.value} 分，建议结合每题反馈继续复盘回答结构和关键技术点。`
)
const fallbackReferenceAnswer = computed(
  () => '建议按照“核心概念、关键原理、实际场景、风险边界、项目经验”的结构重新组织答案。'
)
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
  return questionResults.value.find((item) => item.suggestedAnswer)?.suggestedAnswer || ''
})
const showRawResponse = computed(() => !structuredResult.value && Boolean(rawResponse.value))
const scoreDescription = computed(() => {
  const score = displayScore.value
  if (score >= 90) {
    return '表现优秀，回答完整度和表达结构都比较成熟。'
  }
  if (score >= 70) {
    return '整体表现良好，建议继续补充底层原理和项目案例。'
  }
  if (score >= 40) {
    return '基础方向可见，但回答深度和关键技术点还需要加强。'
  }
  return '当前表现较弱，建议先补齐核心概念，再进行专项练习。'
})
const scoreRingStyle = computed(() => ({
  '--score-color': scoreBandColor(displayScore.value),
  '--score-deg': `${displayScore.value * 3.6}deg`
}))
const kpiCards = computed(() => [
  {
    label: '单题反馈',
    value: `${questionResults.value.length} 题`,
    description: '覆盖本场面试中的逐题评分与复盘建议'
  },
  {
    label: 'AI Provider',
    value: providerName.value,
    description: '保留模型来源，便于区分 DeepSeek / Mock 演示'
  },
  {
    label: '报告状态',
    value: structuredResult.value ? '结构化' : rawResponse.value ? '原始返回' : '本地结果',
    description: '结构化结果优先展示，解析失败时安全回退'
  },
  {
    label: '复盘重点',
    value: suggestions.value[0] ? '已生成' : '待补充',
    description: suggestions.value[0] || '建议从低分题和薄弱维度开始复盘'
  }
])
const analysisGroups = computed(() => [
  {
    title: '优势总结',
    icon: '✓',
    items: strengths.value,
    emptyText: '暂无优势总结，可结合每题点评继续复盘。',
    className: 'analysis-section--green'
  },
  {
    title: '主要不足',
    icon: '!',
    items: weaknesses.value,
    emptyText: '暂无不足分析，建议关注低分题。',
    className: 'analysis-section--red'
  },
  {
    title: '改进建议',
    icon: '→',
    items: suggestions.value,
    emptyText: '暂无改进建议，可以先按题目参考思路整理答案。',
    className: 'analysis-section--blue'
  }
])

onMounted(async () => {
  result.value = getInterviewResult(route.params.id as string)
  pageReady.value = true
  await nextTick()
  renderRadarChart()
  window.addEventListener('resize', resizeRadarChart)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeRadarChart)
  radarChart?.dispose()
})

function renderRadarChart() {
  if (!radarChartRef.value || !result.value || displayScore.value <= 0) {
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

function formatScore(value: number | null | undefined) {
  return normalizeScore(value ?? 0)
}

function scoreBandColor(score: number) {
  return scoreColor(normalizeScore(score))
}

function scoreBandText(score: number) {
  const value = normalizeScore(score)
  if (value >= 90) {
    return '优秀'
  }
  if (value >= 70) {
    return '良好'
  }
  if (value >= 40) {
    return '中等'
  }
  return '较弱'
}

function scoreBandClass(score: number) {
  const value = normalizeScore(score)
  if (value >= 90) {
    return 'score-band--excellent'
  }
  if (value >= 70) {
    return 'score-band--good'
  }
  if (value >= 40) {
    return 'score-band--medium'
  }
  return 'score-band--weak'
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
    ElMessage.success('PDF 报告已开始下载')
  } catch {
    ElMessage.error('PDF 报告下载失败，请确认面试已提交并稍后重试')
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
    ElMessage.success('分享链接已生成')
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
.result-page {
  padding-bottom: var(--ai-space-2);
}

.report-shell {
  display: flex;
  flex-direction: column;
  gap: var(--ai-space-2);
}

.panel {
  border: 1px solid rgba(217, 226, 236, 0.9);
  border-radius: var(--ai-radius-lg);
  background: rgba(255, 255, 255, 0.96);
  box-shadow: var(--ai-shadow-soft);
}

.report-skeleton-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--ai-space-3);
  padding: var(--ai-space-3);
}

.report-skeleton-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--ai-space-2);
  padding: 0 var(--ai-space-3) var(--ai-space-3);
}

.report-hero {
  display: flex;
  justify-content: space-between;
  gap: var(--ai-space-3);
  overflow: hidden;
  padding: var(--ai-space-3);
  background:
    radial-gradient(circle at 88% 16%, rgba(124, 58, 237, 0.2), transparent 26%),
    radial-gradient(circle at 12% 18%, rgba(37, 99, 235, 0.14), transparent 28%),
    linear-gradient(135deg, #ffffff 0%, #f8fbff 48%, #eef6ff 100%);
}

.report-hero__content {
  max-width: 760px;
}

.report-eyebrow {
  display: inline-flex;
  align-items: center;
  border-radius: var(--ai-radius-pill);
  background: rgba(37, 99, 235, 0.1);
  padding: 5px 10px;
  color: var(--ai-color-primary);
  font-size: var(--ai-font-size-xs);
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.report-hero h1 {
  margin: 14px 0 10px;
  font-size: 30px;
}

.report-hero p {
  margin: 0;
  color: var(--ai-text-secondary);
  line-height: 1.8;
}

.report-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: var(--ai-space-2);
}

.report-meta span {
  border: 1px solid rgba(217, 226, 236, 0.9);
  border-radius: var(--ai-radius-pill);
  background: rgba(255, 255, 255, 0.78);
  padding: 7px 12px;
  color: var(--ai-text-secondary);
  font-size: var(--ai-font-size-sm);
  font-weight: 700;
}

.report-hero__score {
  display: flex;
  align-items: center;
  gap: var(--ai-space-2);
  min-width: 320px;
  border: 1px solid rgba(217, 226, 236, 0.9);
  border-radius: var(--ai-radius-lg);
  background: rgba(255, 255, 255, 0.86);
  padding: var(--ai-space-2);
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.08);
}

.score-ring {
  display: grid;
  width: 132px;
  height: 132px;
  place-items: center;
  border-radius: 50%;
  background: conic-gradient(var(--score-color) var(--score-deg), #e5edf5 0deg);
}

.score-ring__inner {
  display: grid;
  width: 96px;
  height: 96px;
  place-items: center;
  border-radius: 50%;
  background: #ffffff;
  line-height: 1;
}

.score-ring__inner strong {
  font-size: 34px;
  font-weight: 850;
}

.score-ring__inner span {
  color: var(--ai-text-muted);
  font-size: var(--ai-font-size-xs);
  font-weight: 700;
}

.score-level {
  display: block;
  color: var(--score-color);
  font-size: var(--ai-font-size-xl);
  font-weight: 850;
}

.report-hero__score p {
  margin-top: 8px;
  font-size: var(--ai-font-size-sm);
}

.score-band--excellent {
  --score-color: var(--ai-color-success);
}

.score-band--good {
  --score-color: var(--ai-color-primary);
}

.score-band--medium {
  --score-color: var(--ai-color-warning);
}

.score-band--weak {
  --score-color: var(--ai-color-danger);
}

.report-actions {
  position: sticky;
  top: 12px;
  z-index: 8;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--ai-space-2);
  border: 1px solid rgba(217, 226, 236, 0.9);
  border-radius: var(--ai-radius-md);
  background: rgba(255, 255, 255, 0.9);
  padding: 12px var(--ai-space-2);
  box-shadow: var(--ai-shadow-card);
  backdrop-filter: blur(14px);
}

.report-actions strong,
.report-actions span {
  display: block;
}

.report-actions strong {
  color: var(--ai-text-primary);
}

.report-actions span {
  color: var(--ai-text-muted);
  font-size: var(--ai-font-size-xs);
}

.report-actions__buttons {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--ai-space-2);
}

.kpi-card {
  border: 1px solid rgba(217, 226, 236, 0.9);
  border-radius: var(--ai-radius-md);
  background: var(--ai-bg-card);
  padding: var(--ai-space-2);
  box-shadow: var(--ai-shadow-soft);
}

.kpi-card span {
  color: var(--ai-text-muted);
  font-size: var(--ai-font-size-xs);
  font-weight: 800;
}

.kpi-card strong {
  display: block;
  margin-top: 8px;
  color: var(--ai-text-primary);
  font-size: var(--ai-font-size-xl);
}

.kpi-card p {
  margin: 8px 0 0;
  color: var(--ai-text-secondary);
  font-size: var(--ai-font-size-sm);
  line-height: 1.7;
}

.report-grid {
  display: grid;
  grid-template-columns: minmax(280px, 360px) minmax(0, 1fr);
  gap: var(--ai-space-2);
}

.report-card {
  border-radius: var(--ai-radius-md);
  box-shadow: var(--ai-shadow-soft);
}

.card-title,
.section-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--ai-space-2);
}

.card-title span {
  font-size: var(--ai-font-size-md);
  font-weight: 800;
}

.card-title small {
  color: var(--ai-text-muted);
  font-size: var(--ai-font-size-xs);
}

.score-overview {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--ai-space-2);
  text-align: center;
}

.score-overview p {
  margin: 0;
  color: var(--ai-text-secondary);
  line-height: 1.8;
}

.radar-chart {
  height: 300px;
}

.radar-note {
  margin: 0 0 4px;
  color: var(--ai-text-muted);
  font-size: var(--ai-font-size-sm);
  line-height: 1.7;
}

.analysis-card,
.question-results,
.empty-report {
  padding: var(--ai-space-3);
}

.section-heading {
  margin-bottom: var(--ai-space-2);
}

.section-heading h2 {
  margin: 8px 0 0;
}

.section-heading p {
  max-width: 560px;
  margin: 0;
  color: var(--ai-text-muted);
  line-height: 1.7;
}

.analysis-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--ai-space-2);
}

.analysis-section {
  border: 1px solid var(--ai-border-soft);
  border-radius: var(--ai-radius-md);
  padding: var(--ai-space-2);
  background: #ffffff;
  min-height: 170px;
}

.analysis-section--green {
  border-color: #bbf7d0;
  background: linear-gradient(180deg, #f0fdf4, #ffffff);
}

.analysis-section--red {
  border-color: #fecaca;
  background: linear-gradient(180deg, #fef2f2, #ffffff);
}

.analysis-section--blue {
  border-color: #bfdbfe;
  background: linear-gradient(180deg, #eff6ff, #ffffff);
}

.analysis-section__header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.analysis-section__header span {
  display: grid;
  width: 28px;
  height: 28px;
  place-items: center;
  border-radius: var(--ai-radius-pill);
  background: rgba(255, 255, 255, 0.86);
  color: var(--ai-color-primary);
  font-weight: 900;
}

.analysis-section h3,
.reference-section h3,
.question-result h3,
.answer-grid h4,
.mini-analysis-grid h4 {
  margin: 0;
}

.analysis-section ul,
.mini-analysis-grid ul {
  margin: 12px 0 0;
  padding-left: 18px;
  color: var(--ai-text-secondary);
  line-height: 1.8;
}

.empty-copy,
.mini-analysis-grid p {
  margin: 12px 0 0;
  color: var(--ai-text-muted);
  line-height: 1.7;
}

.reference-section {
  margin-top: var(--ai-space-2);
  border: 1px solid var(--ai-border-soft);
  border-radius: var(--ai-radius-md);
  background: var(--ai-bg-card-muted);
  padding: var(--ai-space-2);
}

.reference-section__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--ai-space-2);
}

.reference-section__header span {
  color: var(--ai-text-muted);
  font-size: var(--ai-font-size-xs);
  font-weight: 700;
}

.reference-section pre,
.answer-grid pre {
  margin: 12px 0 0;
  color: var(--ai-text-secondary);
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.75;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', monospace;
}

.raw-section pre {
  max-height: 280px;
  overflow: auto;
}

.question-timeline {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: var(--ai-space-2);
  padding-left: 24px;
}

.question-timeline::before {
  content: '';
  position: absolute;
  left: 11px;
  top: 12px;
  bottom: 12px;
  width: 2px;
  border-radius: var(--ai-radius-pill);
  background: linear-gradient(180deg, var(--ai-color-primary-subtle), #ddd6fe);
}

.question-result {
  position: relative;
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  gap: var(--ai-space-2);
}

.question-result__marker {
  position: relative;
  z-index: 1;
  display: grid;
  width: 24px;
  height: 24px;
  place-items: center;
  border: 3px solid #ffffff;
  border-radius: var(--ai-radius-pill);
  background: linear-gradient(135deg, var(--ai-color-primary), var(--ai-color-accent));
  color: #ffffff;
  font-size: 11px;
  font-weight: 900;
  box-shadow: 0 0 0 3px var(--ai-color-primary-subtle);
}

.question-result__body {
  border: 1px solid rgba(217, 226, 236, 0.9);
  border-radius: var(--ai-radius-md);
  background: #ffffff;
  padding: var(--ai-space-2);
  box-shadow: var(--ai-shadow-soft);
}

.question-result__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--ai-space-2);
}

.question-label {
  display: inline-block;
  margin-bottom: 8px;
  color: var(--ai-color-primary);
  font-size: var(--ai-font-size-xs);
  font-weight: 850;
}

.question-result h3 {
  line-height: 1.5;
}

.question-score {
  display: flex;
  align-items: baseline;
  gap: 3px;
  min-width: 72px;
  justify-content: center;
  border: 1px solid color-mix(in srgb, var(--question-score-color), white 68%);
  border-radius: var(--ai-radius-sm);
  background: color-mix(in srgb, var(--question-score-color), white 90%);
  padding: 8px 10px;
  color: var(--question-score-color);
}

.question-score strong {
  font-size: 24px;
  line-height: 1;
}

.question-score span {
  font-size: var(--ai-font-size-xs);
  font-weight: 800;
}

.question-review {
  margin: 12px 0;
  color: var(--ai-text-secondary);
  line-height: 1.8;
}

.question-collapse {
  border-top: 1px solid var(--ai-border-soft);
  border-bottom: 0;
}

.question-collapse :deep(.el-collapse-item__header) {
  color: var(--ai-text-primary);
  font-weight: 750;
}

.answer-grid,
.mini-analysis-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--ai-space-2);
}

.answer-grid section,
.mini-analysis-grid section {
  border: 1px solid var(--ai-border-soft);
  border-radius: var(--ai-radius-sm);
  background: var(--ai-bg-card-muted);
  padding: var(--ai-space-2);
}

.mini-analysis-grid {
  margin-top: var(--ai-space-2);
}

.share-hint {
  margin: 0 0 12px;
  color: var(--ai-text-muted);
  line-height: 1.7;
}

@media (max-width: 1180px) {
  .report-hero,
  .report-actions,
  .section-heading {
    flex-direction: column;
    align-items: stretch;
  }

  .report-hero__score {
    min-width: 0;
  }

  .kpi-grid,
  .report-skeleton-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .report-grid,
  .analysis-grid,
  .answer-grid,
  .mini-analysis-grid {
    grid-template-columns: 1fr;
  }

  .report-actions__buttons {
    justify-content: flex-start;
  }
}

@media (max-width: 760px) {
  .report-hero,
  .analysis-card,
  .question-results,
  .empty-report {
    padding: var(--ai-space-2);
  }

  .report-hero__score,
  .question-result__header {
    flex-direction: column;
    align-items: stretch;
  }

  .score-ring {
    width: 116px;
    height: 116px;
  }

  .score-ring__inner {
    width: 84px;
    height: 84px;
  }

  .kpi-grid,
  .report-skeleton-grid {
    grid-template-columns: 1fr;
  }

  .report-actions {
    position: static;
  }

  .report-actions__buttons .el-button {
    width: 100%;
    margin-left: 0;
  }
}
</style>
