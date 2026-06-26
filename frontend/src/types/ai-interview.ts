import type { QuestionDifficulty } from '@/types/question'

export type AiRecordType = 'ANSWER_REVIEW' | 'MOCK_INTERVIEW' | 'WEAKNESS_SUMMARY'

export type InterviewStatus = 'CREATED' | 'IN_PROGRESS' | 'SUBMITTED'

export type EnterpriseInterviewerType = 'ALIBABA' | 'TENCENT' | 'BYTEDANCE' | 'STARTUP'

export interface EnterpriseScore {
  technicalScore: number
  architectureScore: number
  communicationScore: number
  complexityScore: number
}

export interface CreateInterviewPayload {
  position: string
  difficulty: QuestionDifficulty
  focusTags: string[]
  questionCount: number
  interviewerType?: EnterpriseInterviewerType
  positionModel?: string
  pressureMode?: boolean
  templateId?: number
}

export interface InterviewQuestion {
  id?: number
  questionNo: number
  question: string
  category: string
  difficulty: QuestionDifficulty
  referencePoints: string[]
}

export interface Interview {
  id: string | number
  position: string
  difficulty: QuestionDifficulty
  questions: InterviewQuestion[]
  modelName: string
  status: InterviewStatus
  createTime: string
  interviewerType?: EnterpriseInterviewerType
  positionModel?: string
  pressureMode?: boolean
}

export interface InterviewAnswer {
  questionId?: number
  questionNo: number
  question: string
  answer: string
}

export interface InterviewQuestionResult extends InterviewAnswer {
  userAnswer?: string
  score: number | null
  review: string
  advantages: string[]
  improvements: string[]
  referenceAnswer?: string
  suggestedAnswer: string
}

export interface AiStructuredResult {
  score: number
  strengths: string[]
  weaknesses: string[]
  suggestions: string[]
  referenceAnswer: string
}

export interface InterviewResult {
  id: string | number
  position: string
  difficulty: QuestionDifficulty
  totalScore: number
  level: string
  summary: string
  advantages: string[]
  disadvantages: string[]
  improvements: string[]
  suggestions: string[]
  questionResults: InterviewQuestionResult[]
  modelName: string
  createTime: string
  rawResponse?: string
  structuredResult?: AiStructuredResult | null
  interviewerType?: EnterpriseInterviewerType
  enterpriseScore?: EnterpriseScore
}

export interface SubmitInterviewPayload {
  answers: InterviewAnswer[]
}

export interface InterviewConversationMessage {
  role: 'user' | 'ai'
  content: string
}

export interface InterviewFollowUpPayload {
  question: string
  answer: string
  history: InterviewConversationMessage[]
  interviewerType?: EnterpriseInterviewerType
}

export interface InterviewFollowUpResponse {
  nextQuestion: string
  reason: string
}

export interface InterviewShareLink {
  shareUrl: string
}

export interface AiGrowthTrendItem {
  date: string
  score: number
}

export interface AiGrowthDimension {
  java: number
  spring: number
  database: number
  systemDesign: number
  project: number
}

export interface AiUserGrowth {
  userId: number
  averageScore: number
  interviewCount: number
  trend: AiGrowthTrendItem[]
  dimension: AiGrowthDimension
}

export interface InterviewTemplatePayload {
  positionModel: string
  companyType: EnterpriseInterviewerType
  difficulty: QuestionDifficulty
  questionCount: number
  focusAreas: string[]
  scoringWeights: Record<string, number>
}

export interface InterviewTemplate extends InterviewTemplatePayload {
  id: number
  createTime: string
}

export interface EnterpriseFitAnalysis {
  fitScore: number
  gapAreas: string[]
  recommendation: string
}

export interface InterviewHistoryQuery {
  page: number
  size: number
  keyword?: string
  startTime?: string
  endTime?: string
}

export interface InterviewHistory {
  id: number
  recordType: AiRecordType
  questionId: number | null
  questionTitle: string | null
  score: number | null
  modelName: string
  createTime: string
}

export interface InterviewHistoryDetail {
  id: number
  recordType: AiRecordType
  questionId: number | null
  questionTitle: string | null
  score: number | null
  modelName: string
  createTime: string
  result: unknown
}

export interface StoredInterviewSession {
  interview: Interview
  answers: Record<number, string>
  conversationHistories?: Record<number, InterviewConversationMessage[]>
  aiFollowUpList?: Record<number, string[]>
  result?: InterviewResult
}
