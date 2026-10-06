<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import {
  ArrowLeft,
  ArrowRight,
  Calendar,
  Notebook,
  Clock,
  View,
  ChatDotRound,
  Star
} from '@element-plus/icons-vue'
import { hotArticles, pageFrontArticles } from '@/api/article'
import { listCategories } from '@/api/category'
import { listTags } from '@/api/tag'
import { getPublicProfile } from '@/api/user'
import { useConfigStore } from '@/store/config'

const configStore = useConfigStore()
const feed = ref([])
const feedTotal = ref(0)
const feedQuery = reactive({ pageNum: 1, pageSize: 6 })
const hot = ref([])
const hotRange = ref('total')
const profile = ref(null)
const stats = reactive({ articles: 0, categories: 0, tags: 0 })

const siteName = computed(() => configStore.siteName)
const siteDescription = computed(() => configStore.siteDescription)
const avatar = computed(() => profile.value?.avatar)
const email = computed(() => profile.value?.email)
const github = computed(() => profile.value?.github)
/** 博主昵称（个人资料），作为卡片主名称；未配置时退回站点名 */
const displayName = computed(() => profile.value?.nickname || siteName.value)

/* ===== 日历（周一起始；今天深色圆角高亮；过去日期淡化；非本月留空） ===== */
const weekLabels = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
const calendarDate = ref(new Date())
const todayIso = (() => {
  const d = new Date()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${d.getFullYear()}-${m}-${day}`
})()

const calendarTitle = computed(() => {
  const d = calendarDate.value
  return `${d.getFullYear()}年${d.getMonth() + 1}月`
})

function changeMonth(offset) {
  const d = new Date(calendarDate.value)
  d.setDate(1)
  d.setMonth(d.getMonth() + offset)
  calendarDate.value = d
}

/** 仅用于本月日期：今天高亮 / 过去淡化 */
function cellClass(data) {
  if (data.day === todayIso) return 'today'
  return data.day < todayIso ? 'muted' : ''
}

/** 按 250 字/分钟估算阅读时长（至少 1 分钟） */
function readMinutes(wordCount) {
  return Math.max(1, Math.ceil((wordCount || 0) / 250))
}

async function loadHot() {
  try {
    hot.value = await hotArticles(5, hotRange.value)
  } catch (e) {
    // 错误提示已由拦截器处理
  }
}

/** 首页文章流（已发布，分页） */
async function loadFeed() {
  try {
    const data = await pageFrontArticles({ ...feedQuery })
    feed.value = data.records || []
    feedTotal.value = data.total || 0
  } catch (e) {
    // 错误提示已由拦截器处理
  }
}

onMounted(async () => {
  loadFeed()
  loadHot()
  try {
    profile.value = await getPublicProfile() // 个人博客信息（头像/邮箱/GitHub 来自「个人资料」）
  } catch (e) {
    // 错误提示已由拦截器处理
  }
  try {
    const [page, cats, tags] = await Promise.all([
      pageFrontArticles({ pageNum: 1, pageSize: 1 }),
      listCategories(),
      listTags()
    ])
    stats.articles = page.total || 0
    stats.categories = cats.length
    stats.tags = tags.length
  } catch (e) {
    // 错误提示已由拦截器处理
  }
})
</script>

<template>
  <div class="home">
    <el-row :gutter="20">
      <!-- 左侧：个人博客信息（头像/名称/描述/联系方式/统计） -->
      <el-col :xs="24" :sm="24" :md="5" class="col">
        <el-card class="card profile-card">
          <div class="profile">
            <div class="avatar-ring">
              <el-avatar :size="76" :src="avatar || ''">{{ (displayName || '博').slice(0, 1) }}</el-avatar>
            </div>
            <h2 class="site-name">{{ displayName }}</h2>
            <p class="site-desc">
              {{ siteName }}<template v-if="siteDescription"> · {{ siteDescription }}</template>
            </p>
            <div class="contacts">
              <a v-if="email" :href="`mailto:${email}`" class="contact-item" :title="email">
                <svg
                  class="contact-icon"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="1.8"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  aria-hidden="true"
                >
                  <rect x="2.5" y="4.5" width="19" height="15" rx="2.5" />
                  <path d="M3 7l9 6.5L21 7" />
                </svg>
                <span>邮箱</span>
              </a>
              <a
                v-if="github"
                :href="github"
                target="_blank"
                rel="noopener"
                class="contact-item"
                :title="github"
              >
                <svg class="contact-icon" viewBox="0 0 16 16" fill="currentColor" aria-hidden="true">
                  <path
                    d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27s1.36.09 2 .27c1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.01 8.01 0 0 0 16 8c0-4.42-3.58-8-8-8Z"
                  />
                </svg>
                <span>GitHub</span>
              </a>
              <span v-if="!email && !github" class="contact-empty">邮箱 / GitHub 待配置</span>
            </div>
            <div class="stats">
              <div class="stat">
                <div class="stat-num">{{ stats.articles }}</div>
                <div class="stat-label">文章</div>
              </div>
              <div class="stat">
                <div class="stat-num">{{ stats.categories }}</div>
                <div class="stat-label">分类</div>
              </div>
              <div class="stat">
                <div class="stat-num">{{ stats.tags }}</div>
                <div class="stat-label">标签</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 中间：最新动态 -->
      <el-col :xs="24" :sm="24" :md="13" class="col">
        <el-card class="card feed-card">
          <template #header>
            <div class="card-head">
              <span class="accent"></span>
              <span class="card-title">最新动态</span>
            </div>
          </template>
          <el-empty v-if="feed.length === 0" description="暂无文章" :image-size="72" />
          <div v-else class="post-list">
            <router-link v-for="item in feed" :key="item.id" :to="`/article/${item.id}`" class="post">
              <div class="post-main">
                <div class="post-title">{{ item.title }}</div>
                <div class="post-summary">{{ item.summary }}</div>
                <div class="post-meta">
                  <span class="meta-cat">{{ item.categoryName || '未分类' }}</span>
                  <span class="meta-item">
                    <el-icon><Calendar /></el-icon>{{ (item.createTime || '').slice(5, 16) }}
                  </span>
                  <span class="meta-item">
                    <el-icon><Notebook /></el-icon>{{ item.wordCount || 0 }} 字
                  </span>
                  <span class="meta-item">
                    <el-icon><Clock /></el-icon>{{ readMinutes(item.wordCount) }} 分钟
                  </span>
                  <span class="meta-item">
                    <el-icon><View /></el-icon>{{ item.viewCount || 0 }}
                  </span>
                  <span class="meta-item">
                    <el-icon><ChatDotRound /></el-icon>{{ item.commentCount || 0 }}
                  </span>
                  <span class="meta-item">
                    <el-icon><Star /></el-icon>{{ item.likeCount || 0 }}
                  </span>
                </div>
              </div>
              <img v-if="item.coverImage" :src="item.coverImage" class="post-cover" alt="文章封面" />
            </router-link>
          </div>
          <div v-if="feedTotal > feedQuery.pageSize" class="pagination">
            <el-pagination
              v-model:current-page="feedQuery.pageNum"
              :page-size="feedQuery.pageSize"
              :total="feedTotal"
              layout="prev, pager, next"
              @current-change="loadFeed"
            />
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：热门文章 + 日历 -->
      <el-col :xs="24" :sm="24" :md="6" class="col">
        <el-card class="card side-card">
          <template #header>
            <div class="hot-header">
              <div class="card-head">
                <span class="accent"></span>
                <span class="card-title">热门文章</span>
              </div>
              <el-radio-group v-model="hotRange" size="small" @change="loadHot">
                <el-radio-button value="total">总榜</el-radio-button>
                <el-radio-button value="week">周榜</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <el-empty v-if="hot.length === 0" description="暂无数据" :image-size="72" />
          <ul v-else class="hot-list">
            <li v-for="(item, index) in hot" :key="item.id">
              <router-link :to="`/article/${item.id}`" class="hot-item">
                <span class="rank" :class="{ top: index < 3 }">{{ index + 1 }}</span>
                <span class="hot-title">{{ item.title }}</span>
                <span class="views">{{ item.viewCount }} 阅读</span>
              </router-link>
            </li>
          </ul>
        </el-card>

        <el-card class="card side-card calendar-card">
          <template #header>
            <div class="card-head">
              <span class="accent"></span>
              <span class="card-title">日历</span>
            </div>
          </template>
          <el-calendar v-model="calendarDate">
            <template #header>
              <div class="cal-header">
                <el-icon class="cal-nav" @click="changeMonth(-1)"><ArrowLeft /></el-icon>
                <span class="cal-title">{{ calendarTitle }}</span>
                <el-icon class="cal-nav" @click="changeMonth(1)"><ArrowRight /></el-icon>
              </div>
              <div class="cal-weeks">
                <span v-for="w in weekLabels" :key="w">{{ w }}</span>
              </div>
            </template>
            <template #date-cell="{ data }">
              <div v-if="data.type === 'current-month'" class="cal-day" :class="cellClass(data)">
                {{ Number(data.day.split('-')[2]) }}
              </div>
            </template>
          </el-calendar>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.col {
  margin-bottom: 20px;
}

/* 卡片小标题（竖条 + 文字） */
.card-head {
  display: flex;
  align-items: center;
  gap: 8px;
}
.accent {
  width: 4px;
  height: 15px;
  border-radius: 2px;
  background: linear-gradient(180deg, var(--blog-primary), #a78bfa);
}
.card-title {
  color: var(--blog-text);
  font-weight: 600;
  font-size: 15px;
}

/* ===== 左侧：个人博客信息 ===== */
.profile-card :deep(.el-card__body) {
  padding: 28px 20px 20px;
}
.profile {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  text-align: center;
}
.avatar-ring {
  padding: 3px;
  border-radius: 50%;
  line-height: 0;
  background: linear-gradient(135deg, var(--blog-primary), #a78bfa);
  box-shadow: 0 8px 22px var(--blog-primary-soft);
}
.site-name {
  font-size: 18px;
  color: var(--blog-text);
  margin-top: 8px;
}
.site-desc {
  color: var(--blog-text-light);
  font-size: 13px;
}
.contacts {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;
  gap: 8px 14px;
  margin-top: 6px;
}
.contact-item {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 12px;
  border-radius: var(--blog-radius-pill);
  background: var(--blog-surface-2);
  color: var(--blog-text-light);
  font-size: 13px;
  text-decoration: none;
  transition: color 0.2s, background-color 0.2s;
}
.contact-item:hover {
  color: var(--blog-primary);
  background: var(--blog-primary-soft);
}
.contact-icon {
  width: 15px;
  height: 15px;
  flex-shrink: 0;
}
.contact-empty {
  color: var(--blog-text-light);
  font-size: 13px;
}
.stats {
  display: flex;
  width: 100%;
  margin-top: 18px;
  padding-top: 16px;
  border-top: 1px solid var(--blog-divider);
}
.stat {
  flex: 1;
}
.stat-num {
  font-size: 20px;
  font-weight: 700;
  color: var(--blog-text);
}
.stat-label {
  margin-top: 2px;
  font-size: 12px;
  color: var(--blog-text-light);
}

/* ===== 中间：最新动态 ===== */
.post-list {
  display: flex;
  flex-direction: column;
}
.post {
  display: flex;
  gap: 14px;
  padding: 14px;
  margin: 0 -6px;
  border-radius: var(--blog-radius-sm);
  border-bottom: 1px solid var(--blog-divider);
  color: inherit;
  text-decoration: none;
  transition: background-color 0.2s;
}
.post:last-child {
  border-bottom: none;
}
.post:hover {
  background: var(--blog-primary-soft);
}
.post-main {
  flex: 1;
  min-width: 0;
}
.post-cover {
  width: 120px;
  height: 80px;
  object-fit: cover;
  border-radius: 10px;
  flex-shrink: 0;
  transition: transform 0.3s;
}
.post:hover .post-cover {
  transform: scale(1.03);
}
.post-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--blog-text);
  transition: color 0.2s;
}
.post:hover .post-title {
  color: var(--blog-primary);
}
.post-summary {
  margin: 6px 0 8px;
  color: var(--blog-text-light);
  font-size: 13px;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.post-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px 12px;
  font-size: 12px;
  color: var(--blog-text-light);
}
.meta-cat {
  padding: 2px 9px;
  border-radius: var(--blog-radius-pill);
  background: var(--blog-chip-bg);
  color: var(--blog-primary);
}
.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 3px;
}
.meta-item .el-icon {
  font-size: 13px;
}
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 18px;
}

/* ===== 右侧：热门文章 + 日历 ===== */
.side-card:not(:last-child) {
  margin-bottom: 20px;
}
.hot-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}
.hot-list {
  list-style: none;
}
.hot-list li {
  border-bottom: 1px solid var(--blog-divider);
}
.hot-list li:last-child {
  border-bottom: none;
}
.hot-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 9px 2px;
  color: inherit;
  text-decoration: none;
}
.hot-item:hover .hot-title {
  color: var(--blog-primary);
}
.rank {
  width: 20px;
  height: 20px;
  line-height: 20px;
  text-align: center;
  border-radius: 6px;
  font-size: 12px;
  background: var(--blog-surface-2);
  color: var(--blog-text-light);
  flex-shrink: 0;
}
.rank.top {
  background: linear-gradient(135deg, var(--blog-primary), #a78bfa);
  color: #fff;
}
.hot-title {
  flex: 1;
  min-width: 0;
  font-size: 13px;
  line-height: 1.5;
  color: var(--blog-text);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.views {
  font-size: 12px;
  color: var(--blog-text-light);
  flex-shrink: 0;
}

/* ===== 日历（无网格线 / 周一起始 / 今天深色圆角标 / 过去淡化） ===== */
.cal-weeks {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  text-align: center;
  font-size: 12px;
  color: var(--blog-text-light);
  padding-bottom: 6px;
}
.calendar-card :deep(.el-calendar__header) {
  display: block; /* 覆盖 EP 的 flex：导航行与星期行上下排列 */
  padding: 0 0 4px;
  border-bottom: none;
}
.calendar-card :deep(.el-calendar__body) {
  padding: 0;
}
.calendar-card :deep(.el-calendar-table thead) {
  display: none;
}
.calendar-card :deep(.el-calendar-table td) {
  border: none;
  padding: 1px 0;
}
/* 非本月日期留空（占位但不可见） */
.calendar-card :deep(.el-calendar-table td.prev),
.calendar-card :deep(.el-calendar-table td.next) {
  visibility: hidden;
}
.calendar-card :deep(.el-calendar-table tr:first-child td),
.calendar-card :deep(.el-calendar-table tr td:first-child) {
  border: none;
}
.calendar-card :deep(.el-calendar-table td.is-selected) {
  background-color: transparent;
}
.calendar-card :deep(.el-calendar-table .el-calendar-day) {
  width: 26px;
  height: 30px;
  margin: 0 auto;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
}
.calendar-card :deep(.el-calendar-table .el-calendar-day:hover) {
  background-color: var(--blog-primary-soft);
}
.cal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 0 4px;
}
.cal-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--blog-text);
}
.cal-nav {
  cursor: pointer;
  color: var(--blog-text-light);
  font-size: 14px;
}
.cal-nav:hover {
  color: var(--blog-primary);
}
.cal-day {
  width: 26px;
  height: 26px;
  line-height: 26px;
  text-align: center;
  border-radius: 8px;
  font-size: 12px;
  color: var(--blog-text);
}
.cal-day.muted {
  color: var(--blog-text-light);
  opacity: 0.55;
}
.cal-day.today {
  background: var(--blog-text);
  color: var(--blog-card-solid);
  font-weight: 600;
}
</style>