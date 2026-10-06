import request from './request'

// ===== 前台 =====
export function getConfigAll() {
  return request.get('/config/all')
}

// ===== 后台管理 =====
export function listConfigs() {
  return request.get('/admin/config/list')
}

export function updateConfigs(data) {
  return request.put('/admin/config', data)
}