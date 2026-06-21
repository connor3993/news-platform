<template>
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
.category-tabs {
  position: sticky;
  top: var(--navbar-height);
  z-index: 50;
  padding-top: 14px;
  padding-bottom: 10px;
}

.tabs-scroll {
  display: flex;
  overflow-x: auto;
  white-space: nowrap;
  padding: 6px;
  scrollbar-width: none;
  -ms-overflow-style: none;
  gap: 6px;
  background-color: var(--bg-white);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  box-shadow: var(--shadow-sm);
}

.tabs-scroll::-webkit-scrollbar {
  display: none;
}

.tab-item {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 34px;
  padding: 0 14px;
  font-size: var(--font-size-base);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  flex-shrink: 0;
  user-select: none;
  border-radius: 6px;
}

.tab-item::after {
  display: none;
}

.tab-item.active {
  color: var(--primary-color);
  font-weight: 600;
  background-color: var(--primary-light);
}

.tab-item:hover {
  color: var(--primary-color);
  background-color: var(--primary-light);
}

/* Mobile: smaller tabs */
@media (max-width: 767px) {
  .category-tabs {
    top: 0;
    padding-top: 10px;
    padding-bottom: 8px;
  }

  .tab-item {
    height: 32px;
    padding: 0 12px;
    font-size: var(--font-size-sm);
  }
}
</style>
