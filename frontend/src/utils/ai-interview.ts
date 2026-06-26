import type {
  Interview,
  InterviewAnswer,
  InterviewQuestion,
  InterviewResult,
  StoredInterviewSession
} from '@/types/ai-interview'
import type { QuestionDifficulty } from '@/types/question'
import { difficultyLabel, formatDateTime } from '@/utils/question'

const SESSION_PREFIX = 'ai-interview-session:'
const RESULT_PREFIX = 'ai-interview-result:'

export function interviewStatusText(status: Interview['status']) {
  const textMap: Record<Interview['status'], string> = {
    CREATED: '未开始',
    IN_PROGRESS: '进行中',
    SUBMITTED: '已完成'
  }
  return textMap[status]
}

export function scoreColor(score: number) {
  if (score >= 85) {
    return '#16a34a'
  }
  if (score >= 70) {
    return '#2563eb'
  }
  if (score >= 60) {
    return '#d97706'
  }
  return '#dc2626'
}

export function scoreLevel(score: number) {
  if (score >= 90) {
    return '优秀'
  }
  if (score >= 80) {
    return '良好'
  }
  if (score >= 60) {
    return '可提升'
  }
  return '需要加强'
}

export function formatInterviewTime(value?: string) {
  return formatDateTime(value)
}

export function difficultyText(difficulty: QuestionDifficulty) {
  return difficultyLabel(difficulty)
}

export function saveInterviewSession(session: StoredInterviewSession) {
  window.localStorage.setItem(`${SESSION_PREFIX}${session.interview.id}`, JSON.stringify(session))
}

export function getInterviewSession(id: string) {
  const raw = window.localStorage.getItem(`${SESSION_PREFIX}${id}`)
  if (!raw) {
    return null
  }
  try {
    return JSON.parse(raw) as StoredInterviewSession
  } catch {
    return null
  }
}

export function saveInterviewResult(result: InterviewResult) {
  window.localStorage.setItem(`${RESULT_PREFIX}${result.id}`, JSON.stringify(result))
}

export function getInterviewResult(id: string) {
  const raw = window.localStorage.getItem(`${RESULT_PREFIX}${id}`)
  if (!raw) {
    return null
  }
  try {
    return JSON.parse(raw) as InterviewResult
  } catch {
    return null
  }
}

export function buildLocalInterviewResult(
  interview: Interview,
  answers: Record<number, string>
): InterviewResult {
  const questionResults = interview.questions.map((question) => {
    const answer = answers[question.questionNo]?.trim() || ''
    const score = scoreAnswer(answer, question)
    return {
      questionNo: question.questionNo,
      question: question.question,
      answer,
      score,
      review: buildQuestionReview(score, question),
      advantages: buildAdvantages(answer, question),
      improvements: buildImprovements(answer, question),
      suggestedAnswer: `建议围绕 ${question.referencePoints.join('、') || question.category} 展开，结合项目经验说明取舍。`
    }
  })

  const totalScore = Math.round(
    questionResults.reduce((sum, item) => sum + (item.score || 0), 0) / Math.max(questionResults.length, 1)
  )

  return {
    id: interview.id,
    position: interview.position,
    difficulty: interview.difficulty,
    totalScore,
    level: scoreLevel(totalScore),
    summary: `本次 ${interview.position} 模拟面试整体表现为「${scoreLevel(totalScore)}」，建议继续强化表达结构和关键原理说明。`,
    advantages: [
      '能够完成整场面试作答',
      '多数回答能围绕题目主题展开',
      `已覆盖 ${questionResults.filter((item) => item.answer).length} 道题的回答`
    ],
    disadvantages: [
      '部分回答对底层原理说明仍可加强',
      '开放题可以补充更具体的项目案例'
    ],
    improvements: [
      '部分回答可以补充底层原理和边界场景',
      '建议用“结论、原因、例子、总结”的结构组织回答',
      '遇到开放题时可以结合项目经验说明落地方案'
    ],
    suggestions: [
      '复盘低分题并重新组织答案',
      `针对 ${difficultyText(interview.difficulty)} 难度题目准备 3 到 5 个项目案例`,
      '使用答题历史和错题本持续跟踪薄弱知识点'
    ],
    questionResults,
    modelName: interview.modelName || 'LOCAL-MOCK',
    createTime: new Date().toISOString()
  }
}

function scoreAnswer(answer: string, question: InterviewQuestion) {
  if (!answer) {
    return 45
  }
  const lengthScore = Math.min(30, Math.floor(answer.length / 12))
  const keywordScore = question.referencePoints.reduce(
    (score, point) => score + (answer.toLowerCase().includes(point.toLowerCase()) ? 8 : 0),
    0
  )
  const difficultyBonus: Record<QuestionDifficulty, number> = {
    EASY: 18,
    MEDIUM: 14,
    HARD: 10
  }
  return Math.max(0, Math.min(100, 45 + lengthScore + keywordScore + difficultyBonus[question.difficulty]))
}

function buildQuestionReview(score: number, question: InterviewQuestion) {
  if (score >= 85) {
    return `回答较完整，能覆盖 ${question.category} 的核心点，并具备继续追问的基础。`
  }
  if (score >= 70) {
    return `回答方向正确，但对 ${question.category} 的关键机制还可以补充更多细节。`
  }
  return `回答仍偏简略，建议围绕 ${question.category} 的概念、场景和常见问题重新整理。`
}

function buildAdvantages(answer: string, question: InterviewQuestion) {
  const advantages = answer ? ['能够给出自己的回答', '回答内容与题目主题相关'] : ['完成了题目阅读']
  const matchedPoint = question.referencePoints.find((point) =>
    answer.toLowerCase().includes(point.toLowerCase())
  )
  if (matchedPoint) {
    advantages.push(`提到了关键点：${matchedPoint}`)
  }
  return advantages
}

function buildImprovements(answer: string, question: InterviewQuestion) {
  const missingPoints = question.referencePoints
    .filter((point) => !answer.toLowerCase().includes(point.toLowerCase()))
    .slice(0, 2)
  if (!answer) {
    return ['补充完整回答', '说明核心概念和实际应用场景']
  }
  if (!missingPoints.length) {
    return ['补充项目案例', '说明方案的优缺点和适用边界']
  }
  return missingPoints.map((point) => `补充说明：${point}`)
}

export function answersToPayload(questions: InterviewQuestion[], answers: Record<number, string>) {
  return questions.map((question): InterviewAnswer => ({
    questionId: question.id,
    questionNo: question.questionNo,
    question: question.question,
    answer: answers[question.questionNo] || ''
  }))
}
