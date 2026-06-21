import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getCategoryPage } from '@/api/category'
import { articleStatusMap } from '@/utils/format'

export const useDictStore = defineStore('dict', () => {
  const categoryList = ref([])

  async function fetchCategories() {
    try {
      const res = await getCategoryPage({ page: 1, pageSize: 100, status: 1 })
      categoryList.value = res.data.records || res.data.list || res.data || []
    } catch (e) {
      categoryList.value = []
    }
  }

  return {
    categoryList,
    articleStatusMap,
    fetchCategories
  }
})
