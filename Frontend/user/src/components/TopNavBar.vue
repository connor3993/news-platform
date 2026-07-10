<template>
  <header class="app-bar">
    <span class="brand" @click="go('/home')">新闻资讯</span>
    <nav class="nav-links">
      <a @click="go('/home')">首页</a>
      <a @click="go('/submit')">投稿</a>
      <a @click="go('/favorites')">收藏</a>
      <a @click="go('/profile')">我的</a>
    </nav>
    <span class="bar-spacer"></span>
    <form class="nav-search" @submit.prevent="onSearch">
      <input v-model="searchKeyword" class="input nav-search-input" placeholder="搜索资讯…" />
      <button class="btn btn-sm" type="submit">搜索</button>
    </form>
    <span class="bar-right">
      <template v-if="userStore.isLogin">
        <span class="uname">{{ displayName }}</span>
        <a @click="logout">退出</a>
      </template>
      <template v-else>
        <a @click="go('/login')">登录</a>
        <a @click="go('/register')">注册</a>
      </template>
    </span>
  </header>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const searchKeyword = ref(route.query.keyword || '')

watch(
  () => route.query.keyword,
  (kw) => {
    searchKeyword.value = kw || ''
  }
)

const displayName = computed(() => {
  const i = userStore.userInfo || {}
  return i.nickname || i.username || '我的'
})

const go = (p) => router.push(p)
const onSearch = () => {
  const kw = searchKeyword.value.trim()
  router.push({ path: '/home', query: kw ? { keyword: kw } : {} })
}
const logout = () => {
  userStore.logout()
  router.push('/home')
}
</script>

<style scoped>
.app-bar .nav-links { margin-left: 0; }
.bar-spacer { flex: 1; }
.nav-search { display: flex; align-items: center; gap: 8px; }
.nav-search-input { width: 200px; padding: 5px 10px; font-size: 13px; }
</style>
