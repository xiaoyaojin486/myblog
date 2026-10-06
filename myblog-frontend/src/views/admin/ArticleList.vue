<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { pageAdminArticles, deleteArticle, publishArticle, unpublishArticle } from '@/api/article'
import { listCategories } from '@/api/category'

const router = useRouter()
const loading = ref(false)
const categories = ref([])
const total = ref(0)
const list = ref([])
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', categoryId: null, status: null })

async function loadData() {
  loading.value = true
  try {
    const data = await pageAdminArticles(query)
    list.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

/** 序号：跨分页连续，从 1 开始 */
function indexMethod(i) {
  return (query.pageNum - 1) * query.pageSize + i + 1
}

function handleSearch() {
  query.pageNum = 1
  loadData()
}

function handleReset() {
  query.keyword = ''
  query.categoryId = null
  query.status = null
  handleSearch()
}

function handleCreate() {
  router.push('/admin/article/edit')
}

function handleEdit(row) {
  router.push(`/admin/article/edit/${row.id}`)
}

async function handlePublish(row) {
  await ElMessageBox.confirm(`确定发布《${row.title}》吗？`, '提示', { type: 'warning' })
  await publishArticle(row.id)
  ElMessage.success('发布成功')
  loadData()
}

async function handleUnpublish(row) {
  await ElMessageBox.confirm(`确定下线《${row.title}》吗？下线后将回到草稿，前台不再展示`, '提示', {
    type: 'warning'
  })
  await unpublishArticle(row.id)
  ElMessage.success('已下线，文章已回到草稿')
  loadData()
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除《${row.title}》吗？删除后不可恢复`, '警告', { type: 'warning' })
  await deleteArticle(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(async () => {
  categories.value = await listCategories()
  loadData()
})
</script>

<template>
  <el-card>
    <div class="toolbar">
      <el-input
        v-model="query.keyword"
        placeholder="搜索标题/摘要"
        clearable
        class="w-220"
        @keyup.enter="handleSearch"
      />
      <el-select v-model="query.categoryId" placeholder="全部分类" clearable class="w-160">
        <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
      <el-select v-model="query.status" placeholder="全部状态" clearable class="w-140">
        <el-option label="已发布" :value="1" />
        <el-option label="草稿" :value="0" />
      </el-select>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
      <div class="spacer"></div>
      <el-button type="primary" @click="handleCreate">写文章</el-button>
    </div>

    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column type="index" label="序号" width="70" :index="indexMethod" />
      <el-table-column prop="title" label="标题" min-width="240" show-overflow-tooltip />
      <el-table-column label="分类" width="120">
        <template #default="{ row }">{{ row.categoryName || '未分类' }}</template>
      </el-table-column>
      <el-table-column prop="viewCount" label="浏览" width="80" />
      <el-table-column prop="likeCount" label="点赞" width="80" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '已发布' : '草稿' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button v-if="row.status === 0" link type="success" @click="handlePublish(row)">发布</el-button>
          <el-button v-else link type="warning" @click="handleUnpublish(row)">下线</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="loadData"
        @size-change="handleSearch"
      />
    </div>
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
.w-220 {
  width: 220px;
}
.w-160 {
  width: 160px;
}
.w-140 {
  width: 140px;
}
.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>