<template>
  <div>
    <div class="page-header">
      <div>
        <h1>用户管理</h1>
        <p>查看用户基础信息、角色和注册时间。</p>
      </div>
    </div>

    <div class="filter-panel">
      <el-input v-model.trim="query.keyword" clearable placeholder="搜索用户名、昵称或邮箱" @keyup.enter="search" />
      <el-button type="primary" @click="search">查询</el-button>
      <el-button @click="reset">重置</el-button>
    </div>

    <el-alert v-if="errorText" :title="errorText" type="error" show-icon :closable="false" />

    <section v-loading="loading">
      <el-table v-if="users.length" :data="users" class="admin-table">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" min-width="130" />
        <el-table-column prop="nickname" label="昵称" min-width="130" />
        <el-table-column prop="email" label="邮箱" min-width="180">
          <template #default="{ row }">{{ nullableText(row.email) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="userStatusType(row.status)">{{ userStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="角色" min-width="140">
          <template #default="{ row }">
            <el-tag v-for="role in row.roles" :key="role" size="small" effect="plain">{{ role }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
      </el-table>
      <el-empty v-else-if="!loading" description="暂无用户" />
      <div class="pagination-row">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadUsers"
          @current-change="loadUsers"
        />
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'

import { listAdminUsersApi } from '@/api/admin'
import type { AdminUser, AdminUserQuery } from '@/types/admin'
import { nullableText, userStatusText, userStatusType } from '@/utils/admin'
import { errorMessage } from '@/utils/error'
import { formatDateTime } from '@/utils/question'

const loading = ref(false)
const errorText = ref('')
const users = ref<AdminUser[]>([])
const total = ref(0)
const query = reactive<AdminUserQuery>({ page: 1, size: 10, keyword: '' })

onMounted(loadUsers)

async function loadUsers() {
  loading.value = true
  errorText.value = ''
  try {
    const data = await listAdminUsersApi({ ...query, keyword: query.keyword || undefined })
    users.value = data.records
    total.value = data.total
  } catch (error) {
    users.value = []
    total.value = 0
    errorText.value = errorMessage(error, '加载用户列表失败')
  } finally {
    loading.value = false
  }
}

function search() {
  query.page = 1
  loadUsers()
}

function reset() {
  query.page = 1
  query.size = 10
  query.keyword = ''
  loadUsers()
}
</script>

<style scoped>
.filter-panel {
  display: grid;
  grid-template-columns: minmax(220px, 1fr) auto auto;
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

@media (max-width: 640px) {
  .filter-panel {
    grid-template-columns: 1fr;
  }
}
</style>
