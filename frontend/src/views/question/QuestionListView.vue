<template>
  <div>
    <div class="page-header">
      <div>
        <h1>题库</h1>
        <p>按题型、难度和知识标签筛选练习题。</p>
      </div>
    </div>

    <QuestionFilter :model="query" :tags="tags" @search="search" @reset="reset" />

    <el-alert v-if="errorText" :title="errorText" type="error" show-icon :closable="false" />

    <section v-loading="loading" class="question-list-panel">
      <div v-if="loading && !questions.length" class="question-skeleton-list">
        <el-skeleton v-for="item in 5" :key="item" animated>
          <template #template>
            <div class="question-skeleton-card">
              <el-skeleton-item variant="h3" style="width: 48%" />
              <div class="skeleton-meta">
                <el-skeleton-item variant="text" style="width: 76px" />
                <el-skeleton-item variant="text" style="width: 64px" />
                <el-skeleton-item variant="text" style="width: 92px" />
              </div>
              <el-skeleton-item variant="text" style="width: 86%" />
            </div>
          </template>
        </el-skeleton>
      </div>

      <template v-else-if="questions.length">
        <div class="list-toolbar">
          <div>
            <strong>题目列表</strong>
            <span>共 {{ total }} 道题，当前第 {{ query.page }} 页</span>
          </div>
          <el-tag type="info" effect="plain">每页 {{ query.size }} 道</el-tag>
        </div>

        <QuestionCard
          v-for="question in questions"
          :key="question.id"
          :question="question"
          @detail="goDetail"
          @practice="goPractice"
        />

        <div class="pagination-row">
          <el-pagination
            v-model:current-page="query.page"
            v-model:page-size="query.size"
            :total="total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadQuestions"
            @current-change="loadQuestions"
          />
        </div>
      </template>

      <el-empty v-else-if="!loading" description="暂无符合条件的题目">
        <el-button type="primary" @click="reset">重置筛选</el-button>
      </el-empty>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

import { listQuestionsApi, listTagsApi } from '@/api/question'
import QuestionCard from '@/components/question/QuestionCard.vue'
import QuestionFilter from '@/components/question/QuestionFilter.vue'
import type { Question, QuestionQuery, Tag } from '@/types/question'
import { errorMessage } from '@/utils/error'

const router = useRouter()
const loading = ref(false)
const errorText = ref('')
const questions = ref<Question[]>([])
const tags = ref<Tag[]>([])
const total = ref(0)

const query = reactive<QuestionQuery>({
  page: 1,
  size: 10,
  keyword: '',
  questionType: '',
  difficulty: '',
  tagId: ''
})

onMounted(async () => {
  await Promise.all([loadTags(), loadQuestions()])
})

async function loadTags() {
  try {
    tags.value = await listTagsApi()
  } catch (error) {
    errorText.value = errorMessage(error, '加载标签失败')
  }
}

async function loadQuestions() {
  loading.value = true
  errorText.value = ''
  try {
    const data = await listQuestionsApi({
      ...query,
      keyword: query.keyword || undefined,
      questionType: query.questionType || undefined,
      difficulty: query.difficulty || undefined,
      tagId: query.tagId || undefined
    })
    questions.value = data.records
    total.value = data.total
  } catch (error) {
    questions.value = []
    total.value = 0
    errorText.value = errorMessage(error, '加载题库失败')
  } finally {
    loading.value = false
  }
}

function search() {
  query.page = 1
  loadQuestions()
}

function reset() {
  query.page = 1
  query.size = 10
  query.keyword = ''
  query.questionType = ''
  query.difficulty = ''
  query.tagId = ''
  loadQuestions()
}

function goDetail(id: number) {
  router.push(`/questions/${id}`)
}

function goPractice(id: number) {
  router.push(`/practice/${id}`)
}
</script>

<style scoped>
.question-list-panel {
  min-height: 320px;
  border: 1px solid #e5edf5;
  border-radius: 16px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(248, 250, 252, 0.96)),
    #ffffff;
  padding: 18px;
  box-shadow: 0 14px 34px rgba(15, 23, 42, 0.06);
}

.list-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  border-bottom: 1px solid #e5edf5;
  margin-bottom: 14px;
  padding-bottom: 14px;
}

.question-skeleton-list {
  display: grid;
  gap: 14px;
}

.question-skeleton-card {
  border: 1px solid #e5edf5;
  border-radius: 14px;
  background: #ffffff;
  padding: 18px;
}

.skeleton-meta {
  display: flex;
  gap: 10px;
  margin: 14px 0;
}

.list-toolbar strong,
.list-toolbar span {
  display: block;
}

.list-toolbar strong {
  color: #102a43;
  font-size: 16px;
}

.list-toolbar span {
  margin-top: 4px;
  color: #62748e;
  font-size: 13px;
}

.pagination-row {
  display: flex;
  justify-content: flex-end;
  border-top: 1px solid #e5edf5;
  margin-top: 18px;
  padding-top: 16px;
  overflow-x: auto;
}

.pagination-row :deep(.el-pagination) {
  border-radius: 999px;
  background: #f8fafc;
  padding: 8px 12px;
}

.el-alert {
  margin-bottom: 16px;
}

@media (max-width: 760px) {
  .question-list-panel {
    padding: 12px;
  }

  .list-toolbar,
  .pagination-row {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>
