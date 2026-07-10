<template>
  <div>
    <div class="back-bar" @click="goBack"><span>‹ 返回</span></div>

    <div v-if="loading" class="loading" style="min-height: 60vh">加载中…</div>

    <div v-else-if="article" class="detail pad">
      <h1 class="d-title">{{ article.title }}</h1>
      <div class="d-meta">
        <span class="pill" v-if="article.categoryName">{{ article.categoryName }}</span>
        <span v-if="article.authorName">作者：{{ article.authorName }}</span>
        <span>{{ formatDate(article.publishTime) }}</span>
        <span>阅 {{ formatNumber(article.viewCount || 0) }}</span>
      </div>

      <div class="d-cover" v-if="article.coverUrl">
        <img :src="article.coverUrl" :alt="article.title" />
      </div>

      <div class="article-content" v-html="article.content"></div>

      <div class="vote-bar">
        <button
          class="btn btn-sm"
          :class="{ 'btn-primary': article.userVote === 1 }"
          :disabled="voteLoading === 'like'"
          @click="vote('like')"
        >
          赞 {{ formatNumber(article.likeCount || 0) }}
        </button>
        <button
          class="btn btn-sm"
          :class="{ 'btn-primary': article.userVote === -1 }"
          :disabled="voteLoading === 'dislike'"
          @click="vote('dislike')"
        >
          踩 {{ formatNumber(article.dislikeCount || 0) }}
        </button>
        <button
          class="btn btn-sm"
          :class="{ 'btn-primary': article.favorited }"
          :disabled="voteLoading === 'favorite'"
          @click="fav"
        >
          ★ {{ formatNumber(article.favoriteCount || 0) }}
        </button>
      </div>
    </div>

    <EmptyState
      v-else
      text="文章不存在或已被删除"
      :show-action="true"
      action-text="返回首页"
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

function goBack() {
  if (window.history.length > 1) router.back()
  else router.push('/home')
}
function requireLogin() {
  if (!userStore.isLogin) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return false
  }
  return true
}
async function vote(type) {
  if (!requireLogin()) return
  voteLoading.value = type
  try {
    article.value =
      type === 'like'
        ? await articleStore.likeArticle(article.value.id)
        : await articleStore.dislikeArticle(article.value.id)
  } finally {
    voteLoading.value = ''
  }
}
async function fav() {
  if (!requireLogin()) return
  voteLoading.value = 'favorite'
  try {
    article.value = await articleStore.favoriteArticle(article.value.id)
  } finally {
    voteLoading.value = ''
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
    articleStore.recordRead(id).catch(() => {})
  } catch (e) {
    article.value = null
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.back-bar {
  position: sticky; top: 52px; z-index: 30; background: #fff;
  border-bottom: 1px solid #000; padding: 12px 14px; font-size: 14px; cursor: pointer;
}
.back-bar:hover { background: #f4f4f4; }
.d-title { font-size: 20px; font-weight: 700; line-height: 1.4; margin-bottom: 10px; }
.d-meta { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; font-size: 11px; color: #999; margin-bottom: 14px; }
.d-cover { margin: 4px 0 16px; }
.d-cover img { width: 100%; }
.vote-bar { display: flex; justify-content: center; gap: 10px; padding: 18px 0 6px; flex-wrap: wrap; }
.comment-form { background: #f6f6f6; border: 1px solid #e2e2e2; padding: 12px; margin-bottom: 14px; }
.comment-actions { display: flex; justify-content: flex-end; margin-top: 10px; }
</style>
