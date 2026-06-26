<template>
  <div>
    <div class="page-header"><div><h1>AI 面试记录</h1><p>查看用户生成和提交的 AI 模拟面试。</p></div></div>
    <div class="filter-panel">
      <el-input v-model.trim="query.userKeyword" clearable placeholder="搜索用户" @keyup.enter="search" />
      <el-input v-model.trim="query.positionKeyword" clearable placeholder="搜索岗位" @keyup.enter="search" />
      <el-date-picker v-model="dateRange" type="datetimerange" start-placeholder="开始时间" end-placeholder="结束时间" value-format="YYYY-MM-DDTHH:mm:ss" clearable />
      <el-button type="primary" @click="search">查询</el-button><el-button @click="reset">重置</el-button>
    </div>
    <el-alert v-if="errorText" :title="errorText" type="error" show-icon :closable="false" />
    <section v-loading="loading">
      <el-table v-if="records.length" :data="records" class="admin-table">
        <el-table-column label="用户" min-width="130"><template #default="{ row }">{{ adminRecordUser(row) }}</template></el-table-column>
        <el-table-column label="岗位" min-width="220"><template #default="{ row }">{{ nullableText(row.position) }}</template></el-table-column>
        <el-table-column label="得分" width="100"><template #default="{ row }">{{ row.score ?? '未提交' }}</template></el-table-column>
        <el-table-column prop="modelName" label="模型" width="120" />
        <el-table-column label="创建时间" width="170"><template #default="{ row }">{{ formatDateTime(row.createTime) }}</template></el-table-column>
        <el-table-column label="操作" width="120" fixed="right"><template #default="{ row }"><el-button link type="primary" @click="openDetail(row)">查看详情</el-button></template></el-table-column>
      </el-table>
      <el-empty v-else-if="!loading" description="暂无 AI 面试记录" />
      <PaginationBar v-model:page="query.page" v-model:size="query.size" :total="total" @change="loadRecords" />
    </section>
    <el-dialog v-model="detailVisible" title="AI 面试记录详情" width="820px" destroy-on-close>
      <div v-loading="detailLoading">
        <template v-if="selectedDetail">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="用户">{{ adminRecordUser(selectedDetail) }}</el-descriptions-item>
            <el-descriptions-item label="岗位">{{ nullableText(selectedDetail.position) }}</el-descriptions-item>
            <el-descriptions-item label="难度">{{ nullableText(detailResult.difficulty) }}</el-descriptions-item>
            <el-descriptions-item label="总分">{{ detailResult.totalScore ?? selectedDetail.score ?? '未提交' }}</el-descriptions-item>
            <el-descriptions-item label="模型">{{ nullableText(selectedDetail.modelName) }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatDateTime(selectedDetail.createTime) }}</el-descriptions-item>
            <el-descriptions-item label="AI 总评" :span="2">{{ detailResult.summary || '暂无总评' }}</el-descriptions-item>
            <el-descriptions-item label="优点" :span="2">{{ formatList(detailResult.advantages) }}</el-descriptions-item>
            <el-descriptions-item label="不足" :span="2">{{ formatList(detailResult.disadvantages) }}</el-descriptions-item>
            <el-descriptions-item label="改进建议" :span="2">{{ formatList(detailResult.suggestions) }}</el-descriptions-item>
          </el-descriptions>
          <div class="question-detail-list">
            <el-empty v-if="!detailResult.questionResults.length" description="暂无逐题点评" />
            <div v-for="(item, index) in detailResult.questionResults" :key="`${item.questionId || index}-${index}`" class="question-detail-card">
              <div class="question-detail-title">第 {{ index + 1 }} 题 · {{ item.score ?? '-' }} 分</div>
              <p><strong>题目：</strong>{{ item.question || '-' }}</p>
              <p><strong>用户回答：</strong>{{ item.userAnswer || item.answer || '未作答' }}</p>
              <p><strong>AI 点评：</strong>{{ item.comment || item.review || '-' }}</p>
              <p><strong>改进建议：</strong>{{ formatList(item.improvements || item.suggestions) }}</p>
              <p><strong>参考思路：</strong>{{ item.referenceAnswer || item.suggestedAnswer || '-' }}</p>
            </div>
          </div>
        </template>
        <el-empty v-else-if="!detailLoading" description="暂无详情" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { getAdminAiInterviewDetailApi, listAdminAiInterviewsApi } from '@/api/admin'
import type { AdminAiInterviewDetail, AdminAiInterviewRecord, AdminRecordQuery } from '@/types/admin'
import { adminRecordUser, nullableText } from '@/utils/admin'
import { errorMessage } from '@/utils/error'
import { formatDateTime } from '@/utils/question'
import PaginationBar from './PaginationBar.vue'

interface AiQuestionResult {
  questionId?: number
  question?: string
  userAnswer?: string
  answer?: string
  score?: number
  comment?: string
  review?: string
  improvements?: string[]
  suggestions?: string[]
  referenceAnswer?: string
  suggestedAnswer?: string
}

interface AiResultContent {
  totalScore?: number
  difficulty?: string
  summary?: string
  advantages: string[]
  disadvantages: string[]
  suggestions: string[]
  questionResults: AiQuestionResult[]
}

const loading = ref(false)
const detailLoading = ref(false)
const errorText = ref('')
const records = ref<AdminAiInterviewRecord[]>([])
const total = ref(0)
const detailVisible = ref(false)
const selectedDetail = ref<AdminAiInterviewDetail | null>(null)
const dateRange = ref<[string, string] | [] | null>([])
const query = reactive<AdminRecordQuery>({ page: 1, size: 10, userKeyword: '', positionKeyword: '' })
const detailResult = computed(() => normalizeAiResult(selectedDetail.value?.result))
onMounted(loadRecords)
async function loadRecords() {
  loading.value = true
  errorText.value = ''
  try {
    const [startTime, endTime] = dateRange.value || []
    const data = await listAdminAiInterviewsApi({ ...query, userKeyword: query.userKeyword || undefined, positionKeyword: query.positionKeyword || undefined, startTime, endTime })
    records.value = data.records
    total.value = data.total
  } catch (error) {
    records.value = []
    total.value = 0
    errorText.value = errorMessage(error, '加载 AI 面试记录失败')
  } finally {
    loading.value = false
  }
}
function search() { query.page = 1; loadRecords() }
function reset() { query.page = 1; query.size = 10; query.userKeyword = ''; query.positionKeyword = ''; dateRange.value = []; loadRecords() }

async function openDetail(record: AdminAiInterviewRecord) {
  detailVisible.value = true
  detailLoading.value = true
  selectedDetail.value = null
  try {
    selectedDetail.value = await getAdminAiInterviewDetailApi(record.id)
  } catch (error) {
    errorText.value = errorMessage(error, '加载 AI 面试详情失败')
  } finally {
    detailLoading.value = false
  }
}

function normalizeAiResult(value: unknown): AiResultContent {
  if (!isRecord(value)) {
    return emptyAiResult()
  }
  return {
    totalScore: numberValue(value.totalScore),
    difficulty: stringValue(value.difficulty),
    summary: stringValue(value.summary),
    advantages: stringList(value.advantages),
    disadvantages: stringList(value.disadvantages || value.improvements),
    suggestions: stringList(value.suggestions || value.studyPlan),
    questionResults: questionList(value.questionResults || value.questions)
  }
}

function emptyAiResult(): AiResultContent {
  return { advantages: [], disadvantages: [], suggestions: [], questionResults: [] }
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null
}

function stringValue(value: unknown): string | undefined {
  return typeof value === 'string' ? value : undefined
}

function numberValue(value: unknown): number | undefined {
  return typeof value === 'number' ? value : undefined
}

function stringList(value: unknown): string[] {
  return Array.isArray(value) ? value.filter((item): item is string => typeof item === 'string') : []
}

function questionList(value: unknown): AiQuestionResult[] {
  if (!Array.isArray(value)) {
    return []
  }
  return value.filter(isRecord).map((item) => ({
    questionId: numberValue(item.questionId),
    question: stringValue(item.question),
    userAnswer: stringValue(item.userAnswer),
    answer: stringValue(item.answer),
    score: numberValue(item.score),
    comment: stringValue(item.comment),
    review: stringValue(item.review),
    improvements: stringList(item.improvements),
    suggestions: stringList(item.suggestions),
    referenceAnswer: stringValue(item.referenceAnswer),
    suggestedAnswer: stringValue(item.suggestedAnswer)
  }))
}

function formatList(items?: string[]) {
  return items?.length ? items.join('；') : '-'
}
</script>

<style scoped>
.filter-panel { display: grid; grid-template-columns: minmax(160px, 1fr) minmax(180px, 1fr) minmax(280px, 1.3fr) auto auto; gap: 10px; border: 1px solid #d9e2ec; border-radius: 8px; background: #fff; padding: 16px; margin-bottom: 16px; }
.admin-table { border: 1px solid #d9e2ec; border-radius: 8px; }
.question-detail-list { display: grid; gap: 12px; margin-top: 16px; }
.question-detail-card { border: 1px solid #d9e2ec; border-radius: 8px; padding: 14px; background: #fff; }
.question-detail-card p { margin: 8px 0 0; line-height: 1.7; word-break: break-word; }
.question-detail-title { font-weight: 700; color: #1f2a37; }
.el-alert { margin-bottom: 16px; }
@media (max-width: 980px) { .filter-panel { grid-template-columns: 1fr; } }
</style>
