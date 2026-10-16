/**
 * 运维工具箱风险等级（与后端 tool_command.risk_level 对应）
 * 0 只读 / 1 变更 / 2 高危
 */
export const TOOL_RISK = [
  { value: 0, label: '只读', type: 'success' },
  { value: 1, label: '变更', type: 'warning' },
  { value: 2, label: '高危', type: 'danger' }
]

/** 风险中文名（未知值回退为「只读」） */
export function riskLabel(value) {
  return TOOL_RISK.find((r) => r.value === value)?.label || '只读'
}

/** 风险对应的 el-tag 类型（后台列表用） */
export function riskTagType(value) {
  return TOOL_RISK.find((r) => r.value === value)?.type || 'success'
}
