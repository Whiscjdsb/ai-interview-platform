import { request } from '@/api/http'
import type { LearningTrendItem, UserStatisticsOverview, WrongAnalysisItem } from '@/api/types'

export function userOverviewApi() {
  return request.get<UserStatisticsOverview>('/api/statistics/user/overview')
}

export function userTrendApi(days = 7) {
  return request.get<LearningTrendItem[]>('/api/statistics/user/trend', { params: { days } })
}

export function userWrongAnalysisApi() {
  return request.get<WrongAnalysisItem[]>('/api/statistics/user/wrong-analysis')
}
