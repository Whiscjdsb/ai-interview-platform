import { request } from '@/api/http'
import type { PageResult } from '@/types/question'
import type { WrongQuestionItem, WrongQuestionQuery } from '@/types/wrong-question'

export function listWrongQuestionsApi(params: WrongQuestionQuery) {
  return request.get<PageResult<WrongQuestionItem>>('/api/wrong-questions', { params })
}

export function removeWrongQuestionApi(questionId: number | string) {
  return request.delete<void>(`/api/wrong-questions/${questionId}`)
}
