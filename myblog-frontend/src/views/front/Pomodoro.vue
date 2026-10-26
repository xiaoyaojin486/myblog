<script setup>
import { computed, watch } from 'vue'
import {
  VideoPlay,
  VideoPause,
  RefreshLeft,
  DArrowRight
} from '@element-plus/icons-vue'
import { usePomodoroStore } from '@/store/pomodoro'
import { POMODORO_MODE } from '@/constants/pomodoro'

const store = usePomodoroStore()
store.init()

/** 环形进度圆半径 / 周长 */
const RING_RADIUS = 96
const RING_CIRCUMFERENCE = 2 * Math.PI * RING_RADIUS

const ringOffset = computed(() => RING_CIRCUMFERENCE * (1 - store.progress))

const isRunning = computed(() => store.status === 'running')
const isPaused = computed(() => store.status === 'paused')
const primaryLabel = computed(() => (isRunning.value ? '暂停' : isPaused.value ? '继续' : '开始'))

const statusText = computed(() => {
  if (isRunning.value) return '进行中'
  if (isPaused.value) return '已暂停'
  return '准备开始'
})

const modeList = [
  { key: POMODORO_MODE.FOCUS, label: '专注' },
  { key: POMODORO_MODE.SHORT_BREAK, label: '短休息' },
  { key: POMODORO_MODE.LONG_BREAK, label: '长休息' }
]

/** 本轮进度点：长休阶段全部点亮 */
const cycleFilled = computed(() => {
  const filled = store.round % store.config.roundsBeforeLong
  return filled === 0 && store.round > 0 ? store.config.roundsBeforeLong : filled
})

const durationItems = [
  { key: 'focus', label: '专注时长', unit: '分钟', min: 1, max: 180 },
  { key: 'shortBreak', label: '短休息', unit: '分钟', min: 1, max: 60 },
  { key: 'longBreak', label: '长休息', unit: '分钟', min: 1, max: 120 },
  { key: 'roundsBeforeLong', label: '长休间隔', unit: '个番茄', min: 2, max: 12 }
]

/** 近 7 天柱状图的最高值，用于算柱高 */
const maxDayCount = computed(() => Math.max(1, ...store.last7Days.map((d) => d.count)))

function barHeight(count) {
  if (!count) return '0%'
  return `${Math.max(8, Math.round((count / maxDayCount.value) * 100))}%`
}

/** 首次开始时顺带申请桌面通知权限 */
function handlePrimary() {
  if (isRunning.value) {
    store.pause()
    return
  }
  store.requestNotificationPermission()
  store.start()
}

// 阶段结束（专注/休息）时给页面内提示，通知权限未开也能看到
watch(
  () => store.lastFinished,
  (info) => {
    if (!info) return
    if (info.mode === POMODORO_MODE.FOCUS) {
      ElMessage.success(`完成 ${info.minutes} 分钟专注，休息一下`)
    } else {
      ElMessage.info('休息结束，开始下一轮专注')
    }
  }
)
</script>

<template>
  <div class="pomodoro">
    <!-- 页头 -->
    <div class="page-head">
      <div>
        <div class="eyebrow">FOCUS TIMER</div>
        <h2 class="title">番茄钟</h2>
        <p class="subtitle">
          专注 {{ store.config.focus }} 分钟，短休 {{ store.config.shortBreak }} 分钟，每
          {{ store.config.roundsBeforeLong }} 个番茄长休 {{ store.config.longBreak }} 分钟。
        </p>
      </div>
      <div class="status-badge" :class="store.status">
        <span class="dot"></span>
        {{ statusText }}
      </div>
    </div>

    <div class="main-grid">
      <!-- 左：表盘 + 控制 -->
      <section class="card timer-card">
        <div class="mode-tabs">
          <button
            v-for="m in modeList"
            :key="m.key"
            type="button"
            class="mode-tab"
            :class="{ active: store.mode === m.key }"
            :disabled="isRunning"
            @click="store.setMode(m.key)"
          >
            {{ m.label }}
          </button>
        </div>

        <div class="dial">
          <svg class="ring" viewBox="0 0 220 220" aria-hidden="true">
            <circle class="ring-track" cx="110" cy="110" :r="RING_RADIUS" />
            <circle
              class="ring-fill"
              cx="110"
              cy="110"
              :r="RING_RADIUS"
              :style="{
                strokeDasharray: RING_CIRCUMFERENCE,
                strokeDashoffset: ringOffset,
                stroke: store.modeMeta.color
              }"
            />
          </svg>
          <div class="dial-content">
            <div class="time">{{ store.displayTime }}</div>
            <div class="dial-label">{{ store.modeMeta.label }}</div>
            <div v-if="store.task" class="task-hint">{{ store.task }}</div>
          </div>
        </div>

        <div class="controls">
          <button type="button" class="ctrl primary" @click="handlePrimary">
            <el-icon>
              <VideoPause v-if="isRunning" />
              <VideoPlay v-else />
            </el-icon>
            {{ primaryLabel }}
          </button>
          <button
            type="button"
            class="ctrl"
            :disabled="store.status === 'idle'"
            @click="store.reset()"
          >
            <el-icon><RefreshLeft /></el-icon>
            重置
          </button>
          <button type="button" class="ctrl ghost" @click="store.skip()">
            <el-icon><DArrowRight /></el-icon>
            跳过
          </button>
        </div>
      </section>

      <!-- 右：节奏 / 任务 / 时长 -->
      <section class="card side-card">
        <h3 class="card-title">本轮节奏</h3>
        <div class="cycle">
          <div class="cycle-dots">
            <span
              v-for="i in store.config.roundsBeforeLong"
              :key="i"
              class="cycle-dot"
              :class="{ filled: i <= cycleFilled }"
            />
          </div>
          <span class="cycle-text">
            第 {{ store.roundInCycle }} / {{ store.config.roundsBeforeLong }} 个番茄
          </span>
        </div>

        <el-divider />

        <h3 class="card-title">本轮任务</h3>
        <el-input
          v-model="store.task"
          maxlength="50"
          show-word-limit
          placeholder="这一轮要专注什么？"
          @change="store.persist()"
        />

        <h3 class="card-title setting-title">时长设置</h3>
        <div v-for="item in durationItems" :key="item.key" class="setting-row">
          <span class="setting-label">{{ item.label }}</span>
          <div class="setting-control">
            <el-input-number
              :model-value="store.config[item.key]"
              :min="item.min"
              :max="item.max"
              size="small"
              controls-position="right"
              :disabled="store.status !== 'idle'"
              @change="(value) => store.setConfig({ [item.key]: value })"
            />
            <span class="setting-unit">{{ item.unit }}</span>
          </div>
        </div>
        <p class="tip">计时进行中不可修改时长与模式，请先暂停并重置。</p>
      </section>
    </div>

    <!-- 统计 -->
    <section class="card stats-card">
      <div class="stats-head">
        <div class="stat">
          <span class="stat-num">{{ store.todayCount }}</span>
          <span class="stat-label">今日番茄</span>
        </div>
        <div class="stat">
          <span class="stat-num">{{ store.todayMinutes }}</span>
          <span class="stat-label">今日专注 / 分钟</span>
        </div>
        <div class="stat">
          <span class="stat-num">{{ store.streak }}</span>
          <span class="stat-label">连续专注 / 天</span>
        </div>
      </div>

      <div class="chart-head">
        <h3 class="card-title">近 7 天</h3>
        <span class="chart-note">柱高为番茄数，悬停可看专注分钟</span>
      </div>
      <div class="chart">
        <div v-for="day in store.last7Days" :key="day.date" class="bar-col">
          <span class="bar-count">{{ day.count || '' }}</span>
          <div class="bar-track">
            <div
              class="bar-fill"
              :class="{ empty: !day.count }"
              :style="{ height: barHeight(day.count) }"
              :title="`${day.date}：${day.count} 个番茄，${day.minutes} 分钟`"
            />
          </div>
          <span class="bar-label">{{ day.label }}</span>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
/* ===== 页头（沿用工具箱页的视觉语言） ===== */
.page-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 18px;
  padding-left: 4px;
}
.eyebrow {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 1.5px;
  color: var(--blog-text-light);
}
.title {
  margin: 4px 0 6px;
  font-size: 24px;
  color: var(--blog-text);
}
.subtitle {
  font-size: 14px;
  color: var(--blog-text-light);
}
.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 7px 16px;
  border-radius: var(--blog-radius-pill);
  background: var(--blog-chip-bg);
  color: var(--blog-primary);
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
}
.status-badge .dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: currentColor;
}
.status-badge.running {
  background: var(--blog-success-soft);
  color: var(--blog-success);
}
.status-badge.running .dot {
  animation: pulse 1.6s ease-in-out infinite;
}
.status-badge.paused {
  background: var(--blog-warning-soft);
  color: var(--blog-warning);
}
@keyframes pulse {
  0%,
  100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.4;
    transform: scale(0.8);
  }
}

/* ===== 卡片通用 ===== */
.card {
  border: 1px solid var(--blog-border);
  border-radius: var(--blog-radius);
  background: var(--blog-card-bg);
  box-shadow: var(--blog-shadow);
}
.card-title {
  margin-bottom: 10px;
  font-size: 14px;
  font-weight: 600;
  color: var(--blog-text);
}
.main-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.05fr) minmax(0, 0.95fr);
  gap: 18px;
  align-items: start;
}

/* ===== 左：计时卡片 ===== */
.timer-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 20px 24px;
}
.mode-tabs {
  display: flex;
  gap: 6px;
  padding: 4px;
  border-radius: var(--blog-radius-pill);
  background: var(--blog-surface-2);
  border: 1px solid var(--blog-divider);
}
.mode-tab {
  padding: 7px 18px;
  border: none;
  border-radius: var(--blog-radius-pill);
  background: transparent;
  color: var(--blog-text-light);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s, color 0.2s;
}
.mode-tab:hover:not(:disabled) {
  color: var(--blog-primary);
}
.mode-tab.active {
  background: var(--blog-card-solid);
  color: var(--blog-primary);
  box-shadow: 0 2px 10px rgba(64, 96, 160, 0.12);
}
.mode-tab:disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

.dial {
  position: relative;
  width: 250px;
  height: 250px;
  margin: 26px auto 22px;
}
.ring {
  width: 100%;
  height: 100%;
}
.ring-track {
  fill: none;
  stroke: var(--blog-divider);
  stroke-width: 10;
}
.ring-fill {
  fill: none;
  stroke-width: 10;
  stroke-linecap: round;
  transform: rotate(-90deg);
  transform-origin: 50% 50%;
  transition: stroke-dashoffset 0.3s linear, stroke 0.3s;
}
.dial-content {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 0 24px;
  text-align: center;
}
.time {
  font-size: 46px;
  font-weight: 700;
  letter-spacing: 1px;
  font-variant-numeric: tabular-nums;
  color: var(--blog-text);
}
.dial-label {
  font-size: 13px;
  color: var(--blog-text-light);
}
.task-hint {
  max-width: 160px;
  margin-top: 4px;
  font-size: 12px;
  color: var(--blog-text-light);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.controls {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: center;
}
.ctrl {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  border: 1px solid var(--blog-divider);
  border-radius: var(--blog-radius-pill);
  background: var(--blog-surface-2);
  color: var(--blog-text);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s, color 0.2s, box-shadow 0.2s, transform 0.2s;
}
.ctrl:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: var(--blog-shadow);
}
.ctrl:disabled {
  cursor: not-allowed;
  opacity: 0.45;
}
.ctrl.primary {
  min-width: 120px;
  justify-content: center;
  border-color: transparent;
  background: linear-gradient(120deg, var(--blog-primary), #a78bfa);
  color: #fff;
}
.ctrl.ghost {
  background: transparent;
  color: var(--blog-text-light);
}

/* ===== 右：设置卡片 ===== */
.side-card {
  padding: 20px;
}
.cycle {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}
.cycle-dots {
  display: flex;
  gap: 6px;
}
.cycle-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: var(--blog-divider);
  transition: background-color 0.25s, transform 0.25s;
}
.cycle-dot.filled {
  background: var(--blog-primary);
  transform: scale(1.08);
}
.cycle-text {
  font-size: 13px;
  color: var(--blog-text-light);
}
.setting-title {
  margin-top: 18px;
}
.setting-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}
.setting-label {
  font-size: 13px;
  color: var(--blog-text);
}
.setting-control {
  display: flex;
  align-items: center;
  gap: 8px;
}
.setting-unit {
  min-width: 46px;
  font-size: 12px;
  color: var(--blog-text-light);
}
.tip {
  margin-top: 6px;
  font-size: 12px;
  color: var(--blog-text-light);
}

/* ===== 统计卡片 ===== */
.stats-card {
  margin-top: 18px;
  padding: 20px;
}
.stats-head {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 18px;
}
.stat {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 14px 16px;
  border-radius: var(--blog-radius-sm);
  background: var(--blog-surface-2);
  border: 1px solid var(--blog-divider);
}
.stat-num {
  font-size: 26px;
  font-weight: 700;
  color: var(--blog-primary);
  font-variant-numeric: tabular-nums;
}
.stat-label {
  font-size: 12px;
  color: var(--blog-text-light);
}
.chart-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}
.chart-note {
  font-size: 12px;
  color: var(--blog-text-light);
}
.chart {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: 10px;
  margin-top: 8px;
}
.bar-col {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}
.bar-count {
  height: 14px;
  font-size: 12px;
  font-weight: 600;
  color: var(--blog-primary);
  font-variant-numeric: tabular-nums;
}
.bar-track {
  position: relative;
  width: 100%;
  height: 96px;
  border-radius: var(--blog-radius-sm);
  background: var(--blog-surface-2);
  overflow: hidden;
}
.bar-fill {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: var(--blog-radius-sm) var(--blog-radius-sm) 0 0;
  background: linear-gradient(180deg, var(--blog-primary), rgba(167, 139, 250, 0.85));
  transition: height 0.4s ease;
}
.bar-fill.empty {
  background: transparent;
}
.bar-label {
  font-size: 12px;
  color: var(--blog-text-light);
}

/* ===== 响应式 ===== */
@media (max-width: 860px) {
  .main-grid {
    grid-template-columns: minmax(0, 1fr);
  }
  .dial {
    width: 220px;
    height: 220px;
    margin: 20px auto;
  }
  .time {
    font-size: 40px;
  }
  .stats-head {
    grid-template-columns: minmax(0, 1fr);
  }
  .bar-track {
    height: 72px;
  }
}
</style>
