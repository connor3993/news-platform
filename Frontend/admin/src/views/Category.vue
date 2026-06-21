<template>
  <div class="page-container">
    <div class="page-header">
      <span class="page-title">分类管理</span>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>新建分类
      </el-button>
    </div>

    <!-- Filter -->
    <div class="filter-area">
      <el-input
        v-model="filter.name"
        placeholder="分类名称"
        clearable
        style="width: 200px;"
        @clear="handleSearch"
      />
      <el-select
        v-model="filter.status"
        placeholder="状态"
        clearable
        style="width: 120px;"
        @change="handleSearch"
      >
        <el-option label="启用" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <!-- Table -->
    <el-table :data="tableData" v-loading="loading" stripe border>
      <el-table-column prop="name" label="分类名称" min-width="200" />
      <el-table-column prop="sort" label="排序" width="100" align="center" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-switch
            :model-value="row.status === 1"
            @change="handleStatusChange(row)"
            active-text="启用"
            inactive-text="禁用"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
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

    <!-- Add/Edit Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEditDialog ? '编辑分类' : '新建分类'"
      width="500px"
      @close="resetForm"
    >
      <el-form ref="dialogFormRef" :model="dialogForm" :rules="dialogRules" label-width="80px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="dialogForm.name" placeholder="请输入分类名称" maxlength="20" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="dialogForm.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="dialogForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="dialogLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCategoryPage, addCategory, updateCategory, deleteCategory, updateCategoryStatus } from '@/api/category'
import { useDictStore } from '@/stores/dict'

const dictStore = useDictStore()

const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const filter = reactive({
  name: '',
  status: ''
})

const dialogVisible = ref(false)
const dialogLoading = ref(false)
const dialogFormRef = ref(null)
const isEditDialog = ref(false)

const dialogForm = reactive({
  id: null,
  name: '',
  sort: 0,
  status: 1
})

const dialogRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

async function fetchList() {
  loading.value = true
  try {
    const params = {
      page: page.value,
      pageSize: pageSize.value,
      name: filter.name || undefined,
      status: filter.status !== '' ? filter.status : undefined
    }
    const res = await getCategoryPage(params)
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
  filter.name = ''
  filter.status = ''
  page.value = 1
  fetchList()
}

function handleAdd() {
  isEditDialog.value = false
  dialogForm.id = null
  dialogForm.name = ''
  dialogForm.sort = 0
  dialogForm.status = 1
  dialogVisible.value = true
}

function handleEdit(row) {
  isEditDialog.value = true
  dialogForm.id = row.id
  dialogForm.name = row.name
  dialogForm.sort = row.sort || 0
  dialogForm.status = row.status
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await dialogFormRef.value.validate().catch(() => false)
  if (!valid) return

  dialogLoading.value = true
  try {
    if (isEditDialog.value) {
      await updateCategory(dialogForm)
    } else {
      await addCategory(dialogForm)
    }
    ElMessage.success(isEditDialog.value ? '编辑成功' : '新建成功')
    dialogVisible.value = false
    fetchList()
    dictStore.fetchCategories()
  } catch (e) { /* handled */ } finally {
    dialogLoading.value = false
  }
}

function resetForm() {
  dialogFormRef.value?.resetFields()
}

async function handleStatusChange(row) {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await updateCategoryStatus(row.id, newStatus)
    ElMessage.success('状态更新成功')
    fetchList()
    dictStore.fetchCategories()
  } catch (e) { /* handled */ }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除分类「${row.name}」吗？`, '提示', {
      type: 'warning'
    })
    await deleteCategory(row.id)
    ElMessage.success('删除成功')
    fetchList()
    dictStore.fetchCategories()
  } catch (e) { /* cancel */ }
}

onMounted(() => {
  fetchList()
})
</script>
