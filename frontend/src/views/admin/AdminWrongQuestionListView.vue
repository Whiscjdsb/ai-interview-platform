<template>
  <div>
    <div class="page-header"><div><h1>错题记录</h1><p>查看全站用户错题分布与状态。</p></div></div>
    <div class="filter-panel">
      <el-input v-model.trim="query.userKeyword" clearable placeholder="搜索用户" @keyup.enter="search" />
      <el-select v-model="query.questionType" clearable placeholder="全部题型">
        <el-option v-for="option in QUESTION_TYPE_OPTIONS" :key="option.value" :label="option.label" :value="option.value" />
      </el-select>
      <el-select v-model="query.status" clearable placeholder="全部状态">
        <el-option v-for="option in WRONG_STATUS_OPTIONS" :key="option.value" :label="option.label" :value="option.value" />
      </el-select>
      <el-date-picker v-model="dateRange" type="datetimerange" start-placeholder="开始时间" end-placeholder="结束时间" value-format="YYYY-MM-DDTHH:mm:ss" clearable />
      <el-button type="primary" @click="search">查询</el-button><el-button @click="reset">重置</el-button>
    </div>
    <el-alert v-if="errorText" :title="errorText" type="error" show-icon :closable="false" />
    <section v-loading="loading">
      <el-table v-if="records.length" :data="records" class="admin-table">
        <el-table-column label="用户" min-width="130"><template #default="{ row }">{{ adminRecordUser(row) }}</template></el-table-column>
        <el-table-column prop="questionTitle" label="题目" min-width="220" show-overflow-tooltip />
        <el-table-column label="题型" width="100"><template #default="{ row }">{{ row.questionType ? questionTypeText(row.questionType) : '-' }}</template></el-table-column>
        <el-table-column label="错误次数" width="100"><template #default="{ row }">{{ row.wrongCount }}</template></el-table-column>
        <el-table-column label="状态" width="100"><template #default="{ row }"><el-tag :type="wrongStatusType(row.status)">{{ wrongStatusText(row.status) }}</el-tag></template></el-table-column>
        <el-table-column label="最后答错" width="170"><template #default="{ row }">{{ formatDateTime(row.lastWrongTime) }}</template></el-table-column>
      </el-table>
      <el-empty v-else-if="!loading" description="暂无错题记录" />
      <PaginationBar v-model:page="query.page" v-model:size="query.size" :total="total" @change="loadRecords" />
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { listAdminWrongQuestionsApi } from '@/api/admin'
import type { AdminRecordQuery, AdminWrongQuestionRecord } from '@/types/admin'
import { adminRecordUser } from '@/utils/admin'
import { errorMessage } from '@/utils/error'
import { formatDateTime, QUESTION_TYPE_OPTIONS, questionTypeText, WRONG_STATUS_OPTIONS, wrongStatusText, wrongStatusType } from '@/utils/question'
import PaginationBar from './PaginationBar.vue'

const loading = ref(false)
const errorText = ref('')
const records = ref<AdminWrongQuestionRecord[]>([])
const total = ref(0)
const dateRange = ref<[string, string] | [] | null>([])
const query = reactive<AdminRecordQuery>({ page: 1, size: 10, userKeyword: '', questionType: '', status: '' })
onMounted(loadRecords)
async function loadRecords() {
  loading.value = true
  errorText.value = ''
  try {
    const [startTime, endTime] = dateRange.value || []
    const data = await listAdminWrongQuestionsApi({ ...query, userKeyword: query.userKeyword || undefined, questionType: query.questionType || undefined, status: query.status || undefined, startTime, endTime })
    records.value = data.records
    total.value = data.total
  } catch (error) {
    records.value = []
    total.value = 0
    errorText.value = errorMessage(error, '加载错题记录失败')
  } finally {
    loading.value = false
  }
}
function search() { query.page = 1; loadRecords() }
function reset() { query.page = 1; query.size = 10; query.userKeyword = ''; query.questionType = ''; query.status = ''; dateRange.value = []; loadRecords() }
</script>

<style scoped>
.filter-panel { display: grid; grid-template-columns: minmax(150px, 1fr) minmax(130px, .6fr) minmax(130px, .6fr) minmax(280px, 1.4fr) auto auto; gap: 10px; border: 1px solid #d9e2ec; border-radius: 8px; background: #fff; padding: 16px; margin-bottom: 16px; }
.admin-table { border: 1px solid #d9e2ec; border-radius: 8px; }
.el-alert { margin-bottom: 16px; }
@media (max-width: 1080px) { .filter-panel { grid-template-columns: 1fr 1fr; } }
@media (max-width: 640px) { .filter-panel { grid-template-columns: 1fr; } }
</style>
