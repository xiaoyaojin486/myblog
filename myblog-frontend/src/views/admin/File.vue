<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { pageAdminFiles, renameFile, removeFile, removeFiles, uploadFile } from '@/api/file'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, bizType: '', keyword: '' })

const selected = ref([])
const uploadBizType = ref('other')

const BIZ_TYPES = [
  { label: '头像', value: 'avatar' },
  { label: '文章', value: 'article' },
  { label: '项目', value: 'project' },
  { label: '动态', value: 'moment' },
  { label: '其它', value: 'other' }
]
const IMAGE_EXTS = ['jpg', 'jpeg', 'png', 'gif', 'webp']

const renameVisible = ref(false)
const renaming = ref(false)
const renameFormRef = ref(null)
const renameForm = reactive({ objectKey: '', newName: '', targetBizType: '' })
const renameRules = {
  newName: [{ required: true, message: '请输入文件名', trigger: 'blur' }]
}

const selectedKeys = computed(() => selected.value.map((row) => row.objectKey))

function isImage(row) {
  return IMAGE_EXTS.includes(row.ext)
}

function formatSize(bytes) {
  if (bytes === null || bytes === undefined) return '-'
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / 1024 / 1024).toFixed(2)} MB`
}

async function loadData() {
  loading.value = true
  try {
    const data = await pageAdminFiles(query)
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
  query.bizType = ''
  query.keyword = ''
  handleSearch()
}

function handleSelectionChange(rows) {
  selected.value = rows
}

async function handleCopy(row) {
  try {
    await navigator.clipboard.writeText(row.url)
    ElMessage.success('链接已复制')
  } catch (e) {
    ElMessage.warning(`复制失败，请手动复制：${row.url}`)
  }
}

function openRename(row) {
  const suffix = row.ext ? `.${row.ext}` : ''
  renameForm.objectKey = row.objectKey
  renameForm.newName = suffix && row.name.toLowerCase().endsWith(suffix)
    ? row.name.slice(0, -suffix.length)
    : row.name
  renameForm.targetBizType = row.bizType
  renameVisible.value = true
}

async function handleRename() {
  try {
    await renameFormRef.value.validate()
  } catch (e) {
    return
  }
  renaming.value = true
  try {
    await renameFile({ ...renameForm })
    ElMessage.success('已保存')
    renameVisible.value = false
    loadData()
  } catch (e) {
    // 错误提示已由拦截器处理
  } finally {
    renaming.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除文件「${row.name}」吗？删除后不可恢复。`, '警告', {
    type: 'warning'
  })
  await removeFile(row.objectKey)
  ElMessage.success('删除成功')
  loadData()
}

async function handleBatchDelete() {
  const count = selectedKeys.value.length
  await ElMessageBox.confirm(`确定删除选中的 ${count} 个文件吗？删除后不可恢复。`, '警告', {
    type: 'warning'
  })
  await removeFiles(selectedKeys.value)
  ElMessage.success(`已删除 ${count} 个文件`)
  loadData()
}

async function handleUpload(options) {
  try {
    await uploadFile(options.file, uploadBizType.value)
    ElMessage.success('上传成功')
    loadData()
  } catch (e) {
    // 错误提示已由拦截器处理
  }
}

onMounted(loadData)
</script>

<template>
  <el-card>
    <div class="toolbar">
      <el-select v-model="query.bizType" placeholder="全部类型" clearable class="w-140">
        <el-option v-for="t in BIZ_TYPES" :key="t.value" :label="t.label" :value="t.value" />
      </el-select>
      <el-input
        v-model="query.keyword"
        placeholder="搜索文件名"
        clearable
        class="w-220"
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
      <div class="spacer"></div>
      <el-select v-model="uploadBizType" class="w-140">
        <el-option v-for="t in BIZ_TYPES" :key="t.value" :label="`上传到：${t.label}`" :value="t.value" />
      </el-select>
      <el-upload :show-file-list="false" accept="image/*" :http-request="handleUpload">
        <el-button type="primary">上传文件</el-button>
      </el-upload>
      <el-button type="danger" :disabled="selected.length === 0" @click="handleBatchDelete">
        批量删除{{ selected.length ? `（${selected.length}）` : '' }}
      </el-button>
    </div>

    <el-table v-loading="loading" :data="list" row-key="objectKey" stripe @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="46" />
      <el-table-column label="预览" width="90">
        <template #default="{ row }">
          <img v-if="isImage(row)" :src="row.url" class="thumb" alt="预览" />
          <span v-else class="ext-badge">{{ row.ext || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="文件名" min-width="220" show-overflow-tooltip />
      <el-table-column label="类型" width="100">
        <template #default="{ row }">
          <el-tag size="small">{{ row.bizType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="大小" width="110">
        <template #default="{ row }">{{ formatSize(row.size) }}</template>
      </el-table-column>
      <el-table-column prop="lastModified" label="最后修改" width="170" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleCopy(row)">复制链接</el-button>
          <el-button link type="primary" @click="openRename(row)">重命名</el-button>
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

    <el-dialog v-model="renameVisible" title="重命名 / 移动" width="520px">
      <el-form ref="renameFormRef" :model="renameForm" :rules="renameRules" label-width="90px">
        <el-form-item label="原文件">
          <span class="origin-key">{{ renameForm.objectKey }}</span>
        </el-form-item>
        <el-form-item label="文件名" prop="newName">
          <el-input v-model="renameForm.newName" maxlength="120" placeholder="不含扩展名（自动沿用原扩展名）" />
        </el-form-item>
        <el-form-item label="移动到">
          <el-select v-model="renameForm.targetBizType" class="w-full">
            <el-option v-for="t in BIZ_TYPES" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="renameVisible = false">取消</el-button>
        <el-button type="primary" :loading="renaming" @click="handleRename">确定</el-button>
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
.w-full {
  width: 100%;
}
.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
.thumb {
  width: 64px;
  height: 36px;
  object-fit: cover;
  border-radius: 4px;
}
.ext-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  background: #f4f4f5;
  color: #909399;
  font-size: 12px;
  text-transform: uppercase;
}
.origin-key {
  color: #909399;
  font-size: 12px;
  word-break: break-all;
}
</style>