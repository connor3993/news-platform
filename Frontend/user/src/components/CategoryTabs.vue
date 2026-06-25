<template>
  <div class="category-tabs-wrapper">
    <div class="category-tabs container">
      <div class="tabs-scroll" role="tablist" aria-label="新闻分类">
        <div
          class="tab-item"
          :class="{ active: !activeId }"
          @click="handleSelect(null)"
        >
          全部
        </div>
        <div
          v-for="cat in categories"
          :key="cat.id"
          class="tab-item"
          :class="{ active: activeId === cat.id }"
          @click="handleSelect(cat.id)"
        >
          {{ cat.name }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  categories: {
    type: Array,
    default: () => []
  },
  activeId: {
    type: [Number, String, null],
    default: null
  }
})

const emit = defineEmits(['select'])

const handleSelect = (id) => {
  emit('select', id)
}
</script>

<style scoped>
.category-tabs-wrapper {
  position: sticky;
  top: var(--navbar-height);
  z-index: 50;
  background-color: var(--bg-color);
  border-bottom: 1px solid var(--border-color);
}

.category-tabs {
  padding-top: 12px;
  padding-bottom: 12px;
}

.tabs-scroll {
  display: flex;
  overflow-x: auto;
  white-space: nowrap;
  scrollbar-width: none;
  -ms-overflow-style: none;
  gap: 8px;
  padding: 4px 0;
}

.tabs-scroll::-webkit-scrollbar {
  display: none;
}

.tab-item {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 36px;
  padding: 0 16px;
  font-size: var(--font-size-base);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  flex-shrink: 0;
  user-select: none;
  border-radius: 18px;
  background-color: var(--bg-white);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm);
}

.tab-item.active {
  color: var(--primary-color);
  font-weight: 600;
  background-color: var(--primary-light);
  border-color: var(--primary-light);
}

.tab-item:hover:not(.active) {
  color: var(--primary-color);
  border-color: var(--border-strong);
  background-color: var(--bg-white);
  box-shadow: var(--shadow-md);
}

/* Mobile: smaller tabs */
@media (max-width: 767px) {
  .category-tabs-wrapper {
    top: 0;
  }

  .category-tabs {
    padding-top: 10px;
    padding-bottom: 10px;
  }

  .tab-item {
    height: 32px;
    padding: 0 12px;
    font-size: var(--font-size-sm);
    border-radius: 16px;
  }
}
</style>
