<template>
  <div class="auth-shell">
    <section class="auth-panel">
      <h1 class="auth-title">创建账号</h1>
      <p class="auth-subtitle">用一个简单账号开始记录你的面试练习。</p>

      <el-alert v-if="errorText" :title="errorText" type="error" show-icon :closable="false" />

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="submit">
        <el-form-item label="用户名" prop="username">
          <el-input v-model.trim="form.username" placeholder="3-32 位用户名" autocomplete="username" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model.trim="form.nickname" placeholder="可选" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="至少 6 位" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="再次输入密码" show-password />
        </el-form-item>
        <div class="auth-actions">
          <el-link type="primary" @click="$router.push('/login')">已有账号，去登录</el-link>
          <el-button type="primary" :loading="loading" @click="submit">注册</el-button>
        </div>
      </el-form>
    </section>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'

import { registerApi } from '@/api/auth'
import { errorMessage } from '@/utils/error'

interface RegisterForm {
  username: string
  nickname: string
  password: string
  confirmPassword: string
}

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const errorText = ref('')

const form = reactive<RegisterForm>({
  username: '',
  nickname: '',
  password: '',
  confirmPassword: ''
})

const rules: FormRules<RegisterForm> = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 32, message: '用户名长度为 3 到 32 位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (_rule: unknown, value: string, callback: (error?: Error) => void) => {
        if (value !== form.password) {
          callback(new Error('两次输入的密码不一致'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
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
    await registerApi({
      username: form.username,
      password: form.password,
      confirmPassword: form.confirmPassword,
      nickname: form.nickname || undefined
    })
    ElMessage.success('注册成功，请登录')
    router.replace('/login')
  } catch (error) {
    errorText.value = errorMessage(error, '注册失败')
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
