<template>
  <div class="mobile-tabbar">
    <div
      v-for="tab in tabs"
      :key="tab.path"
      class="tabbar-item"
      :class="{ active: isActive(tab.path) }"
      @click="navigateTo(tab.path)"
    >
      <div class="tab-icon">
        <el-icon :size="22">
          <component :is="tab.icon" />
        </el-icon>
      </div>
      <span class="tab-label">{{ tab.label }}</span>
    </div>
  </div>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const tabs = [
  { path: '/home', label: '首页', icon: 'HomeFilled' },
  { path: '/hot', label: '热点', icon: 'TrendCharts' },
  { path: '/submit', label: '投稿', icon: 'EditPen' },
  { path: '/search', label: '搜索', icon: 'Search' },
  { path: '/profile', label: '我的', icon: 'User' }
]

const isActive = (path) => {
  return route.path === path || route.path.startsWith(path + '/')
}

const navigateTo = (path) => {
  if (route.path !== path) {
    router.push(path)
  }
}
</script>

<style scoped>
.mobile-tabbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: var(--tabbar-height);
  background-color: var(--bg-white);
  border-top: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: space-around;
  z-index: 1000;
  padding-bottom: env(safe-area-inset-bottom, 0);
}

.tabbar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  height: 100%;
  cursor: pointer;
  transition: color 0.2s ease;
  color: var(--text-tertiary);
  user-select: none;
  -webkit-tap-highlight-color: transparent;
}

.tabbar-item:active {
  opacity: 0.7;
}

.tabbar-item.active {
  color: var(--primary-color);
}

.tab-icon {
  margin-bottom: 2px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tab-label {
  font-size: 10px;
  line-height: 1;
}
</style>
