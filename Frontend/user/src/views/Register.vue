<template>
  <div class="register-page">
    <div class="register-card">
      <div class="register-header">
        <h2 class="register-title">创建账号</h2>
        <p class="register-subtitle">注册后即可使用全部功能</p>
      </div>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="register-form"
        @submit.prevent="handleRegister"
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
        <el-form-item prop="nickname">
          <el-input
            v-model="form.nickname"
            placeholder="请输入昵称"
            prefix-icon="UserFilled"
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
          />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请确认密码"
            prefix-icon="Lock"
            size="large"
            show-password
            clearable
            @keyup.enter="handleRegister"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="register-btn"
            :loading="loading"
            @click="handleRegister"
          >
            注册
          </el-button>
        </el-form-item>
      </el-form>
      <div class="register-footer">
        <span>已有账号？</span>
        <router-link to="/login" class="link">去登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { registerApi } from '@/api/auth'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  nickname: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度3-20个字符', trigger: 'blur' }
  ],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await registerApi({
        username: form.username,
        password: form.password,
        nickname: form.nickname
      })
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } catch (err) {
      // Error handled by interceptor
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.register-card {
  width: 100%;
  max-width: 400px;
  background-color: var(--bg-white);
  border-radius: var(--radius-lg);
  padding: 40px 32px;
  box-shadow: var(--shadow-lg);
}

.register-header {
  text-align: center;
  margin-bottom: 32px;
}

.register-title {
  font-size: var(--font-size-xxl);
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.register-subtitle {
  font-size: var(--font-size-base);
  color: var(--text-tertiary);
}

.register-form {
  margin-bottom: 8px;
}

.register-btn {
  width: 100%;
  font-size: var(--font-size-md);
}

.register-footer {
  text-align: center;
  font-size: var(--font-size-sm);
  color: var(--text-tertiary);
}

.register-footer .link {
  color: var(--primary-color);
  font-weight: 500;
  margin-left: 4px;
}

.register-footer .link:hover {
  text-decoration: underline;
}

@media (max-width: 767px) {
  .register-card {
    padding: 32px 24px;
    border-radius: var(--radius-md);
  }

  .register-title {
    font-size: var(--font-size-xl);
  }
}
</style>
