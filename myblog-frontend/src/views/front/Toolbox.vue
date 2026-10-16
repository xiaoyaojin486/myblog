<script setup>
import { computed, onMounted, ref } from 'vue'
import { Search, Tools } from '@element-plus/icons-vue'
import { getToolboxOverview } from '@/api/toolbox'
import { riskLabel } from '@/constants/toolRisk'

const loading = ref(false)
const categories = ref([])
const commands = ref([])
const total = ref(0)

/** 关键词（匹配命令原文 / 用途标题 / 说明） */
const keyword = ref('')
/** 当前分类（null = 全部分类） */
const activeCategory = ref(null)

/** 筛选结果：分类（AND）与关键词叠加 */
const filtered = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  return commands.value.filter((c) => {
    if (activeCategory.value !== null && c.categoryId !== activeCategory.value) return false
    if (!kw) return true
    return (
      (c.command || '').toLowerCase().includes(kw) ||
      (c.name || '').toLowerCase().includes(kw) ||
      (c.description || '').toLowerCase().includes(kw)
    )
  })
})

async function loadData() {
  loading.value = true
  try {
    const data = await getToolboxOverview()
    categories.value = data.categories || []
    commands.value = data.commands || []
    total.value = data.total || 0
  } catch (e) {
    // 错误提示已由拦截器处理
  } finally {
    loading.value = false
  }
}

/** 复制命令：优先 Clipboard API，非安全上下文回退 execCommand */
async function copyCommand(text) {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制')
  } catch (e) {
    fallbackCopy(text)
  }
}

function fallbackCopy(text) {
  const el = document.createElement('textarea')
  el.value = text
  el.setAttribute('readonly', '')
  el.style.position = 'absolute'
  el.style.left = '-9999px'
  document.body.appendChild(el)
  el.select()
  try {
    document.execCommand('copy')
    ElMessage.success('已复制')
  } catch (e) {
    ElMessage.error('复制失败，请手动复制')
  } finally {
    document.body.removeChild(el)
  }
}

onMounted(loadData)
</script>

<template>
  <div class="toolbox">
    <!-- 页头：标题 + 安全徽标 -->
    <div class="page-head">
      <div class="head-left">
        <div class="eyebrow">SRE TOOLKIT</div>
        <h2 class="title">运维工具箱</h2>
        <p class="subtitle">{{ total }} 条高频运维命令，支持搜索、分类筛选与一键复制。</p>
      </div>
      <div class="safe-badge">
        <span class="dot"></span>
        本地速查 · 不会自动执行
      </div>
    </div>

    <!-- 工具栏：搜索 + 分类 + 计数 -->
    <div class="toolbar">
      <div class="search-box">
        <el-icon class="search-icon"><Search /></el-icon>
        <input
          v-model="keyword"
          class="search-input"
          type="text"
          placeholder="搜索命令、用途或关键词，例如 kubectl、磁盘、日志"
        />
      </div>
      <el-select
        v-model="activeCategory"
        class="category-select"
        placeholder="全部分类"
        clearable
      >
        <el-option label="全部分类" :value="null" />
        <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
      <div class="counter">显示 {{ filtered.length }} / {{ total }} 条命令</div>
    </div>

    <!-- 命令卡片网格 -->
    <div v-loading="loading" class="grid-wrap">
      <el-empty
        v-if="!loading && filtered.length === 0"
        description="没有找到相关命令"
        :image-size="72"
      />
      <div v-else class="grid">
        <div v-for="c in filtered" :key="c.id" class="cmd-card">
          <div class="cmd-head">
            <code class="cmd-text" :title="c.command">{{ c.command }}</code>
            <span class="risk-badge" :class="`risk-${c.riskLevel}`">{{ riskLabel(c.riskLevel) }}</span>
          </div>
          <div class="cmd-body">
            <span class="cmd-icon">
              <el-icon><Tools /></el-icon>
            </span>
            <div class="cmd-info">
              <div class="cmd-name">{{ c.name }}</div>
              <div class="cmd-cat">{{ c.categoryName }}</div>
            </div>
            <button type="button" class="copy-btn" @click="copyCommand(c.command)">复制命令</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ===== 页头 ===== */
.page-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 18px;
  padding-left: 4px;
}
.eyebrow {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 1.5px;
  color: var(--blog-text-light);
}
.title {
  margin: 4px 0 6px;
  font-size: 24px;
  color: var(--blog-text);
}
.subtitle {
  font-size: 14px;
  color: var(--blog-text-light);
}
.safe-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 7px 16px;
  border-radius: var(--blog-radius-pill);
  background: var(--blog-success-soft);
  color: var(--blog-success);
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
}
.safe-badge .dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--blog-success);
}

/* ===== 工具栏 ===== */
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 20px;
  padding: 12px 14px;
  border: 1px solid var(--blog-border);
  border-radius: var(--blog-radius);
  background: var(--blog-card-bg);
  box-shadow: var(--blog-shadow);
}
.search-box {
  flex: 1 1 320px;
  min-width: 220px;
  display: flex;
  align-items: center;
  gap: 8px;
  height: 40px;
  padding: 0 14px;
  border-radius: var(--blog-radius-sm);
  background: var(--blog-surface-2);
  border: 1px solid var(--blog-divider);
  transition: border-color 0.2s;
}
.search-box:focus-within {
  border-color: var(--blog-primary);
}
.search-icon {
  font-size: 16px;
  color: var(--blog-text-light);
}
.search-input {
  flex: 1;
  min-width: 0;
  border: none;
  outline: none;
  background: transparent;
  color: var(--blog-text);
  font-size: 14px;
}
.search-input::placeholder {
  color: var(--blog-text-light);
}
.category-select {
  width: 160px;
  flex: 0 0 auto;
}
.counter {
  flex: 0 0 auto;
  font-size: 13px;
  color: var(--blog-text-light);
  white-space: nowrap;
}

/* ===== 卡片网格 ===== */
.grid-wrap {
  min-height: 160px;
}
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}
.cmd-card {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 14px 16px;
  border: 1px solid var(--blog-border);
  border-radius: var(--blog-radius);
  background: var(--blog-card-bg);
  box-shadow: var(--blog-shadow);
  transition: box-shadow 0.25s, transform 0.25s;
}
.cmd-card:hover {
  box-shadow: var(--blog-shadow-hover);
  transform: translateY(-2px);
}
.cmd-head {
  display: flex;
  align-items: center;
  gap: 10px;
}
.cmd-text {
  flex: 1;
  min-width: 0;
  display: block;
  padding: 8px 12px;
  border-radius: var(--blog-radius-sm);
  background: var(--blog-surface-2);
  color: var(--blog-text);
  font-family: 'JetBrains Mono', 'Fira Code', Consolas, Monaco, monospace;
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
/* 风险徽标：三档配色 */
.risk-badge {
  flex: 0 0 auto;
  padding: 3px 10px;
  border-radius: var(--blog-radius-pill);
  font-size: 12px;
  font-weight: 600;
}
.risk-0 {
  background: var(--blog-success-soft);
  color: var(--blog-success);
}
.risk-1 {
  background: var(--blog-warning-soft);
  color: var(--blog-warning);
}
.risk-2 {
  background: rgba(233, 79, 79, 0.14);
  color: #e94f4f;
}
.cmd-body {
  display: flex;
  align-items: center;
  gap: 12px;
}
.cmd-icon {
  flex: 0 0 auto;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: var(--blog-radius-sm);
  background: var(--blog-primary);
  color: #fff;
  font-size: 17px;
}
.cmd-info {
  flex: 1;
  min-width: 0;
}
.cmd-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--blog-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.cmd-cat {
  margin-top: 2px;
  font-size: 12px;
  color: var(--blog-text-light);
}
.copy-btn {
  flex: 0 0 auto;
  padding: 6px 14px;
  border: 1px solid var(--blog-divider);
  border-radius: var(--blog-radius-pill);
  background: var(--blog-surface-2);
  color: var(--blog-primary);
  font-size: 13px;
  cursor: pointer;
  transition: color 0.2s, border-color 0.2s, background-color 0.2s;
}
.copy-btn:hover {
  color: #fff;
  border-color: var(--blog-primary);
  background: var(--blog-primary);
}

@media (max-width: 720px) {
  .grid {
    grid-template-columns: 1fr;
  }
  .category-select {
    width: 100%;
  }
}
</style>
