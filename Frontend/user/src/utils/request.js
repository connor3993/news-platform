import axios from 'axios'
import { toast } from '@/utils/message'
import router from '@/router'

const EXPIRED_KEYWORDS = ['expired', 'jwt', 'token', '过期', '已过期', 'invalid']

function isAuthError(msg) {
  if (!msg) return false
  const lower = String(msg).toLowerCase()
  return EXPIRED_KEYWORDS.some(k => lower.includes(k))
}

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Request interceptor: attach token header
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('user_token')
    if (token) {
      config.headers['token'] = token
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor: handle errors
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 1) {
      // 认证失败：静默退出，不弹提示
      if (res.code === 401 || res.code === 403 || isAuthError(res.msg)) {
        localStorage.removeItem('user_token')
        localStorage.removeItem('user_info')
        router.push('/login')
        return Promise.reject(new Error('auth expired'))
      }
      toast(res.msg || '请求失败', 'error')
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
    return res
  },
  (error) => {
    // 没有响应（网络错误/CORS/后端未启动）：静默失败，不弹提示
    if (!error.response) {
      return Promise.reject(error)
    }

    const status = error.response.status
    const errMsg = error.response.data?.msg

    // 认证相关：静默退出
    if (status === 401 || status === 403 || isAuthError(errMsg)) {
      localStorage.removeItem('user_token')
      localStorage.removeItem('user_info')
      router.push('/login')
      return Promise.reject(error)
    }

    if (status === 500) {
      toast('服务器内部错误', 'error')
    } else if (status === 404) {
      toast('请求的资源不存在', 'error')
    } else {
      toast(errMsg || error.message, 'error')
    }
    return Promise.reject(error)
  }
)

export default request
