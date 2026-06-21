<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <h2 class="login-title">新闻资讯平台</h2>
        <p class="login-subtitle">登录以获取完整体验</p>
      </div>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            size="large"
            clearable
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="large"
            show-password
            clearable
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="login-footer">
        <span>还没有账号？</span>
        <router-link to="/register" class="link">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不少于6位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await userStore.login(form)
      ElMessage.success('登录成功')
      const redirect = route.query.redirect || '/home'
      router.push(redirect)
    } catch (err) {
      // Error handled by interceptor
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-card {
  width: 100%;
  max-width: 400px;
  background-color: var(--bg-white);
  border-radius: var(--radius-lg);
  padding: 40px 32px;
  box-shadow: var(--shadow-lg);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-title {
  font-size: var(--font-size-xxl);
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.login-subtitle {
  font-size: var(--font-size-base);
  color: var(--text-tertiary);
}

.login-form {
  margin-bottom: 8px;
}

.login-btn {
  width: 100%;
  font-size: var(--font-size-md);
}

.login-footer {
  text-align: center;
  font-size: var(--font-size-sm);
  color: var(--text-tertiary);
}

.login-footer .link {
  color: var(--primary-color);
  font-weight: 500;
  margin-left: 4px;
}

.login-footer .link:hover {
  text-decoration: underline;
}

/* Mobile adjustments */
@media (max-width: 767px) {
  .login-card {
    padding: 32px 24px;
    border-radius: var(--radius-md);
  }

  .login-title {
    font-size: var(--font-size-xl);
  }
}
</style>
