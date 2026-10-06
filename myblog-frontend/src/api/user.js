import request from './request'

/**
 * 登录
 * @param {{username: string, password: string}} data
 * @returns {Promise<{token: string, user: object}>}
 */
export function login(data) {
  return request.post('/auth/login', data)
}

// ===== 个人资料 =====
export function getProfile() {
  return request.get('/admin/profile')
}

export function getPublicProfile() {
  return request.get('/profile')
}

export function updateProfile(data) {
  return request.put('/admin/profile', data)
}

export function updatePassword(data) {
  return request.put('/admin/profile/password', data)
}