import { defineStore } from 'pinia'

/**
 * 用户登录状态（token + 用户信息）
 */
export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: null
  }),
  actions: {
    setLogin(token, user) {
      this.token = token
      this.user = user
      localStorage.setItem('token', token)
    },
    /** 合并更新用户信息（如个人资料保存后刷新头像/昵称） */
    setUser(user) {
      this.user = { ...this.user, ...user }
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('token')
    }
  }
})