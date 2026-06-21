<template>
  <div class="top-navbar">
    <div class="navbar-inner container">
      <div class="navbar-brand" @click="router.push('/home')">
        <span class="brand-text">新闻资讯</span>
      </div>
      <div class="navbar-menu">
        <div
          class="menu-item"
          :class="{ active: route.path === '/home' }"
          @click="router.push('/home')"
        >
          首页
        </div>
        <div
          class="menu-item"
          :class="{ active: route.path === '/hot' }"
          @click="router.push('/hot')"
        >
          热点
        </div>
        <div
          v-if="userStore.isLogin"
          class="menu-item"
          :class="{ active: route.path === '/submit' }"
          @click="router.push('/submit')"
        >
          投稿
        </div>
      </div>
      <div class="navbar-actions">
        <div class="search-btn" @click="router.push('/search')">
          <el-icon :size="20"><Search /></el-icon>
        </div>
        <template v-if="userStore.isLogin">
          <el-dropdown trigger="hover" @command="handleUserCommand">
            <div class="user-trigger">
              <el-avatar :size="32" :src="userStore.userInfo?.avatar">
                {{ userInitial }}
              </el-avatar>
              <span class="user-name">{{ displayName }}</span>
              <el-icon :size="14"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon> 个人中心
                </el-dropdown-item>
                <el-dropdown-item command="submit">
                  <el-icon><EditPen /></el-icon> 发布投稿
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button type="primary" size="small" @click="router.push('/login')">
            登录
          </el-button>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const displayName = computed(() => {
  const info = userStore.userInfo || {}
  return info.nickname || info.username || '我的'
})

const userInitial = computed(() => displayName.value.charAt(0).toUpperCase())

const handleUserCommand = (command) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'submit') {
    router.push('/submit')
  } else if (command === 'logout') {
    userStore.logout()
    router.push('/home')
  }
}
</script>

<style scoped>
.top-navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: var(--navbar-height);
  background-color: var(--bg-white);
  border-bottom: 1px solid var(--border-color);
  z-index: 1000;
  backdrop-filter: saturate(180%) blur(12px);
  box-shadow: 0 1px 10px rgba(15, 23, 42, 0.04);
}

.navbar-inner {
  display: flex;
  align-items: center;
  height: 100%;
  gap: 28px;
}

.navbar-brand {
  flex-shrink: 0;
  cursor: pointer;
}

.brand-text {
  font-size: var(--font-size-xl);
  font-weight: 700;
  color: var(--primary-color);
}

.navbar-menu {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  overflow: hidden;
}

.menu-item {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 64px;
  height: 36px;
  padding: 0 14px;
  font-size: var(--font-size-base);
  color: var(--text-secondary);
  cursor: pointer;
  border-radius: 18px;
  transition: all 0.2s ease;
  white-space: nowrap;
  user-select: none;
}

.menu-item:hover {
  color: var(--primary-color);
  background-color: var(--primary-light);
}

.menu-item.active {
  color: var(--primary-color);
  font-weight: 600;
  background-color: var(--primary-light);
}

.navbar-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.search-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  cursor: pointer;
  color: var(--text-secondary);
  transition: all 0.2s ease;
}

.search-btn:hover {
  background-color: var(--primary-light);
  color: var(--primary-color);
}

.user-trigger {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  height: 38px;
  padding: 3px 8px 3px 3px;
  border-radius: 20px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: background-color 0.2s ease, color 0.2s ease;
}

.user-trigger:hover {
  background-color: var(--primary-light);
  color: var(--primary-color);
}

.user-name {
  max-width: 96px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: var(--font-size-sm);
}
</style>
