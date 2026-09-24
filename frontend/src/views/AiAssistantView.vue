<template>
  <div class="assistant-page">
    <el-card class="assistant-card">
      <template #header>
        <div class="header">
          <div>
            <h1>学习搭子</h1>
            <p>基于项目资料检索后回答，并展示参考资料</p>
          </div>
          <el-tag type="success">RAG 原型</el-tag>
        </div>
      </template>

      <el-alert
        title="回答仅基于当前项目资料，资料不足时会明确说明；不要输入密码、API Key 等敏感信息。"
        type="info"
        show-icon
        :closable="false"
      />
      <div class="messages">
        <div v-if="!answer" class="empty">例如：项目如何实现 JWT 和 RBAC？</div>
        <div v-else class="answer">
          <h3>回答</h3>
          <p>{{ answer }}</p>
          <div v-if="sources.length" class="sources">
            <span>参考资料：</span>
            <el-tag v-for="source in sources" :key="source" size="small">{{ source }}</el-tag>
          </div>
        </div>
      </div>
      <el-input
        v-model="question"
        type="textarea"
        :rows="4"
        maxlength="1000"
        show-word-limit
        placeholder="请输入关于项目或实验的问题"
        @keydown.ctrl.enter="ask"
      />
      <div class="actions">
        <span>Ctrl + Enter 发送</span>
        <el-button type="primary" :loading="loading" @click="ask">发送问题</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const question = ref('')
const answer = ref('')
const sources = ref([])
const loading = ref(false)

const ask = async () => {
  if (!question.value.trim() || loading.value) return
  loading.value = true
  try {
    const result = await request.post('/ai/ask', { question: question.value })
    answer.value = result.answer
    sources.value = result.sources || []
  } catch (error) {
    ElMessage.error(error.message || '问答服务暂时不可用')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.assistant-page { max-width: 900px; margin: 0 auto; padding: 40px 20px; }
.assistant-card { border-radius: 16px; }
.header { display: flex; justify-content: space-between; align-items: flex-start; }
h1 { margin: 0 0 8px; }
.header p { margin: 0; color: #909399; }
.messages { min-height: 220px; padding: 24px 4px; }
.empty { color: #a8abb2; text-align: center; padding-top: 80px; }
.answer { white-space: pre-wrap; line-height: 1.8; }
.answer h3 { margin-top: 0; }
.sources { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; color: #606266; }
.actions { display: flex; justify-content: space-between; align-items: center; margin-top: 12px; color: #909399; font-size: 13px; }
</style>
