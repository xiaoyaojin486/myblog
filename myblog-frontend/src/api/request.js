import axios from 'axios'
// ElMessage 由 unplugin-auto-import 自动导入（Element Plus 解析器）
import router from '@/router'

/**
 * axios 实例：统一 baseURL、Token 注入、Result<T> 解包与错误提示
 */
const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截：携带 Token
request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截：统一处理 Result<T>
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 200) {
      return res.data // 直接返回业务数据
    }
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      router.push('/admin/login')
    }
    ElMessage.error('网络异常，请稍后重试')
    return Promise.reject(error)
  }
)

export default request