import type { QuestionDifficulty, QuestionType, Tag } from '@/types/question'

export interface FavoriteQuery {
  page: number
  size: number
}

export interface FavoriteItem {
  favoriteId: number
  questionId: number
  title: string
  content: string
  questionType: QuestionType
  difficulty: QuestionDifficulty
  tags: Tag[]
  favoriteTime: string
}
