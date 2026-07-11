<template>
  <div class="app">
    <TopNavBar v-if="!hideChrome" />
    <router-view />
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import TopNavBar from '@/components/TopNavBar.vue'

const route = useRoute()
const hideChrome = computed(() => ['Login', 'Register'].includes(route.name))

const userStore = useUserStore()
onMounted(() => {
  // 刷新页面后恢复登录态时，重新拉取用户信息以触发 WS 重连
  if (userStore.isLogin) {
    userStore.fetchUserInfo().catch(() => {})
  }
})
</script>
