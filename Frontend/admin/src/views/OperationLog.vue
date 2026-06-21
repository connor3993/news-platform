<template>
  <div class="page-container">
    <div class="page-header">
      <span class="page-title">操作日志</span>
    </div>

    <!-- Filter -->
    <div class="filter-area">
      <el-input
        v-model="filter.operation"
        placeholder="操作名称"
        clearable
        style="width: 200px;"
        @clear="handleSearch"
      />
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
      <el-table-column prop="operator" label="操作人" width="120" />
      <el-table-column prop="operation" label="操作类型" width="150" />
      <el-table-column prop="requestPath" label="请求路径" min-width="200" show-overflow-tooltip />
      <el-table-column prop="requestMethod" label="请求方法" width="100" align="center" />
      <el-table-column prop="ip" label="IP地址" width="140" />
      <el-table-column prop="createTime" label="操作时间" width="170" />
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
import { getLogPage } from '@/api/log'

const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dateRange = ref(null)

const filter = reactive({
  operation: ''
})

async function fetchList() {
  loading.value = true
  try {
    const params = {
      page: page.value,
      pageSize: pageSize.value,
      operation: filter.operation || undefined,
      beginTime: dateRange.value ? dateRange.value[0] : undefined,
      endTime: dateRange.value ? dateRange.value[1] : undefined
    }
    const res = await getLogPage(params)
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
  filter.operation = ''
  dateRange.value = null
  page.value = 1
  fetchList()
}

onMounted(() => {
  fetchList()
})
</script>
