import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, logout as logoutApi, getAdminInfo } from '@/api/auth'
import wsService from '@/utils/websocket'
import router from '@/router'

export const useAdminStore = defineStore('admin', () => {
  const token = ref(localStorage.getItem('admin_token') || '')
  const adminInfo = ref(JSON.parse(localStorage.getItem('admin_info') || '{}'))

  const isLogin = computed(() => !!token.value)

  async function login(credentials) {
    const res = await loginApi(credentials)
    const data = res.data
    token.value = data.token
    adminInfo.value = {
      id: data.id,
      username: data.username,
      name: data.name
    }
    localStorage.setItem('admin_token', data.token)
    localStorage.setItem('admin_info', JSON.stringify(adminInfo.value))
    wsService.connect(data.id)
    return data
  }

  async function logout() {
    try {
      await logoutApi()
    } catch (e) {
      // ignore
    }
    wsService.disconnect()
    token.value = ''
    adminInfo.value = {}
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_info')
    router.push('/login')
  }

  async function fetchAdminInfo() {
    try {
      const res = await getAdminInfo()
      adminInfo.value = res.data
      localStorage.setItem('admin_info', JSON.stringify(res.data))
    } catch (e) {
      // ignore
    }
  }

  return {
    token,
    adminInfo,
    isLogin,
    login,
    logout,
    fetchAdminInfo
  }
})
