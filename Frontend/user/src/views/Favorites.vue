<template>
  <div class="favorites-page page">
    <div class="favorites-container container">
      <div class="page-head">
        <h1>收藏夹</h1>
        <p>你收藏过的文章会保存在这里。</p>
      </div>
      <div class="article-grid" v-if="articleStore.favoriteArticles.length > 0">
        <ArticleCard
          v-for="article in articleStore.favoriteArticles"
          :key="article.id"
          :article="article"
        />
      </div>
      <div class="loading-container" v-if="loading">
        <el-icon class="is-loading" :size="24"><Loading /></el-icon>
      </div>
      <EmptyState
        v-if="!loading && articleStore.favoriteArticles.length === 0"
        text="暂无收藏文章"
        :showAction="true"
        actionText="去首页看看"
        @action="router.push('/home')"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useArticleStore } from '@/stores/article'
import ArticleCard from '@/components/ArticleCard.vue'
import EmptyState from '@/components/EmptyState.vue'

const router = useRouter()
const articleStore = useArticleStore()
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    await articleStore.fetchFavoriteArticles({ page: 1, pageSize: 20 })
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.favorites-page {
  padding: 24px 0 40px;
}

.page-head {
  margin-bottom: 18px;
}

.page-head h1 {
  font-size: var(--font-size-xxl);
  color: var(--text-primary);
}

.page-head p {
  margin-top: 4px;
  color: var(--text-tertiary);
}

.article-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 16px;
}

@media (max-width: 767px) {
  .article-grid {
    gap: 12px;
  }
}

@media (min-width: 768px) {
  .article-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
  }
}

@media (min-width: 1024px) {
  .article-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 24px;
  }
}
</style>
