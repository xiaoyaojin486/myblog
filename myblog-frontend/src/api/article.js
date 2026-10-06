import request from './request'

// ===== 后台管理 =====
export function pageAdminArticles(params) {
  return request.get('/admin/article/page', { params })
}

export function getAdminArticle(id) {
  return request.get(`/admin/article/${id}`)
}

export function createArticle(data) {
  return request.post('/admin/article', data)
}

export function updateArticle(id, data) {
  return request.put(`/admin/article/${id}`, data)
}

export function deleteArticle(id) {
  return request.delete(`/admin/article/${id}`)
}

export function publishArticle(id) {
  return request.put(`/admin/article/${id}/publish`)
}

export function unpublishArticle(id) {
  return request.put(`/admin/article/${id}/unpublish`)
}

// ===== 前台展示 =====
export function pageFrontArticles(params) {
  return request.get('/article/page', { params })
}

/** 全站搜索（关键字匹配标题/摘要） */
export function searchArticles(params) {
  return request.get('/article/search', { params })
}

export function getArticleDetail(id) {
  return request.get(`/article/${id}`)
}

export function latestArticles(limit = 6) {
  return request.get('/article/latest', { params: { limit } })
}

export function hotArticles(limit = 8, range = 'total') {
  return request.get('/article/hot', { params: { limit, range } })
}

export function likeArticle(id) {
  return request.post(`/article/${id}/like`)
}