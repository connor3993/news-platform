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

      <!-- 付费开关 -->
      <div class="field paid-toggle-row">
        <label class="paid-toggle">
          <input type="checkbox" v-model="isPaidFlag" />
          <span>设为付费文章</span>
        </label>
        <span class="paid-hint">开启后用户需付费解锁完整正文</span>
      </div>

      <!-- 免费文章：单段正文 -->
      <div v-if="!isPaidFlag" class="field">
        <label>正文</label>
        <textarea v-model="form.content" class="textarea" placeholder="请输入文章正文，支持 HTML 段落"></textarea>
      </div>

      <!-- 付费文章：预览 + 付费内容 -->
      <template v-else>
        <div class="field">
          <label>免费预览（用户未购买前可见）</label>
          <textarea v-model="form.paidPreview" class="textarea" style="min-height: 100px" placeholder="给出文章开头 1~2 段，足够吸引用户付费解锁"></textarea>
        </div>
        <div class="field-row">
          <div class="field">
            <label>价格（元）</label>
            <input v-model.number="form.price" type="number" min="0.01" step="0.01" class="input" placeholder="1.68" />
          </div>
          <div class="field">
            <label>提示</label>
            <div class="paid-tip">付费内容会在用户购买后展示在预览之后</div>
          </div>
        </div>
        <div class="field">
          <label>付费正文（购买后可见）</label>
          <textarea v-model="form.paidOnly" class="textarea" style="min-height: 160px" placeholder="请输入付费内容正文，支持 HTML 段落"></textarea>
        </div>
      </template>

      <button class="btn btn-primary btn-block" :disabled="submitting" @click="submit">提交审核</button>
    </div>

    <div class="section">
      <h2>我的投稿</h2>
      <div v-if="mineLoading" class="loading">加载中…</div>
      <div v-for="m in articleStore.myArticles" :key="m.id" class="mine-item">
        <div class="mine-title clamp-2">
          <span v-if="m.isPaid === 1" class="paid-badge">付费</span>
          {{ m.title }}
        </div>
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
import { reactive, ref, computed, onMounted } from 'vue'
import { useArticleStore } from '@/stores/article'
import { useCategoryStore } from '@/stores/category'
import { toast } from '@/utils/message'
import EmptyState from '@/components/EmptyState.vue'

const articleStore = useArticleStore()
const categoryStore = useCategoryStore()
const submitting = ref(false)
const mineLoading = ref(false)

const form = reactive({
  title: '',
  summary: '',
  categoryId: '',
  coverUrl: '',
  content: '',
  isPaid: 0,
  price: 1.68,
  paidPreview: '',
  paidOnly: ''
})

const isPaidFlag = computed({
  get: () => form.isPaid === 1,
  set: (v) => {
    form.isPaid = v ? 1 : 0
    // 切到付费模式时，把当前正文搬到付费内容
    if (v && form.content && !form.paidOnly) {
      form.paidOnly = form.content
      form.paidPreview = form.paidPreview || (form.content.length > 200 ? form.content.substring(0, 200) + '…' : form.content)
      form.content = ''
    }
    if (!v) {
      // 切回免费模式：付费内容搬回正文
      if (form.paidOnly && !form.content) form.content = form.paidOnly
    }
  }
})

function statusText(s) {
  return (
    { DRAFT: '草稿', PENDING: '待审核', APPROVED: '已通过', REJECTED: '已驳回', PUBLISHED: '已发布', OFFLINE: '已下架' }[s] || '未知'
  )
}
function validate() {
  if (!form.title.trim()) return toast('请输入标题', 'error')
  if (!form.categoryId) return toast('请选择分类', 'error')
  if (form.isPaid === 1) {
    if (!form.paidPreview.trim()) return toast('请填写付费预览内容', 'error')
    if (!form.paidOnly.trim()) return toast('请填写付费正文', 'error')
    if (!form.price || form.price <= 0) return toast('请填写大于 0 的价格', 'error')
  } else if (!form.content.trim()) {
    return toast('请输入正文', 'error')
  }
  return true
}
async function submit() {
  if (!validate()) return
  submitting.value = true
  try {
    const payload = { ...form }
    if (payload.isPaid !== 1) {
      // 免费文章清空付费字段，避免后端误存
      payload.paidPreview = null
      payload.paidOnly = null
      payload.price = 0
    }
    await articleStore.submitUserArticle(payload)
    toast('已提交审核')
    Object.assign(form, {
      title: '', summary: '', categoryId: '', coverUrl: '', content: '',
      isPaid: 0, price: 1.68, paidPreview: '', paidOnly: ''
    })
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
.mine-title { font-size: 14px; font-weight: 700; margin-bottom: 6px; display: flex; align-items: center; gap: 6px; }
.paid-badge { background: #f56c6c; color: #fff; font-size: 10px; padding: 1px 5px; border-radius: 3px; font-weight: 500; }
.mine-meta { display: flex; align-items: center; gap: 8px; font-size: 11px; color: #999; flex-wrap: wrap; }
.paid-toggle-row { display: flex; align-items: center; gap: 12px; padding: 8px 0; }
.paid-toggle { display: flex; align-items: center; gap: 6px; font-weight: 500; cursor: pointer; }
.paid-hint { font-size: 12px; color: #999; }
.paid-tip { padding: 8px 0; font-size: 12px; color: #999; }
@media (max-width: 480px) {
  .field-row { grid-template-columns: 1fr; gap: 0; }
}
</style>
