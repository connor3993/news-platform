<template>
  <div>
    <CategoryTabs
      :categories="categoryStore.categoryList"
      :activeId="activeCategoryId"
      @select="onCategory"
    />

    <div class="home-list">
      <ArticleCard v-for="a in articleStore.articleList" :key="a.id" :article="a" />
      <div v-if="articleStore.loading" class="loading">加载中…</div>
      <EmptyState
        v-if="!articleStore.loading && articleStore.articleList.length === 0"
        text="暂无文章"
        :show-action="true"
        action-text="刷新"
        @action="load(1)"
      />
      <div v-if="hasMore && !articleStore.loading" ref="sentinel" class="sentinel"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useCategoryStore } from '@/stores/category'
import { useArticleStore } from '@/stores/article'
import ArticleCard from '@/components/ArticleCard.vue'
import CategoryTabs from '@/components/CategoryTabs.vue'
import EmptyState from '@/components/EmptyState.vue'

defineOptions({ name: 'Home' })

const route = useRoute()
const categoryStore = useCategoryStore()
const articleStore = useArticleStore()

const activeCategoryId = ref(null)
const searchKeyword = ref(route.query.keyword || '')
const currentPage = ref(1)
const pageSize = 10
const sentinel = ref(null)
let observer = null

const hasMore = computed(() => articleStore.articleList.length < articleStore.total)

function buildParams(page) {
  const p = { page, pageSize }
  if (activeCategoryId.value) p.categoryId = activeCategoryId.value
  if (searchKeyword.value.trim()) p.keyword = searchKeyword.value.trim()
  return p
}

async function load(page = 1) {
  currentPage.value = page
  await articleStore.fetchArticles(buildParams(page))
}

// 无限滚动：触底自动加载下一页（配合后端 PageHelper 分页）
async function loadMore() {
  if (articleStore.loading || !hasMore.value) return
  await load(currentPage.value + 1)
}

function onCategory(id) {
  activeCategoryId.value = id
  articleStore.resetArticles()
  load(1)
}

function setupObserver() {
  if (!sentinel.value) return
  observer = new IntersectionObserver(
    (entries) => {
      if (entries[0].isIntersecting) loadMore()
    },
    { rootMargin: '200px' }
  )
  observer.observe(sentinel.value)
}

watch(
  () => route.query.keyword,
  (kw) => {
    searchKeyword.value = kw || ''
    articleStore.resetArticles()
    load(1)
  }
)

onMounted(async () => {
  await categoryStore.fetchCategories()
  await load(1)
  await nextTick()
  setupObserver()
})

onUnmounted(() => {
  if (observer) observer.disconnect()
})
</script>

<style scoped>
.sentinel { height: 1px; }
</style>
