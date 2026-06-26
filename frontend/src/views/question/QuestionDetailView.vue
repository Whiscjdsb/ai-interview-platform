<template>
  <div>
    <el-breadcrumb separator="/" class="question-breadcrumb">
      <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item :to="{ path: '/questions' }">题库</el-breadcrumb-item>
      <el-breadcrumb-item>题目详情</el-breadcrumb-item>
    </el-breadcrumb>

    <section v-loading="loading" class="question-detail panel">
      <template v-if="question">
        <div class="question-detail__header">
          <div>
            <h1>{{ question.title }}</h1>
            <div class="question-detail__meta">
              <el-tag>{{ questionTypeText(question.questionType) }}</el-tag>
              <el-tag :type="difficultyType(question.difficulty)">
                {{ difficultyLabel(question.difficulty) }}
              </el-tag>
              <el-tag v-for="tag in question.tags" :key="tag.id" effect="plain">{{ tag.tagName }}</el-tag>
            </div>
          </div>
          <div class="question-detail__actions">
            <el-button @click="$router.push('/questions')">返回题库</el-button>
            <el-button :loading="favoriteLoading" :type="favorited ? 'warning' : 'default'" @click="toggleFavorite">
              {{ favorited ? '已收藏' : '收藏题目' }}
            </el-button>
            <el-button type="primary" @click="$router.push(`/practice/${question.id}`)">开始答题</el-button>
          </div>
        </div>

        <el-divider />

        <article class="question-content">
          <pre>{{ question.content }}</pre>
        </article>
      </template>

      <el-empty v-else-if="!loading" description="题目不存在或已下架">
        <el-button type="primary" @click="$router.push('/questions')">返回题库</el-button>
      </el-empty>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'

import { addFavoriteApi, listFavoritesApi, removeFavoriteApi } from '@/api/favorite'
import { getQuestionApi } from '@/api/question'
import type { Question } from '@/types/question'
import { errorMessage } from '@/utils/error'
import { difficultyLabel, difficultyType, questionTypeText } from '@/utils/question'

const route = useRoute()
const loading = ref(false)
const favoriteLoading = ref(false)
const favorited = ref(false)
const question = ref<Question | null>(null)

onMounted(loadQuestion)

async function loadQuestion() {
  loading.value = true
  try {
    question.value = await getQuestionApi(route.params.id as string)
    await loadFavoriteState(question.value.id)
  } catch {
    question.value = null
  } finally {
    loading.value = false
  }
}

async function loadFavoriteState(questionId: number) {
  favorited.value = false
  try {
    const data = await listFavoritesApi({ page: 1, size: 100 })
    favorited.value = data.records.some((item) => item.questionId === questionId)
  } catch {
    favorited.value = false
  }
}

async function toggleFavorite() {
  if (!question.value || favoriteLoading.value) {
    return
  }

  favoriteLoading.value = true
  try {
    if (favorited.value) {
      await removeFavoriteApi(question.value.id)
      favorited.value = false
      ElMessage.success('已取消收藏')
      return
    }
    await addFavoriteApi(question.value.id)
    favorited.value = true
    ElMessage.success('收藏成功')
  } catch (error) {
    const message = errorMessage(error, favorited.value ? '取消收藏失败' : '收藏失败')
    if (message.toLowerCase().includes('already')) {
      favorited.value = true
      ElMessage.info('这道题已经在收藏夹里了')
      return
    }
    if (message.toLowerCase().includes('does not exist')) {
      favorited.value = false
    }
    ElMessage.error(message)
  } finally {
    favoriteLoading.value = false
  }
}
</script>

<style scoped>
.question-breadcrumb {
  margin-bottom: 16px;
}

.question-detail {
  padding: 22px;
  min-height: 320px;
}

.question-detail__header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

.question-detail h1 {
  margin: 0 0 12px;
  font-size: 24px;
}

.question-detail__meta,
.question-detail__actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.question-detail__actions {
  justify-content: flex-end;
  align-items: flex-start;
}

.question-content pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.8;
  font-family: inherit;
  color: #1f2933;
}

@media (max-width: 760px) {
  .question-detail__header {
    flex-direction: column;
  }

  .question-detail__actions {
    justify-content: flex-start;
  }
}
</style>
