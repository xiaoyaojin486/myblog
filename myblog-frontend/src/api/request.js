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
      return Promise.reject(error)
    }
    // 区分三种失败：超时 / 真的断了网 / 服务器返回了错误状态码。
    // 以前一律提示「网络异常」，把 413、500 这类配置或服务端问题
    // 也伪装成网络故障，排查时会被误导。
    if (!error.response) {
      ElMessage.error(error.code === 'ECONNABORTED' ? '请求超时，请重试' : '网络连接失败')
      return Promise.reject(error)
    }
    ElMessage.error(`请求失败（${error.response.status}）`)
    return Promise.reject(error)
  }
)

export default request