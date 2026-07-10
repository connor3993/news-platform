<template>
  <div class="auth">
    <div class="auth-card">
      <h2 class="auth-title">新闻资讯平台</h2>
      <p class="auth-sub">登录以获取完整体验</p>
      <form @submit.prevent="login">
        <div class="field">
          <label>用户名</label>
          <input v-model="form.username" class="input" placeholder="请输入用户名" />
        </div>
        <div class="field">
          <label>密码</label>
          <input v-model="form.password" type="password" class="input" placeholder="请输入密码" />
        </div>
        <button class="btn btn-primary btn-block" type="submit" :disabled="loading">登录</button>
      </form>
      <div class="auth-foot">
        还没有账号？<a @click="$router.push('/register')">立即注册</a>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { toast } from '@/utils/message'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

async function login() {
  if (!form.username.trim() || !form.password) {
    toast('请输入用户名和密码', 'error')
    return
  }
  loading.value = true
  try {
    await userStore.login({ ...form })
    toast('登录成功')
    router.push(route.query.redirect || '/home')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth { min-height: 100vh; display: flex; align-items: center; justify-content: center; padding: 20px; }
.auth-card { width: 100%; max-width: 360px; border: 1px solid #000; background: #fff; padding: 28px 24px; }
.auth-title { font-size: 19px; font-weight: 700; text-align: center; margin-bottom: 4px; }
.auth-sub { font-size: 13px; color: #888; text-align: center; margin-bottom: 22px; }
.auth-foot { text-align: center; font-size: 12px; color: #888; margin-top: 14px; }
.auth-foot a { color: #111; font-weight: 700; cursor: pointer; text-decoration: underline; }
</style>
