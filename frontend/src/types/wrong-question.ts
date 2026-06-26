import type { QuestionDifficulty, QuestionType, Tag } from '@/types/question'

export type WrongQuestionStatus = 'ACTIVE' | 'RESOLVED'

export interface WrongQuestionQuery {
  page: number
  size: number
  questionType?: QuestionType | ''
  difficulty?: QuestionDifficulty | ''
  status?: WrongQuestionStatus | ''
}

export interface WrongQuestionItem {
  id: number
  questionId: number
  title: string
  content: string
  questionType: QuestionType
  difficulty: QuestionDifficulty
  tags: Tag[]
  wrongCount: number
  lastWrongTime: string
  status: WrongQuestionStatus
}
