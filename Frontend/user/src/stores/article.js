import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
  getArticlePageApi,
  getArticleDetailApi,
  recordReadApi,
  submitUserArticleApi,
  getMyArticlesApi,
  likeArticleApi,
  dislikeArticleApi,
  favoriteArticleApi,
  getFavoriteArticlesApi
} from '@/api/article'

export const useArticleStore = defineStore('article', () => {
  const articleList = ref([])
  const total = ref(0)
  const currentArticle = ref(null)
  const myArticles = ref([])
  const myTotal = ref(0)
  const favoriteArticles = ref([])
  const favoriteTotal = ref(0)
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
    const detail = res.data
    if (localStorage.getItem('user_token') && detail && typeof detail.favorited !== 'boolean') {
      try {
        const favoriteRes = await getFavoriteArticlesApi({ page: 1, pageSize: 1000 })
        const favorites = favoriteRes.data?.records || []
        detail.favorited = favorites.some((item) => String(item.id) === String(id))
      } catch (err) {
        detail.favorited = false
      }
    }
    currentArticle.value = detail
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

  async function favoriteArticle(id) {
    const res = await favoriteArticleApi(id)
    currentArticle.value = res.data
    return res.data
  }

  async function fetchFavoriteArticles(params) {
    const res = await getFavoriteArticlesApi(params)
    const data = res.data
    favoriteArticles.value = data.records || []
    favoriteTotal.value = data.total || 0
    return data
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
    currentArticle,
    myArticles,
    myTotal,
    favoriteArticles,
    favoriteTotal,
    loading,
    fetchArticles,
    fetchArticleDetail,
    recordRead,
    submitUserArticle,
    fetchMyArticles,
    likeArticle,
    dislikeArticle,
    favoriteArticle,
    fetchFavoriteArticles,
    resetArticles
  }
})
