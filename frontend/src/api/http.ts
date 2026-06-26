import axios, { type AxiosRequestConfig } from 'axios'

import type { BackendResult } from '@/api/types'
import { clearAuthStorage, getStoredToken } from '@/utils/storage'

interface ApiError extends Error {
  code?: number
}

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 15000
})

http.interceptors.request.use((config) => {
  const token = getStoredToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    const body = response.data as BackendResult<unknown>
    if (typeof body?.code !== 'number') {
      return response.data
    }
    if (body.code === 0) {
      return body.data
    }
    if (body.code === 401) {
      clearAuthStorage()
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    const error = new Error(body.message || '请求失败') as ApiError
    error.code = body.code
    throw error
  },
  (error) => {
    if (error?.response?.status === 401) {
      clearAuthStorage()
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    throw new Error(error?.response?.data?.message || error.message || '网络请求失败')
  }
)

export const request = {
  get<T>(url: string, config?: AxiosRequestConfig) {
    return http.get<T, T>(url, config)
  },
  post<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
    return http.post<T, T>(url, data, config)
  },
  put<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
    return http.put<T, T>(url, data, config)
  },
  delete<T>(url: string, config?: AxiosRequestConfig) {
    return http.delete<T, T>(url, config)
  }
}
