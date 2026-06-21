import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

/**
 * Format timestamp to readable date string
 */
export function formatDate(date, format = 'YYYY-MM-DD HH:mm') {
  if (!date) return ''
  return dayjs(date).format(format)
}

/**
 * Format timestamp to relative time (e.g., "2小时前")
 */
export function formatRelativeTime(date) {
  if (!date) return ''
  return dayjs(date).fromNow()
}

/**
 * Format large numbers with K/W suffix
 */
export function formatNumber(num) {
  if (!num && num !== 0) return '0'
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万'
  }
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return String(num)
}

/**
 * Truncate text with ellipsis
 */
export function truncateText(text, maxLength = 80) {
  if (!text) return ''
  if (text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
}
