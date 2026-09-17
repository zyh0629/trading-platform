<template>
  <div class="profile-container">
    <h1>个人中心</h1>

    <div class="tabs">
      <button :class="{ active: activeTab === 'info' }" @click="activeTab = 'info'">个人信息</button>
      <button :class="{ active: activeTab === 'products' }" @click="activeTab = 'products'">我的商品</button>
      <button :class="{ active: activeTab === 'favorites' }" @click="activeTab = 'favorites'">我的收藏</button>
    </div>

    <!-- 个人信息 -->
    <div v-if="activeTab === 'info'" class="tab-content">
      <div class="info-form">
        <div class="form-group">
          <label>用户名</label>
          <input v-model="userInfo.username" disabled />
        </div>
        <div class="form-group">
          <label>学号</label>
          <input v-model="userInfo.studentId" disabled />
        </div>
        <div class="form-group">
          <label>邮箱</label>
          <input v-model="userInfo.email" />
        </div>
        <div class="form-group">
          <label>手机号</label>
          <input v-model="userInfo.phone" placeholder="选填" />
        </div>
        <button @click="updateProfile">保存修改</button>
      </div>
    </div>

    <!-- 我的商品 -->
    <div v-if="activeTab === 'products'" class="tab-content">
      <div class="product-list">
        <div v-for="item in myProducts" :key="item.id" class="product-card">
          <div class="product-info">
            <h3>{{ item.title }}</h3>
            <p>{{ item.description }}</p>
            <span class="price">¥{{ item.price }}</span>
            <span class="status" :class="{ sold: item.status === 1 }">
              {{ item.status === 0 ? '在售' : '已售' }}
            </span>
          </div>
          <div class="product-actions">
            <button v-if="item.status === 0" @click="goToEdit(item.id)" class="edit-btn">编辑</button>
            <button v-if="item.status === 0" @click="offShelf(item.id)" class="off-btn">下架</button>
          </div>
        </div>
        <div v-if="myProducts.length === 0">暂无商品</div>
      </div>
    </div>

    <!-- 我的收藏 -->
    <div v-if="activeTab === 'favorites'" class="tab-content">
      <div class="product-list">
        <div v-for="item in myFavorites" :key="item.id" class="product-card">
          <div class="product-info">
            <h3>{{ item.product?.title || '商品已下架' }}</h3>
            <p>{{ item.product?.description || '' }}</p>
            <span class="price">¥{{ item.product?.price || 0 }}</span>
          </div>
          <button @click="removeFavorite(item.productId)" class="unfav-btn">取消收藏</button>
        </div>
        <div v-if="myFavorites.length === 0">暂无收藏</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const router = useRouter()
const route = useRoute()
const activeTab = ref('info')
const user = ref(null)
const userInfo = ref({})
const myProducts = ref([])
const myFavorites = ref([])

const loadUserInfo = async () => {
  if (!user.value) return
  const res = await request.get(`/user/${user.value.id}`)
  userInfo.value = res.data?.user || res.data || res
}

const updateProfile = async () => {
  try {
    await request.put('/user/update', userInfo.value)
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const loadMyProducts = async () => {
  if (!user.value) return
  try {
    const res = await request.get(`/product/my/${user.value.id}`)
    myProducts.value = Array.isArray(res) ? res : (res.data || [])
  } catch (error) {
    myProducts.value = []
  }
}

const goToEdit = (id) => {
  router.push(`/product/edit/${id}`)
}

const offShelf = async (productId) => {
  if (!confirm('确定下架该商品吗？')) return
  try {
    await request.delete(`/product/${productId}?userId=${user.value.id}`)
    ElMessage.success('下架成功')
    await loadMyProducts()
  } catch (error) {
    ElMessage.error('下架失败')
  }
}

const loadMyFavorites = async () => {
  if (!user.value) return
  try {
    const res = await request.get(`/behavior/favorites/${user.value.id}`)
    const favorites = Array.isArray(res) ? res : (res.data || [])
    myFavorites.value = favorites
    for (const fav of myFavorites.value) {
      try {
        const productRes = await request.get(`/product/${fav.productId}`)
        fav.product = productRes.data || productRes
      } catch (error) {
        fav.product = null
      }
    }
  } catch (error) {
    myFavorites.value = []
  }
}

const removeFavorite = async (productId) => {
  if (!user.value) return
  try {
    await request.delete(`/behavior/favorite?userId=${user.value.id}&productId=${productId}`)
    ElMessage.success('取消收藏成功')
    await loadMyFavorites()
    window.dispatchEvent(new Event('refreshRecommend'))
  } catch (error) {
    ElMessage.error('取消收藏失败')
  }
}

onMounted(() => {
  const savedUser = localStorage.getItem('user')
  if (!savedUser) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  user.value = JSON.parse(savedUser)

  // 根据 URL 参数切换标签
  const tab = route.query.tab
  if (tab === 'products') {
    activeTab.value = 'products'
  } else if (tab === 'favorites') {
    activeTab.value = 'favorites'
  } else {
    activeTab.value = 'info'
  }

  loadUserInfo()
  loadMyProducts()
  loadMyFavorites()
})
</script>

<style scoped>
.profile-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}
.tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  border-bottom: 1px solid #ddd;
}
.tabs button {
  padding: 10px 20px;
  background: none;
  border: none;
  cursor: pointer;
  font-size: 16px;
}
.tabs button.active {
  border-bottom: 2px solid #42b983;
  color: #42b983;
}
.tab-content {
  min-height: 400px;
}
.info-form {
  max-width: 400px;
}
.form-group {
  margin-bottom: 15px;
}
.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}
.form-group input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
}
.info-form button {
  padding: 10px 20px;
  background: #42b983;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
.product-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}
.product-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border: 1px solid #ddd;
  border-radius: 8px;
}
.product-info h3 {
  margin: 0 0 5px 0;
}
.product-actions {
  display: flex;
  gap: 8px;
}
.price {
  color: #f50;
  font-weight: bold;
  margin-right: 10px;
}
.status {
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
  background: #4caf50;
  color: white;
}
.status.sold {
  background: #999;
}
.edit-btn {
  padding: 6px 12px;
  background: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
.off-btn, .unfav-btn {
  padding: 6px 12px;
  background: #ff9800;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
.unfav-btn {
  background: #f44336;
}
</style>