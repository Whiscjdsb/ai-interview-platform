<template>
  <div class="question-card">
    <div class="question-card__main">
      <h3>{{ question.title }}</h3>
      <div class="question-card__meta">
        <el-tag size="small">{{ questionTypeText(question.questionType) }}</el-tag>
        <el-tag size="small" :type="difficultyType(question.difficulty)">
          {{ difficultyLabel(question.difficulty) }}
        </el-tag>
        <span class="question-card__time">{{ formatDateTime(question.createTime) }}</span>
      </div>
      <div class="question-card__tags">
        <el-tag v-for="tag in question.tags" :key="tag.id" size="small" effect="plain">
          {{ tag.tagName }}
        </el-tag>
      </div>
    </div>

    <div class="question-card__actions">
      <el-button @click="$emit('detail', question.id)">查看详情</el-button>
      <el-button type="primary" @click="$emit('practice', question.id)">开始练习</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Question } from '@/types/question'
import { difficultyLabel, difficultyType, formatDateTime, questionTypeText } from '@/utils/question'

defineProps<{
  question: Question
}>()

defineEmits<{
  detail: [id: number]
  practice: [id: number]
}>()
</script>

<style scoped>
.question-card {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  border: 1px solid #d9e2ec;
  border-radius: 8px;
  background: #ffffff;
  padding: 18px;
}

.question-card + .question-card {
  margin-top: 12px;
}

.question-card__main {
  min-width: 0;
}

.question-card h3 {
  margin: 0 0 10px;
  font-size: 17px;
}

.question-card__meta,
.question-card__tags,
.question-card__actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.question-card__time {
  color: #62748e;
  font-size: 13px;
}

.question-card__tags {
  margin-top: 10px;
}

.question-card__actions {
  justify-content: flex-end;
  min-width: 190px;
}

@media (max-width: 760px) {
  .question-card {
    flex-direction: column;
  }

  .question-card__actions {
    justify-content: flex-start;
    min-width: 0;
  }
}
</style>
