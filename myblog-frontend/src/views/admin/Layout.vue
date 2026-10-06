<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { getProfile } from '@/api/user'
import {
  Odometer,
  Document,
  Grid,
  FolderOpened,
  Files,
  CollectionTag,
  ChatDotRound,
  Setting,
  User,
  Postcard,
  ArrowDown,
  SwitchButton
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 文章编辑页也保持「文章管理」菜单高亮
const activeMenu = computed(() => {
  if (route.path.startsWith('/admin/article')) return '/admin/article'
  return route.path
})

const avatarText = computed(() => (userStore.user?.nickname || 'admin').slice(0, 1))

onMounted(async () => {
  try {
    userStore.setUser(await getProfile()) // 刷新头像/昵称，保持右上角最新
  } catch (e) {
    // 错误提示已由拦截器处理
  }
})

function handleCommand(command) {
  if (command === 'profile') {
    router.push('/admin/profile')
  } else if (command === 'logout') {
    handleLogout()
  }
}

function handleLogout() {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/admin/login')
}
</script>

<template>
  <el-container class="admin-layout">
    <el-aside width="220px" class="aside">
      <div class="logo">我的博客</div>
      <el-menu :default-active="activeMenu" router class="menu">
        <el-menu-item-group title="概览">
          <el-menu-item index="/admin/dashboard">
            <el-icon><Odometer /></el-icon>
            <span>仪表盘</span>
          </el-menu-item>
        </el-menu-item-group>
        <el-menu-item-group title="内容管理">
          <el-menu-item index="/admin/article">
            <el-icon><Document /></el-icon>
            <span>文章管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/project">
            <el-icon><Grid /></el-icon>
            <span>项目管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/category">
            <el-icon><FolderOpened /></el-icon>
            <span>分类管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/tag">
            <el-icon><CollectionTag /></el-icon>
            <span>标签管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/file">
            <el-icon><Files /></el-icon>
            <span>文件管理</span>
          </el-menu-item>
        </el-menu-item-group>
        <el-menu-item-group title="互动管理">
          <el-menu-item index="/admin/comment">
            <el-icon><ChatDotRound /></el-icon>
            <span>评论管理</span>
          </el-menu-item>
        </el-menu-item-group>
        <el-menu-item-group title="系统管理">
          <el-menu-item index="/admin/settings">
            <el-icon><Setting /></el-icon>
            <span>站点信息</span>
          </el-menu-item>
          <el-menu-item index="/admin/profile">
            <el-icon><User /></el-icon>
            <span>个人资料</span>
          </el-menu-item>
          <el-menu-item index="/admin/about">
            <el-icon><Postcard /></el-icon>
            <span>关于我</span>
          </el-menu-item>
        </el-menu-item-group>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>管理后台</el-breadcrumb-item>
          <el-breadcrumb-item>{{ route.meta.title || '' }}</el-breadcrumb-item>
        </el-breadcrumb>
        <div class="header-right">
          <a href="/" target="_blank" rel="noopener" class="front-link">前台首页</a>
          <el-dropdown trigger="click" @command="handleCommand">
          <div class="user-trigger">
            <el-avatar :size="32" :src="userStore.user?.avatar || ''">{{ avatarText }}</el-avatar>
            <span class="username">{{ userStore.user?.nickname || 'admin' }}</span>
            <el-icon class="arrow"><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <el-icon><User /></el-icon>个人资料
              </el-dropdown-item>
              <el-dropdown-item command="logout" divided>
                <el-icon><SwitchButton /></el-icon>退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        </div>
      </el-header>
      <el-main class="main">
        <div class="page-body">
          <router-view />
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.admin-layout {
  height: 100vh;
}
.aside {
  background: #fff;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
}
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 17px;
  border-bottom: 1px solid #f0f0f0;
}
.menu {
  border-right: none;
  flex: 1;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
}
.user-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  transition: background-color 0.2s;
  outline: none;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}
.front-link {
  color: #606266;
  font-size: 14px;
  text-decoration: none;
}
.front-link:hover {
  color: #409eff;
}
.menu :deep(.el-menu-item-group__title) {
  font-size: 12px;
  color: #909399;
}
.user-trigger:hover {
  background-color: #f5f7fa;
}
.user-trigger .arrow {
  color: #909399;
  font-size: 12px;
}
.username {
  color: #606266;
  font-size: 14px;
}
.main {
  background: #f5f7fa;
  overflow: auto;
}
/* 窄窗口下保留管理页最小宽度，避免表格被压缩裁切 */
.page-body {
  min-width: 1080px;
}
</style>