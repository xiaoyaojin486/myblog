<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import {
  pageAdminComments,
  approveComment,
  rejectComment,
  replyComment,
  updateCommentsStatus,
  deleteComment,
  deleteComments
} from '@/api/comment'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, status: undefined, keyword: '' })

const selected = ref([])
const selectedIds = computed(() => selected.value.map((row) => row.id))

const replyVisible = ref(false)
const replying = ref(false)
const replyFormRef = ref(null)
const replyTarget = ref(null)
const replyForm = reactive({ content: '' })
const replyRules = {
  content: [{ required: true, message: '请输入回复内容', trigger: 'blur' }]
}

/** 审核状态：0待审核，1通过，2拒绝 */
function statusLabel(status) {
  if (status === 1) return '已通过'
  if (status === 2) return '已拒绝'
  return '待审核'
}

function statusTagType(status) {
  if (status === 1) return 'success'
  if (status === 2) return 'info'
  return 'warning'
}

/** 序号：跨分页连续，从 1 开始 */
function indexMethod(i) {
  return (query.pageNum - 1) * query.pageSize + i + 1
}

async function loadData() {
  loading.value = true
  try {
    const data = await pageAdminComments(query)
    list.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    // 错误提示已由拦截器处理
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.pageNum = 1
  loadData()
}

function handleReset() {
  query.status = undefined
  query.keyword = ''
  handleSearch()
}

function handleSelectionChange(rows) {
  selected.value = rows
}

async function handleApprove(row) {
  await approveComment(row.id)
  ElMessage.success('已通过审核')
  loadData()
}

async function handleReject(row) {
  await ElMessageBox.confirm(
    `确定拒绝「${row.nickname}」的这条评论吗？拒绝后前台不再展示（记录保留）。`,
    '警告',
    { type: 'warning' }
  )
  await rejectComment(row.id)
  ElMessage.success('已拒绝')
  loadData()
}

function openReply(row) {
  replyTarget.value = row
  replyForm.content = ''
  replyVisible.value = true
}

async function handleReply() {
  try {
    await replyFormRef.value.validate()
  } catch (e) {
    return
  }
  replying.value = true
  try {
    await replyComment(replyTarget.value.id, replyForm.content)
    ElMessage.success('回复已发布')
    replyVisible.value = false
    loadData()
  } catch (e) {
    // 错误提示已由拦截器处理
  } finally {
    replying.value = false
  }
}

async function handleBatchStatus(status, label) {
  const count = selectedIds.value.length
  await ElMessageBox.confirm(`确定将选中的 ${count} 条评论标记为「${label}」吗？`, '警告', {
    type: 'warning'
  })
  await updateCommentsStatus(selectedIds.value, status)
  ElMessage.success(`已${label} ${count} 条评论`)
  loadData()
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该评论吗？其直接回复也会一并删除。', '警告', {
    type: 'warning'
  })
  await deleteComment(row.id)
  ElMessage.success('删除成功')
  loadData()
}

async function handleBatchDelete() {
  const count = selectedIds.value.length
  await ElMessageBox.confirm(
    `确定删除选中的 ${count} 条评论吗？各自的直接回复也会一并删除。`,
    '警告',
    { type: 'warning' }
  )
  await deleteComments(selectedIds.value)
  ElMessage.success(`已删除 ${count} 条评论`)
  loadData()
}

onMounted(loadData)
</script>

<template>
  <el-card>
    <div class="toolbar">
      <el-select
        v-model="query.status"
        placeholder="审核状态"
        clearable
        class="w-140"
        @change="handleSearch"
      >
        <el-option label="待审核" :value="0" />
        <el-option label="已通过" :value="1" />
        <el-option label="已拒绝" :value="2" />
      </el-select>
      <el-input
        v-model="query.keyword"
        placeholder="昵称 / 内容 / 邮箱"
        clearable
        class="w-220"
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
      <div class="spacer"></div>
      <el-button :disabled="selected.length === 0" @click="handleBatchStatus(1, '通过')">
        批量通过{{ selected.length ? `（${selected.length}）` : '' }}
      </el-button>
      <el-button :disabled="selected.length === 0" @click="handleBatchStatus(2, '拒绝')">
        批量拒绝{{ selected.length ? `（${selected.length}）` : '' }}
      </el-button>
      <el-button type="danger" :disabled="selected.length === 0" @click="handleBatchDelete">
        批量删除{{ selected.length ? `（${selected.length}）` : '' }}
      </el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="list"
      row-key="id"
      stripe
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="46" />
      <el-table-column type="index" label="序号" width="70" :index="indexMethod" />
      <el-table-column label="所属文章" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">
          <router-link
            v-if="row.articleTitle && row.articleTitle !== '文章已删除'"
            :to="`/article/${row.articleId}`"
            target="_blank"
            class="article-link"
          >
            {{ row.articleTitle }}
          </router-link>
          <span v-else class="deleted">{{ row.articleTitle || '文章已删除' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="昵称" width="150">
        <template #default="{ row }">
          <span class="nick">{{ row.nickname }}</span>
          <el-tag v-if="row.blogger" size="small" effect="plain" class="blogger-tag">博主</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="email" label="邮箱" width="160" show-overflow-tooltip />
      <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
      <el-table-column label="IP" width="130" show-overflow-tooltip>
        <template #default="{ row }">{{ row.ip || '-' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)" size="small">
            {{ statusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="时间" width="170" />
      <el-table-column label="操作" width="210" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status !== 1" link type="success" @click="handleApprove(row)">
            通过
          </el-button>
          <el-button v-if="row.status !== 2" link type="warning" @click="handleReject(row)">
            拒绝
          </el-button>
          <el-button link type="primary" @click="openReply(row)">回复</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="query.pageNum"
        :page-size="query.pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadData"
      />
    </div>

    <el-dialog v-model="replyVisible" title="以博主身份回复" width="560px">
      <div v-if="replyTarget" class="reply-quote">
        <div class="quote-head">{{ replyTarget.nickname }}：</div>
        <div class="quote-content">{{ replyTarget.content }}</div>
      </div>
      <el-form ref="replyFormRef" :model="replyForm" :rules="replyRules">
        <el-form-item prop="content">
          <el-input
            v-model="replyForm.content"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="以博主身份回复，发布后直接展示在前台"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" :loading="replying" @click="handleReply">发布回复</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<style scoped>
.toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}
.spacer {
  flex: 1;
}
.w-140 {
  width: 140px;
}
.w-220 {
  width: 220px;
}
.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
.article-link {
  color: var(--el-color-primary);
  text-decoration: none;
}
.article-link:hover {
  text-decoration: underline;
}
.nick {
  margin-right: 6px;
}
.blogger-tag {
  vertical-align: middle;
}
.deleted {
  color: #c0c4cc;
}
.reply-quote {
  margin-bottom: 12px;
  padding: 10px 12px;
  border-radius: 6px;
  background: #f5f7fa;
  font-size: 13px;
}
.quote-head {
  color: #909399;
}
.quote-content {
  margin-top: 4px;
  color: #303133;
  word-break: break-word;
}
</style>