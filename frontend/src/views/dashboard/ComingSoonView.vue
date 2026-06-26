<template>
  <div class="coming-soon">
    <el-empty :description="`${title}开发中`">
      <el-button type="primary" @click="$router.push('/dashboard')">返回首页</el-button>
    </el-empty>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const title = computed(() => (route.meta.title as string) || '当前功能')
</script>
