<template>
  <div class="orders-container">
    <h2>我的订单</h2>

    <div class="tabs">
      <button :class="{ active: activeTab === 'buy' }" @click="switchTab('buy')">我买的</button>
      <button :class="{ active: activeTab === 'sell' }" @click="switchTab('sell')">我卖的</button>
    </div>

    <div class="order-list">
      <div v-for="order in orderList" :key="order.id" class="order-card">
        <div class="order-info">
          <h3>订单 #{{ order.id }}</h3>
          <p>商品：{{ order.productTitle || '商品已下架' }}</p>
          <p>价格：¥{{ order.price }}</p>
          <p>状态：
            <span :class="getStatusClass(order.status)">
              {{ ['待确认', '已确认', '已取消', '已完成'][order.status] }}
            </span>
          </p>
          <p v-if="order.message">买家留言：{{ order.message }}</p>
          <p>时间：{{ formatTime(order.createTime) }}</p>
        </div>
        <div class="order-actions">
          <button v-if="order.status === 0 && activeTab === 'sell'" @click="confirmOrder(order.id)" class="confirm-btn">确认交易</button>
          <button v-if="order.status === 0" @click="cancelOrder(order.id)" class="cancel-btn">取消</button>
          <button v-if="order.status === 1" @click="goToChat(order)" class="chat-btn">💬 私信</button>
        </div>
      </div>
      <el-empty v-if="orderList.length === 0" description="暂无订单" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const router = useRouter()
const activeTab = ref('buy')
const orderList = ref([])
const user = ref(null)

// 切换标签时重新加载
const switchTab = (tab) => {
  activeTab.value = tab
  loadOrders()
}

const loadOrders = async () => {
  if (!user.value) {
    console.warn('用户未登录')
    return
  }
  try {
    let res
    if (activeTab.value === 'buy') {
      res = await request.get(`/trade/buyer/${user.value.id}`)
      console.log('买家订单:', res)
    } else {
      res = await request.get(`/trade/seller/${user.value.id}`)
      console.log('卖家订单:', res)
    }
    const orders = Array.isArray(res) ? res : (res.data || [])
    orderList.value = orders
    console.log('当前订单数量:', orderList.value.length)

    // 加载每个订单的商品信息
    for (const order of orderList.value) {
      try {
        const productRes = await request.get(`/product/${order.productId}`)
        order.productTitle = productRes.data?.title || productRes?.title || '商品已下架'
      } catch (error) {
        order.productTitle = '商品已下架'
      }
    }
  } catch (error) {
    console.error('加载订单失败', error)
    orderList.value = []
  }
}

const confirmOrder = async (orderId) => {
  if (!confirm('确认这笔交易吗？')) return
  try {
    await request.put(`/trade/confirm?tradeId=${orderId}&sellerId=${user.value.id}`)
    ElMessage.success('交易确认成功！')
    loadOrders()
    // 通知首页刷新推荐
    window.dispatchEvent(new Event('refreshRecommend'))
  } catch (error) {
    console.error('确认失败', error)
    ElMessage.error('确认失败')
  }
}

const cancelOrder = async (orderId) => {
  if (!confirm('确定取消这笔交易吗？')) return
  try {
    await request.put(`/trade/cancel?tradeId=${orderId}&userId=${user.value.id}`)
    ElMessage.success('已取消')
    loadOrders()
    // 通知首页刷新推荐
    window.dispatchEvent(new Event('refreshRecommend'))
  } catch (error) {
    console.error('取消失败', error)
    ElMessage.error('取消失败')
  }
}

const goToChat = (order) => {
  const otherUserId = activeTab.value === 'buy' ? order.sellerId : order.buyerId
  router.push(`/chat/${otherUserId}`)
}

const getStatusClass = (status) => {
  return ['status-pending', 'status-confirmed', 'status-canceled', 'status-done'][status] || ''
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString()
}

onMounted(() => {
  const savedUser = localStorage.getItem('user')
  if (savedUser) {
    user.value = JSON.parse(savedUser)
    loadOrders()
  } else {
    ElMessage.warning('请先登录')
    router.push('/login')
  }
})
</script>

<style scoped>
.orders-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}
.tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}
.tabs button {
  padding: 8px 20px;
  border: none;
  background: #f5f5f5;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}
.tabs button.active {
  background: #42b983;
  color: white;
}
.order-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border: 1px solid #eee;
  border-radius: 8px;
  margin-bottom: 12px;
  background: white;
}
.order-info h3 {
  margin: 0 0 8px;
}
.order-info p {
  margin: 4px 0;
  font-size: 14px;
}
.order-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.confirm-btn {
  background: #42b983;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
}
.cancel-btn {
  background: #f56c6c;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
}
.chat-btn {
  background: #667eea;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
}
.status-pending { color: #e6a23c; }
.status-confirmed { color: #42b983; }
.status-canceled { color: #999; }
.status-done { color: #409eff; }
</style>