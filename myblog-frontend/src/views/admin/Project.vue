<script setup>
import { ref, reactive, onMounted } from 'vue'
import {
  pageAdminProjects,
  getAdminProject,
  createProject,
  updateProject,
  deleteProject
} from '@/api/project'
import MarkdownEditor from '@/components/MarkdownEditor.vue'
import ImageUpload from '@/components/ImageUpload.vue'
import { PROJECT_PROGRESS, progressLabel, progressTagType } from '@/constants/projectProgress'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', status: null, progress: null })

const dialogVisible = ref(false)
const saving = ref(false)
const editingId = ref(null)
const formRef = ref(null)
const form = reactive({
  name: '',
  description: '',
  content: '',
  coverImage: '',
  techStack: '',
  githubUrl: '',
  demoUrl: '',
  sort: 0,
  progress: 1,
  status: 1
})
const rules = {
  name: [{ required: true, message: '请输入项目名称', trigger: 'blur' }]
}

async function loadData() {
  loading.value = true
  try {
    const data = await pageAdminProjects(query)
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
  query.status = null
  query.progress = null
  handleSearch()
}

function resetForm() {
  Object.assign(form, {
    name: '',
    description: '',
    content: '',
    coverImage: '',
    techStack: '',
    githubUrl: '',
    demoUrl: '',
    sort: 0,
    progress: 1,
    status: 1
  })
}

function openCreate() {
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

async function openEdit(row) {
  editingId.value = row.id
  resetForm()
  const data = await getAdminProject(row.id)
  Object.assign(form, {
    name: data.name,
    description: data.description || '',
    content: data.content || '',
    coverImage: data.coverImage || '',
    techStack: data.techStack || '',
    githubUrl: data.githubUrl || '',
    demoUrl: data.demoUrl || '',
    sort: data.sort ?? 0,
    progress: data.progress ?? 1,
    status: data.status ?? 1
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }
  saving.value = true
  try {
    if (editingId.value) {
      await updateProject(editingId.value, form)
      ElMessage.success('更新成功')
    } else {
      await createProject(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    // 错误提示已由拦截器处理
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除项目「${row.name}」吗？`, '警告', { type: 'warning' })
  await deleteProject(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(loadData)
</script>

<template>
  <el-card>
    <div class="toolbar">
      <el-input
        v-model="query.keyword"
        placeholder="搜索项目名"
        clearable
        class="w-220"
        @keyup.enter="handleSearch"
      />
      <el-select v-model="query.status" placeholder="上架状态" clearable class="w-140">
        <el-option label="上架" :value="1" />
        <el-option label="下架" :value="0" />
      </el-select>
      <el-select v-model="query.progress" placeholder="全部进度" clearable class="w-140">
        <el-option v-for="p in PROJECT_PROGRESS" :key="p.value" :label="p.label" :value="p.value" />
      </el-select>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
      <div class="spacer"></div>
      <el-button type="primary" @click="openCreate">新增项目</el-button>
    </div>

    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column type="index" label="序号" width="70" :index="indexMethod" />
      <el-table-column label="封面" width="90">
        <template #default="{ row }">
          <img v-if="row.coverImage" :src="row.coverImage" class="thumb" alt="封面" />
          <span v-else class="no-cover">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="项目名称" min-width="160" show-overflow-tooltip />
      <el-table-column prop="techStack" label="技术栈" min-width="160" show-overflow-tooltip />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column label="进度" width="90">
        <template #default="{ row }">
          <el-tag :type="progressTagType(row.progress)" size="small">
            {{ progressLabel(row.progress) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
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

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑项目' : '新增项目'"
      width="760px"
      top="4vh"
      class="project-dialog"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="项目名称" prop="name">
              <el-input v-model="form.name" maxlength="100" placeholder="项目名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="技术栈">
              <el-input v-model="form.techStack" maxlength="200" placeholder="逗号分隔，如 Vue3,Spring Boot" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="源码地址">
              <el-input v-model="form.githubUrl" placeholder="GitHub 地址（可选）" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="演示地址">
              <el-input v-model="form.demoUrl" placeholder="在线演示地址（可选）" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="排序">
              <el-input-number v-model="form.sort" :min="0" :max="999" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="进度">
              <el-select v-model="form.progress" class="w-full">
                <el-option v-for="p in PROJECT_PROGRESS" :key="p.value" :label="p.label" :value="p.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态">
              <el-switch
                v-model="form.status"
                :active-value="1"
                :inactive-value="0"
                active-text="上架"
                inactive-text="下架"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="封面">
          <ImageUpload v-model="form.coverImage" biz-type="project" />
        </el-form-item>
        <el-form-item label="项目简介">
          <el-input v-model="form.description" type="textarea" :rows="2" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="详细介绍">
          <MarkdownEditor v-model="form.content" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">确定</el-button>
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
.w-220 {
  width: 220px;
}
.w-140 {
  width: 140px;
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
.no-cover {
  color: #c0c4cc;
}
</style>

<style>
/* 编辑弹窗内容过长时内部滚动 */
.project-dialog .el-dialog__body {
  max-height: 72vh;
  overflow: auto;
}
</style>