<template>
  <div class="dashboard">
    <!-- Stat Cards -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6">
        <el-card class="stat-card stat-blue">
          <el-statistic title="文章总数" :value="stats.totalArticles || 0">
            <template #prefix><el-icon><Document /></el-icon></template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card stat-green">
          <el-statistic title="已发布" :value="stats.publishedCount || 0">
            <template #prefix><el-icon><CircleCheck /></el-icon></template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card stat-orange">
          <el-statistic title="评论总数" :value="stats.commentCount || 0">
            <template #prefix><el-icon><ChatDotRound /></el-icon></template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card stat-purple">
          <el-statistic title="总浏览量" :value="stats.totalViews || 0">
            <template #prefix><el-icon><View /></el-icon></template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>

    <!-- Charts -->
    <el-row :gutter="16" class="chart-row">
      <el-col :span="12">
        <el-card>
          <template #header>分类文章数量</template>
          <div ref="categoryChartRef" class="chart-container" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>近7天发布趋势</template>
          <div ref="trendChartRef" class="chart-container" />
        </el-card>
      </el-col>
    </el-row>

    <!-- Recent Articles -->
    <el-card class="recent-card">
      <template #header>最近文章</template>
      <el-table :data="stats.recentArticles || []" stripe>
        <el-table-column prop="title" label="标题">
          <template #default="{ row }">
            <el-link type="primary" :href="`/article/${row.id}`" target="_blank">
              {{ row.title }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="authorName" label="作者" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览量" width="100" />
        <el-table-column prop="createTime" label="发布时间" width="120">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { Document, CircleCheck, ChatDotRound, View } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getDashboard } from '../../api/admin'

const stats = ref({})
const categoryChartRef = ref(null)
const trendChartRef = ref(null)
let categoryChart = null
let trendChart = null

onMounted(async () => {
  await loadDashboard()
})

async function loadDashboard() {
  try {
    const res = await getDashboard()
    stats.value = res.data || {}
    await nextTick()
    renderCharts()
  } catch {
    ElMessage.error('加载统计数据失败')
  }
}

function renderCharts() {
  renderCategoryChart()
  renderTrendChart()
}

function renderCategoryChart() {
  if (!categoryChartRef.value) return
  categoryChart = echarts.init(categoryChartRef.value)
  const data = stats.value.categoryChart || []
  categoryChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: data.map(d => d.name),
      axisLabel: { rotate: 30 }
    },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      type: 'bar',
      data: data.map(d => d.count),
      itemStyle: { color: '#409eff', borderRadius: [4, 4, 0, 0] }
    }],
    grid: { left: 40, right: 20, top: 20, bottom: 60 }
  })
}

function renderTrendChart() {
  if (!trendChartRef.value) return
  trendChart = echarts.init(trendChartRef.value)
  const data = stats.value.trend || []
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: data.map(d => d.date),
      boundaryGap: false
    },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      type: 'line',
      data: data.map(d => d.count),
      smooth: true,
      areaStyle: { color: 'rgba(64,158,255,0.1)' },
      itemStyle: { color: '#409eff' }
    }],
    grid: { left: 40, right: 20, top: 20, bottom: 30 }
  })
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return String(dateStr).slice(0, 10)
}
</script>

<style scoped>
.dashboard {
  width: 100%;
}
.stat-row {
  margin-bottom: 16px;
}
.stat-card {
  position: relative;
  overflow: hidden;
}
.stat-card::after {
  content: '';
  position: absolute;
  right: -10px;
  top: -10px;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  opacity: 0.1;
}
.stat-blue::after { background: #409eff; }
.stat-green::after { background: #67c23a; }
.stat-orange::after { background: #e6a23c; }
.stat-purple::after { background: #9c27b0; }
.chart-row {
  margin-bottom: 16px;
}
.chart-container {
  height: 280px;
  width: 100%;
}
.recent-card {
  margin-bottom: 16px;
}
</style>
