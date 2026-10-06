<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProject } from '@/api/project'
import MarkdownView from '@/components/MarkdownView.vue'
import ProgressTag from '@/components/front/ProgressTag.vue'

const route = useRoute()
const router = useRouter()
const project = ref(null)
const loading = ref(true)

function techTags(techStack) {
  return (techStack || '')
    .split(',')
    .map((t) => t.trim())
    .filter(Boolean)
}

onMounted(async () => {
  try {
    project.value = await getProject(route.params.id)
  } catch (e) {
    router.replace('/projects')
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div v-loading="loading">
    <el-card v-if="project" class="card">
      <div class="header">
        <div class="title-row">
          <h1 class="name">{{ project.name }}</h1>
          <ProgressTag :progress="project.progress" />
        </div>
        <div class="links">
          <el-button
            v-if="project.githubUrl"
            tag="a"
            :href="project.githubUrl"
            target="_blank"
            type="primary"
            plain
            round
            size="small"
          >
            源码
          </el-button>
          <el-button
            v-if="project.demoUrl"
            tag="a"
            :href="project.demoUrl"
            target="_blank"
            round
            size="small"
          >
            在线演示
          </el-button>
        </div>
      </div>
      <div class="tech">
        <el-tag v-for="t in techTags(project.techStack)" :key="t" size="small" class="tech-tag">
          {{ t }}
        </el-tag>
      </div>
      <p class="desc">{{ project.description }}</p>
      <el-divider />
      <MarkdownView :text="project.content || ''" />
    </el-card>
  </div>
</template>

<style scoped>
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}
.title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}
.name {
  font-size: 25px;
  font-weight: 700;
  color: var(--blog-text);
}
.links {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}
.tech {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 14px;
}
.desc {
  margin-top: 14px;
  line-height: 1.8;
  color: var(--blog-text-light);
}
</style>