<script setup>
import { onMounted } from 'vue'
import TheHeader from '@/components/front/TheHeader.vue'
import TheFooter from '@/components/front/TheFooter.vue'
import { useConfigStore } from '@/store/config'

const configStore = useConfigStore()

onMounted(() => {
  configStore.load() // 拉取站点配置（站点名/描述/页脚等文案）
})
</script>

<template>
  <div class="front-layout">
    <!-- 柔和渐变光斑背景层（固定不动，营造 Firefly 风格氛围） -->
    <div class="bg-layer" aria-hidden="true"></div>
    <TheHeader />
    <main class="main">
      <router-view />
    </main>
    <TheFooter />
  </div>
</template>

<style scoped>
.front-layout {
  position: relative;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--blog-bg);
  /* 用 clip 而非 hidden：hidden 会把 overflow-y 隐式变成 auto 而成为滚动容器，
     使后代 position: sticky（页头、文章目录）失效 */
  overflow-x: clip;
  transition: background-color 0.3s;
}
/* 多个径向渐变叠加成柔和光斑 */
.bg-layer {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  background:
    radial-gradient(46vw 46vw at 8% 2%, var(--blog-blob-1), transparent 62%),
    radial-gradient(40vw 40vw at 96% 6%, var(--blog-blob-2), transparent 64%),
    radial-gradient(52vw 52vw at 72% 86%, var(--blog-blob-3), transparent 66%),
    radial-gradient(34vw 34vw at 20% 70%, var(--blog-blob-2), transparent 68%);
  filter: blur(14px);
  opacity: 0.9;
}
.main {
  position: relative;
  z-index: 1;
  flex: 1;
  width: 100%;
  max-width: 1280px;
  margin: 0 auto;
  padding: 22px 16px 48px;
}
</style>