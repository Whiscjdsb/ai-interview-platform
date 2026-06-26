import { request } from '@/api/http'
import type { LoginPayload, LoginResult, RegisterPayload, UserInfo } from '@/api/types'

export function loginApi(payload: LoginPayload) {
  return request.post<LoginResult>('/api/auth/login', payload)
}

export function registerApi(payload: RegisterPayload) {
  return request.post<UserInfo>('/api/auth/register', payload)
}

export function currentUserApi() {
  return request.get<UserInfo>('/api/auth/current')
}
