<template>
  <div class="auth">
    <div class="auth-card">
      <h2 class="auth-title">创建账号</h2>
      <p class="auth-sub">注册后即可使用全部功能</p>
      <form @submit.prevent="register">
        <div class="field">
          <label>用户名</label>
          <input v-model="form.username" class="input" placeholder="3-20 个字符" />
        </div>
        <div class="field">
          <label>昵称</label>
          <input v-model="form.nickname" class="input" placeholder="请输入昵称" />
        </div>
        <div class="field">
          <label>密码</label>
          <input v-model="form.password" type="password" class="input" placeholder="至少 6 位" />
        </div>
        <div class="field">
          <label>确认密码</label>
          <input v-model="form.confirmPassword" type="password" class="input" placeholder="再次输入密码" />
        </div>
        <button class="btn btn-primary btn-block" type="submit" :disabled="loading">注册</button>
      </form>
      <div class="auth-foot">
        已有账号？<a @click="$router.push('/login')">去登录</a>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { registerApi } from '@/api/auth'
import { toast } from '@/utils/message'

const router = useRouter()
const loading = ref(false)
const form = reactive({ username: '', nickname: '', password: '', confirmPassword: '' })

async function register() {
  if (!form.username.trim() || !form.nickname.trim() || !form.password) {
    toast('请完整填写', 'error')
    return
  }
  if (form.password.length < 6) {
    toast('密码至少 6 位', 'error')
    return
  }
  if (form.password !== form.confirmPassword) {
    toast('两次密码不一致', 'error')
    return
  }
  loading.value = true
  try {
    await registerApi({
      username: form.username,
      password: form.password,
      nickname: form.nickname
    })
    toast('注册成功，请登录')
    router.push('/login')
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
