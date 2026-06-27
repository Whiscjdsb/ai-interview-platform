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
  border-radius: 16px;
  background:
    linear-gradient(180deg, #ffffff 0%, #fbfdff 100%);
  padding: 18px;
  box-shadow: 0 8px 22px rgba(15, 23, 42, 0.04);
  transition:
    border-color 0.18s ease,
    box-shadow 0.18s ease,
    transform 0.18s ease;
}

.question-card:hover {
  border-color: #bfdbfe;
  box-shadow: 0 16px 38px rgba(37, 99, 235, 0.12);
  transform: translateY(-2px);
}

.question-card + .question-card {
  margin-top: 12px;
}

.question-card__main {
  min-width: 0;
  flex: 1;
}

.question-card h3 {
  margin: 0 0 10px;
  color: #102a43;
  font-size: 18px;
  line-height: 1.45;
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
  border-left: 1px solid #d9e2ec;
  padding-left: 10px;
  color: #62748e;
  font-size: 13px;
}

.question-card__tags {
  margin-top: 10px;
}

.question-card__tags :deep(.el-tag) {
  border: 0;
  border-radius: 999px;
  background: #eff6ff;
  color: #1d4ed8;
  font-weight: 600;
}

.question-card__tags :deep(.el-tag:nth-child(3n + 1)) {
  background: #ecfdf5;
  color: #047857;
}

.question-card__tags :deep(.el-tag:nth-child(3n + 2)) {
  background: #fff7ed;
  color: #c2410c;
}

.question-card__meta :deep(.el-tag) {
  border-radius: 999px;
  font-weight: 700;
}

.question-card__actions {
  justify-content: flex-end;
  min-width: 190px;
  align-content: center;
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
