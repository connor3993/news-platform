import request from '@/utils/request'

export function getTodayStats() {
  return request({
    url: '/admin/dashboard/today',
    method: 'get'
  })
}

export function getReadStatistics(params) {
  return request({
    url: '/admin/statistics/read',
    method: 'get',
    params
  })
}

export function getPublishStatistics(params) {
  return request({
    url: '/admin/statistics/publish',
    method: 'get',
    params
  })
}

export function getHotArticles() {
  return request({
    url: '/admin/statistics/hot',
    method: 'get'
  })
}
