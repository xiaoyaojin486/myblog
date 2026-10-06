import request from './request'

export function listTags(keyword) {
  return request.get('/tag/list', { params: { keyword } })
}

export function createTag(data) {
  return request.post('/admin/tag', data)
}

export function updateTag(id, data) {
  return request.put(`/admin/tag/${id}`, data)
}

export function deleteTag(id) {
  return request.delete(`/admin/tag/${id}`)
}