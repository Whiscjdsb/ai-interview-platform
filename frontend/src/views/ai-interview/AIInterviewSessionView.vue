<template>
  <div class="session-page">
    <el-breadcrumb separator="/" class="ai-breadcrumb">
      <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item :to="{ path: '/ai-interview' }">AI 面试助手</el-breadcrumb-item>
      <el-breadcrumb-item>面试进行中</el-breadcrumb-item>
    </el-breadcrumb>

    <section v-loading="loading" class="session-panel panel">
      <template v-if="session && currentQuestion">
        <header class="session-hero">
          <div class="session-hero__copy">
            <span class="session-eyebrow">Live Interview</span>
            <h1>{{ interviewTitle }}</h1>
            <p>{{ interviewMeta }}</p>
          </div>

          <div class="session-status-grid">
            <div class="status-card status-card--progress">
              <el-progress
                type="dashboard"
                :percentage="progress"
                :width="92"
                :format="() => `${currentQuestionIndex + 1}/${questionCount}`"
              />
              <span>{{ questionProgressText }}</span>
            </div>
            <div class="status-card">
              <span>面试状态</span>
              <strong>{{ interviewStatusText }}</strong>
            </div>
            <div class="status-card">
              <span>当前难度</span>
              <strong>{{ difficultyText(currentQuestion.difficulty) }}</strong>
            </div>
          </div>
        </header>

        <div class="system-strip">
          <span>系统提示</span>
          <p>回答当前问题后可生成 AI 追问。Enter 生成追问，Shift + Enter 换行。</p>
        </div>

        <div class="session-grid">
          <main class="session-main">
            <section class="question-box">
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
            </section>

            <section class="answer-composer">
              <div class="composer-header">
                <strong>我的回答</strong>
                <span>{{ currentAnswer.length }}/5000</span>
              </div>
              <el-input
                v-model="currentAnswer"
                class="answer-input"
                type="textarea"
                :rows="9"
                maxlength="5000"
                show-word-limit
                :disabled="followUpLoading || submitLoading"
                @keydown.enter.exact.prevent="handleEnterSubmit"
                placeholder="请输入你的回答。可以先完整回答问题，再点击“生成追问”让 AI 面试官继续深入提问。"
              />
              <div class="answer-hint">
                <span>Enter 生成 AI 追问</span>
                <span>Shift + Enter 换行</span>
              </div>
            </section>

            <div class="session-actions">
              <el-button :disabled="currentQuestionIndex === 0 || followUpLoading || submitLoading" @click="goPrevious">
                上一题
              </el-button>
              <el-button
                :loading="followUpLoading"
                :disabled="submitLoading"
                type="primary"
                @click="generateFollowUp"
              >
                生成追问
              </el-button>
              <el-button v-if="!isLastQuestion" :disabled="followUpLoading || submitLoading" @click="goNext">
                下一题
              </el-button>
              <el-button v-else :loading="submitLoading" :disabled="followUpLoading" type="success" @click="submitAll">
                提交整场面试
              </el-button>
            </div>
          </main>

          <aside class="chat-panel">
            <div class="chat-panel__header">
              <div>
                <strong>AI 多轮追问</strong>
                <p>围绕当前题目逐步深入</p>
              </div>
              <span>{{ conversationHistory.length }} 条对话</span>
            </div>

            <div class="chat-scroll">
              <div class="system-message">
                <span>System</span>
                <p>当前题目已就绪。提交本轮回答后，AI 会根据历史对话生成一个追问题。</p>
              </div>

              <div v-if="conversationHistory.length" ref="chatListRef" class="chat-list">
                <article
                  v-for="(item, index) in conversationHistory"
                  :key="`${item.role}-${index}`"
                  class="chat-bubble"
                  :class="item.role === 'ai' ? 'chat-bubble--ai' : 'chat-bubble--user'"
                >
                  <span>{{ item.role === 'ai' ? 'AI 面试官' : '我' }}</span>
                  <p>{{ item.content }}</p>
                </article>
              </div>

              <div v-else class="empty-chat">
                <strong>等待第一轮追问</strong>
                <p>先在左侧输入回答，再点击“生成追问”。</p>
              </div>

              <div v-if="followUpLoading" class="thinking-row">
                <span class="thinking-avatar">AI</span>
                <div>
                  <strong>AI 正在思考</strong>
                  <p>正在阅读你的回答并生成下一轮追问</p>
                </div>
                <span class="typing-dots"><i /><i /><i /></span>
              </div>
            </div>

            <div v-if="aiFollowUpList.length" class="follow-up-list">
              <h3>追问记录</h3>
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
import { computed, nextTick, onMounted, ref, watch } from 'vue'
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
const submitLoading = ref(false)
const session = ref<StoredInterviewSession | null>(null)
const currentQuestionIndex = ref(0)
const currentAnswer = ref('')
const lastFollowUpReason = ref('')
const chatListRef = ref<HTMLDivElement>()

const currentQuestion = computed(() => session.value?.interview.questions[currentQuestionIndex.value])
const questionCount = computed(() => session.value?.interview.questions.length || 0)
const interviewTitle = computed(() => session.value?.interview.position || '模拟面试')
const interviewMeta = computed(() => {
  if (!session.value) {
    return ''
  }
  return `${difficultyText(session.value.interview.difficulty)} · ${session.value.interview.modelName}`
})
const interviewStatusText = computed(() => {
  if (submitLoading.value) {
    return '提交中'
  }
  if (followUpLoading.value) {
    return 'AI 追问中'
  }
  return session.value?.interview.status === 'SUBMITTED' ? '已完成' : '进行中'
})
const questionProgressText = computed(() => `第 ${currentQuestionIndex.value + 1} 题 / 共 ${questionCount.value} 题`)
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
watch(
  () => conversationHistory.value.length,
  () => {
    scrollChatToBottom()
  }
)

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
    ElMessage.warning('请先输入本轮回答，再生成 AI 追问')
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
    await scrollChatToBottom()
  } catch (error) {
    ElMessage.error(errorMessage(error, '生成 AI 追问失败，请检查网络或稍后重试'))
  } finally {
    followUpLoading.value = false
  }
}

function handleEnterSubmit() {
  if (!followUpLoading.value && !submitLoading.value) {
    generateFollowUp()
  }
}

async function scrollChatToBottom() {
  await nextTick()
  const element = chatListRef.value
  if (element) {
    element.scrollTo({
      top: element.scrollHeight,
      behavior: 'smooth'
    })
  }
}

async function submitAll() {
  if (!session.value || submitLoading.value) {
    return
  }
  try {
    await ElMessageBox.confirm('提交后将生成本次模拟面试结果，确认提交吗？', '提交面试', {
      confirmButtonText: '提交面试',
      cancelButtonText: '继续作答',
      type: 'warning'
    })
  } catch {
    return
  }

  submitLoading.value = true
  try {
    const result = await submitInterview(session.value.interview.id, {
      answers: answersToPayload(session.value.interview.questions, session.value.answers)
    })
    session.value.interview.status = 'SUBMITTED'
    session.value.result = result
    saveInterviewSession(session.value)
    saveInterviewResult(result)
    await router.push(`/ai-interview/result/${session.value.interview.id}`)
  } catch (error) {
    ElMessage.error(errorMessage(error, '提交面试失败，请稍后重试'))
  } finally {
    submitLoading.value = false
  }
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
.session-page {
  display: grid;
  gap: var(--ai-space-2);
}

.ai-breadcrumb {
  margin-bottom: 2px;
}

.session-panel {
  padding: 22px;
  min-height: 420px;
  background:
    radial-gradient(circle at top right, rgba(124, 58, 237, 0.08), transparent 34%),
    #ffffff;
}

.session-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(360px, 0.9fr);
  gap: var(--ai-space-3);
  align-items: stretch;
  border: 1px solid rgba(191, 219, 254, 0.75);
  border-radius: var(--ai-radius-lg);
  background:
    radial-gradient(circle at 88% 20%, rgba(124, 58, 237, 0.18), transparent 30%),
    linear-gradient(135deg, #0f172a 0%, #1e3a8a 48%, var(--ai-color-primary) 100%);
  padding: 24px;
  color: #ffffff;
  box-shadow: 0 18px 44px rgba(37, 99, 235, 0.18);
}

.session-hero__copy {
  align-self: center;
}

.session-eyebrow {
  display: inline-flex;
  border: 1px solid rgba(255, 255, 255, 0.24);
  border-radius: var(--ai-radius-pill);
  background: rgba(255, 255, 255, 0.12);
  padding: 5px 11px;
  font-size: var(--ai-font-size-xs);
  font-weight: 800;
}

.session-hero h1 {
  margin: 14px 0 8px;
  color: #ffffff;
  font-size: 30px;
  line-height: 1.2;
}

.session-hero p {
  margin: 0;
  color: rgba(255, 255, 255, 0.76);
}

.session-status-grid {
  display: grid;
  grid-template-columns: 1.2fr 1fr 1fr;
  gap: 12px;
}

.status-card {
  display: grid;
  align-content: center;
  border: 1px solid rgba(255, 255, 255, 0.16);
  border-radius: var(--ai-radius-md);
  background: rgba(255, 255, 255, 0.1);
  padding: 14px;
  backdrop-filter: blur(12px);
}

.status-card--progress {
  justify-items: center;
}

.status-card span {
  color: rgba(255, 255, 255, 0.72);
  font-size: var(--ai-font-size-sm);
}

.status-card strong {
  margin-top: 8px;
  color: #ffffff;
  font-size: 20px;
}

.system-strip {
  display: flex;
  align-items: center;
  gap: var(--ai-space-2);
  margin: var(--ai-space-2) 0;
  border: 1px solid var(--ai-color-primary-subtle);
  border-radius: var(--ai-radius-md);
  background: var(--ai-color-primary-soft);
  padding: 12px 14px;
}

.system-strip span {
  flex: none;
  border-radius: var(--ai-radius-pill);
  background: #ffffff;
  padding: 4px 10px;
  color: var(--ai-color-primary);
  font-size: var(--ai-font-size-xs);
  font-weight: 800;
}

.system-strip p {
  margin: 0;
  color: var(--ai-text-secondary);
}

.session-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 400px;
  gap: var(--ai-space-3);
}

.session-main {
  display: grid;
  gap: var(--ai-space-2);
  min-width: 0;
  border: 1px solid var(--ai-border-soft);
  border-radius: var(--ai-radius-md);
  background: linear-gradient(180deg, #ffffff 0%, var(--ai-bg-card-muted) 100%);
  padding: 18px;
}

.question-box,
.answer-composer,
.chat-panel {
  border: 1px solid var(--ai-border-soft);
  border-radius: var(--ai-radius-md);
  background: #ffffff;
  box-shadow: var(--ai-shadow-soft);
}

.question-box {
  border-left: 4px solid var(--ai-color-primary);
  padding: 18px;
}

.question-box h2 {
  margin: 14px 0;
  font-size: 21px;
  line-height: 1.55;
  word-break: break-word;
}

.question-meta,
.reference-points,
.session-actions,
.answer-hint {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.follow-up-reason {
  margin: 0 0 12px;
  border-radius: var(--ai-radius-sm);
  background: var(--ai-color-warning-soft);
  padding: 10px 12px;
  color: #92400e;
  line-height: 1.6;
}

.reference-points span {
  border-radius: var(--ai-radius-pill);
  background: var(--ai-bg-card-muted);
  padding: 5px 10px;
  color: var(--ai-text-secondary);
  font-size: var(--ai-font-size-sm);
}

.answer-composer {
  padding: 14px;
}

.composer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.composer-header span {
  color: var(--ai-text-muted);
  font-size: var(--ai-font-size-sm);
}

.answer-input :deep(.el-textarea__inner) {
  min-height: 220px;
  border-radius: var(--ai-radius-md);
  border-color: var(--ai-color-primary-subtle);
  box-shadow: 0 12px 26px rgba(37, 99, 235, 0.08);
  line-height: 1.7;
  padding: 14px 16px;
}

.answer-input :deep(.el-textarea__inner:focus) {
  box-shadow:
    0 0 0 1px var(--ai-color-primary) inset,
    0 0 0 4px rgba(37, 99, 235, 0.1);
}

.answer-hint {
  justify-content: flex-end;
  margin-top: 8px;
  color: var(--ai-text-muted);
  font-size: var(--ai-font-size-xs);
}

.session-actions {
  justify-content: flex-end;
  position: sticky;
  bottom: 0;
  z-index: 2;
  border: 1px solid rgba(217, 226, 236, 0.86);
  border-radius: var(--ai-radius-md);
  background: rgba(255, 255, 255, 0.92);
  padding: 12px;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.08);
  backdrop-filter: blur(12px);
}

.chat-panel {
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 580px;
  padding: 14px;
  background:
    linear-gradient(180deg, #f8fbff 0%, #f5f8fc 100%);
}

.chat-panel__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
  color: var(--ai-text-primary);
}

.chat-panel__header p {
  margin: 4px 0 0;
  color: var(--ai-text-muted);
  font-size: var(--ai-font-size-sm);
}

.chat-panel__header > span {
  flex: none;
  border-radius: var(--ai-radius-pill);
  background: #ffffff;
  padding: 5px 10px;
  color: var(--ai-text-muted);
  font-size: var(--ai-font-size-xs);
  font-weight: 700;
}

.chat-scroll {
  min-height: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow: hidden;
}

.system-message,
.empty-chat,
.thinking-row {
  border-radius: var(--ai-radius-md);
  padding: 12px 14px;
}

.system-message {
  border: 1px solid var(--ai-border-soft);
  background: rgba(255, 255, 255, 0.8);
}

.system-message span {
  color: var(--ai-color-info);
  font-size: var(--ai-font-size-xs);
  font-weight: 800;
}

.system-message p,
.empty-chat p,
.thinking-row p {
  margin: 4px 0 0;
  color: var(--ai-text-muted);
  line-height: 1.6;
}

.chat-list {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 360px;
  overflow: auto;
  scroll-behavior: smooth;
  padding: 4px 6px 8px 16px;
}

.chat-list::before {
  content: '';
  position: absolute;
  left: 5px;
  top: 8px;
  bottom: 8px;
  width: 2px;
  border-radius: var(--ai-radius-pill);
  background: linear-gradient(180deg, var(--ai-color-primary-subtle), #ddd6fe);
}

.empty-chat {
  display: grid;
  place-items: center;
  min-height: 180px;
  border: 1px dashed var(--ai-border);
  background: rgba(255, 255, 255, 0.72);
  text-align: center;
}

.chat-bubble {
  position: relative;
  width: fit-content;
  max-width: 88%;
  border-radius: 18px;
  padding: 12px 14px;
  line-height: 1.65;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.06);
  animation: bubble-in 0.18s ease both;
}

.chat-bubble span {
  display: block;
  margin-bottom: 4px;
  font-size: var(--ai-font-size-xs);
  font-weight: 800;
}

.chat-bubble p {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
}

.chat-bubble--ai {
  align-self: flex-start;
  border: 1px solid var(--ai-color-primary-subtle);
  background: linear-gradient(135deg, var(--ai-color-primary-soft), #eef2ff);
  color: #1e3a8a;
}

.chat-bubble--user {
  align-self: flex-end;
  border: 1px solid #bbf7d0;
  background: linear-gradient(135deg, var(--ai-color-success-soft), #f0fdf4);
  color: #166534;
}

.thinking-row {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: center;
  gap: 10px;
  border: 1px solid #fde68a;
  background: var(--ai-color-warning-soft);
  color: #92400e;
}

.thinking-avatar {
  display: inline-grid;
  place-items: center;
  width: 34px;
  height: 34px;
  border-radius: 12px;
  background: #ffffff;
  color: var(--ai-color-warning);
  font-size: var(--ai-font-size-xs);
  font-weight: 900;
}

.typing-dots {
  display: inline-flex;
  gap: 4px;
}

.typing-dots i {
  display: block;
  width: 6px;
  height: 6px;
  border-radius: var(--ai-radius-pill);
  background: var(--ai-color-warning);
  animation: typing-dot 1s ease-in-out infinite;
}

.typing-dots i:nth-child(2) {
  animation-delay: 0.16s;
}

.typing-dots i:nth-child(3) {
  animation-delay: 0.32s;
}

.follow-up-list {
  margin-top: 14px;
  border-top: 1px solid var(--ai-border-soft);
  padding-top: 12px;
}

.follow-up-list h3 {
  margin: 0 0 8px;
  font-size: var(--ai-font-size-md);
}

.follow-up-list ol {
  max-height: 160px;
  overflow: auto;
  margin: 0;
  padding-left: 20px;
  color: var(--ai-text-secondary);
  line-height: 1.7;
}

@keyframes typing-dot {
  0%,
  100% {
    opacity: 0.35;
    transform: translateY(0);
  }

  50% {
    opacity: 1;
    transform: translateY(-3px);
  }
}

@keyframes bubble-in {
  from {
    opacity: 0;
    transform: translateY(6px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 1200px) {
  .session-hero,
  .session-grid {
    grid-template-columns: 1fr;
  }

  .chat-panel {
    min-height: 420px;
  }
}

@media (max-width: 760px) {
  .session-panel,
  .session-hero {
    padding: 16px;
  }

  .session-status-grid {
    grid-template-columns: 1fr;
  }

  .system-strip,
  .session-actions {
    align-items: stretch;
    flex-direction: column;
  }

  .chat-bubble {
    max-width: 96%;
  }
}
</style>
