import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
  getArticlePageApi,
  getArticleDetailApi,
  recordReadApi,
  getHotArticlesApi,
  submitUserArticleApi,
  getMyArticlesApi,
  likeArticleApi,
  dislikeArticleApi
} from '@/api/article'

export const useArticleStore = defineStore('article', () => {
  const articleList = ref([])
  const total = ref(0)
  const hotList = ref([])
  const currentArticle = ref(null)
  const myArticles = ref([])
  const myTotal = ref(0)
  const loading = ref(false)

  /**
   * Fetch article list with pagination
   */
  async function fetchArticles(params) {
    loading.value = true
    try {
      const res = await getArticlePageApi(params)
      const data = res.data
      if (params.page === 1) {
        articleList.value = data.records || []
      } else {
        articleList.value = [...articleList.value, ...(data.records || [])]
      }
      total.value = data.total || 0
      return data
    } finally {
      loading.value = false
    }
  }

  /**
   * Fetch article detail
   */
  async function fetchArticleDetail(id) {
    const res = await getArticleDetailApi(id)
    currentArticle.value = res.data
    return res.data
  }

  /**
   * Fetch hot articles top 10
   */
  async function fetchHotArticles(params = {}) {
    const res = await getHotArticlesApi(params)
    hotList.value = res.data || []
    return res.data
  }

  /**
   * Record article read
   */
  async function recordRead(id) {
    await recordReadApi(id)
  }

  async function submitUserArticle(data) {
    await submitUserArticleApi(data)
  }

  async function fetchMyArticles(params) {
    const res = await getMyArticlesApi(params)
    const data = res.data
    myArticles.value = data.records || []
    myTotal.value = data.total || 0
    return data
  }

  async function likeArticle(id) {
    const res = await likeArticleApi(id)
    currentArticle.value = res.data
    return res.data
  }

  async function dislikeArticle(id) {
    const res = await dislikeArticleApi(id)
    currentArticle.value = res.data
    return res.data
  }

  /**
   * Reset article list
   */
  function resetArticles() {
    articleList.value = []
    total.value = 0
  }

  return {
    articleList,
    total,
    hotList,
    currentArticle,
    myArticles,
    myTotal,
    loading,
    fetchArticles,
    fetchArticleDetail,
    fetchHotArticles,
    recordRead,
    submitUserArticle,
    fetchMyArticles,
    likeArticle,
    dislikeArticle,
    resetArticles
  }
})
