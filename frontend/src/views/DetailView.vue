<template>
  <div class="detail-container">
    <div v-if="product" class="product-detail">
      <div class="product-image-card">
        <img
          :src="product.images || 'https://picsum.photos/800/600?random=' + product.id"
          :alt="product.title"
        />
      </div>
      <div class="product-copy">
        <h1>{{ product.title }}</h1>
        <div class="price">¥{{ product.price }}</div>
        <div class="description">
          <h3>商品描述</h3>
          <p>{{ product.description }}</p>
        </div>
        <div class="info">
          <p>卖家ID：{{ product.userId }}</p>
          <p>分类：{{ getCategoryName(product.categoryId) }}</p>
          <p v-if="product.spotName" class="spot-chip">📍 {{ product.spotName }}</p>
          <p v-if="product.spotDescription" class="spot-description">{{ product.spotDescription }}</p>
          <p>状态：{{ product.status === 0 ? '在售' : '已售' }}</p>
          <p>浏览量：{{ product.views }}</p>
        </div>
        <div class="actions">
          <button @click="addFavorite" :disabled="favorited">❤️ 收藏</button>
          <button v-if="user && user.id !== product.userId && product.status === 0" @click="buyProduct" class="buy-btn">🛒 立即购买</button>
          <button v-if="user && user.id !== product.userId" @click="goToChat">💬 私信卖家</button>
          <span v-else-if="user && user.id === product.userId" class="self-tip">这是你的商品</span>
          <span v-if="product.status === 1" class="sold-tag">已售出</span>
        </div>
      </div>
    </div>
    <div v-else class="loading">加载中...</div>

    <!-- 留言区 -->
    <div class="message-section">
      <h3>留言板</h3>
      <div class="message-list">
        <div v-for="msg in messageList" :key="msg.id" class="message-item">
          <strong>用户{{ msg.fromUserId }}</strong>
          <p>{{ msg.content }}</p>
          <span>{{ formatTime(msg.createTime) }}</span>
        </div>
      </div>
      <div class="message-input" v-if="user">
        <textarea v-model="messageContent" placeholder="写下你的留言..."></textarea>
        <button @click="sendMessage">发表留言</button>
      </div>
      <p v-else>请登录后留言</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const route = useRoute()
const router = useRouter()
const product = ref(null)
const favorited = ref(false)
const messageList = ref([])
const messageContent = ref('')
const user = ref(null)

const productId = route.params.id

// 获取商品详情
const loadProduct = async () => {
  try {
    const res = await request.get(`/product/${productId}`)
    product.value = res.data || res
    if (user.value) {
      checkFavorite()
    }
  } catch (error) {
    console.error('加载商品失败', error)
  }
}

// 检查是否已收藏
const checkFavorite = async () => {
  try {
    const res = await request.get(`/behavior/favorite/check?userId=${user.value.id}&productId=${productId}`)
    favorited.value = res.favorited || false
  } catch (error) {
    console.error('检查收藏失败', error)
  }
}

// 添加收藏 - 只写 behavior_log
const addFavorite = async () => {
  if (!user.value) {
    ElMessage.warning('请先登录')
    return
  }
  if (favorited.value) {
    ElMessage.warning('已收藏')
    return
  }
  try {
    await request.post(`/recommend/favorite?userId=${user.value.id}&productId=${productId}`)
    ElMessage.success('收藏成功')
    favorited.value = true
  } catch (error) {
    ElMessage.error('收藏失败')
  }
}

// 购买商品
const buyProduct = async () => {
  if (!user.value) {
    ElMessage.warning('请先登录')
    return
  }
  if (product.value.status !== 0) {
    ElMessage.warning('商品已售出')
    return
  }
  if (user.value.id === product.value.userId) {
    ElMessage.warning('不能购买自己的商品')
    return
  }

  const msg = prompt('给卖家留言（选填）：', '我对这个商品感兴趣，想购买')
  if (msg === null) return

  try {
    const res = await request.post(`/trade/create?productId=${product.value.id}&buyerId=${user.value.id}&message=${encodeURIComponent(msg || '')}`)
    ElMessage.success('🎉 购买请求已发送！等待卖家确认')
    loadProduct()
  } catch (error) {
    console.error('购买失败', error)
    ElMessage.error('购买请求失败，请稍后重试')
  }
}

// 获取留言列表
const loadMessages = async () => {
  try {
    const res = await request.get(`/message/list/${productId}`)
    messageList.value = res.data || res || []
  } catch (error) {
    console.error('加载留言失败', error)
  }
}

// 发送留言
const sendMessage = async () => {
  if (!messageContent.value.trim()) {
    ElMessage.warning('请输入留言内容')
    return
  }
  try {
    await request.post(`/message/add?productId=${productId}&fromUserId=${user.value.id}&toUserId=${product.value.userId}&content=${messageContent.value}`)
    messageContent.value = ''
    loadMessages()
    ElMessage.success('留言成功')
  } catch (error) {
    ElMessage.error('留言失败')
  }
}

// 跳转到私信
const goToChat = () => {
  router.push(`/chat/${product.value.userId}`)
}

const getCategoryName = (id) => {
  const map = { 1: '教材教辅', 2: '电子产品', 3: '生活用品', 4: '体育器材', 5: '服饰鞋包', 6: '其他' }
  return map[id] || '未知'
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString()
}

onMounted(() => {
  const savedUser = localStorage.getItem('user')
  if (savedUser) {
    user.value = JSON.parse(savedUser)
  }
  loadProduct()
  loadMessages()
})
</script>

<style scoped>
.detail-container {
  max-width: 1120px;
  margin: 0 auto;
  padding: var(--space-lg);
  font-family: var(--font-family);
  color: var(--color-text-primary);
}
.product-detail {
  display: grid;
  grid-template-columns: minmax(280px, 0.9fr) minmax(0, 1.1fr);
  gap: var(--space-lg);
  padding: var(--space-lg);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-bg-card);
  box-shadow: var(--shadow-card);
}
.product-image-card {
  align-self: start;
  overflow: hidden;
  border-radius: var(--radius-lg);
  background: var(--color-bg-page);
  box-shadow: var(--shadow-sm);
}
.product-image-card img {
  display: block;
  width: 100%;
  min-height: 280px;
  max-height: 480px;
  object-fit: cover;
}
.price {
  margin: var(--space-sm) 0 var(--space-lg);
  color: var(--color-price);
  font-size: 32px;
  font-weight: 700;
}
.spot-description {
  margin-top: -8px;
  color: var(--color-text-secondary);
}
.spot-chip {
  display: inline-flex;
  width: fit-content;
  padding: 4px 12px;
  border-radius: var(--radius-pill);
  background: var(--color-primary-soft);
  color: var(--color-primary-dark);
}
.actions {
  display: flex;
  gap: var(--space-sm);
  margin-top: var(--space-lg);
  flex-wrap: wrap;
  align-items: center;
}
.actions button {
  padding: 10px 20px;
  background: var(--color-primary-gradient);
  color: white;
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
  font-weight: 600;
  transition: var(--transition-base);
}
.actions button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}
.actions button:disabled {
  background: var(--color-border);
  cursor: not-allowed;
}
.buy-btn {
  background: var(--color-accent-gradient);
  color: white;
  border: none;
}
.self-tip {
  color: var(--color-text-tertiary);
  font-size: 14px;
}
.sold-tag {
  background: var(--color-text-tertiary);
  color: white;
  padding: 4px 12px;
  border-radius: var(--radius-pill);
  font-size: 14px;
}
.message-section {
  margin-top: var(--space-lg);
  padding: var(--space-lg);
  border-radius: var(--radius-lg);
  background: var(--color-bg-card);
  box-shadow: var(--shadow-sm);
}
.message-item {
  border-bottom: 1px solid var(--color-border);
  padding: 10px 0;
}
.message-input {
  margin-top: 20px;
}
.message-input textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  min-height: 80px;
}
.message-input button {
  margin-top: 10px;
  padding: 8px 16px;
  background: var(--color-primary-gradient);
  color: white;
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
}
@media (max-width: 760px) {
  .detail-container {
    padding: var(--space-md);
  }
  .product-detail {
    grid-template-columns: 1fr;
    gap: var(--space-md);
    padding: var(--space-md);
  }
  .product-image-card img {
    min-height: 220px;
    max-height: 360px;
  }
}
</style>