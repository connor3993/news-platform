import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useAdminStore } from '@/stores/admin'

const EXPIRED_KEYWORDS = ['expired', 'jwt', 'token', '过期', '已过期', 'invalid']

function isAuthError(msg) {
  if (!msg) return false
  const lower = String(msg).toLowerCase()
  return EXPIRED_KEYWORDS.some(k => lower.includes(k))
}

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 15000
})

request.interceptors.request.use(
  (config) => {
    const adminStore = useAdminStore()
    if (adminStore.token) {
      config.headers['token'] = adminStore.token
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 1) {
      // 认证失败：静默退出登录，不弹提示
      if (res.code === 401 || res.code === 403 || isAuthError(res.msg)) {
        const adminStore = useAdminStore()
        adminStore.logout()
        return Promise.reject(new Error('auth expired'))
      }
      ElMessage.error(res.msg || '请求失败')
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
    return res
  },
  (error) => {
    // HTTP 401/403 或认证相关错误：静默退出
    const status = error.response?.status
    const errMsg = error.response?.data?.msg || error.message
    if ((status === 401 || status === 403) || isAuthError(errMsg)) {
      try {
        const adminStore = useAdminStore()
        if (adminStore.token) adminStore.logout()
      } catch (_) {}
      return Promise.reject(error)
    }
    ElMessage.error(errMsg || '网络异常')
    return Promise.reject(error)
  }
)

export default request
