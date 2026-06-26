import { request } from '@/api/http'
import type {
  AiUserGrowth,
  CreateInterviewPayload,
  EnterpriseFitAnalysis,
  Interview,
  InterviewHistory,
  InterviewHistoryDetail,
  InterviewHistoryQuery,
  InterviewFollowUpPayload,
  InterviewFollowUpResponse,
  InterviewResult,
  InterviewShareLink,
  InterviewTemplate,
  InterviewTemplatePayload,
  SubmitInterviewPayload
} from '@/types/ai-interview'
import type { PageResult } from '@/types/question'

interface GenerateInterviewResponse {
  id?: number
  position: Interview['position']
  difficulty: Interview['difficulty']
  questionCount?: number
  focusAreas?: string[]
  status?: Interview['status']
  questions: Interview['questions']
  modelName: string
  createTime?: string
  interviewerType?: Interview['interviewerType']
  positionModel?: string
  pressureMode?: boolean
}

interface InterviewDetailResponse {
  id: number
  position: string
  difficulty: Interview['difficulty']
  questionCount: number
  focusAreas: string[]
  status: Interview['status']
  questions: Array<{
    id: number
    questionNo: number
    content: string
    category: string
    difficulty: Interview['difficulty']
    referencePoints: string[]
    referenceAnswer: string | null
  }>
  modelName: string
  createTime: string
  interviewerType?: Interview['interviewerType']
  positionModel?: string
  pressureMode?: boolean
}

export async function createInterview(payload: CreateInterviewPayload) {
  const data = await request.post<GenerateInterviewResponse>('/api/ai/generate-interview', payload)
  return {
    id: data.id || `local-${Date.now()}`,
    position: data.position,
    difficulty: data.difficulty,
    questions: data.questions,
    modelName: data.modelName,
    status: data.status || 'CREATED',
    createTime: data.createTime || new Date().toISOString(),
    interviewerType: data.interviewerType || payload.interviewerType,
    positionModel: data.positionModel || payload.positionModel,
    pressureMode: data.pressureMode ?? payload.pressureMode
  } satisfies Interview
}

export async function getInterview(id: number | string) {
  const data = await request.get<InterviewDetailResponse>(`/api/ai/interviews/${id}`)
  return {
    id: data.id,
    position: data.position,
    difficulty: data.difficulty,
    questions: data.questions.map((question) => ({
      id: question.id,
      questionNo: question.questionNo,
      question: question.content,
      category: question.category,
      difficulty: question.difficulty,
      referencePoints: question.referencePoints
    })),
    modelName: data.modelName,
    status: data.status,
    createTime: data.createTime,
    interviewerType: data.interviewerType,
    positionModel: data.positionModel,
    pressureMode: data.pressureMode
  } satisfies Interview
}

export function submitInterview(id: number | string, payload: SubmitInterviewPayload) {
  return request.post<InterviewResult>(`/api/ai/interviews/${id}/submit`, payload)
}

export function downloadInterviewPdf(id: number | string) {
  return request.get<Blob>(`/api/ai/interview/${id}/export-pdf`, {
    responseType: 'blob'
  })
}

export function createInterviewShareLink(id: number | string) {
  return request.post<InterviewShareLink>(`/api/ai/interview/${id}/share`)
}

export function getSharedInterview(token: string) {
  return request.get<InterviewResult>(`/api/share/${token}`)
}

export function getUserGrowth() {
  return request.get<AiUserGrowth>('/api/ai/user/growth')
}

export function getNextInterviewQuestion(payload: InterviewFollowUpPayload) {
  return request.post<InterviewFollowUpResponse>('/api/ai/interview/next-question', payload)
}

export function createInterviewTemplate(payload: InterviewTemplatePayload) {
  return request.post<InterviewTemplate>('/api/ai/interview/template/create', payload)
}

export function getInterviewTemplates() {
  return request.get<InterviewTemplate[]>('/api/ai/interview/template/list')
}

export function getEnterpriseFitAnalysis(params: {
  positionModel?: string
  companyType?: Interview['interviewerType']
}) {
  return request.get<EnterpriseFitAnalysis>('/api/ai/interview/fit-analysis', { params })
}

export function getInterviewHistory(params: InterviewHistoryQuery) {
  return request.get<PageResult<InterviewHistory>>('/api/ai/history', {
    params: {
      page: params.page,
      size: params.size,
      recordType: 'MOCK_INTERVIEW'
    }
  })
}

export function getInterviewDetail(id: number | string) {
  return request.get<InterviewHistoryDetail>(`/api/ai/history/${id}`)
}
