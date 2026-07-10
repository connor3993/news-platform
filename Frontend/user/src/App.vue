<template>
  <div class="app">
    <TopNavBar v-if="!hideChrome" />
    <router-view />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import TopNavBar from '@/components/TopNavBar.vue'

const route = useRoute()
const hideChrome = computed(() => ['Login', 'Register'].includes(route.name))
</script>
