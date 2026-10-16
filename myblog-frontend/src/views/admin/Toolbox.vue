<script setup>
import { ref, reactive, onMounted } from 'vue'
import {
  pageAdminCommands,
  getAdminCommand,
  createCommand,
  updateCommand,
  deleteCommand,
  listAdminCategories,
  createCategory,
  updateCategory,
  deleteCategory
} from '@/api/toolbox'
import { TOOL_RISK, riskLabel, riskTagType } from '@/constants/toolRisk'

// ===================== 命令列表 =====================
const loading = ref(false)
const list = ref([])
const total = ref(0)
const categories = ref([])
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', categoryId: null, riskLevel: null, status: null })

const dialogVisible = ref(false)
const saving = ref(false)
const editingId = ref(null)
const formRef = ref(null)
const form = reactive({
  name: '',
  command: '',
  categoryId: null,
  description: '',
  riskLevel: 0,
  sort: 0,
  status: 1
})
const rules = {
  name: [{ required: true, message: '请输入用途标题', trigger: 'blur' }],
  command: [{ required: true, message: '请输入命令原文', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

async function loadData() {
  loading.value = true
  try {
    const data = await pageAdminCommands(query)
    list.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

async function loadCategories() {
  categories.value = await listAdminCategories()
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
  query.riskLevel = null
  query.status = null
  handleSearch()
}

function resetForm() {
  Object.assign(form, {
    name: '',
    command: '',
    categoryId: null,
    description: '',
    riskLevel: 0,
    sort: 0,
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
  const data = await getAdminCommand(row.id)
  Object.assign(form, {
    name: data.name,
    command: data.command,
    categoryId: data.categoryId,
    description: data.description || '',
    riskLevel: data.riskLevel ?? 0,
    sort: data.sort ?? 0,
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
      await updateCommand(editingId.value, form)
      ElMessage.success('更新成功')
    } else {
      await createCommand(form)
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
  await ElMessageBox.confirm(`确定删除命令「${row.name}」吗？`, '警告', { type: 'warning' })
  await deleteCommand(row.id)
  ElMessage.success('删除成功')
  loadData()
}

// ===================== 分类管理 =====================
const catDialogVisible = ref(false)
const catSaving = ref(false)
const catList = ref([])
const editingCatId = ref(null)
const catFormRef = ref(null)
const catForm = reactive({ name: '', sort: 0, status: 1 })
const catRules = {
  name: [{ required: true, message: '请输入分类名', trigger: 'blur' }]
}

async function reloadCatList() {
  catList.value = await listAdminCategories()
}

async function openCatDialog() {
  editingCatId.value = null
  resetCatForm()
  await reloadCatList()
  catDialogVisible.value = true
}

function resetCatForm() {
  Object.assign(catForm, { name: '', sort: 0, status: 1 })
}

function editCat(row) {
  editingCatId.value = row.id
  Object.assign(catForm, { name: row.name, sort: row.sort ?? 0, status: row.status ?? 1 })
}

async function saveCat() {
  try {
    await catFormRef.value.validate()
  } catch (e) {
    return
  }
  catSaving.value = true
  try {
    if (editingCatId.value) {
      await updateCategory(editingCatId.value, catForm)
      ElMessage.success('分类已更新')
    } else {
      await createCategory(catForm)
      ElMessage.success('分类已新增')
    }
    editingCatId.value = null
    resetCatForm()
    await reloadCatList()
    await loadCategories()
  } catch (e) {
    // 错误提示已由拦截器处理
  } finally {
    catSaving.value = false
  }
}

async function removeCat(row) {
  await ElMessageBox.confirm(`确定删除分类「${row.name}」吗？`, '警告', { type: 'warning' })
  await deleteCategory(row.id)
  ElMessage.success('删除成功')
  if (editingCatId.value === row.id) {
    editingCatId.value = null
    resetCatForm()
  }
  await reloadCatList()
  await loadCategories()
}

onMounted(() => {
  loadCategories()
  loadData()
})
</script>

<template>
  <el-card>
    <div class="toolbar">
      <el-input
        v-model="query.keyword"
        placeholder="搜索用途/命令/说明"
        clearable
        class="w-220"
        @keyup.enter="handleSearch"
      />
      <el-select v-model="query.categoryId" placeholder="全部分类" clearable class="w-140">
        <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
      <el-select v-model="query.riskLevel" placeholder="全部风险" clearable class="w-140">
        <el-option v-for="r in TOOL_RISK" :key="r.value" :label="r.label" :value="r.value" />
      </el-select>
      <el-select v-model="query.status" placeholder="上架状态" clearable class="w-140">
        <el-option label="上架" :value="1" />
        <el-option label="下架" :value="0" />
      </el-select>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
      <div class="spacer"></div>
      <el-button @click="openCatDialog">分类管理</el-button>
      <el-button type="primary" @click="openCreate">新增命令</el-button>
    </div>

    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column type="index" label="序号" width="70" :index="indexMethod" />
      <el-table-column prop="command" label="命令" min-width="220" show-overflow-tooltip>
        <template #default="{ row }">
          <code class="cmd-code">{{ row.command }}</code>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="用途标题" min-width="140" show-overflow-tooltip />
      <el-table-column prop="categoryName" label="分类" width="120" />
      <el-table-column label="风险" width="90">
        <template #default="{ row }">
          <el-tag :type="riskTagType(row.riskLevel)" size="small">{{ riskLabel(row.riskLevel) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
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

    <!-- 命令新增/编辑 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑命令' : '新增命令'"
      width="640px"
      top="6vh"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="用途标题" prop="name">
          <el-input v-model="form.name" maxlength="100" placeholder="如：查看当前目录" />
        </el-form-item>
        <el-form-item label="命令原文" prop="command">
          <el-input
            v-model="form.command"
            type="textarea"
            :rows="2"
            maxlength="500"
            placeholder="如：pwd / kubectl get pods -o wide"
          />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="分类" prop="categoryId">
              <el-select v-model="form.categoryId" class="w-full" placeholder="请选择分类">
                <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="风险等级">
              <el-select v-model="form.riskLevel" class="w-full">
                <el-option v-for="r in TOOL_RISK" :key="r.value" :label="r.label" :value="r.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="排序">
              <el-input-number v-model="form.sort" :min="0" :max="999" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
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
        <el-form-item label="说明">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            maxlength="255"
            show-word-limit
            placeholder="命令说明 / 注意事项（可选）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分类管理 -->
    <el-dialog v-model="catDialogVisible" title="分类管理" width="560px" top="8vh">
      <el-form ref="catFormRef" :model="catForm" :rules="catRules" inline>
        <el-form-item label="分类名" prop="name">
          <el-input v-model="catForm.name" maxlength="50" placeholder="分类名" class="cat-name" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="catForm.sort" :min="0" :max="999" controls-position="right" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="catForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="catSaving" @click="saveCat">
            {{ editingCatId ? '保存' : '新增' }}
          </el-button>
          <el-button v-if="editingCatId" @click="((editingCatId = null), resetCatForm())">取消</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="catList" size="small" max-height="320">
        <el-table-column prop="name" label="分类名" min-width="140" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button link type="primary" @click="editCat(row)">编辑</el-button>
            <el-button link type="danger" @click="removeCat(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </el-card>
</template>

<style scoped>
.toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
  flex-wrap: wrap;
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
.cat-name {
  width: 160px;
}
.cmd-code {
  font-family: 'JetBrains Mono', 'Fira Code', Consolas, Monaco, monospace;
  font-size: 13px;
  color: var(--el-text-color-regular, #606266);
}
.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
