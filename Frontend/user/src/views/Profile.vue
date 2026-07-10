<template>
  <div class="pad-lg">
    <div class="profile-card">
      <div class="avatar-lg">{{ initial }}</div>
      <div>
        <div class="p-name">{{ userInfo?.nickname || '未设置昵称' }}</div>
        <div class="p-user">@{{ userInfo?.username }}</div>
      </div>
    </div>

    <div class="section" style="margin-top: 16px">
      <h2>编辑资料</h2>
      <div class="field">
        <label>昵称</label>
        <input v-model="editForm.nickname" class="input" placeholder="请输入昵称" />
      </div>
      <div class="field">
        <label>头像 URL</label>
        <input v-model="editForm.avatar" class="input" placeholder="头像图片链接" />
      </div>
      <div class="avatar-preview" v-if="editForm.avatar">
        <img :src="editForm.avatar" alt="预览" />
        <span class="muted">头像预览</span>
      </div>
      <button class="btn btn-primary btn-block" :disabled="saving" @click="save">保存修改</button>
    </div>

    <div class="quick">
      <button class="quick-item" @click="$router.push('/submit')">
        <span><strong>我的投稿</strong><em>发布文章、查看审核状态</em></span><span>›</span>
      </button>
      <button class="quick-item" @click="$router.push('/favorites')">
        <span><strong>收藏夹</strong><em>查看收藏过的文章</em></span><span>›</span>
      </button>
    </div>

    <div class="logout-wrap">
      <button class="btn btn-block" @click="logout">退出登录</button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { toast } from '@/utils/message'

const router = useRouter()
const userStore = useUserStore()
const saving = ref(false)

const userInfo = computed(() => userStore.userInfo)
const initial = computed(() => (userInfo.value?.nickname || 'U').charAt(0).toUpperCase())
const editForm = reactive({ nickname: '', avatar: '' })

onMounted(async () => {
  if (userStore.isLogin) {
    try {
      await userStore.fetchUserInfo()
    } catch (e) {
      // ignore
    }
    editForm.nickname = userInfo.value?.nickname || ''
    editForm.avatar = userInfo.value?.avatar || ''
  }
})

async function save() {
  if (!editForm.nickname.trim()) {
    toast('请输入昵称', 'error')
    return
  }
  saving.value = true
  try {
    await userStore.updateUserInfo({
      nickname: editForm.nickname,
      avatar: editForm.avatar
    })
    toast('保存成功')
  } finally {
    saving.value = false
  }
}

function logout() {
  if (window.confirm('确定要退出登录吗？')) {
    userStore.logout()
    toast('已退出登录')
    router.push('/home')
  }
}
</script>

<style scoped>
.profile-card { display: flex; align-items: center; gap: 14px; border: 1px solid #000; padding: 18px; background: #fff; }
.avatar-lg {
  width: 54px; height: 54px; flex-shrink: 0; border: 1px solid #000; background: #f4f4f4;
  display: flex; align-items: center; justify-content: center; font-size: 22px; font-weight: 700;
}
.p-name { font-size: 17px; font-weight: 700; }
.p-user { font-size: 12px; color: #999; }
.avatar-preview { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
.avatar-preview img { width: 48px; height: 48px; object-fit: cover; border: 1px solid #000; }
.quick { border: 1px solid #000; margin-top: 16px; background: #fff; }
.quick-item {
  width: 100%; min-height: 52px; display: flex; align-items: center; justify-content: space-between;
  padding: 10px 14px; border: none; background: none; border-bottom: 1px solid #ececec;
  text-align: left; cursor: pointer;
}
.quick-item:last-child { border-bottom: none; }
.quick-item:hover { background: #f4f4f4; }
.quick-item strong { display: block; font-size: 14px; }
.quick-item em { display: block; font-style: normal; font-size: 12px; color: #999; margin-top: 2px; }
.logout-wrap { margin-top: 16px; }
</style>
