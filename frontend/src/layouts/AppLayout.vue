<template>
  <div class="app-shell">
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-mark">AI</div>
        <span>面试刷题平台</span>
      </div>

      <el-menu class="sidebar-menu" :default-active="activePath" router>
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/questions">
          <el-icon><Reading /></el-icon>
          <span>题库</span>
        </el-menu-item>
        <el-menu-item index="/favorites">
          <el-icon><StarFilled /></el-icon>
          <span>我的收藏</span>
        </el-menu-item>
        <el-menu-item index="/wrong-questions">
          <el-icon><WarningFilled /></el-icon>
          <span>错题本</span>
        </el-menu-item>
        <el-menu-item index="/answer-history">
          <el-icon><Clock /></el-icon>
          <span>答题历史</span>
        </el-menu-item>
        <el-menu-item index="/ai-interview">
          <el-icon><ChatDotRound /></el-icon>
          <span>AI 面试助手</span>
        </el-menu-item>
        <el-menu-item index="/profile">
          <el-icon><User /></el-icon>
          <span>个人中心（开发中）</span>
        </el-menu-item>
        <el-sub-menu v-if="userStore.isAdmin" index="/admin">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>管理后台</span>
          </template>
          <el-menu-item index="/admin">后台首页</el-menu-item>
          <el-menu-item index="/admin/users">用户管理</el-menu-item>
          <el-menu-item index="/admin/questions">题库管理</el-menu-item>
          <el-menu-item index="/admin/favorites">收藏记录</el-menu-item>
          <el-menu-item index="/admin/wrong-questions">错题记录</el-menu-item>
          <el-menu-item index="/admin/answers">答题记录</el-menu-item>
          <el-menu-item index="/admin/ai-interviews">AI 面试记录</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </aside>

    <section class="content-area">
      <header class="topbar">
        <div class="topbar-title">{{ title }}</div>
        <div class="user-box">
          <el-avatar :size="34">{{ avatarText }}</el-avatar>
          <span>{{ userStore.nickname }}</span>
          <el-button :icon="SwitchButton" @click="handleLogout">退出</el-button>
        </div>
      </header>
      <main class="page-main">
        <RouterView />
      </main>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ChatDotRound,
  Clock,
  HomeFilled,
  Reading,
  Setting,
  StarFilled,
  SwitchButton,
  User,
  WarningFilled
} from '@element-plus/icons-vue'

import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activePath = computed(() => {
  if (route.path.startsWith('/questions') || route.path.startsWith('/practice')) {
    return '/questions'
  }
  if (route.path.startsWith('/answer-history')) {
    return '/answer-history'
  }
  if (route.path.startsWith('/ai-interview')) {
    return '/ai-interview'
  }
  if (route.path.startsWith('/admin/questions/')) {
    return '/admin/questions'
  }
  return route.path
})
const title = computed(() => (route.meta.title as string) || '首页')
const avatarText = computed(() => userStore.nickname.slice(0, 1).toUpperCase())

function handleLogout() {
  userStore.logout()
  router.replace('/login')
}
</script>

<style scoped>
.sidebar-menu {
  border-right: 0;
}

.el-menu-item {
  border-radius: 8px;
  margin-bottom: 4px;
}
</style>
