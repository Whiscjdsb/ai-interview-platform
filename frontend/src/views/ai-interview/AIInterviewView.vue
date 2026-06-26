<template>
  <div>
    <div class="page-header">
      <div>
        <h1>AI 面试助手</h1>
        <p>根据岗位、难度和知识方向生成模拟面试题，完成后获得结构化点评。</p>
      </div>
      <el-button @click="router.push('/ai-interview/history')">查看历史</el-button>
    </div>

    <section class="ai-home-grid">
      <div class="intro-panel panel">
        <h2>模拟真实后端面试节奏</h2>
        <p>
          当前前端接入后端 Mock AI 题目生成接口。整场面试提交评分接口后端尚未提供，
          因此前端会先使用本地规则生成结果，接口位置已预留。
        </p>
        <div class="intro-list">
          <div>
            <strong>结构化题目</strong>
            <span>按岗位和难度生成 1 到 10 道题</span>
          </div>
          <div>
            <strong>逐题作答</strong>
            <span>支持上一题、下一题和进度展示</span>
          </div>
          <div>
            <strong>结果复盘</strong>
            <span>输出总评分、逐题评分、优点和改进建议</span>
          </div>
        </div>
      </div>

      <div class="create-panel panel">
        <h2>创建新的模拟面试</h2>
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
          <el-form-item label="目标岗位" prop="position">
            <el-input v-model.trim="form.position" maxlength="128" placeholder="例如：Java 后端开发工程师" />
          </el-form-item>
          <el-form-item label="难度" prop="difficulty">
            <el-select v-model="form.difficulty" placeholder="选择难度">
              <el-option
                v-for="option in DIFFICULTY_OPTIONS"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="题目数量" prop="questionCount">
            <el-input-number v-model="form.questionCount" :min="1" :max="10" />
          </el-form-item>
          <el-form-item label="关注方向">
            <el-select
              v-model="form.focusTags"
              multiple
              filterable
              allow-create
              default-first-option
              placeholder="例如：Java 基础、Spring Boot、MySQL、Redis"
            >
              <el-option v-for="tag in defaultTags" :key="tag" :label="tag" :value="tag" />
            </el-select>
          </el-form-item>
          <el-button type="primary" :loading="loading" class="start-button" @click="startInterview">
            开始面试
          </el-button>
        </el-form>

        <el-alert v-if="errorText" :title="errorText" type="error" show-icon :closable="false" />
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'

import { createInterview } from '@/api/ai-interview'
import type { CreateInterviewPayload } from '@/types/ai-interview'
import { errorMessage } from '@/utils/error'
import { DIFFICULTY_OPTIONS } from '@/utils/question'
import { saveInterviewSession } from '@/utils/ai-interview'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const errorText = ref('')
const defaultTags = ['Java 基础', 'Spring Boot', 'MySQL', 'Redis', '并发编程', 'JVM']
const form = reactive<CreateInterviewPayload>({
  position: 'Java 后端开发工程师',
  difficulty: 'MEDIUM',
  focusTags: ['Java 基础', 'Spring Boot', 'MySQL'],
  questionCount: 5
})

const rules: FormRules<CreateInterviewPayload> = {
  position: [{ required: true, message: '请输入目标岗位', trigger: 'blur' }],
  difficulty: [{ required: true, message: '请选择难度', trigger: 'change' }],
  questionCount: [{ required: true, message: '请选择题目数量', trigger: 'change' }]
}

async function startInterview() {
  if (!formRef.value) {
    return
  }
  await formRef.value.validate()
  loading.value = true
  errorText.value = ''
  try {
    const interview = await createInterview(form)
    saveInterviewSession({
      interview: { ...interview, status: 'IN_PROGRESS' },
      answers: {}
    })
    await router.push(`/ai-interview/session/${interview.id}`)
  } catch (error) {
    errorText.value = errorMessage(error, '创建模拟面试失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.ai-home-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(360px, 0.75fr);
  gap: 18px;
}

.intro-panel,
.create-panel {
  padding: 22px;
}

.intro-panel h2,
.create-panel h2 {
  margin: 0 0 12px;
  font-size: 20px;
}

.intro-panel p {
  margin: 0;
  color: #62748e;
  line-height: 1.7;
}

.intro-list {
  display: grid;
  gap: 12px;
  margin-top: 24px;
}

.intro-list div {
  border: 1px solid #d9e2ec;
  border-radius: 8px;
  padding: 14px;
  background: #f8fafc;
}

.intro-list strong,
.intro-list span {
  display: block;
}

.intro-list span {
  margin-top: 6px;
  color: #62748e;
}

.start-button {
  width: 100%;
}

.el-alert {
  margin-top: 16px;
}

@media (max-width: 960px) {
  .ai-home-grid {
    grid-template-columns: 1fr;
  }
}
</style>
