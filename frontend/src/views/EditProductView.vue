<template>
  <div class="edit-container">
    <h2>编辑商品</h2>

    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" v-loading="loading">
      <el-form-item label="商品标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入商品标题（2-30字）" />
      </el-form-item>

      <el-form-item label="商品描述" prop="description">
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
        <div v-if="form.images && !form.images.startsWith('/upload/')" class="image-preview">
          <img :src="form.images" alt="商品图片" />
        </div>
      </el-form-item>

      <el-form-item label="图片链接" prop="imageUrl">
        <el-input v-model="form.images" placeholder="或手动输入图片URL（可选）" />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="handleUpdate" :loading="submitting">保存修改</el-button>
        <el-button @click="$router.push('/')">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const router = useRouter()
const route = useRoute()
const submitting = ref(false)
const loading = ref(false)

const formRef = ref(null)

const form = ref({
  id: null,
  title: '',
  description: '',
  price: '',
  categoryId: '',
  images: ''
})

// 上传请求头（携带 Token）
const uploadHeaders = computed(() => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    const user = JSON.parse(userStr)
    return { Authorization: `Bearer ${user.token}` }
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
  ]
}

// 加载商品信息
const loadProduct = async () => {
  const productId = route.params.id
  if (!productId) {
    ElMessage.error('商品不存在')
    router.push('/')
    return
  }

  loading.value = true
  try {
    const res = await request.get(`/product/${productId}`)
    if (res) {
      form.value = {
        id: res.id,
        title: res.title,
        description: res.description,
        price: res.price,
        categoryId: res.categoryId,
        images: res.images || ''
      }
    } else {
      ElMessage.error('商品不存在')
      router.push('/')
    }
  } catch (error) {
    ElMessage.error('加载商品失败')
    router.push('/')
  } finally {
    loading.value = false
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

// 保存修改
const handleUpdate = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true

  try {
    const userStr = localStorage.getItem('user')
    if (!userStr) {
      ElMessage.warning('请先登录')
      router.push('/login')
      return
    }

    const res = await request.put('/product/update', {
      id: form.value.id,
      title: form.value.title,
      description: form.value.description,
      price: parseFloat(form.value.price),
      categoryId: parseInt(form.value.categoryId),
      images: form.value.images
    })

    ElMessage.success('修改成功！')
    setTimeout(() => router.push('/'), 1500)
  } catch (error) {
    ElMessage.error('修改失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadProduct()
})
</script>

<style scoped>
.edit-container {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}
.edit-container h2 {
  margin-bottom: 24px;
  text-align: center;
}
.upload-demo {
  width: 100%;
}
.el-upload__tip {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
.image-preview {
  margin-top: 10px;
}
.image-preview img {
  max-width: 200px;
  max-height: 200px;
  border-radius: 8px;
  border: 1px solid #eee;
}
</style>