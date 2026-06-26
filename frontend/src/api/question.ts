import { request } from '@/api/http'
import type { PageResult, Question, QuestionQuery, Tag } from '@/types/question'

export function listQuestionsApi(params: QuestionQuery) {
  return request.get<PageResult<Question>>('/api/questions', { params })
}

export function getQuestionApi(id: number | string) {
  return request.get<Question>(`/api/questions/${id}`)
}

export function listTagsApi() {
  return request.get<Tag[]>('/api/tags')
}
