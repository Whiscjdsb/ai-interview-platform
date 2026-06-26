<template>
  <div>
    <div class="page-header">
      <div>
        <h1>AI 面试历史</h1>
        <p>查看后端保存的模拟面试生成记录。</p>
      </div>
      <el-button type="primary" @click="router.push('/ai-interview')">创建面试</el-button>
    </div>

    <div class="filter-panel">
      <el-form :model="query" label-position="top" class="filter-grid">
        <el-form-item label="搜索">
          <el-input v-model.trim="query.keyword" clearable placeholder="搜索题目标题或模型名" @keyup.enter="search" />
        </el-form-item>
        <el-form-item label="创建时间">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DDTHH:mm:ss"
            clearable
          />
        </el-form-item>
        <div class="filter-actions">
          <el-button type="primary" @click="search">查询</el-button>
          <el-button @click="reset">重置</el-button>
        </div>
      </el-form>
    </div>

    <el-alert v-if="errorText" :title="errorText" type="error" show-icon :closable="false" />

    <section v-loading="loading" class="history-panel">
      <template v-if="displayRecords.length">
        <el-table :data="displayRecords" class="history-table">
          <el-table-column label="记录" min-width="220">
            <template #default="{ row }">
              {{ row.questionTitle || '模拟面试题生成' }}
            </template>
          </el-table-column>
          <el-table-column prop="modelName" label="模型" width="120" />
          <el-table-column label="得分" width="100">
            <template #default="{ row }">{{ row.score ?? '未评分' }}</template>
          </el-table-column>
          <el-table-column label="创建时间" width="180">
            <template #default="{ row }">{{ formatInterviewTime(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link @click="router.push(`/ai-interview/history/${row.id}`)">
                查看详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-row">
          <el-pagination
            v-model:current-page="query.page"
            v-model:page-size="query.size"
            :total="total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadHistory"
            @current-change="loadHistory"
          />
        </div>
      </template>

      <el-empty v-else-if="!loading" description="暂无 AI 面试历史">
        <el-button type="primary" @click="router.push('/ai-interview')">创建面试</el-button>
      </el-empty>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

import { getInterviewHistory } from '@/api/ai-interview'
import type { InterviewHistory, InterviewHistoryQuery } from '@/types/ai-interview'
import { formatInterviewTime } from '@/utils/ai-interview'
import { errorMessage } from '@/utils/error'

const router = useRouter()
const loading = ref(false)
const errorText = ref('')
const records = ref<InterviewHistory[]>([])
const total = ref(0)
const dateRange = ref<[string, string] | [] | null>([])
const query = reactive<InterviewHistoryQuery>({
  page: 1,
  size: 10,
  keyword: ''
})

const displayRecords = computed(() => {
  const keyword = query.keyword?.trim().toLowerCase()
  const [startTime, endTime] = dateRange.value || []
  return records.value.filter((item) => {
    const matchesKeyword = !keyword
      || (item.questionTitle || '').toLowerCase().includes(keyword)
      || item.modelName.toLowerCase().includes(keyword)
    const matchesStart = !startTime || item.createTime >= startTime
    const matchesEnd = !endTime || item.createTime <= endTime
    return matchesKeyword && matchesStart && matchesEnd
  })
})

onMounted(loadHistory)

async function loadHistory() {
  loading.value = true
  errorText.value = ''
  try {
    const data = await getInterviewHistory(query)
    records.value = data.records
    total.value = data.total
  } catch (error) {
    records.value = []
    total.value = 0
    errorText.value = errorMessage(error, '加载 AI 面试历史失败')
  } finally {
    loading.value = false
  }
}

function search() {
  query.page = 1
  loadHistory()
}

function reset() {
  query.page = 1
  query.size = 10
  query.keyword = ''
  dateRange.value = []
  loadHistory()
}
</script>

<style scoped>
.filter-panel {
  border: 1px solid #d9e2ec;
  border-radius: 8px;
  background: #ffffff;
  padding: 16px 16px 2px;
  margin-bottom: 16px;
}

.filter-grid {
  display: grid;
  grid-template-columns: minmax(220px, 1fr) minmax(320px, 1.2fr) auto;
  gap: 12px;
  align-items: end;
}

.filter-actions {
  display: flex;
  gap: 8px;
  padding-bottom: 18px;
}

.history-panel {
  min-height: 320px;
}

.history-table {
  width: 100%;
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
  .filter-grid {
    grid-template-columns: 1fr;
  }
}
</style>
