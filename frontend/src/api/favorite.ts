import { request } from '@/api/http'
import type { FavoriteItem, FavoriteQuery } from '@/types/favorite'
import type { PageResult } from '@/types/question'

export function addFavoriteApi(questionId: number | string) {
  return request.post<FavoriteItem>(`/api/favorites/${questionId}`)
}

export function removeFavoriteApi(questionId: number | string) {
  return request.delete<void>(`/api/favorites/${questionId}`)
}

export function listFavoritesApi(params: FavoriteQuery) {
  return request.get<PageResult<FavoriteItem>>('/api/favorites', { params })
}
