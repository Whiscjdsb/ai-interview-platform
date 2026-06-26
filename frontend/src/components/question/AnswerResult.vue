<template>
  <div class="answer-result">
    <div class="answer-result__header">
      <el-result
        :icon="resultIcon"
        :title="resultTitle"
        :sub-title="result.message || resultSubtitle"
      />
    </div>

    <el-descriptions border :column="1">
      <el-descriptions-item label="用户答案">
        <pre class="answer-result__text">{{ userAnswer }}</pre>
      </el-descriptions-item>
      <el-descriptions-item label="得分">
        {{ result.score ?? '待评估' }}
      </el-descriptions-item>
      <el-descriptions-item label="正确答案">
        <pre class="answer-result__text">{{ result.correctAnswer || '暂无' }}</pre>
      </el-descriptions-item>
      <el-descriptions-item label="题目解析">
        <pre class="answer-result__text">{{ result.analysis || '暂无解析' }}</pre>
      </el-descriptions-item>
    </el-descriptions>

    <div class="answer-result__actions">
      <el-button @click="$emit('retry')">再做一次</el-button>
      <el-button @click="$emit('back')">返回题库</el-button>
      <el-button type="primary" @click="$emit('next')">下一题</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

import type { AnswerSubmitResult } from '@/types/answer'

const props = defineProps<{
  result: AnswerSubmitResult
  userAnswer: string
}>()

defineEmits<{
  retry: []
  back: []
  next: []
}>()

const resultIcon = computed(() => {
  if (props.result.isCorrect === true) {
    return 'success'
  }
  if (props.result.isCorrect === false) {
    return 'error'
  }
  return 'info'
})

const resultTitle = computed(() => {
  if (props.result.isCorrect === true) {
    return '回答正确'
  }
  if (props.result.isCorrect === false) {
    return '回答错误'
  }
  return '已提交，等待评估'
})

const resultSubtitle = computed(() =>
  props.result.isCorrect === null ? '等待 AI 点评或人工评估' : '判题完成'
)
</script>

<style scoped>
.answer-result {
  border: 1px solid #d9e2ec;
  border-radius: 8px;
  background: #ffffff;
  padding: 8px 18px 18px;
}

.answer-result__header {
  border-bottom: 1px solid #edf2f7;
  margin-bottom: 18px;
}

.answer-result__text {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: inherit;
}

.answer-result__actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 18px;
  flex-wrap: wrap;
}
</style>
