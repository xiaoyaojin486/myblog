<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { VideoPlay, VideoPause, Timer } from '@element-plus/icons-vue'
import { usePomodoroStore } from '@/store/pomodoro'

const store = usePomodoroStore()
const route = useRoute()
const router = useRouter()

/** 只在"正在计时/暂停"且不在番茄钟页面时显示，避免与主页面重复 */
const visible = computed(() => store.status !== 'idle' && route.path !== '/pomodoro')

function toggle() {
  if (store.status === 'running') store.pause()
  else store.start()
}

function goDetail() {
  router.push('/pomodoro')
}
</script>

<template>
  <transition name="mini-fade">
    <div
      v-if="visible"
      class="pomodoro-mini"
      :style="{ '--mini-color': store.modeMeta.color }"
      role="button"
      tabindex="0"
      title="打开番茄钟"
      @click="goDetail"
      @keyup.enter="goDetail"
    >
      <div class="mini-left">
        <span class="mini-dot" :class="{ paused: store.status === 'paused' }"></span>
        <div class="mini-info">
          <span class="mini-mode">{{ store.modeMeta.short }}</span>
          <span class="mini-time">{{ store.displayTime }}</span>
        </div>
      </div>
      <div class="mini-actions">
        <button
          type="button"
          class="mini-btn"
          :title="store.status === 'running' ? '暂停' : '继续'"
          @click.stop="toggle"
        >
          <el-icon>
            <VideoPause v-if="store.status === 'running'" />
            <VideoPlay v-else />
          </el-icon>
        </button>
        <button type="button" class="mini-btn" title="打开番茄钟" @click.stop="goDetail">
          <el-icon><Timer /></el-icon>
        </button>
      </div>
      <div class="mini-progress" :style="{ width: `${store.progress * 100}%` }"></div>
    </div>
  </transition>
</template>

<style scoped>
.pomodoro-mini {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 40;
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 176px;
  padding: 10px 12px 13px;
  border: 1px solid var(--blog-border);
  border-radius: var(--blog-radius);
  background: var(--blog-card-bg);
  box-shadow: var(--blog-shadow-hover);
  backdrop-filter: blur(var(--blog-glass-blur));
  -webkit-backdrop-filter: blur(var(--blog-glass-blur));
  cursor: pointer;
  overflow: hidden;
  transition: transform 0.2s, box-shadow 0.2s;
}
.pomodoro-mini:hover {
  transform: translateY(-2px);
}
.mini-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
}
.mini-dot {
  width: 8px;
  height: 8px;
  flex: 0 0 auto;
  border-radius: 50%;
  background: var(--mini-color);
  animation: mini-pulse 1.6s ease-in-out infinite;
}
.mini-dot.paused {
  animation: none;
  opacity: 0.5;
}
@keyframes mini-pulse {
  0%,
  100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.4;
    transform: scale(0.75);
  }
}
.mini-info {
  display: flex;
  align-items: baseline;
  gap: 8px;
  min-width: 0;
}
.mini-mode {
  font-size: 12px;
  color: var(--blog-text-light);
  white-space: nowrap;
}
.mini-time {
  font-size: 18px;
  font-weight: 700;
  color: var(--blog-text);
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
}
.mini-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 0 0 auto;
}
.mini-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  border: none;
  border-radius: 50%;
  background: var(--blog-surface-2);
  color: var(--blog-text-light);
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.2s, color 0.2s;
}
.mini-btn:hover {
  background: var(--blog-primary-soft);
  color: var(--blog-primary);
}
.mini-progress {
  position: absolute;
  left: 0;
  bottom: 0;
  height: 3px;
  background: var(--mini-color);
  transition: width 0.3s linear;
}

.mini-fade-enter-active,
.mini-fade-leave-active {
  transition: opacity 0.2s, transform 0.2s;
}
.mini-fade-enter-from,
.mini-fade-leave-to {
  opacity: 0;
  transform: translateY(8px);
}

@media (max-width: 640px) {
  .pomodoro-mini {
    right: 12px;
    bottom: 12px;
    min-width: 160px;
  }
  .mini-time {
    font-size: 16px;
  }
}
</style>
