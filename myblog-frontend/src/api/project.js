import request from './request'

// ===== 前台展示 =====
export function listProjects() {
  return request.get('/project/list')
}

export function getProject(id) {
  return request.get(`/project/${id}`)
}

// ===== 后台管理 =====
export function pageAdminProjects(params) {
  return request.get('/admin/project/page', { params })
}

export function getAdminProject(id) {
  return request.get(`/admin/project/${id}`)
}

export function createProject(data) {
  return request.post('/admin/project', data)
}

export function updateProject(id, data) {
  return request.put(`/admin/project/${id}`, data)
}

export function deleteProject(id) {
  return request.delete(`/admin/project/${id}`)
}