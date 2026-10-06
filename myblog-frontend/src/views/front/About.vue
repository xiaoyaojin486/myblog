<script setup>
import { ref, computed, onMounted } from 'vue'
import { useConfigStore } from '@/store/config'
import { getPublicProfile } from '@/api/user'
import { getAbout } from '@/api/about'
import MarkdownView from '@/components/MarkdownView.vue'

const configStore = useConfigStore()
const profile = ref(null)
const about = ref(null)

const avatar = computed(() => profile.value?.avatar)
/** 联系方式属于博主个人信息，统一取「个人资料」（与首页左侧卡片一致） */
const email = computed(() => profile.value?.email)
const githubUrl = computed(() => profile.value?.github)

/** 未配置自我介绍时的兜底文案 */
const defaultContent = `你好，我是这个博客的博主，在这里记录技术与生活。

本站技术栈：Vue3 + Vite + Element Plus ｜ Spring Boot + MyBatis + MySQL ｜ ECharts ｜ 阿里云 OSS`

const content = computed(() => (about.value?.content || '').trim() || defaultContent)
const skills = computed(() => about.value?.skills || [])
const experiences = computed(() => about.value?.experiences || [])
const resumeUrl = computed(() => about.value?.resumeUrl || '')

onMounted(async () => {
  try {
    profile.value = await getPublicProfile()
  } catch (e) {
    // 错误提示已由拦截器处理
  }
  try {
    about.value = await getAbout()
  } catch (e) {
    // 错误提示已由拦截器处理
  }
})
</script>

<template>
  <div class="about-page">
    <el-card class="card">
      <div class="profile">
        <div class="avatar-ring">
          <img v-if="avatar" :src="avatar" class="avatar" alt="博主头像" />
          <el-avatar v-else :size="76">{{ (configStore.siteName || '博').slice(0, 1) }}</el-avatar>
        </div>
        <div class="intro">
          <h2 class="site-name">{{ configStore.siteName }}</h2>
          <p class="desc">{{ configStore.siteDescription }}</p>
          <p v-if="profile?.signature" class="signature">{{ profile.signature }}</p>
        </div>
      </div>
      <el-divider />
      <MarkdownView :text="content" />
      <div class="contact">
        <a v-if="email" :href="`mailto:${email}`" class="contact-btn">{{ email }}</a>
        <a v-if="githubUrl" :href="githubUrl" target="_blank" rel="noopener" class="contact-btn">
          GitHub
        </a>
        <a v-if="resumeUrl" :href="resumeUrl" target="_blank" rel="noopener" class="contact-btn">
          下载简历
        </a>
      </div>
    </el-card>

    <el-row v-if="skills.length || experiences.length" :gutter="20">
      <el-col v-if="skills.length" :xs="24" :sm="24" :md="12" class="col">
        <el-card class="card">
          <template #header>
            <div class="card-head">
              <span class="accent"></span>
              <span class="header">技能栈</span>
            </div>
          </template>
          <div v-for="(s, index) in skills" :key="s.id ?? index" class="skill">
            <div class="skill-top">
              <span class="skill-name">{{ s.name }}</span>
              <span v-if="s.levelLabel" class="skill-label">{{ s.levelLabel }}</span>
              <span class="skill-percent">{{ s.percent }}%</span>
            </div>
            <el-progress :percentage="s.percent" :show-text="false" :stroke-width="6" />
          </div>
        </el-card>
      </el-col>

      <el-col v-if="experiences.length" :xs="24" :sm="24" :md="12" class="col">
        <el-card class="card">
          <template #header>
            <div class="card-head">
              <span class="accent"></span>
              <span class="header">经历</span>
            </div>
          </template>
          <div class="exp-list">
            <div v-for="(e, index) in experiences" :key="e.id ?? index" class="exp">
              <span class="exp-dot"></span>
              <div class="exp-body">
                <div class="exp-title">{{ e.title }}</div>
                <div class="exp-meta">
                  <span v-if="e.organization">{{ e.organization }}</span>
                  <span v-if="e.startDate || e.endDate">{{ e.startDate }} ~ {{ e.endDate || '至今' }}</span>
                </div>
                <p v-if="e.description" class="exp-desc">{{ e.description }}</p>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.col {
  margin-bottom: 20px;
}
.about-page > .el-row {
  margin-top: 20px;
}
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
.header {
  color: var(--blog-text);
  font-weight: 600;
  font-size: 15px;
}
.profile {
  display: flex;
  align-items: center;
  gap: 18px;
}
.avatar-ring {
  padding: 3px;
  border-radius: 50%;
  line-height: 0;
  flex-shrink: 0;
  background: linear-gradient(135deg, var(--blog-primary), #a78bfa);
  box-shadow: 0 8px 22px var(--blog-primary-soft);
}
.avatar {
  width: 76px;
  height: 76px;
  border-radius: 50%;
  object-fit: cover;
  display: block;
}
.site-name {
  font-size: 20px;
  color: var(--blog-text);
}
.desc {
  color: var(--blog-text-light);
}
.signature {
  margin-top: 4px;
  color: var(--blog-text-light);
  font-size: 13px;
}
.contact {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 22px;
}
.contact-btn {
  padding: 6px 16px;
  border-radius: var(--blog-radius-pill);
  background: var(--blog-surface-2);
  color: var(--blog-primary);
  font-size: 13px;
  transition: background-color 0.2s;
}
.contact-btn:hover {
  background: var(--blog-primary-soft);
}
.skill {
  margin-bottom: 14px;
}
.skill:last-child {
  margin-bottom: 0;
}
.skill-top {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
  font-size: 13px;
}
.skill-name {
  color: var(--blog-text);
  font-weight: 600;
}
.skill-label {
  padding: 1px 9px;
  border-radius: var(--blog-radius-pill);
  background: var(--blog-chip-bg);
  color: var(--blog-primary);
  font-size: 12px;
}
.skill-percent {
  margin-left: auto;
  color: var(--blog-text-light);
}
.skill :deep(.el-progress-bar__outer) {
  background: var(--blog-surface-2);
}
.skill :deep(.el-progress-bar__inner) {
  background: linear-gradient(90deg, var(--blog-primary), #a78bfa);
}
/* 经历时间轴 */
.exp-list {
  position: relative;
}
.exp {
  position: relative;
  display: flex;
  gap: 12px;
  padding: 0 0 18px 0;
}
.exp:last-child {
  padding-bottom: 0;
}
.exp::before {
  content: '';
  position: absolute;
  left: 4px;
  top: 14px;
  bottom: -4px;
  width: 2px;
  background: var(--blog-divider);
}
.exp:last-child::before {
  display: none;
}
.exp-dot {
  position: relative;
  z-index: 1;
  width: 10px;
  height: 10px;
  margin-top: 5px;
  border-radius: 50%;
  flex-shrink: 0;
  background: var(--blog-primary);
  box-shadow: 0 0 0 3px var(--blog-primary-soft);
}
.exp-body {
  flex: 1;
  min-width: 0;
}
.exp-title {
  font-weight: 600;
  color: var(--blog-text);
}
.exp-meta {
  display: flex;
  gap: 14px;
  margin-top: 4px;
  font-size: 12px;
  color: var(--blog-text-light);
}
.exp-desc {
  margin-top: 6px;
  font-size: 13px;
  line-height: 1.8;
  color: var(--blog-text-light);
}
</style>