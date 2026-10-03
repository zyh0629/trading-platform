<template>
  <div class="publish-container">
    <h2>发布商品</h2>

    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="商品标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入商品标题（2-30字）" />
      </el-form-item>

      <el-form-item label="商品描述" prop="description">
        <el-button
          class="ai-generate-button"
          :loading="generatingDescription"
          :disabled="generatingDescription"
          @click="generateDescription"
        >
          {{ generatingDescription ? '生成中...' : '✨ AI 生成描述' }}
        </el-button>
        <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请详细描述商品情况（10-200字）" />
      </el-form-item>

      <el-form-item label="价格" prop="price">
        <el-input v-model="form.price" type="number" placeholder="请输入价格（0.01-99999）" />
      </el-form-item>

      <el-form-item label="商品分类" prop="categoryId">
        <el-select v-model="form.categoryId" placeholder="请选择分类">
          <el-option label="教材" :value="1" />
          <el-option label="电子产品" :value="2" />
          <el-option label="生活用品" :value="3" />
          <el-option label="其他" :value="4" />
        </el-select>
      </el-form-item>

      <el-form-item label="交易点" prop="spotId">
        <el-select
          v-model="form.spotId"
          placeholder="📍 选择交易点"
          filterable
          @change="handleSpotSelection"
        >
          <el-option
            v-for="spot in spotList"
            :key="spot.id"
            :label="spot.isPublic === 0 ? `📍 我的：${spot.name}` : spot.name"
            :value="spot.id"
          />
          <el-option label="➕ 新增交易点" value="__add__" />
        </el-select>
        <div v-if="mySpotList.length" class="my-spot-list">
          <span v-for="spot in mySpotList" :key="spot.id" class="my-spot-item">
            📍 {{ spot.name }}
            <el-button link type="danger" size="small" @click.stop="deleteMySpot(spot)">删除</el-button>
          </span>
        </div>
      </el-form-item>

      <!-- 商品图片上传 -->
      <el-form-item label="商品图片" prop="images">
        <el-upload
          class="upload-demo"
          action="/api/upload/image"
          :headers="uploadHeaders"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadError"
          :before-upload="beforeUpload"
          :limit="1"
          list-type="picture"
        >
          <el-button type="primary">点击上传图片</el-button>
          <template #tip>
            <div class="el-upload__tip">支持 jpg/png 格式，大小不超过 5MB</div>
          </template>
        </el-upload>
      </el-form-item>

      <el-form-item label="图片链接" prop="imageUrl">
        <el-input v-model="form.images" placeholder="或手动输入图片URL（可选）" />
      </el-form-item>

      <el-form-item>
        <el-button class="submit-button" type="primary" @click="handlePublish" :loading="submitting">发布商品</el-button>
        <el-button @click="$router.push('/')">取消</el-button>
      </el-form-item>
    </el-form>

    <CampusSpotDialog v-model="spotDialogVisible" @created="handleSpotCreated" />
    <p v-if="message" :class="messageType">{{ message }}</p>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../api/request'
import CampusSpotDialog from '../components/CampusSpotDialog.vue'

const router = useRouter()
const submitting = ref(false)
const generatingDescription = ref(false)
const spotList = ref([])
const mySpotList = ref([])
const spotDialogVisible = ref(false)
const ADD_SPOT_OPTION = '__add__'
const message = ref('')
const messageType = ref('')

const formRef = ref(null)

const form = ref({
  title: '',
  description: '',
  price: '',
  categoryId: '',
  spotId: '',
  images: ''
})

// 上传请求头（携带 Token）
const uploadHeaders = computed(() => {
  const token = sessionStorage.getItem('access_token')
  if (token) {
    return { Authorization: `Bearer ${token}` }
  }
  return {}
})

// 校验规则
const rules = {
  title: [
    { required: true, message: '请输入商品标题', trigger: 'blur' },
    { min: 2, max: 30, message: '标题长度在 2-30 字', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入商品描述', trigger: 'blur' },
    { min: 5, max: 200, message: '描述长度在 5-200 字', trigger: 'blur' }
  ],
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' },
    { pattern: /^\d+(\.\d{1,2})?$/, message: '请输入正确的价格格式', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择商品分类', trigger: 'change' }
  ],
  spotId: [
    { required: true, message: '请选择校内交易点', trigger: 'change' }
  ]
}

const loadSpots = async () => {
  try {
    const [allSpots, mySpots] = await Promise.all([
      request.get('/campus-spot/list'),
      request.get('/campus-spot/my')
    ])
    spotList.value = Array.isArray(allSpots) ? allSpots : []
    mySpotList.value = Array.isArray(mySpots) ? mySpots : []
  } catch (error) {
    ElMessage.error(error.message || '加载交易点失败')
  }
}

const handleSpotSelection = (value) => {
  if (value === ADD_SPOT_OPTION) {
    form.value.spotId = ''
    spotDialogVisible.value = true
  }
}

const handleSpotCreated = async (spot) => {
  await loadSpots()
  form.value.spotId = spot.id
  ElMessage.success('私有交易点已创建')
}

const deleteMySpot = async (spot) => {
  if (!window.confirm(`确定删除私有交易点“${spot.name}”吗？`)) return
  try {
    await request.delete(`/campus-spot/my/${spot.id}`)
    if (form.value.spotId === spot.id) form.value.spotId = ''
    await loadSpots()
    ElMessage.success('私有交易点已删除')
  } catch (error) {
    ElMessage.error(error.message || '删除交易点失败')
  }
}

// 上传成功回调
const handleUploadSuccess = (response, file) => {
  if (response.code === 200 && response.data?.url) {
    form.value.images = response.data.url
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

// 上传失败回调
const handleUploadError = () => {
  ElMessage.error('上传失败，请重试')
}

// 上传前校验
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  return true
}

const generateDescription = async () => {
  if (!form.value.title.trim()) {
    ElMessage.warning('请先填写商品标题')
    return
  }
  if (!form.value.categoryId) {
    ElMessage.warning('请先选择商品分类')
    return
  }

  const categoryNames = {
    1: '教材教辅',
    2: '电子产品',
    3: '生活用品',
    4: '其他'
  }

  generatingDescription.value = true
  try {
    const result = await request.post('/ai/generate-description', {
      title: form.value.title.trim(),
      category: categoryNames[form.value.categoryId] || '其他'
    })
    const description = result.description || ''
    const descriptionChars = Array.from(description.trim())
    if (descriptionChars.length > 200) {
      const punctuation = /[。！？；，]/
      let endIndex = 200
      for (let index = 199; index >= 0; index--) {
        if (punctuation.test(descriptionChars[index])) {
          endIndex = index + 1
          break
        }
      }
      form.value.description = descriptionChars.slice(0, endIndex).join('')
      ElMessage.warning('描述已自动截断到 200 字以内')
    } else {
      form.value.description = description.trim()
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || error.message || '描述生成失败，请稍后重试')
  } finally {
    generatingDescription.value = false
  }
}

const handlePublish = async () => {
  // 表单校验
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  message.value = ''

  try {
    const userStr = localStorage.getItem('user')
    if (!userStr) {
      ElMessage.warning('请先登录')
      router.push('/login')
      return
    }
    const user = JSON.parse(userStr)

    const res = await request.post('/product/add', {
      userId: user.id,
      title: form.value.title,
      description: form.value.description,
      price: parseFloat(form.value.price),
      categoryId: parseInt(form.value.categoryId),
      spotId: form.value.spotId,
      images: form.value.images
    })

    if (res === '发布成功') {
      ElMessage.success('发布成功！')
      setTimeout(() => router.push('/'), 1500)
    } else {
      ElMessage.error(res || '发布失败')
    }
  } catch (error) {
    ElMessage({
      message: error.message || '发布失败，请稍后重试',
      type: 'error',
      duration: 8000
    })
  } finally {
    submitting.value = false
  }
}

onMounted(loadSpots)
</script>

<style scoped>
.publish-container {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
}
.publish-container h2 {
  margin-bottom: 24px;
  text-align: center;
}
.upload-demo {
  width: 100%;
}
.el-upload__tip {
  font-size: 12px;
  color: var(--color-text-secondary);
  margin-top: 4px;
}
.ai-generate-button {
  display: flex;
  margin-bottom: 8px;
  border: none;
  color: var(--color-bg-card);
  background: var(--color-accent-gradient);
}
.ai-generate-button:hover,
.ai-generate-button:focus {
  color: var(--color-bg-card);
  background: var(--color-accent-gradient);
}
:deep(.submit-button) {
  border: 0;
  background: var(--color-primary-gradient);
}
.my-spot-list {
  display: flex;
  flex-wrap: wrap;
  gap: 4px 12px;
  margin-top: 6px;
}
.my-spot-item {
  color: var(--color-text-secondary);
  font-size: 12px;
}

@media (max-width: 640px) {
  .publish-container {
    margin: var(--space-sm);
    padding: var(--space-md);
  }
}
</style>