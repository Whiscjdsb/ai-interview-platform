<template>
  <div>
    <div class="page-header">
      <div>
        <h1>我的收藏</h1>
        <p>把常看的题目收在这里，练习时可以直接进入。</p>
      </div>
      <el-button type="primary" @click="router.push('/questions')">去题库看看</el-button>
    </div>

    <el-alert v-if="errorText" :title="errorText" type="error" show-icon :closable="false" />

    <section v-loading="loading" class="list-panel">
      <template v-if="favorites.length">
        <div v-for="item in favorites" :key="item.favoriteId" class="record-card">
          <div class="record-card__main">
            <h3>{{ item.title }}</h3>
            <div class="record-card__meta">
              <el-tag size="small">{{ questionTypeText(item.questionType) }}</el-tag>
              <el-tag size="small" :type="difficultyType(item.difficulty)">
                {{ difficultyLabel(item.difficulty) }}
              </el-tag>
              <span>收藏于 {{ formatDateTime(item.favoriteTime) }}</span>
            </div>
            <div class="record-card__tags">
              <el-tag v-for="tag in item.tags" :key="tag.id" size="small" effect="plain">
                {{ tag.tagName }}
              </el-tag>
            </div>
          </div>
          <div class="record-card__actions">
            <el-button @click="router.push(`/questions/${item.questionId}`)">查看详情</el-button>
            <el-button type="primary" @click="router.push(`/practice/${item.questionId}`)">开始练习</el-button>
            <el-button type="danger" plain @click="confirmRemove(item)">取消收藏</el-button>
          </div>
        </div>

        <div class="pagination-row">
          <el-pagination
            v-model:current-page="query.page"
            v-model:page-size="query.size"
            :total="total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadFavorites"
            @current-change="loadFavorites"
          />
        </div>
      </template>

      <el-empty v-else-if="!loading" description="还没有收藏题目">
        <el-button type="primary" @click="router.push('/questions')">去题库看看</el-button>
      </el-empty>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

import { listFavoritesApi, removeFavoriteApi } from '@/api/favorite'
import type { FavoriteItem, FavoriteQuery } from '@/types/favorite'
import { errorMessage } from '@/utils/error'
import { difficultyLabel, difficultyType, formatDateTime, questionTypeText } from '@/utils/question'

const router = useRouter()
const loading = ref(false)
const errorText = ref('')
const favorites = ref<FavoriteItem[]>([])
const total = ref(0)
const query = reactive<FavoriteQuery>({
  page: 1,
  size: 10
})

onMounted(loadFavorites)

async function loadFavorites() {
  loading.value = true
  errorText.value = ''
  try {
    const data = await listFavoritesApi(query)
    favorites.value = data.records
    total.value = data.total
  } catch (error) {
    favorites.value = []
    total.value = 0
    errorText.value = errorMessage(error, '加载收藏夹失败')
  } finally {
    loading.value = false
  }
}

async function confirmRemove(item: FavoriteItem) {
  try {
    await ElMessageBox.confirm(`确认取消收藏「${item.title}」吗？`, '取消收藏', {
      confirmButtonText: '取消收藏',
      cancelButtonText: '再想想',
      type: 'warning'
    })
  } catch {
    return
  }

  try {
    await removeFavoriteApi(item.questionId)
    ElMessage.success('已取消收藏')
    if (favorites.value.length === 1 && query.page > 1) {
      query.page -= 1
    }
    await loadFavorites()
  } catch (error) {
    ElMessage.error(errorMessage(error, '取消收藏失败'))
  }
}
</script>

<style scoped>
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
  min-width: 260px;
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

@media (max-width: 820px) {
  .record-card {
    flex-direction: column;
  }

  .record-card__actions {
    justify-content: flex-start;
    min-width: 0;
  }
}
</style>
