<template>
  <div class="admin-layout">
    <!-- 侧边栏 -->
    <div class="admin-sidebar">
      <div class="admin-logo">
        <span class="logo-icon">⚙️</span>
        <span>管理后台</span>
      </div>
      <el-menu :default-active="activeMenu" class="admin-menu" @select="handleMenuSelect">
        <el-menu-item index="stats">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据统计</span>
        </el-menu-item>
        <el-menu-item index="users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="products">
          <el-icon><Goods /></el-icon>
          <span>商品管理</span>
        </el-menu-item>
        <el-menu-item index="orders">
          <el-icon><ShoppingCart /></el-icon>
          <span>交易管理</span>
        </el-menu-item>
      </el-menu>
    </div>

    <div class="admin-content">
      <div class="admin-header">
        <h2>{{ pageTitle }}</h2>
        <div class="admin-user">
          <span>欢迎，{{ admin?.username }}</span>
          <el-button size="small" @click="logout">退出</el-button>
        </div>
      </div>

      <!-- 数据统计 -->
      <div v-if="activeMenu === 'stats'" class="admin-panel">
        <div class="stats-grid">
          <div class="stat-card" style="background: linear-gradient(135deg, #667eea, #764ba2);">
            <div class="stat-icon">👤</div>
            <div>
              <div class="stat-num">{{ totalUsers }}</div>
              <div class="stat-label">总用户</div>
            </div>
          </div>
          <div class="stat-card" style="background: linear-gradient(135deg, #42b983, #2d8b5e);">
            <div class="stat-icon">📦</div>
            <div>
              <div class="stat-num">{{ totalProducts }}</div>
              <div class="stat-label">总商品</div>
            </div>
          </div>
          <div class="stat-card" style="background: linear-gradient(135deg, #f5af19, #f12711);">
            <div class="stat-icon">🛒</div>
            <div>
              <div class="stat-num">{{ totalTrades }}</div>
              <div class="stat-label">总交易</div>
            </div>
          </div>
          <div class="stat-card" style="background: linear-gradient(135deg, #11998e, #38ef7d);">
            <div class="stat-icon">📈</div>
            <div>
              <div class="stat-num">{{ recommendRate }}%</div>
              <div class="stat-label">推荐点击率</div>
            </div>
          </div>
        </div>

        <div class="filter-bar">
          <label>时间范围：</label>
          <el-select v-model="dateRange" @change="loadStatsData" size="small">
            <el-option label="最近7天" :value="7" />
            <el-option label="最近30天" :value="30" />
            <el-option label="最近90天" :value="90" />
          </el-select>
        </div>

        <div class="charts-row">
          <div class="chart-box">
            <h4>📈 用户增长趋势</h4>
            <div ref="statsUserChartRef" class="chart"></div>
          </div>
          <div class="chart-box">
            <h4>📊 商品分类占比</h4>
            <div ref="statsCategoryChartRef" class="chart"></div>
          </div>
        </div>
        <div class="charts-row">
          <div class="chart-box">
            <h4>📉 交易趋势</h4>
            <div ref="statsTradeChartRef" class="chart"></div>
          </div>
          <div class="chart-box">
            <h4>🎯 推荐效果</h4>
            <div ref="statsRecommendChartRef" class="chart"></div>
          </div>
        </div>
      </div>

      <!-- 用户管理 -->
      <div v-if="activeMenu === 'users'" class="admin-panel">
        <el-table :data="userList" style="width: 100%;" stripe>
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="username" label="用户名" />
          <el-table-column prop="studentId" label="学号" />
          <el-table-column prop="email" label="邮箱" />
          <el-table-column label="角色" width="80">
            <template #default="{ row }">
              <el-tag :type="row.role === 1 ? 'success' : 'info'">
                {{ row.role === 1 ? '管理员' : '用户' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 0 ? 'success' : 'danger'">
                {{ row.status === 0 ? '正常' : '封禁' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button v-if="row.role !== 1" size="small" :type="row.status === 0 ? 'danger' : 'success'" @click="toggleUser(row)">
                {{ row.status === 0 ? '封禁' : '解封' }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 商品管理 -->
      <div v-if="activeMenu === 'products'" class="admin-panel">
        <el-table :data="allProducts" style="width: 100%;" stripe>
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="title" label="商品名称" />
          <el-table-column label="卖家" width="100">
            <template #default="{ row }">
              {{ getSellerName(row.userId) }}
            </template>
          </el-table-column>
          <el-table-column prop="price" label="价格" width="100">
            <template #default="{ row }">
              ¥{{ row.price }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 0 ? 'success' : 'info'">
                {{ row.status === 0 ? '在售' : '已售' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button v-if="row.status === 0" size="small" type="danger" @click="offShelf(row.id)">下架</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 交易管理 -->
      <div v-if="activeMenu === 'orders'" class="admin-panel">
        <el-table :data="tradeList" style="width: 100%;" stripe>
          <el-table-column prop="id" label="订单ID" width="80" />
          <el-table-column prop="productId" label="商品ID" width="80" />
          <el-table-column label="买家" width="100">
            <template #default="{ row }">
              {{ getUserName(row.buyerId) }}
            </template>
          </el-table-column>
          <el-table-column label="卖家" width="100">
            <template #default="{ row }">
              {{ getUserName(row.sellerId) }}
            </template>
          </el-table-column>
          <el-table-column prop="price" label="价格" width="100">
            <template #default="{ row }">
              ¥{{ row.price }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="['warning', 'success', 'danger', 'info'][row.status]">
                {{ ['待确认', '已确认', '已取消', '已完成'][row.status] }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="时间" width="180" />
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { DataAnalysis, User, Goods, ShoppingCart } from '@element-plus/icons-vue'
import request from '../api/request'

const router = useRouter()
const admin = ref(null)
const activeMenu = ref('stats')
const pageTitle = ref('数据统计')
const dateRange = ref(30)

const totalUsers = ref(0)
const totalProducts = ref(0)
const totalTrades = ref(0)
const recommendRate = ref(0)

const userList = ref([])
const allProducts = ref([])
const tradeList = ref([])
const sellerMap = ref({})
const userMap = ref({})

let statsUserChart = null
let statsCategoryChart = null
let statsTradeChart = null
let statsRecommendChart = null

const statsUserChartRef = ref(null)
const statsCategoryChartRef = ref(null)
const statsTradeChartRef = ref(null)
const statsRecommendChartRef = ref(null)

const handleMenuSelect = (index) => {
  const titles = { stats: '数据统计', users: '用户管理', products: '商品管理', orders: '交易管理' }
  activeMenu.value = index
  pageTitle.value = titles[index] || '数据统计'

  if (index === 'stats') {
    loadStatsData()
    loadOverview()
  } else if (index === 'users') {
    loadUsers()
  } else if (index === 'products') {
    loadAllProducts()
  } else if (index === 'orders') {
    loadTrades()
  }
}

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
    console.error('加载总览失败', error)
  }
}

const loadStatsData = async () => {
  try {
    // 用户增长趋势
    const usersRes = await request.get(`/admin/stats/users?days=${dateRange.value}`)
    const dates = usersRes?.dates || []
    const counts = usersRes?.counts || []
    if (statsUserChart && dates.length > 0) {
      statsUserChart.setOption({
        tooltip: { trigger: 'axis' },
        xAxis: { data: dates },
        yAxis: { type: 'value', name: '人数' },
        series: [{ data: counts, type: 'line', smooth: true, areaStyle: {} }]
      })
    }

    // 商品分类占比
    const catRes = await request.get('/admin/stats/categories')
    const catData = catRes || []
    if (statsCategoryChart && catData.length > 0) {
      statsCategoryChart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {c}件 ({d}%)' },
        series: [{ data: catData, type: 'pie', radius: ['40%', '70%'], label: { formatter: '{b}\n{d}%' } }]
      })
    }

    // 交易趋势
    const tradeRes = await request.get(`/admin/stats/trades?days=${dateRange.value}`)
    const tradeDates = tradeRes?.dates || []
    const tradeCounts = tradeRes?.counts || []
    if (statsTradeChart && tradeDates.length > 0) {
      statsTradeChart.setOption({
        tooltip: { trigger: 'axis' },
        xAxis: { data: tradeDates },
        yAxis: { type: 'value', name: '交易量' },
        series: [{ data: tradeCounts, type: 'bar' }]
      })
    }

    // 推荐效果
    const recommendRes = await request.get('/admin/stats/recommend')
    const rate = recommendRes?.clickRate || 0
    if (statsRecommendChart) {
      statsRecommendChart.setOption({
        series: [{
          type: 'gauge',
          center: ['50%', '50%'],
          radius: '70%',
          progress: { show: true },
          axisLine: { lineStyle: { width: 18, color: [[0.3, '#f56c6c'], [0.7, '#e6a23c'], [1, '#67c23a']] } },
          axisTick: { show: false },
          splitLine: { show: false },
          axisLabel: { show: false },
          pointer: { show: true, length: '60%', width: 8 },
          detail: { valueAnimation: true, fontSize: 24, offsetCenter: [0, 20] },
          title: { show: true, offsetCenter: [0, -10] },
          data: [{ value: rate, name: '点击率' }]
        }]
      })
    }
  } catch (error) {
    console.error('加载统计数据失败', error)
  }
}

const loadUsers = async () => {
  try {
    const res = await request.get('/user/list')
    userList.value = Array.isArray(res) ? res : (res.data || [])
  } catch (error) {
    console.error('加载用户列表失败', error)
  }
}

const toggleUser = async (user) => {
  const newStatus = user.status === 0 ? 1 : 0
  if (confirm(`确定${newStatus === 1 ? '封禁' : '解封'}用户 ${user.username} 吗？`)) {
    await request.put('/user/update', { id: user.id, status: newStatus })
    ElMessage.success('操作成功')
    loadUsers()
  }
}

const loadAllProducts = async () => {
  try {
    const res = await request.get('/product/all')
    allProducts.value = Array.isArray(res) ? res : (res.data || [])
    for (const p of allProducts.value) {
      if (!sellerMap.value[p.userId]) {
        try {
          const userRes = await request.get(`/user/${p.userId}`)
          sellerMap.value[p.userId] = userRes.data?.username || '未知'
        } catch (e) {
          sellerMap.value[p.userId] = '未知'
        }
      }
    }
  } catch (error) {
    console.error('加载商品列表失败', error)
  }
}

const getSellerName = (userId) => sellerMap.value[userId] || '加载中...'

const offShelf = async (productId) => {
  if (confirm('确定下架该商品吗？')) {
    await request.delete(`/product/${productId}?userId=1`)
    ElMessage.success('下架成功')
    loadAllProducts()
  }
}

const loadTrades = async () => {
  try {
    const res = await request.get('/trade/all')
    tradeList.value = Array.isArray(res) ? res : (res.data || [])
    for (const t of tradeList.value) {
      if (!userMap.value[t.buyerId]) {
        const res2 = await request.get(`/user/${t.buyerId}`)
        userMap.value[t.buyerId] = res2.data?.username || '未知'
      }
      if (!userMap.value[t.sellerId]) {
        const res2 = await request.get(`/user/${t.sellerId}`)
        userMap.value[t.sellerId] = res2.data?.username || '未知'
      }
    }
  } catch (error) {
    console.error('加载交易列表失败', error)
  }
}

const getUserName = (userId) => userMap.value[userId] || '加载中...'

const initCharts = () => {
  if (statsUserChartRef.value) {
    if (statsUserChart) statsUserChart.dispose()
    statsUserChart = echarts.init(statsUserChartRef.value)
    statsUserChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category' },
      yAxis: { type: 'value', name: '人数' },
      series: [{ type: 'line', smooth: true, areaStyle: {} }]
    })
  }
  if (statsCategoryChartRef.value) {
    if (statsCategoryChart) statsCategoryChart.dispose()
    statsCategoryChart = echarts.init(statsCategoryChartRef.value)
    statsCategoryChart.setOption({
      tooltip: { trigger: 'item' },
      series: [{ type: 'pie', radius: ['40%', '70%'], label: { formatter: '{b}\n{d}%' } }]
    })
  }
  if (statsTradeChartRef.value) {
    if (statsTradeChart) statsTradeChart.dispose()
    statsTradeChart = echarts.init(statsTradeChartRef.value)
    statsTradeChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category' },
      yAxis: { type: 'value', name: '交易量' },
      series: [{ type: 'bar' }]
    })
  }
  if (statsRecommendChartRef.value) {
    if (statsRecommendChart) statsRecommendChart.dispose()
    statsRecommendChart = echarts.init(statsRecommendChartRef.value)
  }
}

const logout = () => {
  localStorage.removeItem('user')
  router.push('/login')
}

const handleResize = () => {
  if (statsUserChart) statsUserChart.resize()
  if (statsCategoryChart) statsCategoryChart.resize()
  if (statsTradeChart) statsTradeChart.resize()
  if (statsRecommendChart) statsRecommendChart.resize()
}

onMounted(() => {
  const savedUser = localStorage.getItem('user')
  if (!savedUser) {
    router.push('/login')
    return
  }
  admin.value = JSON.parse(savedUser)
  if (admin.value.role !== 1) {
    ElMessage.warning('需要管理员权限')
    router.push('/')
    return
  }
  nextTick(() => {
    initCharts()
    loadOverview()
    loadStatsData()
  })
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (statsUserChart) statsUserChart.dispose()
  if (statsCategoryChart) statsCategoryChart.dispose()
  if (statsTradeChart) statsTradeChart.dispose()
  if (statsRecommendChart) statsRecommendChart.dispose()
})
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: #f0f2f5;
}
.admin-sidebar {
  width: 220px;
  background: #1a1a2e;
  color: white;
  min-height: 100vh;
  padding: 20px 0;
  flex-shrink: 0;
}
.admin-logo {
  font-size: 18px;
  font-weight: bold;
  padding: 0 20px 20px;
  border-bottom: 1px solid rgba(255,255,255,0.1);
  display: flex;
  align-items: center;
  gap: 10px;
}
.admin-logo .logo-icon {
  font-size: 24px;
}
.admin-menu {
  background: transparent;
  border: none;
  padding: 10px 0;
}
.admin-menu .el-menu-item {
  color: rgba(255,255,255,0.7);
  border-radius: 8px;
  margin: 4px 12px;
  padding: 0 16px;
  height: 44px;
}
.admin-menu .el-menu-item:hover {
  background: rgba(255,255,255,0.1);
  color: white;
}
.admin-menu .el-menu-item.is-active {
  background: #42b983;
  color: white;
}
.admin-menu .el-menu-item .el-icon {
  margin-right: 10px;
}
.admin-content {
  flex: 1;
  padding: 20px;
  min-width: 0;
}
.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
  padding: 16px 24px;
  border-radius: 12px;
  margin-bottom: 20px;
}
.admin-header h2 {
  margin: 0;
  font-size: 18px;
}
.admin-user {
  display: flex;
  align-items: center;
  gap: 16px;
}
.admin-panel {
  background: white;
  border-radius: 12px;
  padding: 20px;
}
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}
.stat-card {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px;
  border-radius: 12px;
  color: white;
}
.stat-card .stat-icon {
  font-size: 36px;
}
.stat-card .stat-num {
  font-size: 28px;
  font-weight: bold;
}
.stat-card .stat-label {
  font-size: 13px;
  opacity: 0.8;
}
.filter-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 8px;
}
.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}
.chart-box {
  background: #fafafa;
  border-radius: 8px;
  padding: 16px;
}
.chart-box h4 {
  margin: 0 0 12px;
  font-size: 14px;
  color: #333;
}
.chart {
  width: 100%;
  height: 280px;
}
</style>