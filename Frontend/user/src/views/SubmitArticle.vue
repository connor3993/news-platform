<template>
  <div class="pad-lg">
    <h1 class="page-title">发布投稿</h1>
    <p class="page-desc">提交后进入管理员审核，通过并发布后展示在资讯列表。</p>

    <div class="panel">
      <div class="field">
        <label>标题</label>
        <input v-model="form.title" class="input" maxlength="100" placeholder="请输入文章标题" />
      </div>
      <div class="field">
        <label>摘要</label>
        <textarea v-model="form.summary" class="textarea" style="min-height: 64px" maxlength="220" placeholder="用一两句话说明文章重点"></textarea>
      </div>
      <div class="field-row">
        <div class="field">
          <label>分类</label>
          <select v-model="form.categoryId" class="select">
            <option value="">请选择分类</option>
            <option v-for="c in categoryStore.categoryList" :key="c.id" :value="c.id">{{ c.name }}</option>
          </select>
        </div>
        <div class="field">
          <label>封面地址（可选）</label>
          <input v-model="form.coverUrl" class="input" placeholder="填写图片 URL" />
        </div>
      </div>
      <div class="field">
        <label>正文</label>
        <textarea v-model="form.content" class="textarea" placeholder="请输入文章正文，支持 HTML 段落"></textarea>
      </div>
      <button class="btn btn-primary btn-block" :disabled="submitting" @click="submit">提交审核</button>
    </div>

    <div class="section">
      <h2>我的投稿</h2>
      <div v-if="mineLoading" class="loading">加载中…</div>
      <div v-for="m in articleStore.myArticles" :key="m.id" class="mine-item">
        <div class="mine-title clamp-2">{{ m.title }}</div>
        <div class="mine-meta">
          <span v-if="m.categoryName">{{ m.categoryName }}</span>
          <span class="status">{{ statusText(m.status) }}</span>
          <span class="muted">{{ m.updateTime }}</span>
        </div>
      </div>
      <EmptyState v-if="!mineLoading && articleStore.myArticles.length === 0" text="还没有投稿" />
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useArticleStore } from '@/stores/article'
import { useCategoryStore } from '@/stores/category'
import { toast } from '@/utils/message'
import EmptyState from '@/components/EmptyState.vue'

const articleStore = useArticleStore()
const categoryStore = useCategoryStore()
const submitting = ref(false)
const mineLoading = ref(false)

const form = reactive({ title: '', summary: '', categoryId: '', coverUrl: '', content: '' })

function statusText(s) {
  return (
    { 0: '草稿', 1: '待审核', 2: '已通过', 3: '已驳回', 4: '已发布', 5: '已下架' }[s] || '未知'
  )
}
function validate() {
  if (!form.title.trim()) return toast('请输入标题', 'error')
  if (!form.categoryId) return toast('请选择分类', 'error')
  if (!form.content.trim()) return toast('请输入正文', 'error')
  return true
}
async function submit() {
  if (!validate()) return
  submitting.value = true
  try {
    await articleStore.submitUserArticle({ ...form })
    toast('已提交审核')
    form.title = ''
    form.summary = ''
    form.categoryId = ''
    form.coverUrl = ''
    form.content = ''
    fetchMine()
  } finally {
    submitting.value = false
  }
}
async function fetchMine() {
  mineLoading.value = true
  try {
    await articleStore.fetchMyArticles({ page: 1, pageSize: 10 })
  } finally {
    mineLoading.value = false
  }
}
onMounted(async () => {
  await categoryStore.fetchCategories()
  fetchMine()
})
</script>

<style scoped>
.panel { border: 1px solid #000; padding: 16px; background: #fff; }
.field-row { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.mine-item { padding: 12px 0; border-bottom: 1px solid #ececec; }
.mine-title { font-size: 14px; font-weight: 700; margin-bottom: 6px; }
.mine-meta { display: flex; align-items: center; gap: 8px; font-size: 11px; color: #999; flex-wrap: wrap; }
@media (max-width: 480px) {
  .field-row { grid-template-columns: 1fr; gap: 0; }
}
</style>
