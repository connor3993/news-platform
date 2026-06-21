<template>
  <div class="article-card" @click="handleClick">
    <div class="card-inner">
      <div class="card-text">
        <h3 class="card-title text-ellipsis-2">{{ article.title }}</h3>
        <p class="card-summary text-ellipsis-2" v-if="article.summary">
          {{ article.summary }}
        </p>
        <div class="card-meta">
          <span class="meta-category" v-if="article.categoryName">
            {{ article.categoryName }}
          </span>
          <span class="meta-time">{{ formatRelativeTime(article.publishTime) }}</span>
          <span class="meta-views" v-if="article.viewCount">
            <el-icon><View /></el-icon>
            {{ formatNumber(article.viewCount) }}
          </span>
          <span class="meta-hot" v-if="article.hotScore">
            <el-icon><TrendCharts /></el-icon>
            {{ formatNumber(article.hotScore) }}
          </span>
        </div>
      </div>
      <div class="card-cover">
        <img
          v-if="article.coverUrl && !imageError"
          :src="article.coverUrl"
          :alt="article.title"
          loading="lazy"
          @error="imageError = true"
        />
        <div v-else class="cover-placeholder">
          <span>{{ article.categoryName || '资讯' }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { formatRelativeTime, formatNumber } from '@/utils/format'

const props = defineProps({
  article: {
    type: Object,
    required: true
  }
})

const router = useRouter()
const imageError = ref(false)

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
  box-shadow: var(--shadow-sm);
  overflow: hidden;
  cursor: pointer;
  border: 1px solid rgba(220, 227, 236, 0.8);
  transition: box-shadow 0.2s ease, transform 0.15s ease, border-color 0.2s ease;
}

.article-card:hover {
  box-shadow: var(--shadow-md);
  border-color: rgba(22, 119, 255, 0.18);
}

.article-card:active {
  transform: scale(0.99);
}

.card-inner {
  display: flex;
  padding: 14px;
  gap: 12px;
}

.card-text {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.card-title {
  font-size: var(--font-size-md);
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.5;
  margin-bottom: 8px;
}

.card-summary {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  line-height: 1.5;
  margin-bottom: 8px;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
  flex-wrap: wrap;
}

.meta-category {
  background-color: var(--primary-light);
  color: var(--primary-color);
  padding: 2px 8px;
  border-radius: 6px;
  font-size: var(--font-size-xs);
}

.meta-views,
.meta-hot {
  display: flex;
  align-items: center;
  gap: 2px;
}

.card-cover {
  width: 120px;
  height: 80px;
  flex-shrink: 0;
  border-radius: var(--radius-sm);
  overflow: hidden;
}

.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #eef6ff 0%, #f8fbff 100%);
  color: var(--primary-color);
  font-size: var(--font-size-sm);
  font-weight: 600;
}

/* Mobile: smaller cover, more compact */
@media (max-width: 767px) {
  .card-inner {
    padding: 12px;
    gap: 10px;
  }

  .card-title {
    font-size: var(--font-size-base);
  }

  .card-cover {
    width: 100px;
    height: 70px;
  }

  .card-summary {
    font-size: var(--font-size-xs);
  }
}

/* PC: larger cover and more spacing */
@media (min-width: 768px) {
  .card-inner {
    padding: 20px;
    gap: 20px;
  }

  .card-title {
    font-size: var(--font-size-lg);
  }

  .card-cover {
    width: 180px;
    height: 118px;
  }

  .card-summary {
    font-size: var(--font-size-base);
  }

  .article-card:hover .card-title {
    color: var(--primary-color);
  }
}
</style>
