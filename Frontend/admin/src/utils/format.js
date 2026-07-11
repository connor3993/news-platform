import dayjs from 'dayjs'

export function formatDate(date, format = 'YYYY-MM-DD') {
  if (!date) return ''
  return dayjs(date).format(format)
}

export function formatDateTime(date) {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

export const articleStatusMap = {
  DRAFT: { label: '草稿', type: 'info' },
  PENDING: { label: '待审核', type: 'warning' },
  APPROVED: { label: '审核通过', type: 'success' },
  REJECTED: { label: '审核驳回', type: 'danger' },
  PUBLISHED: { label: '已发布', type: '' },
  OFFLINE: { label: '已下架', type: 'info' }
}

export function getArticleStatusLabel(status) {
  const item = articleStatusMap[status]
  return item ? item.label : '未知'
}

export function getArticleStatusType(status) {
  const item = articleStatusMap[status]
  return item ? item.type : 'info'
}

export function getLastNDays(n) {
  const days = []
  for (let i = n - 1; i >= 0; i--) {
    days.push(dayjs().subtract(i, 'day').format('YYYY-MM-DD'))
  }
  return days
}
