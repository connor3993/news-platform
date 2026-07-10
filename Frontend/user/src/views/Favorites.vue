<template>
  <div>
    <div class="page-head pad">
      <h1 class="page-title">收藏夹</h1>
      <p class="page-desc">你收藏过的文章会保存在这里。</p>
    </div>
    <div v-if="loading" class="loading">加载中…</div>
    <template v-else>
      <ArticleCard v-for="a in articleStore.favoriteArticles" :key="a.id" :article="a" />
      <EmptyState
        v-if="articleStore.favoriteArticles.length === 0"
        text="暂无收藏文章"
        :show-action="true"
        action-text="去首页看看"
        @action="$router.push('/home')"
      />
    </template>
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
