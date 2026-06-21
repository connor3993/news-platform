import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useAdminStore } from '@/stores/admin'
import router from '@/router'

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
      ElMessage.error(res.msg || '请求失败')
      if (res.code === 401 || res.code === 403) {
        const adminStore = useAdminStore()
        adminStore.logout()
        router.push('/login')
      }
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
    return res
  },
  (error) => {
    if (error.response && (error.response.status === 401 || error.response.status === 403)) {
      const adminStore = useAdminStore()
      adminStore.logout()
      router.push('/login')
    }
    ElMessage.error(error.response?.data?.msg || error.message || '网络异常')
    return Promise.reject(error)
  }
)

export default request
