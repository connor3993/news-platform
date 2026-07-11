<template>
  <div class="article-item" @click="go">
    <div class="a-title-row">
      <span v-if="isPaidArticle" class="paid-badge">付费</span>
      <span class="a-title clamp-2">{{ cleanTitle }}</span>
    </div>
    <div class="a-summary" v-if="article.summary">{{ article.summary }}</div>
    <div class="a-meta">
      <span class="pill" v-if="article.categoryName">{{ article.categoryName }}</span>
      <span v-if="isPaidArticle" class="paid-price">¥ {{ formatAmount(article.price) }}</span>
      <span>{{ formatRelativeTime(article.publishTime) }}</span>
      <span class="a-stats">
        阅 {{ formatNumber(article.viewCount || 0) }} · 赞
        {{ formatNumber(article.likeCount || 0) }}
      </span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { formatRelativeTime, formatNumber, formatAmount } from '@/utils/format'

const props = defineProps({
  article: { type: Object, required: true }
})

const router = useRouter()
const isPaidArticle = computed(() => Number(props.article.isPaid) === 1)
const cleanTitle = computed(() => String(props.article.title || '').replace(/^【付费】\s*/, ''))
const go = () => router.push(`/article/${props.article.id}`)
</script>
