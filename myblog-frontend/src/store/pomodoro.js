import { defineStore } from 'pinia'
import dayjs from 'dayjs'
import {
  DEFAULT_CONFIG,
  DURATION_LIMIT,
  MODE_META,
  POMODORO_MODE,
  SESSION_KEEP_DAYS,
  STORAGE_KEY,
  formatClock
} from '@/constants/pomodoro'

/**
 * 番茄钟计时核心（纯前端）
 *
 * 设计要点：
 * 1. 计时不"每秒减一"，而是记住结束时间戳 endAt，每次都用 endAt - now 现算，
 *    这样浏览器切到后台被节流也不会累积误差。
 * 2. 状态写在 Pinia 里，页面切走/组件销毁不影响计时；组件每次挂载都能读到同一份状态。
 * 3. 每次动作后把状态抄进 localStorage，刷新或重开浏览器可恢复（含"离开期间已结束"的补记）。
 */

/** 模块级变量：不放进 state，避免被响应式代理 */
let ticker = null
let audioCtx = null

/** 统一的日期键（YYYY-MM-DD） */
function dateKey(ms = Date.now()) {
  return dayjs(ms).format('YYYY-MM-DD')
}

/** 数值收敛到区间内 */
function clamp(value, min, max) {
  const num = Number(value)
  if (!Number.isFinite(num)) return min
  return Math.min(max, Math.max(min, Math.round(num)))
}

/** 用 WebAudio 现场合三声短音，不需要额外的音频文件 */
function playBeep() {
  try {
    const Ctx = window.AudioContext || window.webkitAudioContext
    if (!Ctx) return
    if (!audioCtx) audioCtx = new Ctx()
    if (audioCtx.state === 'suspended') audioCtx.resume()
    ;[0, 0.18, 0.36].forEach((offset, index) => {
      const osc = audioCtx.createOscillator()
      const gain = audioCtx.createGain()
      const start = audioCtx.currentTime + offset
      osc.type = 'sine'
      osc.frequency.value = 660 + index * 110
      gain.gain.setValueAtTime(0.0001, start)
      gain.gain.exponentialRampToValueAtTime(0.18, start + 0.02)
      gain.gain.exponentialRampToValueAtTime(0.0001, start + 0.16)
      osc.connect(gain)
      gain.connect(audioCtx.destination)
      osc.start(start)
      osc.stop(start + 0.18)
    })
  } catch (e) {
    // 浏览器不支持或被音频策略拦截时静默降级
  }
}

export const usePomodoroStore = defineStore('pomodoro', {
  state: () => ({
    /** 是否已从 localStorage 恢复过（init 幂等标记） */
    initialized: false,
    /** 页面原始标题，用于倒计时结束后还原 */
    baseTitle: '',
    /** 时长配置（分钟） */
    config: { ...DEFAULT_CONFIG },
    /** 当前模式：focus / shortBreak / longBreak */
    mode: POMODORO_MODE.FOCUS,
    /** 计时状态：idle 待开始 / running 进行中 / paused 已暂停 */
    status: 'idle',
    /** 本次阶段总秒数 */
    totalSeconds: DEFAULT_CONFIG.focus * 60,
    /** 本次阶段剩余秒数 */
    remaining: DEFAULT_CONFIG.focus * 60,
    /** 结束时间戳（运行中有效） */
    endAt: 0,
    /** 本轮周期内已完成的专注个数 */
    round: 0,
    /** 当前任务备注 */
    task: '',
    /** 本地专注记录（仅专注阶段，保留最近 90 天） */
    sessions: [],
    /** 最近一次结束信息，供页面弹提示 */
    lastFinished: null
  }),

  getters: {
    modeMeta: (state) => MODE_META[state.mode] || MODE_META[POMODORO_MODE.FOCUS],
    /** mm:ss */
    displayTime: (state) => formatClock(state.remaining),
    /** 已完成比例 0~1，用于环形进度 */
    progress: (state) => {
      if (!state.totalSeconds) return 0
      return Math.min(1, Math.max(0, (state.totalSeconds - state.remaining) / state.totalSeconds))
    },
    /** 当前是周期内第几个番茄（休息阶段显示上一个已完成数） */
    roundInCycle: (state) => Math.min(state.round + 1, state.config.roundsBeforeLong),
    /** 今日完成的番茄数 */
    todayCount: (state) => state.sessions.filter((s) => s.date === dateKey()).length,
    /** 今日专注分钟数 */
    todayMinutes: (state) =>
      state.sessions
        .filter((s) => s.date === dateKey())
        .reduce((sum, s) => sum + (s.minutes || 0), 0),
    /** 连续专注天数（今天没记录时从昨天往前算） */
    streak: (state) => {
      const dates = new Set(state.sessions.map((s) => s.date))
      let cursor = dayjs()
      if (!dates.has(cursor.format('YYYY-MM-DD'))) cursor = cursor.subtract(1, 'day')
      let count = 0
      while (dates.has(cursor.format('YYYY-MM-DD'))) {
        count += 1
        cursor = cursor.subtract(1, 'day')
      }
      return count
    },
    /** 近 7 天柱状数据 */
    last7Days: (state) => {
      const days = []
      for (let i = 6; i >= 0; i -= 1) {
        const day = dayjs().subtract(i, 'day')
        const date = day.format('YYYY-MM-DD')
        const list = state.sessions.filter((s) => s.date === date)
        days.push({
          date,
          label: day.format('MM-DD'),
          count: list.length,
          minutes: list.reduce((sum, s) => sum + (s.minutes || 0), 0)
        })
      }
      return days
    }
  },

  actions: {
    /** 初始化：从 localStorage 恢复状态（幂等，可被任意组件重复调用） */
    init() {
      if (this.initialized) return
      this.initialized = true
      this.baseTitle = document.title

      const saved = this.readStorage()
      if (saved) {
        this.config = { ...DEFAULT_CONFIG, ...(saved.config || {}) }
        this.sessions = Array.isArray(saved.sessions) ? saved.sessions : []
        this.round = Number(saved.round) || 0
        this.task = typeof saved.task === 'string' ? saved.task : ''
        const savedMode = MODE_META[saved.mode] ? saved.mode : POMODORO_MODE.FOCUS
        const timer = saved.timer || {}

        if (timer.status === 'running' && timer.endAt) {
          // 关掉页面时还在计时：用旧的结束时间戳继续算
          this.mode = savedMode
          this.totalSeconds = Number(timer.totalSeconds) || this.config[savedMode] * 60
          this.endAt = Number(timer.endAt)
          this.remaining = Math.max(0, Math.ceil((this.endAt - Date.now()) / 1000))
          this.status = 'running'
          if (this.remaining <= 0) {
            // 离开期间已经结束，补记一条完成记录
            this.complete()
          } else {
            this.ensureTicker()
          }
        } else if (timer.status === 'paused') {
          this.mode = savedMode
          this.totalSeconds = Number(timer.totalSeconds) || this.config[savedMode] * 60
          this.remaining = Math.min(
            this.totalSeconds,
            Math.max(0, Number(timer.remaining) || 0)
          )
          this.status = 'paused'
        } else {
          this.applyMode(savedMode)
        }
      } else {
        this.totalSeconds = this.config[this.mode] * 60
        this.remaining = this.totalSeconds
      }

      this.pruneSessions()
      this.syncTitle()
      // 切回页面时立即对表，抹平后台节流造成的误差
      document.addEventListener('visibilitychange', this.handleVisibility)
    },

    /** 开始 / 继续 */
    start() {
      if (this.status === 'running') return
      if (this.status === 'paused') {
        this.endAt = Date.now() + this.remaining * 1000
      } else {
        this.totalSeconds = this.config[this.mode] * 60
        this.remaining = this.totalSeconds
        this.endAt = Date.now() + this.totalSeconds * 1000
      }
      this.status = 'running'
      this.ensureTicker()
      this.syncTitle()
      this.persist()
    },

    /** 暂停：把剩余秒数存下来，清掉结束时间戳 */
    pause() {
      if (this.status !== 'running') return
      this.remaining = Math.max(0, Math.ceil((this.endAt - Date.now()) / 1000))
      this.status = 'paused'
      this.endAt = 0
      this.stopTicker()
      this.syncTitle()
      this.persist()
    },

    /** 重置当前阶段 */
    reset() {
      this.stopTicker()
      this.status = 'idle'
      this.endAt = 0
      this.totalSeconds = this.config[this.mode] * 60
      this.remaining = this.totalSeconds
      this.syncTitle()
      this.persist()
    },

    /** 跳过当前阶段（跳过的专注不计数） */
    skip() {
      this.stopTicker()
      this.status = 'idle'
      this.endAt = 0
      if (this.mode === POMODORO_MODE.FOCUS) {
        const needLong = this.round > 0 && this.round % this.config.roundsBeforeLong === 0
        this.applyMode(needLong ? POMODORO_MODE.LONG_BREAK : POMODORO_MODE.SHORT_BREAK)
      } else {
        if (this.mode === POMODORO_MODE.LONG_BREAK) this.round = 0
        this.applyMode(POMODORO_MODE.FOCUS)
      }
      this.syncTitle()
      this.persist()
    },

    /** 切换模式（仅未开始时可切） */
    setMode(mode) {
      if (this.status === 'running' || !MODE_META[mode]) return
      this.applyMode(mode)
      this.syncTitle()
      this.persist()
    },

    /** 内部：应用某模式并重置时长 */
    applyMode(mode) {
      this.mode = mode
      this.status = 'idle'
      this.endAt = 0
      this.totalSeconds = this.config[mode] * 60
      this.remaining = this.totalSeconds
      this.persist()
    },

    /** 修改配置：分钟数收敛到 1~180，长休间隔 2~12 */
    setConfig(patch = {}) {
      const next = { ...this.config }
      Object.keys(patch).forEach((key) => {
        if (key === 'roundsBeforeLong') next[key] = clamp(patch[key], 2, 12)
        else if (key in next) next[key] = clamp(patch[key], DURATION_LIMIT.min, DURATION_LIMIT.max)
      })
      this.config = next
      // 周期变短后，已完成数超界则重新开始计数
      if (this.round >= next.roundsBeforeLong) this.round = 0
      if (this.status === 'idle') {
        this.totalSeconds = next[this.mode] * 60
        this.remaining = this.totalSeconds
      }
      this.persist()
    },

    /** 每 250ms 校一次表；只有秒数变化才更新 UI */
    tick() {
      if (this.status !== 'running') return
      const left = Math.max(0, Math.ceil((this.endAt - Date.now()) / 1000))
      if (left !== this.remaining) {
        this.remaining = left
        this.syncTitle()
      }
      if (left <= 0) this.complete()
    },

    /** 一个阶段自然结束 */
    complete() {
      const finishedMode = this.mode
      const endedAtMs = this.endAt || Date.now()
      const minutes = Math.round(this.totalSeconds / 60)

      this.stopTicker()
      this.status = 'idle'
      this.endAt = 0
      this.remaining = 0

      if (finishedMode === POMODORO_MODE.FOCUS) {
        this.round += 1
        this.recordSession(minutes, endedAtMs)
      }
      this.lastFinished = { mode: finishedMode, minutes, at: Date.now() }

      // 自动切到下一阶段：专注结束进休息，休息结束回专注
      if (finishedMode === POMODORO_MODE.FOCUS) {
        const needLong = this.round % this.config.roundsBeforeLong === 0
        this.applyMode(needLong ? POMODORO_MODE.LONG_BREAK : POMODORO_MODE.SHORT_BREAK)
      } else {
        if (finishedMode === POMODORO_MODE.LONG_BREAK) this.round = 0
        this.applyMode(POMODORO_MODE.FOCUS)
      }

      this.syncTitle()
      this.notify(finishedMode, minutes)
      playBeep()
    },

    ensureTicker() {
      if (ticker) return
      ticker = setInterval(() => this.tick(), 250)
    },

    stopTicker() {
      if (!ticker) return
      clearInterval(ticker)
      ticker = null
    },

    /** 浏览器从后台切回前台时立刻对表 */
    handleVisibility() {
      if (!document.hidden) this.tick()
    },

    /** 请求桌面通知权限（失败静默，页面内仍有提示兜底） */
    requestNotificationPermission() {
      if (typeof Notification === 'undefined' || Notification.permission !== 'default') return
      try {
        const result = Notification.requestPermission()
        if (result && typeof result.catch === 'function') result.catch(() => {})
      } catch (e) {
        // 非安全上下文等场景不支持，忽略
      }
    },

    notify(mode, minutes) {
      if (typeof Notification === 'undefined' || Notification.permission !== 'granted') return
      const isFocus = mode === POMODORO_MODE.FOCUS
      const title = isFocus ? '专注完成' : '休息结束'
      const body = isFocus
        ? `完成了 ${minutes} 分钟专注，去休息一下。`
        : '休息结束，开始下一轮专注吧。'
      try {
        // 同一个 tag 复用同一条通知，避免堆积
        new Notification(title, { body, tag: 'myblog-pomodoro' })
      } catch (e) {
        // 部分浏览器在非安全上下文下会抛错，忽略
      }
    },

    /** 记一条专注记录（只记专注阶段） */
    recordSession(minutes, endedAtMs = Date.now()) {
      this.sessions.push({
        id: `${endedAtMs}-${Math.random().toString(36).slice(2, 7)}`,
        date: dateKey(endedAtMs),
        endedAt: dayjs(endedAtMs).format('YYYY-MM-DD HH:mm:ss'),
        minutes,
        task: (this.task || '').trim(),
        completed: true
      })
    },

    /** 只保留最近 N 天的记录，防止 localStorage 无限膨胀 */
    pruneSessions() {
      const limit = dayjs().subtract(SESSION_KEEP_DAYS, 'day').format('YYYY-MM-DD')
      this.sessions = this.sessions.filter((s) => s.date >= limit)
    },

    /** 标题栏倒计时，切到别的页面也能一眼看到剩余时间 */
    syncTitle() {
      const base = this.baseTitle || '我的博客'
      if (this.status === 'running') {
        document.title = `${formatClock(this.remaining)} · ${MODE_META[this.mode].label} | ${base}`
      } else if (this.status === 'paused') {
        document.title = `已暂停 ${formatClock(this.remaining)} | ${base}`
      } else {
        document.title = base
      }
    },

    readStorage() {
      try {
        const raw = localStorage.getItem(STORAGE_KEY)
        return raw ? JSON.parse(raw) : null
      } catch (e) {
        return null
      }
    },

    /** 抄一份状态到本地草稿箱 */
    persist() {
      try {
        localStorage.setItem(
          STORAGE_KEY,
          JSON.stringify({
            config: this.config,
            mode: this.mode,
            round: this.round,
            task: this.task,
            sessions: this.sessions,
            timer: {
              status: this.status,
              totalSeconds: this.totalSeconds,
              remaining: this.remaining,
              endAt: this.endAt
            }
          })
        )
      } catch (e) {
        // localStorage 不可用（隐私模式等）时静默降级，功能仍可在当前会话使用
      }
    }
  }
})
