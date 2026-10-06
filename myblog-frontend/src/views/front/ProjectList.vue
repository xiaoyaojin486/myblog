<script setup>
import { ref, onMounted } from 'vue'
import { listProjects } from '@/api/project'
import { PROJECT_PROGRESS } from '@/constants/projectProgress'
import ProgressTag from '@/components/front/ProgressTag.vue'

const list = ref([])
const loading = ref(false)
/** 当前筛选的进度（null = 全部） */
const activeProgress = ref(null)

const filters = [
  { label: '全部', value: null },
  ...PROJECT_PROGRESS.map((p) => ({ label: p.label, value: p.value }))
]

function techTags(techStack) {
  return (techStack || '')
    .split(',')
    .map((t) => t.trim())
    .filter(Boolean)
}

async function loadList() {
  loading.value = true
  try {
    list.value = await listProjects(activeProgress.value)
  } catch (e) {
    // 错误提示已由拦截器处理
  } finally {
    loading.value = false
  }
}

function selectProgress(value) {
  if (activeProgress.value === value) return
  activeProgress.value = value
  loadList()
}

onMounted(loadList)
</script>

<template>
  <div class="project-list">
    <div class="page-head">
      <span class="accent"></span>
      <h2 class="page-title">我的项目</h2>
    </div>

    <div class="filters">
      <button
        v-for="f in filters"
        :key="f.value === null ? 'all' : f.value"
        type="button"
        class="filter-tab"
        :class="{ active: activeProgress === f.value }"
        @click="selectProgress(f.value)"
      >
        {{ f.label }}
      </button>
    </div>

    <div v-loading="loading">
      <el-empty
        v-if="list.length === 0"
        :description="activeProgress === null ? '暂无项目' : '该状态下暂无项目'"
        :image-size="72"
      />
      <el-row v-else :gutter="20">
        <el-col v-for="p in list" :key="p.id" :xs="24" :sm="12" :md="8" class="col">
          <router-link :to="`/project/${p.id}`" class="project-link">
            <el-card class="project-card">
              <img v-if="p.coverImage" :src="p.coverImage" class="cover" alt="项目封面" />
              <div v-else class="cover placeholder">{{ p.name.slice(0, 1) }}</div>
              <div class="name-row">
                <span class="name">{{ p.name }}</span>
                <ProgressTag :progress="p.progress" />
              </div>
              <div class="desc">{{ p.description }}</div>
              <div class="tech">
                <el-tag v-for="t in techTags(p.techStack)" :key="t" size="small" class="tech-tag">
                  {{ t }}
                </el-tag>
              </div>
            </el-card>
          </router-link>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<style scoped>
.page-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  padding-left: 4px;
}
.accent {
  width: 4px;
  height: 20px;
  border-radius: 2px;
  background: linear-gradient(180deg, var(--blog-primary), #a78bfa);
}
.page-title {
  color: var(--blog-text);
  font-size: 20px;
}
/* 进度筛选：胶囊按钮，选中态用主题色描边 */
.filters {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 20px;
  padding-left: 4px;
}
.filter-tab {
  padding: 6px 20px;
  border: 1px solid var(--blog-divider);
  border-radius: var(--blog-radius-pill);
  background: var(--blog-surface-2);
  color: var(--blog-text-light);
  font-size: 14px;
  cursor: pointer;
  transition: color 0.2s, border-color 0.2s, background-color 0.2s;
}
.filter-tab:hover {
  color: var(--blog-primary);
  border-color: var(--blog-primary);
}
.filter-tab.active {
  color: var(--blog-primary);
  border-color: var(--blog-primary);
  background: var(--blog-primary-soft);
}
.col {
  margin-bottom: 20px;
}
.project-link {
  display: block;
  text-decoration: none;
}
.project-card {
  transition: transform 0.25s, box-shadow 0.25s;
}
.project-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--blog-shadow-hover);
}
.project-card :deep(.el-card__body) {
  padding: 14px;
}
.cover {
  width: 100%;
  height: 150px;
  object-fit: cover;
  border-radius: var(--blog-radius-sm);
  display: block;
}
.cover.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  font-weight: 700;
  background: var(--blog-primary-soft);
  color: var(--blog-primary);
}
.name-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 14px;
}
.name {
  flex: 1;
  min-width: 0;
  font-size: 17px;
  font-weight: 600;
  color: var(--blog-text);
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.desc {
  margin: 8px 0;
  font-size: 13px;
  line-height: 1.6;
  color: var(--blog-text-light);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.tech {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
</style>