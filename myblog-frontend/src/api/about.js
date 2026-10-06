import request from './request'

// ===== 前台 =====
export function getAbout() {
  return request.get('/about')
}

// ===== 后台管理 =====
export function getAdminAbout() {
  return request.get('/admin/about')
}

export function saveAbout(data) {
  return request.put('/admin/about', data)
}