<script setup>
import { ref, computed, onMounted } from 'vue'
import { pageFrontArticles } from '@/api/article'

const list = ref([])

const grouped = computed(() => {
  const map = {}
  list.value.forEach((item) => {
    const year = (item.createTime || '').slice(0, 4)
    const month = (item.createTime || '').slice(5, 7)
    if (!map[year]) map[year] = {}
    if (!map[year][month]) map[year][month] = []
    map[year][month].push(item)
  })
  return map
})

onMounted(async () => {
  const data = await pageFrontArticles({ pageNum: 1, pageSize: 100 })
  list.value = data.records || []
})
</script>

<template>
  <el-card class="card">
    <template #header>
      <div class="card-head">
        <span class="accent"></span>
        <span class="header">文章归档（共 {{ list.length }} 篇）</span>
      </div>
    </template>
    <el-empty v-if="list.length === 0" description="暂无文章" :image-size="72" />
    <div v-else class="archive">
      <div v-for="(months, year) in grouped" :key="year" class="year-block">
        <div class="year">{{ year }}</div>
        <div v-for="(items, month) in months" :key="month" class="month-block">
          <div class="month">{{ month }} 月</div>
          <router-link
            v-for="item in items"
            :key="item.id"
            :to="`/article/${item.id}`"
            class="archive-item"
          >
            <span class="date">{{ (item.createTime || '').slice(5, 10) }}</span>
            <span class="title">{{ item.title }}</span>
          </router-link>
        </div>
      </div>
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
.year {
  font-size: 22px;
  font-weight: 700;
  color: var(--blog-primary);
  margin: 8px 0 12px;
}
.month-block {
  margin: 0 0 16px 12px;
  padding-left: 18px;
  border-left: 2px solid var(--blog-divider);
}
.month {
  color: var(--blog-text-light);
  font-size: 14px;
  margin-bottom: 8px;
}
.archive-item {
  display: flex;
  gap: 14px;
  padding: 7px 10px;
  margin-left: -10px;
  border-radius: var(--blog-radius-sm);
  color: inherit;
  text-decoration: none;
  transition: background-color 0.2s;
}
.archive-item:hover {
  background: var(--blog-primary-soft);
}
.archive-item:hover .title {
  color: var(--blog-primary);
}
.date {
  color: var(--blog-text-light);
  font-size: 13px;
  flex-shrink: 0;
}
.title {
  color: var(--blog-text);
  font-size: 14px;
  transition: color 0.2s;
}
</style>