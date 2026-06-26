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
            :format="() => `${currentQuestionIndex + 1}/${questionCount}`"
          />
        </div>

        <el-divider />

        <div class="session-grid">
          <div>
            <div class="question-box">
              <div class="question-meta">
                <el-tag>第 {{ currentQuestion.questionNo }} 题 / 共 {{ questionCount }} 题</el-tag>
                <el-tag type="info">{{ currentQuestion.category }}</el-tag>
                <el-tag :type="difficultyTagType">{{ difficultyText(currentQuestion.difficulty) }}</el-tag>
              </div>
              <h2>{{ currentPrompt }}</h2>
              <p v-if="lastFollowUpReason" class="follow-up-reason">{{ lastFollowUpReason }}</p>
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
              placeholder="请输入你的回答。点击“生成追问”后，AI 会基于本轮回答继续深入提问。"
            />

            <div class="session-actions">
              <el-button :disabled="currentQuestionIndex === 0" @click="goPrevious">上一题</el-button>
              <el-button :loading="followUpLoading" type="warning" @click="generateFollowUp">生成追问</el-button>
              <el-button v-if="!isLastQuestion" type="primary" @click="goNext">下一题</el-button>
              <el-button v-else type="success" @click="submitAll">提交整场面试</el-button>
            </div>
          </div>

          <aside class="chat-panel">
            <div class="chat-panel__header">
              <strong>多轮追问</strong>
              <span>{{ conversationHistory.length }} 条对话</span>
            </div>
            <div v-if="conversationHistory.length" class="chat-list">
              <div
                v-for="(item, index) in conversationHistory"
                :key="`${item.role}-${index}`"
                class="chat-bubble"
                :class="item.role === 'ai' ? 'chat-bubble--ai' : 'chat-bubble--user'"
              >
                <span>{{ item.role === 'ai' ? 'AI面试官' : '我' }}</span>
                <p>{{ item.content }}</p>
              </div>
            </div>
            <el-empty v-else description="回答后点击生成追问，开始多轮面试" :image-size="82" />

            <div v-if="aiFollowUpList.length" class="follow-up-list">
              <h3>AI 追问记录</h3>
              <ol>
                <li v-for="item in aiFollowUpList" :key="item">{{ item }}</li>
              </ol>
            </div>
          </aside>
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
import { ElMessage, ElMessageBox } from 'element-plus'

import { getInterview, getNextInterviewQuestion, submitInterview } from '@/api/ai-interview'
import type { InterviewConversationMessage, StoredInterviewSession } from '@/types/ai-interview'
import {
  answersToPayload,
  difficultyText,
  getInterviewSession,
  saveInterviewResult,
  saveInterviewSession
} from '@/utils/ai-interview'
import { errorMessage } from '@/utils/error'
import { difficultyType } from '@/utils/question'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const followUpLoading = ref(false)
const session = ref<StoredInterviewSession | null>(null)
const currentQuestionIndex = ref(0)
const currentAnswer = ref('')
const lastFollowUpReason = ref('')

const currentQuestion = computed(() => session.value?.interview.questions[currentQuestionIndex.value])
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
  return currentQuestionIndex.value === session.value.interview.questions.length - 1
})
const progress = computed(() => {
  if (!session.value) {
    return 0
  }
  return Math.round(((currentQuestionIndex.value + 1) / session.value.interview.questions.length) * 100)
})
const difficultyTagType = computed(() => difficultyType(currentQuestion.value?.difficulty || 'MEDIUM'))
const conversationHistory = computed(() => getQuestionHistory())
const userAnswerList = computed(() =>
  conversationHistory.value.filter((item) => item.role === 'user').map((item) => item.content)
)
const aiFollowUpList = computed(() => {
  if (!session.value || !currentQuestion.value) {
    return []
  }
  return session.value.aiFollowUpList?.[currentQuestion.value.questionNo] || []
})
const currentPrompt = computed(() => {
  const latestFollowUp = aiFollowUpList.value[aiFollowUpList.value.length - 1]
  return latestFollowUp || currentQuestion.value?.question || ''
})

watch(currentQuestionIndex, syncAnswerFromSession)
watch(currentAnswer, (value) => {
  if (!session.value || !currentQuestion.value) {
    return
  }
  session.value.answers[currentQuestion.value.questionNo] = buildCombinedAnswer(value)
  saveInterviewSession(session.value)
})

onMounted(loadSession)

async function loadSession() {
  loading.value = true
  try {
    const localSession = getInterviewSession(route.params.id as string)
    if (localSession) {
      session.value = normalizeSession(localSession)
    } else {
      const interview = await getInterview(route.params.id as string)
      session.value = normalizeSession({
        interview,
        answers: {}
      })
      saveInterviewSession(session.value)
    }
    syncAnswerFromSession()
  } catch {
    session.value = null
  } finally {
    loading.value = false
  }
}

function normalizeSession(value: StoredInterviewSession) {
  value.conversationHistories = value.conversationHistories || {}
  value.aiFollowUpList = value.aiFollowUpList || {}
  return value
}

function syncAnswerFromSession() {
  lastFollowUpReason.value = ''
  if (!session.value || !currentQuestion.value) {
    currentAnswer.value = ''
    return
  }
  currentAnswer.value = getQuestionHistory().length ? '' : session.value.answers[currentQuestion.value.questionNo] || ''
}

function goPrevious() {
  if (currentQuestionIndex.value > 0) {
    currentQuestionIndex.value -= 1
  }
}

function goNext() {
  if (!session.value || currentQuestionIndex.value >= session.value.interview.questions.length - 1) {
    return
  }
  currentQuestionIndex.value += 1
}

async function generateFollowUp() {
  if (!session.value || !currentQuestion.value) {
    return
  }
  const answer = currentAnswer.value.trim()
  if (!answer) {
    ElMessage.warning('请先输入本轮回答')
    return
  }
  followUpLoading.value = true
  try {
    const response = await getNextInterviewQuestion({
      question: currentPrompt.value,
      answer,
      history: conversationHistory.value,
      interviewerType: session.value.interview.interviewerType
    })
    const nextQuestion = response.nextQuestion?.trim()
    if (!nextQuestion) {
      ElMessage.warning('AI 暂未生成有效追问，请稍后重试')
      return
    }
    const history = getQuestionHistory()
    history.push({ role: 'user', content: answer })
    history.push({ role: 'ai', content: nextQuestion })
    session.value.conversationHistories![currentQuestion.value.questionNo] = history
    session.value.aiFollowUpList![currentQuestion.value.questionNo] = history
      .filter((item) => item.role === 'ai')
      .map((item) => item.content)
    session.value.answers[currentQuestion.value.questionNo] = buildCombinedAnswer('')
    lastFollowUpReason.value = response.reason
    currentAnswer.value = ''
    saveInterviewSession(session.value)
  } catch (error) {
    ElMessage.error(errorMessage(error, '生成追问失败'))
  } finally {
    followUpLoading.value = false
  }
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

function getQuestionHistory() {
  if (!session.value || !currentQuestion.value) {
    return [] as InterviewConversationMessage[]
  }
  session.value.conversationHistories = session.value.conversationHistories || {}
  return session.value.conversationHistories[currentQuestion.value.questionNo] || []
}

function buildCombinedAnswer(draft: string) {
  const transcript = getQuestionHistory().map((item, index) => {
    const label = item.role === 'ai' ? 'AI追问' : index === 0 ? '初始回答' : '追问回答'
    return `${label}：${item.content}`
  })
  const normalizedDraft = draft.trim()
  if (normalizedDraft) {
    transcript.push(`${transcript.length ? '追问回答' : '初始回答'}：${normalizedDraft}`)
  }
  return transcript.join('\n\n')
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

.session-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 18px;
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

.follow-up-reason {
  margin: 0 0 12px;
  color: #62748e;
  line-height: 1.6;
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

.chat-panel {
  border: 1px solid #d9e2ec;
  border-radius: 8px;
  background: #ffffff;
  padding: 14px;
  min-height: 320px;
}

.chat-panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  color: #102a43;
}

.chat-panel__header span {
  color: #829ab1;
  font-size: 13px;
}

.chat-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-height: 420px;
  overflow: auto;
  padding-right: 4px;
}

.chat-bubble {
  border-radius: 8px;
  padding: 10px 12px;
  line-height: 1.6;
}

.chat-bubble span {
  display: block;
  margin-bottom: 4px;
  font-size: 12px;
  font-weight: 700;
}

.chat-bubble p {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
}

.chat-bubble--ai {
  background: #eff6ff;
  color: #1e3a8a;
}

.chat-bubble--user {
  background: #f0fdf4;
  color: #166534;
}

.follow-up-list {
  margin-top: 16px;
  border-top: 1px solid #e5edf5;
  padding-top: 12px;
}

.follow-up-list h3 {
  margin: 0 0 8px;
  font-size: 15px;
}

.follow-up-list ol {
  margin: 0;
  padding-left: 20px;
  color: #486581;
  line-height: 1.7;
}

@media (max-width: 960px) {
  .session-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .session-header,
  .session-actions {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
