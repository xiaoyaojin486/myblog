<script setup>
import { ref, reactive, onMounted } from 'vue'
import { listTags, createTag, updateTag, deleteTag } from '@/api/tag'

const loading = ref(false)
const list = ref([])
const query = reactive({ keyword: '' })

const dialogVisible = ref(false)
const saving = ref(false)
const editingId = ref(null)
const formRef = ref(null)
const form = reactive({ name: '' })
const rules = {
  name: [{ required: true, message: '请输入标签名', trigger: 'blur' }]
}

async function loadData() {
  loading.value = true
  try {
    list.value = await listTags(query.keyword || undefined)
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
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  form.name = row.name
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
      await updateTag(editingId.value, { name: form.name })
      ElMessage.success('更新成功')
    } else {
      await createTag({ name: form.name })
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
  await ElMessageBox.confirm(`确定删除标签「${row.name}」吗？已被文章使用的标签不能删除`, '警告', {
    type: 'warning'
  })
  await deleteTag(row.id)
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
        placeholder="搜索标签名"
        clearable
        class="w-220"
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
      <div class="spacer"></div>
      <el-button type="primary" @click="openCreate">新增标签</el-button>
    </div>

    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column type="index" label="序号" width="80" />
      <el-table-column prop="name" label="标签名" min-width="160" />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="140">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑标签' : '新增标签'" width="420px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="70px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="标签名" maxlength="50" />
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