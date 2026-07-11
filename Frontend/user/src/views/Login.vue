<template>
  <div class="auth">
    <div class="auth-card">
      <h2 class="auth-title">新闻资讯平台</h2>
      <p class="auth-sub">登录以获取完整体验</p>

      <template v-if="!showWechat">
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
        <button class="btn btn-block btn-wechat" @click="startWechat" :disabled="loading">微信扫码登录</button>
        <div class="auth-foot">
          还没有账号？<a @click="$router.push('/register')">立即注册</a>
        </div>
      </template>

      <div v-else class="wechat-box">
        <p class="auth-sub">请使用微信扫一扫登录</p>
        <div class="qr">
          <img v-if="qrUrl" :src="qrUrl" alt="微信扫码登录二维码" />
          <p v-else class="qr-tip">二维码生成中…</p>
        </div>
        <button class="btn btn-block" @click="backToPwd">返回账号登录</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { toast } from '@/utils/message'
import { wechatLoginApi, wechatCheckApi } from '@/api/auth'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

const showWechat = ref(false)
const qrUrl = ref('')
const qrTicket = ref('')
let pollTimer = null

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

async function startWechat() {
  showWechat.value = true
  qrUrl.value = ''
  try {
    const res = await wechatLoginApi()
    qrTicket.value = res.data
    qrUrl.value = `https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=${encodeURIComponent(res.data)}`
    startPoll()
  } catch (e) {
    toast('获取微信二维码失败', 'error')
  }
}

function startPoll() {
  stopPoll()
  pollTimer = setInterval(async () => {
    try {
      const res = await wechatCheckApi(qrTicket.value)
      if (res.code === 1 && res.data) {
        stopPoll()
        await userStore.loginSuccess(res.data)
        toast('登录成功')
        router.push(route.query.redirect || '/home')
      }
    } catch (e) {
      // 未扫码或网络抖动，继续轮询
    }
  }, 1500)
}

function stopPoll() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

function backToPwd() {
  stopPoll()
  showWechat.value = false
}

onUnmounted(stopPoll)
</script>

<style scoped>
.auth { min-height: 100vh; display: flex; align-items: center; justify-content: center; padding: 20px; }
.auth-card { width: 100%; max-width: 360px; border: 1px solid #000; background: #fff; padding: 28px 24px; }
.auth-title { font-size: 19px; font-weight: 700; text-align: center; margin-bottom: 4px; }
.auth-sub { font-size: 13px; color: #888; text-align: center; margin-bottom: 22px; }
.auth-foot { text-align: center; font-size: 12px; color: #888; margin-top: 14px; }
.auth-foot a { color: #111; font-weight: 700; cursor: pointer; text-decoration: underline; }
.wechat-box { text-align: center; }
.qr { width: 200px; height: 200px; margin: 0 auto 18px; border: 1px solid #000; display: flex; align-items: center; justify-content: center; }
.qr img { width: 100%; height: 100%; object-fit: contain; }
.qr-tip { font-size: 13px; color: #888; }
.btn-wechat { margin-top: 12px; border-color: #07c160; color: #07c160; }
.btn-wechat:hover { background: #07c160; color: #fff; }
</style>
