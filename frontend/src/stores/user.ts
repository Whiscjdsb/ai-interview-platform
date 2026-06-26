import { defineStore } from 'pinia'

import { currentUserApi, loginApi } from '@/api/auth'
import type { LoginPayload, UserInfo } from '@/api/types'
import {
  clearAuthStorage,
  getStoredToken,
  getStoredUser,
  setStoredToken,
  setStoredUser
} from '@/utils/storage'

interface UserState {
  token: string
  user: UserInfo | null
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    token: getStoredToken(),
    user: getStoredUser()
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.token),
    nickname: (state) => state.user?.nickname || state.user?.username || '同学',
    roles: (state) => state.user?.roles || [],
    isAdmin: (state) => Boolean(state.user?.roles?.includes('ADMIN'))
  },
  actions: {
    async login(payload: LoginPayload) {
      const data = await loginApi(payload)
      this.token = data.token
      this.user = {
        id: data.id,
        username: data.username,
        nickname: data.nickname,
        roles: data.roles
      }
      setStoredToken(data.token)
      setStoredUser(this.user)
      return data
    },
    async fetchCurrentUser() {
      const user = await currentUserApi()
      this.user = user
      setStoredUser(user)
      return user
    },
    async ensureCurrentUser() {
      if (this.token && !this.user) {
        await this.fetchCurrentUser()
      }
    },
    logout() {
      this.token = ''
      this.user = null
      clearAuthStorage()
    }
  }
})
