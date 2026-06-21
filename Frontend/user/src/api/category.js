import request from '@/utils/request'

/**
 * Get category list
 */
export function getCategoryListApi() {
  return request.get('/user/category/list')
}
