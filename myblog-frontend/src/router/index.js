import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  // ===== 前台（无需登录，共用 Header/Footer 布局）=====
  {
    path: '/',
    component: () => import('@/views/front/FrontLayout.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('@/views/front/Home.vue') },
      { path: 'article/:id', name: 'ArticleDetail', component: () => import('@/views/front/ArticleDetail.vue') },
      { path: 'projects', name: 'ProjectList', component: () => import('@/views/front/ProjectList.vue') },
      { path: 'project/:id', name: 'ProjectDetail', component: () => import('@/views/front/ProjectDetail.vue') },
      { path: 'category/:id', name: 'CategoryPage', component: () => import('@/views/front/Category.vue') },
      { path: 'tag/:id', name: 'TagPage', component: () => import('@/views/front/Tag.vue') },
      { path: 'archive', name: 'Archive', component: () => import('@/views/front/Archive.vue') },
      { path: 'search', name: 'Search', component: () => import('@/views/front/Search.vue') },
      { path: 'about', name: 'About', component: () => import('@/views/front/About.vue') }
    ]
  },

  // ===== 后台（需登录）=====
  { path: '/admin/login', name: 'AdminLogin', component: () => import('@/views/admin/Login.vue') },
  {
    path: '/admin',
    component: () => import('@/views/admin/Layout.vue'),
    meta: { requiresAuth: true },
    redirect: '/admin/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '仪表盘' }
      },
      {
        path: 'article',
        name: 'AdminArticleList',
        component: () => import('@/views/admin/ArticleList.vue'),
        meta: { title: '文章管理' }
      },
      {
        path: 'article/edit/:id?',
        name: 'AdminArticleEdit',
        component: () => import('@/views/admin/ArticleEdit.vue'),
        meta: { title: '文章编辑' }
      },
      {
        path: 'project',
        name: 'AdminProject',
        component: () => import('@/views/admin/Project.vue'),
        meta: { title: '项目管理' }
      },
      {
        path: 'category',
        name: 'AdminCategory',
        component: () => import('@/views/admin/Category.vue'),
        meta: { title: '分类管理' }
      },
      {
        path: 'tag',
        name: 'AdminTag',
        component: () => import('@/views/admin/Tag.vue'),
        meta: { title: '标签管理' }
      },
      {
        path: 'file',
        name: 'AdminFile',
        component: () => import('@/views/admin/File.vue'),
        meta: { title: '文件管理' }
      },
      {
        path: 'comment',
        name: 'AdminComment',
        component: () => import('@/views/admin/Comment.vue'),
        meta: { title: '评论管理' }
      },
      {
        path: 'settings',
        name: 'AdminSettings',
        component: () => import('@/views/admin/Settings.vue'),
        meta: { title: '站点信息' }
      },
      {
        path: 'profile',
        name: 'AdminProfile',
        component: () => import('@/views/admin/Profile.vue'),
        meta: { title: '个人资料' }
      },
      {
        path: 'about',
        name: 'AdminAbout',
        component: () => import('@/views/admin/About.vue'),
        meta: { title: '关于我' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：后台需登录（校验本地 token）；已登录访问登录页自动进入后台
router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    return { path: '/admin/login', query: { redirect: to.fullPath } }
  }
  if (to.path === '/admin/login' && token) {
    return { path: '/admin/dashboard' }
  }
})

export default router