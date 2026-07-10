<template>
  <div class="article-item" @click="go">
    <div class="a-title clamp-2">{{ article.title }}</div>
    <div class="a-summary" v-if="article.summary">{{ article.summary }}</div>
    <div class="a-meta">
      <span class="pill" v-if="article.categoryName">{{ article.categoryName }}</span>
      <span>{{ formatRelativeTime(article.publishTime) }}</span>
      <span class="a-stats">
        阅 {{ formatNumber(article.viewCount || 0) }} · 赞
        {{ formatNumber(article.likeCount || 0) }}
      </span>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { formatRelativeTime, formatNumber } from '@/utils/format'

const props = defineProps({
  article: { type: Object, required: true }
})

const router = useRouter()
const go = () => router.push(`/article/${props.article.id}`)
</script>
