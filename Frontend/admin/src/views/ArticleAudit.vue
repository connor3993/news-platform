<template>
  <div class="page-container">
    <div class="page-header">
      <span class="page-title">文章审核</span>
    </div>

    <!-- Table -->
    <el-table :data="tableData" v-loading="loading" stripe border>
      <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="categoryName" label="分类" width="120" />
      <el-table-column label="来源" width="110" align="center">
        <template #default="{ row }">
          <el-tag :type="row.authorType === 'user' ? 'success' : 'info'" effect="light">
            {{ row.authorType === 'user' ? '用户投稿' : '平台编辑' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="authorName" label="作者" width="120" show-overflow-tooltip />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <ArticleStatusTag :status="row.status" />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="提交时间" width="170" />
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handleView(row)">查看详情</el-button>
          <el-button type="success" link size="small" @click="handleApprove(row)">通过</el-button>
          <el-button type="danger" link size="small" @click="handleReject(row)">驳回</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Pagination -->
    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchList"
        @current-change="fetchList"
      />
    </div>

    <!-- Detail Dialog -->
    <el-dialog v-model="detailVisible" title="文章详情" width="700px" top="5vh">
      <div v-if="currentArticle" class="article-detail">
        <h3>{{ currentArticle.title }}</h3>
        <div class="detail-meta">
          <span>分类：{{ currentArticle.categoryName }}</span>
          <span style="margin-left: 16px;">作者：{{ currentArticle.authorName }}</span>
          <span style="margin-left: 16px;">提交时间：{{ currentArticle.createTime }}</span>
        </div>
        <el-divider />
        <p class="detail-summary" v-if="currentArticle.summary">
          <strong>摘要：</strong>{{ currentArticle.summary }}
        </p>
        <div class="detail-cover" v-if="currentArticle.coverUrl">
          <img :src="currentArticle.coverUrl" alt="封面" />
        </div>
        <div class="detail-content" v-html="currentArticle.content"></div>
      </div>
    </el-dialog>

    <!-- Approve Dialog -->
    <el-dialog v-model="approveVisible" title="审核通过" width="500px">
      <el-form>
        <el-form-item label="审核意见">
          <el-input
            v-model="approveComment"
            type="textarea"
            :rows="3"
            placeholder="请输入审核意见（可选）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveVisible = false">取消</el-button>
        <el-button type="primary" :loading="auditLoading" @click="confirmApprove">确认通过</el-button>
      </template>
    </el-dialog>

    <!-- Reject Dialog -->
    <el-dialog v-model="rejectVisible" title="审核驳回" width="500px">
      <el-form>
        <el-form-item label="驳回原因" required>
          <el-input
            v-model="rejectComment"
            type="textarea"
            :rows="3"
            placeholder="请输入驳回原因（必填）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" :loading="auditLoading" @click="confirmReject">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getArticlePage, getArticleDetail, approveArticle, rejectArticle } from '@/api/article'
import ArticleStatusTag from '@/components/ArticleStatusTag.vue'

const loading = ref(false)
const auditLoading = ref(false)
const tableData = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const detailVisible = ref(false)
const approveVisible = ref(false)
const rejectVisible = ref(false)
const currentArticle = ref(null)
const approveComment = ref('')
const rejectComment = ref('')
const currentRow = ref(null)

async function fetchList() {
  loading.value = true
  try {
    const res = await getArticlePage({
      page: page.value,
      pageSize: pageSize.value,
      status: 1
    })
    const data = res.data
    tableData.value = data.records || data.list || []
    total.value = data.total || 0
  } catch (e) {
    tableData.value = []
  } finally {
    loading.value = false
  }
}

async function handleView(row) {
  try {
    const res = await getArticleDetail(row.id)
    currentArticle.value = res.data
    detailVisible.value = true
  } catch (e) {
    ElMessage.error('获取详情失败')
  }
}

function handleApprove(row) {
  currentRow.value = row
  approveComment.value = ''
  approveVisible.value = true
}

async function confirmApprove() {
  auditLoading.value = true
  try {
    await approveArticle(currentRow.value.id, { auditComment: approveComment.value })
    ElMessage.success('审核通过')
    approveVisible.value = false
    fetchList()
  } catch (e) { /* handled */ } finally {
    auditLoading.value = false
  }
}

function handleReject(row) {
  currentRow.value = row
  rejectComment.value = ''
  rejectVisible.value = true
}

async function confirmReject() {
  if (!rejectComment.value.trim()) {
    ElMessage.warning('请输入驳回原因')
    return
  }
  auditLoading.value = true
  try {
    await rejectArticle(currentRow.value.id, { auditComment: rejectComment.value })
    ElMessage.success('已驳回')
    rejectVisible.value = false
    fetchList()
  } catch (e) { /* handled */ } finally {
    auditLoading.value = false
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.article-detail h3 {
  font-size: 20px;
  margin-bottom: 12px;
}

.detail-meta {
  color: #909399;
  font-size: 13px;
}

.detail-summary {
  color: #606266;
  margin-bottom: 16px;
  line-height: 1.6;
}

.detail-cover img {
  max-width: 100%;
  max-height: 200px;
  border-radius: 4px;
  margin-bottom: 16px;
}

.detail-content {
  line-height: 1.8;
}

.detail-content :deep(img) {
  max-width: 100%;
}
</style>
