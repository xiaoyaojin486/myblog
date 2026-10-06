<script setup>
import { ref, reactive, onMounted } from 'vue'
import { listCategories, createCategory, updateCategory, deleteCategory } from '@/api/category'

const loading = ref(false)
const list = ref([])
const query = reactive({ keyword: '' })
const dialogVisible = ref(false)
const saving = ref(false)
const editingId = ref(null)
const formRef = ref(null)
const form = reactive({ name: '', sort: 0 })
const rules = {
  name: [{ required: true, message: '请输入分类名', trigger: 'blur' }]
}

async function loadData() {
  loading.value = true
  try {
    list.value = await listCategories(query.keyword || undefined)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  loadData()
}

function handleReset() {
  query.keyword = ''
  loadData()
}

function openCreate() {
  editingId.value = null
  form.name = ''
  form.sort = 0
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  form.name = row.name
  form.sort = row.sort ?? 0
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
      await updateCategory(editingId.value, { name: form.name, sort: form.sort })
      ElMessage.success('更新成功')
    } else {
      await createCategory({ name: form.name, sort: form.sort })
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
  await ElMessageBox.confirm(`确定删除分类「${row.name}」吗？分类下有文章时不可删除`, '警告', { type: 'warning' })
  await deleteCategory(row.id)
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
        placeholder="搜索分类名"
        clearable
        class="w-220"
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
      <div class="spacer"></div>
      <el-button type="primary" @click="openCreate">新增分类</el-button>
    </div>

    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column type="index" label="序号" width="80" />
      <el-table-column prop="name" label="分类名" min-width="160" />
      <el-table-column prop="sort" label="排序" width="100" />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑分类' : '新增分类'" width="420px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="70px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="分类名" maxlength="50" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :max="999" />
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
</style>