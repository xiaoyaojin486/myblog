/**
 * 番茄钟常量：模式、默认时长、本地存储 key
 * 纯前端实现，数据只存在浏览器 localStorage，不涉及后端
 */

/** 三种计时模式 */
export const POMODORO_MODE = {
  FOCUS: 'focus',
  SHORT_BREAK: 'shortBreak',
  LONG_BREAK: 'longBreak'
}

/** 模式元信息：完整名 / 短名 / 强调色（取主题变量，自动适配日夜模式） */
export const MODE_META = {
  [POMODORO_MODE.FOCUS]: { label: '专注', short: '专注', color: 'var(--blog-primary)' },
  [POMODORO_MODE.SHORT_BREAK]: { label: '短休息', short: '短休', color: 'var(--blog-success)' },
  [POMODORO_MODE.LONG_BREAK]: { label: '长休息', short: '长休', color: 'var(--blog-warning)' }
}

/** 默认配置（分钟） */
export const DEFAULT_CONFIG = {
  focus: 25,
  shortBreak: 5,
  longBreak: 15,
  roundsBeforeLong: 4
}

/** 时长可调范围 */
export const DURATION_LIMIT = {
  min: 1,
  max: 180
}

/** localStorage 键与本地记录保留天数 */
export const STORAGE_KEY = 'myblog-pomodoro'
export const SESSION_KEEP_DAYS = 90

/** 秒数格式化为 mm:ss（超过 99 分钟时自然变成 100:00） */
export function formatClock(totalSeconds) {
  const seconds = Math.max(0, Math.floor(totalSeconds))
  const minutes = Math.floor(seconds / 60)
  const rest = seconds % 60
  return `${String(minutes).padStart(2, '0')}:${String(rest).padStart(2, '0')}`
}
