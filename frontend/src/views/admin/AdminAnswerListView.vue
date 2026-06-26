<template>
  <div>
    <div class="page-header"><div><h1>答题记录</h1><p>查看用户答题提交和评分结果。</p></div></div>
    <div class="filter-panel">
      <el-input v-model.trim="query.userKeyword" clearable placeholder="搜索用户" @keyup.enter="search" />
      <el-select v-model="query.questionType" clearable placeholder="全部题型"><el-option v-for="option in QUESTION_TYPE_OPTIONS" :key="option.value" :label="option.label" :value="option.value" /></el-select>
      <el-select v-model="query.isCorrect" clearable placeholder="全部结果"><el-option label="正确" :value="true" /><el-option label="错误" :value="false" /></el-select>
      <el-button type="primary" @click="search">查询</el-button><el-button @click="reset">重置</el-button>
    </div>
    <el-alert v-if="errorText" :title="errorText" type="error" show-icon :closable="false" />
    <section v-loading="loading">
      <el-table v-if="records.length" :data="records" class="admin-table">
        <el-table-column label="用户" min-width="130"><template #default="{ row }">{{ adminRecordUser(row) }}</template></el-table-column>
        <el-table-column prop="questionTitle" label="题目" min-width="220" show-overflow-tooltip />
        <el-table-column label="题型" width="100"><template #default="{ row }">{{ row.questionType ? questionTypeText(row.questionType) : '-' }}</template></el-table-column>
        <el-table-column label="结果" width="140"><template #default="{ row }"><el-tag :type="answerResultType(row.isCorrect)">{{ answerResultText(row.isCorrect) }}</el-tag></template></el-table-column>
        <el-table-column label="得分" width="90"><template #default="{ row }">{{ formatScore(row.score) }}</template></el-table-column>
        <el-table-column label="答题时间" width="170"><template #default="{ row }">{{ formatDateTime(row.answerTime) }}</template></el-table-column>
        <el-table-column label="操作" width="120" fixed="right"><template #default="{ row }"><el-button link type="primary" @click="showDetail(row)">查看详情</el-button></template></el-table-column>
      </el-table>
      <el-empty v-else-if="!loading" description="暂无答题记录" />
      <PaginationBar v-model:page="query.page" v-model:size="query.size" :total="total" @change="loadRecords" />
    </section>
    <el-dialog v-model="detailVisible" title="答题记录详情" width="760px" destroy-on-close>
      <el-descriptions v-if="selectedRecord" :column="1" border>
        <el-descriptions-item label="用户">{{ adminRecordUser(selectedRecord) }}</el-descriptions-item>
        <el-descriptions-item label="题目">{{ selectedRecord.questionTitle || '-' }}</el-descriptions-item>
        <el-descriptions-item label="题型">{{ selectedRecord.questionType ? questionTypeText(selectedRecord.questionType) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="是否正确"><el-tag :type="answerResultType(selectedRecord.isCorrect)">{{ answerResultText(selectedRecord.isCorrect) }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="得分">{{ formatScore(selectedRecord.score) }}</el-descriptions-item>
        <el-descriptions-item label="答题耗时">{{ formatDuration(selectedRecord.answerDuration) }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ formatDateTime(selectedRecord.answerTime) }}</el-descriptions-item>
        <el-descriptions-item label="用户答案"><pre class="answer-text">{{ selectedRecord.userAnswer || '无答案内容' }}</pre></el-descriptions-item>
        <el-descriptions-item label="正确答案"><pre class="answer-text">{{ selectedRecord.correctAnswer || '-' }}</pre></el-descriptions-item>
        <el-descriptions-item label="解析"><pre class="answer-text">{{ selectedRecord.analysis || '-' }}</pre></el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { listAdminAnswersApi } from '@/api/admin'
import type { AdminAnswerRecord, AdminRecordQuery } from '@/types/admin'
import { adminRecordUser } from '@/utils/admin'
import { answerResultText, answerResultType, formatDuration, formatScore } from '@/utils/answer'
import { errorMessage } from '@/utils/error'
import { formatDateTime, QUESTION_TYPE_OPTIONS, questionTypeText } from '@/utils/question'
import PaginationBar from './PaginationBar.vue'

const loading = ref(false)
const errorText = ref('')
const records = ref<AdminAnswerRecord[]>([])
const total = ref(0)
const detailVisible = ref(false)
const selectedRecord = ref<AdminAnswerRecord | null>(null)
const query = reactive<AdminRecordQuery>({ page: 1, size: 10, userKeyword: '', questionType: '', isCorrect: '' })
onMounted(loadRecords)
async function loadRecords() {
  loading.value = true
  errorText.value = ''
  try {
    const data = await listAdminAnswersApi({ ...query, userKeyword: query.userKeyword || undefined, questionType: query.questionType || undefined, isCorrect: query.isCorrect === '' ? undefined : query.isCorrect })
    records.value = data.records
    total.value = data.total
  } catch (error) {
    records.value = []
    total.value = 0
    errorText.value = errorMessage(error, '加载答题记录失败')
  } finally {
    loading.value = false
  }
}
function search() { query.page = 1; loadRecords() }
function reset() { query.page = 1; query.size = 10; query.userKeyword = ''; query.questionType = ''; query.isCorrect = ''; loadRecords() }
function showDetail(record: AdminAnswerRecord) { selectedRecord.value = record; detailVisible.value = true }
</script>

<style scoped>
.filter-panel { display: grid; grid-template-columns: minmax(170px, 1fr) minmax(140px, .7fr) minmax(140px, .7fr) auto auto; gap: 10px; border: 1px solid #d9e2ec; border-radius: 8px; background: #fff; padding: 16px; margin-bottom: 16px; }
.admin-table { border: 1px solid #d9e2ec; border-radius: 8px; }
.answer-text { margin: 0; white-space: pre-wrap; word-break: break-word; font-family: inherit; }
.el-alert { margin-bottom: 16px; }
@media (max-width: 900px) { .filter-panel { grid-template-columns: 1fr; } }
</style>
