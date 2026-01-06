import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    const token = userStore.token || localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
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
    if (res.code === 200) {
      return res.data
    } else if (res.code === 401) {
      // 未登录，跳转到登录页
      const userStore = useUserStore()
      userStore.logout()
      router.push('/login')
      ElMessage.error('请先登录')
      return Promise.reject(new Error('未登录'))
    } else {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  error => {
    // 对于404错误，如果是未实现的接口（如未读消息数、未读通知数），静默处理
    if (error.response?.status === 404) {
      const url = error.config?.url || ''
      // 如果是未读消息数或未读通知数接口，不显示错误提示（这些接口可能还未实现）
      if (url.includes('/chat/unread-count') || 
          url.includes('/notification/unread-count') ||
          url.includes('/chat/relationships') ||
          url.includes('/chat/messages')) {
        return Promise.reject(error) // 静默失败，不显示错误提示
      }
    }
    
    // 对于401错误，跳转到登录页
    if (error.response?.status === 401) {
      const userStore = useUserStore()
      userStore.logout()
      router.push('/login')
      ElMessage.error('请先登录')
      return Promise.reject(new Error('未登录'))
    }
    
    const message = error.response?.data?.message || error.message || '网络错误'
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default request
