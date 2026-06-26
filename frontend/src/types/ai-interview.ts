import type { QuestionDifficulty } from '@/types/question'

export type AiRecordType = 'ANSWER_REVIEW' | 'MOCK_INTERVIEW' | 'WEAKNESS_SUMMARY'

export type InterviewStatus = 'CREATED' | 'IN_PROGRESS' | 'SUBMITTED'

export interface CreateInterviewPayload {
  position: string
  difficulty: QuestionDifficulty
  focusTags: string[]
  questionCount: number
}

export interface InterviewQuestion {
  id?: number
  questionNo: number
  question: string
  category: string
  difficulty: QuestionDifficulty
  referencePoints: string[]
}

export interface Interview {
  id: string | number
  position: string
  difficulty: QuestionDifficulty
  questions: InterviewQuestion[]
  modelName: string
  status: InterviewStatus
  createTime: string
}

export interface InterviewAnswer {
  questionId?: number
  questionNo: number
  question: string
  answer: string
}

export interface InterviewQuestionResult extends InterviewAnswer {
  score: number | null
  review: string
  advantages: string[]
  improvements: string[]
  suggestedAnswer: string
}

export interface InterviewResult {
  id: string | number
  position: string
  difficulty: QuestionDifficulty
  totalScore: number
  level: string
  summary: string
  advantages: string[]
  disadvantages: string[]
  improvements: string[]
  suggestions: string[]
  questionResults: InterviewQuestionResult[]
  modelName: string
  createTime: string
}

export interface SubmitInterviewPayload {
  answers: InterviewAnswer[]
}

export interface InterviewHistoryQuery {
  page: number
  size: number
  keyword?: string
  startTime?: string
  endTime?: string
}

export interface InterviewHistory {
  id: number
  recordType: AiRecordType
  questionId: number | null
  questionTitle: string | null
  score: number | null
  modelName: string
  createTime: string
}

export interface InterviewHistoryDetail {
  id: number
  recordType: AiRecordType
  questionId: number | null
  questionTitle: string | null
  score: number | null
  modelName: string
  createTime: string
  result: unknown
}

export interface StoredInterviewSession {
  interview: Interview
  answers: Record<number, string>
  result?: InterviewResult
}
