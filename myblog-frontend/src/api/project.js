import request from './request'

// ===== 前台展示 =====
/** 上架项目列表；progress 传入时按进度筛选（0规划中/1进行中/2已完成/3已暂停） */
export function listProjects(progress) {
  return request.get('/project/list', { params: { progress } })
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