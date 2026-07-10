<template>
  <div class="empty">
    <p>{{ text }}</p>
    <button v-if="showAction" class="btn btn-sm" @click="$emit('action')">{{ actionText }}</button>
  </div>
</template>

<script setup>
defineProps({
  text: { type: String, default: '暂无内容' },
  showAction: { type: Boolean, default: false },
  actionText: { type: String, default: '刷新' }
})
defineEmits(['action'])
</script>
