import type { Question, QuestionDifficulty, QuestionType, Tag } from '@/types/question'
import type { WrongQuestionStatus } from '@/types/wrong-question'

export interface AdminDashboard {
  totalUserCount: number
  totalQuestionCount: number
  totalFavoriteCount: number
  totalWrongQuestionCount: number
  totalAnswerCount: number
  totalAiInterviewCount: number
}

export interface AdminUserQuery {
  page: number
  size: number
  keyword?: string
}

export interface AdminUser {
  id: number
  username: string
  nickname: string
  email: string | null
  status: number
  roles: string[]
  answerCount: number
  lastAnswerTime: string | null
  createTime: string
}

export interface AdminRecordQuery {
  page: number
  size: number
  userKeyword?: string
  questionKeyword?: string
  positionKeyword?: string
  questionType?: QuestionType | ''
  difficulty?: QuestionDifficulty | ''
  isCorrect?: boolean | ''
  status?: WrongQuestionStatus | ''
  startTime?: string
  endTime?: string
}

export interface AdminFavoriteRecord {
  id: number
  userId: number
  username: string | null
  nickname: string | null
  questionId: number
  questionTitle: string | null
  questionType: QuestionType | null
  difficulty: QuestionDifficulty | null
  createTime: string
}

export interface AdminWrongQuestionRecord extends AdminFavoriteRecord {
  wrongCount: number
  status: WrongQuestionStatus
  lastWrongTime: string
}

export interface AdminAnswerRecord extends AdminFavoriteRecord {
  userAnswer: string
  correctAnswer: string | null
  analysis: string | null
  isCorrect: boolean | null
  score: number | null
  answerDuration: number
  answerTime: string
}

export interface AdminAiInterviewRecord {
  id: number
  userId: number
  username: string | null
  nickname: string | null
  position: string | null
  score: number | null
  modelName: string | null
  createTime: string
}

export interface AdminAiInterviewDetail extends AdminAiInterviewRecord {
  result: unknown
}

export interface AdminQuestionPayload {
  title: string
  content: string
  questionType: QuestionType
  difficulty: QuestionDifficulty
  correctAnswer: string
  analysis?: string
  tagIds: number[]
}

export interface AdminQuestionFormModel extends AdminQuestionPayload {
  optionsText: string
}

export type AdminQuestion = Question

export type AdminTag = Tag
