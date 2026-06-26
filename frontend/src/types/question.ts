export type QuestionType = 'SINGLE_CHOICE' | 'MULTIPLE_CHOICE' | 'JUDGE' | 'SHORT_ANSWER' | 'CODING'

export type QuestionDifficulty = 'EASY' | 'MEDIUM' | 'HARD'

export interface Tag {
  id: number
  tagName: string
  description?: string
}

export interface Question {
  id: number
  title: string
  content: string
  questionType: QuestionType
  difficulty: QuestionDifficulty
  createTime: string
  updateTime?: string
  tags: Tag[]
  correctAnswer?: string
  analysis?: string
}

export interface QuestionQuery {
  page: number
  size: number
  keyword?: string
  questionType?: QuestionType | ''
  difficulty?: QuestionDifficulty | ''
  tagId?: number | ''
}

export interface PageResult<T> {
  page: number
  size: number
  total: number
  pages: number
  records: T[]
}

export interface OptionItem {
  label: string
  text: string
}
