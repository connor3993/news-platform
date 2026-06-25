<template>
  <div class="article-card" :class="{ 'featured': featured }" @click="handleClick">
    <div class="card-cover" v-if="showCover">
      <img
        :src="article.coverUrl"
        :alt="article.title"
        loading="lazy"
        @error="imageError = true"
      />
    </div>
    <div v-else class="card-cover card-cover-placeholder">
      <div class="placeholder-pattern" :style="placeholderStyle"></div>
    </div>
    <div class="card-body">
      <h3 class="card-title text-ellipsis-2">{{ article.title }}</h3>
      <p class="card-summary text-ellipsis-2" v-if="article.summary">
        {{ article.summary }}
      </p>
      <div class="card-meta">
        <span class="meta-category" v-if="article.categoryName">
          {{ article.categoryName }}
        </span>
        <span class="meta-time">{{ formatRelativeTime(article.publishTime) }}</span>
        <div class="meta-stats">
          <span class="meta-stat" v-if="article.viewCount != null">
            <Eye :size="12" />
            {{ formatNumber(article.viewCount || 0) }}
          </span>
          <span class="meta-stat" v-if="article.likeCount != null">
            <ThumbsUp :size="12" />
            {{ formatNumber(article.likeCount || 0) }}
          </span>
          <span class="meta-stat">
            <MessageCircle :size="12" />
            {{ formatNumber(article.commentCount || 0) }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Eye, ThumbsUp, MessageCircle } from 'lucide-vue-next'
import { formatRelativeTime, formatNumber } from '@/utils/format'

const props = defineProps({
  article: {
    type: Object,
    required: true
  },
  featured: {
    type: Boolean,
    default: false
  }
})

const router = useRouter()
const imageError = ref(false)

const showCover = computed(() => {
  return props.article.coverUrl && !imageError.value
})

const placeholderStyle = computed(() => {
  const colors = [
    'linear-gradient(135deg, #e0f2fe 0%, #bae6fd 100%)',
    'linear-gradient(135deg, #f3e8ff 0%, #e9d5ff 100%)',
    'linear-gradient(135deg, #dcfce7 0%, #bbf7d0 100%)',
    'linear-gradient(135deg, #ffedd5 0%, #fed7aa 100%)',
    'linear-gradient(135deg, #f1f5f9 0%, #cbd5e1 100%)'
  ]
  const index = (props.article.id || 0) % colors.length
  return { background: colors[index] }
})

watch(
  () => props.article.coverUrl,
  () => {
    imageError.value = false
  }
)

const handleClick = () => {
  router.push(`/article/${props.article.id}`)
}
</script>

<style scoped>
.article-card {
  background-color: var(--bg-card);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow 0.25s ease, transform 0.2s ease, border-color 0.25s ease;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.article-card:hover {
  box-shadow: var(--shadow-md);
  border-color: var(--border-strong);
  transform: translateY(-2px);
}

.article-card:active {
  transform: translateY(0) scale(0.995);
}

.card-cover {
  position: relative;
  width: 100%;
  aspect-ratio: 16 / 10;
  overflow: hidden;
  background: linear-gradient(135deg, #f1f5f9 0%, #e2e8f0 100%);
  flex-shrink: 0;
}

.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.article-card:hover .card-cover img {
  transform: scale(1.05);
}

.card-cover-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
}

.placeholder-pattern {
  width: 100%;
  height: 100%;
  opacity: 0.6;
}

.card-body {
  flex: 1;
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.card-title {
  font-size: var(--font-size-base);
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.5;
  transition: color 0.2s ease;
}

.article-card:hover .card-title {
  color: var(--primary-color);
}

.card-summary {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  line-height: 1.55;
  display: none;
}

.card-meta {
  margin-top: auto;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
  flex-wrap: wrap;
  row-gap: 6px;
}

.meta-category {
  background-color: var(--bg-subtle);
  color: var(--text-secondary);
  padding: 3px 8px;
  border-radius: 6px;
  font-size: var(--font-size-xs);
  font-weight: 500;
}

.meta-time {
  white-space: nowrap;
}

.meta-stats {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
}

.meta-stat {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  color: var(--text-tertiary);
}

/* Featured card: spans 2 columns on desktop */
@media (min-width: 1024px) {
  .article-card.featured {
    grid-column: span 2;
    flex-direction: row;
  }

  .article-card.featured .card-cover {
    width: 55%;
    aspect-ratio: auto;
    height: 100%;
  }

  .article-card.featured .card-body {
    width: 45%;
    padding: 22px;
    justify-content: center;
  }

  .article-card.featured .card-title {
    font-size: var(--font-size-xl);
  }

  .article-card.featured .card-summary {
    display: -webkit-box;
  }
}

/* Tablet */
@media (min-width: 768px) {
  .card-body {
    padding: 16px;
  }

  .card-title {
    font-size: var(--font-size-md);
  }

  .card-summary {
    display: -webkit-box;
  }
}

/* Mobile */
@media (max-width: 767px) {
  .card-body {
    padding: 12px;
  }

  .card-title {
    font-size: var(--font-size-base);
  }

  .meta-stats {
    margin-left: 0;
    width: 100%;
  }
}
</style>
