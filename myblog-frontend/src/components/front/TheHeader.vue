<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  HomeFilled,
  FolderOpened,
  Calendar,
  User,
  Search,
  Sunny,
  Moon
} from '@element-plus/icons-vue'
import { useThemeStore } from '@/store/theme'
import { useConfigStore } from '@/store/config'

const route = useRoute()
const router = useRouter()
const themeStore = useThemeStore()
const configStore = useConfigStore()

const keyword = ref('')

// 回车/点击图标：跳转搜索结果页
function goSearch() {
  const kw = keyword.value.trim()
  router.push({ path: '/search', query: kw ? { keyword: kw } : {} })
}

const navs = [
  { path: '/', label: '首页', icon: HomeFilled },
  { path: '/projects', label: '项目', icon: FolderOpened },
  { path: '/archive', label: '归档', icon: Calendar },
  { path: '/about', label: '关于', icon: User }
]

// 子页面（如文章详情/项目详情）也保持对应导航高亮
const activeMenu = computed(() => {
  const path = route.path
  if (path === '/') return '/'
  if (path.startsWith('/project')) return '/projects'
  if (path.startsWith('/archive')) return '/archive'
  if (path.startsWith('/about')) return '/about'
  return path
})
</script>

<template>
  <header class="the-header">
    <div class="inner">
      <router-link to="/" class="logo">{{ configStore.siteName }}</router-link>

      <nav class="nav">
        <router-link
          v-for="n in navs"
          :key="n.path"
          :to="n.path"
          class="nav-item"
          :class="{ active: activeMenu === n.path }"
        >
          <el-icon class="nav-icon"><component :is="n.icon" /></el-icon>
          <span>{{ n.label }}</span>
        </router-link>
      </nav>

      <div class="actions">
        <div class="search-box">
          <el-icon class="search-icon"><Search /></el-icon>
          <input
            v-model="keyword"
            class="search-input"
            type="text"
            placeholder="搜索文章、项目…"
            @keyup.enter="goSearch"
          />
        </div>
        <el-button circle class="icon-btn search-toggle" title="搜索" @click="goSearch">
          <el-icon><Search /></el-icon>
        </el-button>
        <el-button circle class="icon-btn" title="切换日夜主题" @click="themeStore.toggleTheme()">
          <el-icon>
            <Moon v-if="!themeStore.isDark" />
            <Sunny v-else />
          </el-icon>
        </el-button>
      </div>
    </div>
  </header>
</template>

<style scoped>
.the-header {
  position: sticky;
  top: 0;
  z-index: 20;
  background: var(--blog-card-bg);
  border-bottom: 1px solid var(--blog-border);
  backdrop-filter: blur(var(--blog-glass-blur));
  -webkit-backdrop-filter: blur(var(--blog-glass-blur));
  transition: background-color 0.3s, border-color 0.3s;
}
.inner {
  max-width: 1280px;
  height: 58px;
  margin: 0 auto;
  padding: 0 16px;
  display: flex;
  align-items: center;
}
.logo {
  flex: 1 1 0;
  min-width: 0;
  font-size: 19px;
  font-weight: 700;
  white-space: nowrap;
  background: linear-gradient(120deg, var(--blog-primary), #a78bfa);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}
.nav {
  flex: 0 1 auto;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  min-width: 0;
  overflow-x: auto;
  scrollbar-width: none;
}
.nav::-webkit-scrollbar {
  display: none;
}
/* 图标 + 文字；选中项下方短横线（对照参考图） */
.nav-item {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  font-size: 14px;
  color: var(--blog-text-light);
  white-space: nowrap;
  transition: color 0.2s;
}
.nav-item:hover {
  color: var(--blog-primary);
}
.nav-item.active {
  color: var(--blog-primary);
  font-weight: 600;
}
.nav-item.active::after {
  content: '';
  position: absolute;
  left: 50%;
  bottom: 0;
  transform: translateX(-50%);
  width: 70%;
  height: 3px;
  border-radius: 2px;
  background: linear-gradient(90deg, var(--blog-primary), #a78bfa);
}
.nav-icon {
  font-size: 16px;
}
.actions {
  flex: 1 1 0;
  min-width: 0;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
}
/* 全站搜索框 */
.search-box {
  display: flex;
  align-items: center;
  gap: 6px;
  height: 34px;
  padding: 0 12px;
  border-radius: var(--blog-radius-pill);
  background: var(--blog-surface-2);
  border: 1px solid var(--blog-divider);
  transition: border-color 0.2s, background-color 0.2s;
}
.search-box:focus-within {
  border-color: var(--blog-primary);
  background: var(--blog-primary-soft);
}
.search-icon {
  font-size: 15px;
  color: var(--blog-text-light);
}
.search-input {
  width: 128px;
  border: none;
  outline: none;
  background: transparent;
  color: var(--blog-text);
  font-size: 13px;
}
.search-input::placeholder {
  color: var(--blog-text-light);
}
/* 窄屏下搜索框折叠为图标按钮 */
.search-toggle {
  display: none;
}
.icon-btn {
  border: 1px solid var(--blog-divider);
  background: var(--blog-surface-2);
  color: var(--blog-text-light);
  transition: color 0.2s, border-color 0.2s, background-color 0.2s;
}
.icon-btn:hover {
  color: var(--blog-primary);
  border-color: var(--blog-primary);
  background: var(--blog-primary-soft);
}

@media (max-width: 720px) {
  .logo {
    font-size: 16px;
  }
  .nav {
    flex: 1 1 auto;
    justify-content: flex-start;
  }
  .nav-item {
    padding: 8px 10px;
  }
  .nav-item span {
    display: none;
  }
  .search-box {
    display: none;
  }
  .search-toggle {
    display: inline-flex;
  }
}
</style>