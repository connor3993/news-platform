<template>
  <div class="layout-container">
    <div class="layout-sidebar">
      <AppSidebar />
    </div>
    <div class="layout-main">
      <AppHeader />
      <div class="layout-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import AppHeader from '@/components/AppHeader.vue'
import AppSidebar from '@/components/AppSidebar.vue'
import { useDictStore } from '@/stores/dict'

const dictStore = useDictStore()

onMounted(() => {
  dictStore.fetchCategories()
})
</script>

<style scoped>
.layout-container {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.layout-sidebar {
  width: 220px;
  min-width: 220px;
  height: 100vh;
  background-color: #304156;
  overflow-y: auto;
}

.layout-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: #f0f2f5;
}

.layout-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}
</style>
