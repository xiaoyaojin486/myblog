/**
 * 项目进度（与后端 project.progress 对应）
 * 0 规划中 / 1 进行中 / 2 已完成 / 3 已暂停
 */
export const PROJECT_PROGRESS = [
  { value: 0, label: '规划中', type: 'info' },
  { value: 1, label: '进行中', type: 'primary' },
  { value: 2, label: '已完成', type: 'success' },
  { value: 3, label: '已暂停', type: 'warning' }
]

/** 进度中文名（未知值回退） */
export function progressLabel(value) {
  return PROJECT_PROGRESS.find((p) => p.value === value)?.label || '未设置'
}

/** 进度对应的 el-tag 类型（后台列表用） */
export function progressTagType(value) {
  return PROJECT_PROGRESS.find((p) => p.value === value)?.type || 'info'
}