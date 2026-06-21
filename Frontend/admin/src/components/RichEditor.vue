<template>
  <div class="rich-editor">
    <div style="border: 1px solid #ccc">
      <Toolbar
        :editor="editorRef"
        :defaultConfig="toolbarConfig"
        :mode="'simple'"
        style="border-bottom: 1px solid #ccc"
      />
      <Editor
        :defaultConfig="editorConfig"
        :modelValue="modelValue"
        :mode="'simple'"
        style="height: 400px; overflow-y: hidden"
        @onCreated="handleCreated"
        @onChange="handleChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, shallowRef, onBeforeUnmount } from 'vue'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import '@wangeditor/editor/dist/css/style.css'
import { uploadFile } from '@/api/upload'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue'])

const editorRef = shallowRef(null)

const toolbarConfig = {}

const editorConfig = {
  placeholder: '请输入文章内容...',
  MENU_CONF: {
    uploadImage: {
      async customUpload(file, insertFn) {
        try {
          const res = await uploadFile(file)
          insertFn(res.data.url, file.name, '')
        } catch (e) {
          console.error('Upload image failed:', e)
        }
      }
    }
  }
}

function handleCreated(editor) {
  editorRef.value = editor
}

function handleChange(editor) {
  emit('update:modelValue', editor.getHtml())
}

onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor) {
    editor.destroy()
  }
})
</script>

<style scoped>
.rich-editor {
  line-height: normal;
}
</style>
