<template>
  <div>
    <div class="page-header">
      <div>
        <h1>题库管理</h1>
        <p>管理题目内容、答案、解析和标签。</p>
      </div>
      <el-button type="primary" @click="router.push('/admin/questions/create')">新增题目</el-button>
    </div>

    <div class="filter-panel">
      <el-input v-model.trim="query.keyword" clearable placeholder="搜索标题或内容" @keyup.enter="search" />
      <el-select v-model="query.questionType" clearable placeholder="全部题型">
        <el-option v-for="option in QUESTION_TYPE_OPTIONS" :key="option.value" :label="option.label" :value="option.value" />
      </el-select>
      <el-select v-model="query.difficulty" clearable placeholder="全部难度">
        <el-option v-for="option in DIFFICULTY_OPTIONS" :key="option.value" :label="option.label" :value="option.value" />
      </el-select>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button @click="reset">重置</el-button>
    </div>

    <el-alert v-if="errorText" :title="errorText" type="error" show-icon :closable="false" />

    <section v-loading="loading">
      <el-table v-if="questions.length" :data="questions" class="admin-table">
        <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
        <el-table-column label="题型" width="100">
          <template #default="{ row }">{{ questionTypeText(row.questionType) }}</template>
        </el-table-column>
        <el-table-column label="难度" width="90">
          <template #default="{ row }">
            <el-tag :type="difficultyType(row.difficulty)">{{ difficultyLabel(row.difficulty) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="标签" min-width="160">
          <template #default="{ row }">
            <el-tag v-for="tag in row.tags" :key="tag.id" size="small" effect="plain">{{ tag.tagName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="router.push(`/admin/questions/${row.id}/edit`)">编辑</el-button>
            <el-button type="danger" link @click="confirmDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else-if="!loading" description="暂无题目" />
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
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

import { deleteAdminQuestionApi, listAdminQuestionsApi } from '@/api/admin'
import type { AdminQuestion } from '@/types/admin'
import type { QuestionQuery } from '@/types/question'
import { errorMessage } from '@/utils/error'
import {
  DIFFICULTY_OPTIONS,
  QUESTION_TYPE_OPTIONS,
  difficultyLabel,
  difficultyType,
  questionTypeText
} from '@/utils/question'

const router = useRouter()
const loading = ref(false)
const errorText = ref('')
const questions = ref<AdminQuestion[]>([])
const total = ref(0)
const query = reactive<QuestionQuery>({
  page: 1,
  size: 10,
  keyword: '',
  questionType: '',
  difficulty: '',
  tagId: ''
})

onMounted(loadQuestions)

async function loadQuestions() {
  loading.value = true
  errorText.value = ''
  try {
    const data = await listAdminQuestionsApi({
      ...query,
      keyword: query.keyword || undefined,
      questionType: query.questionType || undefined,
      difficulty: query.difficulty || undefined,
      tagId: undefined
    })
    questions.value = data.records
    total.value = data.total
  } catch (error) {
    questions.value = []
    total.value = 0
    errorText.value = errorMessage(error, '加载题目列表失败')
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
  loadQuestions()
}

async function confirmDelete(question: AdminQuestion) {
  try {
    await ElMessageBox.confirm(`确认删除「${question.title}」吗？`, '删除题目', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await deleteAdminQuestionApi(question.id)
    ElMessage.success('删除成功')
    await loadQuestions()
  } catch (error) {
    ElMessage.error(errorMessage(error, '删除题目失败'))
  }
}
</script>

<style scoped>
.filter-panel {
  display: grid;
  grid-template-columns: minmax(220px, 1fr) minmax(150px, 0.5fr) minmax(150px, 0.5fr) auto auto;
  gap: 10px;
  border: 1px solid #d9e2ec;
  border-radius: 8px;
  background: #ffffff;
  padding: 16px;
  margin-bottom: 16px;
}

.admin-table {
  border: 1px solid #d9e2ec;
  border-radius: 8px;
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

@media (max-width: 900px) {
  .filter-panel {
    grid-template-columns: 1fr;
  }
}
</style>
