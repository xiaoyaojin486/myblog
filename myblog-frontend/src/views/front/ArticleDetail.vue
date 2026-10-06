<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Star, StarFilled, Calendar, View, ChatDotRound, List, ArrowLeft } from '@element-plus/icons-vue'
import { getArticleDetail, likeArticle } from '@/api/article'
import { listComments, createComment } from '@/api/comment'
import MarkdownView from '@/components/MarkdownView.vue'
import CommentItem from '@/components/front/CommentItem.vue'

const route = useRoute()
const router = useRouter()
const article = ref(null)
const loading = ref(true)
const liked = ref(false)
const liking = ref(false)
const comments = ref([])
const replyTo = ref(null)
const commentFormRef = ref(null)
const commentSubmitting = ref(false)
const commentForm = reactive({ nickname: '', email: '', content: '' })
const commentRules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  content: [{ required: true, message: '请输入评论内容', trigger: 'blur' }]
}

const commentCount = computed(() => countAll(comments.value))

function countAll(list) {
  return (list || []).reduce((sum, c) => sum + 1 + countAll(c.children), 0)
}

async function loadComments() {
  try {
    comments.value = await listComments(route.params.id)
  } catch (e) {
    // 错误提示已由拦截器处理
  }
}

async function handleLike() {
  if (liked.value || liking.value) return
  liking.value = true
  try {
    const res = await likeArticle(article.value.id)
    article.value.likeCount = res.likeCount
    liked.value = true
    if (res.counted) {
      ElMessage.success('感谢点赞！')
    } else {
      ElMessage.info('您已经点过赞啦')
    }
  } catch (e) {
    // 错误提示已由拦截器处理
  } finally {
    liking.value = false
  }
}

function startReply(comment) {
  replyTo.value = comment
}

function cancelReply() {
  replyTo.value = null
}

async function submitComment() {
  try {
    await commentFormRef.value.validate()
  } catch (e) {
    return
  }
  commentSubmitting.value = true
  try {
    await createComment({
      articleId: article.value.id,
      nickname: commentForm.nickname,
      email: commentForm.email || undefined,
      content: commentForm.content,
      parentId: replyTo.value?.id
    })
    ElMessage.success('评论提交成功，审核通过后展示')
    commentForm.content = ''
    replyTo.value = null
  } catch (e) {
    // 错误提示已由拦截器处理
  } finally {
    commentSubmitting.value = false
  }
}

/* ===== 文章目录（TOC）：从已渲染正文提取标题树，滚动高亮当前章节 ===== */
const bodyRef = ref(null)
const toc = ref([])
const activeId = ref('')
/** 吸顶导航高度（58px）+ 余量，跳转与高亮判定都要补偿 */
const HEADER_OFFSET = 76
let rafId = 0

/** 重建目录：把锚点 id 写回正文 DOM，供点击跳转 */
function buildToc() {
  const root = bodyRef.value
  if (!root) return
  const headings = Array.from(root.querySelectorAll('h1, h2, h3, h4, h5, h6'))
  const items = headings.map((el, i) => {
    el.id = `article-heading-${i}`
    return { id: el.id, level: Number(el.tagName.slice(1)), text: el.textContent.trim() }
  })
  const base = items.length ? Math.min(...items.map((t) => t.level)) : 2
  // 以最小标题层级为基准缩进，最多缩进两级
  items.forEach((t) => {
    t.depth = Math.min(t.level - base, 2)
  })
  toc.value = items
  syncActive()
}

/** 高亮当前所在章节 */
function syncActive() {
  if (!toc.value.length) return
  // 已滚到底部时，末个标题可能永远到不了顶部，直接点亮最后一项
  const atBottom = window.innerHeight + window.scrollY >= document.documentElement.scrollHeight - 2
  if (atBottom) {
    activeId.value = toc.value[toc.value.length - 1].id
    return
  }
  let current = toc.value[0].id
  for (const item of toc.value) {
    const el = document.getElementById(item.id)
    if (el && el.getBoundingClientRect().top - HEADER_OFFSET <= 0) {
      current = item.id
    } else {
      break
    }
  }
  activeId.value = current
}

function onScroll() {
  if (rafId) return
  rafId = requestAnimationFrame(() => {
    rafId = 0
    syncActive()
  })
}

/** 跳转到指定标题（补偿吸顶导航高度，避免标题被遮住） */
function goHeading(id) {
  const el = document.getElementById(id)
  if (!el) return
  activeId.value = id
  window.scrollTo({
    top: el.getBoundingClientRect().top + window.scrollY - HEADER_OFFSET,
    behavior: 'smooth'
  })
}

onMounted(async () => {
  try {
    article.value = await getArticleDetail(route.params.id)
  } catch (e) {
    router.replace('/')
    return
  } finally {
    loading.value = false
  }
  await loadComments()
  await nextTick() // 等正文 Markdown 渲染完再提取标题
  buildToc()
  window.addEventListener('scroll', onScroll, { passive: true })
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', onScroll)
  if (rafId) cancelAnimationFrame(rafId)
})
</script>

<template>
  <div v-loading="loading" class="article-detail">
    <el-row :gutter="20">
      <!-- 左栏：正文 + 评论 -->
      <el-col :xs="24" :sm="24" :md="18" class="col">
        <el-card v-if="article" class="card">
          <h1 class="title">{{ article.title }}</h1>
          <div class="meta">
            <router-link
              v-if="article.categoryId"
              :to="`/category/${article.categoryId}`"
              class="meta-cat"
            >
              {{ article.categoryName || '未分类' }}
            </router-link>
            <span class="meta-item">
              <el-icon><Calendar /></el-icon>{{ (article.createTime || '').slice(5, 16) }}
            </span>
            <span class="meta-item">
              <el-icon><View /></el-icon>{{ article.viewCount }} 阅读
            </span>
            <span class="meta-item">
              <el-icon><ChatDotRound /></el-icon>{{ commentCount }} 评论
            </span>
          </div>
          <el-divider />
          <!-- 目录锚点由此容器内的标题生成 -->
          <div ref="bodyRef" class="article-body">
            <MarkdownView :text="article.content || ''" />
          </div>
          <div class="tags">
            <template v-if="article.tags && article.tags.length">
              <el-tag v-for="tag in article.tags" :key="tag" class="tag">{{ tag }}</el-tag>
            </template>
            <el-button
              class="like-btn"
              :type="liked ? 'success' : 'primary'"
              :plain="!liked"
              :disabled="liked"
              :loading="liking"
              round
              @click="handleLike"
            >
              <el-icon>
                <StarFilled v-if="liked" />
                <Star v-else />
              </el-icon>
              {{ liked ? '已赞' : '点赞' }} {{ article.likeCount || 0 }}
            </el-button>
          </div>
        </el-card>

        <el-card v-if="article" class="card comment-card">
          <template #header>
            <div class="card-head">
              <span class="accent"></span>
              <span class="card-title">评论（{{ commentCount }}）</span>
            </div>
          </template>
          <el-form ref="commentFormRef" :model="commentForm" :rules="commentRules" class="comment-form">
            <div v-if="replyTo" class="reply-tip">
              正在回复 @{{ replyTo.nickname }}
              <el-button link type="primary" size="small" @click="cancelReply">取消</el-button>
            </div>
            <div class="form-row">
              <el-form-item prop="nickname">
                <el-input v-model="commentForm.nickname" placeholder="昵称（必填）" maxlength="50" />
              </el-form-item>
              <el-form-item prop="email">
                <el-input v-model="commentForm.email" placeholder="邮箱（选填，不公开）" maxlength="100" />
              </el-form-item>
            </div>
            <el-form-item prop="content">
              <el-input
                v-model="commentForm.content"
                type="textarea"
                :rows="3"
                maxlength="500"
                show-word-limit
                placeholder="说点什么吧（提交后需管理员审核）"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" round :loading="commentSubmitting" @click="submitComment">
                提交评论
              </el-button>
            </el-form-item>
          </el-form>
          <el-divider />
          <el-empty v-if="comments.length === 0" description="暂无评论，来抢沙发吧" :image-size="72" />
          <div v-else class="comment-list">
            <CommentItem v-for="c in comments" :key="c.id" :comment="c" @reply="startReply" />
          </div>
        </el-card>
      </el-col>

      <!-- 右栏：文章目录 + 返回文章列表 -->
      <el-col :xs="24" :sm="24" :md="6" class="col">
        <div class="side-col">
          <el-card class="card toc-card">
            <template #header>
              <div class="card-head">
                <el-icon class="toc-icon"><List /></el-icon>
                <span class="card-title">目录</span>
              </div>
            </template>
            <nav v-if="toc.length" class="toc-nav">
              <button
                v-for="item in toc"
                :key="item.id"
                type="button"
                class="toc-item"
                :class="{ active: activeId === item.id }"
                :style="{ paddingLeft: `${8 + item.depth * 14}px` }"
                @click="goHeading(item.id)"
              >
                {{ item.text }}
              </button>
            </nav>
            <p v-else class="toc-empty">本文暂无目录</p>
            <div class="toc-footer">
              <router-link to="/" class="back-link">
                <el-icon><ArrowLeft /></el-icon>
                <span>返回文章列表</span>
              </router-link>
            </div>
          </el-card>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.title {
  font-size: 26px;
  font-weight: 700;
  line-height: 1.4;
  color: var(--blog-text);
}
.meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px 14px;
  margin-top: 14px;
  font-size: 13px;
  color: var(--blog-text-light);
}
.meta-cat {
  padding: 2px 10px;
  border-radius: var(--blog-radius-pill);
  background: var(--blog-chip-bg);
  color: var(--blog-primary);
}
.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.meta-item .el-icon {
  font-size: 14px;
}
.tags {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 26px;
}
.like-btn {
  margin-left: auto;
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
.card-title {
  color: var(--blog-text);
  font-weight: 600;
  font-size: 15px;
}
.comment-card {
  margin-top: 20px;
}
.reply-tip {
  margin-bottom: 10px;
  font-size: 13px;
  color: var(--blog-primary);
}
.form-row {
  display: flex;
  gap: 16px;
}
.form-row :deep(.el-form-item) {
  flex: 1;
}
.comment-list {
  margin-top: -8px;
}

/* ===== 两栏布局：左正文 / 右目录 ===== */
.col {
  margin-bottom: 20px;
}
/* 目录常驻跟随（仅左右并排时生效，窄屏堆叠后无需吸顶） */
@media (min-width: 992px) {
  .side-col {
    position: sticky;
    top: 76px;
  }
}

/* ===== 文章目录 ===== */
.toc-icon {
  font-size: 15px;
  color: var(--blog-text-light);
}
.toc-nav {
  display: flex;
  flex-direction: column;
  max-height: calc(100vh - 260px);
  overflow-y: auto;
}
.toc-item {
  display: block;
  width: 100%;
  padding: 5px 8px;
  border: none;
  border-left: 2px solid transparent;
  border-radius: 0 6px 6px 0;
  background: none;
  font-size: 13px;
  line-height: 1.5;
  text-align: left;
  color: var(--blog-text-light);
  cursor: pointer;
  transition: color 0.2s, background-color 0.2s, border-color 0.2s;
}
.toc-item:hover {
  color: var(--blog-primary);
  background: var(--blog-primary-soft);
}
.toc-item.active {
  color: var(--blog-primary);
  border-left-color: var(--blog-primary);
  background: var(--blog-primary-soft);
}
.toc-empty {
  font-size: 13px;
  color: var(--blog-text-light);
}
.toc-footer {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px solid var(--blog-divider);
}
.back-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--blog-text-light);
  text-decoration: none;
  transition: color 0.2s;
}
.back-link:hover {
  color: var(--blog-primary);
}
</style>