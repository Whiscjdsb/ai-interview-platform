<template>
  <div>
    <el-breadcrumb separator="/" class="ai-breadcrumb">
      <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item :to="{ path: '/ai-interview' }">AI 面试助手</el-breadcrumb-item>
      <el-breadcrumb-item>面试进行中</el-breadcrumb-item>
    </el-breadcrumb>

    <section v-loading="loading" class="session-panel panel">
      <template v-if="session && currentQuestion">
        <div class="session-header">
          <div>
            <h1>{{ interviewTitle }}</h1>
            <p>{{ interviewMeta }}</p>
          </div>
          <el-progress
            type="dashboard"
            :percentage="progress"
            :width="92"
            :format="() => `${currentIndex + 1}/${questionCount}`"
          />
        </div>

        <el-divider />

        <div class="question-box">
          <div class="question-meta">
            <el-tag>第 {{ currentQuestion.questionNo }} 题 / 共 {{ questionCount }} 题</el-tag>
            <el-tag type="info">{{ currentQuestion.category }}</el-tag>
            <el-tag :type="difficultyTagType">{{ difficultyText(currentQuestion.difficulty) }}</el-tag>
          </div>
          <h2>{{ currentQuestion.question }}</h2>
          <div class="reference-points">
            <span v-for="point in currentQuestion.referencePoints" :key="point">{{ point }}</span>
          </div>
        </div>

        <el-input
          v-model="currentAnswer"
          type="textarea"
          :rows="10"
          maxlength="5000"
          show-word-limit
          placeholder="请输入你的回答。建议按“结论、原因、案例、总结”的结构组织。"
        />

        <div class="session-actions">
          <el-button :disabled="currentIndex === 0" @click="goPrevious">上一题</el-button>
          <el-button v-if="!isLastQuestion" type="primary" @click="goNext">下一题</el-button>
          <el-button v-else type="success" @click="submitAll">提交整场面试</el-button>
        </div>
      </template>

      <el-empty v-else-if="!loading" description="面试会话不存在或已过期">
        <el-button type="primary" @click="router.push('/ai-interview')">重新创建</el-button>
      </el-empty>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'

import { getInterview, submitInterview } from '@/api/ai-interview'
import type { StoredInterviewSession } from '@/types/ai-interview'
import {
  answersToPayload,
  difficultyText,
  getInterviewSession,
  saveInterviewResult,
  saveInterviewSession
} from '@/utils/ai-interview'
import { difficultyType } from '@/utils/question'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const session = ref<StoredInterviewSession | null>(null)
const currentIndex = ref(0)
const currentAnswer = ref('')

const currentQuestion = computed(() => session.value?.interview.questions[currentIndex.value])
const questionCount = computed(() => session.value?.interview.questions.length || 0)
const interviewTitle = computed(() => session.value?.interview.position || '模拟面试')
const interviewMeta = computed(() => {
  if (!session.value) {
    return ''
  }
  return `${difficultyText(session.value.interview.difficulty)} · ${session.value.interview.modelName}`
})
const isLastQuestion = computed(() => {
  if (!session.value) {
    return false
  }
  return currentIndex.value === session.value.interview.questions.length - 1
})
const progress = computed(() => {
  if (!session.value) {
    return 0
  }
  return Math.round(((currentIndex.value + 1) / session.value.interview.questions.length) * 100)
})
const difficultyTagType = computed(() => difficultyType(currentQuestion.value?.difficulty || 'MEDIUM'))

watch(currentIndex, syncAnswerFromSession)
watch(currentAnswer, (value) => {
  if (!session.value || !currentQuestion.value) {
    return
  }
  session.value.answers[currentQuestion.value.questionNo] = value
  saveInterviewSession(session.value)
})

onMounted(loadSession)

async function loadSession() {
  loading.value = true
  try {
    const localSession = getInterviewSession(route.params.id as string)
    if (localSession) {
      session.value = localSession
    } else {
      const interview = await getInterview(route.params.id as string)
      session.value = {
        interview,
        answers: {}
      }
      saveInterviewSession(session.value)
    }
    syncAnswerFromSession()
  } catch {
    session.value = null
  } finally {
    loading.value = false
  }
}

function syncAnswerFromSession() {
  if (!session.value || !currentQuestion.value) {
    currentAnswer.value = ''
    return
  }
  currentAnswer.value = session.value.answers[currentQuestion.value.questionNo] || ''
}

function goPrevious() {
  if (currentIndex.value > 0) {
    currentIndex.value -= 1
  }
}

function goNext() {
  if (!session.value || currentIndex.value >= session.value.interview.questions.length - 1) {
    return
  }
  currentIndex.value += 1
}

async function submitAll() {
  if (!session.value) {
    return
  }
  try {
    await ElMessageBox.confirm('提交后将生成本次模拟面试结果，确认提交吗？', '提交面试', {
      confirmButtonText: '提交',
      cancelButtonText: '继续作答',
      type: 'warning'
    })
  } catch {
    return
  }

  const result = await submitInterview(session.value.interview.id, {
    answers: answersToPayload(session.value.interview.questions, session.value.answers)
  })
  session.value.interview.status = 'SUBMITTED'
  session.value.result = result
  saveInterviewSession(session.value)
  saveInterviewResult(result)
  await router.push(`/ai-interview/result/${session.value.interview.id}`)
}
</script>

<style scoped>
.ai-breadcrumb {
  margin-bottom: 16px;
}

.session-panel {
  padding: 22px;
  min-height: 420px;
}

.session-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
}

.session-header h1 {
  margin: 0 0 8px;
  font-size: 24px;
}

.session-header p {
  margin: 0;
  color: #62748e;
}

.question-box {
  margin-bottom: 18px;
}

.question-box h2 {
  margin: 14px 0;
  font-size: 20px;
  line-height: 1.5;
}

.question-meta,
.reference-points,
.session-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.reference-points span {
  border-radius: 999px;
  background: #edf2f7;
  padding: 5px 10px;
  color: #486581;
  font-size: 13px;
}

.session-actions {
  justify-content: flex-end;
  margin-top: 18px;
}

@media (max-width: 760px) {
  .session-header,
  .session-actions {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
