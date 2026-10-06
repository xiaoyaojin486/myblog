<script setup>
import { Plus } from '@element-plus/icons-vue'
import { uploadFile } from '@/api/file'

const props = defineProps({
  modelValue: { type: String, default: '' },
  bizType: { type: String, default: 'article' }
})
const emit = defineEmits(['update:modelValue'])

function beforeUpload(file) {
  const isImage = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'].includes(file.type)
  if (!isImage) {
    ElMessage.error('仅支持 jpg/png/gif/webp 格式图片')
    return false
  }
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 10MB')
    return false
  }
  return true
}

async function handleUpload(options) {
  try {
    const data = await uploadFile(options.file, props.bizType)
    emit('update:modelValue', data.url)
    ElMessage.success('上传成功')
  } catch (e) {
    // 错误提示已由 axios 拦截器统一处理
  }
}

function handleRemove() {
  emit('update:modelValue', '')
}
</script>

<template>
  <div class="image-upload-wrapper">
    <el-upload
      class="image-upload"
      :show-file-list="false"
      :before-upload="beforeUpload"
      :http-request="handleUpload"
      accept="image/*"
    >
      <img v-if="modelValue" :src="modelValue" class="preview" alt="图片预览" />
      <div v-else class="placeholder">
        <el-icon><Plus /></el-icon>
        <span>点击上传图片</span>
      </div>
    </el-upload>
    <div v-if="modelValue" class="remove-link">
      <el-button link type="danger" size="small" @click="handleRemove">移除图片</el-button>
    </div>
  </div>
</template>

<style scoped>
.image-upload :deep(.el-upload) {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  overflow: hidden;
  transition: border-color 0.2s;
}
.image-upload :deep(.el-upload:hover) {
  border-color: #409eff;
}
.preview {
  width: 240px;
  height: 135px;
  object-fit: cover;
  display: block;
}
.placeholder {
  width: 240px;
  height: 135px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: #909399;
  font-size: 13px;
  background: #fafafa;
}
.remove-link {
  margin-top: 4px;
}
</style>