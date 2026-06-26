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
      <template v-if="questions.length">
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
}

.pagination-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 18px;
  overflow-x: auto;
}

.el-alert {
  margin-bottom: 16px;
}
</style>
