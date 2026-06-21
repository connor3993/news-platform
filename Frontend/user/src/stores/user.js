import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { loginApi } from '@/api/auth'
import { getUserInfoApi, updateUserInfoApi } from '@/api/user'
import notifyWs from '@/utils/websocket'

export const useUserStore = defineStore(
  'user',
  () => {
    const token = ref(localStorage.getItem('user_token') || '')
    const userInfo = ref(JSON.parse(localStorage.getItem('user_info') || 'null'))

    const isLogin = computed(() => !!token.value)

    /**
     * Login user
     */
    async function login(loginForm) {
      const res = await loginApi(loginForm)
      const data = res.data
      token.value = data.token
      userInfo.value = {
        id: data.id,
        username: data.username,
        nickname: data.nickname,
        avatar: data.avatar
      }
      localStorage.setItem('user_token', data.token)
      localStorage.setItem('user_info', JSON.stringify(userInfo.value))
      notifyWs.connect(data.id)
      return data
    }

    /**
     * Fetch current user info from server
     */
    async function fetchUserInfo() {
      const res = await getUserInfoApi()
      userInfo.value = res.data
      localStorage.setItem('user_info', JSON.stringify(res.data))
      if (res.data?.id) {
        notifyWs.connect(res.data.id)
      }
      return res.data
    }

    /**
     * Update user info (nickname, avatar)
     */
    async function updateUserInfo(data) {
      await updateUserInfoApi(data)
      userInfo.value = { ...userInfo.value, ...data }
      localStorage.setItem('user_info', JSON.stringify(userInfo.value))
    }

    /**
     * Logout user
     */
    function logout() {
      token.value = ''
      userInfo.value = null
      notifyWs.disconnect()
      localStorage.removeItem('user_token')
      localStorage.removeItem('user_info')
    }

    return {
      token,
      userInfo,
      isLogin,
      login,
      logout,
      fetchUserInfo,
      updateUserInfo
    }
  }
)
