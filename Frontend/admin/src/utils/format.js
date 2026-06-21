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
  0: { label: '草稿', type: 'info' },
  1: { label: '待审核', type: 'warning' },
  2: { label: '审核通过', type: 'success' },
  3: { label: '审核驳回', type: 'danger' },
  4: { label: '已发布', type: '' },
  5: { label: '已下架', type: 'info' }
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
