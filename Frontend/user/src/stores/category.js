import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getCategoryListApi } from '@/api/category'

const FALLBACK_CATEGORIES = [
  { id: 1, name: '科技' },
  { id: 2, name: '财经' },
  { id: 3, name: '传媒' },
  { id: 4, name: '国际' }
]

export const useCategoryStore = defineStore('category', () => {
  const categoryList = ref([])

  async function fetchCategories() {
    try {
      const res = await getCategoryListApi()
      const list = (res.data || []).filter(isValidCategory)
      if (list.length > 0) {
        categoryList.value = list
      } else {
        categoryList.value = [...FALLBACK_CATEGORIES]
      }
    } catch (e) {
      categoryList.value = [...FALLBACK_CATEGORIES]
    }
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
