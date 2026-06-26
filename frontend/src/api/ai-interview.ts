import { request } from '@/api/http'
import type {
  CreateInterviewPayload,
  Interview,
  InterviewHistory,
  InterviewHistoryDetail,
  InterviewHistoryQuery,
  InterviewResult,
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
    createTime: data.createTime || new Date().toISOString()
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
    createTime: data.createTime
  } satisfies Interview
}

export function submitInterview(id: number | string, payload: SubmitInterviewPayload) {
  return request.post<InterviewResult>(`/api/ai/interviews/${id}/submit`, payload)
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
