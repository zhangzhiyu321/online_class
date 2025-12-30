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
    // 如果后端返回的 code 不是 200，则视为错误
    if (res.code !== undefined && res.code !== 200) {
      console.error('API Error:', res.message || 'Error')
      return Promise.reject(new Error(res.message || 'Error'))
    }
    // 如果后端直接返回数据，则返回数据
    return res.data !== undefined ? res.data : res
  },
  error => {
    console.error('Request Error:', error.message || 'Request Error')
    return Promise.reject(error)
  }
)

export default request

