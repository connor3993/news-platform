<template>
  <div class="page-container">
    <div class="page-header">
      <span class="page-title">文章列表</span>
      <el-button type="primary" @click="$router.push('/article/add')">
        <el-icon><Plus /></el-icon>新建文章
      </el-button>
    </div>

    <!-- Filter -->
    <div class="filter-area">
      <el-input
        v-model="filter.title"
        placeholder="文章标题"
        clearable
        style="width: 200px;"
        @clear="handleSearch"
      />
      <el-select
        v-model="filter.categoryId"
        placeholder="选择分类"
        clearable
        style="width: 150px;"
        @change="handleSearch"
      >
        <el-option
          v-for="cat in dictStore.categoryList"
          :key="cat.id"
          :label="cat.name"
          :value="cat.id"
        />
      </el-select>
      <el-select
        v-model="filter.status"
        placeholder="文章状态"
        clearable
        style="width: 150px;"
        @change="handleSearch"
      >
        <el-option
          v-for="(item, key) in articleStatusMap"
          :key="key"
          :label="item.label"
          :value="Number(key)"
        />
      </el-select>
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="YYYY-MM-DD"
        style="width: 260px;"
        @change="handleSearch"
      />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
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
      <el-table-column prop="publishTime" label="发布时间" width="170" />
      <el-table-column prop="viewCount" label="浏览量" width="90" align="right" />
      <el-table-column prop="likeCount" label="点赞" width="80" align="right" />
      <el-table-column prop="hotScore" label="热度分" width="90" align="right" />
      <el-table-column label="操作" width="300" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="[0, 1, 2, 3, 4, 5].includes(row.status)"
            type="primary"
            link
            size="small"
            @click="handleEdit(row)"
          >
            编辑
          </el-button>
          <el-button
            v-if="row.status === 0"
            type="success"
            link
            size="small"
            @click="handleSubmit(row)"
          >
            提交审核
          </el-button>
          <el-button
            v-if="row.status === 2"
            type="warning"
            link
            size="small"
            @click="handlePublish(row)"
          >
            发布
          </el-button>
          <el-button
            v-if="row.status === 4"
            type="danger"
            link
            size="small"
            @click="handleOffline(row)"
          >
            下架
          </el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getArticlePage, submitArticle, publishArticle, offlineArticle, deleteArticle } from '@/api/article'
import { useDictStore } from '@/stores/dict'
import { articleStatusMap } from '@/utils/format'
import ArticleStatusTag from '@/components/ArticleStatusTag.vue'

const router = useRouter()
const dictStore = useDictStore()

const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dateRange = ref(null)

const filter = reactive({
  title: '',
  categoryId: '',
  status: ''
})

async function fetchList() {
  loading.value = true
  try {
    const params = {
      page: page.value,
      pageSize: pageSize.value,
      title: filter.title || undefined,
      categoryId: filter.categoryId || undefined,
      status: filter.status !== '' ? filter.status : undefined,
      beginTime: dateRange.value ? dateRange.value[0] : undefined,
      endTime: dateRange.value ? dateRange.value[1] : undefined
    }
    const res = await getArticlePage(params)
    const data = res.data
    tableData.value = data.records || data.list || []
    total.value = data.total || 0
  } catch (e) {
    tableData.value = []
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  fetchList()
}

function handleReset() {
  filter.title = ''
  filter.categoryId = ''
  filter.status = ''
  dateRange.value = null
  page.value = 1
  fetchList()
}

function handleEdit(row) {
  router.push(`/article/edit/${row.id}`)
}

async function handleSubmit(row) {
  try {
    await ElMessageBox.confirm('确定提交审核吗？', '提示', { type: 'warning' })
    await submitArticle(row.id)
    ElMessage.success('提交成功')
    fetchList()
  } catch (e) { /* cancel */ }
}

async function handlePublish(row) {
  try {
    await ElMessageBox.confirm('确定发布该文章吗？', '提示', { type: 'warning' })
    await publishArticle(row.id)
    ElMessage.success('发布成功')
    fetchList()
  } catch (e) { /* cancel */ }
}

async function handleOffline(row) {
  try {
    await ElMessageBox.confirm('确定下架该文章吗？', '提示', { type: 'warning' })
    await offlineArticle(row.id)
    ElMessage.success('下架成功')
    fetchList()
  } catch (e) { /* cancel */ }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除《${row.title}》吗？`, '提示', { type: 'warning' })
    await deleteArticle(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch (e) { /* cancel */ }
}

onMounted(() => {
  fetchList()
})
</script>
