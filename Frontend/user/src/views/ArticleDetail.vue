<template>
  <div>
    <div class="back-bar" @click="goBack"><span>‹ 返回</span></div>

    <div v-if="loading" class="loading" style="min-height: 60vh">加载中…</div>

    <div v-else-if="article" class="detail pad">
      <h1 class="d-title">
        <span v-if="isPaidArticle" class="paid-badge">付费</span>
        <span>{{ cleanTitle }}</span>
      </h1>
      <div class="d-meta">
        <span class="pill" v-if="article.categoryName">{{ article.categoryName }}</span>
        <span v-if="article.authorName">作者：{{ article.authorName }}</span>
        <span>{{ formatDate(article.publishTime) }}</span>
        <span>阅 {{ formatNumber(article.viewCount || 0) }}</span>
        <span v-if="isPaidArticle" class="paid-price">¥ {{ formatAmount(article.price) }}</span>
      </div>

      <div class="d-cover" v-if="article.coverUrl">
        <img :src="article.coverUrl" :alt="cleanTitle" />
      </div>

      <div class="article-content" v-html="article.content"></div>

      <div v-if="isPaidArticle" class="paid-panel">
        <div>
          <div class="paid-panel-title">
            {{ isUnlocked ? '付费内容已解锁' : '继续阅读完整付费内容' }}
          </div>
          <div class="paid-panel-desc">
            {{ isUnlocked ? unlockText : `支付 ¥ ${formatAmount(article.price)} 后解锁完整正文` }}
          </div>
        </div>
        <button
          v-if="showPaywall"
          class="btn btn-primary paid-action"
          :disabled="paywallLoading"
          @click="goPay"
        >
          {{ paywallLoading ? '创建订单中…' : `立即解锁 ¥ ${formatAmount(article.price)}` }}
        </button>
        <span v-else class="paid-status">{{ unlockText }}</span>
      </div>

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
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useArticleStore } from '@/stores/article'
import { useUserStore } from '@/stores/user'
import { usePayStore } from '@/stores/pay'
import { formatDate, formatNumber, formatAmount } from '@/utils/format'
import EmptyState from '@/components/EmptyState.vue'

const route = useRoute()
const router = useRouter()
const articleStore = useArticleStore()
const userStore = useUserStore()
const payStore = usePayStore()

const loading = ref(true)
const article = ref(null)
const voteLoading = ref('')
const paywallLoading = ref(false)

const isPaidArticle = computed(() => Number(article.value?.isPaid) === 1)
const cleanTitle = computed(() => String(article.value?.title || '').replace(/^【付费】\s*/, ''))
const isAuthor = computed(() => {
  if (!article.value || !userStore.userInfo) return false
  return String(userStore.userInfo.id) === String(article.value.authorId)
})
const isUnlocked = computed(() => isPaidArticle.value && (article.value?.purchased === true || isAuthor.value))
const unlockText = computed(() => (isAuthor.value ? '作者本人可查看' : '已解锁'))
const showPaywall = computed(() => {
  if (!article.value) return false
  return isPaidArticle.value && !isUnlocked.value
})

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
async function goPay() {
  if (!requireLogin()) return
  paywallLoading.value = true
  try {
    const data = await payStore.createOrder(article.value.id)
    router.push({ path: '/pay/checkout', query: { orderId: data.orderId, articleId: article.value.id } })
  } catch (e) {
    // message handled by request interceptor
  } finally {
    paywallLoading.value = false
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
    // 已登录但用户资料尚未加载时，补取一次用于判断"作者本人"。
    if (!userStore.userInfo && userStore.isLogin) {
      userStore.fetchUserInfo().catch(() => {})
    }
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
.d-title { font-size: 20px; font-weight: 700; line-height: 1.4; margin-bottom: 10px; display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.d-meta { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; font-size: 11px; color: #999; margin-bottom: 14px; }
.d-cover { margin: 4px 0 16px; }
.d-cover img { width: 100%; }
.vote-bar { display: flex; justify-content: center; gap: 10px; padding: 18px 0 6px; flex-wrap: wrap; }
.comment-form { background: #f6f6f6; border: 1px solid #e2e2e2; padding: 12px; margin-bottom: 14px; }
.comment-actions { display: flex; justify-content: flex-end; margin-top: 10px; }
.paid-panel { margin: 24px 0 8px; }
.paid-action { min-width: 160px; }
@media (max-width: 640px) {
  .paid-action { width: 100%; }
}
</style>
