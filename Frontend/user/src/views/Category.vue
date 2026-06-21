<template>
  <div class="category-page page">
    <!-- Category tabs -->
    <CategoryTabs
      :categories="categoryStore.categoryList"
      :activeId="activeCategoryId"
      @select="handleCategorySelect"
    />

    <!-- Article list -->
    <div class="article-list container">
      <div class="article-grid" v-if="articleStore.articleList.length > 0">
        <ArticleCard
          v-for="article in articleStore.articleList"
          :key="article.id"
          :article="article"
        />
      </div>

      <!-- Loading -->
      <div class="loading-container" v-if="articleStore.loading">
        <el-icon class="is-loading" :size="24"><Loading /></el-icon>
        <span style="margin-left: 8px; color: var(--text-tertiary);">加载中...</span>
      </div>

      <!-- Empty -->
      <EmptyState
        v-if="!articleStore.loading && articleStore.articleList.length === 0"
        text="该分类暂无文章"
        :showAction="true"
        actionText="刷新"
        @action="loadArticles"
      />

      <!-- Load more -->
      <div class="load-more" v-if="hasMore && !articleStore.loading && articleStore.articleList.length > 0">
        <el-button
          type="primary"
          plain
          :loading="loadingMore"
          @click="loadMore"
        >
          加载更多
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCategoryStore } from '@/stores/category'
import { useArticleStore } from '@/stores/article'
import ArticleCard from '@/components/ArticleCard.vue'
import CategoryTabs from '@/components/CategoryTabs.vue'
import EmptyState from '@/components/EmptyState.vue'

const route = useRoute()
const router = useRouter()
const categoryStore = useCategoryStore()
const articleStore = useArticleStore()

const activeCategoryId = computed(() => {
  return route.params.id ? Number(route.params.id) : null
})

const currentPage = ref(1)
const pageSize = 10
const loadingMore = ref(false)

const hasMore = computed(() => {
  return articleStore.articleList.length < articleStore.total
})

const loadArticles = async (page = 1) => {
  const params = {
    page,
    pageSize,
    sort: 'latest'
  }
  if (activeCategoryId.value) {
    params.categoryId = activeCategoryId.value
  }
  await articleStore.fetchArticles(params)
}

const loadMore = async () => {
  loadingMore.value = true
  currentPage.value++
  try {
    const params = {
      page: currentPage.value,
      pageSize,
      sort: 'latest'
    }
    if (activeCategoryId.value) {
      params.categoryId = activeCategoryId.value
    }
    await articleStore.fetchArticles(params)
  } finally {
    loadingMore.value = false
  }
}

const handleCategorySelect = (id) => {
  if (id) {
    router.push(`/category/${id}`)
  } else {
    router.push('/home')
  }
}

// Reload when route params change
watch(
  () => route.params.id,
  () => {
    currentPage.value = 1
    articleStore.resetArticles()
    loadArticles()
  }
)

onMounted(async () => {
  if (categoryStore.categoryList.length === 0) {
    await categoryStore.fetchCategories()
  }
  await loadArticles()
})

// Infinite scroll
const handleScroll = () => {
  if (loadingMore.value || !hasMore.value) return
  const scrollTop = document.documentElement.scrollTop || document.body.scrollTop
  const scrollHeight = document.documentElement.scrollHeight
  const clientHeight = document.documentElement.clientHeight
  if (scrollTop + clientHeight >= scrollHeight - 200) {
    loadMore()
  }
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
})

import { onUnmounted } from 'vue'
onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.category-page {
  padding-bottom: 20px;
}

.article-list {
  padding-top: 4px;
}

.article-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.load-more {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

@media (min-width: 768px) {
  .article-grid {
    display: grid;
    grid-template-columns: minmax(0, 1fr);
    gap: 16px;
    max-width: 860px;
  }

  .article-grid > * {
    margin-bottom: 0;
  }
}
</style>
