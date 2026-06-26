<template>
  <div class="auth-shell">
    <section class="auth-panel">
      <h1 class="auth-title">欢迎回来</h1>
      <p class="auth-subtitle">登录后继续查看学习进度和练习概览。</p>

      <el-alert v-if="errorText" :title="errorText" type="error" show-icon :closable="false" />

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="submit">
        <el-form-item label="用户名" prop="username">
          <el-input v-model.trim="form.username" placeholder="请输入用户名" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            autocomplete="current-password"
            show-password
          />
        </el-form-item>
        <div class="auth-actions">
          <el-link type="primary" @click="$router.push('/register')">注册新账号</el-link>
          <el-button type="primary" :loading="loading" @click="submit">登录</el-button>
        </div>
      </el-form>
    </section>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'

import { useUserStore } from '@/stores/user'
import { errorMessage } from '@/utils/error'

interface LoginForm {
  username: string
  password: string
}

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const errorText = ref('')

const form = reactive<LoginForm>({
  username: '',
  password: ''
})

const rules: FormRules<LoginForm> = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ]
}

async function submit() {
  errorText.value = ''
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }

  loading.value = true
  try {
    await userStore.login(form)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/dashboard'
    router.replace(redirect)
  } catch (error) {
    errorText.value = errorMessage(error, '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.el-alert {
  margin-bottom: 18px;
}
</style>
