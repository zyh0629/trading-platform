<template>
  <div class="login-container">
    <div class="login-card">
      <div class="logo">
        <span class="logo-icon">📚</span>
        <span class="logo-text">校园二手交易平台</span>
      </div>

      <el-tabs v-model="activeTab" class="login-tabs">
        <!-- 登录 -->
        <el-tab-pane label="登录" name="login">
          <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" @submit.prevent="handleLogin">
            <el-form-item prop="username">
              <el-input v-model="loginForm.username" placeholder="请输入用户名" prefix-icon="User" size="large" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" prefix-icon="Lock" size="large" show-password />
            </el-form-item>
            <el-form-item>
              <div class="login-footer">
                <span class="forgot-password" @click="openForgotDialog">忘记密码？</span>
              </div>
              <el-button type="primary" size="large" @click="handleLogin" :loading="loading" class="submit-btn">登录</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 注册 -->
        <el-tab-pane label="注册" name="register">
          <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" @submit.prevent="handleRegister">
            <el-form-item prop="username">
              <el-input v-model="registerForm.username" placeholder="请输入用户名（4-16位）" prefix-icon="User" size="large" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="registerForm.password" type="password" placeholder="请输入密码（6-20位）" prefix-icon="Lock" size="large" show-password />
            </el-form-item>
            <el-form-item prop="studentId">
              <el-input v-model="registerForm.studentId" placeholder="请输入学号" prefix-icon="School" size="large" />
            </el-form-item>
            <el-form-item prop="email">
              <el-input v-model="registerForm.email" placeholder="请输入邮箱" prefix-icon="Message" size="large" />
            </el-form-item>
            <el-form-item prop="securityQuestion">
              <el-select v-model="registerForm.securityQuestion" placeholder="请选择安全问题" size="large" style="width:100%;">
                <el-option label="你的小学名称是什么？" value="你的小学名称是什么？" />
                <el-option label="你的生日是什么时候？" value="你的生日是什么时候？" />
                <el-option label="你的宠物名字是什么？" value="你的宠物名字是什么？" />
                <el-option label="你的家乡是哪里？" value="你的家乡是哪里？" />
              </el-select>
            </el-form-item>
            <el-form-item prop="securityAnswer">
              <el-input v-model="registerForm.securityAnswer" placeholder="请输入安全问题答案" size="large" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="large" @click="handleRegister" :loading="loading" class="submit-btn">注册</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 忘记密码弹窗 -->
    <el-dialog v-model="showForgotDialog" title="找回密码" width="420px">
      <el-form>
        <el-form-item label="用户名">
          <div style="display: flex; gap: 10px;">
            <el-input v-model="forgotUsername" placeholder="请输入用户名" style="flex:1;" />
            <el-button type="primary" @click="getSecurityQuestion">获取安全问题</el-button>
          </div>
        </el-form-item>
        <el-form-item v-if="securityQuestion" label="安全问题">
          <el-input :value="securityQuestion" disabled />
        </el-form-item>
        <el-form-item v-if="securityQuestion" label="答案">
          <el-input v-model="securityAnswer" placeholder="请输入安全问题的答案" />
        </el-form-item>
        <el-form-item v-if="securityQuestion" label="新密码">
          <el-input v-model="newPassword" type="password" placeholder="请输入新密码（6-20位）" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showForgotDialog = false">取消</el-button>
        <el-button v-if="securityQuestion" type="primary" @click="handleResetBySecurity">验证并重置</el-button>
        <el-button type="warning" @click="handleDirectReset">一键重置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, register } from '../api/user'
import request from '../api/request'

const router = useRouter()
const activeTab = ref('login')
const loading = ref(false)

const loginFormRef = ref(null)
const registerFormRef = ref(null)

const loginForm = ref({ username: '', password: '' })
const registerForm = ref({
  username: '',
  password: '',
  studentId: '',
  email: '',
  securityQuestion: '',
  securityAnswer: ''
})

const showForgotDialog = ref(false)
const forgotUsername = ref('')
const securityQuestion = ref('')
const securityAnswer = ref('')
const newPassword = ref('')

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2-20 位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6-20 位', trigger: 'blur' }
  ]
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2-20 位', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_\u4e00-\u9fa5]+$/, message: '用户名只能包含字母、数字、下划线、中文', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6-20 位', trigger: 'blur' }
  ],
  studentId: [
    { required: true, message: '请输入学号', trigger: 'blur' },
    { min: 5, max: 20, message: '学号长度在 5-20 位', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  securityQuestion: [
    { required: true, message: '请选择安全问题', trigger: 'change' }
  ],
  securityAnswer: [
    { required: true, message: '请输入安全问题答案', trigger: 'blur' },
    { min: 1, max: 50, message: '答案长度在 1-50 位', trigger: 'blur' }
  ]
}

const openForgotDialog = () => {
  showForgotDialog.value = true
  forgotUsername.value = ''
  securityQuestion.value = ''
  securityAnswer.value = ''
  newPassword.value = ''
}

const getSecurityQuestion = async () => {
  if (!forgotUsername.value.trim()) {
    ElMessage.warning('请输入用户名')
    return
  }
  try {
    const res = await request.get(`/user/security-question?username=${forgotUsername.value}`)
    console.log('安全问题返回:', res)
    if (res && typeof res === 'string') {
      securityQuestion.value = res
      ElMessage.success('获取安全问题成功')
    } else {
      securityQuestion.value = ''
    }
  } catch (error) {
    console.error('获取安全问题失败:', error)
    securityQuestion.value = ''
  }
}

const handleResetBySecurity = async () => {
  if (!securityAnswer.value.trim() || !newPassword.value.trim()) {
    ElMessage.warning('请填写答案和新密码')
    return
  }
  if (newPassword.value.length < 6 || newPassword.value.length > 20) {
    ElMessage.warning('密码长度在 6-20 位')
    return
  }
  try {
    const res = await request.post(`/user/reset-by-security?username=${forgotUsername.value}&answer=${securityAnswer.value}&newPassword=${newPassword.value}`)
    console.log('重置密码返回:', res)
    ElMessage.success('密码重置成功！')
    showForgotDialog.value = false
    loginForm.value.username = forgotUsername.value
    loginForm.value.password = newPassword.value
    forgotUsername.value = ''
    securityQuestion.value = ''
    securityAnswer.value = ''
    newPassword.value = ''
  } catch (error) {
    console.error('重置失败:', error)
    ElMessage.error(error.message || '重置失败，请检查答案是否正确')
  }
}

const handleDirectReset = async () => {
  if (!forgotUsername.value.trim()) {
    ElMessage.warning('请输入用户名')
    return
  }
  try {
    const res = await request.post(`/user/reset?username=${forgotUsername.value}`)
    console.log('一键重置返回:', res)
    ElMessage.success('密码已重置为 123456')
    showForgotDialog.value = false
    loginForm.value.username = forgotUsername.value
    loginForm.value.password = '123456'
    forgotUsername.value = ''
    securityQuestion.value = ''
    securityAnswer.value = ''
    newPassword.value = ''
  } catch (error) {
    console.error('重置失败:', error)
    ElMessage.error('重置失败，用户不存在')
  }
}

const handleLogin = async () => {
  const valid = await loginFormRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await login(loginForm.value.username, loginForm.value.password)
    console.log('登录返回:', res)
    if (res && res.token) {
      sessionStorage.setItem('access_token', res.token)
      localStorage.setItem('user', JSON.stringify(res.user))
      ElMessage.success('登录成功')
      if (res.user.role === 1) {
        router.push('/admin')
      } else {
        router.push('/')
      }
    } else {
      ElMessage.error(res?.message || '登录失败')
    }
  } catch (error) {
    console.error('登录错误:', error)
    ElMessage.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  const valid = await registerFormRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await register(registerForm.value)
    if (res !== undefined) {
      ElMessage.success('注册成功，请登录')
      activeTab.value = 'login'
      registerForm.value = { username: '', password: '', studentId: '', email: '', securityQuestion: '', securityAnswer: '' }
      registerFormRef.value?.resetFields()
    } else {
      ElMessage.error(res?.message || '注册失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-card {
  width: 420px;
  background: white;
  border-radius: 16px;
  padding: 40px 32px;
  box-shadow: 0 20px 40px rgba(0,0,0,0.1);
}
.logo {
  text-align: center;
  margin-bottom: 32px;
}
.logo-icon {
  font-size: 48px;
  display: block;
  margin-bottom: 8px;
}
.logo-text {
  font-size: 20px;
  font-weight: bold;
  color: #42b983;
}
.login-tabs :deep(.el-tabs__header) {
  margin-bottom: 24px;
}
.login-tabs :deep(.el-tabs__item) {
  font-size: 16px;
  font-weight: 500;
}
.submit-btn {
  width: 100%;
}
.login-footer {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 10px;
}
.forgot-password {
  color: #409eff;
  cursor: pointer;
  font-size: 14px;
}
.forgot-password:hover {
  color: #66b1ff;
}
</style>