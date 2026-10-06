import { createApp } from 'vue'
import { createPinia } from 'pinia'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
// Element Plus 暗色主题变量（日夜切换基础）
import 'element-plus/theme-chalk/dark/css-vars.css'
import '@/styles/index.scss'
import '@/styles/theme.scss'
import App from './App.vue'
import router from './router'
import { useThemeStore } from '@/store/theme'

// dayjs 中文区域：el-calendar 以周一为一周第一天
dayjs.locale('zh-cn')

const app = createApp(App)

app.use(createPinia())
app.use(router)

// 初始化主题（localStorage → 系统偏好）
useThemeStore().initTheme()

app.mount('#app')