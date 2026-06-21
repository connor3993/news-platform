import request from '@/utils/request'

export function getCategoryPage(params) {
  return request({
    url: '/admin/category/page',
    method: 'get',
    params
  })
}

export function addCategory(data) {
  return request({
    url: '/admin/category',
    method: 'post',
    data
  })
}

export function updateCategory(data) {
  return request({
    url: '/admin/category',
    method: 'put',
    data
  })
}

export function deleteCategory(id) {
  return request({
    url: `/admin/category/${id}`,
    method: 'delete'
  })
}

export function updateCategoryStatus(id, status) {
  return request({
    url: `/admin/category/status/${status}`,
    method: 'post',
    params: { id }
  })
}
