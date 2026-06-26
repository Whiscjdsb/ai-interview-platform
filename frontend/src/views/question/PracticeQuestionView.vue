<template>
  <div>
    <el-breadcrumb separator="/" class="question-breadcrumb">
      <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item :to="{ path: '/questions' }">题库</el-breadcrumb-item>
      <el-breadcrumb-item>刷题</el-breadcrumb-item>
    </el-breadcrumb>

    <section v-loading="loading" class="practice panel">
      <template v-if="question">
        <div class="practice__header">
          <div>
            <h1>{{ question.title }}</h1>
            <div class="practice__meta">
              <el-tag>{{ questionTypeText(question.questionType) }}</el-tag>
              <el-tag :type="difficultyType(question.difficulty)">
                {{ difficultyLabel(question.difficulty) }}
              </el-tag>
              <el-tag v-for="tag in question.tags" :key="tag.id" effect="plain">{{ tag.tagName }}</el-tag>
            </div>
          </div>
          <el-statistic title="答题计时" :value="elapsedSeconds" suffix="秒" />
        </div>

        <el-divider />

        <article class="question-content">
          <pre>{{ stemContent }}</pre>
        </article>

        <div v-if="!submitResult" class="answer-panel">
          <el-radio-group v-if="question.questionType === 'SINGLE_CHOICE'" v-model="singleAnswer" class="option-group">
            <el-radio v-for="option in choiceOptions" :key="option.label" :label="option.label">
              {{ option.label }}. {{ option.text }}
            </el-radio>
          </el-radio-group>

          <el-checkbox-group
            v-else-if="question.questionType === 'MULTIPLE_CHOICE'"
            v-model="multipleAnswer"
            class="option-group"
          >
            <el-checkbox v-for="option in choiceOptions" :key="option.label" :label="option.label">
              {{ option.label }}. {{ option.text }}
            </el-checkbox>
          </el-checkbox-group>

          <el-radio-group v-else-if="question.questionType === 'JUDGE'" v-model="judgeAnswer" class="option-group">
            <el-radio label="正确">正确</el-radio>
            <el-radio label="错误">错误</el-radio>
          </el-radio-group>

          <div v-else>
            <el-input
              v-model="textAnswer"
              type="textarea"
              :rows="question.questionType === 'CODING' ? 14 : 7"
              :maxlength="5000"
              show-word-limit
              :placeholder="question.questionType === 'CODING' ? '请输入代码或解题思路' : '请输入不少于 20 个字符的答案'"
            />
            <p v-if="question.questionType === 'SHORT_ANSWER'" class="answer-tip">
              建议至少输入 20 个字符，便于后续 AI 点评或人工评估。
            </p>
          </div>

          <div class="practice__actions">
            <el-button @click="$router.push('/questions')">返回题库</el-button>
            <el-button type="primary" :loading="submitting" :disabled="!canSubmit" @click="submit">
              提交答案
            </el-button>
          </div>
        </div>

        <AnswerResult
          v-else
          :result="submitResult"
          :user-answer="submittedAnswer"
          @retry="retry"
          @back="$router.push('/questions')"
          @next="$router.push('/questions')"
        />
      </template>

      <el-empty v-else-if="!loading" description="题目不存在或已下架">
        <el-button type="primary" @click="$router.push('/questions')">返回题库</el-button>
      </el-empty>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

import { submitAnswerApi } from '@/api/answer'
import { getQuestionApi } from '@/api/question'
import AnswerResult from '@/components/question/AnswerResult.vue'
import type { AnswerSubmitResult } from '@/types/answer'
import type { OptionItem, Question } from '@/types/question'
import {
  difficultyLabel,
  difficultyType,
  parseChoiceOptions,
  questionTypeText,
  removeChoiceOptions
} from '@/utils/question'
import { errorMessage } from '@/utils/error'

const route = useRoute()
const loading = ref(false)
const submitting = ref(false)
const question = ref<Question | null>(null)
const startedAt = ref(Date.now())
const elapsedSeconds = ref(0)
const singleAnswer = ref('')
const multipleAnswer = ref<string[]>([])
const judgeAnswer = ref('')
const textAnswer = ref('')
const submitResult = ref<AnswerSubmitResult | null>(null)
const submittedAnswer = ref('')
let timer: number | undefined

const choiceOptions = computed<OptionItem[]>(() => {
  if (!question.value) {
    return []
  }
  return parseChoiceOptions(question.value.content)
})

const stemContent = computed(() => {
  if (!question.value) {
    return ''
  }
  if (question.value.questionType === 'SINGLE_CHOICE' || question.value.questionType === 'MULTIPLE_CHOICE') {
    return removeChoiceOptions(question.value.content)
  }
  return question.value.content
})

const currentAnswer = computed(() => {
  if (!question.value) {
    return ''
  }
  if (question.value.questionType === 'SINGLE_CHOICE') {
    return singleAnswer.value
  }
  if (question.value.questionType === 'MULTIPLE_CHOICE') {
    return [...multipleAnswer.value].sort().join(',')
  }
  if (question.value.questionType === 'JUDGE') {
    return judgeAnswer.value
  }
  return textAnswer.value.trim()
})

const canSubmit = computed(() => Boolean(currentAnswer.value && !submitting.value))

onMounted(async () => {
  await loadQuestion()
  startTimer()
})

onUnmounted(() => {
  if (timer) {
    window.clearInterval(timer)
  }
})

async function loadQuestion() {
  loading.value = true
  try {
    question.value = await getQuestionApi(route.params.id as string)
    resetAnswer()
  } catch {
    question.value = null
  } finally {
    loading.value = false
  }
}

function startTimer() {
  startedAt.value = Date.now()
  elapsedSeconds.value = 0
  if (timer) {
    window.clearInterval(timer)
  }
  timer = window.setInterval(() => {
    elapsedSeconds.value = Math.max(0, Math.floor((Date.now() - startedAt.value) / 1000))
  }, 1000)
}

async function submit() {
  if (!question.value) {
    return
  }
  if (question.value.questionType === 'SHORT_ANSWER' && currentAnswer.value.length < 20) {
    ElMessage.warning('简答题建议至少输入 20 个字符')
    return
  }

  try {
    await ElMessageBox.confirm('提交后将展示正确答案和解析，确认提交吗？', '确认提交', {
      confirmButtonText: '提交',
      cancelButtonText: '再想想',
      type: 'warning'
    })
  } catch {
    return
  }

  submitting.value = true
  try {
    const answer = currentAnswer.value
    const result = await submitAnswerApi({
      questionId: question.value.id,
      userAnswer: answer,
      answerDuration: elapsedSeconds.value
    })
    submittedAnswer.value = answer
    submitResult.value = result
    ElMessage.success('提交成功')
  } catch (error) {
    ElMessage.error(errorMessage(error, '提交失败'))
  } finally {
    submitting.value = false
  }
}

function retry() {
  resetAnswer()
  startTimer()
}

function resetAnswer() {
  singleAnswer.value = ''
  multipleAnswer.value = []
  judgeAnswer.value = ''
  textAnswer.value = ''
  submittedAnswer.value = ''
  submitResult.value = null
}
</script>

<style scoped>
.question-breadcrumb {
  margin-bottom: 16px;
}

.practice {
  padding: 22px;
  min-height: 360px;
}

.practice__header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

.practice h1 {
  margin: 0 0 12px;
  font-size: 24px;
}

.practice__meta {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.question-content pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.8;
  font-family: inherit;
}

.answer-panel {
  margin-top: 20px;
}

.option-group {
  display: grid;
  gap: 12px;
  align-items: start;
}

.option-group :deep(.el-radio),
.option-group :deep(.el-checkbox) {
  min-height: 42px;
  align-items: flex-start;
  white-space: normal;
}

.answer-tip {
  margin: 8px 0 0;
  color: #62748e;
  font-size: 13px;
}

.practice__actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 18px;
}

@media (max-width: 760px) {
  .practice__header,
  .practice__actions {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
