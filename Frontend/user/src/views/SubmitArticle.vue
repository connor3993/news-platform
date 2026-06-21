<template>
  <div class="submit-page page">
    <div class="submit-container container">
      <section class="submit-panel">
        <div class="panel-head">
          <div>
            <h1>发布投稿</h1>
            <p>提交后会进入管理员审核，通过并发布后展示在用户端资讯列表。</p>
          </div>
          <el-button text @click="resetForm">
            <el-icon><Refresh /></el-icon>
            清空
          </el-button>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
          <el-form-item label="标题" prop="title">
            <el-input v-model="form.title" maxlength="100" show-word-limit placeholder="请输入文章标题" />
          </el-form-item>
          <el-form-item label="摘要" prop="summary">
            <el-input
              v-model="form.summary"
              type="textarea"
              :rows="3"
              maxlength="220"
              show-word-limit
              placeholder="用一两句话说明文章重点"
            />
          </el-form-item>
          <div class="form-grid">
            <el-form-item label="分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%;">
                <el-option
                  v-for="item in categoryStore.categoryList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="封面地址">
              <el-input v-model="form.coverUrl" placeholder="可选，填写图片 URL" clearable />
            </el-form-item>
          </div>
          <el-form-item label="正文" prop="content">
            <el-input
              v-model="form.content"
              type="textarea"
              :rows="10"
              placeholder="请输入文章正文，支持直接填写 HTML 段落"
            />
          </el-form-item>
          <div class="submit-actions">
            <el-button type="primary" :loading="submitting" @click="handleSubmit">
              <el-icon><Promotion /></el-icon>
              提交审核
            </el-button>
          </div>
        </el-form>
      </section>

      <section class="mine-panel">
        <div class="panel-head compact">
          <div>
            <h2>我的投稿</h2>
            <p>审核、发布、驳回等状态会在这里更新。</p>
          </div>
          <el-button text @click="fetchMine">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
        <el-table :data="articleStore.myArticles" v-loading="mineLoading">
          <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
          <el-table-column prop="categoryName" label="分类" width="90" />
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="statusType(row.status)" effect="light">{{ statusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="updateTime" label="更新时间" min-width="150" />
        </el-table>
      </section>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useArticleStore } from '@/stores/article'
import { useCategoryStore } from '@/stores/category'

const articleStore = useArticleStore()
const categoryStore = useCategoryStore()
const formRef = ref(null)
const submitting = ref(false)
const mineLoading = ref(false)

const emptyForm = () => ({
  title: '',
  summary: '',
  categoryId: '',
  coverUrl: '',
  content: ''
})

const form = reactive(emptyForm())

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  content: [{ required: true, message: '请输入正文', trigger: 'blur' }]
}

const resetForm = () => {
  Object.assign(form, emptyForm())
  formRef.value?.clearValidate()
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await articleStore.submitUserArticle({ ...form })
    ElMessage.success('已提交审核')
    resetForm()
    fetchMine()
  } finally {
    submitting.value = false
  }
}

const fetchMine = async () => {
  mineLoading.value = true
  try {
    await articleStore.fetchMyArticles({ page: 1, pageSize: 10 })
  } finally {
    mineLoading.value = false
  }
}

const statusText = (status) => {
  const map = {
    0: '草稿',
    1: '待审核',
    2: '已通过',
    3: '已驳回',
    4: '已发布',
    5: '已下架'
  }
  return map[status] || '未知'
}

const statusType = (status) => {
  const map = {
    1: 'warning',
    2: 'success',
    3: 'danger',
    4: 'primary',
    5: 'info'
  }
  return map[status] || 'info'
}

onMounted(async () => {
  await categoryStore.fetchCategories()
  fetchMine()
})
</script>

<style scoped>
.submit-page {
  padding: 24px 0 48px;
}

.submit-container {
  max-width: 980px;
  display: grid;
  gap: 20px;
}

.submit-panel,
.mine-panel {
  background: var(--bg-white);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 24px;
}

.panel-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 22px;
}

.panel-head h1,
.panel-head h2 {
  font-size: var(--font-size-xl);
  line-height: 1.3;
  color: var(--text-primary);
}

.panel-head p {
  margin-top: 6px;
  color: var(--text-tertiary);
  font-size: var(--font-size-sm);
}

.panel-head.compact {
  margin-bottom: 14px;
}

.panel-head.compact h2 {
  font-size: var(--font-size-lg);
}

.form-grid {
  display: grid;
  grid-template-columns: 220px 1fr;
  gap: 16px;
}

.submit-actions {
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 767px) {
  .submit-page {
    padding: 12px 0 32px;
  }

  .submit-panel,
  .mine-panel {
    padding: 16px;
    border-radius: var(--radius-md);
  }

  .panel-head {
    align-items: center;
  }

  .form-grid {
    grid-template-columns: 1fr;
    gap: 0;
  }

  .submit-actions .el-button {
    width: 100%;
  }
}
</style>
