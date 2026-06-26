import type { QuestionDifficulty, QuestionType, Tag } from '@/types/question'

export interface AnswerSubmitPayload {
  questionId: number
  userAnswer: string
  answerDuration: number
}

export interface AnswerSubmitResult {
  answerRecordId: number
  isCorrect: boolean | null
  score: number | null
  correctAnswer: string
  analysis: string
  message: string
}

export interface AnswerHistoryQuery {
  page: number
  size: number
  questionType?: QuestionType | ''
  isCorrect?: boolean | ''
  startTime?: string
  endTime?: string
}

export interface AnswerHistoryItem {
  id: number
  questionId: number
  title: string
  questionType: QuestionType
  difficulty: QuestionDifficulty
  userAnswer: string
  isCorrect: boolean | null
  score: number | null
  answerDuration: number
  answerTime: string
}

export interface AnswerHistoryDetail extends AnswerHistoryItem {
  content: string
  tags: Tag[]
  correctAnswer: string
  analysis: string
}
