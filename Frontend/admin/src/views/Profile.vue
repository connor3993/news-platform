<template>
  <div class="page-container">
    <div class="page-header">
      <span class="page-title">个人中心</span>
    </div>

    <div class="profile-card">
      <el-descriptions :column="1" border size="large" style="max-width: 500px;">
        <el-descriptions-item label="用户名">
          {{ adminStore.adminInfo.username }}
        </el-descriptions-item>
        <el-descriptions-item label="姓名">
          {{ adminStore.adminInfo.name }}
        </el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag type="primary" effect="plain">
            {{ adminStore.adminInfo.role || '管理员' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <div style="margin-top: 32px;">
        <el-button type="danger" @click="handleLogout">退出登录</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useAdminStore } from '@/stores/admin'

const adminStore = useAdminStore()

function handleLogout() {
  adminStore.logout()
}
</script>

<style scoped>
.profile-card {
  padding: 20px 0;
}
</style>
