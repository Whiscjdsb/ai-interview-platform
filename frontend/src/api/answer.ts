import { request } from '@/api/http'
import type {
  AnswerHistoryDetail,
  AnswerHistoryItem,
  AnswerHistoryQuery,
  AnswerSubmitPayload,
  AnswerSubmitResult
} from '@/types/answer'
import type { PageResult } from '@/types/question'

export function submitAnswerApi(payload: AnswerSubmitPayload) {
  return request.post<AnswerSubmitResult>('/api/answers/submit', payload)
}

export function listAnswerHistoryApi(params: AnswerHistoryQuery) {
  return request.get<PageResult<AnswerHistoryItem>>('/api/answers/history', { params })
}

export function getAnswerHistoryDetailApi(id: number | string) {
  return request.get<AnswerHistoryDetail>(`/api/answers/history/${id}`)
}
