import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// 请求拦截器：自动添加 Token
request.interceptors.request.use(
  config => {
    const userStr = localStorage.getItem('user')
    if (userStr) {
      try {
        const user = JSON.parse(userStr)
        if (user.token) {
          config.headers.Authorization = `Bearer ${user.token}`
        }
      } catch (e) {
        console.error('解析用户信息失败', e)
      }
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== undefined) {
      if (res.code === 200) {
        return res.data
      } else if (res.code === 401) {
        localStorage.removeItem('user')
        window.location.href = '/login'
        return Promise.reject(new Error(res.message))
      } else {
        return Promise.reject(new Error(res.message || '请求失败'))
      }
    }
    return res
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

export default request