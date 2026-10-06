<script setup>
import { progressLabel } from '@/constants/projectProgress'

// 前台项目进度标签：浅底 + 主题色系文字，日夜模式各自取色
const props = defineProps({
  progress: { type: Number, default: null }
})
</script>

<template>
  <span v-if="props.progress !== null" class="progress-tag" :class="`p${props.progress}`">
    {{ progressLabel(props.progress) }}
  </span>
</template>

<style scoped>
.progress-tag {
  display: inline-block;
  flex-shrink: 0;
  padding: 2px 10px;
  border-radius: var(--blog-radius-pill);
  font-size: 12px;
  line-height: 18px;
  font-weight: 400;
  /* 默认（规划中）：中性灰 */
  background: var(--blog-surface-2);
  color: var(--blog-text-light);
}
.progress-tag.p1 {
  background: var(--blog-primary-soft);
  color: var(--blog-primary);
}
.progress-tag.p2 {
  background: var(--blog-success-soft);
  color: var(--blog-success);
}
.progress-tag.p3 {
  background: var(--blog-warning-soft);
  color: var(--blog-warning);
}
</style>