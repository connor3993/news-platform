<template>
  <div class="page-container">
    <div class="page-header">
      <span class="page-title">{{ isEdit ? '编辑文章' : '新建文章' }}</span>
      <el-button @click="$router.back()">返回</el-button>
    </div>

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      style="max-width: 900px;"
    >
      <el-form-item label="文章标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入文章标题" maxlength="100" show-word-limit />
      </el-form-item>

      <el-form-item label="文章摘要" prop="summary">
        <el-input
          v-model="form.summary"
          type="textarea"
          :rows="3"
          placeholder="请输入文章摘要"
          maxlength="300"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="文章分类" prop="categoryId">
        <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 300px;">
          <el-option
            v-for="cat in dictStore.categoryList"
            :key="cat.id"
            :label="cat.name"
            :value="cat.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="封面图片" prop="coverUrl">
        <UploadImage v-model="form.coverUrl" />
      </el-form-item>

      <el-form-item label="文章内容" prop="content">
        <RichEditor v-model="form.content" />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" :loading="saving" @click="handleSave(false)">保存草稿</el-button>
        <el-button type="success" :loading="submitting" @click="handleSave(true)">保存并提交审核</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getArticleDetail, addArticle, updateArticle, submitArticle } from '@/api/article'
import { useDictStore } from '@/stores/dict'
import UploadImage from '@/components/UploadImage.vue'
import RichEditor from '@/components/RichEditor.vue'

const route = useRoute()
const router = useRouter()
const dictStore = useDictStore()

const formRef = ref(null)
const saving = ref(false)
const submitting = ref(false)

const isEdit = computed(() => !!route.params.id)

const form = reactive({
  id: null,
  title: '',
  summary: '',
  categoryId: '',
  coverUrl: '',
  content: ''
})

const rules = {
  title: [{ required: true, message: '请输入文章标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  content: [{ required: true, message: '请输入文章内容', trigger: 'blur' }]
}

async function fetchDetail() {
  if (!route.params.id) return
  try {
    const res = await getArticleDetail(route.params.id)
    const data = res.data
    Object.assign(form, {
      id: data.id,
      title: data.title,
      summary: data.summary || '',
      categoryId: data.categoryId,
      coverUrl: data.coverUrl || '',
      content: data.content || ''
    })
  } catch (e) {
    ElMessage.error('获取文章详情失败')
  }
}

async function handleSave(isSubmit) {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  if (isSubmit) {
    submitting.value = true
  } else {
    saving.value = true
  }

  try {
    const payload = {
      title: form.title,
      summary: form.summary,
      categoryId: form.categoryId,
      coverUrl: form.coverUrl,
      content: form.content
    }

    let articleId = form.id
    if (isEdit.value) {
      payload.id = form.id
      await updateArticle(payload)
    } else {
      const res = await addArticle(payload)
      articleId = res.data?.id || res.data
    }

    ElMessage.success(isSubmit ? '提交成功' : '保存成功')

    if (isSubmit && articleId) {
      await submitArticle(articleId)
      ElMessage.success('已提交审核')
    }

    router.push('/article/list')
  } catch (e) {
    // handled in interceptor
  } finally {
    saving.value = false
    submitting.value = false
  }
}

onMounted(() => {
  fetchDetail()
})
</script>
