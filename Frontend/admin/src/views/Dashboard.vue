<template>
  <div class="dashboard-page">
    <!-- Stat Cards -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background-color: #e6f7ff;">
            <el-icon :size="28" color="#1890ff"><Document /></el-icon>
          </div>
          <div class="stat-value">{{ todayStats.publishCount || 0 }}</div>
          <div class="stat-label">今日发布</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background-color: #f6ffed;">
            <el-icon :size="28" color="#52c41a"><View /></el-icon>
          </div>
          <div class="stat-value">{{ todayStats.viewCount || 0 }}</div>
          <div class="stat-label">今日浏览</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background-color: #fff7e6;">
            <el-icon :size="28" color="#faad14"><Checked /></el-icon>
          </div>
          <div class="stat-value">{{ todayStats.auditCount || 0 }}</div>
          <div class="stat-label">今日审核</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background-color: #fff1f0;">
            <el-icon :size="28" color="#f5222d"><CloseBold /></el-icon>
          </div>
          <div class="stat-value">{{ todayStats.rejectCount || 0 }}</div>
          <div class="stat-label">今日驳回</div>
        </div>
      </el-col>
    </el-row>

    <!-- Charts -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-title">阅读趋势（近7日）</div>
          <div ref="readChartRef" style="height: 300px;"></div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-title">发布趋势（近7日）</div>
          <div ref="publishChartRef" style="height: 300px;"></div>
        </div>
      </el-col>
    </el-row>

    <!-- Hot Articles -->
    <div class="chart-card" style="margin-top: 20px;">
      <div class="chart-title">热门文章 Top10</div>
      <el-table :data="hotArticles" stripe size="small" v-loading="hotLoading">
        <el-table-column type="index" label="排名" width="70" align="center" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="viewCount" label="浏览量" width="100" align="right" />
        <el-table-column prop="hotScore" label="热度分" width="100" align="right" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getTodayStats, getReadStatistics, getPublishStatistics, getHotArticles } from '@/api/dashboard'
import { getLastNDays } from '@/utils/format'

const todayStats = ref({})
const hotArticles = ref([])
const hotLoading = ref(false)
const readChartRef = ref(null)
const publishChartRef = ref(null)
let readChart = null
let publishChart = null

async function fetchTodayStats() {
  try {
    const res = await getTodayStats()
    todayStats.value = res.data || {}
  } catch (e) { /* ignore */ }
}

async function fetchReadChart() {
  const days = getLastNDays(7)
  try {
    const res = await getReadStatistics({ begin: days[0], end: days[days.length - 1] })
    const data = res.data || {}
    const dates = data.dates || days
    const values = data.values || days.map(() => 0)

    await nextTick()
    readChart = echarts.init(readChartRef.value)
    readChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 50, right: 20, top: 20, bottom: 30 },
      xAxis: { type: 'category', data: dates, boundaryGap: false },
      yAxis: { type: 'value' },
      series: [{
        name: '阅读量',
        type: 'line',
        data: values,
        smooth: true,
        areaStyle: { opacity: 0.15 },
        lineStyle: { width: 2 },
        itemStyle: { color: '#409eff' }
      }]
    })
  } catch (e) { /* ignore */ }
}

async function fetchPublishChart() {
  const days = getLastNDays(7)
  try {
    const res = await getPublishStatistics({ begin: days[0], end: days[days.length - 1] })
    const data = res.data || {}
    const dates = data.dates || days
    const values = data.values || days.map(() => 0)

    await nextTick()
    publishChart = echarts.init(publishChartRef.value)
    publishChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 50, right: 20, top: 20, bottom: 30 },
      xAxis: { type: 'category', data: dates },
      yAxis: { type: 'value' },
      series: [{
        name: '发布量',
        type: 'bar',
        data: values,
        barWidth: 30,
        itemStyle: { color: '#67c23a', borderRadius: [4, 4, 0, 0] }
      }]
    })
  } catch (e) { /* ignore */ }
}

async function fetchHotArticles() {
  hotLoading.value = true
  try {
    const res = await getHotArticles()
    hotArticles.value = res.data || []
  } catch (e) {
    hotArticles.value = []
  } finally {
    hotLoading.value = false
  }
}

function handleResize() {
  readChart?.resize()
  publishChart?.resize()
}

onMounted(() => {
  fetchTodayStats()
  fetchReadChart()
  fetchPublishChart()
  fetchHotArticles()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  readChart?.dispose()
  publishChart?.dispose()
})
</script>

<style scoped>
.dashboard-page {
  padding: 0;
}

.stat-row .stat-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
}
</style>
