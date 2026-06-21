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
 * Get hot articles top 10
 */
export function getHotArticlesApi(params) {
  return request.get('/user/article/hot', { params })
}
