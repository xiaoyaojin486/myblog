<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { Calendar, Notebook, View, ChatDotRound } from '@element-plus/icons-vue'
import { pageFrontArticles } from '@/api/article'
import { listTags } from '@/api/tag'

const route = useRoute()
const tags = ref([])
const list = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, tagId: Number(route.params.id) })

const name = computed(() => tags.value.find((t) => t.id === query.tagId)?.name || '标签')

async function loadData() {
  const data = await pageFrontArticles(query)
  list.value = data.records || []
  total.value = data.total || 0
}

watch(
  () => route.params.id,
  (id) => {
    query.tagId = Number(id)
    query.pageNum = 1
    loadData()
  }
)

onMounted(async () => {
  tags.value = await listTags()
  loadData()
})
</script>

<template>
  <el-card class="card">
    <template #header>
      <div class="card-head">
        <span class="accent"></span>
        <span class="header">标签：{{ name }}（{{ total }} 篇）</span>
      </div>
    </template>
    <el-empty v-if="list.length === 0" description="该标签暂无文章" :image-size="72" />
    <div v-else class="post-list">
      <router-link v-for="item in list" :key="item.id" :to="`/article/${item.id}`" class="post">
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
              <el-icon><View /></el-icon>{{ item.viewCount || 0 }}
            </span>
            <span class="meta-item">
              <el-icon><ChatDotRound /></el-icon>{{ item.commentCount || 0 }}
            </span>
          </div>
        </div>
        <img v-if="item.coverImage" :src="item.coverImage" class="post-cover" alt="文章封面" />
      </router-link>
    </div>
    <div v-if="total > query.pageSize" class="pagination">
      <el-pagination
        v-model:current-page="query.pageNum"
        :page-size="query.pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadData"
      />
    </div>
  </el-card>
</template>

<style scoped>
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
  width: 140px;
  height: 92px;
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
  font-size: 13px;
  color: var(--blog-text-light);
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
</style>