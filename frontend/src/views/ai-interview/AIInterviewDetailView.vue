<template>
  <div>
    <el-breadcrumb separator="/" class="ai-breadcrumb">
      <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item :to="{ path: '/ai-interview' }">AI 面试助手</el-breadcrumb-item>
      <el-breadcrumb-item :to="{ path: '/ai-interview/history' }">历史记录</el-breadcrumb-item>
      <el-breadcrumb-item>详情</el-breadcrumb-item>
    </el-breadcrumb>

    <section v-loading="loading" class="detail-panel panel">
      <template v-if="detail">
        <div class="detail-header">
          <div>
            <h1>{{ detail.questionTitle || '模拟面试记录' }}</h1>
            <p>{{ detail.modelName }} · {{ formatInterviewTime(detail.createTime) }}</p>
          </div>
          <el-button type="primary" @click="router.push('/ai-interview/history')">返回历史</el-button>
        </div>

        <el-divider />

        <template v-if="questionDetails.length">
          <div v-for="item in questionDetails" :key="item.questionNo" class="detail-question">
            <div class="detail-question__header">
              <h2>第 {{ item.questionNo }} 题：{{ item.question }}</h2>
              <el-tag v-if="item.score !== null" :color="scoreColor(item.score || 0)" effect="dark">
                {{ item.score }} 分
              </el-tag>
            </div>
            <el-descriptions border :column="1">
              <el-descriptions-item label="我的回答">
                <pre class="plain-text">{{ item.answer || '暂无回答记录' }}</pre>
              </el-descriptions-item>
              <el-descriptions-item label="AI 回答建议">
                <pre class="plain-text">{{ item.suggestedAnswer || '暂无建议' }}</pre>
              </el-descriptions-item>
              <el-descriptions-item label="AI 点评">
                <pre class="plain-text">{{ item.review || '暂无点评' }}</pre>
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </template>

        <pre v-else class="raw-json">{{ formattedResult }}</pre>
      </template>

      <el-empty v-else-if="!loading" description="历史记录不存在或无权限访问">
        <el-button type="primary" @click="router.push('/ai-interview/history')">返回历史</el-button>
      </el-empty>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { getInterviewDetail } from '@/api/ai-interview'
import type { InterviewHistoryDetail, InterviewQuestionResult } from '@/types/ai-interview'
import { formatInterviewTime, scoreColor } from '@/utils/ai-interview'

interface ResultWithQuestions {
  questions?: Array<{
    questionNo?: number
    question?: string
    referencePoints?: string[]
  }>
  questionResults?: InterviewQuestionResult[]
}

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref<InterviewHistoryDetail | null>(null)

const questionDetails = computed<InterviewQuestionResult[]>(() => {
  if (!detail.value || !isResultWithQuestions(detail.value.result)) {
    return []
  }
  if (detail.value.result.questionResults?.length) {
    return detail.value.result.questionResults
  }
  return (detail.value.result.questions || []).map((item) => ({
    questionNo: item.questionNo || 0,
    question: item.question || '未命名题目',
    answer: '',
    score: null,
    review: '后端历史记录仅保存了题目生成结果，尚未保存整场面试作答和评分。',
    advantages: [],
    improvements: [],
    suggestedAnswer: item.referencePoints?.length
      ? `建议覆盖：${item.referencePoints.join('、')}`
      : '暂无建议'
  }))
})

const formattedResult = computed(() => JSON.stringify(detail.value?.result || {}, null, 2))

onMounted(loadDetail)

async function loadDetail() {
  loading.value = true
  try {
    detail.value = await getInterviewDetail(route.params.id as string)
  } catch {
    detail.value = null
  } finally {
    loading.value = false
  }
}

function isResultWithQuestions(value: unknown): value is ResultWithQuestions {
  return typeof value === 'object' && value !== null
}
</script>

<style scoped>
.ai-breadcrumb {
  margin-bottom: 16px;
}

.detail-panel {
  min-height: 360px;
  padding: 22px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

.detail-header h1 {
  margin: 0 0 8px;
  font-size: 24px;
}

.detail-header p {
  margin: 0;
  color: #62748e;
}

.detail-question {
  border: 1px solid #d9e2ec;
  border-radius: 8px;
  padding: 16px;
}

.detail-question + .detail-question {
  margin-top: 12px;
}

.detail-question__header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.detail-question h2 {
  margin: 0;
  font-size: 18px;
  line-height: 1.5;
}

.plain-text,
.raw-json {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: inherit;
}

.raw-json {
  border: 1px solid #d9e2ec;
  border-radius: 8px;
  background: #f8fafc;
  padding: 16px;
  color: #486581;
}

@media (max-width: 760px) {
  .detail-header,
  .detail-question__header {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
