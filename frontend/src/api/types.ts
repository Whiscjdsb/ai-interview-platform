export interface BackendResult<T> {
  code: number
  message: string
  data: T
}

export interface UserInfo {
  id: number
  username: string
  nickname: string
  roles: string[]
}

export interface LoginResult extends UserInfo {
  token: string
  expiresIn: number
}

export interface RegisterPayload {
  username: string
  password: string
  confirmPassword: string
  nickname?: string
}

export interface LoginPayload {
  username: string
  password: string
}

export interface UserStatisticsOverview {
  totalAnswerCount: number
  correctAnswerCount: number
  accuracyRate: number
  totalStudyDuration: number
  favoriteCount: number
  wrongQuestionCount: number
  activeWrongQuestionCount: number
  resolvedWrongQuestionCount: number
  continuousLearningDays: number
  todayAnswerCount: number
  todayStudyDuration: number
}

export interface LearningTrendItem {
  date: string
  answerCount: number
  correctCount: number
  accuracyRate: number
  studyDuration: number
}

export interface WrongAnalysisItem {
  tagId: number
  tagName: string
  wrongQuestionCount: number
  totalWrongCount: number
}
