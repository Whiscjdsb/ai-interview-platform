import { request } from '@/api/http'
import type {
  AdminAiInterviewDetail,
  AdminAiInterviewRecord,
  AdminAnswerRecord,
  AdminDashboard,
  AdminFavoriteRecord,
  AdminQuestion,
  AdminQuestionPayload,
  AdminRecordQuery,
  AdminUser,
  AdminUserQuery,
  AdminWrongQuestionRecord
} from '@/types/admin'
import type { PageResult, QuestionQuery } from '@/types/question'

export function getAdminDashboardApi() {
  return request.get<AdminDashboard>('/api/admin/dashboard')
}

export function listAdminUsersApi(params: AdminUserQuery) {
  return request.get<PageResult<AdminUser>>('/api/admin/users', { params })
}

export function listAdminQuestionsApi(params: QuestionQuery) {
  return request.get<PageResult<AdminQuestion>>('/api/admin/questions', { params })
}

export function getAdminQuestionApi(id: number | string) {
  return request.get<AdminQuestion>(`/api/admin/questions/${id}`)
}

export function createAdminQuestionApi(payload: AdminQuestionPayload) {
  return request.post<AdminQuestion>('/api/admin/questions', payload)
}

export function updateAdminQuestionApi(id: number | string, payload: AdminQuestionPayload) {
  return request.put<AdminQuestion>(`/api/admin/questions/${id}`, payload)
}

export function deleteAdminQuestionApi(id: number | string) {
  return request.delete<void>(`/api/admin/questions/${id}`)
}

export function listAdminFavoritesApi(params: AdminRecordQuery) {
  return request.get<PageResult<AdminFavoriteRecord>>('/api/admin/favorites', { params })
}

export function listAdminWrongQuestionsApi(params: AdminRecordQuery) {
  return request.get<PageResult<AdminWrongQuestionRecord>>('/api/admin/wrong-questions', { params })
}

export function listAdminAnswersApi(params: AdminRecordQuery) {
  return request.get<PageResult<AdminAnswerRecord>>('/api/admin/answers', { params })
}

export function listAdminAiInterviewsApi(params: AdminRecordQuery) {
  return request.get<PageResult<AdminAiInterviewRecord>>('/api/admin/ai-interviews', { params })
}

export function getAdminAiInterviewDetailApi(id: number | string) {
  return request.get<AdminAiInterviewDetail>(`/api/admin/ai-interviews/${id}`)
}
