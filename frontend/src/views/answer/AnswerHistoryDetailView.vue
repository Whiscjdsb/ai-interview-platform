<template>
  <div>
    <el-breadcrumb separator="/" class="answer-breadcrumb">
      <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item :to="{ path: '/answer-history' }">答题历史</el-breadcrumb-item>
      <el-breadcrumb-item>记录详情</el-breadcrumb-item>
    </el-breadcrumb>

    <section v-loading="loading" class="detail-panel panel">
      <template v-if="detail">
        <div class="detail-panel__header">
          <div>
            <h1>{{ detail.title }}</h1>
            <div class="detail-panel__meta">
              <el-tag>{{ questionTypeText(detail.questionType) }}</el-tag>
              <el-tag :type="difficultyType(detail.difficulty)">
                {{ difficultyLabel(detail.difficulty) }}
              </el-tag>
              <el-tag v-for="tag in detail.tags" :key="tag.id" effect="plain">{{ tag.tagName }}</el-tag>
            </div>
          </div>
          <div class="detail-panel__actions">
            <el-button @click="router.push('/answer-history')">返回答题历史</el-button>
            <el-button type="primary" @click="router.push(`/practice/${detail.questionId}`)">再次练习</el-button>
          </div>
        </div>

        <el-divider />

        <el-descriptions border :column="1">
          <el-descriptions-item label="题目内容">
            <pre class="detail-text">{{ detail.content }}</pre>
          </el-descriptions-item>
          <el-descriptions-item label="用户答案">
            <pre class="detail-text">{{ detail.userAnswer }}</pre>
          </el-descriptions-item>
          <el-descriptions-item label="判题结果">
            <el-tag :type="answerResultType(detail.isCorrect)">
              {{ answerResultText(detail.isCorrect) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="得分">{{ formatScore(detail.score) }}</el-descriptions-item>
          <el-descriptions-item label="答题耗时">{{ formatDuration(detail.answerDuration) }}</el-descriptions-item>
          <el-descriptions-item label="答题时间">{{ formatDateTime(detail.answerTime) }}</el-descriptions-item>
          <el-descriptions-item label="正确答案">
            <pre class="detail-text">{{ detail.correctAnswer || '暂无' }}</pre>
          </el-descriptions-item>
          <el-descriptions-item label="题目解析">
            <pre class="detail-text">{{ detail.analysis || '暂无解析' }}</pre>
          </el-descriptions-item>
        </el-descriptions>
      </template>

      <el-empty v-else-if="!loading" description="记录不存在、无权限或加载失败">
        <el-button type="primary" @click="router.push('/answer-history')">返回答题历史</el-button>
      </el-empty>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { getAnswerHistoryDetailApi } from '@/api/answer'
import type { AnswerHistoryDetail } from '@/types/answer'
import { answerResultText, answerResultType, formatDuration, formatScore } from '@/utils/answer'
import { difficultyLabel, difficultyType, formatDateTime, questionTypeText } from '@/utils/question'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref<AnswerHistoryDetail | null>(null)

onMounted(loadDetail)

async function loadDetail() {
  loading.value = true
  try {
    detail.value = await getAnswerHistoryDetailApi(route.params.id as string)
  } catch {
    detail.value = null
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.answer-breadcrumb {
  margin-bottom: 16px;
}

.detail-panel {
  padding: 22px;
  min-height: 360px;
}

.detail-panel__header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

.detail-panel h1 {
  margin: 0 0 12px;
  font-size: 24px;
}

.detail-panel__meta,
.detail-panel__actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.detail-panel__actions {
  justify-content: flex-end;
  align-items: flex-start;
}

.detail-text {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.75;
  font-family: inherit;
}

@media (max-width: 760px) {
  .detail-panel__header {
    flex-direction: column;
  }

  .detail-panel__actions {
    justify-content: flex-start;
  }
}
</style>
