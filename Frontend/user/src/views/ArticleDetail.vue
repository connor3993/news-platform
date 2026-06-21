<template>
  <div class="article-detail-page">
    <!-- Back button -->
    <div class="back-bar" @click="goBack">
      <el-icon :size="20"><ArrowLeft /></el-icon>
      <span>返回</span>
    </div>

    <!-- Loading -->
    <div class="loading-container" v-if="loading" style="min-height: 60vh;">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
    </div>

    <!-- Article content -->
    <div class="article-wrap" v-if="!loading && article">
      <div class="article-header">
        <h1 class="article-title">{{ article.title }}</h1>
        <div class="article-meta">
          <span class="meta-category" v-if="article.categoryName">
            {{ article.categoryName }}
          </span>
          <span class="meta-time">{{ formatDate(article.publishTime) }}</span>
          <span class="meta-author" v-if="article.authorName">作者：{{ article.authorName }}</span>
          <span class="meta-views" v-if="article.viewCount">
            <el-icon><View /></el-icon>
            {{ formatNumber(article.viewCount) }} 阅读
          </span>
        </div>
      </div>

      <!-- Cover image -->
      <div class="article-cover" v-if="article.coverUrl">
        <img :src="article.coverUrl" :alt="article.title" />
      </div>

      <!-- Content body -->
      <div class="article-body article-content" v-html="article.content"></div>

      <div class="vote-bar">
        <el-button
          round
          :type="article.userVote === 1 ? 'primary' : 'default'"
          :loading="voteLoading === 'like'"
          @click="handleVote('like')"
        >
          <el-icon><Pointer /></el-icon>
          点赞 {{ formatNumber(article.likeCount || 0) }}
        </el-button>
        <el-button
          round
          :type="article.userVote === -1 ? 'warning' : 'default'"
          :loading="voteLoading === 'dislike'"
          @click="handleVote('dislike')"
        >
          <el-icon><Remove /></el-icon>
          点踩 {{ formatNumber(article.dislikeCount || 0) }}
        </el-button>
      </div>
    </div>

    <!-- Not found -->
    <EmptyState
      v-if="!loading && !article"
      text="文章不存在或已被删除"
      :showAction="true"
      actionText="返回首页"
      @action="$router.push('/home')"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useArticleStore } from '@/stores/article'
import { useUserStore } from '@/stores/user'
import { formatDate, formatNumber } from '@/utils/format'
import EmptyState from '@/components/EmptyState.vue'

const route = useRoute()
const router = useRouter()
const articleStore = useArticleStore()
const userStore = useUserStore()

const loading = ref(true)
const article = ref(null)
const voteLoading = ref('')

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/home')
  }
}

onMounted(async () => {
  const id = route.params.id
  if (!id) {
    loading.value = false
    return
  }
  try {
    article.value = await articleStore.fetchArticleDetail(id)
    // Record read (fire and forget, don't block UI)
    articleStore.recordRead(id).catch(() => {})
  } catch (err) {
    article.value = null
  } finally {
    loading.value = false
  }
})

const handleVote = async (type) => {
  if (!userStore.isLogin) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  voteLoading.value = type
  try {
    article.value = type === 'like'
      ? await articleStore.likeArticle(article.value.id)
      : await articleStore.dislikeArticle(article.value.id)
  } finally {
    voteLoading.value = ''
  }
}
</script>

<style scoped>
.article-detail-page {
  min-height: 100vh;
  background-color: var(--bg-white);
}

/* Back bar */
.back-bar {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 12px 16px;
  cursor: pointer;
  color: var(--text-secondary);
  font-size: var(--font-size-base);
  border-bottom: 1px solid var(--border-color);
  background-color: var(--bg-white);
  position: sticky;
  top: 0;
  z-index: 50;
}

.back-bar:hover {
  color: var(--primary-color);
}

.back-bar:active {
  opacity: 0.7;
}

/* Article wrapper */
.article-wrap {
  max-width: 780px;
  margin: 0 auto;
  padding: 0 16px 40px;
}

.article-header {
  padding: 24px 0 16px;
}

.article-title {
  font-size: 24px;
  font-weight: 700;
  line-height: 1.4;
  color: var(--text-primary);
  margin-bottom: 12px;
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: var(--font-size-sm);
  color: var(--text-tertiary);
  flex-wrap: wrap;
}

.meta-category {
  background-color: var(--primary-light);
  color: var(--primary-color);
  padding: 2px 8px;
  border-radius: 4px;
  font-size: var(--font-size-xs);
}

.meta-views {
  display: flex;
  align-items: center;
  gap: 3px;
}

.meta-author {
  color: var(--text-secondary);
}

.article-cover {
  margin: 16px 0;
  border-radius: var(--radius-md);
  overflow: hidden;
}

.article-cover img {
  width: 100%;
  height: auto;
  display: block;
}

.vote-bar {
  display: flex;
  justify-content: center;
  gap: 14px;
  padding: 28px 0 10px;
}

/* Mobile: full-width cover */
@media (max-width: 767px) {
  .article-wrap {
    padding: 0 12px 32px;
  }

  .article-title {
    font-size: 20px;
  }

  .article-header {
    padding: 16px 0 12px;
  }

  .article-cover {
    margin: 12px -12px;
    border-radius: 0;
  }

  .article-body {
    font-size: var(--font-size-base);
  }

  .vote-bar {
    gap: 10px;
    padding-top: 22px;
  }
}

/* PC: constrained width */
@media (min-width: 768px) {
  .article-wrap {
    padding: 0 24px 60px;
  }

  .article-title {
    font-size: 28px;
  }

  .article-cover {
    max-height: 420px;
  }

  .article-cover img {
    max-height: 420px;
    object-fit: cover;
  }

  .article-body {
    font-size: var(--font-size-md);
  }
}
</style>
