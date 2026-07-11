import request from '@/utils/request'

/**
 * 创建支付订单（POST /user/pay/create）
 * 返回 { orderId, payUrl, totalAmount, status }
 */
export function createPayOrderApi(articleId) {
  return request.post('/user/pay/create', { articleId })
}

/**
 * Mock 支付成功（POST /user/pay/mock-success?orderId=）
 */
export function mockPaySuccessApi(orderId) {
  return request.post(`/user/pay/mock-success?orderId=${encodeURIComponent(orderId)}`)
}

/**
 * 查订单状态（GET /user/pay/order/{orderId}）
 */
export function getPayOrderApi(orderId) {
  return request.get(`/user/pay/order/${encodeURIComponent(orderId)}`)
}

/**
 * 是否已购买（GET /user/pay/purchased?articleId=）
 */
export function isPurchasedApi(articleId) {
  return request.get('/user/pay/purchased', { params: { articleId } })
}
