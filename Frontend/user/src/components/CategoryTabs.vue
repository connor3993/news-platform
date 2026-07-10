<template>
  <div class="cat-tabs">
    <div class="cat-scroll">
      <div class="cat-item" :class="{ active: !activeId }" @click="select(null)">全部</div>
      <div
        v-for="cat in categories"
        :key="cat.id"
        class="cat-item"
        :class="{ active: activeId === cat.id }"
        @click="select(cat.id)"
      >
        {{ cat.name }}
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  categories: { type: Array, default: () => [] },
  activeId: { type: [Number, String, null], default: null }
})
const emit = defineEmits(['select'])
const select = (id) => emit('select', id)
</script>

<style scoped>
.cat-tabs {
  position: sticky; top: 52px; z-index: 40; background: var(--bg);
  border-bottom: 1px solid var(--border);
}
.cat-scroll {
  display: flex; gap: 8px; overflow-x: auto; white-space: nowrap;
  padding: 10px 14px; scrollbar-width: none;
}
.cat-scroll::-webkit-scrollbar { display: none; }
.cat-item {
  flex-shrink: 0; height: 30px; padding: 0 14px; display: inline-flex; align-items: center;
  border: 1px solid var(--border); font-size: 13px; color: var(--muted); background: var(--bg);
}
.cat-item.active { background: var(--primary); color: #fff; font-weight: 700; border-color: var(--primary); }
</style>
