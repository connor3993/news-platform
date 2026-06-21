import request from '@/utils/request'

/**
 * Get current user info
 */
export function getUserInfoApi() {
  return request.get('/user/user/info')
}

/**
 * Update user info (nickname, avatar)
 */
export function updateUserInfoApi(data) {
  return request.put('/user/user/info', data)
}
