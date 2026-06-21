<template>
  <div class="hot-page page">
    <div class="hot-header">
      <h2 class="hot-title">
        <el-icon :size="24" color="var(--hot-red)"><TrendCharts /></el-icon>
        热门榜单
      </h2>
      <p class="hot-desc">实时热门新闻 Top 10</p>
    </div>

    <!-- Loading -->
    <div class="loading-container" v-if="loading" style="min-height: 40vh;">
      <el-icon class="is-loading" :size="28"><Loading /></el-icon>
      <span style="margin-left: 8px; color: var(--text-tertiary);">加载中...</span>
    </div>

    <!-- Hot list -->
    <div class="hot-list container" v-if="!loading">
      <div
        v-for="(article, index) in articleStore.hotList"
        :key="article.id"
        class="hot-item"
        :class="{ 'top-three': index < 3 }"
        @click="router.push(`/article/${article.id}`)"
      >
        <div class="rank-badge" :class="`rank-${index + 1}`">
          {{ index + 1 }}
        </div>
        <div class="item-content">
          <h3 class="item-title text-ellipsis-2">{{ article.title }}</h3>
          <div class="item-meta">
            <span class="meta-views" v-if="article.viewCount">
              <el-icon><View /></el-icon>
              {{ formatNumber(article.viewCount) }} 阅读
            </span>
            <span class="meta-hot" v-if="article.hotScore">
              <el-icon><TrendCharts /></el-icon>
              热度 {{ formatNumber(article.hotScore) }}
            </span>
            <span class="meta-category" v-if="article.categoryName">
              {{ article.categoryName }}
            </span>
          </div>
        </div>
        <div class="item-cover">
          <img
            v-if="article.coverUrl && !imageErrors[article.id]"
            :src="article.coverUrl"
            :alt="article.title"
            loading="lazy"
            @error="imageErrors[article.id] = true"
          />
          <div v-else class="cover-placeholder">
            {{ article.categoryName || '资讯' }}
          </div>
        </div>
      </div>

      <!-- Empty -->
      <EmptyState
        v-if="articleStore.hotList.length === 0"
        text="暂无热门文章"
        :showAction="true"
        actionText="刷新"
        @action="loadHotArticles"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useArticleStore } from '@/stores/article'
import { formatNumber } from '@/utils/format'
import EmptyState from '@/components/EmptyState.vue'

defineOptions({ name: 'Hot' })

const router = useRouter()
const articleStore = useArticleStore()
const loading = ref(true)
const imageErrors = ref({})

const loadHotArticles = async () => {
  loading.value = true
  try {
    await articleStore.fetchHotArticles()
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadHotArticles()
})
</script>

<style scoped>
.hot-page {
  padding-bottom: 20px;
}

.hot-header {
  background-color: var(--bg-white);
  padding: 20px 16px;
  border-bottom: 1px solid var(--border-color);
}

.hot-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: var(--font-size-xl);
  font-weight: 700;
  color: var(--text-primary);
}

.hot-desc {
  font-size: var(--font-size-sm);
  color: var(--text-tertiary);
  margin-top: 4px;
  margin-left: 32px;
}

.hot-list {
  padding-top: 12px;
}

.hot-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  background-color: var(--bg-card);
  border-radius: var(--radius-md);
  margin-bottom: 8px;
  cursor: pointer;
  transition: box-shadow 0.2s ease, transform 0.15s ease;
  box-shadow: var(--shadow-sm);
}

.hot-item:hover {
  box-shadow: var(--shadow-md);
}

.hot-item:active {
  transform: scale(0.99);
}

.hot-item.top-three {
  background: linear-gradient(135deg, #fff5f5 0%, #ffffff 100%);
}

/* Rank badge */
.rank-badge {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-size-sm);
  font-weight: 700;
  flex-shrink: 0;
  background-color: var(--border-color);
  color: var(--text-tertiary);
}

.rank-1 {
  background: linear-gradient(135deg, #ff6b6b, #ee5a24);
  color: white;
  font-size: var(--font-size-base);
  width: 32px;
  height: 32px;
}

.rank-2 {
  background: linear-gradient(135deg, #ff9f43, #ee8c1b);
  color: white;
  width: 30px;
  height: 30px;
}

.rank-3 {
  background: linear-gradient(135deg, #feca57, #f0b429);
  color: white;
}

.item-content {
  flex: 1;
  min-width: 0;
}

.item-title {
  font-size: var(--font-size-base);
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.5;
  margin-bottom: 6px;
}

.item-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
  flex-wrap: wrap;
}

.meta-views,
.meta-hot {
  display: flex;
  align-items: center;
  gap: 2px;
}

.meta-category {
  background-color: var(--primary-light);
  color: var(--primary-color);
  padding: 1px 6px;
  border-radius: 3px;
}

.item-cover {
  width: 80px;
  height: 56px;
  border-radius: var(--radius-sm);
  overflow: hidden;
  flex-shrink: 0;
}

.item-cover img {
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
  background: linear-gradient(135deg, #fff3f0 0%, #fffaf8 100%);
  color: var(--hot-red);
  font-size: var(--font-size-xs);
  font-weight: 600;
}

/* Mobile: more compact */
@media (max-width: 767px) {
  .hot-header {
    padding: 16px 12px;
  }

  .hot-title {
    font-size: var(--font-size-lg);
  }

  .hot-item {
    padding: 12px;
    gap: 10px;
    margin-bottom: 6px;
  }

  .item-title {
    font-size: var(--font-size-sm);
  }

  .item-cover {
    width: 70px;
    height: 48px;
  }
}

/* PC: wider cards */
@media (min-width: 768px) {
  .hot-item {
    padding: 16px 20px;
    gap: 16px;
    margin-bottom: 10px;
  }

  .item-title {
    font-size: var(--font-size-md);
  }

  .item-cover {
    width: 120px;
    height: 80px;
  }

  .hot-item:hover .item-title {
    color: var(--primary-color);
  }
}
</style>
