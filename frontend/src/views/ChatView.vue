<template>
  <div class="chat-container">
    <div class="chat-header">
      <button @click="goBack">← 返回</button>
      <h2>与 用户{{ otherUserId }} 的私信</h2>
    </div>

    <div class="message-list" ref="messageListRef">
      <div v-for="msg in messageList" :key="msg.id" :class="['message-item', msg.fromUserId === user?.id ? 'my-message' : 'other-message']">
        <div class="message-sender">{{ msg.fromUserId === user?.id ? '我' : '对方' }}</div>
        <div class="message-content">{{ msg.content }}</div>
        <div class="message-time">{{ formatTime(msg.createTime) }}</div>
      </div>
      <div v-if="messageList.length === 0" class="empty-chat">暂无消息，开始聊天吧</div>
    </div>

    <div class="message-input">
      <textarea v-model="inputContent" placeholder="输入消息..." @keydown.enter.prevent="sendMessage"></textarea>
      <button @click="sendMessage" :disabled="!inputContent.trim()">发送</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const route = useRoute()
const router = useRouter()
const otherUserId = ref(parseInt(route.params.id))
const user = ref(null)
const messageList = ref([])
const inputContent = ref('')
const messageListRef = ref(null)

let timer = null

// 加载聊天记录
const loadMessages = async () => {
  if (!user.value) return
  try {
    const res = await request.get(`/message/chat/${user.value.id}/${otherUserId.value}`)
    // 处理返回数据
    messageList.value = Array.isArray(res) ? res : (res.data || [])
    console.log('聊天记录:', messageList.value)

    // 标记收到的消息为已读
    const unreadMessages = messageList.value.filter(msg => msg.toUserId === user.value.id && msg.isRead === 0)
    for (const msg of unreadMessages) {
      try {
        await request.put(`/message/read/${msg.id}?userId=${user.value.id}`)
      } catch (e) {
        console.error('标记已读失败', e)
      }
    }
    scrollToBottom()
  } catch (error) {
    console.error('加载消息失败', error)
    messageList.value = []
  }
}

// 发送消息
const sendMessage = async () => {
  if (!inputContent.value.trim()) return
  try {
    const res = await request.post(`/message/send?fromUserId=${user.value.id}&toUserId=${otherUserId.value}&content=${inputContent.value}`)
    console.log('发送结果:', res)
    if (res && res.success) {
      inputContent.value = ''
      loadMessages()
    } else {
      ElMessage.error('发送失败')
    }
  } catch (error) {
    console.error('发送失败', error)
    ElMessage.error('发送失败')
  }
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getMonth()+1}/${date.getDate()} ${date.getHours().toString().padStart(2,'0')}:${date.getMinutes().toString().padStart(2,'0')}`
}

const goBack = () => {
  router.push('/chatlist')
}

// 定时刷新消息
const startPolling = () => {
  if (timer) clearInterval(timer)
  timer = setInterval(() => {
    if (user.value) {
      loadMessages()
    }
  }, 3000)
}

onMounted(() => {
  const savedUser = localStorage.getItem('user')
  if (!savedUser) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  user.value = JSON.parse(savedUser)
  loadMessages()
  startPolling()
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.chat-container {
  max-width: 800px;
  margin: 0 auto;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}
.chat-header {
  padding: 15px;
  background: white;
  border-bottom: 1px solid #ddd;
  display: flex;
  align-items: center;
  gap: 15px;
}
.chat-header button {
  padding: 5px 10px;
  background: #42b983;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 15px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.message-item {
  max-width: 70%;
  padding: 10px;
  border-radius: 8px;
}
.my-message {
  align-self: flex-end;
  background: #42b983;
  color: white;
}
.other-message {
  align-self: flex-start;
  background: white;
}
.message-sender {
  font-size: 12px;
  margin-bottom: 4px;
  opacity: 0.7;
}
.message-content {
  word-wrap: break-word;
}
.message-time {
  font-size: 10px;
  margin-top: 4px;
  text-align: right;
  opacity: 0.6;
}
.message-input {
  display: flex;
  padding: 15px;
  background: white;
  border-top: 1px solid #ddd;
  gap: 10px;
}
.message-input textarea {
  flex: 1;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 8px;
  resize: none;
  min-height: 60px;
}
.message-input button {
  padding: 0 20px;
  background: #42b983;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}
.message-input button:disabled {
  background: #ccc;
}
.empty-chat {
  text-align: center;
  color: #999;
  padding: 40px 0;
}
</style>