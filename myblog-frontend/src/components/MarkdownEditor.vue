<script setup>
import VMdEditor from '@kangc/v-md-editor'
import '@kangc/v-md-editor/lib/style/base-editor.css'
import githubTheme from '@kangc/v-md-editor/lib/theme/github.js'
import '@kangc/v-md-editor/lib/theme/style/github.css'
import hljs from 'highlight.js'
import { uploadFile } from '@/api/file'

VMdEditor.use(githubTheme, { Hljs: hljs })

defineProps({
  modelValue: { type: String, default: '' }
})
const emit = defineEmits(['update:modelValue'])

/**
 * 编辑器内插入图片：图片按钮 →「上传图片」/ 直接粘贴 / 拖拽
 * 先上传到 OSS（bizType=article），再插入 Markdown 图片链接
 */
async function handleUploadImage(event, insertImage, files) {
  for (const file of files) {
    try {
      const data = await uploadFile(file, 'article')
      insertImage({ url: data.url, desc: file.name.replace(/\.[^.]+$/, '') })
    } catch (e) {
      // 错误提示已由拦截器处理
    }
  }
}
</script>

<template>
  <v-md-editor
    :model-value="modelValue"
    height="520px"
    :disabled-menus="[]"
    @update:model-value="emit('update:modelValue', $event)"
    @upload-image="handleUploadImage"
  />
</template>