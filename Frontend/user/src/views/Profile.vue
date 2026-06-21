<template>
  <div class="profile-page page">
    <div class="profile-container container">
      <!-- User info card -->
      <div class="profile-card">
        <div class="profile-avatar">
          <el-avatar :size="72" :src="userInfo?.avatar">
            <span style="font-size: 28px;">{{ userInfo?.nickname?.charAt(0) || 'U' }}</span>
          </el-avatar>
        </div>
        <div class="profile-info">
          <h2 class="profile-nickname">{{ userInfo?.nickname || '未设置昵称' }}</h2>
          <p class="profile-username">@{{ userInfo?.username }}</p>
        </div>
      </div>

      <!-- Edit form -->
      <div class="edit-section">
        <h3 class="section-title">编辑资料</h3>
        <el-form
          ref="formRef"
          :model="editForm"
          :rules="rules"
          label-position="top"
        >
          <el-form-item label="昵称" prop="nickname">
            <el-input
              v-model="editForm.nickname"
              placeholder="请输入昵称"
              clearable
            />
          </el-form-item>
          <el-form-item label="头像 URL" prop="avatar">
            <el-input
              v-model="editForm.avatar"
              placeholder="请输入头像图片链接"
              clearable
            />
          </el-form-item>
          <div class="avatar-preview" v-if="editForm.avatar">
            <el-avatar :size="48" :src="editForm.avatar" />
            <span class="preview-label">头像预览</span>
          </div>
          <el-form-item>
            <el-button
              type="primary"
              :loading="saving"
              @click="handleSave"
            >
              保存修改
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="quick-section">
        <button class="quick-item" @click="router.push('/submit')">
          <span>
            <strong>我的投稿</strong>
            <em>发布文章、查看审核状态</em>
          </span>
          <el-icon><ArrowRight /></el-icon>
        </button>
        <button class="quick-item" @click="router.push('/favorites')">
          <span>
            <strong>收藏夹</strong>
            <em>查看收藏过的文章</em>
          </span>
          <el-icon><ArrowRight /></el-icon>
        </button>
      </div>

      <!-- Actions -->
      <div class="action-section">
        <el-button
          type="danger"
          plain
          class="logout-btn"
          @click="handleLogout"
        >
          <el-icon><SwitchButton /></el-icon>
          退出登录
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const saving = ref(false)

const userInfo = computed(() => userStore.userInfo)

const editForm = reactive({
  nickname: '',
  avatar: ''
})

const rules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }]
}

onMounted(async () => {
  if (userStore.isLogin) {
    try {
      await userStore.fetchUserInfo()
    } catch (err) {
      // ignore
    }
    editForm.nickname = userInfo.value?.nickname || ''
    editForm.avatar = userInfo.value?.avatar || ''
  }
})

const handleSave = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      await userStore.updateUserInfo({
        nickname: editForm.nickname,
        avatar: editForm.avatar
      })
      ElMessage.success('保存成功')
    } catch (err) {
      // Error handled by interceptor
    } finally {
      saving.value = false
    }
  })
}

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => {
      userStore.logout()
      ElMessage.success('已退出登录')
      router.push('/home')
    })
    .catch(() => {})
}
</script>

<style scoped>
.profile-page {
  padding-bottom: 40px;
}

.profile-container {
  max-width: 560px;
  margin: 0 auto;
  padding-top: 24px;
}

/* Profile card */
.profile-card {
  background-color: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  padding: 32px;
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 24px;
}

.profile-avatar {
  flex-shrink: 0;
}

.profile-nickname {
  font-size: var(--font-size-xl);
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.profile-username {
  font-size: var(--font-size-sm);
  color: var(--text-tertiary);
}

/* Edit section */
.edit-section {
  background-color: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  padding: 24px;
  margin-bottom: 24px;
}

.quick-section {
  background-color: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  padding: 8px;
  margin-bottom: 24px;
}

.quick-item {
  width: 100%;
  min-height: 58px;
  border: 0;
  background: transparent;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  justify-content: space-between;
  text-align: left;
  cursor: pointer;
  padding: 8px 10px;
  border-radius: var(--radius-md);
}

.quick-item:hover {
  background-color: var(--primary-light);
}

.quick-item strong,
.quick-item em {
  display: block;
  font-style: normal;
}

.quick-item em {
  margin-top: 2px;
  color: var(--text-tertiary);
  font-size: var(--font-size-sm);
}

.section-title {
  font-size: var(--font-size-md);
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-color);
}

.avatar-preview {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding: 12px;
  background-color: var(--bg-color);
  border-radius: var(--radius-sm);
}

.preview-label {
  font-size: var(--font-size-sm);
  color: var(--text-tertiary);
}

/* Action section */
.action-section {
  text-align: center;
}

.logout-btn {
  width: 100%;
}

/* Mobile: settings-style list layout */
@media (max-width: 767px) {
  .profile-container {
    padding-top: 12px;
  }

  .profile-card {
    padding: 24px 16px;
    border-radius: var(--radius-md);
    margin-bottom: 12px;
  }

  .profile-avatar :deep(.el-avatar) {
    width: 56px !important;
    height: 56px !important;
  }

  .profile-nickname {
    font-size: var(--font-size-lg);
  }

  .edit-section {
    padding: 16px;
    border-radius: var(--radius-md);
    margin-bottom: 12px;
  }

  .quick-section {
    border-radius: var(--radius-md);
    margin-bottom: 12px;
  }

  .section-title {
    font-size: var(--font-size-base);
    margin-bottom: 16px;
  }
}
</style>
