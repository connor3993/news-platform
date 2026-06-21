<template>
  <div class="app-container">
    <TopNavBar v-if="isPC" />
    <div class="main-content" :class="{ 'with-tabbar': isMobile && showTabBar, 'with-navbar': isPC }">
      <router-view v-slot="{ Component }">
        <keep-alive :include="cachedViews">
          <component :is="Component" />
        </keep-alive>
      </router-view>
    </div>
    <MobileTabBar v-if="isMobile && showTabBar" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import MobileTabBar from '@/components/MobileTabBar.vue'
import TopNavBar from '@/components/TopNavBar.vue'
import { useUserStore } from '@/stores/user'
import notifyWs from '@/utils/websocket'

const route = useRoute()
const userStore = useUserStore()
const screenWidth = ref(window.innerWidth)
const isPC = computed(() => screenWidth.value >= 768)
const isMobile = computed(() => screenWidth.value < 768)

const cachedViews = ['Home', 'Hot', 'Search']

// Hide tab bar on article detail page
const hideTabBarRoutes = ['ArticleDetail', 'Login', 'Register']
const showTabBar = computed(() => !hideTabBarRoutes.includes(route.name))

const handleResize = () => {
  screenWidth.value = window.innerWidth
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
  if (userStore.isLogin && userStore.userInfo?.id) {
    notifyWs.connect(userStore.userInfo.id)
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  notifyWs.disconnect()
})
</script>

<style scoped>
.app-container {
  min-height: 100vh;
  background-color: var(--bg-color);
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  min-height: 0;
}

.main-content.with-tabbar {
  padding-bottom: 56px;
}

.main-content.with-navbar {
  padding-top: 60px;
}
</style>
