<template>
  <div>
    <div class="page-header">
      <div>
        <h1>错题本</h1>
        <p>按状态和题目类型复盘薄弱题目。</p>
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
        <el-form-item label="难度">
          <el-select v-model="query.difficulty" clearable placeholder="全部难度">
            <el-option
              v-for="option in DIFFICULTY_OPTIONS"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部状态">
            <el-option
              v-for="option in WRONG_STATUS_OPTIONS"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <div class="filter-actions">
          <el-button type="primary" @click="search">查询</el-button>
          <el-button @click="reset">重置</el-button>
        </div>
      </el-form>
    </div>

    <el-alert v-if="errorText" :title="errorText" type="error" show-icon :closable="false" />

    <section v-loading="loading" class="list-panel">
      <template v-if="records.length">
        <div v-for="item in records" :key="item.id" class="record-card">
          <div class="record-card__main">
            <h3>{{ item.title }}</h3>
            <div class="record-card__meta">
              <el-tag size="small">{{ questionTypeText(item.questionType) }}</el-tag>
              <el-tag size="small" :type="difficultyType(item.difficulty)">
                {{ difficultyLabel(item.difficulty) }}
              </el-tag>
              <el-tag size="small" :type="wrongStatusType(item.status)">
                {{ wrongStatusText(item.status) }}
              </el-tag>
              <span>错误 {{ item.wrongCount }} 次</span>
              <span>最后答错 {{ formatDateTime(item.lastWrongTime) }}</span>
            </div>
            <div class="record-card__tags">
              <el-tag v-for="tag in item.tags" :key="tag.id" size="small" effect="plain">
                {{ tag.tagName }}
              </el-tag>
            </div>
          </div>
          <div class="record-card__actions">
            <el-button @click="router.push(`/questions/${item.questionId}`)">查看题目</el-button>
            <el-button type="primary" @click="router.push(`/practice/${item.questionId}`)">再次练习</el-button>
            <el-button type="danger" plain @click="confirmRemove(item)">删除</el-button>
          </div>
        </div>

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

      <el-empty v-else-if="!loading" description="暂时没有错题，继续保持">
        <el-button type="primary" @click="router.push('/questions')">继续刷题</el-button>
      </el-empty>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

import { listWrongQuestionsApi, removeWrongQuestionApi } from '@/api/wrong-question'
import type { WrongQuestionItem, WrongQuestionQuery } from '@/types/wrong-question'
import { errorMessage } from '@/utils/error'
import {
  DIFFICULTY_OPTIONS,
  QUESTION_TYPE_OPTIONS,
  WRONG_STATUS_OPTIONS,
  difficultyLabel,
  difficultyType,
  formatDateTime,
  questionTypeText,
  wrongStatusText,
  wrongStatusType
} from '@/utils/question'

const router = useRouter()
const loading = ref(false)
const errorText = ref('')
const records = ref<WrongQuestionItem[]>([])
const total = ref(0)
const query = reactive<WrongQuestionQuery>({
  page: 1,
  size: 10,
  questionType: '',
  difficulty: '',
  status: ''
})

onMounted(loadRecords)

async function loadRecords() {
  loading.value = true
  errorText.value = ''
  try {
    const data = await listWrongQuestionsApi({
      ...query,
      questionType: query.questionType || undefined,
      difficulty: query.difficulty || undefined,
      status: query.status || undefined
    })
    records.value = data.records
    total.value = data.total
  } catch (error) {
    records.value = []
    total.value = 0
    errorText.value = errorMessage(error, '加载错题本失败')
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
  query.difficulty = ''
  query.status = ''
  loadRecords()
}

async function confirmRemove(item: WrongQuestionItem) {
  try {
    await ElMessageBox.confirm(`确认从错题本删除「${item.title}」吗？`, '删除错题记录', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }

  try {
    await removeWrongQuestionApi(item.questionId)
    ElMessage.success('已从错题本删除')
    if (records.value.length === 1 && query.page > 1) {
      query.page -= 1
    }
    await loadRecords()
  } catch (error) {
    ElMessage.error(errorMessage(error, '删除错题记录失败'))
  }
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
  grid-template-columns: repeat(3, minmax(160px, 1fr)) auto;
  gap: 12px;
  align-items: end;
}

.filter-actions {
  display: flex;
  gap: 8px;
  padding-bottom: 18px;
}

.list-panel {
  min-height: 320px;
}

.record-card {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  border: 1px solid #d9e2ec;
  border-radius: 8px;
  background: #ffffff;
  padding: 18px;
}

.record-card + .record-card {
  margin-top: 12px;
}

.record-card h3 {
  margin: 0 0 10px;
  font-size: 17px;
}

.record-card__main {
  min-width: 0;
}

.record-card__meta,
.record-card__tags,
.record-card__actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.record-card__meta {
  color: #62748e;
  font-size: 13px;
}

.record-card__tags {
  margin-top: 10px;
}

.record-card__actions {
  justify-content: flex-end;
  min-width: 250px;
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

@media (max-width: 920px) {
  .filter-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .record-card {
    flex-direction: column;
  }

  .record-card__actions {
    justify-content: flex-start;
    min-width: 0;
  }
}

@media (max-width: 640px) {
  .filter-grid {
    grid-template-columns: 1fr;
  }
}
</style>
