<template>
  <div class="question-filter">
    <el-form :model="model" label-position="top" class="question-filter__form">
      <el-form-item label="关键词">
        <el-input v-model.trim="model.keyword" clearable placeholder="搜索标题或内容" @keyup.enter="emitSearch" />
      </el-form-item>

      <el-form-item label="题型">
        <el-select v-model="model.questionType" clearable placeholder="全部题型">
          <el-option
            v-for="option in QUESTION_TYPE_OPTIONS"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="难度">
        <el-select v-model="model.difficulty" clearable placeholder="全部难度">
          <el-option
            v-for="option in DIFFICULTY_OPTIONS"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="标签">
        <el-select v-model="model.tagId" clearable filterable placeholder="全部标签">
          <el-option v-for="tag in tags" :key="tag.id" :label="tag.tagName" :value="tag.id" />
        </el-select>
      </el-form-item>

      <div class="question-filter__actions">
        <el-button type="primary" @click="emitSearch">查询</el-button>
        <el-button @click="$emit('reset')">重置</el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import type { QuestionQuery, Tag } from '@/types/question'
import { DIFFICULTY_OPTIONS, QUESTION_TYPE_OPTIONS } from '@/utils/question'

defineProps<{
  model: QuestionQuery
  tags: Tag[]
}>()

const emit = defineEmits<{
  search: []
  reset: []
}>()

function emitSearch() {
  emit('search')
}
</script>

<style scoped>
.question-filter {
  border: 1px solid #d9e2ec;
  border-radius: 8px;
  background: #ffffff;
  padding: 16px 16px 2px;
  margin-bottom: 16px;
}

.question-filter__form {
  display: grid;
  grid-template-columns: minmax(220px, 1.4fr) repeat(3, minmax(150px, 1fr)) auto;
  gap: 12px;
  align-items: end;
}

.question-filter__actions {
  display: flex;
  gap: 8px;
  padding-bottom: 18px;
}

@media (max-width: 1080px) {
  .question-filter__form {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .question-filter__form {
    grid-template-columns: 1fr;
  }

  .question-filter__actions {
    padding-bottom: 14px;
  }
}
</style>
