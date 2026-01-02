import axios from 'axios'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 添加 token
    const token = localStorage.getItem('token')
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
    // 统一处理后端响应格式 {code, message, data}
    if (res.code === 200) {
      return res.data
    } else if (res.code === 401) {
      // 未登录，清除token并跳转登录页
      localStorage.removeItem('token')
      window.location.href = '/login'
      return Promise.reject(new Error('未登录'))
    } else {
      // 其他错误
      const message = res.message || '请求失败'
      console.error('API Error:', message)
      return Promise.reject(new Error(message))
    }
  },
  error => {
    // 网络错误或服务器错误
    const message = error.response?.data?.message || error.message || '网络错误'
    console.error('Request Error:', message)
    return Promise.reject(error)
  }
)

export default request

