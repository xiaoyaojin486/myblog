import request from './request'

// ===== 前台 =====
export function listComments(articleId) {
  return request.get('/comment/list', { params: { articleId } })
}

export function createComment(data) {
  return request.post('/comment', data)
}

// ===== 后台管理 =====
export function pageAdminComments(params) {
  return request.get('/admin/comment/page', { params })
}

export function approveComment(id) {
  return request.put(`/admin/comment/${id}/approve`)
}

export function rejectComment(id) {
  return request.put(`/admin/comment/${id}/reject`)
}

/** 博主回复（以博主身份发布，直接通过审核） */
export function replyComment(id, content) {
  return request.post(`/admin/comment/${id}/reply`, { content })
}

/** 批量改状态：status 1 通过 / 2 拒绝 */
export function updateCommentsStatus(ids, status) {
  return request.put('/admin/comment/batch/status', { ids, status })
}

export function deleteComment(id) {
  return request.delete(`/admin/comment/${id}`)
}

/** 批量删除 */
export function deleteComments(ids) {
  return request.delete('/admin/comment/batch', { data: ids })
}