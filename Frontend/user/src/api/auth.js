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

/**
 * 获取微信扫码登录二维码 ticket
 */
export function wechatLoginApi() {
  return request.get('/api/v1/login/weixin_qrcode_ticket')
}

/**
 * 轮询微信扫码登录结果
 */
export function wechatCheckApi(ticket) {
  return request.get('/api/v1/login/check_login', { params: { ticket } })
}
