<template>
  <div class="assistant-page">
    <el-card class="assistant-card" shadow="hover">
      <template #header>
        <div class="assistant-header">
          <div class="title-wrap">
            <el-avatar class="title-avatar" :size="44">学</el-avatar>
            <div>
              <h1>学习搭子</h1>
              <p>基于项目资料检索后回答</p>
            </div>
          </div>
          <el-tag class="rag-tag" effect="dark">RAG</el-tag>
        </div>
      </template>

      <el-alert
        class="security-tip"
        title="回答仅基于当前项目资料，资料不足时会明确说明；不要输入密码、令牌或 API Key。"
        type="info"
        show-icon
        :closable="false"
      />

      <el-scrollbar ref="messageScrollbar" class="message-scrollbar">
        <div class="message-list">
          <div v-if="messages.length === 0" class="welcome-message">
            <el-avatar :size="42">学</el-avatar>
            <div class="bubble ai-bubble">
              你好！我是学习搭子，可以根据项目资料回答实验和系统实现相关问题。
            </div>
          </div>

          <div
            v-for="message in messages"
            :key="message.id"
            class="message-row"
            :class="message.role === 'user' ? 'user-row' : 'ai-row'"
          >
            <el-avatar v-if="message.role === 'assistant'" :size="36">学</el-avatar>
            <div class="message-content">
              <div
                class="bubble"
                :class="[
                  message.role === 'user' ? 'user-bubble' : 'ai-bubble',
                  { 'error-bubble': message.error }
                ]"
              >
                <span v-if="message.loading" class="thinking">
                  <i></i><i></i><i></i>
                  思考中...
                </span>
                <span v-else>{{ message.content }}</span>
              </div>
              <div v-if="message.sources?.length && !message.loading" class="sources">
                <span class="sources-label">参考资料：</span>
                <el-tag
                  v-for="source in message.sources"
                  :key="source"
                  class="source-tag"
                  size="small"
                  effect="plain"
                  @click="showSource(source)"
                >
                  📚 {{ source }}
                </el-tag>
              </div>
            </div>
            <el-avatar v-if="message.role === 'user'" :size="36">我</el-avatar>
          </div>
        </div>
      </el-scrollbar>

      <div class="composer">
        <el-input
          v-model="question"
          class="question-input"
          type="textarea"
          :rows="3"
          maxlength="1000"
          show-word-limit
          resize="none"
          placeholder="请输入关于项目或实验的问题，Ctrl + Enter 发送"
          :disabled="loading"
          @keydown.ctrl.enter.prevent="ask"
        />
        <el-button
          class="send-button"
          type="primary"
          :loading="loading"
          :disabled="!question.trim()"
          @click="ask"
        >
          发送
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { nextTick, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const question = ref('')
const loading = ref(false)
const messageScrollbar = ref(null)
const messages = ref([])
let messageId = 0

const scrollToBottom = async () => {
  await nextTick()
  const scrollbar = messageScrollbar.value
  if (scrollbar) {
    scrollbar.setScrollTop(scrollbar.wrapRef.scrollHeight)
  }
}

const showSource = (source) => {
  ElMessage.info(`参考资料：${source}`)
}

const replaceMessage = (messageId, patch) => {
  const index = messages.value.findIndex(message => message.id === messageId)
  if (index !== -1) {
    messages.value[index] = {
      ...messages.value[index],
      ...patch
    }
  }
}

const ask = async () => {
  const text = question.value.trim()
  if (!text || loading.value) return

  messages.value.push({
    id: ++messageId,
    role: 'user',
    content: text,
    sources: []
  })
  question.value = ''
  loading.value = true

  const thinkingMessage = {
    id: ++messageId,
    role: 'assistant',
    content: '',
    sources: [],
    loading: true
  }
  messages.value.push(thinkingMessage)
  await scrollToBottom()

  try {
    const response = await request.post(
      '/ai/ask',
      { question: text },
      { timeout: 60000 }
    )

    console.log('[AI assistant] /api/ai/ask response:', response)

    // request.js unwraps successful responses to res.data, while this also
    // supports receiving the original Axios response in future callers.
    const envelope = response?.data ?? response

    if (envelope?.code !== undefined && envelope.code !== 200) {
      replaceMessage(thinkingMessage.id, {
        content: envelope.message || '问答服务返回失败',
        sources: [],
        loading: false,
        error: true
      })
    } else {
      const data = envelope?.code === 200 ? envelope.data : envelope
      replaceMessage(thinkingMessage.id, {
        content: data?.answer || '问答服务未返回有效答案',
        sources: data?.sources || [],
        loading: false,
        error: false
      })
    }
  } catch (error) {
    console.error('[AI assistant] /api/ai/ask error:', error)
    const errorData = error?.response?.data
    replaceMessage(thinkingMessage.id, {
      content: errorData?.message || error.message || '问答服务暂时不可用',
      sources: [],
      loading: false,
      error: true
    })
    ElMessage.error(error.message || '问答服务暂时不可用')
  } finally {
    loading.value = false
    await scrollToBottom()
  }
}
</script>

<style scoped>
.assistant-page {
  min-height: 100vh;
  padding: 32px 20px;
  box-sizing: border-box;
  background: #f3f5fb;
}

.assistant-card {
  max-width: 960px;
  height: calc(100vh - 64px);
  min-height: 620px;
  margin: 0 auto;
  overflow: hidden;
  border: 0;
  border-radius: 20px;
  background: #fff;
}

:deep(.el-card__header) {
  padding: 20px 24px;
  border-bottom: 1px solid #edf0f7;
  background: linear-gradient(135deg, #536dfe 0%, #7c4dff 100%);
}

:deep(.el-card__body) {
  height: calc(100% - 86px);
  padding: 18px 24px 20px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

.assistant-header,
.title-wrap,
.composer,
.welcome-message,
.message-row {
  display: flex;
  align-items: center;
}

.assistant-header {
  justify-content: space-between;
  color: #fff;
}

.title-wrap {
  gap: 12px;
}

.title-avatar {
  color: #5b5ce2;
  background: #fff;
}

h1 {
  margin: 0;
  font-size: 22px;
}

.title-wrap p {
  margin: 4px 0 0;
  font-size: 13px;
  opacity: 0.85;
}

.rag-tag {
  border: 0;
  background: rgba(255, 255, 255, 0.22);
}

.security-tip {
  flex-shrink: 0;
  margin-bottom: 12px;
}

.message-scrollbar {
  flex: 1;
  min-height: 0;
  padding: 0 8px;
}

.message-list {
  max-width: 820px;
  margin: 0 auto;
  padding: 14px 0;
}

.message-row,
.welcome-message {
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 18px;
}

.message-content {
  max-width: min(78%, 680px);
}

.user-row {
  justify-content: flex-end;
}

.ai-row {
  justify-content: flex-start;
}

.user-row .message-content {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.bubble {
  padding: 11px 15px;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
  border-radius: 16px;
  box-shadow: 0 3px 12px rgba(45, 55, 90, 0.08);
}

.user-bubble {
  color: #fff;
  border-top-right-radius: 4px;
  background: linear-gradient(135deg, #536dfe 0%, #7c4dff 100%);
}

.ai-bubble {
  color: #303133;
  border: 1px solid #edf0f7;
  border-top-left-radius: 4px;
  background: #fff;
}

.error-bubble {
  color: #b42318;
  border-color: #fecdca;
  background: #fff5f4;
}

.sources {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
  margin-top: 7px;
}

.sources-label {
  color: #909399;
  font-size: 12px;
}

.source-tag {
  cursor: pointer;
}

.source-tag:hover {
  color: #536dfe;
}

.welcome-message {
  justify-content: flex-start;
}

.thinking {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #909399;
}

.thinking i {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #7c4dff;
  animation: blink 1.2s infinite ease-in-out;
}

.thinking i:nth-child(2) {
  animation-delay: 0.15s;
}

.thinking i:nth-child(3) {
  animation-delay: 0.3s;
}

@keyframes blink {
  0%, 80%, 100% { opacity: 0.25; transform: translateY(0); }
  40% { opacity: 1; transform: translateY(-3px); }
}

.composer {
  flex-shrink: 0;
  align-items: flex-end;
  gap: 12px;
  max-width: 820px;
  width: 100%;
  margin: 12px auto 0;
}

.question-input {
  flex: 1;
}

.send-button {
  height: 40px;
  min-width: 76px;
  border: 0;
  background: linear-gradient(135deg, #536dfe 0%, #7c4dff 100%);
}

@media (max-width: 600px) {
  .assistant-page {
    padding: 0;
  }

  .assistant-card {
    height: 100vh;
    min-height: 0;
    border-radius: 0;
  }

  :deep(.el-card__header) {
    padding: 16px;
  }

  :deep(.el-card__body) {
    height: calc(100% - 78px);
    padding: 12px 12px 14px;
  }

  .message-content {
    max-width: 82%;
  }

  .composer {
    gap: 8px;
  }

  .send-button {
    min-width: 62px;
    padding: 0 12px;
  }
}
</style>
