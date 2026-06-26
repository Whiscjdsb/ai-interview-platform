<template>
  <div>
    <div class="page-header">
      <div>
        <h1>答题历史</h1>
        <p>查看自己的提交记录和评分状态。</p>
      </div>
    </div>

    <div class="filter-panel">
      <el-form :model="query" label-position="top" class="filter-grid">
        <el-form-item label="题型">
          <el-select v-model="query.questionType" clearable placeholder="全部题型">
            <el-option
              v-for="option in QUESTION_TYPE_OPTIONS"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="是否正确">
          <el-select v-model="query.isCorrect" clearable placeholder="全部结果">
            <el-option label="正确" :value="true" />
            <el-option label="错误" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item label="答题时间">
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
      <template v-if="records.length">
        <el-table :data="records" class="history-table">
          <el-table-column prop="title" label="题目" min-width="220" show-overflow-tooltip />
          <el-table-column label="题型" width="100">
            <template #default="{ row }">
              <el-tag size="small">{{ questionTypeText(row.questionType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="难度" width="90">
            <template #default="{ row }">
              <el-tag size="small" :type="difficultyType(row.difficulty)">
                {{ difficultyLabel(row.difficulty) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="userAnswer" label="用户答案" min-width="160" show-overflow-tooltip />
          <el-table-column label="结果" width="150">
            <template #default="{ row }">
              <el-tag size="small" :type="answerResultType(row.isCorrect)">
                {{ answerResultText(row.isCorrect) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="得分" width="90">
            <template #default="{ row }">{{ formatScore(row.score) }}</template>
          </el-table-column>
          <el-table-column label="耗时" width="100">
            <template #default="{ row }">{{ formatDuration(row.answerDuration) }}</template>
          </el-table-column>
          <el-table-column label="答题时间" width="170">
            <template #default="{ row }">{{ formatDateTime(row.answerTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="110" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link @click="router.push(`/answer-history/${row.id}`)">查看详情</el-button>
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
            @size-change="loadRecords"
            @current-change="loadRecords"
          />
        </div>
      </template>

      <el-empty v-else-if="!loading" description="暂无答题记录">
        <el-button type="primary" @click="router.push('/questions')">开始刷题</el-button>
      </el-empty>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

import { listAnswerHistoryApi } from '@/api/answer'
import type { AnswerHistoryItem, AnswerHistoryQuery } from '@/types/answer'
import { answerResultText, answerResultType, formatDuration, formatScore } from '@/utils/answer'
import { errorMessage } from '@/utils/error'
import {
  QUESTION_TYPE_OPTIONS,
  difficultyLabel,
  difficultyType,
  formatDateTime,
  questionTypeText
} from '@/utils/question'

const router = useRouter()
const loading = ref(false)
const errorText = ref('')
const records = ref<AnswerHistoryItem[]>([])
const total = ref(0)
const dateRange = ref<[string, string] | [] | null>([])
const query = reactive<AnswerHistoryQuery>({
  page: 1,
  size: 10,
  questionType: '',
  isCorrect: ''
})

onMounted(loadRecords)

async function loadRecords() {
  loading.value = true
  errorText.value = ''
  try {
    const [startTime, endTime] = dateRange.value || []
    const data = await listAnswerHistoryApi({
      ...query,
      questionType: query.questionType || undefined,
      isCorrect: query.isCorrect === '' ? undefined : query.isCorrect,
      startTime,
      endTime
    })
    records.value = data.records
    total.value = data.total
  } catch (error) {
    records.value = []
    total.value = 0
    errorText.value = errorMessage(error, '加载答题历史失败')
  } finally {
    loading.value = false
  }
}

function search() {
  query.page = 1
  loadRecords()
}

function reset() {
  query.page = 1
  query.size = 10
  query.questionType = ''
  query.isCorrect = ''
  dateRange.value = []
  loadRecords()
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
  grid-template-columns: minmax(150px, 0.8fr) minmax(150px, 0.8fr) minmax(300px, 1.5fr) auto;
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

@media (max-width: 1020px) {
  .filter-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .filter-grid {
    grid-template-columns: 1fr;
  }
}
</style>
