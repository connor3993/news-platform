import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getCategoryListApi } from '@/api/category'

export const useCategoryStore = defineStore('category', () => {
  const categoryList = ref([])

  async function fetchCategories() {
    const res = await getCategoryListApi()
    categoryList.value = (res.data || []).filter(isValidCategory)
  }

  function isValidCategory(category) {
    const name = String(category?.name || '').trim()
    return Boolean(category?.id) && name && !name.includes('?') && name.length <= 12
  }

  return {
    categoryList,
    fetchCategories
  }
})
