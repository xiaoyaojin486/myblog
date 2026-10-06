import { defineStore } from 'pinia'

/**
 * 主题状态（日夜切换）
 * 初始值优先级：localStorage → 系统偏好 prefers-color-scheme
 */
export const useThemeStore = defineStore('theme', {
  state: () => ({
    isDark: false
  }),
  actions: {
    initTheme() {
      const saved = localStorage.getItem('blog-theme')
      const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
      this.setDark(saved ? saved === 'dark' : prefersDark)
    },
    toggleTheme() {
      this.setDark(!this.isDark)
      localStorage.setItem('blog-theme', this.isDark ? 'dark' : 'light')
    },
    setDark(dark) {
      this.isDark = dark
      document.documentElement.classList.toggle('dark', dark)
    }
  }
})