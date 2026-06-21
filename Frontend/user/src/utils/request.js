import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Request interceptor: attach authentication header
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('user_token')
    if (token) {
      config.headers['authentication'] = token
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
      ElMessage.error(res.msg || '请求失败')
      // Token expired or invalid
      if (res.code === 401 || res.code === 403) {
        localStorage.removeItem('user_token')
        localStorage.removeItem('user_info')
        router.push('/login')
      }
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
    return res
  },
  (error) => {
    const status = error.response?.status
    if (status === 401 || status === 403) {
      localStorage.removeItem('user_token')
      localStorage.removeItem('user_info')
      router.push('/login')
      ElMessage.error('登录已过期，请重新登录')
    } else if (status === 500) {
      ElMessage.error('服务器内部错误')
    } else if (status === 404) {
      ElMessage.error('请求的资源不存在')
    } else {
      ElMessage.error(error.message || '网络异常')
    }
    return Promise.reject(error)
  }
)

export default request
