import request from '@/utils/request'

export function getLogPage(params) {
  return request({
    url: '/admin/log/page',
    method: 'get',
    params
  })
}
