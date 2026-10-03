<template>
  <div class="recommend-page">
    <div class="page-header">
      <div class="header-content">
        <h1>🎯 智能推荐</h1>
        <p>基于你的行为数据，AI 为你精选可能感兴趣的商品</p>
        <div class="header-tags">
          <span class="tag">📌 收藏推荐</span>
          <span class="tag">🔥 热门推荐</span>
          <span class="tag">🛒 买过相似推荐</span>
        </div>
      </div>
    </div>

    <div class="recommend-content">
      <!-- 收藏推荐 -->
      <div class="recommend-section">
        <div class="section-header">
          <div class="section-title-group">
            <span class="section-icon">📌</span>
            <h2>收藏推荐</h2>
            <span class="section-badge">基于你的收藏记录</span>
          </div>
          <span class="section-count">共 {{ favoriteRecommend.length }} 件</span>
        </div>
        <div class="recommend-grid" v-loading="loading">
          <div v-for="item in favoriteRecommend" :key="item.id" class="recommend-item" @click="goToDetail(item.id)">
            <img :src="item.images || 'https://picsum.photos/400/300?random=' + item.id" class="item-image" />
            <div class="item-info">
              <h3>{{ item.title }}</h3>
              <p>{{ item.description }}</p>
              <div class="item-footer">
                <span class="price">¥{{ item.price }}</span>
                <span class="reason-tag">📌 收藏推荐</span>
              </div>
            </div>
          </div>
          <div v-if="favoriteRecommend.length === 0 && !loading" class="empty-state">
            <el-empty description="暂无收藏推荐，先去收藏一些商品吧" />
          </div>
        </div>
      </div>

      <!-- 热门推荐 -->
      <div class="recommend-section">
        <div class="section-header">
          <div class="section-title-group">
            <span class="section-icon">🔥</span>
            <h2>热门推荐</h2>
            <span class="section-badge">大家都在看</span>
          </div>
          <span class="section-count">共 {{ hotRecommend.length }} 件</span>
        </div>
        <div class="recommend-grid" v-loading="loading">
          <div v-for="item in hotRecommend" :key="item.id" class="recommend-item" @click="goToDetail(item.id)">
            <img :src="item.images || 'https://picsum.photos/400/300?random=' + item.id" class="item-image" />
            <div class="item-info">
              <h3>{{ item.title }}</h3>
              <p>{{ item.description }}</p>
              <div class="item-footer">
                <span class="price">¥{{ item.price }}</span>
                <span class="reason-tag">🔥 热门推荐</span>
              </div>
            </div>
          </div>
          <div v-if="hotRecommend.length === 0 && !loading" class="empty-state">
            <el-empty description="暂无热门商品" />
          </div>
        </div>
      </div>

      <!-- 买过相似推荐 -->
      <div class="recommend-section">
        <div class="section-header">
          <div class="section-title-group">
            <span class="section-icon">🛒</span>
            <h2>买过相似推荐</h2>
            <span class="section-badge">基于你的购买记录</span>
          </div>
          <span class="section-count">共 {{ tradeRecommend.length }} 件</span>
        </div>
        <div class="recommend-grid" v-loading="loading">
          <div v-for="item in tradeRecommend" :key="item.id" class="recommend-item" @click="goToDetail(item.id)">
            <img :src="item.images || 'https://picsum.photos/400/300?random=' + item.id" class="item-image" />
            <div class="item-info">
              <h3>{{ item.title }}</h3>
              <p>{{ item.description }}</p>
              <div class="item-footer">
                <span class="price">¥{{ item.price }}</span>
                <span class="reason-tag">🛒 买过相似推荐</span>
              </div>
            </div>
          </div>
          <div v-if="tradeRecommend.length === 0 && !loading" class="empty-state">
            <el-empty description="暂无购买推荐，去购买一些商品吧" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const router = useRouter()
const loading = ref(false)
const favoriteRecommend = ref([])
const hotRecommend = ref([])
const tradeRecommend = ref([])

const loadRecommend = async () => {
  const userStr = localStorage.getItem('user')
  if (!userStr) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  const user = JSON.parse(userStr)

  loading.value = true
  try {
    // 1. 收藏推荐
    const favRes = await request.get(`/recommend/favorite/${user.id}`)
    favoriteRecommend.value = Array.isArray(favRes) ? favRes : (favRes.data || [])

    // 2. 热门推荐
    const hotRes = await request.get('/recommend/hot/10')
    hotRecommend.value = Array.isArray(hotRes) ? hotRes : (hotRes.data || [])

    // 3. 买过相似推荐
    const tradeRes = await request.get(`/recommend/trade/${user.id}`)
    tradeRecommend.value = Array.isArray(tradeRes) ? tradeRes : (tradeRes.data || [])
  } catch (error) {
    console.error('加载推荐失败', error)
  } finally {
    loading.value = false
  }
}

const goToDetail = (id) => {
  router.push(`/product/${id}`)
}

onMounted(() => {
  loadRecommend()
})
</script>

<style scoped>
.recommend-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
.page-header {
  background: var(--color-primary-gradient);
  border-radius: var(--radius-lg);
  padding: 40px 48px;
  color: var(--color-bg-card);
  margin-bottom: 30px;
}
.page-header h1 {
  font-size: 28px;
  margin-bottom: 8px;
}
.page-header p {
  opacity: 0.85;
  font-size: 15px;
}
.header-tags {
  display: flex;
  gap: 10px;
  margin-top: 16px;
}
.tag {
  color: var(--color-primary-dark);
  background: var(--color-primary-soft);
  padding: 4px 14px;
  border-radius: var(--radius-pill);
  font-size: 13px;
  font-weight: 600;
}
.recommend-section {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: var(--shadow-card);
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.section-title-group {
  display: flex;
  align-items: center;
  gap: 10px;
}
.section-icon {
  font-size: 24px;
}
.section-title-group h2 {
  margin: 0;
  font-size: 18px;
}
.section-badge {
  font-size: 12px;
  color: var(--color-primary-dark);
  background: var(--color-primary-soft);
  padding: 2px 10px;
  border-radius: 10px;
}
.section-count {
  font-size: 13px;
  color: var(--color-text-secondary);
}
.recommend-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}
.recommend-item {
  display: flex;
  gap: 14px;
  padding: 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  cursor: pointer;
  background: var(--color-bg-card);
  box-shadow: var(--shadow-card);
  transition: var(--transition-base);
}
.recommend-item:hover {
  border-color: var(--color-primary-light);
  transform: translateY(-4px);
  box-shadow: var(--shadow-card-hover);
}
.item-image {
  width: 100px;
  height: 80px;
  object-fit: cover;
  border-radius: var(--radius-md);
  flex-shrink: 0;
}
.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
.item-info h3 {
  margin: 0 0 4px;
  font-size: 15px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.item-info p {
  margin: 0 0 8px;
  font-size: 13px;
  color: var(--color-text-secondary);
  height: 36px;
  overflow: hidden;
}
.item-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.item-footer .price {
  color: var(--color-price);
  font-size: 16px;
  font-weight: bold;
}
.reason-tag {
  font-size: 12px;
  color: var(--color-primary-dark);
  background: var(--color-primary-soft);
  padding: 2px 10px;
  border-radius: 10px;
}
.empty-state {
  grid-column: 1 / -1;
  padding: 30px 0;
}

@media (max-width: 640px) {
  .recommend-page {
    padding: var(--space-sm);
  }
  .page-header {
    padding: var(--space-lg);
  }
  .recommend-grid {
    grid-template-columns: 1fr;
  }
  .recommend-item {
    min-width: 0;
  }
}
</style>