import request from '@/utils/request'

export function getArticlePage(params) {
  return request({
    url: '/admin/article/page',
    method: 'get',
    params
  })
}

export function getArticleDetail(id) {
  return request({
    url: `/admin/article/${id}`,
    method: 'get'
  })
}

export function addArticle(data) {
  return request({
    url: '/admin/article',
    method: 'post',
    data
  })
}

export function updateArticle(data) {
  return request({
    url: '/admin/article',
    method: 'put',
    data
  })
}

export function deleteArticle(id) {
  return request({
    url: `/admin/article/${id}`,
    method: 'delete'
  })
}

export function submitArticle(id) {
  return request({
    url: `/admin/article/${id}/submit`,
    method: 'post'
  })
}

export function approveArticle(id, data) {
  return request({
    url: `/admin/article/${id}/approve`,
    method: 'post',
    data
  })
}

export function rejectArticle(id, data) {
  return request({
    url: `/admin/article/${id}/reject`,
    method: 'post',
    data
  })
}

export function publishArticle(id) {
  return request({
    url: `/admin/article/${id}/publish`,
    method: 'post'
  })
}

export function offlineArticle(id) {
  return request({
    url: `/admin/article/${id}/offline`,
    method: 'post'
  })
}
