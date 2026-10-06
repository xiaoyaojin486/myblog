import { defineStore } from 'pinia'
import { getConfigAll } from '@/api/config'

/**
 * 站点配置状态（站点名/描述/头像/页脚文案等，前台全局共享）
 * FrontLayout 挂载时拉取一次，后台设置保存后可 force 刷新
 */
export const useConfigStore = defineStore('config', {
  state: () => ({
    config: {},
    loaded: false
  }),
  getters: {
    siteName: (state) => state.config.site_name || '我的博客',
    siteDescription: (state) => state.config.site_description || '记录技术与生活'
  },
  actions: {
    async load(force = false) {
      if (this.loaded && !force) return
      try {
        this.config = await getConfigAll()
        this.loaded = true
      } catch (e) {
        // 静默失败：前台使用默认文案兜底
      }
    }
  }
})