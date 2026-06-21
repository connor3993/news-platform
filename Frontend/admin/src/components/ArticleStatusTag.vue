<template>
  <el-tag :type="statusType" size="small" effect="plain">
    {{ statusLabel }}
  </el-tag>
</template>

<script setup>
import { computed } from 'vue'
import { getArticleStatusLabel, getArticleStatusType } from '@/utils/format'

const props = defineProps({
  status: {
    type: Number,
    required: true
  }
})

const statusLabel = computed(() => getArticleStatusLabel(props.status))
const statusType = computed(() => getArticleStatusType(props.status))
</script>
