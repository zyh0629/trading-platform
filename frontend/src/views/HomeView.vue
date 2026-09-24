<template>
  <div class="home">
    <!-- 顶部导航栏 -->
    <div class="header">
      <div class="logo" @click="$router.push('/')">
        <span class="logo-icon">📚</span>
        <span class="logo-text">校园二手交易平台</span>
      </div>
      <div class="nav-right">
        <el-badge :value="unreadCount" :hidden="unreadCount === 0">
          <el-icon class="msg-icon" @click="goToChatList"><Message /></el-icon>
        </el-badge>
        <el-dropdown @command="handleMenu">
          <span class="user-info">
            <el-avatar :size="32" :icon="UserFilled" />
            <span class="username">{{ user?.username }}</span>
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人中心</el-dropdown-item>
              <el-dropdown-item command="publish">发布商品</el-dropdown-item>
              <el-dropdown-item command="products">我的商品</el-dropdown-item>
              <el-dropdown-item command="favorites">我的收藏</el-dropdown-item>
              <el-dropdown-item command="orders">我的订单</el-dropdown-item>
              <el-dropdown-item v-if="user?.role === 1" command="admin">管理后台</el-dropdown-item>
              <el-dropdown-item v-if="user?.role === 1" command="stats">数据看板</el-dropdown-item>
              <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <div class="main-layout">
      <!-- 侧边栏 -->
      <div class="sidebar">
        <div class="user-card">
          <el-avatar :size="60" :icon="UserFilled" />
          <h3>{{ user?.username }}</h3>
          <p>{{ user?.studentId || '未绑定学号' }}</p>
        </div>

        <el-menu :default-active="activeMenu" class="sidebar-menu" @select="handleMenuSelect">
          <el-menu-item index="home">
            <el-icon><House /></el-icon>
            <span>首页</span>
          </el-menu-item>
          <el-menu-item index="assistant">
            <el-icon><ChatDotRound /></el-icon>
            <span>学习搭子</span>
          </el-menu-item>
          <el-menu-item index="recommend">
            <el-icon><MagicStick /></el-icon>
            <span>智能推荐</span>
          </el-menu-item>
          <el-menu-item index="publish">
            <el-icon><Edit /></el-icon>
            <span>发布商品</span>
          </el-menu-item>
          <el-menu-item index="products">
            <el-icon><Goods /></el-icon>
            <span>我的商品</span>
          </el-menu-item>
          <el-menu-item index="favorites">
            <el-icon><Star /></el-icon>
            <span>我的收藏</span>
          </el-menu-item>
          <el-menu-item index="orders">
            <el-icon><ShoppingCart /></el-icon>
            <span>我的订单</span>
          </el-menu-item>
          <el-menu-item v-if="user?.role === 1" index="admin">
            <el-icon><Setting /></el-icon>
            <span>管理后台</span>
          </el-menu-item>
          <el-menu-item v-if="user?.role === 1" index="stats">
            <el-icon><DataAnalysis /></el-icon>
            <span>数据看板</span>
          </el-menu-item>
          <el-menu-item index="profile">
            <el-icon><User /></el-icon>
            <span>个人资料</span>
          </el-menu-item>
        </el-menu>
      </div>

      <!-- 内容区域 -->
      <div class="content">
        <!-- 推荐横幅 -->
        <div class="banner" v-if="user">
          <div class="banner-content">
            <div class="banner-text">
              <span class="banner-tag">🎯 AI 智能推荐</span>
              <h2>为你推荐</h2>
              <p>基于你的浏览和收藏记录，智能推荐你可能喜欢的商品</p>
            </div>
            <div class="banner-stats">
              <div class="stat-item">
                <span class="stat-num">{{ recommendList.length }}</span>
                <span class="stat-label">今日推荐</span>
              </div>
              <div class="stat-divider"></div>
              <div class="stat-item">
                <span class="stat-num">{{ productList.length }}</span>
                <span class="stat-label">在售商品</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 分类筛选 -->
        <div class="category-filter">
          <el-tabs v-model="activeCategory" @tab-click="handleCategoryChange">
            <el-tab-pane label="全部" name="all"></el-tab-pane>
            <el-tab-pane label="📚 教材教辅" name="1"></el-tab-pane>
            <el-tab-pane label="📱 电子产品" name="2"></el-tab-pane>
            <el-tab-pane label="🏠 生活用品" name="3"></el-tab-pane>
            <el-tab-pane label="⚽ 体育器材" name="4"></el-tab-pane>
          </el-tabs>
        </div>

        <!-- 猜你喜欢 -->
        <div class="section" v-if="user">
          <div class="section-header">
            <div class="section-left">
              <span class="section-icon">✨</span>
              <h2 class="section-title">猜你喜欢</h2>
              <span class="section-badge">协同过滤算法</span>
            </div>
            <span class="section-more" @click="goToRecommend">查看全部 →</span>
          </div>
          <div class="recommend-grid" v-loading="recommendLoading">
            <div v-for="item in recommendList.slice(0, 6)" :key="item.id" class="recommend-card" @click="goToDetail(item.id)">
              <div class="recommend-image">
                <img :src="item.images || 'https://picsum.photos/400/300?random=' + item.id" alt="商品图片" />
                <div class="recommend-tag">🔥 推荐</div>
              </div>
              <div class="recommend-info">
                <h3>{{ item.title }}</h3>
                <p>{{ item.description }}</p>
                <div class="recommend-bottom">
                  <span class="price">¥{{ item.price }}</span>
                  <span class="like-count">❤️ {{ item.favoriteCount || 0 }}</span>
                </div>
              </div>
            </div>
            <div v-if="recommendList.length === 0" class="empty-state">
              <el-empty description="暂无推荐，去浏览更多商品吧">
                <el-button type="primary" @click="loadRecommend">刷新推荐</el-button>
              </el-empty>
            </div>
          </div>
        </div>

        <!-- 热门商品 -->
        <div class="section">
          <div class="section-header">
            <div class="section-left">
              <span class="section-icon">🔥</span>
              <h2 class="section-title">热门商品</h2>
              <span class="section-badge">大家都在看</span>
            </div>
            <span class="section-more">共 {{ productList.length }} 件</span>
          </div>
          <div class="product-grid" v-loading="productLoading">
            <div v-for="item in productList" :key="item.id" class="product-card" @click="goToDetail(item.id)">
              <img :src="item.images || 'https://picsum.photos/300/200?random=' + item.id" class="product-image" />
              <div class="product-info">
                <h3>{{ item.title }}</h3>
                <p>{{ item.description }}</p>
                <div class="product-bottom">
                  <span class="price">¥{{ item.price }}</span>
                  <span class="views">👁 {{ item.views || 0 }}</span>
                </div>
              </div>
            </div>
            <div v-if="productList.length === 0" class="empty-state">
              <el-empty description="暂无商品，快来发布吧">
                <el-button type="primary" @click="goToPublish">发布商品</el-button>
              </el-empty>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Message, UserFilled, ArrowDown, House, Edit, Goods,
  Star, ChatDotRound, Setting, DataAnalysis, User, MagicStick,
  ShoppingCart
} from '@element-plus/icons-vue'
import { getProductList, getRecommendList, getProductsByCategory } from '../api/product'
import request from '../api/request'

const router = useRouter()
const route = useRoute()
const user = ref(null)
const unreadCount = ref(0)
const activeCategory = ref('all')
const productList = ref([])
const recommendList = ref([])
const productLoading = ref(false)
const recommendLoading = ref(false)
const activeMenu = ref('home')

// 防抖相关
let loadTimer = null
const isLoading = ref(false)

const loadUnreadCount = async () => {
  if (user.value) {
    try {
      const res = await request.get(`/message/unread/${user.value.id}`)
      unreadCount.value = res.count || 0
      console.log('未读消息数:', unreadCount.value)
    } catch (error) {
      console.error('获取未读消息数失败', error)
      unreadCount.value = 0
    }
  }
}

const loadProducts = async () => {
  if (isLoading.value) return
  isLoading.value = true
  productLoading.value = true

  try {
    let res
    if (activeCategory.value === 'all') {
      res = await getProductList()
    } else {
      res = await getProductsByCategory(activeCategory.value)
    }
    productList.value = Array.isArray(res) ? res : (res.data || [])
    console.log('当前分类:', activeCategory.value, '商品数量:', productList.value.length)
  } catch (error) {
    console.error('加载商品失败', error)
    productList.value = []
  } finally {
    productLoading.value = false
    isLoading.value = false
  }
}

const loadRecommend = async () => {
  if (!user.value) return
  recommendLoading.value = true
  try {
    const res = await getRecommendList(user.value.id)
    recommendList.value = Array.isArray(res) ? res : (res.data || [])
    console.log('推荐商品数量:', recommendList.value.length)
  } catch (error) {
    console.error('加载推荐失败', error)
    recommendList.value = []
  } finally {
    recommendLoading.value = false
  }
}

// 刷新推荐（供外部调用）
const refreshRecommend = () => {
  loadRecommend()
}

const goToDetail = (id) => {
  router.push(`/product/${id}`)
}

const goToPublish = () => {
  router.push('/publish')
}

const goToRecommend = () => {
  router.push('/recommend')
}

const handleCategoryChange = () => {
  if (loadTimer) clearTimeout(loadTimer)
  loadTimer = setTimeout(() => {
    loadProducts()
  }, 200)
}

const handleMenuSelect = (index) => {
  const map = {
    home: '/',
    recommend: '/recommend',
    publish: '/publish',
    products: '/profile?tab=products',
    favorites: '/profile?tab=favorites',
    orders: '/orders',
    assistant: '/assistant',
    admin: '/admin',
    stats: '/statistics',
    profile: '/profile'
  }
  router.push(map[index] || '/')
}

const handleMenu = (command) => {
  const map = {
    profile: '/profile',
    publish: '/publish',
    products: '/profile?tab=products',
    favorites: '/profile?tab=favorites',
    orders: '/orders',
    admin: '/admin',
    stats: '/statistics'
  }
  if (command === 'logout') {
    localStorage.removeItem('user')
    sessionStorage.removeItem('access_token')
    ElMessage.success('已退出')
    router.push('/login')
  } else {
    router.push(map[command] || '/')
  }
}

const goToChatList = () => {
  router.push('/chatlist')
}

// 监听收藏变化，刷新推荐
const handleRefreshRecommend = () => {
  loadRecommend()
}

onMounted(() => {
  const savedUser = localStorage.getItem('user')
  if (savedUser) {
    user.value = JSON.parse(savedUser)
    loadUnreadCount()
    loadRecommend()
  }
  loadProducts()

  // 根据当前路由设置侧边栏高亮
  if (route.path === '/') activeMenu.value = 'home'
  else if (route.path === '/recommend') activeMenu.value = 'recommend'
  else if (route.path === '/publish') activeMenu.value = 'publish'
  else if (route.path === '/profile') activeMenu.value = 'profile'
  else if (route.path === '/admin') activeMenu.value = 'admin'
  else if (route.path === '/statistics') activeMenu.value = 'stats'

  // 监听收藏变化事件
  window.addEventListener('refreshRecommend', handleRefreshRecommend)
})

onUnmounted(() => {
  window.removeEventListener('refreshRecommend', handleRefreshRecommend)
})
</script>

<style scoped>
.home {
  min-height: 100vh;
  background: #f5f7fa;
}
.header {
  background: white;
  padding: 0 24px;
  height: 60px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  position: sticky;
  top: 0;
  z-index: 100;
}
.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}
.logo-icon { font-size: 28px; }
.logo-text { font-size: 18px; font-weight: bold; color: #42b983; }
.nav-right {
  display: flex;
  align-items: center;
  gap: 20px;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}
.username {
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.msg-icon {
  font-size: 20px;
  cursor: pointer;
}
.main-layout {
  display: flex;
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
  gap: 24px;
}
.sidebar {
  width: 220px;
  flex-shrink: 0;
}
.user-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  text-align: center;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}
.user-card h3 {
  margin: 12px 0 4px;
  font-size: 16px;
}
.user-card p {
  color: #999;
  font-size: 12px;
}
.sidebar-menu {
  background: white;
  border-radius: 12px;
  border: none;
  padding: 8px 0;
}
.sidebar-menu .el-menu-item {
  display: flex;
  align-items: center;
  border-radius: 8px;
  margin: 4px 8px;
  height: 44px;
  padding: 0 16px;
}
.sidebar-menu .el-menu-item.is-active {
  background: #e6f7e6;
  color: #42b983;
}
.sidebar-menu .el-menu-item .el-icon {
  margin-right: 8px;
}
.content { flex: 1; }

.banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 28px 36px;
  margin-bottom: 24px;
  color: white;
}
.banner-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.banner-tag {
  background: rgba(255,255,255,0.2);
  padding: 2px 12px;
  border-radius: 12px;
  font-size: 12px;
}
.banner-text h2 {
  font-size: 22px;
  margin: 6px 0 4px;
}
.banner-text p {
  opacity: 0.85;
  font-size: 14px;
}
.banner-stats {
  display: flex;
  align-items: center;
  gap: 20px;
}
.stat-item {
  text-align: center;
}
.stat-num {
  font-size: 28px;
  font-weight: bold;
  display: block;
}
.stat-label {
  font-size: 12px;
  opacity: 0.8;
}
.stat-divider {
  width: 1px;
  height: 40px;
  background: rgba(255,255,255,0.3);
}

.category-filter {
  background: white;
  border-radius: 12px;
  padding: 0 16px;
  margin-bottom: 24px;
}

.section {
  margin-bottom: 32px;
}
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.section-left {
  display: flex;
  align-items: center;
  gap: 10px;
}
.section-icon {
  font-size: 18px;
}
.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}
.section-badge {
  font-size: 11px;
  color: #42b983;
  background: #e6f7e6;
  padding: 2px 10px;
  border-radius: 10px;
}
.section-more {
  font-size: 13px;
  color: #999;
  cursor: pointer;
}
.section-more:hover {
  color: #42b983;
}

.recommend-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 18px;
}
.recommend-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}
.recommend-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.12);
}
.recommend-image {
  position: relative;
  height: 140px;
  overflow: hidden;
  background: #f0f0f0;
}
.recommend-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.recommend-tag {
  position: absolute;
  top: 10px;
  left: 10px;
  background: #ff6b6b;
  color: white;
  font-size: 11px;
  padding: 2px 10px;
  border-radius: 10px;
}
.recommend-info {
  padding: 12px 14px;
}
.recommend-info h3 {
  font-size: 14px;
  margin: 0 0 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.recommend-info p {
  color: #999;
  font-size: 12px;
  margin-bottom: 8px;
  height: 32px;
  overflow: hidden;
}
.recommend-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.recommend-bottom .price {
  color: #f50;
  font-size: 16px;
  font-weight: bold;
}
.like-count {
  font-size: 12px;
  color: #999;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(190px, 1fr));
  gap: 16px;
}
.product-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}
.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 16px rgba(0,0,0,0.1);
}
.product-image {
  width: 100%;
  height: 130px;
  object-fit: cover;
  background: #f0f0f0;
}
.product-info {
  padding: 10px 12px;
}
.product-info h3 {
  font-size: 14px;
  margin: 0 0 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.product-info p {
  color: #999;
  font-size: 12px;
  margin-bottom: 8px;
  height: 32px;
  overflow: hidden;
}
.product-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.product-bottom .price {
  color: #f50;
  font-size: 16px;
  font-weight: bold;
}
.views {
  font-size: 12px;
  color: #bbb;
}
.empty-state {
  grid-column: 1 / -1;
  padding: 40px 0;
}
</style>