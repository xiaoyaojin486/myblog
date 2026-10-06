<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getAdminArticle, createArticle, updateArticle, publishArticle } from '@/api/article'
import { listCategories } from '@/api/category'
import { listTags } from '@/api/tag'
import MarkdownEditor from '@/components/MarkdownEditor.vue'
import ImageUpload from '@/components/ImageUpload.vue'

const route = useRoute()
const router = useRouter()

const articleId = computed(() => (route.params.id ? Number(route.params.id) : null))
const isEdit = computed(() => !!articleId.value)

const categories = ref([])
const tags = ref([])
const saving = ref(false)
const formRef = ref(null)
const form = reactive({
  title: '',
  content: '',
  summary: '',
  coverImage: '',
  categoryId: null,
  tagIds: []
})
const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'change' }]
}

onMounted(async () => {
  const [cats, tgs] = await Promise.all([listCategories(), listTags()])
  categories.value = cats
  tags.value = tgs
  if (isEdit.value) {
    const data = await getAdminArticle(articleId.value)
    Object.assign(form, {
      title: data.title,
      content: data.content,
      summary: data.summary || '',
      coverImage: data.coverImage || '',
      categoryId: data.categoryId,
      tagIds: data.tagIds || []
    })
  }
})

async function handleSave(publishAfter = false) {
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }
  saving.value = true
  try {
    if (isEdit.value) {
      await updateArticle(articleId.value, form)
      if (publishAfter) {
        await publishArticle(articleId.value)
      }
    } else {
      const id = await createArticle(form)
      if (publishAfter) {
        await publishArticle(id)
      }
    }
    ElMessage.success(publishAfter ? '已发布' : '保存成功')
    router.push('/admin/article')
  } catch (e) {
    // 错误提示已由拦截器处理
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <el-card>
    <template #header>{{ isEdit ? '编辑文章' : '写文章' }}</template>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入文章标题" maxlength="200" show-word-limit />
      </el-form-item>

      <el-row :gutter="16">
        <el-col :span="8">
          <el-form-item label="分类">
            <el-select v-model="form.categoryId" placeholder="选择分类" clearable style="width: 100%">
              <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="16">
          <el-form-item label="标签">
            <el-select v-model="form.tagIds" multiple placeholder="选择标签" clearable style="width: 100%">
              <el-option v-for="t in tags" :key="t.id" :label="t.name" :value="t.id" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="摘要">
        <el-input
          v-model="form.summary"
          type="textarea"
          :rows="2"
          maxlength="500"
          show-word-limit
          placeholder="文章摘要（可选）"
        />
      </el-form-item>

      <el-form-item label="封面">
        <ImageUpload v-model="form.coverImage" biz-type="article" />
      </el-form-item>

      <el-form-item label="内容" prop="content">
        <MarkdownEditor v-model="form.content" />
      </el-form-item>

      <el-form-item>
        <el-button :loading="saving" @click="handleSave(false)">
          {{ isEdit ? '保存' : '保存草稿' }}
        </el-button>
        <el-button type="primary" :loading="saving" @click="handleSave(true)">保存并发布</el-button>
        <el-button @click="router.push('/admin/article')">返回列表</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>