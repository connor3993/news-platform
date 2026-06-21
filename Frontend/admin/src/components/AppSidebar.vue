<template>
  <el-menu
    :default-active="activeMenu"
    router
    class="app-sidebar"
    background-color="#304156"
    text-color="#bfcbd9"
    active-text-color="#409eff"
  >
    <el-menu-item index="/dashboard">
      <el-icon><DataAnalysis /></el-icon>
      <span>工作台</span>
    </el-menu-item>
    <el-sub-menu index="article">
      <template #title>
        <el-icon><Document /></el-icon>
        <span>文章管理</span>
      </template>
      <el-menu-item index="/article/list">文章列表</el-menu-item>
      <el-menu-item index="/article/add">新建文章</el-menu-item>
      <el-menu-item index="/article/audit">文章审核</el-menu-item>
    </el-sub-menu>
    <el-menu-item index="/category">
      <el-icon><Menu /></el-icon>
      <span>分类管理</span>
    </el-menu-item>
    <el-menu-item index="/log">
      <el-icon><Notebook /></el-icon>
      <span>操作日志</span>
    </el-menu-item>
  </el-menu>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const activeMenu = computed(() => {
  return route.path
})
</script>

<style scoped>
.app-sidebar {
  height: 100%;
  border-right: none;
}

.app-sidebar .el-menu-item,
.app-sidebar .el-sub-menu__title {
  height: 50px;
  line-height: 50px;
}
</style>
