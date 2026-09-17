<template>
  <div class="chat-list">
    <h2>消息中心</h2>
    <div class="chat-items">
      <div v-for="msg in conversationList" :key="msg.id" class="chat-item" @click="goToChat(msg)">
        <el-avatar :size="40" :icon="UserFilled" />
        <div class="chat-info">
          <div class="chat-name">
            用户{{ msg.fromUserId === user.id ? msg.toUserId : msg.fromUserId }}
            <span v-if="msg.isRead === 0 && msg.toUserId === user.id" class="unread-dot">●</span>
          </div>
          <div class="chat-preview">{{ msg.content }}</div>
        </div>
        <div class="chat-time">{{ formatTime(msg.createTime) }}</div>
      </div>
      <el-empty v-if="conversationList.length === 0" description="暂无消息" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { UserFilled } from '@element-plus/icons-vue'
import request from '../api/request'

const router = useRouter()
const user = ref(null)
const conversationList = ref([])

const loadConversations = async () => {
  if (!user.value) return
  try {
    const res = await request.get(`/message/conversations/${user.value.id}`)
    conversationList.value = Array.isArray(res) ? res : (res.data || [])
    console.log('对话列表:', conversationList.value)
  } catch (error) {
    console.error('加载对话列表失败', error)
    conversationList.value = []
  }
}

const goToChat = (msg) => {
  const otherUserId = msg.fromUserId === user.value.id ? msg.toUserId : msg.fromUserId
  router.push(`/chat/${otherUserId}`)
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getMonth()+1}/${date.getDate()} ${date.getHours().toString().padStart(2,'0')}:${date.getMinutes().toString().padStart(2,'0')}`
}

onMounted(() => {
  const savedUser = localStorage.getItem('user')
  if (savedUser) {
    user.value = JSON.parse(savedUser)
    loadConversations()
  } else {
    router.push('/login')
  }
})
</script>

<style scoped>
.chat-list {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}
.chat-list h2 {
  margin-bottom: 20px;
}
.chat-items {
  background: white;
  border-radius: 12px;
  overflow: hidden;
}
.chat-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #eee;
  cursor: pointer;
  transition: background 0.2s;
}
.chat-item:hover {
  background: #f5f7fa;
}
.chat-info {
  flex: 1;
  margin-left: 12px;
}
.chat-name {
  font-weight: 500;
  margin-bottom: 4px;
}
.chat-name .unread-dot {
  color: #ff4444;
  font-size: 16px;
  margin-left: 6px;
}
.chat-preview {
  font-size: 13px;
  color: #999;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 300px;
}
.chat-time {
  font-size: 12px;
  color: #bbb;
}
</style>