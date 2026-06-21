<template>
  <div class="home-page page">
    <section class="home-hero">
      <div class="container hero-inner">
        <div class="hero-copy">
          <h1>最新资讯</h1>
          <p>聚合媒体、科技、财经与国际新闻，快速捕捉正在发生的重点。</p>
        </div>
        <div class="hero-search" @click="isMobile && router.push('/search')">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索新闻..."
          size="large"
          clearable
            :readonly="isMobile"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
          <el-button v-if="!isMobile" type="primary" @click.stop="handleSearch">搜索</el-button>
        </div>
      </div>
    </section>

    <!-- Category tabs -->
    <CategoryTabs
      :categories="categoryStore.categoryList"
      :activeId="activeCategoryId"
      @select="handleCategorySelect"
    />

    <!-- Sort bar -->
    <div class="sort-bar container">
      <div class="sort-options">
        <span
          class="sort-item"
          :class="{ active: sortBy === 'latest' }"
          @click="changeSortBy('latest')"
        >
          最新
        </span>
        <span
          class="sort-item"
          :class="{ active: sortBy === 'popular' }"
          @click="changeSortBy('popular')"
        >
          最受欢迎
        </span>
      </div>
      <span class="total-count">共 {{ articleStore.total }} 篇文章</span>
    </div>

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
        text="暂无文章"
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
import { useRouter } from 'vue-router'
import { useCategoryStore } from '@/stores/category'
import { useArticleStore } from '@/stores/article'
import ArticleCard from '@/components/ArticleCard.vue'
import CategoryTabs from '@/components/CategoryTabs.vue'
import EmptyState from '@/components/EmptyState.vue'

defineOptions({ name: 'Home' })

const router = useRouter()
const categoryStore = useCategoryStore()
const articleStore = useArticleStore()

const screenWidth = ref(window.innerWidth)
const isMobile = computed(() => screenWidth.value < 768)

const activeCategoryId = ref(null)
const sortBy = ref('latest')
const searchKeyword = ref('')
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
    sort: sortBy.value
  }
  if (activeCategoryId.value) {
    params.categoryId = activeCategoryId.value
  }
  if (searchKeyword.value) {
    params.keyword = searchKeyword.value
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
      sort: sortBy.value
    }
    if (activeCategoryId.value) {
      params.categoryId = activeCategoryId.value
    }
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }
    await articleStore.fetchArticles(params)
  } finally {
    loadingMore.value = false
  }
}

const handleCategorySelect = (id) => {
  activeCategoryId.value = id
  currentPage.value = 1
  articleStore.resetArticles()
  loadArticles()
}

const changeSortBy = (sort) => {
  if (sortBy.value === sort) return
  sortBy.value = sort
  currentPage.value = 1
  articleStore.resetArticles()
  loadArticles()
}

const handleSearch = () => {
  currentPage.value = 1
  articleStore.resetArticles()
  loadArticles()
}

// Infinite scroll on mobile via scroll event
const handleScroll = () => {
  if (loadingMore.value || !hasMore.value) return
  const scrollTop = document.documentElement.scrollTop || document.body.scrollTop
  const scrollHeight = document.documentElement.scrollHeight
  const clientHeight = document.documentElement.clientHeight
  if (scrollTop + clientHeight >= scrollHeight - 200) {
    loadMore()
  }
}

onMounted(async () => {
  window.addEventListener('resize', () => {
    screenWidth.value = window.innerWidth
  })
  window.addEventListener('scroll', handleScroll)
  await categoryStore.fetchCategories()
  await loadArticles()
})

// Clean up on unmount
import { onUnmounted } from 'vue'
onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.home-page {
  padding-bottom: 20px;
}

.home-hero {
  background: linear-gradient(180deg, #ffffff 0%, #f7fbff 100%);
  border-bottom: 1px solid var(--border-color);
}

.hero-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 32px;
  padding-top: 26px;
  padding-bottom: 26px;
}

.hero-copy h1 {
  font-size: 28px;
  line-height: 1.25;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.hero-copy p {
  font-size: var(--font-size-base);
  color: var(--text-secondary);
}

.hero-search {
  display: flex;
  align-items: center;
  gap: 10px;
  width: min(520px, 48%);
  flex-shrink: 0;
}

.hero-search :deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #dce3ec inset;
}

/* Sort bar */
.sort-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 4px;
  padding-bottom: 16px;
}

.sort-options {
  display: flex;
  gap: 4px;
}

.sort-item {
  display: inline-flex;
  align-items: center;
  height: 32px;
  padding: 0 14px;
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  cursor: pointer;
  border-radius: 16px;
  transition: all 0.2s ease;
}

.sort-item.active {
  color: var(--primary-color);
  background-color: var(--primary-light);
  font-weight: 600;
}

.sort-item:hover {
  color: var(--primary-color);
}

.total-count {
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
}

/* Article grid */
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

/* PC: two-column layout */
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

/* Large screen: wider */
@media (min-width: 1024px) {
  .article-grid {
    grid-template-columns: minmax(0, 1fr);
    gap: 20px;
  }
}

@media (max-width: 767px) {
  .hero-inner {
    display: block;
    padding-top: 16px;
    padding-bottom: 12px;
  }

  .hero-copy h1,
  .hero-copy p {
    display: none;
  }

  .hero-search {
    width: 100%;
  }
}
</style>
