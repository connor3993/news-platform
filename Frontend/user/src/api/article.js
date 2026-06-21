import request from '@/utils/request'
import qs from 'qs'

/**
 * Get article page list
 */
export function getArticlePageApi(params) {
  return request.get('/user/article/page', {
    params,
    paramsSerializer: (params) => qs.stringify(params, { arrayFormat: 'repeat' })
  })
}

/**
 * Get article detail
 */
export function getArticleDetailApi(id) {
  return request.get(`/user/article/${id}`)
}

/**
 * Record article read
 */
export function recordReadApi(id) {
  return request.post(`/user/article/${id}/read`)
}

/**
 * Submit user article for review
 */
export function submitUserArticleApi(data) {
  return request.post('/user/article', data)
}

/**
 * Get current user's submitted articles
 */
export function getMyArticlesApi(params) {
  return request.get('/user/article/mine/page', { params })
}

/**
 * Like an article
 */
export function likeArticleApi(id) {
  return request.post(`/user/article/${id}/like`)
}

/**
 * Dislike an article
 */
export function dislikeArticleApi(id) {
  return request.post(`/user/article/${id}/dislike`)
}

/**
 * Toggle favorite status
 */
export function favoriteArticleApi(id) {
  return request.post(`/user/article/${id}/favorite`)
}

/**
 * Get favorite article page
 */
export function getFavoriteArticlesApi(params) {
  return request.get('/user/article/favorite/page', { params })
}

/**
 * Get article comments
 */
export function getArticleCommentsApi(id, params) {
  return request.get(`/user/article/${id}/comment/page`, { params })
}

/**
 * Add article comment
 */
export function addArticleCommentApi(id, data) {
  return request.post(`/user/article/${id}/comment`, data)
}

/**
 * Get hot articles top 10
 */
export function getHotArticlesApi(params) {
  return request.get('/user/article/hot', { params })
}
