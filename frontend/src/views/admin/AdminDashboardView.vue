<template>
  <div>
    <div class="page-header">
      <div>
        <h1>管理后台</h1>
        <p>查看平台核心数据，并进入各类管理列表。</p>
      </div>
    </div>

    <el-alert v-if="errorText" :title="errorText" type="error" show-icon :closable="false" />

    <section v-loading="loading" class="admin-stats">
      <StatCard label="用户总数" :value="dashboard.totalUserCount" />
      <StatCard label="题目总数" :value="dashboard.totalQuestionCount" />
      <StatCard label="收藏总数" :value="dashboard.totalFavoriteCount" />
      <StatCard label="错题总数" :value="dashboard.totalWrongQuestionCount" />
      <StatCard label="答题记录" :value="dashboard.totalAnswerCount" />
      <StatCard label="AI 面试记录" :value="dashboard.totalAiInterviewCount" />
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'

import { getAdminDashboardApi } from '@/api/admin'
import StatCard from '@/components/StatCard.vue'
import type { AdminDashboard } from '@/types/admin'
import { errorMessage } from '@/utils/error'

const loading = ref(false)
const errorText = ref('')
const dashboard = reactive<AdminDashboard>({
  totalUserCount: 0,
  totalQuestionCount: 0,
  totalFavoriteCount: 0,
  totalWrongQuestionCount: 0,
  totalAnswerCount: 0,
  totalAiInterviewCount: 0
})

onMounted(loadDashboard)

async function loadDashboard() {
  loading.value = true
  errorText.value = ''
  try {
    Object.assign(dashboard, await getAdminDashboardApi())
  } catch (error) {
    errorText.value = errorMessage(error, '加载后台统计失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.admin-stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.el-alert {
  margin-bottom: 16px;
}

@media (max-width: 900px) {
  .admin-stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .admin-stats {
    grid-template-columns: 1fr;
  }
}
</style>
