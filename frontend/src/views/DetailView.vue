<template>
  <div class="detail-container">
    <div v-if="product" class="product-detail">
      <h1>{{ product.title }}</h1>
      <div class="price">¥{{ product.price }}</div>
      <div class="description">
        <h3>商品描述</h3>
        <p>{{ product.description }}</p>
      </div>
      <div class="info">
        <p>卖家ID：{{ product.userId }}</p>
        <p>分类：{{ getCategoryName(product.categoryId) }}</p>
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
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}
.product-detail {
  background: #f9f9f9;
  padding: 20px;
  border-radius: 8px;
}
.price {
  font-size: 28px;
  color: #f50;
  font-weight: bold;
}
.actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;
  flex-wrap: wrap;
  align-items: center;
}
.actions button {
  padding: 10px 20px;
  background: #42b983;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
.actions button:disabled {
  background: #ccc;
  cursor: not-allowed;
}
.buy-btn {
  background: #f56c6c;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
}
.buy-btn:hover {
  background: #e74c3c;
}
.self-tip {
  color: #999;
  font-size: 14px;
}
.sold-tag {
  background: #999;
  color: white;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 14px;
}
.message-section {
  margin-top: 30px;
}
.message-item {
  border-bottom: 1px solid #ddd;
  padding: 10px 0;
}
.message-input {
  margin-top: 20px;
}
.message-input textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  min-height: 80px;
}
.message-input button {
  margin-top: 10px;
  padding: 8px 16px;
  background: #42b983;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
</style>