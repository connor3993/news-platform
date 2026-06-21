<template>
  <div class="search-page page">
    <!-- Search header -->
    <div class="search-header">
      <div class="container">
        <div class="search-input-wrap">
          <el-icon class="search-icon" :size="18"><Search /></el-icon>
          <el-input
            ref="searchInputRef"
            v-model="keyword"
            placeholder="搜索新闻标题或关键词"
            size="large"
            clearable
            @keyup.enter="handleSearch"
            @clear="clearSearch"
          />
          <el-button
            type="primary"
            size="large"
            :loading="searching"
            @click="handleSearch"
          >
            搜索
          </el-button>
        </div>
      </div>
    </div>

    <!-- Recent searches (when no results showing) -->
    <div class="recent-searches container" v-if="!hasSearched && recentSearches.length > 0">
      <div class="recent-header">
        <span class="recent-title">最近搜索</span>
        <span class="recent-clear" @click="clearRecentSearches">
          <el-icon><Delete /></el-icon>
          清除
        </span>
      </div>
      <div class="recent-tags">
        <el-tag
          v-for="(item, index) in recentSearches"
          :key="index"
          class="recent-tag"
          effect="plain"
          round
          @click="searchByRecent(item)"
        >
          {{ item }}
        </el-tag>
      </div>
    </div>

    <!-- Search results -->
    <div class="search-results container" v-if="hasSearched">
      <div class="results-header" v-if="!searching">
        <span>搜索 "{{ displayKeyword }}" 的结果</span>
        <span class="results-count">共 {{ articleStore.total }} 条</span>
      </div>

      <div class="article-grid" v-if="articleStore.articleList.length > 0">
        <ArticleCard
          v-for="article in articleStore.articleList"
          :key="article.id"
          :article="article"
        />
      </div>

      <!-- Loading -->
      <div class="loading-container" v-if="searching">
        <el-icon class="is-loading" :size="24"><Loading /></el-icon>
        <span style="margin-left: 8px; color: var(--text-tertiary);">搜索中...</span>
      </div>

      <!-- Empty -->
      <EmptyState
        v-if="!searching && articleStore.articleList.length === 0 && hasSearched"
        :text="`未找到与 '${displayKeyword}' 相关的文章`"
        :showAction="true"
        actionText="重新搜索"
        @action="focusSearch"
      />

      <!-- Load more -->
      <div class="load-more" v-if="hasMore && !searching && articleStore.articleList.length > 0">
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
import { ref, computed, onMounted, nextTick } from 'vue'
import { useArticleStore } from '@/stores/article'
import ArticleCard from '@/components/ArticleCard.vue'
import EmptyState from '@/components/EmptyState.vue'

defineOptions({ name: 'Search' })

const articleStore = useArticleStore()
const searchInputRef = ref(null)

const keyword = ref('')
const displayKeyword = ref('')
const hasSearched = ref(false)
const searching = ref(false)
const loadingMore = ref(false)
const currentPage = ref(1)
const pageSize = 10

const RECENT_KEY = 'recent_searches'
const recentSearches = ref([])

const hasMore = computed(() => {
  return articleStore.articleList.length < articleStore.total
})

const loadRecentSearches = () => {
  try {
    const stored = localStorage.getItem(RECENT_KEY)
    recentSearches.value = stored ? JSON.parse(stored) : []
  } catch {
    recentSearches.value = []
  }
}

const saveRecentSearch = (kw) => {
  const list = recentSearches.value.filter((item) => item !== kw)
  list.unshift(kw)
  recentSearches.value = list.slice(0, 10)
  localStorage.setItem(RECENT_KEY, JSON.stringify(recentSearches.value))
}

const clearRecentSearches = () => {
  recentSearches.value = []
  localStorage.removeItem(RECENT_KEY)
}

const handleSearch = async () => {
  const kw = keyword.value.trim()
  if (!kw) return
  displayKeyword.value = kw
  saveRecentSearch(kw)
  hasSearched.value = true
  searching.value = true
  currentPage.value = 1
  articleStore.resetArticles()
  try {
    await articleStore.fetchArticles({
      page: 1,
      pageSize,
      keyword: kw
    })
  } finally {
    searching.value = false
  }
}

const clearSearch = () => {
  keyword.value = ''
  hasSearched.value = false
  articleStore.resetArticles()
}

const searchByRecent = (kw) => {
  keyword.value = kw
  handleSearch()
}

const focusSearch = () => {
  nextTick(() => {
    searchInputRef.value?.focus()
  })
}

const loadMore = async () => {
  loadingMore.value = true
  currentPage.value++
  try {
    await articleStore.fetchArticles({
      page: currentPage.value,
      pageSize,
      keyword: displayKeyword.value
    })
  } finally {
    loadingMore.value = false
  }
}

onMounted(() => {
  loadRecentSearches()
  nextTick(() => {
    searchInputRef.value?.focus()
  })
})
</script>

<style scoped>
.search-page {
  padding-bottom: 20px;
}

.search-header {
  background-color: var(--bg-white);
  padding: 16px 0;
  border-bottom: 1px solid var(--border-color);
  position: sticky;
  top: 0;
  z-index: 50;
}

.search-input-wrap {
  display: flex;
  align-items: center;
  gap: 12px;
  max-width: 700px;
  margin: 0 auto;
}

.search-icon {
  color: var(--text-tertiary);
  flex-shrink: 0;
}

.search-input-wrap :deep(.el-input) {
  flex: 1;
}

/* Recent searches */
.recent-searches {
  padding-top: 20px;
}

.recent-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.recent-title {
  font-size: var(--font-size-base);
  font-weight: 600;
  color: var(--text-primary);
}

.recent-clear {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: var(--font-size-sm);
  color: var(--text-tertiary);
  cursor: pointer;
}

.recent-clear:hover {
  color: var(--hot-red);
}

.recent-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.recent-tag {
  cursor: pointer;
}

.recent-tag:hover {
  color: var(--primary-color);
  border-color: var(--primary-color);
}

/* Results */
.search-results {
  padding-top: 16px;
}

.results-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
}

.results-count {
  color: var(--text-tertiary);
}

.article-grid {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.load-more {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

/* Mobile: full screen search */
@media (max-width: 767px) {
  .search-header {
    padding: 10px 0;
  }

  .search-input-wrap {
    gap: 8px;
  }

  .search-input-wrap :deep(.el-button) {
    padding: 8px 12px;
  }

  .article-grid {
    gap: 0;
  }
}

/* PC: two column */
@media (min-width: 768px) {
  .article-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }

  .article-grid > * {
    margin-bottom: 0;
  }
}
</style>
