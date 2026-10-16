import request from './request'

// ===== 前台 =====
/** 工具箱总览：启用分类 + 上架命令（一次返回，前端本地筛选） */
export function getToolboxOverview() {
  return request.get('/toolbox/overview')
}

// ===== 后台管理：命令 =====
export function pageAdminCommands(params) {
  return request.get('/admin/toolbox/page', { params })
}

export function getAdminCommand(id) {
  return request.get(`/admin/toolbox/${id}`)
}

export function createCommand(data) {
  return request.post('/admin/toolbox', data)
}

export function updateCommand(id, data) {
  return request.put(`/admin/toolbox/${id}`, data)
}

export function deleteCommand(id) {
  return request.delete(`/admin/toolbox/${id}`)
}

// ===== 后台管理：分类 =====
export function listAdminCategories() {
  return request.get('/admin/toolbox/category/list')
}

export function createCategory(data) {
  return request.post('/admin/toolbox/category', data)
}

export function updateCategory(id, data) {
  return request.put(`/admin/toolbox/category/${id}`, data)
}

export function deleteCategory(id) {
  return request.delete(`/admin/toolbox/category/${id}`)
}
