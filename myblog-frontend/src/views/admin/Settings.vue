<script setup>
import { ref, reactive, onMounted } from 'vue'
import { listConfigs, updateConfigs } from '@/api/config'
import { useConfigStore } from '@/store/config'

const configStore = useConfigStore()
const loading = ref(false)
const saving = ref(false)
const configs = ref([])
const form = reactive({})

async function loadData() {
  loading.value = true
  try {
    // 本站点信息页只维护站点级配置（昵称/头像/邮箱/GitHub 在「个人资料」，自我介绍/简历在「关于我」）
    const siteKeys = ['site_name', 'site_description', 'icp']
    configs.value = (await listConfigs()).filter((c) => siteKeys.includes(c.configKey))
    configs.value.forEach((c) => {
      form[c.configKey] = c.configValue || ''
    })
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  saving.value = true
  try {
    await updateConfigs({ ...form })
    ElMessage.success('保存成功')
    configStore.load(true) // 刷新全局配置，前台文案即时生效
  } catch (e) {
    // 错误提示已由拦截器处理
  } finally {
    saving.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <el-card v-loading="loading">
    <template #header>
      <span class="header">站点信息</span>
    </template>
    <el-form label-width="110px" class="form" :model="form">
      <el-form-item v-for="c in configs" :key="c.configKey" :label="c.remark || c.configKey">
        <el-input v-model="form[c.configKey]" maxlength="500" />
      </el-form-item>
    </el-form>
    <div class="actions">
      <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
    </div>
  </el-card>
</template>

<style scoped>
.header {
  color: #303133;
  font-weight: 600;
}
.form {
  max-width: 560px;
}
.actions {
  margin-left: 110px;
}
</style>