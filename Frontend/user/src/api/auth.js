import request from '@/utils/request'

/**
 * Register a new user
 */
export function registerApi(data) {
  return request.post('/user/auth/register', data)
}

/**
 * Login user
 */
export function loginApi(data) {
  return request.post('/user/auth/login', data)
}
