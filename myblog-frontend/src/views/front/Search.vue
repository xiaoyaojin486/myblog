<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { searchProviders } from '@/search/providers'

const route = useRoute()
const router = useRouter()

const keyword = ref(String(route.query.keyword || ''))
/** 已提交执行的关键字（与输入框区分开，避免边打字边搜） */
const activeKeyword = ref(keyword.value.trim())
const tab = ref('all')
const loading = ref(false)
/** 各 provider 的搜索结果：key -> { items, total, pageNum, pageSize } */
const store = reactive({})

const PAGE_SIZE = 6

const tabs = [{ key: 'all', label: '全部' }, ...searchProviders.map((p) => ({ key: p.key, label: p.label }))]

/** 当前页签需要展示的分组 */
const visibleProviders = computed(() =>
  tab.value === 'all' ? searchProviders : searchProviders.filter((p) => p.key === tab.value)
)

/** 命中总数（用于整体空状态判断） */
const totalHits = computed(() =>
  Object.values(store).reduce((sum, s) => sum + (s?.total || 0), 0)
)

async function runProvider(provider, pageNum) {
  const { records, total } = await provider.search(activeKeyword.value, {
    pageNum,
    pageSize: PAGE_SIZE
  })
  store[provider.key] = {
    items: records.map((r) => provider.normalize(r)),
    total,
    pageNum,
    pageSize: PAGE_SIZE
  }
}

async function runSearch() {
  if (!activeKeyword.value) {
    Object.keys(store).forEach((k) => delete store[k])
    return
  }
  loading.value = true
  try {
    const targets = visibleProviders.value
    await Promise.all(targets.map((p) => runProvider(p, 1)))
  } catch (e) {
    // 错误提示已由拦截器处理
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  const kw = keyword.value.trim()
  activeKeyword.value = kw
  tab.value = 'all'
  // 同步到地址栏，便于分享与刷新
  router.replace({ path: '/search', query: kw ? { keyword: kw } : {} })
  runSearch()
}

function switchTab(key) {
  tab.value = key
  runSearch()
}

function changePage(provider, pageNum) {
  runProvider(provider, pageNum)
}

// 地址栏关键字变化（如从页头搜索框跳转过来）时重新搜索
watch(
  () => route.query.keyword,
  (kw) => {
    const value = String(kw || '')
    if (value === activeKeyword.value && value === keyword.value.trim()) return
    keyword.value = value
    activeKeyword.value = value
    runSearch()
  }
)

onMounted(runSearch)
</script>

<template>
  <el-card class="card">
    <template #header>
      <div class="card-head">
        <span class="accent"></span>
        <span class="header">全站搜索</span>
      </div>
    </template>

    <div class="search-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索文章、项目…"
        clearable
        size="large"
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" size="large" round @click="handleSearch">搜索</el-button>
    </div>

    <div v-if="activeKeyword" class="tabs">
      <button
        v-for="t in tabs"
        :key="t.key"
        type="button"
        class="tab"
        :class="{ active: tab === t.key }"
        @click="switchTab(t.key)"
      >
        {{ t.label }}
        <span v-if="store[t.key]" class="tab-count">{{ store[t.key].total }}</span>
      </button>
    </div>

    <div v-if="activeKeyword" class="result-tip">
      关键字「<b>{{ activeKeyword }}</b>」共命中 {{ totalHits }} 条结果
    </div>

    <div v-loading="loading">
      <el-empty
        v-if="!activeKeyword"
        description="输入关键字，搜索全站的文章与项目"
        :image-size="72"
      />
      <el-empty
        v-else-if="!loading && totalHits === 0"
        description="没有找到相关内容，换个关键字试试"
        :image-size="72"
      />

      <section v-for="p in visibleProviders" v-show="activeKeyword" :key="p.key" class="section">
        <div v-if="store[p.key]" class="section-head">
          <span class="section-title">{{ p.label }}</span>
          <span class="section-count">共 {{ store[p.key].total }} 条</span>
        </div>

        <div v-if="store[p.key] && store[p.key].items.length" class="item-list">
          <router-link
            v-for="it in store[p.key].items"
            :key="`${it.type}-${it.id}`"
            :to="it.to"
            class="item"
          >
            <div class="item-main">
              <div class="item-title">{{ it.title }}</div>
              <div class="item-summary">{{ it.summary }}</div>
              <div class="item-meta">
                <span class="meta-cat">{{ it.tag }}</span>
                <span v-for="(m, i) in it.meta" :key="i" class="meta-item">{{ m }}</span>
              </div>
            </div>
            <img v-if="it.cover" :src="it.cover" class="item-cover" alt="" />
          </router-link>
        </div>
        <p v-else-if="store[p.key]" class="section-empty">{{ p.label }}中没有匹配内容</p>

        <div
          v-if="store[p.key] && store[p.key].total > store[p.key].pageSize"
          class="pagination"
        >
          <el-pagination
            v-model:current-page="store[p.key].pageNum"
            :page-size="store[p.key].pageSize"
            :total="store[p.key].total"
            layout="prev, pager, next"
            @current-change="(n) => changePage(p, n)"
          />
        </div>
      </section>
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
.search-bar {
  display: flex;
  gap: 10px;
}
.search-bar :deep(.el-input) {
  max-width: 460px;
}
/* 模块页签 */
.tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 18px 0 0;
}
.tab {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 16px;
  border: 1px solid var(--blog-divider);
  border-radius: var(--blog-radius-pill);
  background: var(--blog-surface-2);
  color: var(--blog-text-light);
  font-size: 13px;
  cursor: pointer;
  transition: color 0.2s, background-color 0.2s, border-color 0.2s;
}
.tab:hover {
  color: var(--blog-primary);
  border-color: var(--blog-primary);
}
.tab.active {
  color: #fff;
  background: var(--blog-primary);
  border-color: var(--blog-primary);
}
.tab-count {
  font-size: 12px;
  opacity: 0.85;
}
.result-tip {
  margin: 14px 0 4px;
  font-size: 13px;
  color: var(--blog-text-light);
}
.result-tip b {
  color: var(--blog-primary);
}
/* 分组 */
.section {
  margin-top: 18px;
}
.section-head {
  display: flex;
  align-items: baseline;
  gap: 10px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--blog-divider);
}
.section-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--blog-text);
}
.section-count {
  font-size: 12px;
  color: var(--blog-text-light);
}
.section-empty {
  padding: 18px 2px;
  font-size: 13px;
  color: var(--blog-text-light);
}
.item-list {
  display: flex;
  flex-direction: column;
}
.item {
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
.item:last-child {
  border-bottom: none;
}
.item:hover {
  background: var(--blog-primary-soft);
}
.item-main {
  flex: 1;
  min-width: 0;
}
.item-cover {
  width: 140px;
  height: 92px;
  object-fit: cover;
  border-radius: 10px;
  flex-shrink: 0;
  transition: transform 0.3s;
}
.item:hover .item-cover {
  transform: scale(1.03);
}
.item-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--blog-text);
  transition: color 0.2s;
}
.item:hover .item-title {
  color: var(--blog-primary);
}
.item-summary {
  margin: 6px 0 8px;
  font-size: 13px;
  color: var(--blog-text-light);
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.item-meta {
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
}
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}
</style>