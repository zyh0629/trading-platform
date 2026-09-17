<template>
  <div class="statistics-container">
    <h1>📊 数据统计看板</h1>

    <!-- 筛选条件 -->
    <div class="filter-bar">
      <label>时间范围：</label>
      <el-select v-model="dateRange" @change="loadAllData" size="large">
        <el-option label="最近7天" :value="7" />
        <el-option label="最近30天" :value="30" />
        <el-option label="最近90天" :value="90" />
      </el-select>
    </div>

    <!-- 指标卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon">👤</div>
        <div class="stat-info">
          <h3>总用户数</h3>
          <p class="stat-number">{{ totalUsers }}</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">📦</div>
        <div class="stat-info">
          <h3>总商品数</h3>
          <p class="stat-number">{{ totalProducts }}</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">🛒</div>
        <div class="stat-info">
          <h3>总交易量</h3>
          <p class="stat-number">{{ totalTrades }}</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">📈</div>
        <div class="stat-info">
          <h3>推荐点击率</h3>
          <p class="stat-number">{{ recommendRate }}%</p>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-row">
      <div class="chart-box">
        <h3>📈 用户增长趋势</h3>
        <div ref="userChartRef" class="chart"></div>
      </div>
      <div class="chart-box">
        <h3>📊 商品分类占比</h3>
        <div ref="categoryChartRef" class="chart"></div>
      </div>
    </div>

    <div class="charts-row">
      <div class="chart-box">
        <h3>📉 交易趋势</h3>
        <div ref="tradeChartRef" class="chart"></div>
      </div>
      <div class="chart-box">
        <h3>🎯 推荐效果</h3>
        <div ref="recommendChartRef" class="chart"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import request from '../api/request'

const dateRange = ref(30)
const totalUsers = ref(0)
const totalProducts = ref(0)
const totalTrades = ref(0)
const recommendRate = ref(0)

// 图表实例
let userChart = null
let categoryChart = null
let tradeChart = null
let recommendChart = null

// 图表容器
const userChartRef = ref(null)
const categoryChartRef = ref(null)
const tradeChartRef = ref(null)
const recommendChartRef = ref(null)

// 加载所有数据
const loadAllData = async () => {
  await loadOverview()
  await loadUserTrend()
  await loadCategoryStats()
  await loadTradeTrend()
  await loadRecommendStats()
}

// 加载总览统计
const loadOverview = async () => {
  try {
    const res = await request.get('/admin/stats/overview')
    if (res) {
      totalUsers.value = res.totalUsers || 0
      totalProducts.value = res.totalProducts || 0
      totalTrades.value = res.totalTrades || 0
      recommendRate.value = res.recommendRate || 0
    }
  } catch (error) {
    console.error('加载总览统计失败', error)
  }
}

// 加载用户增长趋势
const loadUserTrend = async () => {
  try {
    const res = await request.get(`/admin/stats/users?days=${dateRange.value}`)
    const dates = res?.dates || []
    const counts = res?.counts || []

    if (userChart && dates.length > 0) {
      userChart.setOption({
        tooltip: { trigger: 'axis' },
        xAxis: { data: dates, name: '日期' },
        yAxis: { type: 'value', name: '人数' },
        series: [{
          data: counts,
          name: '新增用户',
          type: 'line',
          smooth: true,
          areaStyle: { color: 'rgba(102, 126, 234, 0.3)' },
          lineStyle: { color: '#667eea', width: 3 },
          itemStyle: { color: '#667eea' }
        }]
      })
    }
  } catch (error) {
    console.error('加载用户趋势失败', error)
  }
}

// 加载分类统计
const loadCategoryStats = async () => {
  try {
    const res = await request.get('/admin/stats/categories')
    const data = res || []

    if (categoryChart && data.length > 0) {
      const colors = ['#42b983', '#667eea', '#f5af19', '#f56c6c']
      categoryChart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {c}件 ({d}%)' },
        legend: { orient: 'vertical', left: 'left' },
        series: [{
          data: data.map((item, index) => ({ ...item, itemStyle: { color: colors[index % colors.length] } })),
          type: 'pie',
          radius: ['40%', '70%'],
          label: { show: true, formatter: '{b}\n{d}%' }
        }]
      })
    }
  } catch (error) {
    console.error('加载分类统计失败', error)
  }
}

// 加载交易趋势
const loadTradeTrend = async () => {
  try {
    const res = await request.get(`/admin/stats/trades?days=${dateRange.value}`)
    const dates = res?.dates || []
    const counts = res?.counts || []

    if (tradeChart && dates.length > 0) {
      tradeChart.setOption({
        tooltip: { trigger: 'axis' },
        xAxis: { data: dates, name: '日期' },
        yAxis: { type: 'value', name: '交易量' },
        series: [{
          data: counts,
          name: '交易量',
          type: 'bar',
          barWidth: '40%',
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#42b983' },
              { offset: 1, color: '#2d8b5e' }
            ])
          }
        }]
      })
    }
  } catch (error) {
    console.error('加载交易趋势失败', error)
  }
}

// 加载推荐统计
const loadRecommendStats = async () => {
  try {
    const res = await request.get('/admin/stats/recommend')
    const rate = res?.clickRate || 0
    recommendRate.value = rate

    if (recommendChart) {
      recommendChart.setOption({
        series: [{
          type: 'gauge',
          center: ['50%', '50%'],
          radius: '70%',
          startAngle: 220,
          endAngle: -40,
          min: 0,
          max: 100,
          progress: { show: true, width: 18 },
          axisLine: {
            lineStyle: {
              width: 18,
              color: [
                [0.3, '#f56c6c'],
                [0.7, '#e6a23c'],
                [1, '#67c23a']
              ]
            }
          },
          axisTick: { show: false },
          splitLine: { show: false },
          axisLabel: { show: true, fontSize: 12, color: '#999' },
          pointer: { show: true, length: '60%', width: 6 },
          detail: {
            valueAnimation: true,
            fontSize: 28,
            fontWeight: 'bold',
            color: '#333',
            offsetCenter: [0, 20]
          },
          title: { show: true, offsetCenter: [0, -20], fontSize: 14, color: '#999' },
          data: [{ value: rate, name: '点击率' }]
        }]
      })
    }
  } catch (error) {
    console.error('加载推荐统计失败', error)
  }
}

// 初始化图表
const initCharts = () => {
  if (userChartRef.value) {
    userChart = echarts.init(userChartRef.value)
  }
  if (categoryChartRef.value) {
    categoryChart = echarts.init(categoryChartRef.value)
  }
  if (tradeChartRef.value) {
    tradeChart = echarts.init(tradeChartRef.value)
  }
  if (recommendChartRef.value) {
    recommendChart = echarts.init(recommendChartRef.value)
  }
}

// 窗口变化自适应
const handleResize = () => {
  [userChart, categoryChart, tradeChart, recommendChart].forEach(chart => {
    if (chart) chart.resize()
  })
}

onMounted(() => {
  initCharts()
  loadAllData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  [userChart, categoryChart, tradeChart, recommendChart].forEach(chart => {
    if (chart) chart.dispose()
  })
})
</script>

<style scoped>
.statistics-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}
.statistics-container h1 {
  margin-bottom: 24px;
  font-size: 24px;
}
.filter-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  padding: 16px 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}
.filter-bar label {
  font-weight: 500;
  color: #333;
}
.filter-bar .el-select {
  width: 150px;
}
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}
.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}
.stat-icon {
  font-size: 32px;
}
.stat-info h3 {
  margin: 0 0 4px;
  font-size: 13px;
  color: #999;
  font-weight: 400;
}
.stat-number {
  margin: 0;
  font-size: 24px;
  font-weight: bold;
  color: #333;
}
.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}
.chart-box {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}
.chart-box h3 {
  margin: 0 0 16px;
  font-size: 16px;
  color: #333;
}
.chart {
  width: 100%;
  height: 320px;
}
</style>