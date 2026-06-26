<template>
  <div>
    <div class="page-header"><div><h1>收藏记录</h1><p>查看用户收藏题目的记录。</p></div></div>
    <div class="filter-panel">
      <el-input v-model.trim="query.userKeyword" clearable placeholder="搜索用户" @keyup.enter="search" />
      <el-input v-model.trim="query.questionKeyword" clearable placeholder="搜索题目" @keyup.enter="search" />
      <el-button type="primary" @click="search">查询</el-button><el-button @click="reset">重置</el-button>
    </div>
    <el-alert v-if="errorText" :title="errorText" type="error" show-icon :closable="false" />
    <section v-loading="loading">
      <el-table v-if="records.length" :data="records" class="admin-table">
        <el-table-column label="用户" min-width="140"><template #default="{ row }">{{ adminRecordUser(row) }}</template></el-table-column>
        <el-table-column prop="questionTitle" label="题目" min-width="220" show-overflow-tooltip />
        <el-table-column label="题型" width="100"><template #default="{ row }">{{ row.questionType ? questionTypeText(row.questionType) : '-' }}</template></el-table-column>
        <el-table-column label="难度" width="90"><template #default="{ row }"><el-tag v-if="row.difficulty" :type="difficultyType(row.difficulty)">{{ difficultyLabel(row.difficulty) }}</el-tag></template></el-table-column>
        <el-table-column label="收藏时间" width="170"><template #default="{ row }">{{ formatDateTime(row.createTime) }}</template></el-table-column>
      </el-table>
      <el-empty v-else-if="!loading" description="暂无收藏记录" />
      <PaginationBar v-model:page="query.page" v-model:size="query.size" :total="total" @change="loadRecords" />
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { listAdminFavoritesApi } from '@/api/admin'
import type { AdminFavoriteRecord, AdminRecordQuery } from '@/types/admin'
import { adminRecordUser } from '@/utils/admin'
import { errorMessage } from '@/utils/error'
import { difficultyLabel, difficultyType, formatDateTime, questionTypeText } from '@/utils/question'
import PaginationBar from './PaginationBar.vue'

const loading = ref(false)
const errorText = ref('')
const records = ref<AdminFavoriteRecord[]>([])
const total = ref(0)
const query = reactive<AdminRecordQuery>({ page: 1, size: 10, userKeyword: '', questionKeyword: '' })
onMounted(loadRecords)
async function loadRecords() {
  loading.value = true
  errorText.value = ''
  try {
    const data = await listAdminFavoritesApi({ ...query, userKeyword: query.userKeyword || undefined, questionKeyword: query.questionKeyword || undefined })
    records.value = data.records
    total.value = data.total
  } catch (error) {
    records.value = []
    total.value = 0
    errorText.value = errorMessage(error, '加载收藏记录失败')
  } finally {
    loading.value = false
  }
}
function search() { query.page = 1; loadRecords() }
function reset() { query.page = 1; query.size = 10; query.userKeyword = ''; query.questionKeyword = ''; loadRecords() }
</script>

<style scoped>
.filter-panel { display: grid; grid-template-columns: minmax(180px, 1fr) minmax(180px, 1fr) auto auto; gap: 10px; border: 1px solid #d9e2ec; border-radius: 8px; background: #fff; padding: 16px; margin-bottom: 16px; }
.admin-table { border: 1px solid #d9e2ec; border-radius: 8px; }
.el-alert { margin-bottom: 16px; }
@media (max-width: 760px) { .filter-panel { grid-template-columns: 1fr; } }
</style>
