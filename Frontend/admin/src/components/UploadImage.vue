<template>
  <div class="upload-image">
    <el-upload
      class="image-uploader"
      action="#"
      :show-file-list="false"
      :before-upload="beforeUpload"
      :http-request="handleUpload"
      accept="image/*"
    >
      <img v-if="modelValue" :src="modelValue" class="uploaded-image" />
      <el-icon v-else class="upload-placeholder"><Plus /></el-icon>
    </el-upload>
    <div v-if="modelValue" class="image-actions">
      <el-button type="danger" link size="small" @click="handleRemove">移除图片</el-button>
    </div>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { uploadFile } from '@/api/upload'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue'])

function beforeUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  return true
}

async function handleUpload(options) {
  try {
    const res = await uploadFile(options.file)
    emit('update:modelValue', res.data.url)
    ElMessage.success('上传成功')
  } catch (e) {
    ElMessage.error('上传失败')
  }
}

function handleRemove() {
  emit('update:modelValue', '')
}
</script>

<style scoped>
.upload-image {
  display: inline-block;
}

.image-uploader :deep(.el-upload) {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: border-color 0.2s;
  width: 180px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-uploader :deep(.el-upload:hover) {
  border-color: #409eff;
}

.uploaded-image {
  width: 180px;
  height: 120px;
  object-fit: cover;
  display: block;
}

.upload-placeholder {
  font-size: 28px;
  color: #8c939d;
}

.image-actions {
  text-align: center;
  margin-top: 4px;
}
</style>
