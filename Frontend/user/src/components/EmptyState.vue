<template>
  <div class="empty-state">
    <div class="empty-icon">
      <el-icon :size="64" color="var(--text-quaternary)">
        <Document />
      </el-icon>
    </div>
    <p class="empty-text">{{ text }}</p>
    <div class="empty-action" v-if="showAction">
      <el-button type="primary" size="small" @click="$emit('action')">
        {{ actionText }}
      </el-button>
    </div>
  </div>
</template>

<script setup>
defineProps({
  text: {
    type: String,
    default: '暂无内容'
  },
  showAction: {
    type: Boolean,
    default: false
  },
  actionText: {
    type: String,
    default: '刷新重试'
  }
})

defineEmits(['action'])
</script>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.empty-icon {
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-text {
  font-size: var(--font-size-base);
  color: var(--text-tertiary);
  margin-bottom: 16px;
}

.empty-action {
  margin-top: 8px;
}
</style>
