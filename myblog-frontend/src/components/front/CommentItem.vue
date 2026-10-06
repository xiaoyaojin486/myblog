<script setup>
defineOptions({ name: 'CommentItem' })

defineProps({
  comment: { type: Object, required: true }
})
const emit = defineEmits(['reply'])
</script>

<template>
  <div class="comment-item">
    <div class="head">
      <span class="nickname">{{ comment.nickname }}</span>
      <span v-if="comment.blogger" class="blogger-tag">博主</span>
      <span class="time">{{ comment.createTime }}</span>
      <el-button link type="primary" size="small" @click="emit('reply', comment)">回复</el-button>
    </div>
    <div class="content">{{ comment.content }}</div>
    <div v-if="comment.children && comment.children.length" class="children">
      <CommentItem
        v-for="child in comment.children"
        :key="child.id"
        :comment="child"
        @reply="emit('reply', $event)"
      />
    </div>
  </div>
</template>

<style scoped>
.comment-item {
  padding: 10px 0;
}
.head {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
}
.nickname {
  font-weight: 600;
  color: var(--blog-primary);
}
/* 博主标识：与主题色一致，弱化处理避免抢视觉 */
.blogger-tag {
  padding: 1px 7px;
  border-radius: var(--blog-radius-pill);
  background: var(--blog-primary-soft);
  color: var(--blog-primary);
  font-size: 12px;
}
.time {
  color: var(--blog-text-light);
  flex: 1;
}
.content {
  margin-top: 6px;
  color: var(--blog-text);
  font-size: 14px;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
}
.children {
  margin: 8px 0 0 20px;
  padding-left: 16px;
  border-left: 2px solid var(--blog-border);
}
</style>